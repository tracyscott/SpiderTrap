// KinectOpenCV.cpp
// 
// C++ based integration between Azure Kinect SDK and Open CV in an
// attempt to use blob detection on the depth image.
// 
//
#include <osc/OscOutboundPacketStream.h>
#include <ip/UdpSocket.h>
#include <osc/OscReceivedElements.h>
#include <osc/OscPacketListener.h>
#include <ip/IpEndpointName.h>
//#include <Poco/Exception.h>

#include <thread>
#include <iostream>
#include <k4a/k4a.hpp>
#include <k4arecord/record.h>
#include <k4arecord/playback.h>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/features2d.hpp>

#pragma comment(lib, "ws2_32")
#pragma comment(lib, "winmm")

using namespace osc;

// oscpack parameters
#define OSC_SEND_ADDRESS "127.0.0.1"
#define OSC_SEND_PORT 7979
#define OSC_RECV_PORT 7978
#define OSC_OUTPUT_BUFFER_SIZE 1024

using DepthPixel = uint16_t;
//static cv::Mat depth_to_opencv(const k4a::image& im);
void ColorizeDepthImage(const k4a::image& depthImage, std::pair<uint16_t, uint16_t> expectedValueRange, std::vector<uint8_t>* buffer);
uint8_t ColorizeGreyscale(const DepthPixel& value, const DepthPixel& min, const DepthPixel& max);
std::pair<uint16_t, uint16_t> GetDepthModeRange(const k4a_depth_mode_t depthMode);
cv::Mat depthbuffer_to_opencv(int width, int height, void* buffer, size_t stride);
static k4a_result_t depth_2d_to_3d(const k4a_calibration_t* calibration, float x, float y, float z, k4a_float3_t* pos, int* valid);
void parseArgs(int argc, char** argv);
void runServer();


float depthFilterMin = 3.0;
float depthFilterMax = 4.5;
int rightFilter = 100;    // ignore pixels to the right of this
int leftFilter = 100;     // ignore pixels to the left of this
int topFilter = 10;      // ignore pixels above this
int bottomFilter = 200;   // ignore pixels below this
float fps = 10.0;
bool finished = false;
int blobMinArea = 10;
bool verbose = false;
bool preview = false;

std::mutex oscMutex;

const float FEET_PER_METER = 3.28084f;
// We convert the config params into mm and store them here so we only need to mutex
// once per frame.
uint16_t minDepthMm = (uint16_t)(depthFilterMin * 1000 / FEET_PER_METER);
uint16_t maxDepthMm = (uint16_t)(depthFilterMax * 1000 / FEET_PER_METER);
// Store these once per frame since they are referenced many times per frame.
int rightFilterLocal = rightFilter;
int leftFilterLocal = leftFilter;
int topFilterLocal = topFilter;
int bottomFilterLocal = bottomFilter;


int main(int argc, char** argv)
{
    k4a_device_t device = NULL;
    const int32_t TIMEOUT_IN_MS = 1000;
    k4a_capture_t capture = NULL;
    int returnCode = 1;
    int captureFrameCount;
    size_t serial_size = 0;
    uint32_t count;
    char* serial;

    printf("Starting KinectBlob, attempting to parse args.\n");
    //fflush(stdout);
    parseArgs(argc, argv);

    // oscpack requires this to be called on the main thread when the receiver is on another thread.
    NetworkInitializer networkInit;

    // Start the osc listener server on a separate thread
    std::thread serverThread(runServer);

    UdpTransmitSocket transmitSocket(IpEndpointName(OSC_SEND_ADDRESS, OSC_SEND_PORT));
    // OSC sending buffer.
    char buffer[OSC_OUTPUT_BUFFER_SIZE];
    
    captureFrameCount = 3000;

    printf("Starting KinectBlob, attempting to get device count.\n");
    fflush(stdout);

    //uint32_t count = k4a_device_get_installed_count();
    count = k4a_device_get_installed_count();
    std::cout << "device count: " << count << "\n";
    
    // Open the first plugged in Kinect device.  Be sure to close the device
    // before exiting.
    if (K4A_FAILED(k4a_device_open(K4A_DEVICE_DEFAULT, &device))) {
        printf("Failed to open k4a device!");
        fflush(stdout);
        return 1;
    }

    // Get the size of the serial number
    k4a_device_get_serialnum(device, NULL, &serial_size);

    // Allocate memory for the serial, then acquire it
    serial = (char*)(malloc(serial_size));
    k4a_device_get_serialnum(device, serial, &serial_size);
    printf("Opened device: %s\n", serial);
    free(serial);

    k4a_device_configuration_t config = K4A_DEVICE_CONFIG_INIT_DISABLE_ALL;
    config.camera_fps = K4A_FRAMES_PER_SECOND_15;
    config.color_format = K4A_IMAGE_FORMAT_COLOR_MJPG;
    config.color_resolution = K4A_COLOR_RESOLUTION_OFF;
    config.depth_mode = K4A_DEPTH_MODE_WFOV_2X2BINNED;

    k4a_calibration_t calibration;
    if (K4A_RESULT_SUCCEEDED !=
        k4a_device_get_calibration(device, config.depth_mode, config.color_resolution, &calibration))
    {
        printf("Failed to get calibration\n");
        fflush(stdout);
        goto Exit;
    }

    // Start the camera with the given configuration.
    if (K4A_FAILED(k4a_device_start_cameras(device, &config))) {
        printf("Failed to start camera");
        fflush(stdout);
        k4a_device_close(device);
        return 1;
    }

    //... Camera capture and application specific code would go here ...
    while (!finished) {
        k4a_image_t image;

        // Lock the mutex and cache these values for the entire frame processing.  We do this once so that we aren't excessively grabbing the mutex.
        oscMutex.lock();
        uint16_t minDepthMm = (uint16_t)(depthFilterMin * 1000 / FEET_PER_METER);
        uint16_t maxDepthMm = (uint16_t)(depthFilterMax * 1000 / FEET_PER_METER);
        rightFilterLocal = rightFilter;
        leftFilterLocal = leftFilter;
        topFilterLocal = topFilter;
        bottomFilterLocal = bottomFilter;
        bool verboseLocal = verbose;
        bool previewLocal = preview;
        float fpsLocal = fps;
        oscMutex.unlock();

        // Get a depth frame
        switch (k4a_device_get_capture(device, &capture, TIMEOUT_IN_MS))
        {
        case K4A_WAIT_RESULT_SUCCEEDED:
            break;
        case K4A_WAIT_RESULT_TIMEOUT:
            printf("Timed out waiting for a capture\n");
            continue;
            break;
        case K4A_WAIT_RESULT_FAILED:
            printf("Failed to read a capture\n");
            if (device != NULL) k4a_device_close(device);
            return returnCode;
        }

        // Probe for a depth image
        image = k4a_capture_get_depth_image(capture);
        fflush(stdout);

        k4a::image dImg = k4a::image(image);
        std::vector<uint8_t> imgBuffer;
        ColorizeDepthImage(dImg,
            GetDepthModeRange(config.depth_mode),
            &imgBuffer);
        
        cv::Mat cvDepth = depthbuffer_to_opencv(dImg.get_width_pixels(), dImg.get_height_pixels(), &imgBuffer[0], cv::Mat::AUTO_STEP);

        // Set up the detector with default parameters
        cv::SimpleBlobDetector::Params params;

        // Change these as needed for your specific requirements
        params.filterByArea = true;
        params.minThreshold = 0;
        params.maxThreshold = 250;
        params.filterByArea = true;
        params.minArea = blobMinArea;
        params.filterByCircularity = false;
        params.filterByConvexity = false;
        params.filterByInertia = false;
        params.filterByColor = false;

        cv::Ptr<cv::SimpleBlobDetector> detector = cv::SimpleBlobDetector::create(params);

        cvDepth = 255 - cvDepth;

        // Detect the blobs
        std::vector<cv::KeyPoint> keypoints;
        detector->detect(cvDepth, keypoints);

        cv::Mat img_with_keypoints;

       
        if (previewLocal) drawKeypoints(cvDepth, keypoints, img_with_keypoints, cv::Scalar(0, 0, 255), cv::DrawMatchesFlags::DRAW_RICH_KEYPOINTS);


        // Draw bounding boxes around each blob
        for (auto& keypoint : keypoints) {
            
            if (previewLocal) {
                cv::Rect2d box(keypoint.pt.x - keypoint.size / 2, keypoint.pt.y - keypoint.size / 2, keypoint.size, keypoint.size);
                rectangle(img_with_keypoints, box, cv::Scalar(255, 0, 0), 2);
            }

            k4a_float3_t pos3d;
            uint16_t* depth_data = (uint16_t*)(void*)k4a_image_get_buffer(image);
            int index = (int)(keypoint.pt.x * k4a_image_get_width_pixels(image) + keypoint.pt.y);
            float z = (float)depth_data[index];
            int valid;
            if (K4A_RESULT_SUCCEEDED != depth_2d_to_3d(&calibration, keypoint.pt.x, keypoint.pt.y, z, &pos3d, &valid))
                printf("depth_2d_to_3d FAILED!");
            
            if (valid) {
                if (pos3d.xyz.z > 0.05) {
                    // Eventually, we will send all detected blobs as one bundle but first lets get something working.
                    osc::OutboundPacketStream p(buffer, OSC_OUTPUT_BUFFER_SIZE);

                    p << osc::BeginBundleImmediate
                        << osc::BeginMessage("/spidertrap/blob")
                        << pos3d.xyz.x << pos3d.xyz.y << pos3d.xyz.z << osc::EndMessage
                        << osc::EndBundle;

                    transmitSocket.Send(p.Data(), p.Size());
                    if (verboseLocal) {
                        printf("OSC Found keypoint: %f,%f size=%f\n", keypoint.pt.x, keypoint.pt.y, keypoint.size);
                        printf("OSC World coodinates %.2f, %.2f, %.2f\n", pos3d.xyz.x, pos3d.xyz.y, pos3d.xyz.z);
                    }
                }
                else {
                    if (verboseLocal) {
                        printf("Z less thatn 0.05!  BUT Found keypoint: %f,%f size=%f\n", keypoint.pt.x, keypoint.pt.y, keypoint.size);
                        printf("World coodinates %.2f, %.2f, %.2f\n", pos3d.xyz.x, pos3d.xyz.y, pos3d.xyz.z);
                    }
                }
            }
            else {
                if (verboseLocal) {
                    printf("Invalid world pos BUT Found keypoint: %f,%f size=%f\n", keypoint.pt.x, keypoint.pt.y, keypoint.size);
                }
            }
        }

        if (previewLocal) {
            cv::namedWindow("Image Window", cv::WINDOW_NORMAL);
            cv::imshow("Image Window", img_with_keypoints);
            cv::waitKey(1000.0 / fpsLocal);
        }
        else
            std::this_thread::sleep_for(std::chrono::milliseconds((long)(1000.0/fpsLocal)));
        
        k4a_capture_release(capture);
    }

    returnCode = 0;
    k4a_device_stop_cameras(device);

Exit:

    if (device != NULL) k4a_device_close(device);
}


static cv::Mat depthbuffer_to_opencv(int width, int height, void *buffer, size_t stride) {
    return cv::Mat(width, height, CV_8U, buffer, stride);
}

void ColorizeDepthImage(const k4a::image& depthImage,
    std::pair<uint16_t, uint16_t> expectedValueRange,
    std::vector<uint8_t>* buffer)
{
    // This function assumes that the image is made of depth pixels (i.e. uint16_t's),
    // which is only true for IR/depth images.
    //
    const k4a_image_format_t imageFormat = depthImage.get_format();
    if (imageFormat != K4A_IMAGE_FORMAT_DEPTH16 && imageFormat != K4A_IMAGE_FORMAT_IR16)

    {
        throw std::logic_error("Attempted to colorize a non-depth image!");
    }

    const int width = depthImage.get_width_pixels();
    const int height = depthImage.get_height_pixels();

    buffer->resize(static_cast<size_t>(width * height));

    const uint16_t* depthData = reinterpret_cast<const uint16_t*>(depthImage.get_buffer());
    for (int h = 0; h < height; ++h)
    {
        for (int w = 0; w < width; ++w)
        {
            const size_t currentPixel = static_cast<size_t>(h * width + w);
            if (w < leftFilter || w > width - rightFilter)
                (*buffer)[currentPixel] = 0;
            else if (h < topFilter || h > height - bottomFilter)
                (*buffer)[currentPixel] = 0;
            else
                (*buffer)[currentPixel] = ColorizeGreyscale(depthData[currentPixel],
                    expectedValueRange.first,
                    expectedValueRange.second);
        }
    }
}

static inline uint8_t ColorizeGreyscale(const DepthPixel& value, const DepthPixel& min, const DepthPixel& max)
{
    // Clamp to max
    //
    if (value < minDepthMm || value > maxDepthMm)
        return 0;

    DepthPixel pixelValue = std::min(value, max);

    constexpr uint8_t PixelMax = std::numeric_limits<uint8_t>::max();
    auto normalizedValue = static_cast<uint8_t>((pixelValue - min) * (double(PixelMax) / (max - min)));
    return 255 - normalizedValue;
}


inline std::pair<uint16_t, uint16_t> GetDepthModeRange(const k4a_depth_mode_t depthMode)
{
    switch (depthMode)
    {
    case K4A_DEPTH_MODE_NFOV_2X2BINNED:
        return { (uint16_t)500, (uint16_t)5800 };
    case K4A_DEPTH_MODE_NFOV_UNBINNED:
        return { (uint16_t)500, (uint16_t)4000 };
    case K4A_DEPTH_MODE_WFOV_2X2BINNED:
        return { (uint16_t)250, (uint16_t)3000 };
    case K4A_DEPTH_MODE_WFOV_UNBINNED:
        return { (uint16_t)250, (uint16_t)2500 };

    case K4A_DEPTH_MODE_PASSIVE_IR:
    default:
        throw std::logic_error("Invalid depth mode!");
    }
}

static k4a_result_t depth_2d_to_3d(const k4a_calibration_t* calibration, float x, float y, float z, k4a_float3_t* pos, int* valid) {
    k4a_float2_t p;
    p.xy.x = x;
    p.xy.y = y;
    return k4a_calibration_2d_to_3d(
        calibration, &p, z, K4A_CALIBRATION_TYPE_DEPTH, K4A_CALIBRATION_TYPE_DEPTH, pos, valid);
}

// Parse command line arguments.Arguments, minDepth as a float.maxDepth as a float. rightFilter as an int.
// leftFilter as an int. topFilter as an int. bottomFilter as an int. fps as a float .If no arguments are provided, use the defaults.
// Each argument should be named and have a default value.
void parseArgs(int argc, char** argv) {
    // Loop through the command-line arguments
    for (int i = 1; i < argc - 1; i++) {
        std::string arg = argv[i];
        if (arg == "--mindepth") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> depthFilterMin;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--maxdepth") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> depthFilterMax;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--fps") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> fps;
        }
        else if (arg == "--right") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> rightFilter;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--left") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> leftFilter;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--top") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> topFilter;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--bottom") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> bottomFilter;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--blobmin") {
            std::string valueStr = argv[i + 1];
            std::stringstream ss(valueStr);
            ss >> blobMinArea;
            i++; // Skip the value in the next iteration
        }
        else if (arg == "--verbose") {
			verbose = true;
		}
        else if (arg == "--preview") {
            preview = true;
        }
    }
}

// Server to receive OSC messages
class SimpleReceiver : public OscPacketListener {
protected:
    virtual void ProcessMessage(const ReceivedMessage& m, const IpEndpointName& remoteEndpoint) {
        oscMutex.lock();
        try {
            printf("processing message: --%s--\n", m.AddressPattern());
            fflush(stdout);

            if (std::strcmp(m.AddressPattern(), "/mindepth") == 0) {
                osc::ReceivedMessage::const_iterator arg = m.ArgumentsBegin();
                printf("arg type: %d\n", arg->TypeTag());
                depthFilterMin = (arg)->AsFloat();
                printf("Received depth: %f\n", depthFilterMin);
            }
            if (std::strcmp(m.AddressPattern(), "/maxdepth") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> depthFilterMax >> osc::EndMessage;
                printf("Received max depth: %f\n", depthFilterMax);
            }
            if (std::strcmp(m.AddressPattern(), "/fps") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> fps >> osc::EndMessage;
                printf("Received fps: %f\n", fps);
            }
            //rightFilter as an int.
            if (std::strcmp(m.AddressPattern(), "/right") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> rightFilter >> osc::EndMessage;
                printf("Received right: %d\n", rightFilter);
            }
            //leftFilter as an int.
            if (std::strcmp(m.AddressPattern(), "/left") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> leftFilter >> osc::EndMessage;
                printf("Received left: %d\n", leftFilter);
            }
            //topFilter as an int.
            if (std::strcmp(m.AddressPattern(), "/top") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> topFilter >> osc::EndMessage;
                printf("Received top: %d\n", topFilter);
            }
            //bottomFilter as an int.
            if (std::strcmp(m.AddressPattern(), "/bottom") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> bottomFilter >> osc::EndMessage;
                printf("Received bottom: %d\n", bottomFilter);
            }
            if (std::strcmp(m.AddressPattern(), "/minarea") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                args >> blobMinArea >> osc::EndMessage;
                printf("Received min area: %d\n", blobMinArea);
            }
            if (std::strcmp(m.AddressPattern(), "/finished") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                finished = true;
                bool f;
                args >> f >> osc::EndMessage;
                printf("Received finished\n");
            }
            if (std::strcmp(m.AddressPattern(), "/verbose") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                bool f;
                verbose = true;
                args >> f >> osc::EndMessage;
                printf("Received verbose\n");
            }
            if (std::strcmp(m.AddressPattern(), "/verboseoff") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                bool f;
                verbose = false;
                args >> f >> osc::EndMessage;
                printf("Received verboseoff\n");
            }
            if (std::strcmp(m.AddressPattern(), "/preview") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                bool f;
                preview = true;
                args >> f >> osc::EndMessage;
                printf("Received preview enable\n");
                fflush(stdout);
            }
            if (std::strcmp(m.AddressPattern(), "/previewoff") == 0) {
                ReceivedMessageArgumentStream args = m.ArgumentStream();
                bool f;
                args >> f >> osc::EndMessage;
                preview = false;
                printf("Received previewoff\n");
            }
        }
        catch (osc::Exception& e) {
            // NOTE(tracy): Not sure why this is happening but exceptions are always thrown even though
            // we correctly parsed out the value.  So we just ignore the exception.
			// any parsing errors such as unexpected argument types, or
			// missing arguments get thrown as exceptions.
			std::cout << "error while parsing message: " << m.AddressPattern() << ": " << e.what() << "\n";
		}
        oscMutex.unlock();
    }
};


// Function to run the server on a separate thread
void runServer() {
    SimpleReceiver receiver;
    UdpListeningReceiveSocket s(IpEndpointName(IpEndpointName::ANY_ADDRESS, OSC_RECV_PORT), &receiver);
    s.SetAllowReuse(true);
    std::cout << "Server started at port " << OSC_RECV_PORT << "\n";
    s.RunUntilSigInt();
}





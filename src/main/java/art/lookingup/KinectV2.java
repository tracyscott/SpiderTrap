package art.lookingup;

import java.util.ArrayList;
import java.util.logging.Logger;

import art.lookingup.spidertrap.CVBlob;
import art.lookingup.spidertrap.ui.ModelParams;
import processing.core.PVector;
import KinectPV2.KJoint;
import KinectPV2.*;

// Kinect Controller Class
public class KinectV2
{
  private static final Logger logger = Logger.getLogger(KinectV2.class.getName());

  KinectPV2 kinect;
  public int [] RawDepth;
  public ArrayList<KSkeleton> skeletonArray;
  public KJoint[] joints;

  public float minHandDistance = 1.5f;
  public float maxHandDistance = 2.0f;

  public KinectV2(KinectPV2 k) {
    kinect = k;
    kinect.enableSkeleton3DMap(true);
    kinect.init();
    updateDistanceParams();
  }

  public KinectPV2 pv2() {
    return kinect;
  }

  public void updateDistanceParams() {
    synchronized (this) {
      // This will read from a file, lets not do it every frame.
      minHandDistance = ModelParams.getKV2MinD();
      maxHandDistance = ModelParams.getKV2MaxD();
    }
  }

  public void update() {
    skeletonArray = kinect.getSkeleton3d();
    float localMinHandDistance;
    float localMaxHandDistance;
    synchronized (this) {
      localMinHandDistance = minHandDistance;
      localMaxHandDistance = maxHandDistance;
    }

    for (int i = 0; i < skeletonArray.size(); i++) {
      KSkeleton skeleton = (KSkeleton) skeletonArray.get(i);
      if (skeleton.isTracked()) {
        //logger.info("Tracking skeleton: " + i);
        joints = skeleton.getJoints();

        PVector rightHand = joints[KinectPV2.JointType_HandRight].getPosition();
        //logger.info("Right hand: " + rightHand.x + "," + rightHand.y + "," + rightHand.z);
        if (rightHand.z < localMaxHandDistance && rightHand.z > localMinHandDistance) {
          CVBlob.addKinectV2CVBlob(rightHand.x * 1000f, rightHand.y * 1000f, rightHand.z * 1000f);
          CVBlob.cleanExpired();
        }
        PVector leftHand = joints[KinectPV2.JointType_HandLeft].getPosition();
        if (leftHand.z < localMaxHandDistance && leftHand.z > localMinHandDistance) {
          CVBlob.addKinectV2CVBlob(leftHand.x * 1000f, leftHand.y * 1000f, leftHand.z * 1000f);
          CVBlob.cleanExpired();
        }
      }
    }
  }
}
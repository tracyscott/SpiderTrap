import argparse
from pythonosc.udp_client import SimpleUDPClient

# Parse the command-line arguments
parser = argparse.ArgumentParser(description="Send an OSC message to /mindepth with a specified integer value.")

parser.add_argument("--mindepth", type=float, help="Float value for mindepth (units of feet)")
parser.add_argument("--maxdepth", type=float, help="Float value for maxdepth (units of feet)")
parser.add_argument("--fps", type=float, help="Frame processing FPS (max 15)")
parser.add_argument("--verbose", action="store_true", help="Turn on verbose logging")
parser.add_argument("--verboseoff", action="store_true",help="Turn off verbose logging")
parser.add_argument("--preview", action="store_true", help="Turn on OpenCV image preview")
parser.add_argument("--previewoff", action="store_true", help="Turn off OpenCV image preview")
parser.add_argument("--minarea", type=int, help="Minimum OpenCV blob area")
parser.add_argument("--right", type=int, help="Ignore pixels to the right of this margin size")
parser.add_argument("--left", type=int, help="Ignore pixels to the left of this margin size")
parser.add_argument("--top", type=int, help="Ignore pixels above this margin size")
parser.add_argument("--bottom", type=int, help="Ignore pixels below this margin size")
parser.add_argument("--finished", action="store_true", help="Finish processing and exit")
parser.add_argument("--ip", type=str, help="Address of OSC server")
parser.add_argument("--port", type=int, help="Port # of OSC server")


args = parser.parse_args()

# IP and port of the OSC server you want to send messages to
ip = "127.0.0.1"
port = 3030

if args.ip is not None:
	print("Found IP!")
	ip = args.ip
if args.port is not None:
	port = args.port

print ("IP", ip)

# Create a client to send messages to the specified IP and port
client = SimpleUDPClient(ip, port)

# Send the value to the /depth address

#client.send_message("/lx/mixer/channel/4/pattern/3/recall", True)
client.send_message("/lx/tempo/enabled", True)

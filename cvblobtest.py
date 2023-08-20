from pythonosc import udp_client
import sys
import random
import time;

def send_osc_message(ip, port, address, *args):
    client = udp_client.SimpleUDPClient(ip, port)
    client.send_message(address, args)

if __name__ == "__main__":
    # IP and port of the OSC server you want to send messages to
    ip = "127.0.0.1"
    port = 7979

    while True:
        x = random.randrange(-2000,2000)
        y = random.randrange(-2000,2000)
        send_osc_message(ip, port, "/spidertrap/blob", float(x), float(y), float(5))
        sleepTime = random.randrange(2000)
        time.sleep(sleepTime/1000)


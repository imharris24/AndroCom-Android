import subprocess
import json
import time
import socket


# Specify the broadcast IP address and port
broadcast_ip = '192.168.1.31'  # Replace with the broadcast address of your local network
broadcast_port = 54321

# Create a socket object for broadcasting
broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

def get_connected_devices():
    try:
        # Run the arp command and capture its output
        result = subprocess.check_output(['arp', '-a'], universal_newlines=True)

        # Split the output into lines
        lines = result.split('\n')

        # Extract IP addresses and corresponding MAC addresses
        devices = {}
        for line in lines:
            if 'ether' in line:
                parts = line.split()
                ip_address = parts[1]
                mac_address = parts[3]
                devices[mac_address] = ip_address.strip('()')

        return {"devices":devices}

    except subprocess.CalledProcessError:
        print("Error executing the arp command.")
        return None




# Broadcast the JSON string every 2 seconds (for example)
while True:
    connected_devices = get_connected_devices()

    if connected_devices:
        # Create a JSON object to send
        x = json.dumps(connected_devices, indent=4)
        broadcast_socket.sendto(x.encode(), (broadcast_ip, broadcast_port))
        print(f"Broadcasted: {x}")
        time.sleep(2)
    else:
        print("Failed to retrieve connected devices.")
    

# Close the broadcasting socket (this will never be reached in the example)
broadcast_socket.close()
import socket
import json
import threading
import subprocess
import time

# Global list to store data from apps

Active_User_list = []
Active_User_lock = threading.Lock()

def handle_client_connection(client_socket):
    request = client_socket.recv(1024).decode('utf-8')
    print(request)
    
    parsed_data = parse_received_message(request)
    print(parsed_data)

    if parsed_data: #check if data was parsed successfully
        with Active_User_lock:  # Update the active user list
            # Check if data is already store or not
            existing_entry = next((user for user in Active_User_list if user["IP Address"] == parsed_data["IP Address"] and user["Name"] == parsed_data["Name"]),None)
            if not existing_entry:
                Active_User_list.append(parsed_data)
            
    
    # Send ACK
    response = "ACK"
    client_socket.send(response.encode('utf-8'))
    
    # Close the client socket
    client_socket.close()

def start_server(host, port):
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((host, port))
    server_socket.listen(5)
    print(f"Server listening on {host} : {port}")

    while True:
        client_socket, _ = server_socket.accept()
        print("Accepted connection from:", client_socket.getpeername())
        handle_client_connection(client_socket)

def parse_received_message(data):
    # Assuming the received message is in the format: "Umer Ahmed: 192.168.1.15"
    try:    
        name, ip_address = data.strip('"').split(': ')
        mac_address = get_mac_address(ip_address)
        
        return {
            "Name": name,
            "IP Address": ip_address,
            "Mac Address": mac_address
        }
    except Exception as e:
        print("Error in parsing message")
        return None

def get_mac_address(ip_address):
    # Use ARP command to get MAC address based on IP address
    try:
        arp_output = subprocess.check_output(['arp', '-a'], universal_newlines=True)
        lines = arp_output.split('\n')
        for line in lines:
            if ip_address in line:
                parts = line.split()
                return parts[3]  # MAC address is the second element
    except subprocess.CalledProcessError as e:
        print("Error executing the arp command:", e)
    return None

def get_connected_devices():
    connected_devices = {}
    try:
        arp_output = subprocess.check_output(['arp', '-a'], universal_newlines=True)
        lines = arp_output.split('\n')
        for line in lines:
            parts = line.split()
            if len(parts) >= 4:
                ip_address = parts[1].strip('()')
                mac_address = parts[3]
                connected_devices[mac_address] = ip_address
        return connected_devices
    except subprocess.CalledProcessError as e:
        print("Error executing the arp command:", e)
        return {}

def send_udp_broadcast(broadcast_ip, broadcast_port):
    broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    
    while True:
        try:
            # Get connected devices
            connected_devices = get_connected_devices()
            
            # Lock the active user list to prevent concurrent access
            with Active_User_lock:
                # Remove inactive users from the Active_User_list
                Active_User_list[:] = [user for user in Active_User_list if user['Mac Address'] in connected_devices and user['IP Address'] in connected_devices.values()]
                
                # Create a JSON object containing the active user list
                json_data = {"active_users": Active_User_list}
                udp_message = json.dumps(json_data, indent=4)
                
                # Send UDP broadcast
                broadcast_socket.sendto(udp_message.encode(), (broadcast_ip, broadcast_port))
                print(f"Broadcasted: {udp_message}")
                
        except Exception as e:
            print("Error occurred while sending UDP broadcast:", e)
        
        time.sleep(5)  # Wait for a while before attempting again
    
    broadcast_socket.close()


if __name__ == "__main__":
    HOST = '192.168.1.1'  # Listen on all available interfaces
    PORT = 49150  # Choose any available port
    
    # Start the TCP server in a separate thread
    tcp_server_thread = threading.Thread(target=start_server, args=(HOST, PORT))
    tcp_server_thread.start()
    
    # Specify the broadcast IP address and port
    broadcast_ip = '192.168.1.31'  # Replace with the broadcast address of your local network
    broadcast_port = 54321
    
    # Start the UDP broadcast in a separate thread
    udp_broadcast_thread = threading.Thread(target=send_udp_broadcast, args=(broadcast_ip, broadcast_port))
    udp_broadcast_thread.start()
    
    # Join both threads to keep the main thread running
    tcp_server_thread.join()
    udp_broadcast_thread.join()

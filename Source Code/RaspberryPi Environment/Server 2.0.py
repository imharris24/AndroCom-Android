import socket
import json
import threading
import subprocess
import time
import os

# Global list to store data from apps
Active_User_list = []
Active_User_lock = threading.Lock()

def clear_screen():
    if os.name == 'nt':
        os.system('cls')
    else:
        os.system('clear')

def handle_client_connection(client_socket):
    try:
        request = client_socket.recv(1024).decode('utf-8')
        print(request)
        
        parsed_data = parse_received_message(request)
        print(parsed_data)

        if parsed_data:  # Check if data was parsed successfully
            with Active_User_lock:  # Update the active user list
                existing_entry = next((user for user in Active_User_list if user["IP Address"] == parsed_data["IP Address"]), None)
                if existing_entry:
                    # Update the existing entry
                    existing_entry.update(parsed_data)
                else:
                    # Add new entry
                    Active_User_list.append(parsed_data)
        
        response = "ACK"
        client_socket.send(response.encode('utf-8'))
    except Exception as e:
        print(f"Error handling client connection: {e}")
    finally:
        client_socket.close()

def start_server(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind((host, port))
        server_socket.listen(5)
        clear_screen()
        print(f"Server listening on {host}:{port}")

        while True:
            client_socket, _ = server_socket.accept()
            clear_screen()
            print("Accepted connection from:", client_socket.getpeername())
            handle_client_connection(client_socket)

def parse_received_message(data):
    try:
        name, ip_address = data.strip('"').split(': ')
        mac_address = get_mac_address(ip_address)
        return {
            "Name": name,
            "IP Address": ip_address,
            "Mac Address": mac_address
        }
    except Exception as e:
        clear_screen()
        print(f"Error parsing message: {e}")
        return None

def get_mac_address(ip_address):
    try:
        arp_output = subprocess.check_output(['arp', '-a'], universal_newlines=True)
        for line in arp_output.split('\n'):
            if ip_address in line:
                parts = line.split()
                return parts[3].strip()
    except subprocess.CalledProcessError as e:
        print(f"Error executing the arp command: {e}")
    return None

def get_connected_devices():
    connected_devices = {}
    try:
        arp_output = subprocess.check_output(['arp', '-a'], universal_newlines=True)
        for line in arp_output.split('\n'):
            parts = line.split()
            if len(parts) >= 4:
                ip_address = parts[1].strip('()')
                mac_address = parts[3]
                connected_devices[ip_address] = mac_address
    except subprocess.CalledProcessError as e:
        clear_screen()
        print(f"Error executing the arp command: {e}")
    return connected_devices

def send_udp_broadcast(broadcast_ip, broadcast_port):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as broadcast_socket:
        broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        
        while True:
            try:
                connected_devices = get_connected_devices()
                
                with Active_User_lock:
                    # Remove inactive users from the Active_User_list
                    Active_User_list[:] = [user for user in Active_User_list if user['IP Address'] in connected_devices]
                    
                    json_data = {"active_users": Active_User_list}
                    udp_message = json.dumps(json_data, indent=4)
                    
                    broadcast_socket.sendto(udp_message.encode(), (broadcast_ip, broadcast_port))
                    clear_screen()
                    print(f"Broadcasted: {udp_message}")
                    
            except Exception as e:
                clear_screen()
                print(f"Error occurred while sending UDP broadcast: {e}")
            
            time.sleep(2)

if __name__ == "__main__":
    HOST = '192.168.1.1'  # Listen on all available interfaces
    PORT = 49150  # Choose any available port
    
    tcp_server_thread = threading.Thread(target=start_server, args=(HOST, PORT))
    tcp_server_thread.start()
    
    # Specify the broadcast IP address and port
    broadcast_ip = '192.168.1.31'  # Replace with the broadcast address of your local network
    broadcast_port = 54321
    
    udp_broadcast_thread = threading.Thread(target=send_udp_broadcast, args=(broadcast_ip, broadcast_port))
    udp_broadcast_thread.start()
    
    tcp_server_thread.join()
    udp_broadcast_thread.join()

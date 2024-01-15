#!/bin/bash

# Check if NetworkManager is installed
if ! command -v nmcli &> /dev/null; then
    echo "NetworkManager is not installed. Please install it first."
    exit 1
fi

# Set your desired network information
NETWORK_NAME="Ad-HocNetwork"
SSID="Ad-HocNetwork"
PASSWORD="12345678"

#Delete existing connection (if any)
sudo nmcli connection delete "$hotspot_ssid" 2>/dev/null

# Create a new connection with a /27 subnet
nmcli con add type wifi ifname '*' con-name $SSID autoconnect yes ssid $SSID 802-11-wireless.mode ap 802-11-wireless.band bg ipv4.method shared
nmcli con modify $SSID 802-11-wireless.security.key-mgmt wpa-psk
nmcli con modify $SSID 802-11-wireless-security.psk $PASSWORD

# Set the network address and subnet mask for a /27 subnet
nmcli con modify $SSID ipv4.addresses "192.168.1.1/27"

# Bring up the WiFi connection
nmcli con up $SSID

# Display the status of the WiFi connection
nmcli con show $SSID | grep GENERAL.STATE

echo "WiFi network '$SSID' with a /27 subnet is now running."

# Function to list connected devices
list_connected_devices() {
    echo "Connected Devices:"
    sudo arp -a | grep "$wifi_interface" | awk '{print $1, $2}'
}

# Trap to handle SIGINT (Ctrl+C) and list connected devices before exiting
trap 'list_connected_devices; exit' SIGINT

# Continuously list connected devices
while true; do
    list_connected_devices
    sleep 10  # Adjust the sleep interval as needed
done
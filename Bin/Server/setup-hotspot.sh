#!/bin/bash

# Set your Wi-Fi interface name (e.g., wlan0)
wifi_interface="wlan0"

# Set hotspot name and password
hotspot_ssid="Ad-Hoc Network"
hotspot_password="12345678"

#Delete existing connection (if any)
sudo nmcli connection delete "$hotspot_ssid" 2>/dev/null

# Create a Wi-Fi hotspot
sudo nmcli device wifi hotspot con-name "$hotspot_ssid" ssid "$hotspot_ssid" band bg password "$hotspot_password" ifname "$wifi_interface"

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
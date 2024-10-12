<div align="center">
    <img src="https://img.shields.io/github/languages/count/imharris24/AndroCom-Android?label=Languages&style=for-the-badge">
    <img src="https://img.shields.io/github/languages/top/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/repo-size/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/issues/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/issues-pr-closed/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/license/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/forks/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/stars/imharris24/AndroCom-Android?style=for-the-badge">
    <img src="https://img.shields.io/github/last-commit/imharris24/AndroCom-Android?style=for-the-badge">
</div>

# AndroCom – Communication Without Internet

AndroCom is a groundbreaking Android application that empowers users to communicate without an active internet connection. It enables encrypted text messaging, voice calls, and video calls over an ad hoc network established using a **Raspberry Pi 3B+**, making it perfect for use in remote areas, during internet outages, or in emergency situations.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [System Architecture](#system-architecture)
- [Installation](#installation)
- [Usage](#usage)
- [Screenshots](#screenshots)
- [Future Work](#future-work)
- [License](#license)

## Features

AndroCom’s core features include:

- **Offline Communication**: Communicate using text, voice, and video without internet by connecting to a local ad hoc network.
- **Ad Hoc Network**: Utilizes Raspberry Pi 3B+ to create a temporary, decentralized communication network.
- **Encrypted Messages**: Ensures security and privacy through AES encryption.
- **Voice and Video Calls**: Allows users to make voice and video calls within the ad hoc network.
- **Scalability**: Multiple devices can connect to the ad hoc network for group communication.
- **User Blocking and Mute Features**: Provides the ability to block or mute other users for enhanced control over communication.

## Technologies Used

AndroCom leverages a range of technologies to provide its functionality:

- **Programming Languages**: 
  - Kotlin & Java for the Android application.
  - Bash & Python for Raspberry Pi scripts.
- **Networking**: 
  - **UDP** for broadcasting active users across the network.
  - **TCP/IP** for message exchange and voice/video call handling.
- **Encryption**: 
  - **AES (Advanced Encryption Standard)** ensures end-to-end encrypted communication.
- **Hardware**: 
  - **Raspberry Pi 3B+** provides the ad hoc network capabilities, allowing devices to connect without relying on existing infrastructure.
- **Databases**: 
  - **SQLite** for storing local data on the Android devices.
  
## System Architecture

The architecture of AndroCom revolves around the creation of an ad hoc network using a **Raspberry Pi 3B+**, which acts as the central hub for communication. Devices running the AndroCom app connect to this network, enabling them to communicate without relying on the internet. 

**Key Components**:
1. **Client Application** (Android):
    - Handles user authentication, message transmission, and voice/video calls.
2. **Ad Hoc Network**:
    - Created using Raspberry Pi, which broadcasts available users and facilitates communication.
3. **Encryption Module**:
    - AES-based encryption for all text messages, calls, and video transmissions.

**Flow**:
1. Raspberry Pi runs a server to manage the ad hoc network.
2. Devices connect to this network and exchange data using the AndroCom app.
3. Communication is secured through encryption and is sent over the ad hoc network without touching the internet.

## Installation

### Prerequisites
1. **Raspberry Pi 3B+** with Raspbian OS installed.
2. **Android Studio** for building and running the Android app.
3. **Python** installed on Raspberry Pi for server-side scripting.

### Setup Instructions

#### Raspberry Pi
1. Clone the repo: 
    ```bash
    git clone https://github.com/imharris24/AndroCom.git
    ```
2. Navigate to the Raspberry Pi setup script:
    ```bash
    cd AndroCom/RaspberryPi
    bash setup.sh
    ```
3. Follow the script instructions to set up the ad hoc network on your Raspberry Pi.

#### Android App
1. Clone the repository to your local machine:
    ```bash
    git clone https://github.com/imharris24/AndroCom.git
    ```
2. Open the project in Android Studio.
3. Build the APK and run the app on your device.

#### Connecting to the Network
1. Ensure your Android device is connected to the ad hoc network in Wifi settings.
2. Launch the AndroCom app and complete the profile setup.
3. Start chatting, making voice calls, or video calls with connected users!

## Usage

### Sending Messages
1. Open the **Chat** section from the app.
2. Select an active user and start typing your message.
3. Hit **Send** and the message will be delivered securely over the ad hoc network.

### Making Voice Calls
1. Select a user from the **Active Users** list.
2. Tap the **Call** button to initiate a voice call.
3. You can mute/unmute the microphone during the call.

### Making Video Calls
1. Choose a user from the chat or active users list.
2. Tap the **Video Call** button to start a video call.
3. You can switch between the front and rear cameras or mute the camera during the call.

## Screenshots

Here are some screenshots demonstrating the functionality of AndroCom:

<div align="center">
    <img src="https://github.com/imharris24/AndroCom-Android/blob/main/Screenshot/1.png" width="200">
    <img src="https://github.com/imharris24/AndroCom-Android/blob/main/Screenshot/1.png" width="200">
    <img src="https://github.com/imharris24/AndroCom-Android/blob/main/Screenshot/1.png" width="200">
    <img src="https://github.com/imharris24/AndroCom-Android/blob/main/Screenshot/1.png" width="200">
</div>

1. **Splash Screen**: The starting screen of the application.
2. **Chat Screen**: A look at the encrypted text messaging interface.
3. **Video Call Screen**: Initiating a secure video call within the network.

## Future Work

Planned enhancements for AndroCom include:

- **Enhanced User Interface**: A more polished and user-friendly UI for better interaction.
- **File Transfer**: Adding the ability to securely transfer files over the ad hoc network.
- **Group Calls**: Expanding functionality to support group voice and video calls.
- **Network Optimization**: Improvements to handle a larger number of active users in the ad hoc network.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

**Contributors**:  
- [@imharris24](https://www.github.com/imharris24)  
- [@imummer16](https://www.github.com/imummer16)  
- [@wasiyah](https://github.com/wasiyah)

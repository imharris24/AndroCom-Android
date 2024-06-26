package com.application.androcom;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class UDPServer {
    DatagramSocket socket;
    String lastString = (-1 + "");
    int sameTime = 0;
    public handleReceiveData callback;
    private volatile boolean running = true;
    public UDPServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public void setReceiveCallback(handleReceiveData call) {
        callback = call;
    }

    public void service() throws IOException {
        while (true) {
            DatagramPacket dp = new DatagramPacket(new byte[102400], 102400);
            socket.receive(dp);

            byte[] data = dp.getData();
            callback.handleReceive(data);
        }
    }

    public void start() throws SocketException, IOException {
        service();
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    public void sendMsg(final byte[] data, String IP) {

        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null) {
                        SocketAddress socketAddres = new InetSocketAddress(IP, 8804);
                        socket.send(new DatagramPacket(data, data.length, socketAddres));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        send.start();
    }


}
import java.net.DatagramPacket
import java.net.DatagramSocket

fun main() {
    // Specify the port to listen on
    val port = 12345

    // Create a DatagramSocket to listen for UDP broadcasts
    val socket = DatagramSocket(port)

    // Buffer to store incoming data
    val buffer = ByteArray(1024)

    println("Waiting for broadcasts...")

    while (true) {
        // Receive the broadcast packet

        val packet = DatagramPacket(buffer, buffer.size)
        socket.receive(packet)

        // Extract the received data
        val receivedData = String(packet.data, 0, packet.length)
        println("Received: $receivedData")

        // Parse the received JSON string
        // Assuming you have some JSON library (e.g., Gson) in your project
        // Parse the JSON string into a Kotlin data class or a Map
        // For example, if using Gson:
        // val gson = Gson()
        // val data = gson.fromJson(receivedData, YourDataClass::class.java)
        // println(data)
        // Or use a Map directly if the structure is dynamic
    }

    // Note: The code above will run indefinitely; you may want to add a condition to exit the loop if needed.
}

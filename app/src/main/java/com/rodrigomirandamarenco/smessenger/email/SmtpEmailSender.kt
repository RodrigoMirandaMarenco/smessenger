package com.rodrigomirandamarenco.smessenger.email

import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.Base64

class SmtpEmailSender(
    private val smtpServer: String,
    private val smtpPort: Int,
    private val username: String,
    private val password: String
) {
    fun sendEmail(
        toEmail: String,
        subject: String,
        body: String
    ): Boolean {
        return try {
            val socket = Socket(smtpServer, smtpPort)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

            // Read initial response
            readResponse(reader)

            // Send EHLO
            sendCommand(writer, "EHLO localhost")
            readResponse(reader)

            // Start TLS
            sendCommand(writer, "STARTTLS")
            readResponse(reader)

            // Upgrade to TLS socket
            val sslSocket = upgradeToTLS(socket)
            val sslReader = BufferedReader(InputStreamReader(sslSocket.getInputStream()))
            val sslWriter = BufferedWriter(OutputStreamWriter(sslSocket.getOutputStream()))

            // Send EHLO again after TLS
            sendCommand(sslWriter, "EHLO localhost")
            readResponse(sslReader)

            // Authenticate
            val authString = "\u0000$username\u0000$password"
            val encodedAuth = Base64.getEncoder().encodeToString(authString.toByteArray())
            sendCommand(sslWriter, "AUTH PLAIN $encodedAuth")
            readResponse(sslReader)

            // Set from
            sendCommand(sslWriter, "MAIL FROM:<$username>")
            readResponse(sslReader)

            // Set to
            sendCommand(sslWriter, "RCPT TO:<$toEmail>")
            readResponse(sslReader)

            // Send data
            sendCommand(sslWriter, "DATA")
            readResponse(sslReader)

            // Send email content
            val emailContent = buildEmailContent(username, toEmail, subject, body)
            sslWriter.write(emailContent)
            sslWriter.flush()
            sendCommand(sslWriter, ".")
            readResponse(sslReader)

            // Quit
            sendCommand(sslWriter, "QUIT")
            readResponse(sslReader)

            sslSocket.close()
            socket.close()

            Log.d("SmtpEmailSender", "Email sent successfully to $toEmail")
            true
        } catch (e: Exception) {
            Log.e("SmtpEmailSender", "Failed to send email: ${e.message}", e)
            false
        }
    }

    private fun sendCommand(writer: BufferedWriter, command: String) {
        writer.write(command + "\r\n")
        writer.flush()
        Log.d("SmtpEmailSender", ">> $command")
    }

    private fun readResponse(reader: BufferedReader): String {
        var response = ""
        var line: String?
        do {
            line = reader.readLine()
            if (line != null) {
                response += line + "\n"
                Log.d("SmtpEmailSender", "<< $line")
            }
        } while (line != null && line.length > 3 && line[3] == '-')
        return response
    }

    private fun upgradeToTLS(socket: Socket): Socket {
        val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
        sslContext.init(null, null, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return sslSocketFactory.createSocket(socket, smtpServer, smtpPort, true) as javax.net.ssl.SSLSocket
    }

    private fun buildEmailContent(
        from: String,
        to: String,
        subject: String,
        body: String
    ): String {
        return "From: <$from>\r\n" +
                "To: <$to>\r\n" +
                "Subject: $subject\r\n" +
                "Content-Type: text/plain; charset=utf-8\r\n" +
                "\r\n" +
                "$body\r\n"
    }
}

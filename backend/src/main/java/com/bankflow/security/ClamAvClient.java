package com.bankflow.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Component
@RequiredArgsConstructor
public class ClamAvClient {

    private final ClamAvProperties properties;


    public String scan(InputStream inputStream) {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(properties.host(), properties.port()), 3000);

            socket.setSoTimeout(10000);


            try (OutputStream output = socket.getOutputStream();
                 InputStream response = socket.getInputStream()) {

                output.write("zINSTREAM\0".getBytes());
                output.flush();

                byte[] buffer = new byte[2048];

                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    output.write(new byte[]{
                            (byte) ((bytesRead >> 24) & 0xff),
                            (byte) ((bytesRead >> 16) & 0xff),
                            (byte) ((bytesRead >> 8) & 0xff),
                            (byte) (bytesRead & 0xff)});

                    output.write(buffer, 0, bytesRead);
                }


                // End of stream marker
                output.write(new byte[]{0, 0, 0, 0});

                output.flush();


                byte[] responseBytes = response.readNBytes(1024);


                return new String(responseBytes).trim();
            }


        } catch (IOException e) {

            throw new RuntimeException("Unable to connect to ClamAV", e);
        }
    }
}
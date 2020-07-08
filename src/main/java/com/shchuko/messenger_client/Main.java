package com.shchuko.messenger_client;

import com.shchuko.messenger_client.gui.MainForm;
import com.shchuko.messenger_client.security.JerseyHttpClientFactory;
import org.glassfish.jersey.client.ClientProperties;

import javax.swing.*;
import javax.ws.rs.client.Client;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public class Main {
    public static void main(String[] args) {
        try {
            Client client = JerseyHttpClientFactory.getJerseyHTTPSClient();
            client.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);

            SwingUtilities.invokeLater(() -> {
                new MainForm(client);
            });
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            System.out.println("Internal app error while initialization");
        }


    }
}
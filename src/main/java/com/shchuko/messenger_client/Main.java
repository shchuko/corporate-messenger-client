package com.shchuko.messenger_client;

import com.shchuko.messenger_client.gui.MainForm;
import com.shchuko.messenger_client.security.JerseyHttpClientFactory;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientProperties;

import javax.swing.*;
import javax.ws.rs.client.Client;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public class Main {
    public static void main(String[] args) {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");

            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new java.security.SecureRandom());

//            Client client = JerseyHttpClientFactory.getJerseyHTTPSClient();
//            client.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);

            SwingUtilities.invokeLater(() -> {
                new MainForm(ClientBuilder.newBuilder()
                    .sslContext(sslcontext)
                    .hostnameVerifier((s1, s2) -> true)
                    .build());
            });
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            System.out.println("Internal app error while initialization");
        }


    }
}
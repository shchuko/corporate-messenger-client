package com.shchuko.messenger_client.dto.auth;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GeneratedTokensDTO {
    String sessionToken;
    long sessionExpiresOn;

    String refreshToken;
    long refreshExpiresOn;

    String username;

    public GeneratedTokensDTO() {
    }

    public GeneratedTokensDTO(String sessionToken, long sessionExpiresOn, String refreshToken, long refreshExpiresOn, String username) {
        this.sessionToken = sessionToken;
        this.sessionExpiresOn = sessionExpiresOn;
        this.refreshToken = refreshToken;
        this.refreshExpiresOn = refreshExpiresOn;
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }


    public long getSessionExpiresOn() {
        return sessionExpiresOn;
    }


    public String getRefreshToken() {
        return refreshToken;
    }

    public long getRefreshExpiresOn() {
        return refreshExpiresOn;
    }


    public String getUsername() {
        return username;
    }

}

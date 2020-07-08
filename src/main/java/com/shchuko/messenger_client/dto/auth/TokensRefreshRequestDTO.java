package com.shchuko.messenger_client.dto.auth;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TokensRefreshRequestDTO {
    String oldSessionToken;

    Boolean newRefreshTokenNeeded;

    public TokensRefreshRequestDTO() {
    }

    public TokensRefreshRequestDTO(String oldSessionToken, Boolean newRefreshTokenNeeded) {
        this.oldSessionToken = oldSessionToken;
        this.newRefreshTokenNeeded = newRefreshTokenNeeded;
    }

    public String getOldSessionToken() {
        return oldSessionToken;
    }

    public void setOldSessionToken(String oldSessionToken) {
        this.oldSessionToken = oldSessionToken;
    }

    public Boolean getNewRefreshTokenNeeded() {
        return newRefreshTokenNeeded;
    }

    public void setNewRefreshTokenNeeded(Boolean newRefreshTokenNeeded) {
        this.newRefreshTokenNeeded = newRefreshTokenNeeded;
    }
}

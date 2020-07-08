package com.shchuko.messenger_client.security;

import java.util.Date;

public class TokenStorage {
    private static final long REFRESH_REFRESH_SECONDS_BEFORE_EXPIRES = 300;
    private static final long REFRESH_SESSION_SECONDS_BEFORE_EXPIRES = 20;

    String sessionToken;
    Date sessionTokenExpiration;
    Date sessionTokenRefreshOn;

    String refreshToken;
    Date refreshTokenExpiration;
    Date refreshTokenRefreshOn;

    public TokenStorage() {
    }

    public TokenStorage(String sessionToken, long sessionTokenExpiration, String refreshToken, long refreshTokenExpiration) {
        this.sessionToken = sessionToken;
        this.sessionTokenExpiration = new Date(sessionTokenExpiration * 1000);
        this.sessionTokenRefreshOn = new Date((sessionTokenExpiration - REFRESH_SESSION_SECONDS_BEFORE_EXPIRES) * 1000);

        this.refreshToken = refreshToken;
        this.refreshTokenExpiration = new Date(refreshTokenExpiration * 1000);
        this.refreshTokenRefreshOn = new Date((refreshTokenExpiration - REFRESH_REFRESH_SECONDS_BEFORE_EXPIRES) * 1000);

    }

    public void clear() {
        sessionToken = "";
        refreshToken = "";
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public Date getSessionTokenExpiration() {
        return sessionTokenExpiration;
    }

    public Date getSessionTokenRefreshOn() {
        return sessionTokenRefreshOn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Date getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public Date getRefreshTokenRefreshOn() {
        return refreshTokenRefreshOn;
    }

    public void updateSessionToken(String sessionToken, long sessionTokenExpiration) {
        this.sessionToken = sessionToken;
        this.sessionTokenExpiration = new Date(sessionTokenExpiration * 1000);
        this.sessionTokenRefreshOn = new Date((sessionTokenExpiration - REFRESH_SESSION_SECONDS_BEFORE_EXPIRES) * 1000);
    }

    public void updateRefreshToken(String refreshToken, long refreshTokenExpiration) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiration = new Date(refreshTokenExpiration * 1000);
        this.refreshTokenRefreshOn = new Date((refreshTokenExpiration - REFRESH_REFRESH_SECONDS_BEFORE_EXPIRES) * 1000);
    }


}

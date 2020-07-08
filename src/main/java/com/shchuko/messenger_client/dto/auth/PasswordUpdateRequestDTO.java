package com.shchuko.messenger_client.dto.auth;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PasswordUpdateRequestDTO {
    private String oldPassword;

    private String newPassword;

    public PasswordUpdateRequestDTO() {
    }

    public PasswordUpdateRequestDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

package org.shyu.marketapiuser.dto;
import lombok.Data;
import java.io.Serializable;
@Data
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String phone;
    private String email;
    private String verifyCode;
}

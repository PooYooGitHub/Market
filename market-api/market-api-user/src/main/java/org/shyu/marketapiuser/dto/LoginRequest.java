package org.shyu.marketapiuser.dto;
import lombok.Data;
import java.io.Serializable;
/**
 * User Login Request DTO
 */
@Data
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String loginType;
}

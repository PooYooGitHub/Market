package org.shyu.marketapiuser.dto;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

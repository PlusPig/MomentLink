// UserResponse.java
package com.keyy.momentlinkback.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private Integer gender;
    private String signature;
    private Integer status;
    private Integer role;
    private LocalDateTime createdAt;
}

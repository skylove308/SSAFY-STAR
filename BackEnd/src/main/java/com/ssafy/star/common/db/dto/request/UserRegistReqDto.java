package com.ssafy.star.common.db.dto.request;

import lombok.Getter;

@Getter
public class UserRegistReqDto {

    private String email;

    private String name;

    private String nickname;

    private String accountPwd;
}

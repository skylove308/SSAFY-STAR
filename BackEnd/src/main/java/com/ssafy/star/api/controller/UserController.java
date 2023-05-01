package com.ssafy.star.api.controller;

import com.ssafy.star.api.service.UserService;
import com.ssafy.star.common.db.dto.request.*;
import com.ssafy.star.common.util.constant.Msg;
import com.ssafy.star.common.util.dto.ResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Log4j2
@RestController
@Api(tags = {"유저 API"})
@RequiredArgsConstructor
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/regist")
    @ApiOperation(value="회원가입")
    public ResponseEntity<ResponseDto> userRegist(@RequestBody UserRegistReqDto userRegistReqDto) {
        if(userService.registUser(userRegistReqDto)) {
            return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_REGIST));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDto.of(HttpStatus.CONFLICT, Msg.DUPLICATED_ID));
    }

    @PostMapping("/login")
    @ApiOperation(value="로그인")
    public ResponseEntity<ResponseDto> userLogin(@RequestBody UserLoginReqDto userLoginReqDto) {

        Optional<String> tokenOptional = Optional.ofNullable(userService.loginUser(userLoginReqDto));

        return tokenOptional.map(token -> ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_LOGIN, token)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDto.of(HttpStatus.NOT_FOUND, Msg.FAULURE_LOGIN)));
    }

    @PostMapping("/logout")
    @ApiOperation(value="로그아웃")
    public ResponseEntity<ResponseDto> userLogout(@RequestHeader("Authorization") String token) {
        userService.logoutUser(token);
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_LOGOUT));
    }

    @GetMapping("/detail")
    @ApiOperation(value="유저 정보 조회")
    public ResponseEntity<ResponseDto> userGetDetail() {
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_GET, userService.getDetailUser()));
    }

    @GetMapping
    @ApiOperation(value="메인화면 유저 정보 조회")
    public ResponseEntity<ResponseDto> userGet() {
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_GET, userService.getUser()));
    }

    @PutMapping
    @ApiOperation(value="유저정보 수정")
    public ResponseEntity<ResponseDto> userModify(@RequestBody UserModifyReqDto userModifyReqDto) {
        userService.modifyUser(userModifyReqDto);
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_UPDATE));
    }

    @PutMapping("/pwd")
    @ApiOperation(value="비밀번호 수정")
    public ResponseEntity<ResponseDto> userModifyPwd(@RequestBody String newPwd) {
        userService.modifyPwdUser(newPwd);
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_UPDATE));
    }

    @DeleteMapping
    @ApiOperation(value="탈퇴")
    public ResponseEntity<ResponseDto> userDelete() {
        userService.deleteUser();
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_DELETE));
    }

    @GetMapping("/email/check-duplicate")
    @ApiOperation(value="이메일 중복 여부 확인")
    public ResponseEntity<ResponseDto> checkDuplicateEmail(@RequestParam String email) {
        if (userService.duplicateEmailCheck(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDto.of(HttpStatus.CONFLICT, Msg.DUPLICATED_EMAIL));
        }
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.VALID_EMAIL));
    }

    @PostMapping("/email/send-verification")
    @ApiOperation(value="인증메일 전송")
    public ResponseEntity<ResponseDto> sendEmailVerificationCode(@RequestBody String email) {
        userService.emailVerificationCodeSend(email);
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_SEND_EMAIL));
    }

    @GetMapping("/email/find-id")
    @ApiOperation(value="이메일로 아이디 찾기")
    public ResponseEntity<ResponseDto> userFindId(@RequestParam String email) {

        Optional<String> idOptional = Optional.ofNullable(userService.findIdUser(email));

        return idOptional.map(id -> ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_GET, id)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDto.of(HttpStatus.NOT_FOUND, Msg.EMAIL_NOT_FOUND)));
    }

    @PostMapping("/email/find-pwd")
    @ApiOperation(value="아이디와 이메일 체크 후 비밀번호 이메일 전송")
    public ResponseEntity<ResponseDto> userFindPwd(@RequestBody UserFindPwdReqDto userFindPwdReqDto) {
        userService.findPwdUser(userFindPwdReqDto);
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_SEND_EMAIL));
    }

    @PostMapping("/badge")
    // @Secured("CLIENT")
    @ApiOperation(value = "뱃지 인증 요청")
    public ResponseEntity<ResponseDto> badgeRegist(
            @RequestPart BadgeRegistReqDto dto,
            @RequestPart MultipartFile file) throws IOException {
        userService.registBadge(dto, file);
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_REGIST));
    }

    @GetMapping("/badge/status/{type}")
    @ApiOperation(value = "뱃지 인증 진행상태 확인")
    public ResponseEntity<ResponseDto> badgeStatusSearch(@PathVariable("type") String type){
        return ResponseEntity.ok().body(ResponseDto.of(HttpStatus.OK, Msg.SUCCESS_REGIST,userService.searchBadgeStatus(type)));
    }

}

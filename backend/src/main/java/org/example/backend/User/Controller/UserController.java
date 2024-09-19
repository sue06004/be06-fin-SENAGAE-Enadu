package org.example.backend.User.Controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.Common.BaseResponse;
import org.example.backend.Common.BaseResponseStatus;
import org.example.backend.Exception.custom.InvalidUserException;
import org.example.backend.File.Service.CloudFileUploadService;
import org.example.backend.Security.CustomUserDetails;
import org.example.backend.User.Model.Req.UpdateUserPasswordReq;
import org.example.backend.User.Model.Req.UserSignupReq;
import org.example.backend.User.Model.Res.GetUserInfoRes;
import org.example.backend.User.Service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.Util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CloudFileUploadService cloudFileUploadService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public BaseResponse<String> signup(
            @RequestPart UserSignupReq userSignupReq,
            @RequestPart MultipartFile profileImg) {
        String profileImgUrl;
        if(profileImg == null || profileImg.isEmpty()){
            profileImgUrl = "https://dayun2024-s3.s3.ap-northeast-2.amazonaws.com/IMAGE/2024/09/11/0d7ca962-ccee-4fbb-9b5d-f5deec5808c6";
        } else {
            profileImgUrl = cloudFileUploadService.uploadImg(profileImg);
        }

        // 이메일 중복 확인
        if (!userService.checkDuplicateEmail(userSignupReq.getEmail())){
            return new BaseResponse<>(BaseResponseStatus.USER_DUPLICATE_EMAIL);
        }
        // 닉네임 중복 확인
        if(!userService.checkDuplicateNickname(userSignupReq.getNickname())){
            return new BaseResponse<>(BaseResponseStatus.USER_DUPLICATE_NICKNAME);
        }

        userService.signup(userSignupReq, profileImgUrl);
        return new BaseResponse<>();
    }

    @PostMapping("/logout")
    public BaseResponse<String> logout(HttpServletResponse response) {
        Cookie expiredCookie = jwtUtil.removeCookie();
        response.addCookie(expiredCookie);
        return new BaseResponse<>();
    }

    @GetMapping("/duplicate/email")
    public BaseResponse<Boolean> duplicateEmail(String email) {
        return new BaseResponse<>(userService.checkDuplicateEmail(email));
    }

    @GetMapping("/duplicate/nickname")
    public BaseResponse<Boolean> duplicateName(String nickname) {
        return new BaseResponse<>(userService.checkDuplicateNickname(nickname));
    }

    @PatchMapping("/nickname")
    public BaseResponse<String> updateNickname(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody Map<String, String> nicknameMap ) {
        userService.updateNickname(customUserDetails.getUserId(), nicknameMap.get("nickname"));
        return new BaseResponse<>();
    }

    @PatchMapping("/img")
    public BaseResponse<String> updateImg(@AuthenticationPrincipal CustomUserDetails customUserDetails, MultipartFile imgFile) {
        String imgUrl = cloudFileUploadService.uploadImg(imgFile);
        userService.updateImg(customUserDetails.getUserId(), imgUrl);
        return new BaseResponse<>(imgUrl);
    }

    @PatchMapping("/password")
    public BaseResponse<String> updatePassword(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody UpdateUserPasswordReq updateUserPasswordReq) {
        if(updateUserPasswordReq.getNewPassword().equals(updateUserPasswordReq.getConfirmPassword())){
            userService.updatePassword(customUserDetails.getUserId(), updateUserPasswordReq);
        } else {
            throw new InvalidUserException(BaseResponseStatus.USER_NEW_PASSWORDS_DO_NOT_MATCH);
        }
        return new BaseResponse<>();
    }

    @PostMapping("/password/verify")
    public BaseResponse<Boolean> checkPassword(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody Map<String, String> passwordMap) {
        return new BaseResponse<>(userService.checkPassword(customUserDetails.getUserId(), passwordMap.get("password")));
    }

    @PatchMapping("/quit")
    public BaseResponse<String> quit(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody Map<String, String> passwordMap) {
        userService.disableUser(customUserDetails.getUserId(), passwordMap.get("password"));
        return new BaseResponse<>();
    }

    @PostMapping("/info")
    public BaseResponse<GetUserInfoRes> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(userService.getUserInfo(customUserDetails.getUserId()));
    }
}
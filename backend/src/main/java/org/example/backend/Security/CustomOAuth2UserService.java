package org.example.backend.Security;

import lombok.RequiredArgsConstructor;
import org.example.backend.User.Model.Entity.User;
import org.example.backend.User.Repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//        String userNameAttributeName = userRequest.getClientRegistration()
//                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
//        추후 다른 소셜 로그인 추가할 때 사용

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");

        if (email == null) {
            email = fetchEmailFromGitHub(userRequest.getAccessToken().getTokenValue());
            if (email == null) {
                throw new OAuth2AuthenticationException("GitHub 이메일 정보를 가져올 수 없습니다.");
            }
        }
        Optional<User> existingUser = userRepository.findByEmail(email);

        Long userId;

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            userId = user.getId();
        } else { // 회원가입 안 된 유저
            String nickname = (String) attributes.get("login");
            String profileImg = (String) attributes.get("avatar_url");
            User user = User.builder()
                    .email(email)
                    .nickname(setNickname(nickname))
                    .profileImg(profileImg)
                    .type("Github")
                    .verify(true)
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build();
            User resultUser = userRepository.save(user);
            userId = resultUser.getId();
        }

        return new CustomOAuth2User(oAuth2User, userId, email);
    }

    private String fetchEmailFromGitHub(String accessToken) {
        String uri = "https://api.github.com/user/emails";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // GitHub /user/emails API 호출
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return Optional.ofNullable(response.getBody())
                .map(responseBody -> responseBody.stream())
                .orElseGet(() -> Stream.empty())
                .filter(emailInfo -> (Boolean) emailInfo.get("primary") && (Boolean) emailInfo.get("verified"))
                .map(emailInfo -> (String) emailInfo.get("email"))
                .findFirst()
                .orElse(null);
    }

    private String setNickname(String nickname) {
        String newNickname = nickname;
        Random random = new Random();

        while (userRepository.findByNickname(newNickname).isPresent()) {
            int randomNumber = random.nextInt(1000);
            newNickname = nickname + randomNumber;
        }

        return newNickname;
    }
}
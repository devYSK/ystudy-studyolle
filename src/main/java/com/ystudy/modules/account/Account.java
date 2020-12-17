package com.ystudy.modules.account;

import com.ystudy.modules.study.Study;
import com.ystudy.modules.tag.Tag;
import com.ystudy.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email; // 로그인

    @Column(unique = true)
    private String nickname; // 닉네임으로도 로그인 가능

    private String password;

    private boolean emailVerified; // 이메일 인증이 된 계정인지 아닌지 판단하기 위한 변수

    private String emailCheckToken; // 이메일 검증 토큰값. DB에 저장함

    private LocalDateTime joinedAt; // 가입날짜

    private String bio; //

    private String url;

    private String occupation; // 직업

    private String location; // 살고있는 지역

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage; // 프로필 이미지


    private LocalDateTime emailCheckTokenGeneratedAt; // 이메일 토큰 생성 시간

    // 알림 설정 관련

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;// 가입 신청 결과를 이메일로 받을것인가

    private boolean studyEnrollmentResultByWeb = true; // 웹으로 받을것인가

    private boolean studyUpdatedByEmail; // 갱신 정보를 이메일로 받을것인가

    private boolean studyUpdatedByWeb = true; // 웹으로 받을것인가


    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {

        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }


}

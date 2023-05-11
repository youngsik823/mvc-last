package com.spring.mvc.chap05.service;

import com.spring.mvc.chap05.dto.LoginRequestDTO;
import com.spring.mvc.chap05.dto.SignUpRequestDTO;
import com.spring.mvc.chap05.dto.response.LoginUserResponseDTO;
import com.spring.mvc.chap05.entity.Member;
import com.spring.mvc.chap05.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.spring.mvc.chap05.service.LoginResult.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder encoder;

    // 회원가입 처리 서비스
    public boolean join(final SignUpRequestDTO dto) {

        // dto를 entity로 변환
        Member member = Member.builder()
                .account(dto.getAccount())
                .email(dto.getEmail())
                .name(dto.getName())
                .password(encoder.encode(dto.getPassword()))
                .build();

        // 매퍼에게 회원정보 전달해서 저장명령
        return memberMapper.save(member);
    }

    // 중복검사 서비스 처리
    public boolean checkSignUpValue(
            String type, String keyword) {

        int flagNum = memberMapper.isDuplicate(type, keyword);

        return flagNum == 1;
    }

    // 로그인 검증
    public LoginResult authenticate(LoginRequestDTO dto) {

        Member foundMember = memberMapper.findMember(dto.getAccount());

        // 회원가입 여부 확인
        if (foundMember == null) {
            log.info("{} - 회원가입 안했음ㅋ", dto.getAccount());
            return NO_ACC;
        }
        // 비밀번호 일치 확인
        if (!encoder.matches(dto.getPassword(), foundMember.getPassword())) {
            log.info("비밀번호 불일치!");
            return NO_PW;
        }

        log.info("{}님 로그인 성공!", foundMember.getName());
        return SUCCESS;

    }

    public void maintainLoginState(HttpSession session, String account) {
        // 로그인이 성공하면 세션에 로그인한 회원의 정보들을 저장
        /*
            로그인시 클라이언트에게 전달할 회원정보
            - 닉네임
            - 프로필사진
            -마지막 로그인 시간
         */

        Member member = getMember(account);

        // 현재 로그인한 사람의 화면에 보여줄 일부정보
        LoginUserResponseDTO dto = new LoginUserResponseDTO().builder()
                .account(member.getAccount())
                .nickName(member.getName())
                .email(member.getEmail())
                .build();
        session.setAttribute("login", dto);

        // 세션의 수명을 설정
        session.setMaxInactiveInterval(60 * 60); // 1시간

    }
    //  멤버 정보를 가져오는 서비스기능
    public Member getMember(String account) {
        return memberMapper.findMember(account);
    }
}

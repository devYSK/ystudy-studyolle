package com.ystudy.modules.account.validator;


import com.ystudy.modules.account.AccountRepository;
import com.ystudy.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component // 주입받아야 하니까 빈으로 등록
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {

        SignUpForm signUpForm = (SignUpForm)o;

        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email",
                    "invalid.email",
                    new Object[]{signUpForm.getEmail()}, "중복된 이메일 입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname",
                    "invalid.nickname",
                    new Object[]{signUpForm.getEmail()}, "중복된 닉네임 입니다.");

        }

    }
}

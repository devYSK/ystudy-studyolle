package com.ystudy.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class Profile {
    @Length(max = 35)
    private String bio; //

    @Length(max = 50)
    private String url;

    @Length(max = 30)
    private String occupation; // 직업

    private String location; // 살고있는 지역

    private String profileImage;

}

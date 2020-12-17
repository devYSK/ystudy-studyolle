package com.ystudy.modules.main;

import com.ystudy.modules.account.Account;
import com.ystudy.modules.account.CurrentAccount;
import com.ystudy.modules.notification.NotificationRepository;
import com.ystudy.modules.study.Study;
import com.ystudy.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final NotificationRepository notificationRepository;
    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        Long count = notificationRepository.countByAccountAndChecked(account, false);
        model.addAttribute("hasNotification", count > 0);

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model) {
        List<Study> studyList =  studyRepository.findByKeyword(keyword);
        model.addAttribute(studyList);
        model.addAttribute("keyword", keyword);
        return "search";
    }

}

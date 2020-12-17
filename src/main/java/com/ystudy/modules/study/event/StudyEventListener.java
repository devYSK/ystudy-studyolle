package com.ystudy.modules.study.event;

import com.ystudy.infra.config.AppProperties;
import com.ystudy.infra.mail.EmailMessage;
import com.ystudy.infra.mail.EmailService;
import com.ystudy.modules.account.Account;
import com.ystudy.modules.account.AccountPredicates;
import com.ystudy.modules.account.AccountRepository;
import com.ystudy.modules.notification.Notification;
import com.ystudy.modules.notification.NotificationRepository;
import com.ystudy.modules.notification.NotificationType;
import com.ystudy.modules.study.Study;
import com.ystudy.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndJonesById(studyCreatedEvent.getStudy().getId());

        log.info("event!!! email title : {}", study.getTitle() );

        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates
                .findByTagsAndZones(study.getTags(), study.getZones()));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendStudyCreatedEmail(study, account, "새로운 스터디가 생겼습니다",
                        "스터디올래, '" + study.getTitle() + "' 스터디가 생겼습니다.");
                log.info("study notification! email title : {}", study.getTitle() );
            }
            if (account.isStudyCreatedByWeb()) {
                createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });


    }

    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMemberById(studyUpdateEvent.getStudy().getId());

        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if (account.isStudyUpdatedByEmail()) {
                sendStudyCreatedEmail(study, account, studyUpdateEvent.getMessage(),
                        "스터디올래, '" + study.getTitle() + "' 스터디에 새소식이 있습니다.");
            }

            if (account.isStudyCreatedByWeb()) {
                createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }

        });

    }


    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendStudyCreatedEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}

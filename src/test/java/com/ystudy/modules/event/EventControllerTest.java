package com.ystudy.modules.event;

import com.ystudy.infra.AbstractContainerBaseTest;
import com.ystudy.infra.MockMvcTest;
import com.ystudy.modules.account.AccountFactory;
import com.ystudy.modules.account.AccountRepository;
import com.ystudy.modules.account.WithAccount;
import com.ystudy.modules.account.Account;
import com.ystudy.modules.study.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;


    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("youngsoo")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account kim = accountFactory.createAccount("kim");
        Study study = studyFactory.createStudy("test-study", kim);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kim);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account youngsoo = accountRepository.findByNickname("youngsoo");
        isAccepted(youngsoo, event);

    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 ( 인원이 꽉차서 ) ")
    @WithAccount("youngsoo")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account kim = accountFactory.createAccount("kim");
        Study study = studyFactory.createStudy("test-study", kim);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kim);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, june);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account youngsoo = accountRepository.findByNickname("youngsoo");
        isNotAccepted(youngsoo, event);

    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 취소")
    @WithAccount("youngsoo")
    void cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account youngsoo = accountRepository.findByNickname("youngsoo");
        Account kim = accountFactory.createAccount("kim");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", kim);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kim);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, youngsoo);
        eventService.newEnrollment(event, kim);

        isAccepted(may, event);
        isAccepted(youngsoo, event);
        isNotAccepted(kim, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(kim, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, youngsoo));


    }

    private void isNotAccepted(Account account, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }
}
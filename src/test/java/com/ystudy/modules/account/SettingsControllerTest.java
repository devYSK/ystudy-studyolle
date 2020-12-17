package com.ystudy.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ystudy.infra.AbstractContainerBaseTest;
import com.ystudy.infra.MockMvcTest;
import com.ystudy.modules.account.WithAccount;
import com.ystudy.modules.account.Account;
import com.ystudy.modules.account.AccountRepository;
import com.ystudy.modules.account.AccountService;
import com.ystudy.modules.account.SettingsController;
import com.ystudy.modules.tag.Tag;
import com.ystudy.modules.tag.TagForm;
import com.ystudy.modules.tag.TagRepository;
import com.ystudy.modules.zone.Zone;
import com.ystudy.modules.zone.ZoneForm;
import com.ystudy.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.ystudy.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MockMvcTest
class SettingsControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;

    @Autowired AccountService accountService;

    @Autowired AccountRepository accountRepository;

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder()
                                .city("test")
                                .localNameOfCity("테스트시")
                                .province("테스트주")
                                .build();
    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("youngsoo")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZoneForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("youngsoo")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account youngsoo = accountRepository.findByNickname("youngsoo");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(youngsoo.getZones().contains(zone));
    }

    @WithAccount("youngsoo")
    @DisplayName("계정의 지역 정보 삭제")
    @Test
    void removeZone() throws Exception {
        Account youngsoo = accountRepository.findByNickname("youngsoo");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(youngsoo, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
            .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());
    }




    @WithAccount("youngsoo")
    @DisplayName("계정의 태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {

        mockMvc.perform(get(SettingsController.ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));

    }

    @WithAccount("youngsoo")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());



        Optional<Tag> newTag = tagRepository.findByTitle("newTag");

        assertNotNull(newTag.orElseThrow());

        Account youngsoo = accountRepository.findByNickname("youngsoo");

        assertTrue(youngsoo.getTags().contains(newTag.get()));
    }

    @WithAccount("youngsoo")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account youngsoo = accountRepository.findByNickname("youngsoo");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());

        accountService.addTag(youngsoo, newTag);

        assertTrue(youngsoo.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(youngsoo.getTags().contains(newTag));
    }



    @WithAccount("youngsoo")
    @DisplayName("프로필 수정 폼 ")
    @Test
    void updateProfileForm() throws Exception {

        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }

    @WithAccount("youngsoo")
    @DisplayName("프로필 수정 하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {

        String bio = "짧은 소개를 수정 하는 경우 ";

        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account youngsoo = accountRepository.findByNickname("youngsoo");
        assertEquals(bio, youngsoo.getBio());
    }


    @WithAccount("youngsoo")
    @DisplayName("프로필 수정 하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {

        String bio = "길게 소개를 수정 하는 경우길게 소개를 수정 하는 경우길게 소개를 수정 하는 경우길게 소개를 수정 하는 경우길게 소개를 수정 하는 경우길게 소개를 수정 하는 경우";

        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());

        Account youngsoo = accountRepository.findByNickname("youngsoo");
        assertNull(youngsoo.getBio());
    }

    @WithAccount("youngsoo")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("youngsoo")
    @DisplayName("패스워드 수정 - 입력값 정상상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account youngsoo = accountRepository.findByNickname("youngsoo");
        assertTrue(passwordEncoder.matches("12345678", youngsoo.getPassword()));
    }


    @WithAccount("youngsoo")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_fail() throws Exception {

        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "123456678")
                .param("newPasswordConfirm", "1234567899")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }


}
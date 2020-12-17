package com.ystudy.modules.study;

import com.ystudy.modules.tag.Tag;
import com.ystudy.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    List<Study> findByKeyword(String keyword);

    List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones);
}

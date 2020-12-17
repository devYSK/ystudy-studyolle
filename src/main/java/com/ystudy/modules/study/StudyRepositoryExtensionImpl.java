package com.ystudy.modules.study;

import com.querydsl.jpa.JPQLQuery;
import com.ystudy.modules.tag.Tag;
import com.ystudy.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public List<Study> findByKeyword(String keyword) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)));

        return query.fetch();
    }

    @Override
    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        return null;
    }
}

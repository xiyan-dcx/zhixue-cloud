package com.zhixue;

import com.zhixue.business.controller.ChapterController;
import com.zhixue.business.domain.Chapter;
import com.zhixue.business.domain.Course;
import com.zhixue.business.service.ChapterService;
import com.zhixue.business.service.CourseService;
import com.zhixue.common.exception.ServiceException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChapterAccessTests {
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean com.zhixue.business.mapper.ChapterMapper chapterMapper() { return mock(com.zhixue.business.mapper.ChapterMapper.class); }
        @Bean com.zhixue.business.mapper.CourseMapper courseMapper() { return mock(com.zhixue.business.mapper.CourseMapper.class); }
        @Bean ChapterService chapters() { return mock(ChapterService.class); }
        @Bean CourseService courses() { return mock(CourseService.class); }
        @Bean ChapterController controller(ChapterService chapters, CourseService courses) {
            return new ChapterController(chapters, courses, mock(com.zhixue.business.mapper.MyCourseMapper.class));
        }
    }

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void studentCanReadButCannotWriteEvenWithEditPermission() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student", "unused",
                    AuthorityUtils.createAuthorityList("student", "course:list", "course:edit")));
            var controller = context.getBean(ChapterController.class);
            assertDoesNotThrow(() -> controller.list(1L));
            assertThrows(AccessDeniedException.class, () -> controller.add(chapter()));
            assertThrows(AccessDeniedException.class, () -> controller.update(chapter()));
            assertThrows(AccessDeniedException.class, () -> controller.remove(1L));
            verify(context.getBean(ChapterService.class)).listByCourse(1L);
            verifyNoMoreInteractions(context.getBean(ChapterService.class));
        }
    }

    @Test void editorSavesLectureWithoutMovingCourseOrChangingDeletionFlag() {
        var chapters = mock(ChapterService.class);
        var controller = new ChapterController(chapters, mock(CourseService.class), mock(com.zhixue.business.mapper.MyCourseMapper.class));
        var existing = chapter();
        existing.setDeleted(0);
        when(chapters.getById(1L)).thenReturn(existing);
        var input = chapter();
        input.setContent("学习目标\n示例与练习");
        input.setDeleted(1);
        controller.update(input);
        assertEquals(input.getContent(), existing.getContent());
        assertEquals(0, existing.getDeleted());
        verify(chapters).updateById(existing);
        input.setCourseId(2L);
        assertThrows(ServiceException.class, () -> controller.update(input));
    }

    @Test void addingChapterRequiresExistingCourseAndValidFields() {
        var chapters = mock(ChapterService.class);
        var courses = mock(CourseService.class);
        var controller = new ChapterController(chapters, courses, mock(com.zhixue.business.mapper.MyCourseMapper.class));
        assertThrows(ServiceException.class, () -> controller.add(chapter()));
        verifyNoInteractions(chapters);
        when(courses.getById(1L)).thenReturn(new Course());
        var valid = chapter();
        controller.add(valid);
        assertNull(valid.getId());
        verify(chapters).save(valid);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            valid.setTitle(" ");
            valid.setDuration(-1);
            valid.setSortOrder(0);
            assertEquals(3, factory.getValidator().validate(valid).size());
        }
    }

    private Chapter chapter() {
        var chapter = new Chapter();
        chapter.setId(1L);
        chapter.setCourseId(1L);
        chapter.setTitle("示例章节");
        chapter.setSortOrder(1);
        chapter.setDuration(30);
        return chapter;
    }
}

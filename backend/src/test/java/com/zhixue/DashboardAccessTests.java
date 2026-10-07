package com.zhixue;

import com.zhixue.business.controller.DashboardController;
import com.zhixue.business.service.DashboardService;
import com.zhixue.system.domain.SysMenu;
import com.zhixue.system.mapper.SysMenuMapper;
import com.zhixue.system.service.SysMenuService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardAccessTests {
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean DashboardService service() { return mock(DashboardService.class); }
        @Bean DashboardController controller(DashboardService service) {
            return new DashboardController(service);
        }
    }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private void authenticate(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-user", "unused",
                        AuthorityUtils.createAuthorityList(authorities)));
    }

    @Test
    void studentCannotCallAnyDashboardMethodEvenWithStaleMenuPermission() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            authenticate("student", "dashboard:view");
            var controller = context.getBean(DashboardController.class);
            assertThrows(AccessDeniedException.class, controller::overview);
            assertThrows(AccessDeniedException.class, controller::enrollTrend);
            assertThrows(AccessDeniedException.class, controller::courseTop10);
            assertThrows(AccessDeniedException.class, controller::teacherRevenue);
            assertThrows(AccessDeniedException.class, controller::monthRevenue);
            verifyNoInteractions(context.getBean(DashboardService.class));
        }
    }

    @Test
    void adminAndTeacherKeepDashboardAccess() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var controller = context.getBean(DashboardController.class);
            for (String role : List.of("admin", "teacher")) {
                authenticate(role);
                assertDoesNotThrow(controller::overview);
                assertDoesNotThrow(controller::enrollTrend);
                assertDoesNotThrow(controller::courseTop10);
                assertDoesNotThrow(controller::teacherRevenue);
                assertDoesNotThrow(controller::monthRevenue);
            }
            verify(context.getBean(DashboardService.class), times(2)).overview();
        }
    }

    @Test
    void studentMenuOmitsDashboardEvenWhenDatabaseStillGrantsIt() {
        var mapper = mock(SysMenuMapper.class);
        var dashboard = menu(1L, "dashboard");
        var course = menu(2L, "/course");
        when(mapper.selectMenusByUserId(3L)).thenReturn(List.of(dashboard, course));
        var service = new SysMenuService(mapper);
        assertEquals(List.of(course), service.buildRouters(3L, "student"));
        dashboard.setPath("/dashboard/");
        assertEquals(List.of(course), service.buildRouters(3L, "student"));
        assertEquals(2, service.buildRouters(3L, "admin").size());
        assertEquals(2, service.buildRouters(3L, "teacher").size());
    }

    private SysMenu menu(long id, String path) {
        var menu = new SysMenu();
        menu.setId(id);
        menu.setParentId(0L);
        menu.setPath(path);
        return menu;
    }
}

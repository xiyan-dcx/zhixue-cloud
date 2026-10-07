package com.zhixue;

import com.zhixue.business.controller.MyCourseController;
import com.zhixue.business.domain.Course;
import com.zhixue.business.mapper.MyCourseMapper;
import com.zhixue.business.service.CourseService;
import com.zhixue.common.exception.ServiceException;
import com.zhixue.common.security.LoginUser;
import com.zhixue.common.utils.JwtUtils;
import com.zhixue.system.controller.ProfileController;
import com.zhixue.system.domain.SysUser;
import com.zhixue.system.mapper.SysUserMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonalFeaturesTests {
    @TempDir Path temp;
    @BeforeEach void authenticate() {
        var user = new LoginUser(); user.setUserId(42L); user.setRoleKey("student"); user.setPermissions(Set.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void listAndRemoveAlwaysUseAuthenticatedUserId() {
        var mapper = mock(MyCourseMapper.class);
        when(mapper.list(42L, 0, 12)).thenReturn(List.of());
        var controller = new MyCourseController(mapper, mock(CourseService.class));
        controller.list(1, 12); controller.remove(7L);
        verify(mapper).list(42L, 0, 12); verify(mapper).count(42L); verify(mapper).remove(42L, 7L);
        assertThrows(ServiceException.class, () -> controller.list(0, 12));
        assertThrows(ServiceException.class, () -> controller.list(1, 999));
    }

    @Test void onlyPublishedCoursesCanBeJoined() {
        var mapper = mock(MyCourseMapper.class); var courses = mock(CourseService.class);
        var controller = new MyCourseController(mapper, courses);
        assertThrows(ServiceException.class, () -> controller.join(7L, null));
        var course = new Course(); course.setStatus(0); when(mapper.lockCourse(7L)).thenReturn(course);
        assertThrows(ServiceException.class, () -> controller.join(7L, null));
        verify(mapper, never()).join(anyLong(), anyLong());
        course.setStatus(1); course.setPrice(java.math.BigDecimal.ZERO); when(mapper.join(42L, 7L)).thenReturn(1); controller.join(7L, null); verify(mapper).join(42L, 7L);
    }

    @Test void profileOnlyChangesOwnNicknameAndReturnsNoPassword() {
        var mapper = mock(SysUserMapper.class); var user = new SysUser();
        user.setId(42L); user.setUsername("sample"); user.setPassword("private");
        when(mapper.selectById(42L)).thenReturn(user);
        var controller = new ProfileController(mapper, new BCryptPasswordEncoder());
        controller.update(new ProfileController.ProfileBody(" new name "));
        verify(mapper).updateOwnNickname(42L, "new name");
        assertFalse(controller.get().getData().containsKey("password"));
    }

    @Test void paidPurchaseRequiresConfirmationAndCorrectPriceAndAvoidsDuplicates() {
        var mapper = mock(MyCourseMapper.class);
        var controller = new MyCourseController(mapper, mock(CourseService.class));
        var course = new Course(); course.setStatus(1); course.setTitle("Paid course"); course.setPrice(new java.math.BigDecimal("199.00"));
        when(mapper.lockCourse(7L)).thenReturn(course);
        assertThrows(ServiceException.class, () -> controller.join(7L, null));
        assertThrows(ServiceException.class, () -> controller.join(7L, new MyCourseController.PurchaseBody(true, java.math.BigDecimal.ONE)));
        verify(mapper, never()).savePurchase(anyString(), anyLong(), anyLong(), anyString(), any());
        when(mapper.join(42L, 7L)).thenReturn(1);
        var result = controller.join(7L, new MyCourseController.PurchaseBody(true, course.getPrice())).getData();
        assertTrue(result.get("orderNo").toString().startsWith("DEMO-"));
        verify(mapper).savePurchase(anyString(), eq(42L), eq(7L), eq(course.getTitle()), eq(course.getPrice()));
        when(mapper.purchase(42L,7L)).thenReturn(java.util.Map.of("orderNo", result.get("orderNo"), "amount", course.getPrice()));
        assertThrows(ServiceException.class, () -> controller.join(7L, null));
        verify(mapper, times(1)).join(42L,7L);
        controller.restore(7L);
        verify(mapper, times(2)).join(42L,7L);
        verify(mapper, times(1)).savePurchase(anyString(), anyLong(), anyLong(), anyString(), any());
    }

    @Test void passwordRequiresCorrectOldPasswordAndUsesEncodedReplacement() {
        var mapper = mock(SysUserMapper.class); var encoder = new BCryptPasswordEncoder();
        var user = new SysUser(); user.setId(42L); user.setPassword(encoder.encode("old-password"));
        when(mapper.selectById(42L)).thenReturn(user);
        var controller = new ProfileController(mapper, encoder);
        assertThrows(ServiceException.class, () -> controller.password(new ProfileController.PasswordBody("wrong", "new-password")));
        verify(mapper, never()).updateOwnPassword(any(), any(), any());
        assertThrows(ServiceException.class, () -> controller.password(new ProfileController.PasswordBody("old-password", "old-password")));
        when(mapper.updateOwnPassword(eq(42L), eq(user.getPassword()), anyString())).thenReturn(1);
        controller.password(new ProfileController.PasswordBody("old-password", "new-password"));
        var encoded = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mapper).updateOwnPassword(eq(42L), eq(user.getPassword()), encoded.capture());
        assertTrue(encoder.matches("new-password", encoded.getValue()));
    }

    @Test void passwordVersionRejectsOldToken() {
        var jwt = new JwtUtils();
        ReflectionTestUtils.setField(jwt, "secret", "personal-features-test-secret-at-least-32-characters");
        ReflectionTestUtils.setField(jwt, "expire", 3600L);
        String token = jwt.createToken("sample", 0);
        assertEquals("sample", jwt.parseUsername(token));
        assertTrue(jwt.matchesVersion(token, 0));
        assertFalse(jwt.matchesVersion(token, 1));
        assertFalse(jwt.matchesVersion("invalid", 0));
    }

    @Test void avatarReencodesImageAndOnlyUpdatesCurrentUser() throws Exception {
        var mapper = mock(SysUserMapper.class); var controller = new ProfileController(mapper, new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(controller, "uploadPath", temp.toString());
        var image = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var bytes = new java.io.ByteArrayOutputStream(); javax.imageio.ImageIO.write(image, "png", bytes);
        var result = controller.avatar(new MockMultipartFile("file", "../../avatar.png", "image/png", bytes.toByteArray()));
        String url = result.getData().get("url");
        assertTrue(url.startsWith("/uploads/avatars/"));
        assertNotNull(javax.imageio.ImageIO.read(temp.resolve("avatars").resolve(Path.of(url).getFileName()).toFile()));
        verify(mapper).updateOwnAvatar(42L, url);
        assertThrows(ServiceException.class, () -> controller.avatar(new MockMultipartFile("file", "fake.png", "image/png", "not an image".getBytes())));
    }
}

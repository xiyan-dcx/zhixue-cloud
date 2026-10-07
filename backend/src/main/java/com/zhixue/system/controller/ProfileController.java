package com.zhixue.system.controller;

import com.zhixue.common.core.R;
import com.zhixue.common.exception.ServiceException;
import com.zhixue.common.utils.SecurityUtils;
import com.zhixue.system.domain.SysUser;
import com.zhixue.system.mapper.SysUserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final SysUserMapper users;
    private final PasswordEncoder encoder;
    @Value("${zhixue.upload.path}") private String uploadPath;

    public record ProfileBody(@NotBlank @Size(max=64) String nickname) {}
    public record PasswordBody(@NotBlank String oldPassword,
                               @NotBlank @Size(min=8, max=64) String newPassword) {}

    @GetMapping
    public R<Map<String, Object>> get() {
        var user = currentUser();
        return R.ok(Map.of("username", user.getUsername(), "nickname", user.getNickname() == null ? "" : user.getNickname(),
                "avatar", user.getAvatar() == null ? "" : user.getAvatar(),
                "roleKey", SecurityUtils.getLoginUser().getRoleKey()));
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody ProfileBody body) {
        users.updateOwnNickname(SecurityUtils.getUserId(), body.nickname().trim());
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> password(@Valid @RequestBody PasswordBody body) {
        var user = currentUser();
        if (!encoder.matches(body.oldPassword(), user.getPassword())) throw new ServiceException("原密码不正确");
        if (encoder.matches(body.newPassword(), user.getPassword())) throw new ServiceException("新密码不能与原密码相同");
        if (body.newPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ServiceException("新密码的 UTF-8 编码不能超过72字节");
        if (users.updateOwnPassword(user.getId(), user.getPassword(), encoder.encode(body.newPassword())) != 1)
            throw new ServiceException("密码已变更，请重新登录后操作");
        return R.ok();
    }

    @PostMapping("/avatar")
    public R<Map<String, String>> avatar(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        if (file.isEmpty() || file.getSize() > 2 * 1024 * 1024) throw new ServiceException("请选择不超过2MB的图片");
        try (var stream = ImageIO.createImageInputStream(file.getInputStream())) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new ServiceException("仅支持PNG、JPEG图片");
            var reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("PNG") && !format.equalsIgnoreCase("JPEG")) throw new ServiceException("仅支持PNG、JPEG图片");
                reader.setInput(stream);
                if (reader.getWidth(0) > 2048 || reader.getHeight(0) > 2048) throw new ServiceException("头像尺寸不能超过2048×2048");
                var image = reader.read(0);
                String filename = UUID.randomUUID() + ".png";
                Path directory = Path.of(uploadPath, "avatars");
                Files.createDirectories(directory);
                ImageIO.write(image, "png", directory.resolve(filename).toFile());
                String url = "/uploads/avatars/" + filename;
                users.updateOwnAvatar(SecurityUtils.getUserId(), url);
                return R.ok(Map.of("url", url));
            } finally { reader.dispose(); }
        }
    }

    private SysUser currentUser() {
        var user = users.selectById(SecurityUtils.getUserId());
        if (user == null) throw new ServiceException(401, "账号不存在");
        return user;
    }
}

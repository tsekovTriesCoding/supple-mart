package app.admin.service;

import app.admin.dto.AdminUsersResponse;
import app.admin.mapper.AdminMapper;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserService userService;
    private final AdminMapper adminMapper;

    public AdminUsersResponse getAllUsers(String search, String role, Integer page, Integer size) {
        log.info("Admin: Fetching all users - page: {}, size: {}, role: {}", page, size, role);

        Page<User> userPage = userService.getAllUsers(search, role, page, size);

        return adminMapper.toAdminUsersResponse(userPage);
    }
}


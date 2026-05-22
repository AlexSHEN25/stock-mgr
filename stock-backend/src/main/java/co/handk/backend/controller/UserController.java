package co.handk.backend.controller;

import co.handk.backend.context.UserContext;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.LoginService;
import co.handk.backend.service.UserService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.dto.update.ChangePasswordDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginService loginService;
    private final PermissionQueryService permissionQueryService;

    @PostMapping("/login")
    public LoginVO login(@RequestBody @NotNull @Valid LoginDTO dto) {
        return loginService.login(dto);
    }

    @PostMapping("/logout")
    public LogoutVO logout() {
        return loginService.logout();
    }

    /**
     * Paged query.
     */
    @PostMapping("/page")
    public PageResult<UserVO> page(@RequestBody UserQueryDTO dto) {
        return userService.page(dto);
    }

    /**
     * List query.
     */
    @PostMapping("/list")
    public List<UserVO> list(@RequestBody UserQueryDTO dto) {
        return userService.list(dto);
    }

    /**
     * Query by id.
     */
    @GetMapping("/{id:\\d+}")
    public UserVO get(@PathVariable("id") Long id) {
        return userService.getVOById(id);
    }

    /**
     * Create user.
     */
    @PostMapping
    public boolean create(@RequestBody CreateUserDTO dto) {
        return userService.saveByDto(dto);
    }

    /**
     * Update user.
     */
    @PutMapping("/{id:\\d+}")
    public boolean update(@PathVariable("id") Long id, @RequestBody @NotNull @Valid UpdateUserDTO dto) {
        dto.setId(id);
        return userService.updateByDto(dto);
    }

    /**
     * Change user password.
     */
    @PutMapping("/{id:\\d+}/password")
    public boolean changePassword(@PathVariable("id") Long id, @RequestBody @NotNull @Valid ChangePasswordDTO dto) {
        return userService.changePassword(id, dto);
    }

    /**
     * Logical delete by id.
     */
    @DeleteMapping("/{id:\\d+}")
    public int delete(@PathVariable("id") Long id) {
        return userService.deleteByIdLogic(id);
    }

    /**
     * Current login user's permission codes.
     */
    @GetMapping("/permissions")
    public Set<String> permissions() {
        Long userId = UserContext.getUserIdOrDefault();
        return permissionQueryService.getPermissionCodes(userId);
    }

    /**
     * Batch logical delete.
     */
    @DeleteMapping("/batch")
    public int deleteBatch(@RequestBody List<Long> ids) {
        return userService.deleteBatchLogic(ids);
    }
}
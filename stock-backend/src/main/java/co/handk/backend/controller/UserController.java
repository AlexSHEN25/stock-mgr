package co.handk.backend.controller;

import co.handk.backend.service.LoginService;
import co.handk.backend.service.UserService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final LoginService loginService;

    @PostMapping("/login")
    public LoginVO login(@RequestBody @NotNull @Valid LoginDTO dto) {
        return loginService.login(dto);
    }

    @PostMapping("/logout")
    public LogoutVO logout() {
        return loginService.logout();
    }

    /**
     * 蛻・｡ｵ譟･隸｢
     */
    @PostMapping("/page")
    public PageResult<UserVO> page(@RequestBody UserQueryDTO dto) {
        return userService.page(dto);
    }

    /**
     * 蛻苓｡ｨ譟･隸｢・井ｸ榊・鬘ｵ・・
     */
    @PostMapping("/list")
    public List<UserVO> list(@RequestBody UserQueryDTO dto) {
        return userService.list(dto);
    }

    /**
     * 譬ｹ謐ｮID譟･隸｢
     */
    @GetMapping("/{id}")
    public UserVO get(@PathVariable("id") Long id) {
        return userService.getVOById(id);
    }

    /**
     * 譁ｰ蠅・
     */
    @PostMapping
    public boolean create(@RequestBody CreateUserDTO dto) {
        return userService.saveByDto(dto);
    }

    /**
     * 譖ｴ譁ｰ
     */
    @PutMapping("/{id}")
    public boolean update(@RequestBody UpdateUserDTO dto) {
        return userService.updateByDto(dto);
    }

    /**
     * 蜊墓擅騾ｻ霎大唖髯､
     */
    @DeleteMapping("/{id}")
    public int delete(@PathVariable("id") Long id) {
        return userService.deleteByIdLogic(id);
    }

    /**
     * 謇ｹ驥城ｻ霎大唖髯､
     */
    @DeleteMapping("/batch")
    public int deleteBatch(@RequestBody List<Long> ids) {
        return userService.deleteBatchLogic(ids);
    }
}
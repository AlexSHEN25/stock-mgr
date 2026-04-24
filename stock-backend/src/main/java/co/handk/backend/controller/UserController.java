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
     * 分页查询
     */
    @PostMapping("/page")
    public PageResult<UserVO> page(@RequestBody UserQueryDTO dto) {
        return userService.page(dto);
    }

    /**
     * 列表查询（不分页）
     */
    @PostMapping("/list")
    public List<UserVO> list(@RequestBody UserQueryDTO dto) {
        return userService.list(dto);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public UserVO get(@PathVariable Long id) {
        return userService.getVOById(id);
    }

    /**
     * 新增
     */
    @PostMapping
    public boolean create(@RequestBody CreateUserDTO dto) {
        return userService.saveByDto(dto);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public boolean update(@RequestBody UpdateUserDTO dto) {
        return userService.updateByDto(dto);
    }

    /**
     * 单条逻辑删除
     */
    @DeleteMapping("/{id}")
    public int delete(@PathVariable Long id) {
        return userService.deleteByIdLogic(id);
    }

    /**
     * 批量逻辑删除
     */
    @DeleteMapping("/batch")
    public int deleteBatch(@RequestBody List<Long> ids) {
        return userService.deleteBatchLogic(ids);
    }
}
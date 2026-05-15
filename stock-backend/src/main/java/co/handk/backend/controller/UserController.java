package co.handk.backend.controller;

import co.handk.backend.service.LoginService;
import co.handk.backend.service.UserService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
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
     * 陋ｻ繝ｻ・｡・ｵ隴滂ｽ･髫ｸ・｢
     */
    @PostMapping("/page")
    public PageResult<UserVO> page(@RequestBody UserQueryDTO dto) {
        return userService.page(dto);
    }

    /**
     * 陋ｻ闍難ｽ｡・ｨ隴滂ｽ･髫ｸ・｢繝ｻ莠包ｽｸ讎翫・鬯假ｽｵ繝ｻ繝ｻ
     */
    @PostMapping("/list")
    public List<UserVO> list(@RequestBody UserQueryDTO dto) {
        return userService.list(dto);
    }

    /**
     * 隴ｬ・ｹ隰撰ｽｮID隴滂ｽ･髫ｸ・｢
     */
    @GetMapping("/{id}")
    public UserVO get(@PathVariable("id") Long id) {
        return userService.getVOById(id);
    }

    /**
     * 隴・ｽｰ陟・・
     */
    @PostMapping
    public boolean create(@RequestBody CreateUserDTO dto) {
        return userService.saveByDto(dto);
    }

    /**
     * 隴厄ｽｴ隴・ｽｰ
     */
    @PutMapping("/{id}")
    public boolean update(@RequestBody UpdateUserDTO dto) {
        return userService.updateByDto(dto);
    }

    /**
     * 陷雁｢捺套鬨ｾ・ｻ髴主､ｧ蜚夜ｫｯ・､
     */
    @DeleteMapping("/{id}")
    public int delete(@PathVariable("id") Long id) {
        return userService.deleteByIdLogic(id);
    }

    /**
     * 隰・ｽｹ鬩･蝓篠・ｻ髴主､ｧ蜚夜ｫｯ・､
     */
    @DeleteMapping("/batch")
    public int deleteBatch(@RequestBody List<Long> ids) {
        return userService.deleteBatchLogic(ids);
    }
}
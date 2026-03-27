package co.handk.backend.controller;

import co.handk.backend.entity.User;
import co.handk.backend.service.UserService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.UserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginVO login(@RequestBody @NotNull @Valid LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/logout")
    public LogoutVO logout(HttpServletRequest request){
        return userService.logout();
    }

    // 新增
    @PostMapping
    public Boolean save(@RequestBody @NotNull @Valid UserDTO dto) {
        User entity = new User();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return userService.save(entity);
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public User getById(@PathVariable @NotNull Long id) {
        return userService.getById(id);
    }

    // 修改
    @PutMapping
    public boolean update(@RequestBody @NotNull @Valid UserDTO dto) {
        User entity = new User();
        BeanUtils.copyProperties(dto, entity);
        return userService.updateById(entity);
    }

    // 删除
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable @NotNull Long id) {
        return userService.removeById(id);
    }

    // 查询全部
    @GetMapping("/list")
    public List<User> list() {
        return userService.list();
    }

    // 分页查询
    @GetMapping("/page")
    public PageResult<User> page(@Valid PageQuery query) {
        return userService.pageQuery(query);
    }
}

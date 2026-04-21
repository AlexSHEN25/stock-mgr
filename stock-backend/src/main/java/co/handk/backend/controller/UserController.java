package co.handk.backend.controller;


import co.handk.backend.service.UserService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import co.handk.schema.builder.SchemaBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/user")
public class UserController{

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginVO login(@RequestBody @NotNull @Valid LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/logout")
    public LogoutVO logout() {
        return userService.logout();
    }

    // 新增
    @PostMapping
    public Boolean save(@RequestBody @NotNull @Valid CreateUserDTO dto) {
        return userService.create(dto);
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public UserVO getById(@PathVariable @NotNull Long id) {
        return userService.get(id);
    }

    // 修改
    @PutMapping
    public boolean update(@RequestBody @NotNull @Valid UpdateUserDTO dto) {
        return userService.update(dto);
    }

    // 删除
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable @NotNull Long id) {
        return userService.delete(id);
    }

    // 条件分页查询
    @GetMapping("/schema")
    public Object schema() {
        return SchemaBuilder.build(UserVO.class);
    }

    // 分页查询
    @GetMapping("/page")
    public PageResult<UserVO> page(@Valid UserQueryDTO query) {
        return userService.pageQuery(query);
    }
}

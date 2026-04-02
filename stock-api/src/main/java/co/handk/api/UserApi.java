package co.handk.api;

import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
@RequestMapping("/user")
public interface UserApi {


    @PostMapping("/login")
    LoginVO login(@RequestBody @NotNull @Valid LoginDTO dto);

    @PostMapping("/logout")
    LogoutVO logout();

    // 新增
    @PostMapping
    Boolean save(@RequestBody @NotNull @Valid CreateUserDTO dto);

    // 根据ID查询
    @GetMapping("/{id}")
    UserVO getById(@PathVariable @NotNull Long id);

    // 修改
    @PutMapping
    boolean update(@RequestBody @NotNull @Valid UpdateUserDTO dto);

    // 删除
    @DeleteMapping("/{id}")
    boolean delete(@PathVariable @NotNull Long id);

    // 条件分页查询

    // 分页查询
    @GetMapping("/page")
    PageResult<UserVO> page(@Valid UserQueryDTO query);
}

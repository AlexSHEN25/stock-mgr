package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import co.handk.common.model.vo.UserTokenVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/userToken")
public interface UserTokenApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateUserTokenDTO dto);
    @GetMapping("/{id}")
    UserTokenVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateUserTokenDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<UserTokenVO> page(@Valid UserTokenQueryDTO query);
}

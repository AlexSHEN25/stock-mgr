package co.handk.backend.controller;

import co.handk.common.model.vo.UserTokenVO;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import co.handk.backend.service.UserTokenService;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/userToken")
public class UserTokenController {
    @Autowired
    private UserTokenService userTokenService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateUserTokenDTO dto) {
        return userTokenService.create(dto);
    }
    @GetMapping("/{id}")
    public UserTokenVO get(@PathVariable @NotNull Long id) {
        return userTokenService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateUserTokenDTO dto) {
        return userTokenService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return userTokenService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<UserTokenVO> page(@Valid UserTokenQueryDTO query) {
        return userTokenService.pageQuery(query);
    }
}

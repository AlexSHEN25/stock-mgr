package co.handk.backend.controller;

import co.handk.backend.service.UserTokenService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import co.handk.common.model.vo.UserTokenVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/userToken")
public class UserTokenController {
    @Autowired
    private UserTokenService userTokenService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateUserTokenDTO dto) {
        return userTokenService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public UserTokenVO get(@PathVariable("id") @NotNull Long id) {
        return userTokenService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateUserTokenDTO dto) {
        return userTokenService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return userTokenService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<UserTokenVO> page(@Valid UserTokenQueryDTO query) {
        return userTokenService.page(query);
    }
}


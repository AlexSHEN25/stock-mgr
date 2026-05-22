package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.UserTokenService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/userToken")
public class UserTokenController {
    private final UserTokenService userTokenService;

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
        return userTokenService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return userTokenService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<UserTokenVO> page(@Valid UserTokenQueryDTO query) {
        return userTokenService.page(query);
    }
}


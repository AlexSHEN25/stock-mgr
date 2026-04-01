package co.handk.backend.controller;

import co.handk.backend.entity.UserToken;
import co.handk.common.model.vo.UserTokenVO;
import co.handk.common.model.dto.UserTokenDTO;
import co.handk.backend.service.UserTokenService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/userToken")
public class UserTokenController {

    @Autowired
    private UserTokenService userTokenService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid UserTokenDTO dto) {
        return userTokenService.create(dto);
    }

    @GetMapping("/{id}")
    public UserTokenVO get(@PathVariable @NotNull Long id) {
        return userTokenService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UserTokenDTO dto) {
        return userTokenService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return userTokenService.delete(id);
    }

    @GetMapping("/list")
    public List<UserTokenVO> list() {
        return userTokenService.listAll();
    }

    @GetMapping("/page")
    public PageResult<UserTokenVO> page(@Valid PageQuery query) {
        return userTokenService.pageQuery(query);
    }
}

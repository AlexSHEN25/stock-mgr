package co.handk.backend.controller;

import co.handk.backend.entity.UserToken;
import co.handk.backend.service.UserTokenService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userToken")
public class UserTokenController {

    @Autowired
    private UserTokenService userTokenService;

    @PostMapping
    public Boolean create(@RequestBody UserToken entity) {
        return userTokenService.create(entity);
    }

    @GetMapping("/{id}")
    public UserToken get(@PathVariable Long id) {
        return userTokenService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody UserToken entity) {
        return userTokenService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return userTokenService.delete(id);
    }

    @GetMapping("/list")
    public List<UserToken> list() {
        return userTokenService.listAll();
    }

    @GetMapping("/page")
    public PageResult<UserToken> page(PageQuery query) {
        return userTokenService.pageQuery(query);
    }
}

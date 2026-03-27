package co.handk.backend.controller;

import co.handk.backend.entity.User;
import co.handk.backend.service.UserService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginVO login(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/logout")
    public LogoutVO logout(HttpServletRequest request){
        return userService.logout();
    }

    // 新增
    @PostMapping
    public Boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    // 修改
    @PutMapping
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    // 删除
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return userService.removeById(id);
    }

    // 查询全部
    @GetMapping("/list")
    public List<User> list() {
        return userService.list();
    }

    // 分页查询
    @GetMapping("/page")
    public PageResult<User> page(PageQuery query) {
        return userService.pageQuery(query);
    }
}

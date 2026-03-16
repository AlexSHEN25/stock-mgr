package co.handk.backend.controller;

import co.handk.backend.entity.User;
import co.handk.backend.service.UserService;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
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
    public LoginVO login(@RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return userService.login(dto, ip);
    }

    @PostMapping("/logout")
    public Boolean logout(HttpServletRequest request){
        String token = request.getHeader("Authorization").replace("Bearer ","");
        return userService.logout(token);
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
}
package co.handk.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/test")
public interface TestApi {

    @GetMapping("/{name}")
    String hello(@PathVariable("name") String name);

}

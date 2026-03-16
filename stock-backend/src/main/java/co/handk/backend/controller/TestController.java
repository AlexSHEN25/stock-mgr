package co.handk.backend.controller;

import co.handk.api.TestApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController implements TestApi {

    @Override
    public String hello(String name) {
        return  name + " Ok";
    }
}
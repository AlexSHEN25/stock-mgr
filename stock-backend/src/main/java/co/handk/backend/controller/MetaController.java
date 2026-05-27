package co.handk.backend.controller;

import co.handk.backend.meta.EnumOptionRegistry;
import co.handk.common.model.vo.EnumOptionVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meta")
public class MetaController {

    @GetMapping("/enum-options")
    public List<EnumOptionVO> enumOptions(@RequestParam("enumKey") String enumKey) {
        return EnumOptionRegistry.getOptions(enumKey);
    }
}


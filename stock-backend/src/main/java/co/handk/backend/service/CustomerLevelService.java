package co.handk.backend.service;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.dto.CustomerLevelDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface CustomerLevelService extends IService<CustomerLevel> {

    Boolean create(@NotNull CustomerLevelDTO dto);

    CustomerLevel get(@NotNull Long id);

    Boolean update(@NotNull CustomerLevelDTO dto);

    Boolean delete(@NotNull Long id);

    List<CustomerLevel> listAll();

    PageResult<CustomerLevel> pageQuery(@NotNull PageQuery query);
}

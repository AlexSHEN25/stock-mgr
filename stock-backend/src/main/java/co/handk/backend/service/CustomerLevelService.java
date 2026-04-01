package co.handk.backend.service;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.dto.CustomerLevelDTO;
import co.handk.common.model.vo.CustomerLevelVO;
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

    CustomerLevelVO get(@NotNull Long id);

    Boolean update(@NotNull CustomerLevelDTO dto);

    Boolean delete(@NotNull Long id);
    List<CustomerLevelVO> listAll();

    PageResult<CustomerLevelVO> pageQuery(@NotNull PageQuery query);
}

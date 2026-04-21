package co.handk.backend.service;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCustomerLevelDTO;
import co.handk.common.model.dto.query.CustomerLevelQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerLevelDTO;
import co.handk.common.model.vo.CustomerLevelVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface CustomerLevelService extends IService<CustomerLevel> {
    Boolean create(@NotNull CreateCustomerLevelDTO dto);
    CustomerLevelVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateCustomerLevelDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<CustomerLevelVO> pageQuery(@NotNull CustomerLevelQueryDTO query);
}

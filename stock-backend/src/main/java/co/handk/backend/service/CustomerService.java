package co.handk.backend.service;

import co.handk.backend.entity.Customer;
import co.handk.common.model.dto.CustomerDTO;
import co.handk.common.model.vo.CustomerVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface CustomerService extends IService<Customer> {

    Boolean create(@NotNull CustomerDTO dto);

    CustomerVO get(@NotNull Long id);

    Boolean update(@NotNull CustomerDTO dto);

    Boolean delete(@NotNull Long id);
    List<CustomerVO> listAll();

    PageResult<CustomerVO> pageQuery(@NotNull PageQuery query);
}

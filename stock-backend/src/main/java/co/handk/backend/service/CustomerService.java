package co.handk.backend.service;
import co.handk.backend.entity.Customer;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import co.handk.common.model.vo.CustomerVO;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface CustomerService extends IService<Customer> {
    Boolean create(@NotNull CreateCustomerDTO dto);
    CustomerVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateCustomerDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<CustomerVO> pageQuery(@NotNull CustomerQueryDTO query);
}

package co.handk.backend.service;

import co.handk.backend.entity.Customer;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService extends IService<Customer> {

    Boolean create(Customer entity);

    Customer get(Long id);

    Boolean update(Customer entity);

    Boolean delete(Long id);

    List<Customer> listAll();

    PageResult<Customer> pageQuery(PageQuery query);
}

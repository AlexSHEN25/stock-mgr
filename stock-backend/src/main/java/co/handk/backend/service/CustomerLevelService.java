package co.handk.backend.service;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerLevelService extends IService<CustomerLevel> {

    Boolean create(CustomerLevel entity);

    CustomerLevel get(Long id);

    Boolean update(CustomerLevel entity);

    Boolean delete(Long id);

    List<CustomerLevel> listAll();

    PageResult<CustomerLevel> pageQuery(PageQuery query);
}

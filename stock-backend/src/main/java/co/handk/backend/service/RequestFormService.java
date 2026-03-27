package co.handk.backend.service;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RequestFormService extends IService<RequestForm> {

    Boolean create(RequestForm entity);

    RequestForm get(Long id);

    Boolean update(RequestForm entity);

    Boolean delete(Long id);

    List<RequestForm> listAll();

    PageResult<RequestForm> pageQuery(PageQuery query);
}

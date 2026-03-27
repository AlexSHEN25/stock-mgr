package co.handk.backend.service;

import co.handk.backend.entity.RequestItem;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RequestItemService extends IService<RequestItem> {

    Boolean create(RequestItem entity);

    RequestItem get(Long id);

    Boolean update(RequestItem entity);

    Boolean delete(Long id);

    List<RequestItem> listAll();

    PageResult<RequestItem> pageQuery(PageQuery query);
}

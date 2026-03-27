package co.handk.backend.service;

import co.handk.backend.entity.OperateLog;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OperateLogService extends IService<OperateLog> {

    Boolean create(OperateLog entity);

    OperateLog get(Long id);

    Boolean update(OperateLog entity);

    Boolean delete(Long id);

    List<OperateLog> listAll();

    PageResult<OperateLog> pageQuery(PageQuery query);
}

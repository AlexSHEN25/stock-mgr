package co.handk.backend.service;

import co.handk.backend.entity.Dept;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DeptService extends IService<Dept> {

    Boolean create(Dept entity);

    Dept get(Long id);

    Boolean update(Dept entity);

    Boolean delete(Long id);

    List<Dept> listAll();

    PageResult<Dept> pageQuery(PageQuery query);
}

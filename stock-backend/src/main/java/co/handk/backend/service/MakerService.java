package co.handk.backend.service;

import co.handk.backend.entity.Maker;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MakerService extends IService<Maker> {

    Boolean create(Maker entity);

    Maker get(Long id);

    Boolean update(Maker entity);

    Boolean delete(Long id);

    List<Maker> listAll();

    PageResult<Maker> pageQuery(PageQuery query);
}

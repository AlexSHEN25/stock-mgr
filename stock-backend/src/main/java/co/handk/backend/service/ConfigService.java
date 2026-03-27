package co.handk.backend.service;

import co.handk.backend.entity.Config;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConfigService extends IService<Config> {

    Boolean create(Config entity);

    Config get(Long id);

    Boolean update(Config entity);

    Boolean delete(Long id);

    List<Config> listAll();

    PageResult<Config> pageQuery(PageQuery query);
}

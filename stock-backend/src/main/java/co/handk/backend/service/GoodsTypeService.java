package co.handk.backend.service;

import co.handk.backend.entity.GoodsType;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GoodsTypeService extends IService<GoodsType> {

    Boolean create(GoodsType entity);

    GoodsType get(Long id);

    Boolean update(GoodsType entity);

    Boolean delete(Long id);

    List<GoodsType> listAll();

    PageResult<GoodsType> pageQuery(PageQuery query);
}

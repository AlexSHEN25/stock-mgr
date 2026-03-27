package co.handk.backend.service;

import co.handk.backend.entity.Goods;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GoodsService extends IService<Goods> {

    Boolean create(Goods entity);

    Goods get(Long id);

    Boolean update(Goods entity);

    Boolean delete(Long id);

    List<Goods> listAll();

    PageResult<Goods> pageQuery(PageQuery query);
}

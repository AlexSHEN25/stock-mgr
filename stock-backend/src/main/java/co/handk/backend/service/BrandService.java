package co.handk.backend.service;

import co.handk.backend.entity.Brand;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BrandService extends IService<Brand> {

    Boolean create(Brand entity);

    Brand get(Long id);

    Boolean update(Brand entity);

    Boolean delete(Long id);

    List<Brand> listAll();

    PageResult<Brand> pageQuery(PageQuery query);
}

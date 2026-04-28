package co.handk.backend.service;

import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T, V> extends IService<T> {

    T getByIdNotDeleted(Serializable id);

    V getVOById(Serializable id);

    <Q> List<V> list(Q queryDto);

    <Q extends PageQuery> PageResult<V> page(Q queryDto);

    <C> boolean saveByDto(C createDto);

    <U> boolean updateByDto(U updateDto);

    int deleteByIdLogic(Long id);

    int deleteBatchLogic(List<Long> ids);

    boolean existsById(Long id);
}

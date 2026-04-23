package co.handk.backend.service;

import co.handk.backend.entity.BaseEntity;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BaseService<T extends BaseEntity> extends IService<T> {

    /**
     * 单条逻辑删除（返回影响行数）
     */
    int deleteById(Long id, Long operatorId);

    /**
     * 批量逻辑删除（返回实际删除条数）
     */
    int deleteBatchIds(Collection<Long> ids, Long operatorId);

    /**
     * 查询单条（未删除）
     */
    Optional<T> getByIdNotDeleted(Long id);

    /**
     * 查询列表（未删除）
     */
    List<T> listNotDeleted();

    /**
     * 分页查询（未删除）
     */
    PageResult<T> page(PageQuery query);

}
package co.handk.backend.service.impl;

import co.handk.backend.entity.BaseEntity;
import co.handk.backend.service.BaseService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity>
        extends ServiceImpl<M, T> implements BaseService<T> {

    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;

    /**
     * 单条逻辑删除（返回真实条数）
     */
    @Override
    public int deleteById(Long id, Long operatorId) {
        T entity = createUpdateEntity(operatorId);

        return baseMapper.update(entity,
                new LambdaUpdateWrapper<T>()
                        .eq(T::getId, id)
                        .eq(T::getDeleted, NOT_DELETED)
        );
    }

    /**
     * 批量逻辑删除（返回真实条数）
     */
    @Override
    public int deleteBatchIds(Collection<Long> ids, Long operatorId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        T entity = createUpdateEntity(operatorId);

        return baseMapper.update(entity,
                new LambdaUpdateWrapper<T>()
                        .in(T::getId, ids)
                        .eq(T::getDeleted, NOT_DELETED)
        );
    }

    /**
     * 查询单条（未删除）
     */
    @Override
    public Optional<T> getByIdNotDeleted(Long id) {
        return Optional.ofNullable(
                lambdaQuery()
                        .eq(T::getId, id)
                        .eq(T::getDeleted, NOT_DELETED)
                        .one()
        );
    }

    /**
     * 查询列表（未删除）
     */
    @Override
    public List<T> listNotDeleted() {
        return lambdaQuery()
                .eq(T::getDeleted, NOT_DELETED)
                .list();
    }

    /**
     * 分页查询（未删除）
     */
    @Override
    public PageResult<T> page(PageQuery query) {

        Page<T> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(T::getDeleted, NOT_DELETED);
        // 排序
        boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());

        if ("createTime".equals(query.getSortBy())) {
            wrapper.orderBy(true, isAsc, T::getCreateTime);
        } else {
            wrapper.orderBy(true, isAsc, T::getUpdateTime);
        }
        Page<T> result = this.page(page, wrapper);
        return PageResult.build(
                result.getTotal(),
                query.getPageNum(),
                query.getPageSize(),
                result.getRecords()
        );
    }

    /**
     * 构造更新实体（统一封装）
     */
    private T createUpdateEntity(Long operatorId) {
        try {
            T entity = (T) currentModelClass().newInstance();
            entity.setDeleted(DELETED);
            entity.setUpdatedBy(operatorId);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("构造实体失败", e);
        }
    }
}
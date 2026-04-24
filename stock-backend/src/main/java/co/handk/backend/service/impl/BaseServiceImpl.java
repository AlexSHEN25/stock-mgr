package co.handk.backend.service.impl;

import co.handk.backend.annotation.QueryField;
import co.handk.backend.annotation.UpdateIgnore;
import co.handk.backend.entity.BaseEntity;
import co.handk.backend.service.BaseService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.BaseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity, V extends BaseVO>
        extends ServiceImpl<M, T>
        implements BaseService<T, V> {

    /**
     * ================= 查询 =================
     */
    @Override
    public T getByIdNotDeleted(Serializable id) {
        return getOne(new QueryWrapper<T>().eq("id", id).eq("deleted", DeleteEnum.UNDELETED.getCode()));
    }

    @Override
    public V getVOById(Serializable id) {
        T entity = getByIdNotDeleted(id);
        return entity == null ? null : toVO(entity);
    }

    @Override
    public <Q> List<V> list(Q dto) {
        QueryWrapper<T> wrapper = buildWrapper(dto);
        return list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public <Q extends PageQuery> PageResult<V> page(Q dto) {
        Page<T> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<T> wrapper = buildWrapper(dto);
        boolean asc = "asc".equalsIgnoreCase(dto.getSortOrder());
        if ("createTime".equals(dto.getSortBy())) {
            wrapper.orderBy(true, asc, "create_time");
        } else {
            wrapper.orderBy(true, asc, "update_time");
        }
        Page<T> result = this.page(page, wrapper);
        List<V> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.build(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    /**
     * ================= 新增 =================
     */
    @Override
    public <C> boolean saveByDto(C dto) {
        return save(toEntity(dto));
    }

    /**
     * ================= 更新（仅更新非 null 字段） =================
     */
    @Override
    public <U> boolean updateByDto(U dto) {
        T entity = toEntity(dto);
        if (entity.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", entity.getId()).eq("deleted", DeleteEnum.UNDELETED.getCode());
        buildUpdateSet(entity, wrapper);
        return baseMapper.update(null, wrapper) > DeleteEnum.UNDELETED.getCode();
    }

    /**
     * ================= 逻辑删除 =================
     */
    @Override
    public int deleteByIdLogic(Long id) {
        return baseMapper.update(null, new UpdateWrapper<T>()
                .eq("id", id).eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode()));
    }

    @Override
    public int deleteBatchLogic(List<Long> ids) {

        if (ids == null || ids.isEmpty()) return DeleteEnum.UNDELETED.getCode();

        return baseMapper.update(null, new UpdateWrapper<T>().in("id", ids)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode()));
    }

    @Override
    public boolean existsById(Long id) {
        return count(new QueryWrapper<T>().eq("id", id).eq("deleted", DeleteEnum.UNDELETED.getCode())) > 0;
    }

    /**
     * ================= 注解驱动查询 =================
     */
    protected <Q> QueryWrapper<T> buildWrapper(Q dto) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        // 全局过滤未删除
        wrapper.eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (dto == null) return wrapper;
        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            QueryField queryField = field.getAnnotation(QueryField.class);
            if (queryField == null) continue;
            try {
                Object value = field.get(dto);
                if (value == null) continue;
                String column = queryField.column().isEmpty() ? camelToUnderline(field.getName()) : queryField.column();
                switch (queryField.type()) {
                    case EQ -> wrapper.eq(column, value);
                    case LIKE -> wrapper.like(column, value);
                    case GT -> wrapper.gt(column, value);
                    case GE -> wrapper.ge(column, value);
                    case LT -> wrapper.lt(column, value);
                    case LE -> wrapper.le(column, value);
                    case IN -> wrapper.in(column, (Collection<?>) value);
                    case BETWEEN -> {
                        List<?> list = (List<?>) value;
                        if (list.size() == 2) {
                            wrapper.between(column, list.get(0), list.get(1));
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("构建查询失败", e);
            }
        }
        return wrapper;
    }

    /**
     * ================= 更新字段构建 =================
     */
    protected void buildUpdateSet(T entity, UpdateWrapper<T> wrapper) {
        if (entity == null) return;
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //  先判断注解（必须放最前面）
                if (field.isAnnotationPresent(UpdateIgnore.class)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    // 忽略 null
                    if (value == null) continue;
                    String fieldName = field.getName();
                    // 这些字段不允许更新
                    if ("id".equals(fieldName) || "createTime".equals(fieldName) || "createdBy".equals(fieldName) || "deleted".equals(fieldName)) {
                        continue;
                    }
                    String column = camelToUnderline(fieldName);
                    wrapper.set(column, value);
                } catch (Exception e) {
                    throw new RuntimeException("构建 update set 失败", e);
                }
            }
            clazz = clazz.getSuperclass(); // 关键：支持 BaseEntity
        }
    }

    /**
     * 驼峰转下划线
     */
    protected String camelToUnderline(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 子类实现
     */
    protected abstract <D> T toEntity(D dto);

    protected abstract V toVO(T entity);
}
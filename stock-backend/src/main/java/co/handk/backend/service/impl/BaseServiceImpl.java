package co.handk.backend.service.impl;

import co.handk.backend.annotation.JoinQueryConfig;
import co.handk.backend.annotation.JoinTable;
import co.handk.backend.annotation.QueryField;
import co.handk.backend.annotation.UpdateIgnore;
import co.handk.backend.entity.BaseEntity;
import co.handk.backend.enums.JoinType;
import co.handk.backend.service.BaseService;
import co.handk.common.annotation.JoinSelect;
import co.handk.common.annotation.JoinValue;
import co.handk.common.constant.FieldNameConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.BaseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity, V extends BaseVO>
        extends ServiceImpl<M, T>
        implements BaseService<T, V>, ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * ================= 鬯ｮ・ｫ繝ｻ・ｴ髮狗ｿｫ・代・・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・･鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｸ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢ =================
     */
    @Override
    public T getByIdNotDeleted(Serializable id) {
        return getOne(new QueryWrapper<T>().eq(FieldNameConstant.COLUMN_ID, id).eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode()));
    }

    @Override
    public V getVOById(Serializable id) {
        T entity = getByIdNotDeleted(id);
        if (entity == null) {
            return null;
        }
        V vo = toVO(entity);
        fillStatusDesc(vo);
        fillJoinValues(List.of(vo));
        return vo;
    }

    @Override
    public <Q> List<V> list(Q dto) {
        if (hasJoinQueryConfig()) {
            List<V> joined = executeJoinList(dto);
            joined.forEach(this::fillStatusDesc);
            fillJoinValues(joined);
            return joined;
        }
        QueryWrapper<T> wrapper = buildWrapper(dto);
        List<V> voList = list(wrapper).stream()
                .map(this::toVO)
                .peek(this::fillStatusDesc)
                .collect(Collectors.toList());
        fillJoinValues(voList);
        return voList;
    }

    @Override
    public <Q extends PageQuery> PageResult<V> page(Q dto) {
        if (hasJoinQueryConfig()) {
            return executeJoinPage(dto);
        }
        Page<T> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<T> wrapper = buildWrapper(dto);
        boolean asc = "asc".equalsIgnoreCase(dto.getSortOrder());
        if (FieldNameConstant.SORT_BY_CREATE_TIME.equals(dto.getSortBy())) {
            wrapper.orderBy(true, asc, FieldNameConstant.COLUMN_CREATE_TIME);
        } else {
            wrapper.orderBy(true, asc, FieldNameConstant.COLUMN_UPDATE_TIME);
        }
        Page<T> result = this.page(page, wrapper);
        List<V> voList = result.getRecords().stream()
                .map(this::toVO)
                .peek(this::fillStatusDesc)
                .collect(Collectors.toList());
        fillJoinValues(voList);
        return PageResult.build(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected void fillStatusDesc(V vo) {
        if (vo == null) {
            return;
        }
        try {
            Field statusField = findField(vo.getClass(), FieldNameConstant.STATUS);
            Field statusDescField = findField(vo.getClass(), FieldNameConstant.STATUS_DESC);
            if (statusField == null || statusDescField == null) {
                return;
            }
            statusField.setAccessible(true);
            statusDescField.setAccessible(true);
            Object statusValue = statusField.get(vo);
            if (!(statusValue instanceof Number number)) {
                return;
            }
            StatusEnum statusEnum = StatusEnum.fromValue(number.intValue());
            statusDescField.set(vo, statusEnum == null ? null : statusEnum.getMessage());
        } catch (Exception ignored) {
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * ================= 鬯ｮ・ｫ繝ｻ・ｴ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｰ鬯ｮ・ｯ雋・･繝ｻ驛｢譎｢・ｽ・ｻ=================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        return save(toEntity(dto));
    }

    /**
     * ================= 鬯ｮ・ｫ繝ｻ・ｴ髯ｷ・ｴ郢晢ｽｻ繝ｻ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｫ繝ｻ・ｴ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｰ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬮｣雋ｻ・｣・ｰ髯具ｽｹ郢晢ｽｻ繝ｻ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬮ｯ譎｢・ｽ・ｲ郢晢ｽｻ繝ｻ・ｩ鬯ｮ・ｫ繝ｻ・ｴ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｰ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｱ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻnull 鬯ｮ・ｯ隴擾ｽｴ郢晢ｽｻ鬮ｴ繝ｻ・ｽ・ｸ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ=================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        T entity = toEntity(dto);
        if (entity.getId() == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "IDは必須です");
        }
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq(FieldNameConstant.COLUMN_ID, entity.getId()).eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode());
        Long oldVersion = extractVersionValue(entity);
        if (hasVersionField(entity.getClass())) {
            if (oldVersion == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "versionは必須です"
                );
            }
            wrapper.eq(FieldNameConstant.COLUMN_VERSION, oldVersion);
        }
        buildUpdateSet(entity, wrapper);
        boolean versioned = hasVersionField(entity.getClass());
        if (versioned && oldVersion != null) {
            wrapper.set(FieldNameConstant.COLUMN_VERSION, oldVersion + 1L);
        }
        int updated = baseMapper.update(null, wrapper);
        if (versioned && updated <= DeleteEnum.UNDELETED.getCode()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "他のユーザーが先に更新しました。画面を更新して再試行してください"
            );
        }
        return updated > DeleteEnum.UNDELETED.getCode();
    }

    /**
     * ================= 鬯ｯ・ｯ繝ｻ・ｨ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｮ繝ｻ・ｴ髣包ｽｳ繝ｻ・ｻ郢晢ｽｻ繝ｻ・､郢晢ｽｻ繝ｻ・ｧ鬮ｯ・ｷ陞｢・ｼ繝ｻ・､隲幢ｽｶ繝ｻ・ｽ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｯ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・､ =================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<T>()
                .eq(FieldNameConstant.COLUMN_ID, id)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                .set(FieldNameConstant.COLUMN_DELETED, DeleteEnum.DELETED.getCode());
        if (hasVersionField(resolveEntityClass())) {
            T existed = getByIdNotDeleted(id);
            Long oldVersion = extractVersionValue(existed);
            if (oldVersion == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "versionが存在しないため削除できません"
                );
            }
            wrapper.eq(FieldNameConstant.COLUMN_VERSION, oldVersion);
            wrapper.set(FieldNameConstant.COLUMN_VERSION, oldVersion + 1L);
        }
        int deleted = baseMapper.update(null, wrapper);
        if (hasVersionField(resolveEntityClass()) && deleted <= DeleteEnum.UNDELETED.getCode()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "他のユーザーが先に更新しました。画面を更新して再試行してください"
            );
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {

        if (ids == null || ids.isEmpty()) return DeleteEnum.UNDELETED.getCode();

        if (hasVersionField(resolveEntityClass())) {
            int affected = 0;
            for (Long id : ids) {
                affected += deleteByIdLogic(id);
            }
            return affected;
        }

        return baseMapper.update(null, new UpdateWrapper<T>().in(FieldNameConstant.COLUMN_ID, ids)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                .set(FieldNameConstant.COLUMN_DELETED, DeleteEnum.DELETED.getCode()));
    }

    @Override
    public boolean existsById(Long id) {
        return count(new QueryWrapper<T>().eq(FieldNameConstant.COLUMN_ID, id).eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())) > NumberConstant.ZERO;
    }

    /**
     * ================= 鬯ｮ・ｮ闕ｳ・ｻ隴ｯ竏壹・繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｮ繝ｻ・ｫ髫ｴ莨夲ｽｽ・ｦ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｣鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ鬯ｮ・ｯ繝ｻ・ｷ髣費ｽｨ陞滂ｽｲ繝ｻ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｨ鬯ｮ・ｫ繝ｻ・ｴ髮狗ｿｫ・代・・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・･鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｸ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢ =================
     */
    protected <Q> QueryWrapper<T> buildWrapper(Q dto) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        // 鬯ｮ・ｯ繝ｻ・ｷ鬮｣魃会ｽｽ・ｨ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｨ鬯ｮ・ｯ隶厄ｽｸ繝ｻ・ｽ繝ｻ・ｻ郢晢ｽｻ邵ｺ・､・つ鬯ｯ・ｮ繝ｻ・ｴ髣比ｼ夲ｽｽ・｣驛｢譎｢・ｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・､鬯ｮ・ｫ繝ｻ・ｴ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｪ鬯ｮ・ｯ陷茨ｽｷ繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｰ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｯ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・､
        wrapper.eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode());
        if (dto == null) return wrapper;
        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            QueryField queryField = field.getAnnotation(QueryField.class);
            try {
                Object value = field.get(dto);
                if (value == null) continue;
                if (queryField == null) {
                    applyDefaultCondition(wrapper, field.getName(), value);
                    continue;
                }
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
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "検索条件の構築に失敗しました", e);
            }
        }
        buildJoinConditions(dto, wrapper);
        return wrapper;
    }

    protected <Q> void buildJoinConditions(Q dto, QueryWrapper<T> wrapper) {
        // 鬯ｮ・ｯ隴擾ｽｴ郢晢ｽｻ鬯ｲ蛛・ｽｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｮ・ｫ繝ｻ・ｰ髣包ｽｵ雋・ｽｷ隲｢蟶ｷ・ｹ譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｡鬯ｮ・ｯ隲幢ｽｶ繝ｻ・ｽ繝ｻ・ｮ鬯ｮ・｣陜難ｽｼ陞ｻ・ｮ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｦ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬮ｯ譏ｴ繝ｻ繝ｻ・｣繝ｻ・ｰ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬮ｯ讖ｸ・ｽ・｢郢晢ｽｻ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｾ鬮ｯ・ｷ繝ｻ・ｿ郢晢ｽｻ繝ｻ・･驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｦ鬩幢ｽ｢繝ｻ・ｧ髣比ｼ夲ｽｽ・ｰ繝ｻ縺､ﾂ鬮ｯ讓奇ｽｺ・ｷ繝ｻ・･郢晢ｽｻ繝ｻ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｿ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬯ｯ・ｪ繝ｻ・ｭ髯橸ｽｳ繝ｻ・｣繝ｻ繧托ｽｽ・ｧ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｰ鬯ｮ・ｯ隴擾ｽｴ郢晢ｽｻ鬮ｴ繝ｻ・ｽ・ｸ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｵ鬯ｮ・ｯ繝ｻ・ｷ鬮｣魃会ｽｽ・ｨ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｮ繝ｻ・｢郢晢ｽｻ繝ｻ・ｨ鬮ｮ荳ｻ萓帙・・ｾ陟募ｾ後・鬯ｮ・｣騾ｧ・ｮ騾包ｽ･驛｢譎｢・ｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｡驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｮ繝ｻ・ｴ髯樊ｻゑｽｽ・ｧ郢晢ｽｻ繝ｻ・ｹ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｡鬮ｫ・ｶ陷ｻ・ｵ繝ｻ・ｶ繝ｻ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｭ鬮ｯ譎｢・ｽ・ｷ繝ｻ雜｣・ｽ・ｴ繝ｻ縺､ﾂ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ
    }

    private void applyDefaultCondition(QueryWrapper<T> wrapper, String fieldName, Object value) {
        String column = camelToUnderline(fieldName);
        if (value instanceof String stringValue) {
            if (!stringValue.isBlank()) {
                wrapper.like(column, stringValue.trim());
            }
            return;
        }
        if (value instanceof Enum<?> enumValue) {
            Object enumDbValue = resolveEnumDbValue(enumValue);
            if (enumDbValue != null) {
                wrapper.eq(column, enumDbValue);
            }
            return;
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Temporal) {
            wrapper.eq(column, value);
        }
    }

    private Object resolveEnumDbValue(Enum<?> enumValue) {
        try {
            Field codeField = findField(enumValue.getClass(), "code");
            if (codeField != null) {
                codeField.setAccessible(true);
                return codeField.get(enumValue);
            }
        } catch (Exception ignored) {
        }
        return enumValue.name();
    }

    /**
     * ================= 鬯ｮ・ｫ繝ｻ・ｴ髯ｷ・ｴ郢晢ｽｻ繝ｻ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｫ繝ｻ・ｴ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｰ鬯ｮ・ｯ隴擾ｽｴ郢晢ｽｻ鬮ｴ繝ｻ・ｽ・ｸ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｵ鬯ｮ・ｫ繝ｻ・ｴ郢晢ｽｻ繝ｻ・ｫ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ =================
     */
    protected void buildUpdateSet(T entity, UpdateWrapper<T> wrapper) {
        if (entity == null) return;
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //  鬯ｮ・ｯ繝ｻ・ｷ髣費｣ｰ陋ｹ繝ｻ・ｽ・ｽ繝ｻ・ｺ郢晢ｽｻ繝ｻ・･鬮ｫ・ｲ繝ｻ・｢髯晢ｽｷ郢晢ｽｻ繝ｻ・ｽ繝ｻ・ｭ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｭ鬯ｮ・ｮ闕ｳ・ｻ隴ｯ竏壹・繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｮ繝ｻ・ｫ髫ｴ莨夲ｽｽ・ｦ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｣鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬮｣雋ｻ・｣・ｰ郢晢ｽｻ繝ｻ・･驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｿ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｡驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｮ・ｫ繝ｻ・ｰ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｾ鬯ｮ・ｫ繝ｻ・ｴ髯晢｣ｰ繝ｻ・｢繝ｻ縺､ﾂ鬯ｮ・ｯ繝ｻ・ｷ髯樊ｻゑｽｽ・ｧ髮趣ｽｸ繝ｻ・ｵ鬮ｫ・ｰ繝ｻ・ｫ驛｢譎｢・ｽ・ｻ驛｢譎｢・ｽ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ
                if (field.isAnnotationPresent(UpdateIgnore.class)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    // 鬯ｮ・ｯ雋翫ｑ・ｽ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｯ・ｨ繝ｻ・ｾ郢晢ｽｻ繝ｻ・｡驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・･ null
                    if (value == null) continue;
                    String fieldName = field.getName();
                    // 鬯ｯ・ｮ繝ｻ・ｴ髯樊ｻゑｽｽ・ｧ髯晉事萓ｭ郢晢ｽｻ郢晢ｽｻ繝ｻ・ｺ鬮ｯ譎｢・ｽ・ｶ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｭ鬯ｩ髦ｪ繝ｻ繝ｻ・ｽ繝ｻ・ｲ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｵ鬯ｮ・｣陋ｹ繝ｻ・ｽ・ｽ繝ｻ・ｳ鬮ｫ・ｶ陞ｳ闌ｨ・ｽ・ｿ繝ｻ・ｫ驛｢譎｢・ｽ・ｻ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｶ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｸ鬯ｮ・ｫ繝ｻ・ｴ髯ｷ・ｴ郢晢ｽｻ繝ｻ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｫ繝ｻ・ｴ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｰ
                    if (FieldNameConstant.ID.equals(fieldName)
                            || FieldNameConstant.CREATE_TIME.equals(fieldName)
                            || FieldNameConstant.CREATED_BY.equals(fieldName)
                            || FieldNameConstant.DELETED.equals(fieldName)
                            || FieldNameConstant.VERSION.equals(fieldName)) {
                        continue;
                    }
                    String column = camelToUnderline(fieldName);
                    wrapper.set(column, value);
                } catch (Exception e) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "更新項目の構築に失敗しました", e);
                }
            }
            clazz = clazz.getSuperclass(); // 鬯ｮ・ｯ繝ｻ・ｷ鬮｣魃会ｽｽ・ｨ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｮ髫ｶ蜴・ｽｽ・ｸ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｮ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬮ｯ讖ｸ・ｽ・｢郢晢ｽｻ繝ｻ・ｽ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｪ鬯ｮ・ｫ繝ｻ・ｰ髫ｰ雋ｻ・ｽ・ｶ驛｢譎｢・ｽ・ｻBaseEntity
        }
    }

    /**
     * 鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｼ鬯ｮ・ｯ隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｲ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｰ鬯ｯ・ｮ繝ｻ・ｴ鬯ｮ・ｮ繝ｻ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｬ鬯ｮ・｣陋ｹ繝ｻ・ｽ・ｽ繝ｻ・ｳ鬮ｯ・ｷ繝ｻ・ｿ郢晢ｽｻ繝ｻ・･鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・､驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｿ
     */
    protected String camelToUnderline(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 鬯ｮ・ｯ隴擾ｽｴ郢晢ｽｻ鬯ｲ蛛・ｽｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｮ・ｯ隶厄ｽｸ繝ｻ・ｽ繝ｻ・ｳ鬮ｫ・ｶ霓｣蛛・ｽｽ・ｸ隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｴ郢晢ｽｻ繝ｻ・ｫ
     */
    protected abstract <D> T toEntity(D dto);

    protected abstract V toVO(T entity);

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        Class<?> resolved = ResolvableType.forClass(this.getClass())
                .as(BaseServiceImpl.class)
                .getGeneric(1)
                .resolve();
        return (Class<T>) (resolved == null ? BaseEntity.class : resolved);
    }

    private boolean hasVersionField(Class<?> clazz) {
        return findField(clazz, FieldNameConstant.VERSION) != null;
    }

    private Long extractVersionValue(T entity) {
        if (entity == null) {
            return null;
        }
        Field versionField = findField(entity.getClass(), FieldNameConstant.VERSION);
        if (versionField == null) {
            return null;
        }
        try {
            versionField.setAccessible(true);
            Object value = versionField.get(entity);
            if (value instanceof Number number) {
                return number.longValue();
            }
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private void fillJoinValues(List<V> voList) {
        if (voList == null || voList.isEmpty() || applicationContext == null) {
            return;
        }
        Class<?> voClass = voList.get(0).getClass();
        Field[] fields = voClass.getDeclaredFields();
        for (Field targetField : fields) {
            JoinValue joinValue = targetField.getAnnotation(JoinValue.class);
            if (joinValue == null) {
                continue;
            }
            try {
                Field sourceField = findField(voClass, joinValue.sourceField());
                if (sourceField == null) {
                    continue;
                }
                sourceField.setAccessible(true);
                targetField.setAccessible(true);
                Object serviceBean = applicationContext.getBean(joinValue.serviceBean());
                Map<Object, Object> valueCache = new HashMap<>();
                for (V vo : voList) {
                    Object fk = sourceField.get(vo);
                    if (fk == null) {
                        continue;
                    }
                    if (!valueCache.containsKey(fk)) {
                        Object refObj = invokeGetByIdNotDeleted(serviceBean, fk);
                        Object refValue = refObj == null ? null : readFieldValue(refObj, joinValue.targetField());
                        valueCache.put(fk, refValue);
                    }
                    targetField.set(vo, valueCache.get(fk));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Object invokeGetByIdNotDeleted(Object serviceBean, Object id) {
        if (!(id instanceof Serializable serializableId)) {
            return null;
        }
        try {
            return serviceBean.getClass().getMethod("getByIdNotDeleted", Serializable.class)
                    .invoke(serviceBean, serializableId);
        } catch (Exception e) {
            return null;
        }
    }

    private Object readFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Field f = findField(obj.getClass(), fieldName);
        if (f == null) {
            return null;
        }
        try {
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hasJoinQueryConfig() {
        return this.getClass().isAnnotationPresent(JoinQueryConfig.class);
    }

    @SuppressWarnings("unchecked")
    private Class<V> getVoClass() {
        Class<?> resolved = ResolvableType.forClass(this.getClass())
                .as(BaseServiceImpl.class)
                .getGeneric(2)
                .resolve();
        return (Class<V>) (resolved == null ? BaseVO.class : resolved);
    }

    private <Q extends PageQuery> PageResult<V> executeJoinPage(Q dto) {
        NamedParameterJdbcTemplate jdbc = getJoinJdbcTemplate();
        String fromSql = buildFromJoinSql();
        String whereSql = buildJoinWhereSql(dto);
        String orderSql = buildJoinOrderSql(dto);
        String selectSql = buildJoinSelectSql();
        MapSqlParameterSource params = buildJoinParams(dto);
        params.addValue("offset", (dto.getPageNum() - 1) * dto.getPageSize());
        params.addValue("size", dto.getPageSize());

        String countSql = "SELECT COUNT(1) " + fromSql + " " + whereSql;
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        if (total == null || total == NumberConstant.ZERO) {
            return PageResult.build(0L, dto.getPageNum(), dto.getPageSize(), Collections.emptyList());
        }

        String pageSql = selectSql + " " + fromSql + " " + whereSql + " " + orderSql + " LIMIT :offset, :size";
        List<V> list = jdbc.query(pageSql, params, BeanPropertyRowMapper.newInstance(getVoClass()));
        list.forEach(this::fillStatusDesc);
        fillJoinValues(list);
        return PageResult.build(total, dto.getPageNum(), dto.getPageSize(), list);
    }

    private <Q> List<V> executeJoinList(Q dto) {
        NamedParameterJdbcTemplate jdbc = getJoinJdbcTemplate();
        String fromSql = buildFromJoinSql();
        String whereSql = buildJoinWhereSql(dto);
        String selectSql = buildJoinSelectSql();
        MapSqlParameterSource params = buildJoinParams(dto);
        String sql = selectSql + " " + fromSql + " " + whereSql + " ORDER BY "
                + this.getClass().getAnnotation(JoinQueryConfig.class).baseAlias() + ".update_time DESC";
        return jdbc.query(sql, params, BeanPropertyRowMapper.newInstance(getVoClass()));
    }

    private String buildJoinSelectSql() {
        JoinQueryConfig cfg = this.getClass().getAnnotation(JoinQueryConfig.class);
        String baseAlias = cfg.baseAlias();
        List<Field> fields = collectAllFields(getVoClass());
        List<String> columns = new ArrayList<>();
        for (Field f : fields) {
            String name = f.getName();
            if ("serialVersionUID".equals(name) || name.endsWith("Desc")) {
                continue;
            }
            JoinSelect joinSelect = f.getAnnotation(JoinSelect.class);
            if (joinSelect != null) {
                columns.add(joinSelect.value() + " AS " + camelToUnderline(name));
            } else {
                columns.add(baseAlias + "." + camelToUnderline(name) + " AS " + camelToUnderline(name));
            }
        }
        return "SELECT " + String.join(", ", columns);
    }

    private String buildFromJoinSql() {
        JoinQueryConfig cfg = this.getClass().getAnnotation(JoinQueryConfig.class);
        StringBuilder sb = new StringBuilder("FROM ")
                .append(cfg.baseTable()).append(" ").append(cfg.baseAlias()).append(" ");
        for (JoinTable jt : cfg.joins()) {
            sb.append(toJoinKeyword(jt.type())).append(" ")
                    .append(jt.table());
            String aliasOrTable = jt.alias().isBlank() ? jt.table() : jt.alias();
            if (!jt.alias().isBlank()) {
                sb.append(" ").append(jt.alias());
            }
            if (jt.type() != JoinType.CROSS) {
                String onExpr = jt.on();
                if (jt.autoDeletedFilter()) {
                    String deletedExpr = aliasOrTable + "." + jt.deletedColumn() + " = " + DeleteEnum.UNDELETED.getCode();
                    onExpr = onExpr.isBlank() ? deletedExpr : onExpr + " AND " + deletedExpr;
                }
                if (!onExpr.isBlank()) {
                    sb.append(" ON ").append(onExpr);
                }
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private NamedParameterJdbcTemplate getJoinJdbcTemplate() {
        if (namedParameterJdbcTemplate != null) {
            return namedParameterJdbcTemplate;
        }
        if (dataSource != null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            return namedParameterJdbcTemplate;
        }
        throw new IllegalStateException("Join query requires DataSource or NamedParameterJdbcTemplate bean.");
    }

    private String buildJoinWhereSql(Object dto) {
        JoinQueryConfig cfg = this.getClass().getAnnotation(JoinQueryConfig.class);
        String baseAlias = cfg.baseAlias();
        StringBuilder where = new StringBuilder("WHERE ")
                .append(baseAlias)
                .append(".").append(FieldNameConstant.COLUMN_DELETED).append(" = ")
                .append(DeleteEnum.UNDELETED.getCode());
        if (dto == null) {
            return where.toString();
        }
        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value == null) {
                    continue;
                }
                QueryField queryField = field.getAnnotation(QueryField.class);
                String column = queryField != null && !queryField.column().isEmpty()
                        ? queryField.column()
                        : baseAlias + "." + camelToUnderline(field.getName());
                String param = "p_" + field.getName();
                if (queryField != null) {
                    switch (queryField.type()) {
                        case EQ -> where.append(" AND ").append(column).append(" = :").append(param);
                        case LIKE ->
                                where.append(" AND ").append(column).append(" LIKE CONCAT('%', :").append(param).append(", '%')");
                        case GT -> where.append(" AND ").append(column).append(" > :").append(param);
                        case GE -> where.append(" AND ").append(column).append(" >= :").append(param);
                        case LT -> where.append(" AND ").append(column).append(" < :").append(param);
                        case LE -> where.append(" AND ").append(column).append(" <= :").append(param);
                        case IN -> where.append(" AND ").append(column).append(" IN (:").append(param).append(")");
                        case BETWEEN ->
                                where.append(" AND ").append(column).append(" BETWEEN :").append(param).append("_0 AND :").append(param).append("_1");
                    }
                    continue;
                }
                if (value instanceof String str && !str.isBlank()) {
                    where.append(" AND ").append(column).append(" LIKE CONCAT('%', :").append(param).append(", '%')");
                } else if (value instanceof Enum<?> || value instanceof Number || value instanceof Temporal || value instanceof Boolean) {
                    where.append(" AND ").append(column).append(" = :").append(param);
                }
            } catch (Exception ignored) {
            }
        }
        return where.toString();
    }

    private MapSqlParameterSource buildJoinParams(Object dto) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (dto != null) {
            for (Field field : dto.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(dto);
                    if (value == null) {
                        continue;
                    }
                    String param = "p_" + field.getName();
                    QueryField queryField = field.getAnnotation(QueryField.class);
                    if (queryField != null && queryField.type() == co.handk.common.enums.QueryType.BETWEEN && value instanceof List<?> list && list.size() == 2) {
                        map.put(param + "_0", list.get(0));
                        map.put(param + "_1", list.get(1));
                        continue;
                    }
                    if (value instanceof Enum<?> enumValue) {
                        map.put(param, resolveEnumDbValue(enumValue));
                    } else if (value instanceof String str) {
                        if (!str.isBlank()) {
                            map.put(param, str.trim());
                        }
                    } else {
                        map.put(param, value);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return new MapSqlParameterSource(map);
    }

    private String buildJoinOrderSql(PageQuery dto) {
        JoinQueryConfig cfg = this.getClass().getAnnotation(JoinQueryConfig.class);
        boolean asc = "asc".equalsIgnoreCase(dto.getSortOrder());
        String column = FieldNameConstant.COLUMN_UPDATE_TIME;
        if (FieldNameConstant.SORT_BY_CREATE_TIME.equals(dto.getSortBy())) {
            column = FieldNameConstant.COLUMN_CREATE_TIME;
        }
        return "ORDER BY " + cfg.baseAlias() + "." + column + (asc ? " ASC" : " DESC");
    }

    private List<Field> collectAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Collections.addAll(list, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return list;
    }

    private String toJoinKeyword(JoinType type) {
        return switch (type) {
            case INNER -> "INNER JOIN";
            case LEFT -> "LEFT JOIN";
            case RIGHT -> "RIGHT JOIN";
            case FULL -> "FULL JOIN";
            case CROSS -> "CROSS JOIN";
            case LEFT_OUTER -> "LEFT OUTER JOIN";
            case RIGHT_OUTER -> "RIGHT OUTER JOIN";
            case FULL_OUTER -> "FULL OUTER JOIN";
        };
    }
}


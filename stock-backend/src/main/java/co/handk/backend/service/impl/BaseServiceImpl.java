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
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity, V extends BaseVO>
        extends ServiceImpl<M, T>
        implements BaseService<T, V>, ApplicationContextAware {

    private static final Set<String> SKIP_DEFAULT_QUERY_FIELDS = Set.of("stockScope", "groupCode");

    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * internal helper
     */
    @Override
    public T getByIdNotDeleted(Serializable id) {
        return getOne(new QueryWrapper<T>().eq(FieldNameConstant.COLUMN_ID, id));
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
     * internal helper
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        T entity = toEntity(dto);
        applyDefaultStatusIfPresent(entity);
        return save(entity);
    }

    /**
     * internal helper
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        T entity = toEntity(dto);
        if (entity.getId() == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "IDは必須です");
        }
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq(FieldNameConstant.COLUMN_ID, entity.getId());
        boolean versioned = hasVersionField(entity.getClass());
        Long oldVersion = null;
        if (versioned) {
            T existed = getByIdNotDeleted(entity.getId());
            if (existed == null) {
                return false;
            }
            oldVersion = extractVersionValue(existed);
            if (oldVersion == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "versionは必須です"
                );
            }
            wrapper.eq(FieldNameConstant.COLUMN_VERSION, oldVersion);
        }
        buildUpdateSet(entity, wrapper);
        if (versioned && oldVersion != null) {
            wrapper.set(FieldNameConstant.COLUMN_VERSION, oldVersion + 1L);
        }
        int updated = baseMapper.update(null, wrapper);
        if (versioned && updated <= DeleteEnum.UNDELETED.getCode()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "更新に失敗しました。データが変更または削除されている可能性があります。再読み込みしてから再実行してください。"
            );
        }
        return updated > DeleteEnum.UNDELETED.getCode();
    }

    /**
     * internal helper
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<T>()
                .eq(FieldNameConstant.COLUMN_ID, id)
                .set(FieldNameConstant.COLUMN_DELETED, DeleteEnum.DELETED.getCode());
        if (hasVersionField(resolveEntityClass())) {
            T existed = getByIdNotDeleted(id);
            Long oldVersion = extractVersionValue(existed);
            if (oldVersion == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "versionは必須です"
                );
            }
            wrapper.eq(FieldNameConstant.COLUMN_VERSION, oldVersion);
            wrapper.set(FieldNameConstant.COLUMN_VERSION, oldVersion + 1L);
        }
        int deleted = baseMapper.update(null, wrapper);
        if (hasVersionField(resolveEntityClass()) && deleted <= DeleteEnum.UNDELETED.getCode()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "削除に失敗しました。データが変更または削除されている可能性があります。再読み込みしてから再実行してください。"
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
                .set(FieldNameConstant.COLUMN_DELETED, DeleteEnum.DELETED.getCode()));
    }

    @Override
    public boolean existsById(Long id) {
        return count(new QueryWrapper<T>().eq(FieldNameConstant.COLUMN_ID, id)) > NumberConstant.ZERO;
    }

    /**
     * internal helper
     */
    protected <Q> QueryWrapper<T> buildWrapper(Q dto) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        // internal helper
        if (dto == null) return wrapper;
        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (SKIP_DEFAULT_QUERY_FIELDS.contains(field.getName())) {
                continue;
            }
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
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "検索条件の構築に失敗しました",
                        e
                );
            }
        }
        buildJoinConditions(dto, wrapper);
        return wrapper;
    }

    protected <Q> void buildJoinConditions(Q dto, QueryWrapper<T> wrapper) {
        // internal helper
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
     * internal helper
     */
    protected void buildUpdateSet(T entity, UpdateWrapper<T> wrapper) {
        if (entity == null) return;
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // internal helper
                if (field.isAnnotationPresent(UpdateIgnore.class)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    // internal helper
                    if (value == null) continue;
                    String fieldName = field.getName();
                    // internal helper
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
                    throw new co.handk.backend.exception.BusinessException(
                            co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                            "更新項目の構築に失敗しました",
                            e
                    );
                }
            }
            clazz = clazz.getSuperclass(); // internal helper
        }
    }

    /**
     * internal helper
     */
    protected String camelToUnderline(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * internal helper
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

    private void applyDefaultStatusIfPresent(T entity) {
        if (entity == null) {
            return;
        }
        Field statusField = findField(entity.getClass(), FieldNameConstant.STATUS);
        if (statusField == null) {
            return;
        }
        try {
            statusField.setAccessible(true);
            Object currentValue = statusField.get(entity);
            if (currentValue != null) {
                return;
            }
            if (statusField.getType() == Integer.class || statusField.getType() == int.class) {
                statusField.set(entity, StatusEnum.NOMAL.getCode());
            }
        } catch (IllegalAccessException ignored) {
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
            if (Collection.class.isAssignableFrom(f.getType()) || Map.class.isAssignableFrom(f.getType())) {
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

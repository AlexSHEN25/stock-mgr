package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.service.BaseService;
import co.handk.common.enums.DeleteEnum;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class BaseServiceImpl<M extends BaseMapper<T>, T>
        extends ServiceImpl<M, T>
        implements BaseService<T> {

    @Override
    public int logicDeleteById(Serializable id) {
        if (id == null) {
            return 0;
        }
        return logicDeleteBatch(List.of(id));
    }

    @Override
    public int logicDeleteBatch(Collection<? extends Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.in("id", ids)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode())
                .set("updated_by", UserContext.getUserId())
                .set("update_time", LocalDateTime.now());

        return this.baseMapper.update(null, wrapper);
    }
}
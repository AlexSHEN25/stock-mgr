package co.handk.backend.service.impl;

import co.handk.backend.service.BaseService;
import co.handk.common.enums.DeleteEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.function.Consumer;

public class BaseServiceImpl<M extends BaseMapper<T>, T>
        extends ServiceImpl<M, T>
        implements BaseService<T> {

    public boolean updateByIdWithNotDeleted(
            SFunction<T, ?> idField,
            SFunction<T, ?> deletedField,
            Long id,
            Consumer<LambdaUpdateWrapper<T>> setter) {
        LambdaUpdateWrapper<T> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(idField, id)
                .eq(deletedField, DeleteEnum.UNDELETED.getCode());
        setter.accept(wrapper);
        return this.update(wrapper);
    }
}
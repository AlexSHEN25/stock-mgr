package co.handk.backend.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.function.Consumer;

public interface BaseService<T> extends IService<T> {

    boolean updateByIdWithNotDeleted(
            SFunction<T, ?> idField,
            SFunction<T, ?> deletedField,
            Long id,
            Consumer<LambdaUpdateWrapper<T>> setter);

}
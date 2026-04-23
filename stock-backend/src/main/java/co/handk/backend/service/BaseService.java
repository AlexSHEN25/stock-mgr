package co.handk.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;

public interface BaseService<T> extends IService<T> {

    /**
     * 单条逻辑删除
     * @param id 主键
     * @return 实际删除条数（0 或 1）
     */
    int logicDeleteById(Serializable id);

    /**
     * 批量逻辑删除
     * @param ids 主键集合
     * @return 实际删除条数
     */
    int logicDeleteBatch(Collection<? extends Serializable> ids);

}
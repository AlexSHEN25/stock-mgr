package co.handk.backend.service;

import co.handk.backend.entity.OperateLog;
import co.handk.common.model.dto.OperateLogDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface OperateLogService extends IService<OperateLog> {

    Boolean create(@NotNull OperateLogDTO dto);

    OperateLog get(@NotNull Long id);

    Boolean update(@NotNull OperateLogDTO dto);

    Boolean delete(@NotNull Long id);

    List<OperateLog> listAll();

    PageResult<OperateLog> pageQuery(@NotNull PageQuery query);
}

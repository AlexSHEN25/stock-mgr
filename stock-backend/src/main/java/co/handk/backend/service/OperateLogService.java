package co.handk.backend.service;
import co.handk.backend.entity.OperateLog;
import co.handk.common.model.dto.create.CreateOperateLogDTO;
import co.handk.common.model.dto.update.UpdateOperateLogDTO;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.common.model.dto.query.OperateLogQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface OperateLogService extends IService<OperateLog> {
    Boolean create(@NotNull CreateOperateLogDTO dto);
    OperateLogVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateOperateLogDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<OperateLogVO> pageQuery(@NotNull OperateLogQueryDTO query);
}

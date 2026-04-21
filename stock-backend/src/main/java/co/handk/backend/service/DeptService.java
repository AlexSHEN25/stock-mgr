package co.handk.backend.service;

import co.handk.backend.entity.Dept;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateDeptDTO;
import co.handk.common.model.dto.query.DeptQueryDTO;
import co.handk.common.model.dto.update.UpdateDeptDTO;
import co.handk.common.model.vo.DeptVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface DeptService extends IService<Dept> {
    Boolean create(@NotNull CreateDeptDTO dto);
    DeptVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateDeptDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<DeptVO> pageQuery(@NotNull DeptQueryDTO query);
}

package co.handk.backend.service;

import co.handk.backend.entity.Dept;
import co.handk.common.model.dto.DeptDTO;
import co.handk.common.model.vo.DeptVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface DeptService extends IService<Dept> {

    Boolean create(@NotNull DeptDTO dto);

    DeptVO get(@NotNull Long id);

    Boolean update(@NotNull DeptDTO dto);

    Boolean delete(@NotNull Long id);
    List<DeptVO> listAll();

    PageResult<DeptVO> pageQuery(@NotNull PageQuery query);
}

package co.handk.backend.service;

import co.handk.backend.entity.Maker;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateMakerDTO;
import co.handk.common.model.dto.query.MakerQueryDTO;
import co.handk.common.model.dto.update.UpdateMakerDTO;
import co.handk.common.model.vo.MakerVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface MakerService extends IService<Maker> {
    Boolean create(@NotNull CreateMakerDTO dto);
    MakerVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateMakerDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<MakerVO> pageQuery(@NotNull MakerQueryDTO query);
}

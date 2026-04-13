package co.handk.backend.service;

import co.handk.backend.entity.BrandMakerRelation;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateBrandMakerRelationDTO;
import co.handk.common.model.dto.query.BrandMakerRelationQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandMakerRelationDTO;
import co.handk.common.model.vo.BrandMakerRelationVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface BrandMakerRelationService extends IService<BrandMakerRelation> {
    Boolean create(@NotNull CreateBrandMakerRelationDTO dto);

    BrandMakerRelationVO get(@NotNull Long id);

    Boolean update(@NotNull UpdateBrandMakerRelationDTO dto);

    Boolean delete(@NotNull Long id);

    PageResult<BrandMakerRelationVO> pageQuery(@NotNull BrandMakerRelationQueryDTO query);
}

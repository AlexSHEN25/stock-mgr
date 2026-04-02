package co.handk.backend.service;
import co.handk.backend.entity.Brand;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandVO;
import co.handk.common.model.dto.query.BrandQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface BrandService extends IService<Brand> {
    Boolean create(@NotNull CreateBrandDTO dto);
    BrandVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateBrandDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<BrandVO> pageQuery(@NotNull BrandQueryDTO query);
}

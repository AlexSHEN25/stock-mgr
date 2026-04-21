package co.handk.backend.service;

import co.handk.backend.entity.Config;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateConfigDTO;
import co.handk.common.model.dto.query.ConfigQueryDTO;
import co.handk.common.model.dto.update.UpdateConfigDTO;
import co.handk.common.model.vo.ConfigVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface ConfigService extends IService<Config> {
    Boolean create(@NotNull CreateConfigDTO dto);
    ConfigVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateConfigDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<ConfigVO> pageQuery(@NotNull ConfigQueryDTO query);
}

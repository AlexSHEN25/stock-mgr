package co.handk.backend.service;

import co.handk.backend.entity.Config;
import co.handk.common.model.dto.ConfigDTO;
import co.handk.common.model.vo.ConfigVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface ConfigService extends IService<Config> {

    Boolean create(@NotNull ConfigDTO dto);

    ConfigVO get(@NotNull Long id);

    Boolean update(@NotNull ConfigDTO dto);

    Boolean delete(@NotNull Long id);
    List<ConfigVO> listAll();

    PageResult<ConfigVO> pageQuery(@NotNull PageQuery query);
}

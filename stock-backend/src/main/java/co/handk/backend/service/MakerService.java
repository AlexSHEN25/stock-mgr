package co.handk.backend.service;

import co.handk.backend.entity.Maker;
import co.handk.common.model.dto.MakerDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface MakerService extends IService<Maker> {

    Boolean create(@NotNull MakerDTO dto);

    Maker get(@NotNull Long id);

    Boolean update(@NotNull MakerDTO dto);

    Boolean delete(@NotNull Long id);

    List<Maker> listAll();

    PageResult<Maker> pageQuery(@NotNull PageQuery query);
}

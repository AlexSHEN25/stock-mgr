package co.handk.backend.service;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.dto.RequestFormDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface RequestFormService extends IService<RequestForm> {

    Boolean create(@NotNull RequestFormDTO dto);

    RequestForm get(@NotNull Long id);

    Boolean update(@NotNull RequestFormDTO dto);

    Boolean delete(@NotNull Long id);

    List<RequestForm> listAll();

    PageResult<RequestForm> pageQuery(@NotNull PageQuery query);
}

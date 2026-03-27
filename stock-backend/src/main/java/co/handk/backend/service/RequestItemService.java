package co.handk.backend.service;

import co.handk.backend.entity.RequestItem;
import co.handk.common.model.dto.RequestItemDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface RequestItemService extends IService<RequestItem> {

    Boolean create(@NotNull RequestItemDTO dto);

    RequestItem get(@NotNull Long id);

    Boolean update(@NotNull RequestItemDTO dto);

    Boolean delete(@NotNull Long id);

    List<RequestItem> listAll();

    PageResult<RequestItem> pageQuery(@NotNull PageQuery query);
}

package co.handk.backend.service;

import co.handk.backend.entity.Permission;
import co.handk.common.model.vo.EnumOptionVO;
import co.handk.common.model.vo.OptionVO;
import co.handk.common.model.vo.PermissionVO;
import co.handk.common.model.vo.TextOptionVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public interface PermissionService extends BaseService<Permission, PermissionVO> {
    List<OptionVO> options();

    List<TextOptionVO> moduleOptions();

    List<EnumOptionVO> typeOptions();

    List<EnumOptionVO> statusOptions();
}

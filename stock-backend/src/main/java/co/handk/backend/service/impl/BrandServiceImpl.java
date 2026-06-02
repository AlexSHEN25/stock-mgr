package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.entity.Brand;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.FileStorageService;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl extends BaseServiceImpl<BrandMapper, Brand, BrandVO>
        implements BrandService {

    private final FileStorageService fileStorageService;

    @Override
    protected BrandVO toVO(Brand entity) {
        if (entity == null) {
            return null;
        }
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setImage(fileStorageService.toApiPath(UploadBizType.BRAND, vo.getImage()));
        return vo;
    }

    @Override
    protected <D> Brand toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Brand entity = new Brand();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateBrandDTO createDto) {
            createDto.setImage(fileStorageService.normalize(UploadBizType.BRAND, createDto.getImage()));
        }
        return super.saveByDto(dto);
    }

    @Override
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateBrandDTO updateDto) {
            updateDto.setImage(fileStorageService.normalize(UploadBizType.BRAND, updateDto.getImage()));
        }
        return super.updateByDto(dto);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        String imagePath = fileStorageService.upload(UploadBizType.BRAND, file, null);
        return fileStorageService.toApiPath(UploadBizType.BRAND, imagePath);
    }

    @Override
    public String replaceImage(Long id, MultipartFile file) {
        if (id == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ブランドIDは必須です");
        }
        Brand existed = this.getByIdNotDeleted(id);
        if (existed == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ブランドが存在しません");
        }
        String imagePath = fileStorageService.upload(UploadBizType.BRAND, file, existed.getImage());
        existed.setImage(imagePath);
        this.updateById(existed);
        return fileStorageService.toApiPath(UploadBizType.BRAND, imagePath);
    }
}

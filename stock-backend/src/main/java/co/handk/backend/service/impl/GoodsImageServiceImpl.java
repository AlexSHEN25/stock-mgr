package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.GoodsImageMapper;
import co.handk.backend.service.FileStorageService;
import co.handk.backend.service.GoodsImageService;
import co.handk.common.model.vo.GoodsImageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GoodsImageServiceImpl extends BaseServiceImpl<GoodsImageMapper, GoodsImage, GoodsImageVO>
        implements GoodsImageService {

    private final FileStorageService fileStorageService;

    @Override
    protected GoodsImageVO toVO(GoodsImage entity) {
        if (entity == null) {
            return null;
        }
        GoodsImageVO vo = new GoodsImageVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setImageUrl(fileStorageService.toApiPath(UploadBizType.GOODS, vo.getImageUrl()));
        return vo;
    }

    @Override
    protected <D> GoodsImage toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        GoodsImage entity = new GoodsImage();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        String imagePath = fileStorageService.upload(UploadBizType.GOODS, file, null);
        return fileStorageService.toApiPath(UploadBizType.GOODS, imagePath);
    }

    @Override
    public String replaceImage(Long imageId, MultipartFile file) {
        if (imageId == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像IDは必須です");
        }
        GoodsImage existed = this.getByIdNotDeleted(imageId);
        if (existed == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "商品画像が存在しません");
        }
        String imagePath = fileStorageService.upload(UploadBizType.GOODS, file, existed.getImageUrl());
        existed.setImageUrl(imagePath);
        this.updateById(existed);
        return fileStorageService.toApiPath(UploadBizType.GOODS, imagePath);
    }
}

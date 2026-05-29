package co.handk.backend.service.impl;

import co.handk.backend.config.AvatarStorageProperties;
import co.handk.backend.config.BrandImageStorageProperties;
import co.handk.backend.config.GoodsImageStorageProperties;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final String API_PREFIX = "/api";
    private static final int AVATAR_MAX_LENGTH = 64;
    private static final int DEFAULT_MAX_LENGTH = 128;

    private final AvatarStorageProperties avatarStorageProperties;
    private final GoodsImageStorageProperties goodsImageStorageProperties;
    private final BrandImageStorageProperties brandImageStorageProperties;

    @Override
    public String upload(UploadBizType bizType, MultipartFile file, String oldPath) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ファイルは必須です");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像ファイルのみアップロード可能です");
        }
        BizMeta meta = resolveMeta(bizType);
        String ext = resolveExtension(file.getOriginalFilename());
        String fileName = buildFileName(ext);
        try {
            Files.createDirectories(meta.dir());
            Path target = meta.dir().resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            deleteOldIfNeeded(oldPath, meta);
            String path = meta.uriPrefix() + fileName;
            ensureLength(path, meta.maxLength());
            return path;
        } catch (IOException e) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ファイル保存に失敗しました", e);
        }
    }

    @Override
    public String normalize(UploadBizType bizType, String incomingPathOrData) {
        if (!StringUtils.hasText(incomingPathOrData)) {
            return incomingPathOrData;
        }
        BizMeta meta = resolveMeta(bizType);
        String trimmed = incomingPathOrData.trim();
        if (trimmed.startsWith("data:image/")) {
            return saveBase64(meta, trimmed);
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                URI uri = URI.create(trimmed);
                String path = uri.getPath();
                if (!StringUtils.hasText(path)) {
                    throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像URLのパスが不正です");
                }
                trimmed = path;
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像URL形式が不正です");
            }
        }
        ensureLength(trimmed, meta.maxLength());
        return trimmed;
    }

    @Override
    public String toApiPath(UploadBizType bizType, String rawPath) {
        if (!StringUtils.hasText(rawPath)) {
            return rawPath;
        }
        BizMeta meta = resolveMeta(bizType);
        if (rawPath.startsWith(meta.uriPrefix()) && !rawPath.startsWith(API_PREFIX + meta.uriPrefix())) {
            return API_PREFIX + rawPath;
        }
        return rawPath;
    }

    private String saveBase64(BizMeta meta, String dataUri) {
        try {
            int comma = dataUri.indexOf(',');
            if (comma <= 0 || comma >= dataUri.length() - 1) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像データ形式が不正です");
            }
            String header = dataUri.substring(0, comma).toLowerCase();
            String payload = dataUri.substring(comma + 1);
            String ext = ".png";
            if (header.contains("image/jpeg") || header.contains("image/jpg")) {
                ext = ".jpg";
            } else if (header.contains("image/webp")) {
                ext = ".webp";
            } else if (header.contains("image/gif")) {
                ext = ".gif";
            } else if (header.contains("image/svg+xml")) {
                ext = ".svg";
            }
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (bytes.length == 0) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像データが空です");
            }
            Files.createDirectories(meta.dir());
            String fileName = buildFileName(ext);
            Path target = meta.dir().resolve(fileName);
            Files.write(target, bytes);
            String path = meta.uriPrefix() + fileName;
            ensureLength(path, meta.maxLength());
            return path;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像保存に失敗しました", e);
        }
    }

    private void ensureLength(String path, int maxLength) {
        if (StringUtils.hasText(path) && path.length() > maxLength) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像パスが長すぎます");
        }
    }

    private void deleteOldIfNeeded(String oldPath, BizMeta meta) {
        if (!StringUtils.hasText(oldPath) || !oldPath.startsWith(meta.uriPrefix())) {
            return;
        }
        String oldFileName = oldPath.substring(meta.uriPrefix().length());
        if (!StringUtils.hasText(oldFileName)) {
            return;
        }
        try {
            Files.deleteIfExists(meta.dir().resolve(oldFileName));
        } catch (IOException ignored) {
        }
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return ".png";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot >= originalFilename.length() - 1) {
            return ".png";
        }
        String ext = originalFilename.substring(dot).toLowerCase();
        if (ext.length() > 10) {
            return ".png";
        }
        return ext;
    }

    private String buildFileName(String ext) {
        String token = Long.toString(System.currentTimeMillis(), 36);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return token + random + ext;
    }

    private BizMeta resolveMeta(UploadBizType bizType) {
        if (bizType == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "bizTypeは必須です");
        }
        return switch (bizType) {
            case AVATAR -> new BizMeta("/avatar/upload/", avatarStorageProperties.getUploadDir(), AVATAR_MAX_LENGTH);
            case GOODS -> new BizMeta("/upload/goods/", goodsImageStorageProperties.getRootDir(), DEFAULT_MAX_LENGTH);
            case BRAND -> new BizMeta("/upload/brand/", brandImageStorageProperties.getRootDir(), DEFAULT_MAX_LENGTH);
        };
    }

    private record BizMeta(String uriPrefix, Path dir, int maxLength) {
    }
}


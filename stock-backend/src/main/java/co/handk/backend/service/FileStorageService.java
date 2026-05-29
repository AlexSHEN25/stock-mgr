package co.handk.backend.service;

import co.handk.backend.constant.UploadBizType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String upload(UploadBizType bizType, MultipartFile file, String oldPath);

    String normalize(UploadBizType bizType, String incomingPathOrData);

    String toApiPath(UploadBizType bizType, String rawPath);
}


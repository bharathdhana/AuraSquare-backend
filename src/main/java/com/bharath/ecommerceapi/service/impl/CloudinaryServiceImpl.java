package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.BadRequestException;
import com.bharath.ecommerceapi.service.inf.ICloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements ICloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Override
    public Map<String, String> uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new BadRequestException("Failed to upload image to Cloudinary");
            }

            return Map.of(
                    "secureUrl", secureUrl.toString(),
                    "publicId", publicId.toString()
            );
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            log.error("Cloudinary delete failed for publicId={}", publicId, e);
            throw new BadRequestException("Failed to delete image: " + e.getMessage());
        }
    }
}

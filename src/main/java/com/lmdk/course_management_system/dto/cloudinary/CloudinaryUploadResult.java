package com.lmdk.course_management_system.dto.cloudinary;

public record CloudinaryUploadResult(
        String publicId,
        String url,
        String fileName
) {
}
package com.example.financas.user.service;

import com.example.financas.exceptions.NotFoundException;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class SavePhotoService {

    private final UserRepository userRepository;
    private final MinioClient minioClient;

    public SavePhotoService(UserRepository userRepository, MinioClient minioClient) {
        this.minioClient = minioClient;
        this.userRepository = userRepository;
    }

    public String updateUserPhoto(UUID userId, MultipartFile photo) throws Exception{
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found User"));

        String bucketName = "financas-archives";

        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String extension = photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf("."));
        String randomName = UUID.randomUUID().toString() + extension;

        String archivePath = "profile-pictures/user_" + userId + "/" + randomName;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(archivePath)
                        .stream(photo.getInputStream(), photo.getSize(), -1)
                        .contentType(photo.getContentType())
                        .build()
        );

        String publicUrl = "http://localhost:9000/" + bucketName + "/" + archivePath;

        user.setPhotoUrl(publicUrl);
        userRepository.save(user);
        return publicUrl;
    }

}

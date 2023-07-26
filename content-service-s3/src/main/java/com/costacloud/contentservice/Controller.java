package com.costacloud.contentservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/content")
public class Controller {
    @Autowired
    private S3Client s3Client;

    @GetMapping("/bucket/{bucketName}/files")
    public List<String> fileNamesInBucket(@PathVariable String bucketName) {
        List<String> fileNames = new ArrayList<>();
        for(S3Object object : s3Client.listObjects(ListObjectsRequest.builder().bucket(bucketName).build()).contents()){
            fileNames.add(object.key());
        }
        return fileNames;
    }

    @PostMapping(path = "bucket/{bucketName}/files", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFile(@RequestPart(value="file", required=false) MultipartFile file, @PathVariable String bucketName) throws IOException {
        InputStream inputStream =  new BufferedInputStream(file.getInputStream());
        PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(file.getOriginalFilename()).build(), RequestBody.fromInputStream(inputStream, file.getSize()));
        if (putObjectResponse.sdkHttpResponse().isSuccessful()) {
            return "File uploaded successfully";
        } else {
            return "File upload failed";
        }
    }

    @GetMapping(path = "bucket/{bucketName}/files/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String bucketName, @PathVariable String fileName) throws IOException {
        byte[] content = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(fileName).build()).readAllBytes();
        ByteArrayResource resource = new ByteArrayResource(content);
        return ResponseEntity
                .ok()
                .contentLength(content.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("create/{bucketName}")
    public String createBucket(@PathVariable String bucketName) {
        CreateBucketResponse response = s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        if(response.sdkHttpResponse().isSuccessful()) {
            return "Bucket created successfully";
        } else {
            return "Bucket couldn't be created";
        }
    }

    @DeleteMapping("bucket/{bucketName}/files/{fileName}")
    public String deleteBucket(@PathVariable String bucketName, @PathVariable String fileName) {
        DeleteObjectResponse response = s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build());
        if (response.sdkHttpResponse().isSuccessful()) {
            return "File deleted successfully";
        } else {
            return "File couldn't be deleted";
        }
    }

    @DeleteMapping("bucket/{bucketName}")
    public String deleteBucket(@PathVariable String bucketName) {
        DeleteBucketResponse response = s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
        if (response.sdkHttpResponse().isSuccessful()) {
            return "Bucket deleted successfully";
        } else {
            return "Bucket couldn't be deleted";
        }
    }

    }

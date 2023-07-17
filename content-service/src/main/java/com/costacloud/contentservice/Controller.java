package com.costacloud.contentservice;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/content")
public class Controller {
    @Autowired
    private MinioClient minioClient;

    @GetMapping("/bucket/{bucketName}")
    public boolean bucketNameAvailable(@PathVariable String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/bucket/{bucketName}/files")
    public List<String> fileNamesInBucket(@PathVariable String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build());
        List<String> fileNames = new ArrayList<>();
        results.forEach(resultItem -> {
            try {
                fileNames.add(resultItem.get().objectName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return fileNames;
    }

    @PostMapping(path = "bucket/{bucketName}/files", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void uploadFile(@RequestPart(value="file", required=false) MultipartFile file, @PathVariable String bucketName) throws IOException {
        InputStream inputStream =  new BufferedInputStream(file.getInputStream());
        PutObjectArgs poa = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(file.getOriginalFilename())
                .stream(inputStream, -1, 10485760)
                .build();
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(poa);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(path = "bucket/{bucketName}/files/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String bucketName, @PathVariable String fileName) throws IOException {
        InputStream obj = null;
        try {
            obj = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] content = IOUtils.toByteArray(obj);
        obj.close();
        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity
                .ok()
                .contentLength(content.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);

    }

    @GetMapping("create/{bucketName}")
    public void createBucket(@PathVariable String bucketName) {
        System.out.println("method called...");
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("bucket/{bucketName}/files/{fileName}")
    public void deleteBucket(@PathVariable String bucketName, @PathVariable String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    }

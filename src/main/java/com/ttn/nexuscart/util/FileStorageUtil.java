package com.ttn.nexuscart.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private static final String BASE_FOLDER = "src/main/resources/static/product-variation/";
    private static final String IMAGE_ACCESS_URL_PREFIX = "http://localhost:8080/users/product-variation/";
    public String saveImage(MultipartFile image, UUID variationId) throws IOException {
        // Construct the new filename using the variation ID
        String filename = "product-" + variationId + "_" + image.getOriginalFilename();

        // Define the full path to save the image
        Path path = Paths.get(BASE_FOLDER + filename);
        Files.createDirectories(path.getParent());

        // Write the file to disk
        Files.write(path, image.getBytes());

        // Return the access URL for the image
        return IMAGE_ACCESS_URL_PREFIX + filename;
    }


    public List<String> saveImages(List<MultipartFile> images,UUID variationId) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile image : images) {
            urls.add(saveImage(image,variationId));
        }
        return urls;
    }

}
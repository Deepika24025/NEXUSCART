package com.ttn.nexuscart.controller;

import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@RestController
@RequestMapping("/users")
public class ProfileImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("image") MultipartFile file, Principal principal) throws IOException {
        String email = principal.getName();

        // Create directory if not exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Get the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Creating unique filename using ID
        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = user.getId() + "_" + System.currentTimeMillis() + "." + fileExtension;

        // Saving file
        Path targetLocation = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Save filename to user
        user.setImage(filename);
        userRepository.save(user);

        return ResponseEntity.ok("Image uploaded successfully");
    }


    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).toAbsolutePath().resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // You can also determine the mime type dynamically
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    private final String IMAGE_FOLDER = "src/main/resources/static/product-variation/";

    @GetMapping("/product-variation/{imageName:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String imageName) {
        try {
            Path path = Paths.get(IMAGE_FOLDER).resolve(imageName).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

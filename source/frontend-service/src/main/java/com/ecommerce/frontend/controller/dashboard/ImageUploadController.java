package com.ecommerce.frontend.controller.dashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Endpoint upload/chon anh cho form san pham o dashboard. Nam duoi /dashboard/**
// nen tu dong duoc AdminAuthInterceptor bao ve - chi tai khoan da dang nhap moi
// upload duoc.
@Controller
@RequestMapping("/dashboard/upload")
public class ImageUploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final int MAX_PAGE_SIZE = 100;

    @PostMapping("/image")
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files (jpg, jpeg, png, gif, webp) are allowed"));
        }

        try {
            // getAbsoluteFile() la BAT BUOC: MultipartFile.transferTo(File) voi duong
            // dan tuong doi se tu resolve theo thu muc tam noi bo cua Tomcat embedded,
            // KHONG PHAI thu muc chay ung dung nhu cac thao tac File thong thuong khac.
            File dir = new File(uploadDir).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Dat ten file bang UUID (khong dung ten goc tu client) de tranh trung
            // ten/ghi de va tranh moi rui ro path traversal.
            String newFileName = UUID.randomUUID() + "." + extension;
            File target = new File(dir, newFileName);
            file.transferTo(target);

            return ResponseEntity.ok(Map.of("fileName", newFileName));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // offset/limit phan trang + search loc theo ten file (khong phan biet hoa
    // thuong), de tranh load toan bo thu muc uploads/ moi lan mo tab "Choose
    // existing" khi so anh da upload ngay cang nhieu.
    @GetMapping("/images")
    @ResponseBody
    public Map<String, Object> listImages(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "40") int limit,
            @RequestParam(value = "search", defaultValue = "") String search) {
        offset = Math.max(0, offset);
        limit = Math.max(1, Math.min(limit, MAX_PAGE_SIZE));

        File dir = new File(uploadDir).getAbsoluteFile();
        File[] files = dir.listFiles((d, name) -> {
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
            return ALLOWED_EXTENSIONS.contains(ext);
        });
        if (files == null) {
            files = new File[0];
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        String searchLower = search.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (File f : files) {
            if (searchLower.isEmpty() || f.getName().toLowerCase().contains(searchLower)) {
                matched.add(f.getName());
            }
        }

        int end = Math.min(offset + limit, matched.size());
        List<String> page = offset < matched.size() ? matched.subList(offset, end) : List.of();
        boolean hasMore = end < matched.size();

        return Map.of("images", page, "hasMore", hasMore);
    }
}

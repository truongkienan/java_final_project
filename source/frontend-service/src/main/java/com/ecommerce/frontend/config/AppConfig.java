package com.ecommerce.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    // Thu muc luu anh upload tu dashboard (nam ngoai classpath de anh khong bi mat
    // moi lan build lai jar). Mac dinh la ./uploads (tuong doi so voi thu muc chay
    // ung dung), co the doi qua property app.upload.dir neu can.
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Chặn toàn bộ /dashboard/** nếu chưa đăng nhập hoặc không đủ permission,
    // trừ trang đăng nhập Admin, trang thông báo từ chối truy cập,
    // và các thư mục tài nguyên tĩnh /admin/css, /admin/js, /admin/assets (thư mục static
    // này không đổi tên theo route /dashboard/**) - phải luôn truy cập được kể cả khi chưa
    // đăng nhập (nếu không, ngay cả CSS/JS của chính trang login cũng bị chặn theo).
    // (/dashboard/register đã bị xóa - không còn cần loại trừ nữa)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/dashboard/**")
                .excludePathPatterns(
                        "/dashboard/login", "/dashboard/access-denied",
                        "/admin/css/**", "/admin/js/**", "/admin/assets/**");
    }

    // Gop 2 nguon anh vao chung 1 URL /client/images/** - anh demo co san (trong
    // classpath, chi doc) va anh moi upload tu dashboard (thu muc uploads/ ben ngoai,
    // ghi duoc). Spring tu thu tim lan luot theo thu tu khai bao, khong tim thay o
    // nguon truoc thi thu nguon sau. Nho vay khong can sua lai cac noi dang hien thi
    // anh san pham (dashboard/products.html, client/index.html, client/single.html).
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // getAbsolutePath() de dam bao tro chinh xac cung 1 thu muc vat ly ma
        // ImageUploadController dung khi luu file (tranh lech duong dan tuong doi
        // giua cac thanh phan resolve theo working directory khac nhau).
        String absoluteUploadDir = new java.io.File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/client/images/**")
                .addResourceLocations("classpath:/static/client/images/", "file:" + absoluteUploadDir + "/");
    }
}
package com.ecommerce.catalog.grpc;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@GrpcService
public class CatalogGrpcServiceImpl extends CatalogGrpcServiceGrpc.CatalogGrpcServiceImplBase {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void checkProductPrice(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        String productIdStr = request.getProductId();

        try {
            // Trong DB Catalog của chúng ta, Id sản phẩm là số nguyên (Integer)
            Integer productId = Integer.parseInt(productIdStr);
            Optional<Product> productOpt = productRepository.findById(productId);

            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                // Ưu tiên giá khuyến mãi (nếu có), nếu không có thì lấy giá gốc
                double finalPrice = (p.getSaleOfPrice() != null && p.getSaleOfPrice().doubleValue() > 0)
                        ? p.getSaleOfPrice().doubleValue()
                        : (p.getUnitPrice() != null ? p.getUnitPrice().doubleValue() : 0);

                // Dùng Builder (Pattern sinh ra bởi Protobuf) để đóng gói dữ liệu trả về
                ProductResponse response = ProductResponse.newBuilder()
                        .setProductId(productIdStr)
                        .setPrice(finalPrice)
                        .setExists(true)
                        .build();

                // Gửi câu trả lời về cho Client
                responseObserver.onNext(response);
            } else {
                // Không tìm thấy sản phẩm
                ProductResponse response = ProductResponse.newBuilder()
                        .setProductId(productIdStr)
                        .setExists(false)
                        .build();
                responseObserver.onNext(response);
            }
        } catch (Exception e) {
            // Lỗi parse số hoặc lỗi DB
            ProductResponse response = ProductResponse.newBuilder()
                    .setProductId(productIdStr)
                    .setExists(false)
                    .build();
            responseObserver.onNext(response);
        }

        // Bắt buộc phải gọi onCompleted() để báo hiệu kết thúc luồng trả kết quả
        responseObserver.onCompleted();
    }
}

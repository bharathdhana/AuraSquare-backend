package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.ResourceNotFoundException;
import com.bharath.ecommerceapi.exception.UnAuthorizedException;
import com.bharath.ecommerceapi.model.Product;
import com.bharath.ecommerceapi.model.User;
import com.bharath.ecommerceapi.model.dto.request.ProductRequest;
import com.bharath.ecommerceapi.model.dto.response.ProductResponse;
import com.bharath.ecommerceapi.model.enums.Role;
import com.bharath.ecommerceapi.repo.ProductRepository;
import com.bharath.ecommerceapi.service.inf.ICloudinaryService;
import com.bharath.ecommerceapi.service.inf.IProductService;
import com.bharath.ecommerceapi.service.inf.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final IUserService userService;
    private final ICloudinaryService cloudinaryService;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return mapToProductResponse(productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + id)));
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword).stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public String createProduct(ProductRequest request, MultipartFile image) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != Role.SELLER) {
            throw new UnAuthorizedException("Access Denied! Only SELLER's can perform this operation");
        }

        Map<String, String> uploadedImage = cloudinaryService.uploadImage(image);

        Product product = Product.builder()
                .title(request.getTitle())
                .brand(request.getBrand())
                .model(request.getModel())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .imageUrl(uploadedImage.get("secureUrl"))
                .imagePublicId(uploadedImage.get("publicId"))
                .seller(currentUser)
                .build();
        productRepository.save(product);
        return "Product Created Successfully";
    }

    @Override
    @Transactional
    public String updateProduct(Long id, ProductRequest request, MultipartFile image) {
        Product product = productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + id));
        User currentUser = userService.getCurrentUser();
        if (!(product.getSeller().getId().equals(currentUser.getId())) && currentUser.getRole() != Role.ADMIN) {
            throw new UnAuthorizedException("Access Denied! Only SELLER's of this Product or ADMIN can ONLY perform this operation");
        }

        product.setTitle(request.getTitle());
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());

        if (image != null && !image.isEmpty()) {
            String previousPublicId = product.getImagePublicId();
            Map<String, String> uploadedImage = cloudinaryService.uploadImage(image);
            product.setImageUrl(uploadedImage.get("secureUrl"));
            product.setImagePublicId(uploadedImage.get("publicId"));
            cloudinaryService.deleteImage(previousPublicId);
        }

        return "Product Updated Successfully";
    }

    @Override
    @Transactional
    public String deleteProduct(Long id) {
        Product product = productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + id));
        User currentUser = userService.getCurrentUser();
        if (!(product.getSeller().getId().equals(currentUser.getId())) && currentUser.getRole() != Role.ADMIN) {
            throw new UnAuthorizedException("Access Denied! Only SELLER's of this Product or ADMIN can ONLY perform this operation");
        }

        String imagePublicId = product.getImagePublicId();
        productRepository.delete(product);
        cloudinaryService.deleteImage(imagePublicId);
        return "Product Deleted Successfully";
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .brand(product.getBrand())
                .model(product.getModel())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName())
                .build();
    }
}

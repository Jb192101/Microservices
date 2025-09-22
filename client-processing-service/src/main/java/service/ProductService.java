package service;

import entity.Product;
import entity.ProductKey;
import repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        if (product.getCreateDate() == null) {
            product.setCreateDate(LocalDate.now());
        }

        if (productRepository.existsByKey(product.getKey())) {
            throw new IllegalArgumentException("Продукт с ключом уже существует: " + product.getKey());
        }

        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByKey(ProductKey key) {
        return productRepository.findByKey(key);
    }

    public List<Product> getProductsByKeys(List<ProductKey> keys) {
        return productRepository.findByKeyIn(keys);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с id не найден: " + id));

        if (!product.getKey().equals(productDetails.getKey()) &&
                productRepository.existsByKey(productDetails.getKey())) {
            throw new IllegalArgumentException("Продукт с ключом уже существует: " + productDetails.getKey());
        }

        product.setName(productDetails.getName());
        product.setKey(productDetails.getKey());
        product.setCreateDate(productDetails.getCreateDate());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Продукт с id не найден: " + id);
        }
        productRepository.deleteById(id);
    }
}

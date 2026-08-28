package com.example.demo.service;

import com.example.demo.model.ProductDimension;
import com.example.demo.model.SaleFact;
import com.example.demo.model.StoreDimension;
import com.example.demo.model.TimeDimension;
import com.example.demo.repository.ProductDimensionRepository;
import com.example.demo.repository.SaleFactRepository;
import com.example.demo.repository.StoreDimensionRepository;
import com.example.demo.repository.TimeDimensionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductDimensionRepository productRepo;
    private final StoreDimensionRepository storeRepo;
    private final TimeDimensionRepository timeRepo;
    private final SaleFactRepository saleRepo;

    public DataInitializer(ProductDimensionRepository productRepo,
                           StoreDimensionRepository storeRepo,
                           TimeDimensionRepository timeRepo,
                           SaleFactRepository saleRepo) {
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.timeRepo = timeRepo;
        this.saleRepo = saleRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepo.count() > 0) {
            return; // Data already exists
        }

        // 1. Seed Products
        ProductDimension p1 = productRepo.save(new ProductDimension("MacBook Pro M3", "Electronics", "Apple", 1499.99));
        ProductDimension p2 = productRepo.save(new ProductDimension("Dell XPS 15", "Electronics", "Dell", 1299.00));
        ProductDimension p3 = productRepo.save(new ProductDimension("Sony WH-1000XM5 Headphones", "Electronics", "Sony", 279.00));
        ProductDimension p4 = productRepo.save(new ProductDimension("Air Jordan 1 Retro", "Fashion & Apparel", "Nike", 180.00));
        ProductDimension p5 = productRepo.save(new ProductDimension("Classic Denim Jacket", "Fashion & Apparel", "Levi's", 89.50));
        ProductDimension p6 = productRepo.save(new ProductDimension("Smart 4K OLED TV 65\"", "Home & Living", "Samsung", 1399.00));
        ProductDimension p7 = productRepo.save(new ProductDimension("Espresso Coffee Maker Pro", "Home & Living", "Breville", 450.00));
        ProductDimension p8 = productRepo.save(new ProductDimension("Mountain Trail Bike", "Sports & Outdoors", "Trek", 650.00));

        // 2. Seed Stores / Regions
        StoreDimension s1 = storeRepo.save(new StoreDimension("Manhattan 5th Ave", "New York", "NY", "East"));
        StoreDimension s2 = storeRepo.save(new StoreDimension("Silicon Valley Hub", "San Francisco", "CA", "West"));
        StoreDimension s3 = storeRepo.save(new StoreDimension("Michigan Ave Gallery", "Chicago", "IL", "North"));
        StoreDimension s4 = storeRepo.save(new StoreDimension("Austin Tech Ridge", "Austin", "TX", "South"));
        StoreDimension s5 = storeRepo.save(new StoreDimension("Seattle Pine Street", "Seattle", "WA", "West"));

        // 3. Seed Time Dimension dates (2025 - 2026 dates)
        LocalDate d1 = LocalDate.of(2025, 1, 15);
        LocalDate d2 = LocalDate.of(2025, 2, 20);
        LocalDate d3 = LocalDate.of(2025, 4, 10);
        LocalDate d4 = LocalDate.of(2025, 6, 25);
        LocalDate d5 = LocalDate.of(2025, 8, 14);
        LocalDate d6 = LocalDate.of(2025, 10, 5);
        LocalDate d7 = LocalDate.of(2025, 11, 28);
        LocalDate d8 = LocalDate.of(2025, 12, 24);
        LocalDate d9 = LocalDate.of(2026, 1, 18);
        LocalDate d10 = LocalDate.of(2026, 2, 10);

        TimeDimension t1 = timeRepo.save(new TimeDimension(d1));
        TimeDimension t2 = timeRepo.save(new TimeDimension(d2));
        TimeDimension t3 = timeRepo.save(new TimeDimension(d3));
        TimeDimension t4 = timeRepo.save(new TimeDimension(d4));
        TimeDimension t5 = timeRepo.save(new TimeDimension(d5));
        TimeDimension t6 = timeRepo.save(new TimeDimension(d6));
        TimeDimension t7 = timeRepo.save(new TimeDimension(d7));
        TimeDimension t8 = timeRepo.save(new TimeDimension(d8));
        TimeDimension t9 = timeRepo.save(new TimeDimension(d9));
        TimeDimension t10 = timeRepo.save(new TimeDimension(d10));

        // 4. Seed Fact Sales with Temporal Valid Ranges
        createSale(p1, s1, t1, "TechCorp Industries", 3, 1499.99, 100.0, LocalDateTime.of(2025, 1, 1, 0, 0), null, "ACTIVE");
        createSale(p3, s2, t2, "Alice Morgan", 2, 279.00, 20.0, LocalDateTime.of(2025, 2, 1, 0, 0), null, "ACTIVE");
        createSale(p4, s4, t3, "David Miller", 4, 180.00, 30.0, LocalDateTime.of(2025, 4, 1, 0, 0), null, "ACTIVE");
        createSale(p2, s3, t4, "Cybernetics Lab", 2, 1299.00, 50.0, LocalDateTime.of(2025, 6, 1, 0, 0), null, "ACTIVE");
        createSale(p6, s5, t5, "Emma Watson", 1, 1399.00, 100.0, LocalDateTime.of(2025, 8, 1, 0, 0), null, "ACTIVE");
        createSale(p7, s1, t6, "Robert Downey", 2, 450.00, 40.0, LocalDateTime.of(2025, 10, 1, 0, 0), null, "ACTIVE");
        createSale(p5, s3, t7, "Sophia Chen", 5, 89.50, 15.0, LocalDateTime.of(2025, 11, 15, 0, 0), null, "ACTIVE");
        createSale(p8, s2, t8, "Apex Adventure Club", 3, 650.00, 75.0, LocalDateTime.of(2025, 12, 1, 0, 0), null, "ACTIVE");
        createSale(p1, s4, t9, "Innovate Tech Ltd", 2, 1499.99, 50.0, LocalDateTime.of(2026, 1, 1, 0, 0), null, "ACTIVE");
        createSale(p3, s5, t10, "Brian Walker", 1, 279.00, 0.0, LocalDateTime.of(2026, 2, 1, 0, 0), null, "ACTIVE");

        // Seed a Temporal historical revision example
        SaleFact oldRev = createSale(p6, s1, t1, "Apex Media Group (Old Price Contract)", 2, 1200.00, 0.0,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59),
                "REVISED");
        
        createSale(p6, s1, t9, "Apex Media Group (Renewed Contract)", 2, 1399.00, 100.0,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                null,
                "ACTIVE");
    }

    private SaleFact createSale(ProductDimension p, StoreDimension s, TimeDimension t,
                                String customer, int qty, double price, double discount,
                                LocalDateTime validFrom, LocalDateTime validTo, String status) {
        SaleFact sale = new SaleFact();
        sale.setProductDimension(p);
        sale.setStoreDimension(s);
        sale.setTimeDimension(t);
        sale.setCustomerName(customer);
        sale.setQuantity(qty);
        sale.setUnitPrice(price);
        sale.setDiscount(discount);
        sale.setTotalAmount((qty * price) - discount);
        sale.setValidFrom(validFrom);
        sale.setValidTo(validTo);
        sale.setTransactionTime(LocalDateTime.now());
        sale.setStatus(status);
        return saleRepo.save(sale);
    }
}

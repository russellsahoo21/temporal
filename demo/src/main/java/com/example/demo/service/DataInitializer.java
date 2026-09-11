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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            return; // Data already seeded
        }

        // ================= 1. SEED 16 PRODUCTS (5 CATEGORIES) =================
        List<ProductDimension> products = new ArrayList<>();
        // Electronics
        products.add(productRepo.save(new ProductDimension("MacBook Pro M3 16\"", "Electronics", "Apple", 1999.00)));
        products.add(productRepo.save(new ProductDimension("Dell XPS 15 OLED", "Electronics", "Dell", 1399.00)));
        products.add(productRepo.save(new ProductDimension("Sony WH-1000XM5 Headphones", "Electronics", "Sony", 299.00)));
        products.add(productRepo.save(new ProductDimension("iPhone 16 Pro Max", "Electronics", "Apple", 1199.00)));
        products.add(productRepo.save(new ProductDimension("Samsung Galaxy S24 Ultra", "Electronics", "Samsung", 1149.00)));
        
        // Fashion & Apparel
        products.add(productRepo.save(new ProductDimension("Air Jordan 1 High OG", "Fashion & Apparel", "Nike", 190.00)));
        products.add(productRepo.save(new ProductDimension("Classic Denim Trucker Jacket", "Fashion & Apparel", "Levi's", 98.00)));
        products.add(productRepo.save(new ProductDimension("Tech Fleece Windrunner", "Fashion & Apparel", "Nike", 130.00)));
        products.add(productRepo.save(new ProductDimension("Merino Wool Crew Sweater", "Fashion & Apparel", "Uniqlo", 79.50)));

        // Home & Living
        products.add(productRepo.save(new ProductDimension("Smart 4K OLED TV 65\"", "Home & Living", "Samsung", 1499.00)));
        products.add(productRepo.save(new ProductDimension("Barista Touch Espresso Pro", "Home & Living", "Breville", 599.00)));
        products.add(productRepo.save(new ProductDimension("Dyson V15 Cordless Vacuum", "Home & Living", "Dyson", 649.00)));
        products.add(productRepo.save(new ProductDimension("Ergonomic Mesh Task Chair", "Home & Living", "Herman Miller", 895.00)));

        // Sports & Outdoors
        products.add(productRepo.save(new ProductDimension("FuelEx Mountain Trail Bike", "Sports & Outdoors", "Trek", 850.00)));
        products.add(productRepo.save(new ProductDimension("Garmin Fenix 7 Solar Watch", "Sports & Outdoors", "Garmin", 699.00)));
        products.add(productRepo.save(new ProductDimension("Hydro Flask Trail 32oz", "Sports & Outdoors", "Hydro Flask", 45.00)));

        // ================= 2. SEED 8 STORES (4 REGIONS) =================
        List<StoreDimension> stores = new ArrayList<>();
        stores.add(storeRepo.save(new StoreDimension("Manhattan 5th Ave", "New York", "NY", "East")));
        stores.add(storeRepo.save(new StoreDimension("Boston Back Bay", "Boston", "MA", "East")));
        stores.add(storeRepo.save(new StoreDimension("Silicon Valley Hub", "San Francisco", "CA", "West")));
        stores.add(storeRepo.save(new StoreDimension("Seattle Pine Street", "Seattle", "WA", "West")));
        stores.add(storeRepo.save(new StoreDimension("Michigan Ave Gallery", "Chicago", "IL", "North")));
        stores.add(storeRepo.save(new StoreDimension("Minneapolis Center", "Minneapolis", "MN", "North")));
        stores.add(storeRepo.save(new StoreDimension("Austin Tech Ridge", "Austin", "TX", "South")));
        stores.add(storeRepo.save(new StoreDimension("Miami Brickell Ave", "Miami", "FL", "South")));

        // ================= 3. SEED TIME DIMENSIONS (2024 - 2026 across all Quarters/Months) =================
        List<TimeDimension> timeDates = new ArrayList<>();
        // 2024 Quarters
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2024, 2, 14))));  // 2024 Q1 (Feb)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2024, 5, 20))));  // 2024 Q2 (May)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2024, 8, 15))));  // 2024 Q3 (Aug)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2024, 11, 28)))); // 2024 Q4 (Nov)

        // 2025 Quarters (Comprehensive coverage of Q1, Q2, Q3, Q4)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 1, 15))));  // 2025 Q1 (Jan)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 2, 22))));  // 2025 Q1 (Feb)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 3, 10))));  // 2025 Q1 (Mar)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 4, 18))));  // 2025 Q2 (Apr)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 5, 25))));  // 2025 Q2 (May)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 6, 12))));  // 2025 Q2 (Jun)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 7, 20))));  // 2025 Q3 (Jul)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 8, 14))));  // 2025 Q3 (Aug)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 9, 30))));  // 2025 Q3 (Sep)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 10, 15)))); // 2025 Q4 (Oct)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 11, 25)))); // 2025 Q4 (Nov)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2025, 12, 24)))); // 2025 Q4 (Dec)

        // 2026 Quarters
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2026, 1, 18))));  // 2026 Q1 (Jan)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2026, 2, 10))));  // 2026 Q1 (Feb)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2026, 3, 22))));  // 2026 Q1 (Mar)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2026, 4, 15))));  // 2026 Q2 (Apr)
        timeDates.add(timeRepo.save(new TimeDimension(LocalDate.of(2026, 5, 8))));   // 2026 Q2 (May)

        // ================= 4. REALISTIC CUSTOMER ROSTER =================
        String[] customers = {
            "TechCorp Global", "Apex Media Group", "Innovate Analytics", "Nexus Retail Ltd",
            "Horizon Financial", "Vanguard Logistics", "Quantum Systems", "Starlight Media",
            "David Miller", "Alice Morgan", "Robert Downey", "Sophia Chen",
            "Brian Walker", "Emma Watson", "Marcus Brody", "Elena Rostova",
            "James Wilson", "Chloe Bennett", "Liam O'Connor", "Aria Stark",
            "Global Venture Hub", "Summit Outdoor Club", "Pacific Coast Design", "Metro Studio NYC"
        };

        // ================= 5. SEED 130 REALISTIC SALES TRANSACTIONS =================
        Random rand = new Random(42); // Deterministic seed for reproducible dataset

        // Create dense coverage across products, stores, and times
        for (int i = 0; i < 120; i++) {
            ProductDimension prod = products.get(rand.nextInt(products.size()));
            StoreDimension store = stores.get(rand.nextInt(stores.size()));
            TimeDimension time = timeDates.get(rand.nextInt(timeDates.size()));
            String customer = customers[rand.nextInt(customers.length)];

            int quantity = 1 + rand.nextInt(5); // 1 to 5 units
            double unitPrice = prod.getUnitCost();
            double discount = rand.nextBoolean() ? Math.round((rand.nextDouble() * 50.0) * 100.0) / 100.0 : 0.0;

            LocalDate saleDate = time.getDate();
            LocalDateTime validFrom = saleDate.atTime(8 + rand.nextInt(12), rand.nextInt(60));
            LocalDateTime validTo = null; // Current active records

            createSale(prod, store, time, customer, quantity, unitPrice, discount, validFrom, validTo, "ACTIVE");
        }

        // Add 10 explicit Temporal Revision Examples (Historic contracts revised over time)
        for (int j = 0; j < 10; j++) {
            ProductDimension prod = products.get(j % products.size());
            StoreDimension store = stores.get(j % stores.size());
            TimeDimension time = timeDates.get(j % timeDates.size());
            String customer = customers[j % customers.length] + " (Enterprise Contract)";

            // Historical Version (Old contract valid in 2024)
            createSale(prod, store, time, customer, 3, prod.getUnitCost() * 0.9, 0.0,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 12, 31, 23, 59),
                    "REVISED");

            // Current Version (Active contract valid in 2025/2026)
            createSale(prod, store, time, customer, 3, prod.getUnitCost(), 50.0,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    null,
                    "ACTIVE");
        }
    }

    private SaleFact createSale(ProductDimension product,
                                StoreDimension store,
                                TimeDimension time,
                                String customer,
                                int quantity,
                                double unitPrice,
                                double discount,
                                LocalDateTime validFrom,
                                LocalDateTime validTo,
                                String status) {
        double totalAmount = (quantity * unitPrice) - discount;
        if (totalAmount < 0) totalAmount = 0.0;
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;

        SaleFact sale = new SaleFact();
        sale.setProductDimension(product);
        sale.setStoreDimension(store);
        sale.setTimeDimension(time);
        sale.setCustomerName(customer);
        sale.setQuantity(quantity);
        sale.setUnitPrice(unitPrice);
        sale.setDiscount(discount);
        sale.setTotalAmount(totalAmount);
        sale.setValidFrom(validFrom);
        sale.setValidTo(validTo);
        sale.setTransactionTime(LocalDateTime.now());
        sale.setStatus(status);

        return saleRepo.save(sale);
    }
}

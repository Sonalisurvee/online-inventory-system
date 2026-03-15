package com.inventory.system;
//package com.inventory; 1st it was like this


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventorySystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(InventorySystemApplication.class, args);
		System.out.println("========================================");
		System.out.println("✅ Inventory System Started!");
		System.out.println("📝 Access at: http://localhost:8081/inventory");
		System.out.println("========================================");
	}
}
package com.anudip.OnlineFoodOrderingDemo;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
//SessionFactory is created by reading the configuration file (hibernate.cfg.xml).
//Classes related to the system like Customer, Restaurant, FoodItem, FoodOrder, and OrderDetail are added to handle data in the database.
        // Create SessionFactory
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml") // load configuration from hibernate.cfg.xml
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Restaurant.class)
                .addAnnotatedClass(FoodItem.class)
                .addAnnotatedClass(FoodOrder.class)
                .addAnnotatedClass(OrderDetail.class)
                .buildSessionFactory();
        
//The program starts a session (a connection to the database) and uses a Scanner to read inputs from the user.
        // Create Session
        try (Session session = factory.openSession();
             Scanner scanner = new Scanner(System.in)) {

            // Start transaction
            Transaction transaction = session.beginTransaction();

            try {
//The system prompts the user for customer details like name, email, address, and phone number.
//After entering this information, it creates a Customer object and saves it to the database.
                // Getting user input for Customer
                System.out.println("Enter Customer Details:");
                System.out.print("Name: ");
                String customerName = scanner.nextLine();
                System.out.print("Email: ");
                String customerEmail = scanner.nextLine();
                System.out.print("Address: ");
                String customerAddress = scanner.nextLine();
                System.out.print("Phone Number: ");
                String customerPhone = scanner.nextLine();

                Customer customer = new Customer();
                customer.setName(customerName);
                customer.setEmail(customerEmail);
                customer.setAddress(customerAddress);
                customer.setPhoneNumber(customerPhone);

                // Save the customer
                session.save(customer);
//A few restaurants are initialized in the initializeRestaurants() method. Each restaurant has a name, location, and menu.
//Menu items (food) for each restaurant are created using initializeMenuItems(). Every food item has a name, price, category (e.g., main course or bread), and is linked to a restaurant.

                // Initialize restaurants and menu items
                List<Restaurant> restaurants = initializeRestaurants(session);
                List<FoodItem> foodItems = initializeMenuItems(session, restaurants);
              

                List<OrderDetail> orderDetails = new ArrayList<>(); //This line create a list
                double totalAmount = 0;

                // Allow user to select restaurant and order multiple times
                String moreOrders = "yes";
//This variable moreOrders is initialized with the value "yes". It acts as a control variable for the while loop, allowing the user to order from restaurants multiple times until they decide to stop.

//The while loop checks if moreOrders is "yes", and if so, the loop will continue executing, allowing the user to select another restaurant or place more orders. 
//The method equalsIgnoreCase is used to compare the string regardless of case (i.e., it will accept both "yes" and "YES").
                	while (moreOrders.equalsIgnoreCase("yes")) {
                    // Display available restaurants
/*It prints the list of available restaurants for the user to choose from.
 The program uses a for loop to iterate over the restaurants list, which contains all the restaurants previously initialized.
Each restaurant’s details are printed to the console using System.out.printf:Index (i + 1): The restaurant number in the list, starting from 1.
Restaurant Name (rest.getName()): The name of the restaurant.Restaurant Location (rest.getLocation()): The location of the restaurant.
Restaurant Menu (rest.getMenu()): The menu of the restaurant, which was defined earlier in the initializeRestaurants() method.*/
                    System.out.println("\nAvailable Restaurants:");
                    for (int i = 0; i < restaurants.size(); i++) {
                        Restaurant rest = restaurants.get(i);
                        System.out.printf("%d. %s - %s . %s\n", i + 1, rest.getName(), rest.getLocation(),rest.getMenu());
                        System.out.println("==================\n");
                    }
/*The program prompts the user to select a restaurant by entering its corresponding number.
The method getIntInput(scanner) reads the input from the user and ensures that it's a valid integer (handled by the custom method getIntInput).
The user's selection is stored in the variable restaurantSelection.*/
                    System.out.println("Select a restaurant by number: ");
                    int restaurantSelection = getIntInput(scanner);
/*This if statement checks whether the user's selection is a valid number corresponding to one of the available restaurants.
If the user selects a number that is either less than 1 or greater than the total number of restaurants (restaurants.size()), the program prints an error message: "Invalid restaurant selection!"
The continue statement makes the loop jump to the next iteration, allowing the user to select a valid restaurant again without terminating the program.*/
                    if (restaurantSelection < 1 || restaurantSelection > restaurants.size()) {
                        System.out.println("Invalid restaurant selection!");
                        continue;
                    }
/*This line retrieves the restaurant selected by the user.
The user's input (restaurantSelection) is used to get the restaurant from the restaurants list. Since lists in Java are zero-indexed, 
the program subtracts 1 from the user's selection to get the correct restaurant (e.g., if the user selects "1", the program retrieves restaurants.get(0)).
The selected restaurant is stored in the variable selectedRestaurant.*/
                    Restaurant selectedRestaurant = restaurants.get(restaurantSelection - 1);
                    
/*The generateMenuCard method is called to display the menu of the selected restaurant. 
This method shows the list of food items available at the selected restaurant with item numbers, names, prices, and categories.*/
                    // Generate and display the menu card for the selected restaurant
                    generateMenuCard(selectedRestaurant, foodItems);
/*
This variable controls the inner while loop that allows the user to keep ordering more items from the same restaurant. 
It’s set to true, so the loop continues until the user decides they don't want to order more from this restaurant.*/
                    boolean orderAgain = true;

//The inner while loop starts, allowing the user to order multiple food items from the same restaurant until they decide to stop.
                    // Loop for ordering more items from the same restaurant
                    while (orderAgain) {
                        // Allow user to select food items
                        System.out.println("Select food items by number (separated by commas, e.g., 1,2): ");
//The input is read as a single line (e.g., "1,2") and then split into an array (selections) using a comma (",") as the delimiter.
                        String[] selections = scanner.nextLine().split(",");
                        
/*The for loop processes each item selected by the user.
The string input (selection) is trimmed of any leading or trailing spaces and then converted to an integer using Integer.parseInt().
index = Integer.parseInt(selection.trim()) - 1 adjusts for zero-based indexing in Java lists (the user enters numbers starting from 1, so we subtract 1).*/
                        for (String selection : selections) {
                            int index = Integer.parseInt(selection.trim()) - 1;
                            
                            // Filter items based on restaurant
/*This line calls the filterFoodItemsByRestaurant method to get a list of food items specific to the selected restaurant.
  This ensures that the user is only shown and can only select items that belong to that restaurant.*/                            
                            List<FoodItem> filteredItems = filterFoodItemsByRestaurant(selectedRestaurant, foodItems);
                            
/*This checks if the index is within the valid range of available food items.
If the index is valid, the corresponding FoodItem is retrieved from filteredItems and stored in the variable selectedItem.*/                         
                            if (index >= 0 && index < filteredItems.size()) {
                                FoodItem selectedItem = filteredItems.get(index);
/*The program asks the user to enter the quantity for the selected food item.
It uses the getIntInput(scanner) method to ensure the input is a valid integer.*/                                
                                System.out.print("Quantity for " + selectedItem.getName() + ": ");
                                int quantity = getIntInput(scanner);
/*A new OrderDetail object is created for each item the user selects.
The selected FoodItem and the quantity are stored in the OrderDetail.
This OrderDetail is added to the orderDetails list, which stores all the items the user orders.*/
                                OrderDetail orderDetail = new OrderDetail();
                                orderDetail.setFoodItem(selectedItem);
                                orderDetail.setQuantity(quantity);
                                orderDetails.add(orderDetail);
//The price of the selected item (selectedItem.getPrice()) is multiplied by the quantity the user entered, and the result is added to the total order amount (totalAmount).                                
                                totalAmount += selectedItem.getPrice() * quantity;
//If the user enters an invalid item number (outside the valid range of the restaurant’s menu), the program prints an error message: "Invalid selection".
                            } else {
                                System.out.println("Invalid selection: " + selection);
                            }
                        }

                        // Ask if the customer wants to order more from the same restaurant
                        System.out.print("Do you want to order more from the same restaurant? (yes/no): ");
                        String orderAgainResponse = scanner.nextLine();
                        orderAgain = orderAgainResponse.equalsIgnoreCase("yes");
                    }

                    // Ask if the customer wants to order from another restaurant
                    System.out.print("Do you want to order from another restaurant? (yes/no): ");
                    moreOrders = scanner.nextLine();
                }

                // Save the order
                FoodOrder foodOrder = new FoodOrder();
                foodOrder.setOrderDate(new java.sql.Date(System.currentTimeMillis())); // Use current date
                foodOrder.setTotalAmount(totalAmount);
                foodOrder.setCustomer(customer); // Associate order with customer
                session.save(foodOrder);

                // Save each order detail
                for (OrderDetail detail : orderDetails) {
                    detail.setOrder(foodOrder);  // Link the order to order details
                    session.save(detail);
                }

                // Commit transaction
                transaction.commit();
                System.out.printf("\nData saved successfully! Thank you for visiting again..!! :) Total Amount: ₹%.2f\n", totalAmount);

            } catch (Exception e) {
                // Rollback transaction in case of an error
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();  // Log exception
            }

        } catch (HibernateException e) {
            e.printStackTrace();  // Handle SessionFactory issues
        } finally {
            factory.close();  // Ensure the factory is closed
        }
    }

    private static int getIntInput(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
        }
        int result = scanner.nextInt();
        scanner.nextLine();  // Consume the leftover newline
        return result;
    }

    // Initialize restaurants
    private static List<Restaurant> initializeRestaurants(Session session) {
        List<Restaurant> restaurants = new ArrayList<>();
        // Populate the restaurant list
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("**Spice of India**");
        restaurant1.setLocation("12 Taj Road");
        restaurant1.setMenu("\n----------Menu----------:\n1.Butter Chicken-₹350 -Main Course\n2.Garlic Naan-₹50 - Bread\n");
        session.save(restaurant1);
        restaurants.add(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("**Curry House**");
        restaurant2.setLocation("(45 Delhi Street)");
        restaurant2.setMenu("\n---------Menu-----------:\n1.Chicken Curry-₹300 -Main Course\n 2.Tandoori Roti-₹30 -Bread\n");
        session.save(restaurant2);
        restaurants.add(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("**Biryani Palace**");
        restaurant3.setLocation("(78 Mumbai Avenue)");
        restaurant3.setMenu("\n---------Menu---------:\n1.Chicken Biryani-₹250 -Main Course\n2.Mutton Biryani-₹350  -Main Course\n");
        session.save(restaurant3);
        restaurants.add(restaurant3);

        Restaurant restaurant4 = new Restaurant();
        restaurant4.setName("**Tandoori Nights**");
        restaurant4.setLocation("(101 Chennai Circle)");
        restaurant4.setMenu("\n---------Menu---------:\n1.Tandoori Chicken-₹400  -Main Course\n2.Paneer Tikka-₹300  -Appetizer\n");
        session.save(restaurant4);
        restaurants.add(restaurant4);

        return restaurants;
    }

    // Initialize menu items
    private static List<FoodItem> initializeMenuItems(Session session, List<Restaurant> restaurants) {
        List<FoodItem> foodItems = new ArrayList<>();

        // Menu for Spice of India
        FoodItem butterChicken = new FoodItem();
        butterChicken.setName("Butter Chicken");
        butterChicken.setPrice(350); // Price in INR
        butterChicken.setCategory("Main Course");
        butterChicken.setRestaurant(restaurants.get(0));
        session.save(butterChicken);
        foodItems.add(butterChicken);

        FoodItem naan = new FoodItem();
        naan.setName("Garlic Naan");
        naan.setPrice(50); // Price in INR
        naan.setCategory("Bread");
        naan.setRestaurant(restaurants.get(0));
        session.save(naan);
        foodItems.add(naan);

        // Menu for Curry House
        FoodItem chickenCurry = new FoodItem();
        chickenCurry.setName("Chicken Curry");
        chickenCurry.setPrice(300); // Price in INR
        chickenCurry.setCategory("Main Course");
        chickenCurry.setRestaurant(restaurants.get(1));
        session.save(chickenCurry);
        foodItems.add(chickenCurry);

        FoodItem roti = new FoodItem();
        roti.setName("Tandoori Roti");
        roti.setPrice(30); // Price in INR
        roti.setCategory("Bread");
        roti.setRestaurant(restaurants.get(1));
        session.save(roti);
        foodItems.add(roti);

        // Menu for Biryani Palace
        FoodItem chickenBiryani = new FoodItem();
        chickenBiryani.setName("Chicken Biryani");
        chickenBiryani.setPrice(250); // Price in INR
        chickenBiryani.setCategory("Main Course");
        chickenBiryani.setRestaurant(restaurants.get(2));
        session.save(chickenBiryani);
        foodItems.add(chickenBiryani);

        FoodItem muttonBiryani = new FoodItem();
        muttonBiryani.setName("Mutton Biryani");
        muttonBiryani.setPrice(350); // Price in INR
        muttonBiryani.setCategory("Main Course");
        muttonBiryani.setRestaurant(restaurants.get(2));
        session.save(muttonBiryani);
        foodItems.add(muttonBiryani);

        // Menu for Tandoori Nights
        FoodItem tandooriChicken = new FoodItem();
        tandooriChicken.setName("Tandoori Chicken");
        tandooriChicken.setPrice(400); // Price in INR
        tandooriChicken.setCategory("Main Course");
        tandooriChicken.setRestaurant(restaurants.get(3));
        session.save(tandooriChicken);
        foodItems.add(tandooriChicken);

        FoodItem paneerTikka = new FoodItem();
        paneerTikka.setName("Paneer Tikka");
        paneerTikka.setPrice(300); // Price in INR
        paneerTikka.setCategory("Appetizer");
        paneerTikka.setRestaurant(restaurants.get(3));
        session.save(paneerTikka);
        foodItems.add(paneerTikka);

        return foodItems;
    }
//This method generates and displays a menu card for a specific restaurant.
    // Display food items for the selected restaurant
    private static void generateMenuCard(Restaurant restaurant, List<FoodItem> foodItems) {
        System.out.println("\n======================");
        System.out.println(" Menu Card for " + restaurant.getName() + " ");
        System.out.println("======================");
       
        int itemIndex = 1;
/*For each FoodItem in the foodItems list, it checks if the item belongs to the selected restaurant (item.getRestaurant().equals(restaurant)).
If the food item belongs to the restaurant, it is added to the filteredItems list.
Return the Filtered List:*/
        for (FoodItem item : foodItems) {
            if (item.getRestaurant().equals(restaurant)) {
                System.out.printf("%d. %s - ₹%.2f (%s)\n", itemIndex++, item.getName(), item.getPrice(), item.getCategory());
            }
        }
        System.out.println("======================");
    }

    // Filter food items based on selected restaurant
    private static List<FoodItem> filterFoodItemsByRestaurant(Restaurant restaurant, List<FoodItem> foodItems) {
        List<FoodItem> filteredItems = new ArrayList<>();
        for (FoodItem item : foodItems) {
            if (item.getRestaurant().equals(restaurant)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }
}

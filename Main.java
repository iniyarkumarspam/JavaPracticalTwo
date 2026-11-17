/*
Program: Java Practical
Programmer: Iniya Rathinakumar and Alex Kaulfuss
Date: November 15th, 2026

Purpose:
Daily Grind Coffee is a coffee shop ordering and delivery program. Customers can
view the menu, view daily featured items, place an order, provide a delivery
address, and pay using a starting wallet balance. The program tracks inventory,
loyalty points, completed orders, sales, and estimated business results.

Academic Integrity Pledge:
I have only used resources indicated that are allowable.
The work submitted is my/our, and only my/our work.
Signature: Iniya Rathinakumar
Signature: Alex Kaulfuss
Date: Nov 14th

AI assistance was used to help draft and explain portions of this program.
*/

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * Runs the Daily Grind Coffee ordering and delivery program.
 * <p>This is the only public class in this file. The other classes are
 * package-private so that the entire project can be kept in one Main.java file.</p>
 */
public class Main {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final String STATE_FILE = "shop_state.properties";
    private static final String STATISTICS_FILE = "statistics.txt";

    /**
     * Starts the program, loads saved information, and displays the main menu.
     * @param args command-line arguments; not used
     */
    public static void main(String[] args) {
        MenuItem[] menu = createMenu();
        String[] featuredMenu = {
            "Iced Latte",
            "Blueberry Muffin",
            "Cold Brew",
            "Chocolate Croissant",
            "Caramel Macchiato"
        };

        int[] startingStock = {10, 10, 10, 10, 10, 10, 10};
        InventoryManager inventory = new InventoryManager(startingStock);
        Customer customer = new Customer("Customer", 100.00);
        ShopStatistics statistics = new ShopStatistics();

        loadProgramState(customer, inventory, statistics);

        boolean running = true;

        System.out.println("DAILY GRIND COFFEE");
        System.out.println("Welcome to Daily Grind Coffee!");

        while (running) {
            System.out.println("\n MAIN MENU");
            System.out.println("1. View Menu");
            System.out.println("2. View Daily Featured Items");
            System.out.println("3. Place an Order");
            System.out.println("4. View Account and Statistics");
            System.out.println("5. Save and Exit");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    displayMenu(menu, inventory);
                    break;
                case 2:
                    displayFeaturedMenu(featuredMenu);
                    break;
                case 3:
                    placeOrder(menu, featuredMenu, inventory, customer, statistics);
                    break;
                case 4:
                    displayAccount(customer);
                    statistics.displayStatistics();
                    break;
                case 5:
                    saveProgramState(customer, inventory, statistics);
                    writeStatisticsFile(statistics);
                    System.out.println("Your information has been saved.");
                    System.out.println("Thank you for visiting Daily Grind Coffee!");
                    running = false;
                    break;
                default:
                    System.out.println("Please select a number between 1 and 5.");
            }
        }

        INPUT.close();
    }

    /**
     * Creates the menu items sold by the coffee shop.
     * @return an array containing the menu items
     */
    private static MenuItem[] createMenu() {
        return new MenuItem[] {
            new DrinkItem("Iced Latte", 4.50, "Medium", 2),
            new DrinkItem("Caramel Macchiato", 5.00, "Medium", 2),
            new DrinkItem("Cold Brew", 4.00, "Large", 0),
            new DrinkItem("Hot Chocolate", 3.50, "Medium", 0),
            new FoodItem("Blueberry Muffin", 3.00, false),
            new FoodItem("Chocolate Croissant", 3.50, true),
            new FoodItem("Bagel", 2.50, true)
        };
    }

    /**
     * Prints each menu item and its available stock.
     * @param menu the menu item array
     * @param inventory the inventory manager
     */
    private static void displayMenu(MenuItem[] menu, InventoryManager inventory) {
        System.out.println("\nCOFFEE SHOP MENU");
        for (int i = 0; i < menu.length; i++) {
            System.out.println((i + 1) + ". " + menu[i].displayInfo()
                    + " | In stock: " + inventory.getStock(i));
        }
    }

    /**
     * Prints the daily featured items in their matching order.
     * @param featuredMenu the featured item names
     */
    private static void displayFeaturedMenu(String[] featuredMenu) {
        System.out.println("\nDAILY FEATURED MENU");
        for (int i = 0; i < featuredMenu.length; i++) {
            System.out.println((i + 1) + ". " + featuredMenu[i]);
        }
    }

    /**
     * Guides the customer through item selection, checkout, and delivery.
     * @param menu the menu item array
     * @param featuredMenu the daily featured item names
     * @param inventory the inventory manager
     * @param customer the current customer
     * @param statistics the shop statistics
     */
    private static void placeOrder(MenuItem[] menu, String[] featuredMenu,
            InventoryManager inventory, Customer customer,
            ShopStatistics statistics) {

        DeliveryOrder order = new DeliveryOrder();

        System.out.println("\nPLACE YOUR ORDER");
        for (int i = 0; i < menu.length; i++) {
            System.out.println((i + 11) + ". " + menu[i].displayInfo()
                    + " | In stock: " + inventory.getStock(i));
        }
        System.out.println("0. Finish Order");

        boolean choosingItems = true;
        while (choosingItems) {
            int itemChoice = readInt("Select an item to add (0 to finish): ");

            if (itemChoice == 0) {
                choosingItems = false;
            } else if (itemChoice >= 11 && itemChoice <= menu.length + 10) {
                int index = itemChoice - 11;
                if (inventory.getStock(index) > 0) {
                    order.addItem(menu[index]);
                    System.out.println(menu[index].getName() + " added to your order.");
                } else {
                    System.out.println("Sorry, that item is out of stock.");
                }
            } else {
                System.out.println("Invalid selection. Choose 0 or 11 through "
                        + (menu.length + 10) + ".");
            }
        }

        if (order.getItems().isEmpty()) {
            System.out.println("Your order is empty.");
            return;
        }

        System.out.print("Enter your delivery address: ");
        String address = INPUT.nextLine().trim();
        while (address.isEmpty()) {
            System.out.print("Address cannot be blank. Enter your delivery address: ");
            address = INPUT.nextLine().trim();
        }
        order.setDeliveryAddress(address);

        System.out.println("\nYOUR ORDER");
        order.displayOrder();
        System.out.printf("Delivery fee: $%.2f%n", order.getDeliveryFee());
        System.out.printf("Order total: $%.2f%n", order.calculateTotal());
        System.out.printf("Available wallet balance: $%.2f%n",
                customer.getWalletBalance());

        System.out.println("\n1. Checkout");
        System.out.println("2. Cancel Order");
        int checkoutChoice = readInt("Enter your choice: ");

        if (checkoutChoice == 2) {
            System.out.println("Your order has been cancelled.");
            return;
        }
        if (checkoutChoice != 1) {
            System.out.println("Invalid checkout choice. Your order was not placed.");
            return;
        }

        try {
            int[] quantities = countOrderQuantities(order, menu);
            inventory.checkAvailability(quantities);

            double total = order.calculateTotal();
            if (customer.getWalletBalance() < total) {
                throw new InsufficientResourceException(
                        "Insufficient wallet balance. Your order was not charged.");
            }

            String[] orderedNames = order.createOrderedNameArray();
            int matches = countPositionMatches(orderedNames, featuredMenu);
            double multiplier = getMultiplier(matches);
            int pointsEarned = (int) Math.round(total * multiplier);

            customer.subtractBalance(total);
            customer.addLoyaltyPoints(pointsEarned);
            inventory.removeItems(quantities);
            statistics.recordOrder(total, pointsEarned, matches > 0);

            System.out.println("\nRECEIPT");
            order.displayOrder();
            System.out.println("Delivery address: " + order.getDeliveryAddress());
            System.out.printf("Order total: $%.2f%n", total);
            System.out.println("Featured menu matches: " + matches);
            System.out.printf("Loyalty multiplier: %.1fx%n", multiplier);
            System.out.println("Points earned: " + pointsEarned);
            System.out.printf("Remaining wallet balance: $%.2f%n",
                    customer.getWalletBalance());
            System.out.println("Total loyalty points: " + customer.getLoyaltyPoints());

        } catch (InsufficientResourceException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Counts how many of each menu item are in an order.
     * @param order the order to count
     * @param menu the menu array used to identify item positions
     * @return an integer array containing quantities for each menu position
     */
    private static int[] countOrderQuantities(DeliveryOrder order, MenuItem[] menu) {
        int[] quantities = new int[menu.length];
        for (int i = 0; i < order.getItems().size(); i++) {
            MenuItem orderedItem = order.getItems().get(i);
            for (int j = 0; j < menu.length; j++) {
                if (orderedItem.getName().equals(menu[j].getName())) {
                    quantities[j]++;
                    break;
                }
            }
        }
        return quantities;
    }

    /**
     * Compares two arrays at the same index and counts equal item names.
     * @param orderedNames the names selected by the customer in order
     * @param featuredMenu the daily featured item names
     * @return the number of same-position matches
     */
    private static int countPositionMatches(String[] orderedNames,
            String[] featuredMenu) {
        int matches = 0;
        int limit = Math.min(orderedNames.length, featuredMenu.length);

        for (int i = 0; i < limit; i++) {
            if (orderedNames[i].equalsIgnoreCase(featuredMenu[i])) {
                matches++;
            }
        }
        return matches;
    }

    /**
     * Selects the loyalty multiplier based on the number of position matches.
     * @param matches number of matching positions
     * @return the multiplier for the order's loyalty points
     */
    private static double getMultiplier(int matches) {
        if (matches <= 0) {
            return 1.0;
        } else if (matches == 1) {
            return 1.2;
        } else if (matches == 2) {
            return 1.5;
        } else if (matches == 3) {
            return 2.0;
        } else if (matches == 4) {
            return 2.5;
        }
        return 3.0;
    }

    /**
     * Displays the customer's current wallet and loyalty point balance
     * @param customer the customer whose account is displayed
     */
    private static void displayAccount(Customer customer) {
        System.out.println("\nCUSTOMER ACCOUNT");
        System.out.printf("Customer: %s%n", customer.getName());
        System.out.printf("Wallet balance: $%.2f%n", customer.getWalletBalance());
        System.out.println("Loyalty points: " + customer.getLoyaltyPoints());
    }

    /**
     * Reads an integer from the console and recovers from invalid text.
     * @param prompt the message displayed before input
     * @return the integer entered by the user
     */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = INPUT.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    /**
     * Loads saved customer, inventory, and statistics information.
     * @param customer the customer to update
     * @param inventory the inventory to update
     * @param statistics the statistics to update
     */
    private static void loadProgramState(Customer customer,
            InventoryManager inventory, ShopStatistics statistics) {
        Properties data = new Properties();

        try (FileInputStream file = new FileInputStream(STATE_FILE)) {
            data.load(file);
            customer.setWalletBalance(
                    Double.parseDouble(data.getProperty("wallet", "100.0")));
            customer.setLoyaltyPoints(
                    Integer.parseInt(data.getProperty("points", "0")));

            for (int i = 0; i < inventory.getNumberOfItems(); i++) {
                String value = data.getProperty("stock" + i);
                if (value != null) {
                    inventory.setStock(i, Integer.parseInt(value));
                }
            }

            statistics.setCompletedOrders(
                    Integer.parseInt(data.getProperty("orders", "0")));
            statistics.setTotalSales(
                    Double.parseDouble(data.getProperty("sales", "0.0")));
            statistics.setTotalPointsAwarded(
                    Integer.parseInt(data.getProperty("totalPoints", "0")));
            statistics.setFeaturedMatchOrders(
                    Integer.parseInt(data.getProperty("featuredOrders", "0")));

            System.out.println("Saved shop information loaded.");
        } catch (IOException e) {
            System.out.println("No saved shop data found. Starting with default values.");
        } catch (NumberFormatException e) {
            System.out.println("Saved data contains invalid values. Starting with defaults.");
        }
    }

    /**
     * Saves customer, inventory, and statistics information for the next run.
     * @param customer the customer account to save
     * @param inventory the inventory to save
     * @param statistics the statistics to save
     */
    private static void saveProgramState(Customer customer,
            InventoryManager inventory, ShopStatistics statistics) {
        Properties data = new Properties();
        data.setProperty("wallet", String.valueOf(customer.getWalletBalance()));
        data.setProperty("points", String.valueOf(customer.getLoyaltyPoints()));
        data.setProperty("orders", String.valueOf(statistics.getCompletedOrders()));
        data.setProperty("sales", String.valueOf(statistics.getTotalSales()));
        data.setProperty("totalPoints",
                String.valueOf(statistics.getTotalPointsAwarded()));
        data.setProperty("featuredOrders",
                String.valueOf(statistics.getFeaturedMatchOrders()));

        for (int i = 0; i < inventory.getNumberOfItems(); i++) {
            data.setProperty("stock" + i, String.valueOf(inventory.getStock(i)));
        }

        try (FileOutputStream file = new FileOutputStream(STATE_FILE)) {
            data.store(file, "Daily Grind Coffee saved program state");
        } catch (IOException e) {
            System.out.println("Could not save shop data: " + e.getMessage());
        }
    }

    /**
     * Writes the current business statistics to a text file.
     * @param statistics the statistics to write
     */
    private static void writeStatisticsFile(ShopStatistics statistics) {
        try (java.io.PrintWriter writer =
                new java.io.PrintWriter(STATISTICS_FILE)) {
            writer.println("DAILY GRIND COFFEE STATISTICS");
            writer.println("Completed orders: " + statistics.getCompletedOrders());
            writer.printf("Total customer spending (sales): $%.2f%n",
                    statistics.getTotalSales());
            writer.println("Total loyalty points awarded: "
                    + statistics.getTotalPointsAwarded());
            writer.printf("Estimated product costs (60%% of sales): $%.2f%n",
                    statistics.getEstimatedCosts());
            writer.printf("Estimated net profit: $%.2f%n",
                    statistics.getEstimatedProfit());
            writer.printf("Featured-match order rate: %.2f%%%n",
                    statistics.getFeaturedMatchRate());
        } catch (IOException e) {
            System.out.println("Could not write statistics file: " + e.getMessage());
        }
    }
}

/**
 * Represents a general item sold by Daily Grind Coffee.
 * <p>Iniya's superclass. DrinkItem and FoodItem inherit its fields and methods.</p>
 */
class MenuItem {
    private String name;
    private double price;
    private String category;

    /**
     * Creates a menu item.
     * @param name item name
     * @param price item price
     * @param category item category
     */
    public MenuItem(String name, double price, String category) {
        setName(name);
        setPrice(price);
        setCategory(category);
    }

    /** @return the item's name */
    public String getName() {
        return name;
    }

    /** @return the item's price */
    public double getPrice() {
        return price;
    }

    /** @return the item's category */
    public String getCategory() {
        return category;
    }

    /**
     * Changes the item name.
     * @param name new item name
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            this.name = "Unnamed Item";
        }
    }

    /**
     * Changes the item price.
     * @param price new nonnegative price
     */
    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    /**
     * Changes the item category.
     * @param category new category
     */
    public void setCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.category = category.trim();
        }
    }

    /**
     * Creates the text shown for this item.
     * @return formatted menu information
     */
    public String displayInfo() {
        return name + " - $" + String.format("%.2f", price) + " (" + category + ")";
    }
}

/**
 * Represents a drink and extends the general MenuItem class.
 */
class DrinkItem extends MenuItem {
    private String size;
    private int espressoShots;

    /**
     * Creates a drink menu item.
     * @param name drink name
     * @param price drink price
     * @param size drink size
     * @param espressoShots number of espresso shots
     */
    public DrinkItem(String name, double price, String size, int espressoShots) {
        super(name, price, "Drink");
        setSize(size);
        setEspressoShots(espressoShots);
    }

    /** @return drink size */
    public String getSize() {
        return size;
    }

    /** @return number of espresso shots */
    public int getEspressoShots() {
        return espressoShots;
    }

    /**
     * Changes the drink size.
     * @param size new drink size
     */
    public void setSize(String size) {
        if (size != null && !size.trim().isEmpty()) {
            this.size = size.trim();
        } else {
            this.size = "Medium";
        }
    }

    /**
     * Changes the number of espresso shots.
     * @param espressoShots new number of shots
     */
    public void setEspressoShots(int espressoShots) {
        if (espressoShots >= 0) {
            this.espressoShots = espressoShots;
        }
    }

    /**
     * Overrides the general menu display to include drink details.
     * @return formatted drink information
     */
    @Override
    public String displayInfo() {
        return getName() + " (" + size + ", " + espressoShots
                + " espresso shots) - $" + String.format("%.2f", getPrice());
    }
}

/**
 * Represents a food item and extends the general MenuItem class.
 */
class FoodItem extends MenuItem {
    private boolean warmed;

    /**
     * Creates a food menu item.
     * @param name food name
     * @param price food price
     * @param warmed whether the food is served warmed
     */
    public FoodItem(String name, double price, boolean warmed) {
        super(name, price, "Food");
        this.warmed = warmed;
    }

    /** @return true if the food is warmed */
    public boolean isWarmed() {
        return warmed;
    }

    /**
     * Changes whether the food is warmed.
     * @param warmed true if the food should be warmed
     */
    public void setWarmed(boolean warmed) {
        this.warmed = warmed;
    }

    /**
     * Overrides the general menu display to include food details.
     * @return formatted food information
     */
    @Override
    public String displayInfo() {
        String warming = warmed ? "Warmed" : "Not warmed";
        return getName() + " (" + warming + ") - $"
                + String.format("%.2f", getPrice());
    }
}

/**
 * Represents a customer's account, wallet balance, and loyalty points.
 */
class Customer {
    private String name;
    private double walletBalance;
    private int loyaltyPoints;

    /**
     * Creates a customer account.
     * @param name customer name
     * @param walletBalance starting wallet balance
     */
    public Customer(String name, double walletBalance) {
        setName(name);
        setWalletBalance(walletBalance);
        loyaltyPoints = 0;
    }

    /** @return customer name */
    public String getName() {
        return name;
    }

    /** @return current wallet balance */
    public double getWalletBalance() {
        return walletBalance;
    }

    /** @return current loyalty points */
    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    /**
     * Changes the customer name.
     * @param name new customer name
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            this.name = "Customer";
        }
    }

    /**
     * Changes the wallet balance.
     * @param walletBalance new nonnegative wallet balance
     */
    public void setWalletBalance(double walletBalance) {
        if (walletBalance >= 0) {
            this.walletBalance = walletBalance;
        }
    }

    /**
     * Changes the loyalty point balance.
     * @param loyaltyPoints new nonnegative point balance
     */
    public void setLoyaltyPoints(int loyaltyPoints) {
        if (loyaltyPoints >= 0) {
            this.loyaltyPoints = loyaltyPoints;
        }
    }

    /**
     * Adds earned points to the customer's account.
     * @param points number of points earned
     */
    public void addLoyaltyPoints(int points) {
        if (points > 0) {
            loyaltyPoints += points;
        }
    }

    /**
     * Subtracts a purchase from the wallet if enough funds are available.
     * @param amount purchase amount
     * @throws InsufficientResourceException if the wallet does not have enough funds
     */
    public void subtractBalance(double amount)
            throws InsufficientResourceException {
        if (amount < 0 || amount > walletBalance) {
            throw new InsufficientResourceException("Insufficient wallet balance.");
        }
        walletBalance -= amount;
    }
}

/**
 * Stores menu item objects in an ArrayList and calculates an order subtotal.
 * <p>Alex's superclass. DeliveryOrder extends this class.</p>
 */
class CustomerOrder {
    private ArrayList<MenuItem> items;

    /**
     * Creates an empty customer order.
     */
    public CustomerOrder() {
        items = new ArrayList<MenuItem>();
    }

    /**
     * Adds a menu item to the order.
     *
     * @param item menu item to add
     */
    public void addItem(MenuItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    /** @return the list of ordered menu item objects */
    public ArrayList<MenuItem> getItems() {
        return items;
    }

    /**
     * Calculates the subtotal before delivery.
     * @return subtotal of all ordered items
     */
    public double calculateTotal() {
        double total = 0;
        for (MenuItem item : items) {
            total += item.getPrice();
        }
        return total;
    }

    /**
     * Copies ordered item names into an array for position matching.
     * @return an array of ordered item names
     */
    public String[] createOrderedNameArray() {
        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            names[i] = items.get(i).getName();
        }
        return names;
    }

    /**
     * Displays all items and the subtotal.
     */
    public void displayOrder() {
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).displayInfo());
        }
        System.out.printf("Subtotal: $%.2f%n", calculateTotal());
    }
}

/**
 * Represents an order that is delivered and includes a delivery fee.
 */
class DeliveryOrder extends CustomerOrder {
    private String deliveryAddress;
    private double deliveryFee;

    /**
     * Creates a delivery order with a standard delivery fee.
     */
    public DeliveryOrder() {
        super();
        deliveryAddress = "";
        deliveryFee = 2.00;
    }

    /** @return delivery address */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /** @return delivery fee */
    public double getDeliveryFee() {
        return deliveryFee;
    }

    /**
     * Changes the delivery address.
     * @param deliveryAddress new delivery address
     */
    public void setDeliveryAddress(String deliveryAddress) {
        if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
            this.deliveryAddress = deliveryAddress.trim();
        }
    }

    /**
     * Changes the delivery fee.
     * @param deliveryFee new nonnegative delivery fee
     */
    public void setDeliveryFee(double deliveryFee) {
        if (deliveryFee >= 0) {
            this.deliveryFee = deliveryFee;
        }
    }

    /**
     * Overrides the superclass total to include the delivery fee.
     * @return order subtotal plus delivery fee
     */
    @Override
    public double calculateTotal() {
        return super.calculateTotal() + deliveryFee;
    }
}

/**
 * Tracks available quantities for each menu item using a position-based array.
 */
class InventoryManager {
    private int[] stock;

    /**
     * Creates inventory with the supplied starting quantities.
     * @param startingStock initial quantity for each menu position
     */
    public InventoryManager(int[] startingStock) {
        stock = startingStock.clone();
    }

    /**
     * Gets the stock at a menu position.
     * @param index menu array position
     * @return available quantity, or zero for an invalid index
     */
    public int getStock(int index) {
        if (index >= 0 && index < stock.length) {
            return stock[index];
        }
        return 0;
    }

    /**
     * Changes stock at a menu position.
     * @param index menu array position
     * @param quantity new nonnegative quantity
     */
    public void setStock(int index, int quantity) {
        if (index >= 0 && index < stock.length && quantity >= 0) {
            stock[index] = quantity;
        }
    }

    /** @return number of menu positions tracked */
    public int getNumberOfItems() {
        return stock.length;
    }

    /**
     * Checks whether enough stock exists for all requested quantities.
     * @param quantities requested quantity at each menu position
     * @throws InsufficientResourceException if any item has insufficient stock
     */
    public void checkAvailability(int[] quantities)
            throws InsufficientResourceException {
        for (int i = 0; i < quantities.length && i < stock.length; i++) {
            if (quantities[i] > stock[i]) {
                throw new InsufficientResourceException(
                        "Not enough stock for menu item number " + (i + 1) + ".");
            }
        }
    }

    /**
     * Removes purchased quantities from inventory.
     * @param quantities purchased quantity at each menu position
     */
    public void removeItems(int[] quantities) {
        for (int i = 0; i < quantities.length && i < stock.length; i++) {
            stock[i] -= quantities[i];
        }
    }
}

/**
 * Tracks sales, completed orders, rewards, and estimated business results.
 */
class ShopStatistics {
    private int completedOrders;
    private double totalSales;
    private int totalPointsAwarded;
    private int featuredMatchOrders;

    /**
     * Creates statistics with all totals set to zero.
     */
    public ShopStatistics() {
        completedOrders = 0;
        totalSales = 0;
        totalPointsAwarded = 0;
        featuredMatchOrders = 0;
    }

    /** @return number of completed orders */
    public int getCompletedOrders() {
        return completedOrders;
    }

    /** @return total customer spending */
    public double getTotalSales() {
        return totalSales;
    }

    /** @return total loyalty points awarded */
    public int getTotalPointsAwarded() {
        return totalPointsAwarded;
    }

    /** @return number of orders with at least one featured match */
    public int getFeaturedMatchOrders() {
        return featuredMatchOrders;
    }

    /**
     * Restores the completed order count from saved data.
     * @param value saved order count
     */
    public void setCompletedOrders(int value) {
        if (value >= 0) {
            completedOrders = value;
        }
    }

    /**
     * Restores total sales from saved data.
     * @param value saved sales total
     */
    public void setTotalSales(double value) {
        if (value >= 0) {
            totalSales = value;
        }
    }

    /**
     * Restores total points awarded from saved data.
     * @param value saved points total
     */
    public void setTotalPointsAwarded(int value) {
        if (value >= 0) {
            totalPointsAwarded = value;
        }
    }

    /**
     * Restores the number of orders with featured matches.
     * @param value saved featured-match order count
     */
    public void setFeaturedMatchOrders(int value) {
        if (value >= 0) {
            featuredMatchOrders = value;
        }
    }

    /**
     * Records one completed order.
     * @param total order amount paid
     * @param points loyalty points awarded
     * @param hadFeaturedMatch whether the order had a featured match
     */
    public void recordOrder(double total, int points, boolean hadFeaturedMatch) {
        completedOrders++;
        totalSales += total;
        totalPointsAwarded += points;
        if (hadFeaturedMatch) {
            featuredMatchOrders++;
        }
    }

    /**
     * Estimates product costs as 60 percent of sales for this school project.
     * @return estimated product costs
     */
    public double getEstimatedCosts() {
        return totalSales * 0.60;
    }

    /**
     * Calculates estimated net profit as sales minus estimated product costs.
     * @return estimated net profit
     */
    public double getEstimatedProfit() {
        return totalSales - getEstimatedCosts();
    }

    /**
     * Calculates the percentage of orders with at least one featured match.
     * @return featured-match rate as a percentage
     */
    public double getFeaturedMatchRate() {
        if (completedOrders == 0) {
            return 0;
        }
        return (double) featuredMatchOrders / completedOrders * 100.0;
    }

    /**
     * Displays the current business statistics.
     */
    public void displayStatistics() {
        System.out.println("\n----- SHOP STATISTICS -----");
        System.out.println("Completed orders: " + completedOrders);
        System.out.printf("Total customer spending: $%.2f%n", totalSales);
        System.out.println("Total loyalty points awarded: " + totalPointsAwarded);
        System.out.printf("Estimated net profit: $%.2f%n", getEstimatedProfit());
        System.out.printf("Featured-match order rate: %.2f%%%n",
                getFeaturedMatchRate());
    }
}

/**
 * Signals that an order cannot be completed because a required resource is unavailable.
 */
class InsufficientResourceException extends Exception {

    /**
     * Creates an exception with a message describing the unavailable resource.
     * @param message explanation of the problem
     */
    public InsufficientResourceException(String message) {
        super(message);
    }
}

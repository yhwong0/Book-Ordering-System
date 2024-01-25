package onlinebookorderingsystem;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
public class OnlineBookOrderingSystem {

    static int books_num = 0, customers_num = 0, orders_num = 0;

    public static Connection connectToDB(){

        Connection con = null;
        String dbUrl = "jdbc:mysql://localhost:3306/test";
        String dbUsername = "root";
        String dbPassword = "1234";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            System.out.println(con);
        } catch (Exception e) {
            System.out.println(e);
        }

        return con;
    }

    public static void menuInterface(Connection con) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();

        String inputInt = "";
        int input;
        Scanner scan = new Scanner(System.in);
        // Interface printing
        System.out.println("===== Welcome to Book Ordering Management System =====");
        System.out.println("+ System Time: " + dtf.format(currentTime));
        System.out.println("+ Database Records: Books (" + books_num + "), Customers (" + customers_num +"), Orders (" + orders_num +")");
        System.out.println("-------------------------------------------");
        System.out.println("> 1. Database Initialization");
        System.out.println("> 2. Customer Operation");
        System.out.println("> 3. Bookstore Operation");
        System.out.println("> 4. Quit");
        System.out.print(">>> Please Enter Your Query:");
        while(true) {
            inputInt = scan.next();
            try {
                input = Integer.parseInt(inputInt);
                if(input > 0 && input < 5) {
                    break;
                } else {
                    System.out.println("Wrong Query! Please enter again:");
                }
            } catch (Exception e) {
                System.out.println("Wrong Query! Please enter integer 1-4 only:");
            }
        }
        if(input == 1)
            db_init(con);
        else if(input == 2)
            customer_operation(con);
        else if(input == 3) {
            bookstore_operation(con);
        }
        else {
            System.out.println("Thanks for using our System, Goodbye.");
            System.exit(1);
        }
    }

    public static void create_tables(Connection con) throws Exception {
        System.out.println("Creating Tables...");
        Statement stmt = con.createStatement();

        String sql = "CREATE TABLE Customers (cid VARCHAR(8) NOT NULL,";
        sql += "name VARCHAR(25) NOT NULL, address VARCHAR(100) NOT NULL, PRIMARY KEY(cid))";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Orders (oid VARCHAR(8) NOT NULL, isbn VARCHAR(15) REFERENCES Books(isbn), order_date DATE NOT NULL,";
        sql += "shipping_status VARCHAR(10) NOT NULL, PRIMARY KEY(oid, isbn))";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Books (isbn VARCHAR(15) NOT NULL, title VARCHAR(50) NOT NULL, price INTEGER NOT NULL,";
        sql += "inventory_quantity INTEGER NOT NULL, PRIMARY KEY(isbn))";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Author (isbn VARCHAR(15) NOT NULL, name VARCHAR(25) NOT NULL,";
        sql += "FOREIGN KEY (isbn) REFERENCES Books(isbn), PRIMARY KEY(isbn, name))";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Make (isbn VARCHAR(15) NOT NULL, oid VARCHAR(8) NOT NULL, cid VARCHAR(8) NOT NULL,";
        sql += "FOREIGN KEY (isbn) REFERENCES Books(isbn), FOREIGN KEY (oid) REFERENCES Orders(oid), FOREIGN KEY (cid) REFERENCES Customers(cid),";
        sql += "PRIMARY KEY(isbn, oid))";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE Contain (isbn VARCHAR(15) NOT NULL, oid VARCHAR(8) NOT NULL, order_quantity INTEGER NOT NULL,";
        sql += "FOREIGN KEY (isbn) REFERENCES Books(isbn), FOREIGN KEY (oid) REFERENCES Orders(oid),";
        sql += "PRIMARY KEY(isbn, oid))";
        stmt.executeUpdate(sql);

        System.out.println("Tables Created.");
    }

    public static void drop_tables(Connection con) throws Exception {
        books_num = 0;
        orders_num = 0;
        customers_num = 0;
        Statement stmt = con.createStatement();
        System.out.println("Clearing Database...");
        stmt.executeUpdate("DROP TABLE IF EXISTS Author");
        stmt.executeUpdate("DROP TABLE IF EXISTS Make");
        stmt.executeUpdate("DROP TABLE IF EXISTS Contain");
        stmt.executeUpdate("DROP TABLE IF EXISTS Customers");
        stmt.executeUpdate("DROP TABLE IF EXISTS Orders");
        stmt.executeUpdate("DROP TABLE IF EXISTS Books");
        System.out.println("Database is now cleared.");
        stmt.close();
    }

    public static void readFiles(Connection con) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter the Source File Path:"); //Sample directory: C:/Program Files/Database
        String path = scan.nextLine();
        
        try {
            loadCustomer(con, path);
            loadBook(con, path);
            loadOrder(con, path);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please try again.");
            readFiles(con);
        }
    }

    public static void loadCustomer(Connection con, String path) throws Exception {
        File file = new File(path + "/customers.txt"); //cid, name, address
        Scanner scan = new Scanner(file);
        PreparedStatement stmt = con.prepareStatement("INSERT INTO Customers (cid, name, address) VALUES (?, ?, ?)");

        while (scan.hasNextLine()) {
            customers_num++;
            String line = scan.nextLine();
            String[] fields = line.split("\t");
            stmt.setString(1, fields[0]);
            stmt.setString(2, fields[1]);
            stmt.setString(3, fields[2]);
            stmt.executeUpdate();
        } System.out.println("Customer Data Loaded successfully!");
        stmt.close();
        scan.close();
    }

    public static void loadOrder(Connection con, String path) throws Exception {
        File file = new File(path + "/orders.txt"); //oid, cid, order_date, isbn, order_quantity, shipping status
        Scanner scan = new Scanner(file);
        PreparedStatement stmt = con.prepareStatement("INSERT INTO Orders (oid, isbn, order_date, shipping_status) VALUES (?, ?, ?, ?)");
        PreparedStatement stmt1 = con.prepareStatement("INSERT INTO Make (isbn, oid, cid) VALUES (?, ?, ?)");
        PreparedStatement stmt2 = con.prepareStatement("INSERT INTO Contain (isbn, oid, order_quantity) VALUES (?, ?, ?)");

        while (scan.hasNextLine()) {
            orders_num++;
            String line = scan.nextLine();
            String[] fields = line.split("\t");

            stmt.setString(1, fields[0]);
            stmt.setString(2, fields[3]);
            stmt.setString(3, fields[2]);
            stmt.setString(4, fields[5]);
            stmt.executeUpdate();

            stmt1.setString(1, fields[3]);
            stmt1.setString(2, fields[0]);
            stmt1.setString(3, fields[1]);
            stmt1.executeUpdate();

            stmt2.setString(1, fields[3]);
            stmt2.setString(2, fields[0]);
            stmt2.setString(3, fields[4]);
            stmt2.executeUpdate();

        } System.out.println("Order Data Loaded successfully!");
        stmt.close();
        stmt1.close();
        stmt2.close();
        scan.close();
    }

    public static void loadBook(Connection con, String path) throws Exception {
        File file = new File(path + "/books.txt"); //isbn, title, author, price, inventory_quantity
        Scanner scan = new Scanner(file);
        PreparedStatement stmt = con.prepareStatement("INSERT INTO Books (isbn, title, price, inventory_quantity) VALUES (?, ?, ?, ?)");
        PreparedStatement stmt1 = con.prepareStatement("INSERT INTO Author (isbn, name) VALUES (?, ?)");

        while (scan.hasNextLine()) {
            books_num++;
            String line = scan.nextLine();
            String[] fields = line.split("\t");
            stmt.setString(1, fields[0]);
            stmt.setString(2, fields[1]);
            stmt.setString(3, fields[3]);
            stmt.setString(4, fields[4]);
            stmt.executeUpdate();

            stmt1.setString(1, fields[0]);
            stmt1.setString(2, fields[2]);
            stmt1.executeUpdate();
        } System.out.println("Book Data Loaded successfully!");
        stmt.close();
        stmt1.close();
        scan.close();
    }

    public static void db_init(Connection con) {
        System.out.println("Initializing Database...");
        try {
            drop_tables(con);
            create_tables(con);
            readFiles(con);
            menuInterface(con);
        } catch (NullPointerException e) {
            System.out.println(e);
            System.out.println("Can't connect to the database, please fix it in the program.");
            System.exit(1);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void customer_operation(Connection con){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        System.out.println("===== Welcome to Customer Operation =====");
        System.out.println("+ System Time: " + dtf.format(currentTime));
        System.out.println("+ Database Records: Books (" + books_num + "), Customers (" + customers_num +"), Orders (" + orders_num +")");
        System.out.println("-------------------------------------------");
        System.out.println("> 1. Book Search");
        System.out.println("> 2. Place an Order");
        System.out.println("> 3. Check History Orders");
        System.out.println("> 4. Go back to the main menu");
        System.out.print(">>> Please Enter Your Query:");

        Scanner scanner = new Scanner(System.in);
        int input =0;
        try{
            do {
                System.out.println("Please enter [1-4]. ");
                input = scanner.nextInt();
                if (input < 1 || input > 4) {
                    System.out.println("Invalid input");
                }
            } while (input < 1 || input > 4);

            switch (input) {
                case 1:
                    bookSearch(con);
                    break;
                case 2:
                    placeOrder(con);
                    break;
                case 3:
                    checkHistoryOrders(con);
                    break;
                case 4:
                    menuInterface(con);
                    break;
            }
            scanner.close();
        }
        catch(Exception e) {
            System.out.println(e);
            customer_operation(con);
        }
    }

    public static void bookSearch(Connection con){
        
        System.out.println("To query a book by");
        System.out.println("> 1. ISBN");
        System.out.println("> 2. Book Title");
        System.out.println("> 3. Author Name");
        System.out.println("> 4. Go back to last page");

        Scanner scanner = new Scanner(System.in);
        int input=0;
        do {
            System.out.print(">>> Please Enter Your Query:");
            input = scanner.nextInt();
            if (input < 1 || input > 4) {
                System.out.println("Invalid input");
            }
        } while (input < 1 || input > 4);
        String target = "";
        switch (input) {
            case 1:
                System.out.println("Please input ISBN:");
                scanner = new Scanner(System.in);
                target = scanner.next();
                bs("(SELECT * FROM books NATURAL JOIN author) t WHERE isbn = '" + target + "'",con);
                customer_operation(con);
                break;
            case 2:
                System.out.println("Please input Book Title:");
                scanner = new Scanner(System.in);
                target = scanner.nextLine();
                bs("(SELECT * FROM books NATURAL JOIN author) t WHERE title = '" + target + "'",con);
                customer_operation(con);
                break;
            case 3:
                System.out.println("Please input Author Name:");
                scanner = new Scanner(System.in);
                target = scanner.nextLine();
                bs("(SELECT * FROM books NATURAL JOIN author) t WHERE name = '" + target + "'",con);
                customer_operation(con);
                break;
            case 4:
                customer_operation(con);
                break;
        }
    }

    public static void bs(String search, Connection con){
        try {
            Statement statement = con.createStatement();
            int count;
            String sql =  "SELECT COUNT(*) AS count FROM "+search;
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt("count");
                if(count == 0){
                    System.out.println("No results.");
                    return;
                }
            }

            sql = "Select * FROM " + search ;
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                int price = rs.getInt("price");
                int iq = rs.getInt("inventory_quantity");
                String name = rs.getString("name");
                System.out.println("isbn:" +isbn + " title: " + title + " price: " + price + " inventory quantity: " + iq + " author: " + name);
            }
        }catch (SQLException e) {
            System.err.println("Error occured: " + e);
        }
    }

    public static void checkHistoryOrders(Connection con){
        String customerID;
        String CID;
        String shippingStatus = "";
        String isbn = "";
        String oid = "";
        String od = "";
        while(true){
            System.out.println("Please input your Customer ID:");
            Scanner scanner = new Scanner(System.in);
            CID = scanner.next();
            try {
                int count;
                Statement statement = con.createStatement();
                String sql = "SELECT COUNT(*) AS count FROM customers WHERE cid = '" + CID + "'" ;
                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) {
                    count = rs.getInt("count");
                    if(count == 0){
                        System.out.println("CID Not Exist!");
                        break;
                    }
                }
                sql = "Select isbn, oid, cid,order_date,shipping_status FROM (select * from make natural join orders) t where cid = '" + CID + "'";
                rs = statement.executeQuery(sql);

                while(rs.next()){
                    isbn = rs.getString("isbn");
                    oid = rs.getString("oid");
                    od = rs.getString("order_date");
                    shippingStatus = rs.getString("shipping_status");
                    System.out.println("OID: " + oid);
                    System.out.println("ISBN: " + isbn);
                    System.out.println("Order date: " + od);
                    System.out.println("Shipping status: " + shippingStatus);

                }
                customer_operation(con);
            }catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error occured: " + e);
            }
        }
        customer_operation(con);
    }
    
    public static void placeOrder(Connection con){
        String CID ="";
        while(true){
            System.out.println("Please input your Customer ID:");
            Scanner scanner = new Scanner(System.in);
            CID = scanner.next();
            try {
                Statement statement = con.createStatement();
                String sql = "SELECT COUNT(*) AS count FROM customers WHERE cid = '" + CID + "'";
                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) {
                    int count;
                    count = rs.getInt("count");
                    if(count == 0){
                        System.out.println("CID Not Exist!");
                        placeOrder(con);
                        return;
                    }
                }
                System.out.println("Place your order:");
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error occured: " + e);
            }
        }
        try{
            List<String> orderedBooksName= new ArrayList<>();
            List<String> orderedBooksNamecheck= new ArrayList<>();
            List<Integer> orderedBooksQuantity= new ArrayList<Integer>();
            List<Integer> orderedBooksSuccessful= new ArrayList<Integer>();

            Statement statement = con.createStatement();
            int counting=0;
            String sql = "SELECT COUNT(*) AS counting FROM orders";
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()){
                counting=rs.getInt(1);
            }
            int k= counting +10000000;
            int countsuccess=0;
            String orderid = String.format("%d", k);


            System.out.println("Please input the book ISBN and the quantity of the book (q) you would like to order in the format X-XXXX-XXXX-X,q line by line,");
            System.out.println("and input 'finish' at the end:");

            int fin =0;

            while(fin==0){
                Scanner scanner = new Scanner(System.in);
                String order = scanner.nextLine();
                if (order.equals("finish")){
                    fin = 1;
                    int j=0;
                    if(orderedBooksSuccessful.contains(1)){
                        orders_num++;
                        System.out.println("Successcully ordered");
                        System.out.println("Order ID:" + orderid);
                        for(int i=0; i<countsuccess ; i++){
                            while(orderedBooksSuccessful.get(j)==0){
                                j=j+1;
                            }
                            System.out.println("You have ordered " + orderedBooksName.get(j)+ " with quantity " + orderedBooksQuantity.get(j));
                            j=j+1;
                        }
                        customer_operation(con);
                    }
                    else{
                        System.out.println("You have not made the order");
                        customer_operation(con);
                    }

                }
                String bookISBN = order.split(",")[0];
                int quantity = Integer.parseInt(order.split(",")[1]);
                int count =0;
                statement = con.createStatement();
                sql = "SELECT COUNT(*) AS count FROM books WHERE isbn = '" + bookISBN + "'";
                rs = statement.executeQuery(sql);
                if (rs.next()) {
                    count = rs.getInt("count");
                }
                if(count == 0){
                    System.out.println("Book not found.");
                    orderedBooksSuccessful.add(0);
                    orderedBooksName.add("wrong input");
                    orderedBooksQuantity.add(0);

                }
                else{
                    statement = con.createStatement();
                    sql = "SELECT title as book FROM books WHERE isbn= '" + bookISBN + "'"  ;
                    rs = statement.executeQuery(sql);
                    String book ="";
                    if (rs.next()){
                        book = rs.getString(1);
                    }
                    orderedBooksName.add(book);
                    orderedBooksQuantity.add(quantity);


                    if (orderedBooksNamecheck.contains(book)){
                        System.out.println("Book has already been ordered");
                        orderedBooksSuccessful.add(0);
                        continue;
                    }
                   
                    statement = con.createStatement();
                    int biq=0;
                    sql = "SELECT inventory_quantity AS biq FROM books WHERE isbn = '" + bookISBN + "'";
                    rs = statement.executeQuery(sql);
                    if (rs.next()){
                        biq= rs.getInt("biq");
                    }
                    if(biq < quantity){
                        System.out.println("Inventory shortage");
                        orderedBooksSuccessful.add(0);
                        continue;
                       
                    }
                    else{
                        orderedBooksNamecheck.add(book);
                        int newbiq = biq - quantity;
                        sql = "UPDATE books SET inventory_quantity="+ newbiq + " WHERE isbn= '" + bookISBN + "'";
                        statement.executeUpdate(sql);
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime currentTime = LocalDateTime.now();
                        String current_date=dtf.format(currentTime);
                        sql = "INSERT INTO orders (oid, isbn, order_date, shipping_status) VALUES ('" + orderid + "', '" + bookISBN +"', '" + current_date + "', 'ordered')";
                        statement.executeUpdate(sql);
                        sql = "INSERT INTO make (isbn, oid, cid) VALUES ('" + bookISBN + "', '" + orderid +"', '" + CID + "')";
                        statement.executeUpdate(sql);
                        sql = "INSERT INTO contain (isbn, oid, order_quantity) VALUES ('" + bookISBN + "', '" + orderid +"', '" + quantity + "')";
                        statement.executeUpdate(sql);
                    }
                    countsuccess=countsuccess+1;
                    orderedBooksSuccessful.add(1);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occured: " + e);
            System.err.println("Error occured: Incorrect input format ");
        }
        customer_operation(con);
    }
    //include bookstore code below

    public static void bookstore_operation(Connection con){
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        
        // menu printing
        System.out.println("===== Bookstore Operation Menu ===== ");
        System.out.println("+ System Time: " + dtf.format(currentTime));
        System.out.println("+ Database Records: Books (" + books_num + "), Customers (" + customers_num +"), Orders (" + orders_num +")");
        System.out.println("-------------------------------------------");
        System.out.println("> 1. Order Update");
        System.out.println("> 2. Order Query");
        System.out.println("> 3. N Most Popular Books");
        System.out.println("> 4. Go back to the main menu");
        System.out.print(">>> Please Enter Your Query:");
        

        Scanner scan_input=new Scanner(System.in);
        boolean loop_again = false;
        try{
            do{
                String input_index=scan_input.nextLine();

                loop_again = false;
                if(input_index.equals("1")){
                    //order update
                    System.out.println("If you want to update the order, select operation below");
                    System.out.println("-------------------------------------------");
                    System.out.println("> 1. Input Order ID:");
                    System.out.println("> 2. Back to Previous Page");
                    System.out.print(">>> Input index of the operation:");

                    String order_id=scan_input.nextLine();
                    while(!order_id.equals("1") && !order_id.equals("2")){
                        System.out.println("Invalid input, input again:");
                        order_id=scan_input.nextLine();
                    }
                    if(order_id.equals("1")){
                        System.out.println("Input Order ID:");
                        order_id=scan_input.nextLine();
                        Order_update(con,order_id);
                        return;
                    }
                    if(order_id.equals("2")){
                        bookstore_operation(con);
                    }
                    else{
                        System.out.println("Invalid Input!");
                    }

                }
                else if (input_index.equals("2")){
                    Order_query(con);
                }
                else if (input_index.equals("3")){
                    //n most popular books
                    System.out.println("If you want to check out N most popular books, select operation below");
                    System.out.println("-------------------------------------------");
                    System.out.println("> 1. Input N:");
                    System.out.println("> 2. Back to Previous Page");
                    System.out.print(">>> Input index of the operation:");

                    String index=scan_input.nextLine();

                    while(!index.equals("1") && !index.equals("2")){
                        System.out.println("Invalid input, input again:");
                        index=scan_input.nextLine();
                    }

                    if(index.equals("1")) {
                        System.out.println("Input the amount N of most popular books:");
                        int N = scan_input.nextInt();

                        while (N < 0) {
                            System.out.println("Invalid input, input again (N>=0):");
                            N = scan_input.nextInt();
                        }
                        N_most_popular_books(con, N);
                    }
                    else if (index.equals("2")) {
                        bookstore_operation(con);
                    }
                    else{
                        System.out.println("Invalid Input!");
                    }
                }
                else if (input_index.equals("4")){
                    //go back to main menu
                    menuInterface(con);
                }
                else{
                    if(input_index!="" &&input_index!=" "){
                        System.out.println("Invalid Input, Please input again:");}
                    //input_index =  scan_input.nextLine();
                    loop_again = true;
                }

            } while(loop_again == true);


        }
        catch (Exception ex){
            System.err.println("Error occured: " + ex);
        }

        //back to initial state
        bookstore_operation(con);

    }


    public static void Order_update(Connection con, String order_id){
        //assume all orders with same oid have same shipping status

        System.out.println("Order Update, Printing Total amount of order and current shipping status:");

        String sql="";
        String input_str="";

        try{
            Scanner scan_input=new Scanner(System.in);
            sql="SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))";
            Statement stmt0=con.createStatement();
            stmt0.executeUpdate(sql);
            Statement stmt=con.createStatement();
            sql="SELECT COUNT(oid), shipping_status FROM Orders WHERE oid="+order_id;
            ResultSet res = stmt.executeQuery(sql);



            int count=0;
            String shipping_status="";
            if(res.next()){
                count=res.getInt(1);
                shipping_status=res.getString(2);
                if(count>0){
                    System.out.println("| Total order Count | Shipping Status |");
                    System.out.println("| "+count+" | "+shipping_status+" |");
                }
            }
            stmt.close();
            if(count==0){
                System.out.println("Error: No record with order ID "+order_id);
            }
            else{
                System.out.print("Input the updated status of the order:");
                input_str = scan_input.nextLine();
                //System.out.println("input" + input_str);

                //validation
                while((! input_str.equals("shipped") && ! input_str.equals("ordered") && ! input_str.equals("received"))){
                    System.out.println("Invalid input, please input ordered, shipped or received");
                    input_str = scan_input.nextLine();
                }

                Statement stmt_2=con.createStatement();
                String sql_template="UPDATE Orders SET shipping_status =";
                
                while(true) {
                    if(shipping_status.equals("received")){
                    System.out.println("Error: Order with order ID "+order_id+" was already received");
                    //input_str = scan_input.nextLine();
                    break;
                    }
                    else if(shipping_status.equals("shipped")){
                        if(input_str.equals("shipped") || input_str.equals("ordered")){
                            System.out.println("Error: Invalid Operation, cannot input shipping status same as the current one or go back in time");
                            System.out.println("Update failed, please enter again:");
                            input_str = scan_input.nextLine();
                            continue;
                        }
                        else{
                            stmt_2.executeUpdate(sql_template+"'"+input_str+"'"+" WHERE OID ="+order_id);
                            System.out.println("Update successful, "+count+" records with Order ID "+order_id +" has been updated");
                            stmt_2.close();
                            break;
                        }
                    }
                    else if(shipping_status.equals("ordered")){
                        if(input_str.equals("ordered")){
                            System.out.println("Error: Invalid Operation, cannot input shipping status same as the current one");
                            System.out.println("Update failed, please enter again:");
                            input_str = scan_input.nextLine();
                            continue;
                        }
                        else{
                            Statement stmt_3=con.createStatement();
                            stmt_3.executeUpdate(sql_template+"'"+input_str+"'"+" WHERE OID ="+order_id);
                            System.out.println("Update successful, "+count+" records with Order ID "+order_id +" has been updated");
                            stmt_3.close();
                            break;
                        }
                    }
                    else{
                        //impossible unless crahsed
                        System.out.println("Error: System Error");
                        break;
                    }
                }
            }
        }
        catch (Exception ex){
            System.err.println("Error occured: " + ex);
        }

        //back to initial state
        bookstore_operation(con);
    }

    public static void Order_query(Connection con){
        try{
            Scanner scan_input=new Scanner(System.in);
            String sql="set sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';";//query
            Statement stmt1 = con.createStatement();
            stmt1.executeUpdate(sql);
            String input_str="";

            System.out.println("-------------------------------------------");
            System.out.println("If you want to query order, select operation below");
            System.out.println("> 1. Show summary for orders with different shipping status");
            System.out.println("> 2. Show all orders with a specific shipping status");
            System.out.println("> 3. Back to Previous Page");
            System.out.print(">>> Input index of the operation:");
            while(true) {

                input_str = scan_input.nextLine();

                if (input_str.equals("1")) {
                    sql = "select shipping_status as Shipping_Status, SUM(order_quantity) as Total_Order_Quantity ";
                    sql += "FROM ";
                    sql += "(Select * from contain natural JOIN Orders) as joined";
                    sql += " GROUP BY shipping_status";

                    PreparedStatement pdst = con.prepareStatement(sql);
                    //pdst.setString(1,shipping_status);
                    ResultSet res = pdst.executeQuery();
                    System.out.println("Query Result:");
                    System.out.println("| Shipping Status | Order Quantity |");


                    while (res.next()) {
                        String ss = res.getString(1);
                        //assume use string type, need confirm
                        //Date order_date = res.getDate(2);
                        int order_quantity = res.getInt(2);

                        System.out.println("| " + ss + " | " + order_quantity + " | ");
                    }
                    System.out.println("End of Query");
                    break;
                } else if (input_str.equals("2")) {
                    System.out.println("Please Specify Shipping Status");
                    input_str = scan_input.nextLine();

                    while((! input_str.equals("shipped") && ! input_str.equals("ordered") && ! input_str.equals("received"))){
                        System.out.println("Invalid input, please input ordered, shipped or received");
                        input_str=scan_input.nextLine();
                    }

                    sql = "select * ";
                    sql += "FROM ";
                    sql += "(Select * from contain natural JOIN Orders) as joined";
                    sql += " WHERE joined.shipping_status = '"+ input_str+"'";

                    PreparedStatement pdst1 = con.prepareStatement(sql);
                    //pdst.setString(1,shipping_status);
                    ResultSet res1 = pdst1.executeQuery();

                    System.out.println("|   ISBN   |   Oid   |   Order quantity   |   Order date   |   Shipping Status   |");
                    while (res1.next()) {
                        String isbn = res1.getString(1);
                        String oid= res1.getString(2);
                        int order_quantity = res1.getInt(3);
                        String order_date= res1.getString(4);
                        String shipping_status = res1.getString(5);

                        System.out.println("| " + isbn + " | " + oid + " | " + order_quantity + " | " + order_date + " | " + shipping_status + " | ");
                    }
                    System.out.println("End of Query");
                    break;
                }
                else if (input_str.equals("3")) {
                    break;
                }
                else{
                    System.out.println("Invalid input, input again:");

                }
            }
        }
        catch (Exception exc){
            System.err.println("Error: " + exc);
        }

        //back to initial state
        bookstore_operation(con);

    }

    public static void N_most_popular_books(Connection con,int N) { //pending fix of sql
        //out put the top N books in decending number of total order quantity
        Scanner scan_input=new Scanner(System.in);
        System.out.println("printing the information of " + N + " most popular books:");

        String sql = "SELECT SUM(joined.order_quantity) as Total_Order_Quantity, joined.isbn as ISBN, joined.title as Title, joined.inventory_quantity as Inventory_quantity FROM";
        sql = sql + "(SELECT * FROM";
        sql = sql + " (SELECT * FROM Orders NATURAL JOIN contain) Temp";
        sql = sql + " NATURAL JOIN (SELECT isbn ,title,inventory_quantity FROM Books) temp1) joined";
        sql = sql + " GROUP BY isbn ";
        sql = sql + " ORDER BY Total_Order_Quantity DESC;";

        String setting="SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));";
        try {

            Statement stmt1 = con.createStatement();
            stmt1.executeUpdate(setting);

            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery(sql);

            //print result
            int count = 1;

            System.out.println("| Total Order Quantity | ISBN | Title | Inventory Quantity |");
            while (count <= N) {
                if (res.next()) {
                    //get result
                    //output: total order quantity, isbn, title, inventory_quantity
                    int total_order_quantity = res.getInt(1);
                    String isbn = res.getString(2);
                    String title = res.getString(3);
                    int inventory_quantity = res.getInt(4);


                    System.out.println("| " + total_order_quantity + " | " + isbn + " | " + title + " | " + inventory_quantity + " | ");
                    count = count + 1;
                } else {
                    count-=1;
                    System.out.println("There are only " + count + " records in the table.");
                    break;
                }
            }


            stmt.close();
            System.out.println("End of Query");
        } catch (Exception exc) {
            System.err.println("Error: " + exc);
        }
        //back to initial state
        bookstore_operation(con);
    }
    //end bookstore

    public static void main(String[] args) {
        try {
            Connection con = connectToDB();
            menuInterface(con);
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
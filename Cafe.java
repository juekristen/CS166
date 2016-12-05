/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */
/*ideas
if something is not found output a message*/

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.io.*;

//private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql);
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                         \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static String find_type(Cafe esql){
      // Your code goes here.
      // ...
      // ...
      //started not tested home
	String types;
try{
	String query = "SELECT type FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> trys = esql.executeQueryAndReturnResult(query);
        types = (trys.get(0)).get(0);
        
      return types;
}
catch(Exception e){
         System.err.println (e.getMessage());
      return null;
}

   }

   public static void BrowseMenuName(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	//done tested
try{
      String query = "SELECT * FROM Menu WHERE itemName = '";
      System.out.print("\tEnter itemName: ");
      String input = in.readLine();
      query += input;
      query +="'";
 	int rowCount = esql.executeQueryAndPrintResult(query);
         if(rowCount == 0)
	{
		System.out.print("\tItem Name does not exist\n");
	}
}
	catch(Exception e){
         System.err.println (e.getMessage());
      
}
   }//end

   public static void BrowseMenuType(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	//done tested
	try{
	      String query = "SELECT * FROM Menu WHERE type = '";
	      System.out.print("\tEnter item type: ");
	      String input = in.readLine();
	      query += input;
	      query +="'";
	 	int rowCount = esql.executeQueryAndPrintResult(query);
		if(rowCount == 0)
		{
		System.out.print("\tItem Type does not exist\n");
		}
		 //System.out.println ("total row(s): " + rowCount);
	}
		catch(Exception e){
		 System.err.println (e.getMessage());
	      
	}
   }//end

   public static Integer AddOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	//done tested can be extended asking for comments and checking if item name is real
	try{
		System.out.print("\tEnter item name: ");
     		String input = in.readLine();
		String query = "SELECT itemName FROM Menu WHERE itemName ='" + input + "'";
	 	int userNum = esql.executeQuery(query);
		 if (userNum == 0){
			System.out.print("Unreconized Item Name\n");
			return 0;}
		System.out.print("\tComments(optional): ");
     		String comment = in.readLine();
		
		Timestamp date = new Timestamp(System.currentTimeMillis());
		boolean paid = false;
		String stat = "Hasn''t started";
      		query = "SELECT  price FROM MENU WHERE itemName = '" + input + "'";
		List<List<String>> trys = esql.executeQueryAndReturnResult(query);
		String total = (trys.get(0)).get(0);
		query = String.format("INSERT INTO ORDERS ( login, paid, timeStampRecieved, total) 			VALUES ('%s','%s','%s','%s')", authorisedUser,paid , date, total);
		esql.executeUpdate(query);
		query = "SELECT orderid FROM ORDERS WHERE timeStampRecieved = '" + date + "' AND login 			= '" + authorisedUser + "'";
		List<List<String>> id = esql.executeQueryAndReturnResult(query);
		int oid = Integer.parseInt((id.get(0)).get(0));
		query = String.format("INSERT INTO ItemStatus ( orderid, itemName, lastUpdated, status, 		comments)VALUES ('%s','%s','%s','%s','%s')", oid, input ,date,stat,comment);
		esql.executeUpdate(query);
		//query = "SELECT * FROM itemStatus";
		//esql.executeQueryAndPrintResult(query);
		Integer orderid=oid;
      		return orderid;
		
		//query = "SELECT * FROM ORDERS";
		//int rowCount = esql.executeQueryAndPrintResult(query);
	}
		catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
      Integer orderid=0;
      return orderid;
   }//end 

   public static void UpdateOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	//Finished tested can be extended
	try{
		String stat = "Hasn''t Started";
		System.out.print("Do you want to add an item to your order or update existing order (type \"add\" or \"existing\"): ");
		String response = in.readLine();
		if(response.equals("add")){
		System.out.print("\tWhat Orderid do you want to update: ");
		String input = in.readLine();
		String query = "SELECT orderid FROM ORDERS WHERE orderid = '" + input + "' AND 			paid = false AND login = '" + authorisedUser + "'";
	 	int userNum = esql.executeQuery(query);
		 if (userNum == 0){
			System.out.print("Unreconized Orderid or Unauthorized Orderid\n");
			return;}
		System.out.print("Add item\nEnter item name: ");
		String newitem = in.readLine();
		System.out.print("\tComments(optional): ");
     		String comment = in.readLine();
		Timestamp date = new Timestamp(System.currentTimeMillis());
		query = "SELECT * FROM Menu WHERE itemName ='" + newitem + "'";
		int num = esql.executeQuery(query);
		if(num == 0)
		{
			System.out.print("Item Name not reconized");
			return ;
		}	
		query = "SELECT  price FROM MENU WHERE itemName = '" + newitem + "'";
		List<List<String>> trys = esql.executeQueryAndReturnResult(query);
		String total = (trys.get(0)).get(0);
		query = "SELECT  total FROM ORDERS WHERE orderid = '" + input + "'";
		trys = esql.executeQueryAndReturnResult(query);
		Double totalnew = Double.parseDouble((trys.get(0)).get(0)) + Double.parseDouble(total);
		query = "UPDATE ORDERS SET total = " +totalnew + "WHERE orderid = '" + input + "'";
		esql.executeUpdate(query);
		query = "UPDATE ORDERS SET timeStampRecieved = '" + date + "' WHERE orderid = '" + 			input + "'";
		esql.executeUpdate(query);
		query = String.format("INSERT INTO ItemStatus ( orderid, itemName, lastUpdated, status, 		comments)VALUES ('%s','%s','%s','%s','%s')", input, newitem ,date,stat,comment);
		esql.executeUpdate(query);
		}
		else if(response.equals("existing"))
		{
			System.out.print("\tWhat Orderid do you want to update: ");
			String input = in.readLine();
			String query = "SELECT orderid FROM ORDERS WHERE orderid = '" + input + "' AND 				paid = false AND login = '" + authorisedUser + "'";
	 		int userNum = esql.executeQuery(query);
		 if (userNum == 0){
			System.out.print("Unreconized Orderid or Unauthorized Orderid\n");
			return;}
			System.out.print("\tWhat item name do you want to update: ");
			String inputname = in.readLine();
			query = "SELECT itemName FROM ItemStatus WHERE itemName = '" + inputname 				+"' AND orderid = '"+ input + "'";
	 		userNum = esql.executeQuery(query);
		 if (userNum == 0){
			System.out.print("Unreconized item name\n");
			return;}
			System.out.print("Comments: ");
			String comments = in.readLine();
			query = "UPDATE ItemStatus SET comments = '" + comments + "' WHERE orderid = '" 			+ input +"' AND itemName = '" + inputname + "'";
			esql.executeUpdate(query);		
			
			
		}
		//query = "SELECT * FROM ORDERS";
		//int rowCount = esql.executeQueryAndPrintResult(query);
	
	}
	catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
	
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
      try{
        System.out.print("\tWhat Orderid do you want to update: ");
		String input = in.readLine();
		String query = "SELECT orderid FROM ORDERS WHERE orderid = '" + input + "'";
	 	int userNum = esql.executeQuery(query);
		 if (userNum == 0){
			System.out.print("Unreconized Orderid or Unauthorized Orderid\n");
			return;}
        System.out.print("Do you want to change status to paid?: (y or n) ");
        String yesno = in.readLine();
        while(!yesno.equals("y") && !yesno.equals("n"))
        {
            System.out.print("Unrecognized input\n");
             System.out.print("Do you want to change status to paid?: (y or n) ");
            yesno = in.readLine();
        }
        if(yesno.equals("y"))
        {
            query = "UPDATE Orders SET paid = true WHERE orderid = '"+input + "'";
            esql.executeUpdate(query);
        }
        System.out.print("Do you want to update an item status? (y or n) ");
        yesno = in.readLine();
        while(!yesno.equals("y") && !yesno.equals("n"))
        {
            System.out.print("Unrecognized input\n");
             System.out.print("Do you want to update an item status? (y or n) ");
            yesno = in.readLine();
        }
        if(yesno.equals("y"))
        {
            System.out.print("What item do you want to update: ");
            String itemNames = in.readLine();
            query = "SELECT * FROM itemStatus WHERE orderid = '"+ input +"' AND itemName = '" + itemNames + "'";
            int huh = esql.executeQuery(query);
            while(huh == 0 && !itemNames.equals("q"))
            {
                System.out.print("No such item in this order\n");
                System.out.print("What item do you want to update: (q to quit)");
                itemNames = in.readLine();
                query = "SELECT * FROM itemStatus WHERE orderid = '"+ input +"' AND itemName = '" + itemNames + "'";
                 huh = esql.executeQuery(query);
            }
            if(itemNames.equals('q'))
                return;
            Timestamp date = new Timestamp(System.currentTimeMillis());
            System.out.print("What do you want to set the status to? ");
            String stat = in.readLine();
            query = "UPDATE itemStatus SET status = '" + stat + "' WHERE orderid = '" + input + "' AND itemName = '" + itemNames + "'";
            esql.executeUpdate(query);
         }
      }
      catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
          
        
   }//end

   public static void ViewOrderHistory(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	//finished and tested can be extended
	try{
		String query = "SELECT * from ORDERS WHERE login = '"  + authorisedUser + "'  ORDER BY 			timeStampRecieved DESC LIMIT 5";
		esql.executeQueryAndPrintResult(query);
		
   
	}
	catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
   }//end

   public static void UpdateUserInfo(Cafe esql){
      // Your code goes here.
      // ...
      // ...
      // done not tested done at home
      try{
		String updateRes;
          do{
          System.out.print("What do you want to update (phone number,password,or favorite items)(type done when done):");
          updateRes = in.readLine();
          if(updateRes.toLowerCase().equals("phone number"))
          {
              System.out.print("What is the updated phone number: ");
              String phoneno = in.readLine();
              String query = "UPDATE Users SET phoneNum = '" + phoneno + "' WHERE login = '" + authorisedUser + "'";
              esql.executeUpdate(query);
          }
          else if(updateRes.toLowerCase().equals("password"))
          {
              int userNum = 0;
              while(userNum == 0){
                  System.out.print("Please reenter your password for security purposes: ");
                  String pass = in.readLine();
                  String query = "SELECT * FROM USERS WHERE login = '" + authorisedUser + "' AND password = '" + pass + "'";
                  userNum = esql.executeQuery(query);
                  if(userNum == 0)
                  {
                      System.out.print("Incorrect Password: Try again\n");
                  }
              }
              System.out.print("What is the updated password: ");
              String passnew = in.readLine();
              String query = "UPDATE Users SET password = '" + passnew + "' WHERE login = '" + 			authorisedUser + "'";
              esql.executeUpdate(query);
		query = "SELECT * FROM Users WHERE login = '" + authorisedUser +"'";
              esql.executeQueryAndPrintResult(query);
          }
          else if(updateRes.toLowerCase().equals( "favorite items"))
          {
              System.out.print("What are your favorite items: ");
              String favs = in.readLine();
              //String query = "SELECT favItems FROM USERS WHERE login = '" + authorisedUser + "'";
              //List<List<String>> trys = esql.executeQueryAndReturnResult(query);
             //String items = (trys.get(0)).get(0);
              String query = "UPDATE Users SET favItems = '" + favs + "' WHERE login = '" + authorisedUser + "'";
              esql.executeUpdate(query);
          }
          else if(!updateRes.toLowerCase().equals("done"))
          {
              System.out.print("Unrecongized input\n");
          }
          }
          while(!updateRes.toLowerCase().equals("done"));
      }
catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
   }//end

   public static void ManagerUpdateUserInfo(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	try{
	
		String updateRes;
          do{
          System.out.print("What do you want to update (phone number,password,favorite items, or user type)(type done when done):");
          updateRes = in.readLine();
          if(updateRes.toLowerCase().equals("phone number"))
          {
              System.out.print("What is the updated phone number: ");
              String phoneno = in.readLine();
              String query = "UPDATE Users SET phoneNum = '" + phoneno + "' WHERE login = '" + authorisedUser + "'";
              esql.executeUpdate(query);
          }
          else if(updateRes.toLowerCase().equals("password"))
          {
              int userNum = 0;
              while(userNum == 0){
                  System.out.print("Please reenter your password for security purposes: ");
                  String pass = in.readLine();
                  String query = "SELECT * FROM USERS WHERE login = '" + authorisedUser + "' AND password = '" + pass + "'";
                  userNum = esql.executeQuery(query);
                  if(userNum == 0)
                  {
                      System.out.print("Incorrect Password: Try again\n");
                  }
              }
              System.out.print("What is the updated password: ");
              String passnew = in.readLine();
              String query = "UPDATE Users SET password = '" + passnew + "' WHERE login = '" + 			authorisedUser + "'";
              esql.executeUpdate(query);
		query = "SELECT * FROM Users WHERE login = '" + authorisedUser +"'";
              esql.executeQueryAndPrintResult(query);
          }
          else if(updateRes.toLowerCase().equals( "favorite items"))
          {
              System.out.print("What are your favorite items: ");
              String favs = in.readLine();
              //String query = "SELECT favItems FROM USERS WHERE login = '" + authorisedUser + "'";
              //List<List<String>> trys = esql.executeQueryAndReturnResult(query);
             //String items = (trys.get(0)).get(0);
              String query = "UPDATE Users SET favItems = '" + favs + "' WHERE login = '" + authorisedUser + "'";
              esql.executeUpdate(query);
          }
	  else if(updateRes.toLowerCase().equals("user type"))
	{
		System.out.print("What user would you like to change: ");
		String userna = in.readLine();
		String query = "SELECT * FROM Users WHERE login = '" + userna + "'";
		int numba = esql.executeQuery(query);
		while(numba == 0)
		{
			System.out.print("User does not exist\n");
			System.out.print("What user would you like to change: ");
			userna = in.readLine();
			query = "SELECT * FROM Users WHERE login = '" + userna + "'";
			numba = esql.executeQuery(query);
		}
		System.out.print("What is the new status:(Customer, Manager, or Employee)");
		String stat = in.readLine();
		while(!stat.equals("Customer") && !stat.equals("Manager") && !stat.equals("Employee"))
		{
			System.out.print("Unreconized input\n");
			System.out.print("What is the new status:(Customer, Manager, or Employee)");
			stat = in.readLine();
		}
		query = "UPDATE Users SET type = '" + stat + "' WHERE login = '" + userna + "'";
		esql.executeUpdate(query);
	}
          else if(!updateRes.toLowerCase().equals("done"))
          {
              System.out.print("Unrecongized input\n");
          }
          }
          while(!updateRes.toLowerCase().equals("done"));
	}
	catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
   
   }//end

   public static void UpdateMenu(Cafe esql){
      // Your code goes here.
      // ...
      // ...
try{
      System.out.print("Update, add, or delete: ");
      String options = in.readLine();
      if(options.toLowerCase().equals("update"))
      {
	String itUp = "";
	  do{
          	System.out.print("What item would you like to update (Type 'q' to exit.): ");
          	itUp = in.readLine();
          	String query = "SELECT * FROM Menu WHERE itemName = '" + itUp + "'";
          	int count = esql.executeQuery(query);
		if (itUp.toLowerCase().equals("q"))
		{
			break;	
		}
          	if(count == 0)
          	{
              		System.out.print("No such item name\n");
              		return;
          	}
		query = "SELECT * FROM ItemStatus WHERE itemName = '" + itUp + "'";
		count = esql.executeQuery(query);
		if(count != 0)
		{
			System.out.print("Item in order cannot update");
			return;
		}
		if (itUp.toLowerCase().equals("q"))
		{
			break;	
		}
		else
		{
	        	String fieldToUpdate = "";
			do
			{
				System.out.println("What would you like to update(Name, Type, Price, Description, URL, Type 'q' to exit)?: ");
				String q = "";
				fieldToUpdate = in.readLine();
				if (fieldToUpdate.toLowerCase().equals("q"))
				{
					break;	
				}
				else if (fieldToUpdate.toLowerCase().equals("name"))
				{
					System.out.print("New name: ");
					String newnm = in.readLine();
					q = "UPDATE MENU SET ITEMNAME = '" + newnm + "' WHERE ITEMNAME = '" + itUp +"'";
					esql.executeUpdate(q);
				}
				else if (fieldToUpdate.toLowerCase().equals("type"))
				{
					System.out.print("New type: ");
					String newtype = in.readLine();
					q = "UPDATE MENU SET TYPE = '" + newtype + "' WHERE ITEMNAME = '" + itUp +"'";
					esql.executeUpdate(q);	
				}
				else if (fieldToUpdate.toLowerCase().equals("price"))
				{
					System.out.print("New price: ");
					String newprice = in.readLine();
					q = "UPDATE MENU SET PRICE = '" + Double.parseDouble(newprice) + "' WHERE ITEMNAME = '" + itUp +"'";
					esql.executeUpdate(q);					
				}
				else if (fieldToUpdate.toLowerCase().equals("description"))
				{
					System.out.print("New description: ");
					String newdes = in.readLine();
					q = "UPDATE MENU SET DESCRIPTION = '" + newdes + "' WHERE ITEMNAME = '" + itUp +"'";
					esql.executeUpdate(q);					
				}
				else if (fieldToUpdate.toLowerCase().equals("url"))
				{
					System.out.print("New url: ");
					String newurl = in.readLine();
					q = "UPDATE MENU SET IMAGEURL = '" + newurl + "' WHERE ITEMNAME = '" + itUp +"'";
					esql.executeUpdate(q);					
				}
				else
				{
					System.out.println("Unrecognized input. Please try again.");
				}
			} while (true);
		}
	    } while(true);
         
	
      }
      else if(options.toLowerCase().equals("add"))
      {
          System.out.print("Item name: ");
          String iName = in.readLine();
          System.out.print("Type: ");
          String typename = in.readLine();
          System.out.print("Price: ");
          String pricenew = in.readLine();
          System.out.print("Description: ");
          String desc = in.readLine();
          System.out.print("URL: ");
          String url = in.readLine();
          String query = String.format("INSERT INTO Menu ( itemName, type, price, description, imageURL)VALUES ('%s','%s','%s','%s','%s')", iName, typename,pricenew,desc, url);
          esql.executeUpdate(query);
      }
	else if(options.toLowerCase().equals("delete"))
	{
		System.out.print("What item name do you want to delete? ");
		String iname = in.readLine();
		String query = "SELECT * FROM Menu WHERE itemName = '" + iname + "'";
		int count = esql.executeQuery(query);
		if(count == 0)
		{
			System.out.print("No such item name\n ");
			return;
		}
		query = "SELECT * FROM ItemStatus WHERE itemName = '" + iname + "'";
		count = esql.executeQuery(query);
		if(count!=0)
		{
			System.out.print("Cannot delete because still in order\n");
			return;
		}
		query = "DELETE FROM Menu WHERE itemName = '" + iname + "'";
		esql.executeUpdate(query);
	}
	else
	return;
}
catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
   }//end

   public static void ViewOrderStatus(Cafe esql){
      // Your code goes here.
      // ...
      // ...
try{
        System.out.print("What is the order id: ");
        String oid = in.readLine();
        String query = "SELECT * FROM Orders WHERE orderid = '" + oid + "'";
        int count = esql.executeQuery(query);
          if(count == 0)
          {
              System.out.print("No such order id");
              return;
          }
        query = "SELECT itemName, status FROM ItemStatus WHERE orderid = '" + oid +"'";
        esql.executeQueryAndPrintResult(query);
}

catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
   }//end

   public static void ViewCurrentOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
	//timestamp
	try{
		Timestamp date = new Timestamp(System.currentTimeMillis());
		String query = "SELECT * FROM Orders WHERE timeStampRecieved >= NOW() - INTERVAL '1' DAY AND paid = false"; //index this
		esql.executeQueryAndPrintResult(query);
	}
   
catch(Exception e){
		System.err.println (e.getMessage());
	      
	}
   }//end

   public static void Query6(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Cafe

package ReservationManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Customer.Customer;
import DBConnection.DBConnection;
import Reservation.Reservation;
import Table.Table;
import TableNotAvailableException.TableNotAvailableException;
public class ReservationManager {
	 private ArrayList<Table> tables = new ArrayList<>();
	    private HashMap<Integer, Reservation> reservations = new HashMap<>();
	    private static final Lock lock = new ReentrantLock();

	    public ReservationManager() {
	        loadTablesFromDatabase();
	    }

	    private void loadTablesFromDatabase() {
	        try (Connection connection = DBConnection.getConnection();
	             Statement statement = connection.createStatement();
	             ResultSet resultSet = statement.executeQuery("SELECT * FROM Tables")) {

	            while (resultSet.next()) {
	                int tableId = resultSet.getInt("table_id");
	                int capacity = resultSet.getInt("capacity");
	                if (capacity <= 4) {
	                    tables.add(new SmallTable(tableId));
	                } else {
	                    tables.add(new LargeTable(tableId));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	   
	    public synchronized Reservation createReservation(Customer customer, int partySize, String date, String time) throws TableNotAvailableException {
	        lock.lock();
	        try (Connection connection = DBConnection.getConnection()) {
	            int customerId = insertCustomerIfNotExists(connection, customer);

	            for (Table table : tables) {
	                if (table.getCapacity() >= partySize && isTableAvailable(connection, table, date, time)) {
	                    int reservationId = insertReservation(connection, customerId, table.getTableId(), date, time);
	                    Reservation reservation = new Reservation(customer, table, date, time);
	                    reservations.put(reservationId, reservation);
	                    logReservationToFile(reservation);
	                    return reservation;
	                }
	            }
	            throw new TableNotAvailableException("No available table for the given party size and time.");
	        } catch (SQLException e) {
	            e.printStackTrace();
	            throw new TableNotAvailableException("Database error occurred while creating reservation.");
	        } finally {
	            lock.unlock();
	        }
	    }

	   
	    private int insertCustomerIfNotExists(Connection connection, Customer customer) throws SQLException {
	        String checkCustomerQuery = "SELECT customer_id FROM Customers WHERE name = ?";
	        try (PreparedStatement checkStatement = connection.prepareStatement(checkCustomerQuery)) {
	            checkStatement.setString(1, customer.getName());
	            ResultSet resultSet = checkStatement.executeQuery();
	            if (resultSet.next()) {
	                return resultSet.getInt("customer_id");
	            }
	        }

	        String insertCustomerQuery = "INSERT INTO Customers (name) VALUES (?)";
	        try (PreparedStatement insertStatement = connection.prepareStatement(insertCustomerQuery, Statement.RETURN_GENERATED_KEYS)) {
	            insertStatement.setString(1, customer.getName());
	            insertStatement.executeUpdate();
	            ResultSet generatedKeys = insertStatement.getGeneratedKeys();
	            if (generatedKeys.next()) {
	                return generatedKeys.getInt(1);
	            } else {
	                throw new SQLException("Failed to insert customer.");
	            }
	        }
	    }

	    private boolean isTableAvailable(Connection connection, Table table, String date, String time) throws SQLException {
	        String query = "SELECT COUNT(*) AS count FROM Reservations WHERE table_id = ? AND date = ? AND time = ?";
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setInt(1, table.getTableId());
	            statement.setString(2, date);
	            statement.setString(3, time);
	            ResultSet resultSet = statement.executeQuery();
	            return resultSet.next() && resultSet.getInt("count") == 0;
	        }
	    }

	    
	    private int insertReservation(Connection connection, int customerId, int tableId, String date, String time) throws SQLException {
	        String query = "INSERT INTO Reservations (customer_id, table_id, date, time) VALUES (?, ?, ?, ?)";
	        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
	            statement.setInt(1, customerId);
	            statement.setInt(2, tableId);
	            statement.setString(3, date);
	            statement.setString(4, time);
	            statement.executeUpdate();
	            ResultSet generatedKeys = statement.getGeneratedKeys();
	            if (generatedKeys.next()) {
	                return generatedKeys.getInt(1);
	            } else {
	                throw new SQLException("Failed to insert reservation.");
	            }
	        }
	    }

	   
	    private void logReservationToFile(Reservation reservation) {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter("reservation_log.txt", true))) {
	            writer.write("Reservation ID: " + reservation.getReservationId() + ", Customer: " + reservation.getCustomer().getName() +
	                    ", Table ID: " + reservation.getTable().getTableId() + ", Date: " + reservation.getDate() +
	                    ", Time: " + reservation.getTime() + "\n");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    
	    public Reservation getReservationById(int reservationId) {
	        return reservations.get(reservationId);
	    }

	   
	    public boolean cancelReservation(int reservationId) {
	        return reservations.remove(reservationId) != null;
	    }

	  
	    public void showAvailableSlots(String date) {
	        System.out.println("Available slots on " + date + ":");

	        try (Connection connection = DBConnection.getConnection()) {
	            for (Table table : tables) {
	                ArrayList<String> bookedTimes = getBookedTimesForTable(connection, table.getTableId(), date);
	                ArrayList<String> availableTimes = getAvailableTimes(bookedTimes);

	                System.out.println("Table ID " + table.getTableId() + " - Available Times:");
	                if (availableTimes.isEmpty()) {
	                    System.out.println("  No available slots for this table on " + date);
	                } else {
	                    for (String time : availableTimes) {
	                        System.out.println("  " + time);
	                    }
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    
	    private ArrayList<String> getBookedTimesForTable(Connection connection, int tableId, String date) throws SQLException {
	        ArrayList<String> bookedTimes = new ArrayList<>();
	        String query = "SELECT time FROM Reservations WHERE table_id = ? AND date = ?";

	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setInt(1, tableId);
	            statement.setString(2, date);
	            ResultSet resultSet = statement.executeQuery();
	            while (resultSet.next()) {
	                bookedTimes.add(resultSet.getString("time"));
	            }
	        }
	        return bookedTimes;
	    }

	  
	    private ArrayList<String> getAvailableTimes(ArrayList<String> bookedTimes) {
	        ArrayList<String> allPossibleTimes = new ArrayList<>();
	        // Example times - adjust as needed
	        allPossibleTimes.add("12:00");
	        allPossibleTimes.add("13:00");
	        allPossibleTimes.add("14:00");
	        allPossibleTimes.add("15:00");
	        allPossibleTimes.add("16:00");

	        // Remove booked times from the list of all possible times
	        allPossibleTimes.removeAll(bookedTimes);

	        return allPossibleTimes;
	    }

		
}

package main;
import java.util.Scanner;

import Customer.Customer;
import Reservation.Reservation;
import ReservationManager.ReservationManager;
import TableNotAvailableException.TableNotAvailableException;

public class Main {
    private static ReservationManager reservationManager = new ReservationManager();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n--- Restaurant Reservation System ---");
            System.out.println("1. Reserve a Table");
            System.out.println("2. View Available Slots");
            System.out.println("3. Check Reservation Time");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    reserveTable(scanner);
                    break;
                case 2:
                    viewAvailableSlots(scanner);
                    break;
                case 3:
                    checkReservationTime(scanner);
                    break;
                case 4:
                    cancelReservation(scanner);
                    break;
                case 5:
                    System.out.println("Thank you for using the reservation system.");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private static void reserveTable(Scanner scanner) {
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Party Size: ");
        int partySize = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Reservation Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter Reservation Time (HH:MM): ");
        String time = scanner.nextLine();

        Customer customer = new Customer(name);
        try {
            Reservation reservation = reservationManager.createReservation(customer, partySize, date, time);
            System.out.println("Reservation created successfully. Reservation ID: " + reservation.getReservationId());
        } catch (TableNotAvailableException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAvailableSlots(Scanner scanner) {
        System.out.print("Enter Date (YYYY-MM-DD) to check availability: ");
        String date = scanner.nextLine();
        System.out.println("Available slots on " + date + ":");
        reservationManager.showAvailableSlots(date);
    }

    private static void checkReservationTime(Scanner scanner) {
        System.out.print("Enter Reservation ID to check time: ");
        int reservationId = scanner.nextInt();
        scanner.nextLine();

        Reservation reservation = ((Object) reservationManager).getReservationById(reservationId);
        if (reservation != null) {
            System.out.println("Reservation Date and Time: " + reservation.getDate() + " " + reservation.getTime());
        } else {
            System.out.println("Reservation not found.");
        }
    }

    private static void cancelReservation(Scanner scanner) {
        System.out.print("Enter Reservation ID to cancel: ");
        int reservationId = scanner.nextInt();
        scanner.nextLine();

        boolean canceled = reservationManager.cancelReservation(reservationId);
        if (canceled) {
            System.out.println("Reservation cancelled successfully.");
        } else {
            System.out.println("Reservation not found or could not be canceled.");
        }
    }
}


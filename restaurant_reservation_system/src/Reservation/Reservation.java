package Reservation;

import Customer.Customer;
import Table.Table;

public class Reservation {
	private static int counter = 1;
	private int reservationId;
    private Customer customer;
    private Table table;
    private String date;
    private String time;

    public Reservation(Customer customer, Table table, String date, String time) {
        this.reservationId = counter++;
        this.customer = customer;
        this.table = table;
        this.date = date;
        this.time = time;
    }
    public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		Reservation.counter = counter;
	}

	public int getReservationId() {
		return reservationId;
	}

	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	

}

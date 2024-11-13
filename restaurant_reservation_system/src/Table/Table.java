package Table;

public class Table {
	 private int tableId;
	    private int capacity;

	    public Table(int tableId, int capacity) {
	        this.tableId = tableId;
	        this.capacity = capacity;
	    }

	    public int getTableId() {
	        return tableId;
	    }

	    public int getCapacity() {
	        return capacity;
	    }
}
class SmallTable extends Table {
    public SmallTable(int tableId) {
        super(tableId, 4); // Small table with capacity of 4
    }
}

class LargeTable extends Table {
    public LargeTable(int tableId) {
        super(tableId, 8); // Large table with capacity of 8
    }
}

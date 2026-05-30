import java.util.ArrayList;
import java.util.Scanner;

record Produce(String produceName, double price) {
}

class Warehouse {
    private final ArrayList<Produce> storage;
    private double taxSavings;

    public int tomatoesSold;
    public int lettuceSold;
    public int carrotsSold;

    public Warehouse() {
        storage = new ArrayList<>();
        taxSavings = 0;
        tomatoesSold = 0;
        lettuceSold = 0;
        carrotsSold = 0;
    }

    public synchronized void storeProduce(Produce p) {
        storage.add(p);
    }

    public synchronized Produce getProduce() {
        if (storage.isEmpty()) return null;
        return storage.removeFirst();
    }

    public synchronized void addToTaxSavings(double amount) {
        taxSavings += amount;
    }

    public double getTaxSavings() {
        return taxSavings;
    }
}

class FarmHand extends Thread {
    private final Warehouse warehouse;
    private final Produce produce;
    private final int produceAmount;

    public FarmHand(Warehouse warehouse, Produce produce, int produceAmount) {
        this.warehouse = warehouse;
        this.produce = produce;
        this.produceAmount = produceAmount;
    }

    @Override
    public void run() {
        for (int i = 0; i < produceAmount; i++) {
            warehouse.storeProduce(produce);
        }
        System.out.println("Worker storing " + produce.produceName() + " is done.");
    }
}

class CharityWorker extends Thread {
    private static int nextId = 0;
    private final int workerId;
    private final Warehouse warehouse;

    public CharityWorker(Warehouse warehouse) {
        this.workerId = nextId;
        nextId++;
        this.warehouse = warehouse;
    }

    @Override
    public void run() {
        while (true) {
            Produce p = warehouse.getProduce();
            if (p == null) {
                System.out.println("Worker " + workerId + " is done working");
                return;
            }

            warehouse.addToTaxSavings(p.price());

            switch (p.produceName()) {
                case "tomatoes" -> warehouse.tomatoesSold++;
                case "lettuce"  -> warehouse.lettuceSold++;
                case "carrots"  -> warehouse.carrotsSold++;
            }
        }
    }
}

public class Assignment7 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Warehouse warehouse = new Warehouse();

        System.out.println("[GoodFortune Warehouse]\n");

        System.out.print("Store how many boxes of tomatoes: ");
        int tCount = scanner.nextInt();
        System.out.print("What is the price of tomatoes? $");
        double tPrice = scanner.nextDouble();
        Produce tomatoes = new Produce("tomatoes", tPrice);
        FarmHand tWorker = new FarmHand(warehouse, tomatoes, tCount);

        System.out.print("Store how many boxes of lettuce: ");
        int lCount = scanner.nextInt();
        System.out.print("What is the price of lettuce? $");
        double lPrice = scanner.nextDouble();
        Produce lettuce = new Produce("lettuce", lPrice);
        FarmHand lWorker = new FarmHand(warehouse, lettuce, lCount);

        System.out.print("Store how many boxes of carrots: ");
        int cCount = scanner.nextInt();
        System.out.print("What is the price of carrots? $");
        double cPrice = scanner.nextDouble();
        Produce carrots = new Produce("carrots", cPrice);
        FarmHand cWorker = new FarmHand(warehouse, carrots, cCount);

        System.out.println("\nFarm hands are ready to start working.");
        System.out.print("Press any key to start the work...");
        scanner.nextLine();
        scanner.nextLine();

        System.out.println("\nWork has started. Waiting for farm hands to finish...");
        tWorker.start();
        lWorker.start();
        cWorker.start();

        try {
            tWorker.join();
            lWorker.join();
            cWorker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Warehouse is fully loaded.\n");

        System.out.print("Assign how many workers at the warehouse? ");
        int workerCount = scanner.nextInt();

        CharityWorker[] workers = new CharityWorker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new CharityWorker(warehouse);
        }

        System.out.println("All workers are at their stations.");
        System.out.print("Press any key to start charity event...");
        scanner.nextLine();
        scanner.nextLine();

        System.out.println("\nCharity event started...");

        for (CharityWorker cw : workers) cw.start();
        for (CharityWorker cw : workers) {
            try {
                cw.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nThe event was a success!");
        System.out.println("A total of " + warehouse.tomatoesSold + " boxes of tomatoes were given away.");
        System.out.println("A total of " + warehouse.lettuceSold + " boxes of lettuce were given away.");
        System.out.println("A total of " + warehouse.carrotsSold + " boxes of carrots were given away.");
        System.out.printf("A total of $%.2f was made in tax savings.\n", warehouse.getTaxSavings());

        scanner.close();
    }
}

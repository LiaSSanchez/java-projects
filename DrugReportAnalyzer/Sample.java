import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Sample {
    private int id;
    private static int nextId = 0;

    private String drugType;
    private double weight;
    private int gcTime;
    private String msPeaks;
    private String miscTest;
    private ArrayList<String> tests;

    public Sample(String drugType, double weight, int gcTime, String msPeaks, String miscTest) {
        this.tests = new ArrayList<>();
        this.id = nextId++;
        this.drugType = drugType;
        this.weight = weight;
        this.gcTime = gcTime;
        this.msPeaks = msPeaks;
        this.miscTest = miscTest;
    }

    public int getId() { return id; }
    public String getDrugType() { return drugType; }
    public double getWeight() { return weight; }
    public int getGcTime() { return gcTime; }
    public String getMsPeaks() { return msPeaks; }
    public String getMiscTest() { return miscTest; }
    public List<String> getTests() { return tests; }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (String t : tests) sj.add(t);
        return String.format(
                "Case: #%d%nDrug: %s%nWeight: %.2fg%nTests run: %s",
                id, drugType, weight, sj.toString()
        );
    }
}

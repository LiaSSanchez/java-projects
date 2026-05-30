import java.io.*;
import java.util.*;

public class Assignment6 {

    // -------- Processing methods (must NOT use try-catch inside) --------

    public static void processMarijuana(Sample s) throws TestFailedException {
        // Append tests in the order they are performed
        s.getTests().add("Gas Chromatography");
        if (!gcInRange(s.getGcTime(), mmssToSec(5,47), mmssToSec(6,14))) {
            throw new TestFailedException("Separation time out of bounds", s);
        }

        s.getTests().add("Mass Spectrometry");
        int[] peaks = parsePeaks(s.getMsPeaks());
        int matches = countMatches(peaks, new int[]{314, 299, 231});
        if (matches < 2) {
            throw new TestFailedException("Insufficient peak matches", s);
        }

        s.getTests().add("Gas Chromatography Abundance");
        double abundance = parseDoubleStrict(s.getMiscTest());
        if (abundance < 0.3) {
            throw new TestFailedException("Concentration below 0.3%", s);
        }
    }

    public static void processCocaine(Sample s) throws TestFailedException {
        s.getTests().add("Gas Chromatography");
        if (!gcInRange(s.getGcTime(), mmssToSec(6,38), mmssToSec(7, 2))) {
            throw new TestFailedException("Separation time out of bounds", s);
        }

        s.getTests().add("Mass Spectrometry");
        int[] peaks = parsePeaks(s.getMsPeaks());
        int matches = countMatches(peaks, new int[]{149, 91, 58});
        if (matches < 2) {
            throw new TestFailedException("Insufficient peak matches", s);
        }

        s.getTests().add("Ultraviolet Spectroscopy");
        int uv = parseIntStrict(s.getMiscTest());
        if (uv < 192 || uv > 202) {
            throw new TestFailedException("UV reading out of range", s);
        }
    }

    public static void processMethamphetamine(Sample s) throws TestFailedException {
        s.getTests().add("Gas Chromatography");
        if (!gcInRange(s.getGcTime(), mmssToSec(5,7), mmssToSec(5,16))) {
            throw new TestFailedException("Separation time out of bounds", s);
        }

        s.getTests().add("Mass Spectrometry");
        int[] peaks = parsePeaks(s.getMsPeaks());
        int matches = countMatches(peaks, new int[]{303, 182, 82});
        if (matches < 2) {
            throw new TestFailedException("Insufficient peak matches", s);
        }

        s.getTests().add("Logo ID");
        String[] parts = s.getMiscTest().trim().split("\\s+");
        // Expect 3 strings: engraving shape color
        String engraving = parts.length > 0 ? parts[0] : "";
        String shape     = parts.length > 1 ? parts[1] : "";
        String color     = parts.length > 2 ? parts[2] : "";

        // These variants are legal (thus FAIL the test)
        boolean isLegalVariant =
                (engraving.equalsIgnoreCase("R-12") && shape.equalsIgnoreCase("round") && color.equalsIgnoreCase("white")) ||
                        (engraving.equalsIgnoreCase("V-20") && shape.equalsIgnoreCase("oval")  && color.equalsIgnoreCase("blue"))  ||
                        (engraving.equalsIgnoreCase("A-65") && shape.equalsIgnoreCase("capsule")&& color.equalsIgnoreCase("pink"));

        if (isLegalVariant) {
            throw new TestFailedException("Prescription medication", s);
        }
        // All other variants pass
    }

    // -------- File processing --------

    public static void processFile(Scanner fileScanner) {
        try (BufferedWriter passed = new BufferedWriter(new FileWriter("passed.txt"));
             BufferedWriter failed = new BufferedWriter(new FileWriter("failed.txt"))) {

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                // CSV: drug_type,weight,test_1,test_2,test_3
                // test_3 may contain spaces (Logo ID), but will be in a single CSV field.
                String[] cols = splitCsvLine(line, 5);
                if (cols.length < 5) continue; // skip malformed

                String drugType = cols[0].trim();
                double weight = parseDoubleRelaxed(cols[1].trim());
                int gcTime = parseIntRelaxed(cols[2].trim());
                String msPeaks = cols[3].trim();
                String miscTest = cols[4].trim();

                Sample sample = new Sample(drugType, weight, gcTime, msPeaks, miscTest);

                try {
                    switch (drugType.toLowerCase()) {
                        case "marijuana" -> processMarijuana(sample);
                        case "cocaine" -> processCocaine(sample);
                        case "methamphetamine" -> processMethamphetamine(sample);
                        default -> throw new TestFailedException("Unknown drug type", sample);
                    }

                    // If no exception, it passed: compute penalty and write to passed.txt
                    String penalty = computePenalty(sample.getDrugType(), sample.getWeight());
                    writeBlock(passed, sample.toString(), "Result: Positive, " + penalty);

                } catch (TestFailedException ex) {
                    // Failed: write to failed.txt with message
                    writeBlock(failed, sample.toString(), "Result: Negative, " + ex.getMessage());
                }
            }

            passed.flush();
            failed.flush();
        } catch (IOException ioe) {
            // If writing outputs fails, report to console (not specified but sensible)
            System.out.println("I/O error while writing outputs: " + ioe.getMessage());
        }
    }

    // -------- main --------

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("[Drug Report Analyzer]");
        System.out.print("Enter name of drug file: ");
        String name = in.nextLine().trim();

        File f = new File(name);
        if (!f.exists()) {
            System.out.println("Could not find file '" + name + "'");
            System.out.println();
            System.out.println("Program complete.");
            return;
        }

        try (Scanner fileScanner = new Scanner(f)) {
            System.out.println("File loaded, processing...");
            processFile(fileScanner);
            System.out.println("File processed. Outputs written to 'passed.txt' and 'failed.txt'.");
            System.out.println();
            System.out.println("Program complete.");
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file '" + name + "'");
            System.out.println();
            System.out.println("Program complete.");
        }
    }

    // -------- Helpers --------

    private static boolean gcInRange(int seconds, int lo, int hi) {
        return seconds >= lo && seconds <= hi;
    }

    private static int mmssToSec(int mm, int ss) {
        return mm * 60 + ss;
    }

    private static int[] parsePeaks(String s) {
        String[] parts = s.trim().split("\\s+");
        int[] out = new int[Math.min(3, parts.length)];
        for (int i = 0; i < out.length; i++) {
            out[i] = parseIntRelaxed(parts[i]);
        }
        // If fewer than 3 provided, unspecified remain zero (won't match)
        if (out.length < 3) {
            out = Arrays.copyOf(out, 3);
        }
        return out;
    }

    private static int countMatches(int[] peaks, int[] expected) {
        int count = 0;
        for (int p : peaks) {
            for (int e : expected) {
                if (p == e) { count++; break; }
            }
        }
        return count;
    }

    private static String computePenalty(String drugType, double weight) {
        String d = drugType.toLowerCase();
        if (d.equals("marijuana")) {
            // Marijuana penalties
            if (weight < 28.35) return "Misdemeanor";
            if (weight < 4535) return "1 - 10 years in prison";
            if (weight < 907184.7) return "Minimum 5 years, $100,00 fine";
            if (weight < 4535924) return "Minimum 7 years, $250,000 fine";
            return "Minimum 15 years, $1,000,000 fine";
        } else {
            // Cocaine & Methamphetamine penalties
            if (weight < 1) return "Up to 3 years in prison";
            if (weight < 4) return "1 - 8 years in prison";
            if (weight < 28) return "1 - 15 years in prison";
            if (weight < 200) return "Minimum 10 years, $200,000 fine";
            if (weight < 400) return "Minimum 15 years, $300,000 fine";
            return "Minimum 25 years, $1,000,000 fine";
        }
    }

    private static void writeBlock(BufferedWriter bw, String header, String resultLine) throws IOException {
        bw.write(header);
        bw.newLine();
        bw.write(resultLine);
        bw.newLine();
        bw.write("====");
        bw.newLine();
    }

    // Split a CSV line with exactly N fields where the last field may contain commas/spaces
    private static String[] splitCsvLine(String line, int expectedFields) {
        List<String> out = new ArrayList<>(expectedFields);
        int start = 0;
        int commasToSplit = expectedFields - 1;
        for (int i = 0; i < line.length() && commasToSplit > 0; i++) {
            if (line.charAt(i) == ',') {
                out.add(line.substring(start, i));
                start = i + 1;
                commasToSplit--;
            }
        }
        out.add(line.substring(start));
        return out.toArray(new String[0]);
    }

    private static int parseIntRelaxed(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private static int parseIntStrict(String s) {
        return Integer.parseInt(s.trim());
    }

    private static double parseDoubleRelaxed(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return 0.0; }
    }

    private static double parseDoubleStrict(String s) {
        return Double.parseDouble(s.trim());
    }
}

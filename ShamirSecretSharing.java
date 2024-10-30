import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {
    
    public static void main(String[] args) {
        try {
            // Read and process both test cases
            processTestCase("src/resources/testcase1.json");
            processTestCase("src/resources/testcase2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processTestCase(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            System.err.println("File not found or cannot be read: " + filePath);
            return; // Early exit if file cannot be read
        }

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(new JSONTokener(content));

        // Validate JSON structure
        if (!jsonObject.has("keys")) {
            System.err.println("Invalid JSON structure in " + filePath + ": missing 'keys'");
            return;
        }

        int n = jsonObject.getJSONObject("keys").getInt("n");
        int k = jsonObject.getJSONObject("keys").getInt("k");

        if (k > n) {
            System.err.println("Invalid parameters in " + filePath + ": k (" + k + ") cannot be greater than n (" + n + ")");
            return;
        }

        List<Integer> xValues = new ArrayList<>();
        List<BigInteger> yValues = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            JSONObject root = jsonObject.getJSONObject(String.valueOf(i));
            if (!root.has("base") || !root.has("value")) {
                System.err.println("Invalid JSON structure in " + filePath + ": missing 'base' or 'value' in entry " + i);
                return;
            }
            int base = Integer.parseInt(root.getString("base"));
            if (base <= 1) {
                System.err.println("Invalid base in " + filePath + ": base must be greater than 1 for entry " + i);
                return;
            }
            String value = root.getString("value");

            // Decode y value
            try {
                BigInteger decodedValue = decodeValue(value, base);
                xValues.add(i); // Using the index as the x value
                yValues.add(decodedValue);
            } catch (NumberFormatException e) {
                System.err.println("Error decoding value in " + filePath + ": " + e.getMessage());
                return;
            }
        }

        // Compute the constant term (c) of the polynomial
        BigInteger secret = computeConstantTerm(xValues, yValues, k);
        System.out.println("Secret (c) for " + filePath + ": " + secret);
    }

    private static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    private static BigInteger computeConstantTerm(List<Integer> xValues, List<BigInteger> yValues, int k) {
        // Implement Lagrange interpolation or another method to calculate the constant term c
        // Here, we will use a simplified Lagrange interpolation for demonstration purposes
        BigInteger c = BigInteger.ZERO;

        // Ensure we are only using k values
        for (int i = 0; i < k; i++) {
            BigInteger term = yValues.get(i);
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    term = term.multiply(BigInteger.valueOf(-xValues.get(j)))
                                .divide(BigInteger.valueOf(xValues.get(i) - xValues.get(j)));
                }
            }
            c = c.add(term);
        }
        return c;
    }
}

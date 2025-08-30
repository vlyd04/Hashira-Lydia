import java.io.*;
import java.math.BigInteger;
import java.util.*;
import com.google.gson.*;

public class SecretReconstruction {

    /** --- Data holder for one share --- */
    static class Share {
        int base;
        String value;
    }

    /** --- Data holder for keys (n, k) --- */
    static class Keys {
        int n;
        int k;
    }

    /** --- Parse JSON into structured maps --- */
    public static ParsedInput parseJson(String filename) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filename)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            Keys keys = gson.fromJson(root.getAsJsonObject("keys"), Keys.class);

            List<BigInteger[]> points = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getKey().equals("keys")) continue;
                String xStr = entry.getKey();
                Share share = gson.fromJson(entry.getValue(), Share.class);
                BigInteger x = new BigInteger(xStr);
                BigInteger y = new BigInteger(share.value, share.base);
                points.add(new BigInteger[]{x, y});
            }
            points.sort(Comparator.comparing(p -> p[0]));
            return new ParsedInput(keys.n, keys.k, points);
        }
    }

    /** --- Lagrange interpolation at x = 0 --- */
    public static BigInteger lagrangeInterpolation(List<BigInteger[]> points) {
        BigInteger secret = BigInteger.ZERO;
        int k = points.size();

        for (int i = 0; i < k; i++) {
            BigInteger xi = points.get(i)[0];
            BigInteger yi = points.get(i)[1];
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                BigInteger xj = points.get(j)[0];
                num = num.multiply(xj.negate());         // (0 - xj)
                den = den.multiply(xi.subtract(xj));     // (xi - xj)
            }
            secret = secret.add(yi.multiply(num).divide(den));
        }
        return secret;
    }

    /** --- Try to reconstruct polynomial and detect bad shares --- */
    public static Result reconstructSecret(List<BigInteger[]> points, int k) {
        // Pick first k shares
        List<BigInteger[]> candidateSet = new ArrayList<>(points.subList(0, k));
        BigInteger candidateSecret = lagrangeInterpolation(candidateSet);

        // Validate against all shares
        Set<BigInteger> goodX = new HashSet<>();
        for (int i = 0; i < points.size(); i++) {
            List<BigInteger[]> testSet = new ArrayList<>(candidateSet);
            if (!testSet.contains(points.get(i))) {
                testSet.set(k - 1, points.get(i)); // replace last with new
            }
            BigInteger secret = lagrangeInterpolation(testSet);
            if (secret.equals(candidateSecret)) {
                goodX.add(points.get(i)[0]);
            }
        }

        // Identify bad shares
        List<BigInteger[]> badShares = new ArrayList<>();
        for (BigInteger[] pt : points) {
            if (!goodX.contains(pt[0])) badShares.add(pt);
        }

        return new Result(candidateSecret, badShares);
    }

    /** --- Helper records --- */
    static class ParsedInput {
        int n, k;
        List<BigInteger[]> points;
        ParsedInput(int n, int k, List<BigInteger[]> points) {
            this.n = n; this.k = k; this.points = points;
        }
    }
    static class Result {
        BigInteger secret;
        List<BigInteger[]> badShares;
        Result(BigInteger s, List<BigInteger[]> b) { secret = s; badShares = b; }
    }

    /** --- Main --- */
    public static void main(String[] args) throws Exception {
        String filename = (args.length < 1) ? "input.json" : args[0];
        if (args.length < 1) {
            System.out.println("No input file specified. Using default: input.json");
        }

        ParsedInput parsed = parseJson(filename);
        Result res = reconstructSecret(parsed.points, parsed.k);

        // Output
        System.out.println("✅ Reconstructed Secret (decimal): " + res.secret);
        System.out.println("✅ Reconstructed Secret (hex): " + res.secret.toString(16));
        if (res.badShares.isEmpty()) {
            System.out.println("✅ All shares consistent.");
        } else {
            System.out.println("⚠️  Possibly Wrong/Corrupt Shares:");
            for (BigInteger[] share : res.badShares) {
                System.out.println("   x=" + share[0] + " y=" + share[1]);
            }
        }
    }
}

import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class XorYapayZeka {
    // Yapay zeka ağırlık ve bias tanımlamaları
    private static double[][] w1 = { {0.5, 0.2, 0.1, 0.7}, {0.3, 0.8, 0.4, 0.6} };
    private static double[] b1 = {0.1, 0.3, 0.2, 0.5};
    private static double[] w2 = {0.2, 0.6, 0.1, 0.8};
    private static double b2 = 0.4;

    public static void main(String[] args) {
        System.out.println("Yapay Zeka Modeli Baslatiliyor...");
        
        // Modelin çalıştığını loglarda görebilmek için örnek bir test
        double[] testInput = {1.0, 0.0};
        double sonuc = predict(testInput);
        System.out.println("XOR Test (1, 0) Sonucu: " + sonuc);
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double predict(double[] input) {
        double[] hidden = new double[4];
        for (int i = 0; i < 4; i++) {
            hidden[i] = sigmoid(input[0] * w1[0][i] + input[1] * w1[1][i] + b1[i]);
        }
        double output = 0;
        for (int i = 0; i < 4; i++) {
            output += hidden[i] * w2[i];
        }
        return sigmoid(output + b2);
    }

    // Render'ın "Port açılmadı" veya "Program bitti" diyerek çökertmesini önleyen entegre web dinleyicisi
    static {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", exchange -> {
                String response = "Yapay Zeka Aktif ve Canli!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Arka plan dinleyicisi " + port + " portunda basariyla baslatildi.");
        } catch (Exception e) {
            System.out.println("Sistem baslatilirken bir hata olustu: " + e.getMessage());
        }
    }
}

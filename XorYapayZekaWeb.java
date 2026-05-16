import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@SpringBootApplication
@RestController
public class XorYapayZekaWeb {

    // 1. XOR SİNİR AĞI PARAMETRELERİ (Sizin Orijinal Değerleriniz)
    private static double[][] w1 = new double[2][4]; 
    private static double[] b1 = new double[4];      
    private static double[] w2 = new double[4];      
    private static double b2;                         
    private static double learningRate = 0.5;
    private static final String HAFIZA_DOSYASI = "beyin.txt";

    // Sunucu ilk çalıştığında yapay zekayı otomatik eğitir
    public XorYapayZekaWeb() {
        initializeWeights();
        double[][] inputs = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        double[] outputs = {0, 1, 1, 0};
        for (int epoch = 0; epoch < 50000; epoch++) {
            for (int i = 0; i < inputs.length; i++) train(inputs[i], outputs[i]);
        }
        System.out.println("[Sistem]: Çekirdek Yapay Sinir Ağı web sunucusunda eğitildi ve hazır!");
    }

    public static void main(String[] args) {
        // Uygulamayı bir web sunucusu olarak başlatır (Varsayılan port: 8080)
        SpringApplication.run(XorYapayZekaWeb.class, args);
    }

    // 2. WEB ARAYÜZÜ (Tarayıcıdan gelen istekleri karşılar)
    @GetMapping("/ara")
    public String processMessageWeb(@RequestParam(value = "soru", defaultValue = "") String userInput) {
        if (userInput.isEmpty()) {
            return "<html><body><h3>Lütfen bir soru girin. Örn: /ara?soru=teknoloji</h3></body></html>";
        }

        String cleanQuery = userInput.toLowerCase()
                .replace("nedir", "")
                .replace("kimdir", "")
                .replace("?", "")
                .trim();

        String liveData = "";
        boolean hafizadanAlindi = false;

        // Hafıza kontrolü
        String hafizaSonuc = aramaHafizadan(cleanQuery);
        if (hafizaSonuc != null) {
            liveData = "[Hafızadan Okundu]: " + hafizaSonuc;
            hafizadanAlindi = true;
        } else {
            liveData = fetchTurkishData(cleanQuery);
            if (!liveData.startsWith("[Hata]")) {
                String sadeceAciklama = liveData.replace("[Türkçe Açıklama]: ", "");
                hafizayaKaydet(cleanQuery, sadeceAciklama);
            }
        }
        
        double internetSignal = liveData.startsWith("[Hata]") ? 0.0 : 1.0;
        double userSignal = (userInput.length() % 2 == 0) ? 1.0 : 0.0;

        // XOR Analizi
        double[] aiInput = {userSignal, internetSignal};
        double prediction = predict(aiInput);
        long finalDecision = Math.round(prediction);

        // Kullanıcıya şık bir web çıktısı dönelim
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 40px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background: #f9f9f9;'>");
        html.append("<h2 style='color: #2c3e50;'>Hafızalı Canlı Yapay Zeka Sonucu</h2>");
        html.append("<p><b>Aranan Kelime:</b> ").append(userInput).append("</p>");
        html.append("<div style='background: #fff; padding: 15px; border-left: 5px solid #2ecc71; margin: 15px 0;'>").append(liveData).append("</div>");
        html.append("<hr style='border: 0; border-top: 1px solid #eee;'>");
        html.append("<h3>Hibrid XOR Analiz Raporu</h3>");
        html.append("<ul>");
        html.append("<li><b>Veri Kaynağı:</b> ").append(hafizadanAlindi ? "LOKAL BEYİN (BEYİN.TXT)" : "CANLI İNTERNET").append("</li>");
        html.append("<li><b>Kelime Uzunluk Sinyali:</b> ").append(userSignal).append(" | <b>Bilgi Sinyali:</b> ").append(internetSignal).append("</li>");
        html.append("<li><b>Yapay Sinir Ağı XOR Çıktısı:</b> <span style='font-weight:bold; color:#e74c3c;'>").append(finalDecision).append("</span></li>");
        html.append("</ul>");
        
        if (finalDecision == 1) {
            html.append("<p style='color: green;'><b>[AI Yorumu]:</b> Sinyaller XOR süzgecinden başarıyla (1) geçti.</p>");
        } else {
            html.append("<p style='color: gray;'><b>[AI Yorumu]:</b> Matematiksel zıtlıklar ağımda birbirini sıfırladı (0).</p>");
        }
        html.append("</body></html>");

        return html.toString();
    }

    // 3. HAFIZA VE YAPAY SİNİR AĞI METODLARI (Sizin yazdığınız kodlar - Birebir aynı)
    private static void hafizayaKaydet(String kavram, String aciklama) {
        try (FileWriter fw = new FileWriter(HAFIZA_DOSYASI, StandardCharsets.UTF_8, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(kavram + "||" + aciklama);
        } catch (IOException e) {
            System.out.println("Hafızaya yazılırken hata.");
        }
    }

    private static String aramaHafizadan(String kavram) {
        File dosya = new File(HAFIZA_DOSYASI);
        if (!dosya.exists()) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                if (satir.contains("||")) {
                    String[] parcalar = satir.split("\\|\\|");
                    if (parcalar[0].trim().equalsIgnoreCase(kavram.trim())) {
                        return parcalar[1].trim();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Hafıza okunurken hata.");
        }
        return null;
    }

    private static String fetchTurkishData(String cleanQuery) {
        try {
            String encoded = URLEncoder.encode(cleanQuery, "UTF-8");
            URL url = new URL("https://tr.wikipedia.org/api/rest_v1/page/summary/" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 YapayZekaBot/1.0");

            if (conn.getResponseCode() != 200) {
                return "[Hata]: '" + cleanQuery + "' kavramı internette bulunamadı.";
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) result.append(line);
            rd.close();

            String rawJson = result.toString();
            if (rawJson.contains("\"extract\":\"")) {
                int start = rawJson.indexOf("\"extract\":\"") + 11;
                int end = rawJson.indexOf("\"", start);
                String extract = rawJson.substring(start, end).replace("\\n", " ");
                return "[Türkçe Açıklama]: " + extract;
            }
            return "[Hata]: Format çözülemedi.";
        } catch (Exception e) {
            return "[Hata]: İnternet bağlantı kesintisi.";
        }
    }

    private static double sigmoid(double x) { return 1.0 / (1.0 + Math.exp(-x)); }
    private static double sigmoidDerivative(double x) { return x * (1.0 - x); }

    private static void initializeWeights() {
        Random rand = new Random(12345);
        for (int i = 0; i < 4; i++) {
            w1[0][i] = rand.nextGaussian();
            w1[1][i] = rand.nextGaussian();
            b1[i] = rand.nextGaussian();
            w2[i] = rand.nextGaussian();
        }
        b2 = rand.nextGaussian();
    }

    private static void train(double[] input, double target) {
        double[] hidden = new double[4];
        for (int i = 0; i < 4; i++) hidden[i] = sigmoid(input[0] * w1[0][i] + input[1] * w1[1][i] + b1[i]);
        double output = 0;
        for (int i = 0; i < 4; i++) output += hidden[i] * w2[i];
        output = sigmoid(output + b2);

        double outputError = target - output;
        double outputDelta = outputError * sigmoidDerivative(output);
        double[] hiddenErrors = new double[4];
        double[] hiddenDeltas = new double[4];
        for (int i = 0; i < 4; i++) {
            hiddenErrors[i] = outputDelta * w2[i];
            hiddenDeltas[i] = hiddenErrors[i] * sigmoidDerivative(hidden[i]);
        }
        for (int i = 0; i < 4; i++) {
            w2[i] += learningRate * outputDelta * hidden[i];
            w1[0][i] += learningRate * hiddenDeltas[i] * input[0];
            w1[1][i] += learningRate * hiddenDeltas[i] * input[1];
            b1[i] += learningRate * hiddenDeltas[i];
        }
        b2 += learningRate * outputDelta;
    }

    private static double predict(double[] input) {
        double[] hidden = new double[4];
        for (int i = 0; i < 4; i++) hidden[i] = sigmoid(input[0] * w1[0][i] + input[1] * w1[1][i] + b1[i]);
        double output = 0;
        for (int i = 0; i < 4; i++) output += hidden[i] * w2[i];
        return sigmoid(output + b2);
    }
}
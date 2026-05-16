import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class XorYapayZeka {

    // XOR SİNİR AĞI PARAMETRELERİ
    private static double[][] w1 = new double[2][4]; 
    private static double[] b1 = new double[4];      
    private static double[] w2 = new double[4];      
    private static double b2;                         
    private static double learningRate = 0.5;

    private static JTextArea chatArea;
    private static JTextField inputField;
    private static JButton sendButton;
    
    // HAFIZA DOSYASI ADI
    private static final String HAFIZA_DOSYASI = "beyin.txt";

    public static void main(String[] args) {
        initializeWeights();
        double[][] inputs = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        double[] outputs = {0, 1, 1, 0};
        for (int epoch = 0; epoch < 50000; epoch++) {
            for (int i = 0; i < inputs.length; i++) train(inputs[i], outputs[i]);
        }

        // TÜRKÇE GÖRSEL PENCERE TASARIMI
        JFrame frame = new JFrame("Hafızalı TR Canlı Bilgi & XOR Yapay Zeka");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 550);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(245, 247, 250));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bottomPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Ara ve Öğren");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(46, 204, 113));
        sendButton.setForeground(Color.WHITE);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        chatArea.append("[Sistem]: Çekirdek Yapay Sinir Ağı başarıyla eğitildi.\n");
        chatArea.append("[Sistem]: '" + HAFIZA_DOSYASI + "' hafıza katmanı aktif edildi.\n");
        chatArea.append("[AI]: Merhaba! Artık öğrendiğim her şeyi yerel hafızama kaydediyorum.\n");
        chatArea.append("[AI]: Merak ettiğiniz bir şeyi yazın (Örn: 'youtube', 'bilim', 'teknoloji'). Önce hafızama bakacağım, yoksa internetten öğrenip kaydedeceğim!\n\n");

        ActionListener sendAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> processMessage()).start();
            }
        };

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        frame.setVisible(true);
    }

    private static void processMessage() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) return;

        chatArea.append("Siz: " + userInput + "\n");
        inputField.setText("");
        sendButton.setEnabled(false);

        // Arama terimini temizle
        String cleanQuery = userInput.toLowerCase()
                .replace("nedir", "")
                .replace("kimdir", "")
                .replace("?", "")
                .trim();

        chatArea.append("[AI]: Nöronlar taranıyor, '" + cleanQuery + "' hafızada var mı bakılıyor...\n");

        String liveData = "";
        boolean hafizadanAlindi = false;

        // 1. ADIM: ÖNCE BEYİN.TXT DOSYASINDAN (HAFIZADAN) KONTROL ET
        String hafizaSonuc = aramaHafizadan(cleanQuery);
        if (hafizaSonuc != null) {
            liveData = "[Hafızadan Okundu]: " + hafizaSonuc;
            hafizadanAlindi = true;
        } else {
            // 2. ADIM: HAFIZADA YOKSA İNTERNETTEN ÇEK
            chatArea.append("[AI]: Kavram hafızada bulunamadı. İnternet ağına bağlanılıyor...\n");
            liveData = fetchTurkishData(cleanQuery);
            
            // Eğer internetten başarılı bilgi geldiyse beyin.txt'ye KAYDET
            if (!liveData.startsWith("[Hata]")) {
                String sadeceAçıklama = liveData.replace("[Türkçe Açıklama]: ", "");
                hafizayaKaydet(cleanQuery, sadeceAçıklama);
                chatArea.append("[Sistem]: Yeni bilgi '" + HAFIZA_DOSYASI + "' dosyasına başarıyla işlendi ve öğrenildi!\n");
            }
        }
        
        // Eğer veri başarıyla geldiyse (veya hafızadaysa) internet sinyali 1.0, hata varsa 0.0
        double internetSignal = liveData.startsWith("[Hata]") ? 0.0 : 1.0;

        // Kullanıcı kelime uzunluğu çift ise 1.0, tek ise 0.0
        double userSignal = (userInput.length() % 2 == 0) ? 1.0 : 0.0;

        // XOR MATEMATİKSEL ANALİZİ
        double[] aiInput = {userSignal, internetSignal};
        double prediction = predict(aiInput);
        long finalDecision = Math.round(prediction);

        // PENCEREYE RAPORU YAZDIRMA
        chatArea.append("\n================= YAPAY ZEKA BİLGİ ÇIKTISI =================\n");
        chatArea.append(liveData + "\n");
        chatArea.append("==================================================================\n");
        chatArea.append("--- HİBRİT XOR ANALİZ RAPORU ---\n");
        chatArea.append("> Veri Kaynağı: " + (hafizadanAlindi ? "LOKAL BEYİN (BEYİN.TXT)" : "CANLI İNTERNET") + "\n");
        chatArea.append("> Kelime Uzunluk Sinyaliniz: " + userSignal + " | Bilgi Sinyali: " + internetSignal + "\n");
        chatArea.append("> Yapay Sinir Ağı XOR Çıktısı: " + finalDecision + "\n");
        
        if (finalDecision == 1) {
            chatArea.append("[AI Yorumu]: Sinyaller XOR süzgecinden başarıyla (1) geçti.\n");
        } else {
            chatArea.append("[AI Yorumu]: Matematiksel zıtlıklar ağımda birbirini sıfırladı (0).\n");
        }
        chatArea.append("==================================================================\n\n");

        sendButton.setEnabled(true);
    }

    // BEYİN.TXT DOSYASINA YENİ BİLGİ EKLEME FONKSİYONU
    private static void hafizayaKaydet(String kavram, String aciklama) {
        try (FileWriter fw = new FileWriter(HAFIZA_DOSYASI, StandardCharsets.UTF_8, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // Kavram ve açıklamasını satır olarak ekle
            out.println(kavram + "||" + aciklama);
        } catch (IOException e) {
            System.out.println("Hafızaya yazılırken hata oluştu.");
        }
    }

    // BEYİN.TXT DOSYASINDAN KAVRAMI ARAMA FONKSİYONU
    private static String aramaHafizadan(String kavram) {
        File dosya = new File(HAFIZA_DOSYASI);
        if (!dosya.exists()) return null; // Dosya yoksa henüz hafıza oluşmamıştır

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                if (satir.contains("||")) {
                    String[] parcalar = satir.split("\\|\\|");
                    if (parcalar[0].trim().equalsIgnoreCase(kavram.trim())) {
                        return parcalar[1].trim(); // Tanımı döndür
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Hafıza okunurken hata oluştu.");
        }
        return null; // Hafızada yoksa null dön
    }

    // TÜRKÇE ENGELLERİ AŞAN WIKIPEDIA API
    private static String fetchTurkishData(String cleanQuery) {
        try {
            String encoded = URLEncoder.encode(cleanQuery, "UTF-8");
            URL url = new URL("https://tr.wikipedia.org/api/rest_v1/page/summary/" + encoded);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 YapayZekaBot/1.0");

            if (conn.getResponseCode() != 200) {
                return "[Hata]: '" + cleanQuery + "' kavramı internette bulunamadı. Daha sade aramayı deneyin.";
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
                String extract = rawJson.substring(start, end);
                extract = extract.replace("\\n", " ");
                return "[Türkçe Açıklama]: " + extract;
            }
            return "[Hata]: Format çözülemedi.";
        } catch (Exception e) {
            return "[Hata]: İnternet bağlantı kesintisi.";
        }
    }

    // YAPAY SİNİR AĞI MATEMATİĞİ
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
        for (int i = 0; i < 4; i++) output += hidden[i] * w2[i]                              
return sigmoid(output + b2);
    } // predict metodunun kapanışı

    // Render'ın programı kapatmasını önleyecek koruma bloğu
    static {
    private static double predict(double[] input) {
        double[] hidden = new double[4];
        for (int i = 0; i < 4; i++) hidden[i] = sigmoid(input[0] * w1[0][i] + input[1] * w1[1][i] + b1[i]);
        double output = 0;
        for (int i = 0; i < 4; i++) output += hidden[i] * w2[i];
        return sigmoid(output + b2);
    } // predict metodunun kapanışı

    // Render'ın programı kapatmasını önleyecek koruma bloğu
    static {
        Thread worker = new Thread( () -> {
            while (true) {
                try {
                    Thread.sleep(3600000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        worker.setDaemon(false);
        worker.start();
    }
} // Sınıfın (XorYapayZeka) en son kapanış parantezi

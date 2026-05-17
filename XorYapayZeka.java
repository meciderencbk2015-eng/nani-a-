import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;

public class XorYapayZeka {
    private static double[][] w1 = { {0.5, 0.2, 0.1, 0.7}, {0.3, 0.8, 0.4, 0.6} };
    private static double[] b1 = {0.1, 0.3, 0.2, 0.5};
    private static double[] w2 = {0.2, 0.6, 0.1, 0.8};
    private static double b2 = 0.4;
    
    private static final String HAFIZA_DOSYASI = "beyin.txt";

    public static void main(String[] args) {
        System.out.println("Yapay Zeka Hibrit Model Aktif...");
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

    // Render engelini aşmak için tarayıcı taklidi yapan gelişmiş internet metodu
    private static String internettenVeriCek(String urlAdresi) throws Exception {
        URL url = new URL(urlAdresi);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        // KRİTİK: Render engellerini aşmak için sunucuyu gerçek bir tarayıcı gibi gösteriyoruz
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Sunucu hata kodu döndürdü: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder icerik = new StringBuilder();
        String satir;
        
        int sayac = 0;
        // İnternet sayfasındaki ilk 5 satırı temizleyerek çekiyoruz
        while ((satir = in.readLine()) != null && sayac < 5) {
            String temizSatir = satir.trim();
            if (!temizSatir.isEmpty()) {
                icerik.append(temizSatir).append(" ");
                sayac++;
            }
        }
        in.close();
        conn.disconnect();
        
        return icerik.toString().trim();
    }

    // Hem internetten gelen hem de elle yazılan verileri beyin.txt'ye ekler
    private static synchronized void hafizayaKaydet(String veri) {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            BufferedWriter writer = new BufferedWriter(new FileWriter(dosya, true));
            writer.write(veri);
            writer.newLine();
            writer.close();
            System.out.println("Veri basariyla beyin.txt dosyasina islendi.");
        } catch (Exception e) {
            System.out.println("Hafizaya yazma hatasi: " + e.getMessage());
        }
    }

    // beyin.txt dosyasında biriken her şeyi okur
    private static synchronized String hafizayiOku() {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            if (!dosya.exists() || dosya.length() == 0) {
                return "Hafıza şu an boş. Bilgi yazarak veya link göndererek beni eğitin!";
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(dosya));
            StringBuilder toplamHafiza = new StringBuilder();
            String satir;
            while ((satir = reader.readLine()) != null) {
                toplamHafiza.append("- ").append(satir).append("<br>");
            }
            reader.close();
            return toplamHafiza.toString();
        } catch (Exception e) {
            return "Hafıza okunurken hata oluştu: " + e.getMessage();
        }
    }

    static {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Gelişmiş Kullanıcı Arayüzü
            server.createContext("/", exchange -> {
                String html = "<!DOCTYPE html>"
                    + "<html lang='tr'>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "    <title>Yapay Zeka Gelişmiş Destek Merkezi</title>"
                    + "    <style>"
                    + "        body { font-family: 'Segoe UI', sans-serif; background-color: #1a1c23; color: #fff; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 100vh; }"
                    + "        .chat-container { width: 100%; max-width: 650px; background: #222531; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.3); overflow: hidden; display: flex; flex-direction: column; height: 550px; border: 1px solid #2d3142; }"
                    + "        .chat-header { background: #2b2f41; padding: 15px 20px; font-size: 16px; font-weight: bold; border-bottom: 1px solid #343951; display: flex; justify-content: space-between; align-items: center; }"
                    + "        .chat-messages { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; }"
                    + "        .message { max-width: 75%; padding: 12px 16px; border-radius: 8px; font-size: 14px; line-height: 1.5; }"
                    + "        .message.ai { background: #2e344a; align-self: flex-start; color: #e2e8f0; border-bottom-left-radius: 2px; }"
                    + "        .message.user { background: #3b4261; align-self: flex-end; color: #fff; border-bottom-right-radius: 2px; }"
                    + "        .chat-input-area { padding: 15px; background: #1e212c; border-top: 1px solid #2d3142; display: flex; gap: 10px; }"
                    + "        input { flex: 1; background: #2a2e3f; border: 1px solid #3b4261; padding: 12px; border-radius: 6px; color: #fff; font-size: 14px; outline: none; }"
                    + "        button { background: #4c51bf; border: none; color: #fff; padding: 12px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; }"
                    + "        button:hover { background: #5a67d8; }"
                    + "        .hafiza-btn { background: #e53e3e; border: none; color: white; padding: 6px 12px; font-size: 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }"
                    + "        .hafiza-btn:hover { background: #c53030; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='chat-container'>"
                    + "        <div class='chat-header'>"
                    + "            <span>🤖 Yapay Zeka (Çift Yönlü Hafıza Aktif)</span>"
                    + "            <button class='hafiza-btn' onclick='hafizayiGoster()'>🧠 Hafızayı Oku</button>"
                    + "        </div>"
                    + "        <div class='chat-messages' id='chatBox'>"
                    + "            <div class='message ai'>Mükemmel! Artık sistem hem internetten veri çekebilir hem de yazdıklarınızı kaydedebilir.<br><br><b>Kullanım Rehberi:</b><br>1. Sayı Tahmini: <code>1 0</code><br>2. Manuel Bilgi Öğretme: Direkt cümlenizi yazın.<br>3. İnternetten Bilgi Çekme: Doğrudan internet linki yapıştırın (Örn: <code>https://...</code>).</div>"
                    + "        </div>"
                    + "        <div class='chat-input-area'>"
                    + "            <input type='text' id='userInput' placeholder='Cümle yazın, link yapıştırın veya sayı girin...' onkeydown='if(event.key===\"Enter\") sendMessage()'>"
                    + "            <button onclick='sendMessage()'>Gönder</button>"
                    + "        </div>"
                    + "    </div>"
                    + "    <script>"
                    + "        function sendMessage() {"
                    + "            let input = document.getElementById(\"userInput\");"
                    + "            let text = input.value.trim();"
                    + "            if(!text) return;"
                    + "            "
                    + "            let chatBox = document.getElementById(\"chatBox\");"
                    + "            chatBox.innerHTML += `<div class='message user'>${text}</div>`;"
                    + "            input.value = \"\";"
                    + "            chatBox.scrollTop = chatBox.scrollHeight;"
                    + "            "
                    + "            fetch(\"/predict?msg=\" + encodeURIComponent(text))"
                    + "                .then(res => res.text())"
                    + "                .then(data => {"
                    + "                    chatBox.innerHTML += `<div class='message ai'>${data}</div>`;"
                    + "                    chatBox.scrollTop = chatBox.scrollHeight;"
                    + "                });"
                    + "        }"
                    + "        function hafizayiGoster() {"
                    + "            fetch(\"/predict?msg=HAFIZA_OKU\")"
                    + "                .then(res => res.text())"
                    + "                .then(data => {"
                    + "                    let chatBox = document.getElementById(\"chatBox\");"
                    + "                    chatBox.innerHTML += `<div class='message ai'>🧠 <b>Mevcut beyin.txt İçeriği:</b><br>${data}</div>`;"
                    + "                    chatBox.scrollTop = chatBox.scrollHeight;"
                    + "                });"
                    + "        }"
                    + "    </script>"
                    + "</body>"
                    + "</html>";

                byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            });

            // Gelen İstek Mekanizması
            server.createContext("/predict", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response = "İşlem anlaşılamadı.";
                
                if (query != null && query.contains("msg=")) {
                    try {
                        String msg = URLDecoder.decode(query.split("msg=")[1], StandardCharsets.UTF_8.name()).trim();
                        
                        if (msg.equals("HAFIZA_OKU")) {
                            response = hafizayiOku();
                        } else if (msg.startsWith("http://") || msg.startsWith("https://")) {
                            // Link geldiyse internetten çekmeyi dene
                            try {
                                String internetVerisi = internettenVeriCek(msg);
                                hafizayaKaydet("[İnternet Verisi]: " + internetVerisi);
                                response = "🌐 <b>İnternetten veri başarıyla çekildi ve beyin.txt'ye kaydedildi!</b><br><br><i>Çekilen Metin:</i> " + internetVerisi;
                            } catch (Exception e) {
                                response = "❌ İnternetten veri çekilirken Render engeli aşılamadı: " + e.getMessage();
                            }
                        } else {
                            String[] parts = msg.split("\\s+");
                            // Sayı kontrolü
                            if (parts.length == 2 && parts[0].matches("-?\\d+(\\.\\d+)?") && parts[1].matches("-?\\d+(\\.\\d+)?")) {
                                double in1 = Double.parseDouble(parts[0]);
                                double in2 = Double.parseDouble(parts[1]);
                                double result = predict(new double[]{in1, in2});
                                response = "Girdiler: (" + in1 + ", " + in2 + ") <br>XOR Tahmini: <b>" + String.format("%.4f", result) + "</b>";
                            } else {
                                // Normal metin geldiyse elle kaydet
                                hafizayaKaydet("[Kullanıcı Notu]: " + msg);
                                response = "✍️ Yazdığınız bu bilgi başarıyla <b>beyin.txt</b> dosyasına eklendi!";
                            }
                        }
                    } catch (Exception e) {
                        response = "Veri işlenirken beklenmedik hata.";
                    }
                }

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            });

            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }
}

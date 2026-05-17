import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
        System.out.println("Yapay Zeka Modeli Aktif...");
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

    // Gönderilen Türkçe metni doğrudan beyin.txt dosyasına kaydeder
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
                return "Hafıza şu an boş. Alttaki kutudan kelimeler yazarak beni eğitin!";
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

            // Chat Arayüzü (HTML & CSS)
            server.createContext("/", exchange -> {
                String html = "<!DOCTYPE html>"
                    + "<html lang='tr'>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "    <title>Yapay Zeka Destek Merkezi</title>"
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
                    + "            <span>🤖 Yapay Zeka (Doğrudan Hafıza Sistemi)</span>"
                    + "            <button class='hafiza-btn' onclick='hafizayiGoster()'>🧠 Hafızayı Oku</button>"
                    + "        </div>"
                    + "        <div class='chat-messages' id='chatBox'>"
                    + "            <div class='message ai'>Sistem güncellendi! Render engeli kaldırıldı.<br><br><b>Nasıl Kullanılır?</b><br>1. Sayı tahmini için iki sayı girin: <code>1 0</code><br>2. Beyne bilgi öğretmek için direkt cümlenizi yazın (Örn: <code>Araba tekerlekli bir araçtır</code>).</div>"
                    + "        </div>"
                    + "        <div class='chat-input-area'>"
                    + "            <input type='text' id='userInput' placeholder='Sayı girin veya beyne öğreteceğiniz cümleyi yazın...' onkeydown='if(event.key===\"Enter\") sendMessage()'>"
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

            // Gelen İstekleri İşleyen Uç
            server.createContext("/predict", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response = "Hata oluştu.";
                
                if (query != null && query.contains("msg=")) {
                    try {
                        String msg = URLDecoder.decode(query.split("msg=")[1], StandardCharsets.UTF_8.name()).trim();
                        
                        if (msg.equals("HAFIZA_OKU")) {
                            response = hafizayiOku();
                        } else {
                            String[] parts = msg.split("\\s+");
                            // Eğer 2 tane sayı girildiyse XOR tahmini yap
                            if (parts.length == 2 && parts[0].matches("-?\\d+(\\.\\d+)?") && parts[1].matches("-?\\d+(\\.\\d+)?")) {
                                double in1 = Double.parseDouble(parts[0]);
                                double in2 = Double.parseDouble(parts[1]);
                                double result = predict(new double[]{in1, in2});
                                response = "Girdiler: (" + in1 + ", " + in2 + ") <br>XOR Çıktısı: <b>" + String.format("%.4f", result) + "</b>";
                            } else {
                                // Sayı girilmediyse, yazılan metni DOĞRUDAN beyin.txt'ye kaydet!
                                hafizayaKaydet(msg);
                                response = "✍️ Yazdığınız bu bilgi başarıyla <b>beyin.txt</b> dosyasına kaydedildi ve hafızaya alındı!";
                            }
                        }
                    } catch (Exception e) {
                        response = "Metin işlenirken bir sorun oluştu.";
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
            System.out.println("Sunucu aktif.");
        } catch (Exception e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }
}

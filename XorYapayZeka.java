import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;

public class XorYapayZeka {
    // Yapay zeka ağırlık ve bias tanımlamaları
    private static double[][] w1 = { {0.5, 0.2, 0.1, 0.7}, {0.3, 0.8, 0.4, 0.6} };
    private static double[] b1 = {0.1, 0.3, 0.2, 0.5};
    private static double[] w2 = {0.2, 0.6, 0.1, 0.8};
    private static double b2 = 0.4;

    public static void main(String[] args) {
        System.out.println("Yapay Zeka Modeli Baslatiliyor...");
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

    // Render uyumlu Web Sunucusu ve Chat Arayüzü
    static {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Ana Sayfa (Görseldeki Chat Arayüzü)
            server.createContext("/", exchange -> {
                String html = "<!DOCTYPE html>"
                    + "<html lang='tr'>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "    <title>Yapay Zeka Destek Merkezi</title>"
                    + "    <style>"
                    + "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #1a1c23; color: #fff; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 100vh; }"
                    + "        .chat-container { width: 100%; max-width: 650px; background: #222531; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.3); overflow: hidden; display: flex; flex-direction: column; height: 550px; border: 1px solid #2d3142; }"
                    + "        .chat-header { background: #2b2f41; padding: 15px 20px; font-size: 16px; font-weight: bold; border-bottom: 1px solid #343951; display: flex; align-items: center; gap: 10px; }"
                    + "        .chat-messages { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; }"
                    + "        .message { max-width: 75%; padding: 12px 16px; border-radius: 8px; font-size: 14px; line-height: 1.5; }"
                    + "        .message.ai { background: #2e344a; align-self: flex-start; color: #e2e8f0; border-bottom-left-radius: 2px; }"
                    + "        .message.user { background: #3b4261; align-self: flex-end; color: #fff; border-bottom-right-radius: 2px; }"
                    + "        .chat-input-area { padding: 15px; background: #1e212c; border-top: 1px solid #2d3142; display: flex; gap: 10px; }"
                    + "        input { flex: 1; background: #2a2e3f; border: 1px solid #3b4261; padding: 12px; border-radius: 6px; color: #fff; font-size: 14px; outline: none; }"
                    + "        input:focus { border-color: #5a67d8; }"
                    + "        button { background: #4c51bf; border: none; color: #fff; padding: 12px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; transition: background 0.2s; }"
                    + "        button:hover { background: #5a67d8; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='chat-container'>"
                    + "        <div class='chat-header'>🤖 Yapay Zeka Destek Merkezi (AI Support Center)</div>"
                    + "        <div class='chat-messages' id='chatBox'>"
                    + "            <div class='message ai'>Merhaba! XorYapayZeka projeniz CANLI ve hazır! Test etmek için aralarında boşluk bırakarak iki sayı yazın (Örn: 1 0).</div>"
                    + "        </div>"
                    + "        <div class='chat-input-area'>"
                    + "            <input type='text' id='userInput' placeholder='Mesajınızı buraya yazın...' onkeydown='if(event.key===\"Enter\") sendMessage()'>"
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

            // Yapay Zeka Tahmin API Ucu
            server.createContext("/predict", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response = "Lütfen geçerli iki sayı girin (Örn: 1 0)";
                
                if (query != null && query.contains("msg=")) {
                    try {
                        String msg = URLDecoder.decode(query.split("msg=")[1], StandardCharsets.UTF_8.name());
                        String[] parts = msg.split("\\s+");
                        if (parts.length >= 2) {
                            double in1 = Double.parseDouble(parts[0]);
                            double in2 = Double.parseDouble(parts[1]);
                            double result = predict(new double[]{in1, in2});
                            response = "Girdiler: (" + in1 + ", " + in2 + ") <br>Yapay Zeka Ağ Çıktısı (XOR Tahmini): <b>" + String.format("%.4f", result) + "</b>";
                        }
                    } catch (Exception e) {
                        response = "Hata: Sayıları dönüştüremedim. Format '1 0' şeklinde olmalı.";
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
            System.out.println("Arayüz sunucusu " + port + " portunda aktif.");
        } catch (Exception e) {
            System.out.println("Sunucu hatası: " + e.getMessage());
        }
    }
}

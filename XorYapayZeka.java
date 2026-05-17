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
import java.util.regex.Pattern;
import com.sun.net.httpserver.HttpServer;

public class XorYapayZeka {
    private static final String HAFIZA_DOSYASI = "beyin.txt";

    public static void main(String[] args) {
        System.out.println("Çift Yönlü Gelişmiş Zeka Köprüsü Aktif...");
        varsayilanBilgileriYukle();
    }

    // Gelişmiş İnternet Veri Çekme Motoru (User-Agent Korumalı ve HTML Ayıklayıcılı)
    private static String internettenVeriCek(String urlAdresi) {
        try {
            // Eğer link düz github içeriyorsa raw haline otomatik dönüştürerek 404'ü engelliyoruz
            if (urlAdresi.contains("github.com") && !urlAdresi.contains("raw.githubusercontent.com")) {
                urlAdresi = urlAdresi.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/");
            }

            URL url = new URL(urlAdresi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            // Sitelerin bota karşı korumasını (404/403 hatalarını) aşmak için tarayıcı taklidi yapıyoruz
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "❌ İnternetten veri çekilirken Render/Sunucu engeli aşılamadı: Sunucu hata kodu döndürdü: " + responseCode;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String satir;
            StringBuilder hamIcerik = new StringBuilder();
            while ((satir = in.readLine()) != null) {
                hamIcerik.append(satir).append(" ");
            }
            in.close();

            // Gelen içeriği temizleme aşaması
            String htmlMetni = hamIcerik.toString();
            
            // 1. Script ve Stil etiketlerini içindekilerle birlikte komple uçuruyoruz
            htmlMetni = htmlMetni.replaceAll("<script[^>]*?>[\\s\\S]*?</script>", "");
            htmlMetni = htmlMetni.replaceAll("<style[^>]*?>[\\s\\S]*?</style>", "");
            
            // 2. Tüm HTML etiketlerini temizliyoruz
            htmlMetni = htmlMetni.replaceAll("<[^>]*>", "");
            
            // 3. HTML boşluk kodlarını ve gereksiz boşlukları düzenliyoruz
            htmlMetni = htmlMetni.replace("&nbsp;", " ")
                                 .replace("&amp;", "&")
                                 .replace("&lt;", "<")
                                 .replace("&gt;", ">")
                                 .replaceAll("\\s+", " ").trim();

            // Eğer içerik çok uzunsa sistemi yormamak için ilk 300 karakteri cımbızlıyoruz
            if (htmlMetni.length() > 300) {
                htmlMetni = htmlMetni.substring(0, 297) + "...";
            }

            if (htmlMetni.isEmpty()) {
                return "⚠️ Bağlantı kuruldu fakat sayfadan okunabilir temiz bir metin içeriği alınamadı.";
            }

            return htmlMetni;

        } catch (Exception e) {
            return "❌ Bağlantı hatası oluştu: " + e.getMessage();
        }
    }

    private static String hafizadaAra(String arananKelime) {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            if (!dosya.exists()) return null;

            BufferedReader reader = new BufferedReader(new FileReader(dosya, StandardCharsets.UTF_8));
            String satir;
            while ((satir = reader.readLine()) != null) {
                if (satir.toLowerCase().contains(arananKelime.toLowerCase()) && satir.contains(":")) {
                    reader.close();
                    return satir.substring(satir.indexOf(":") + 1).trim();
                }
            }
            reader.close();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static synchronized void hafizayaKaydet(String veri) {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            BufferedWriter writer = new BufferedWriter(new FileWriter(dosya, StandardCharsets.UTF_8, true));
            writer.write(veri);
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            System.out.println("Kayıt hatası: " + e.getMessage());
        }
    }

    private static synchronized String hafizayiOku() {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            if (!dosya.exists() || dosya.length() == 0) {
                return "Hafıza şu an boş.";
            }
            BufferedReader reader = new BufferedReader(new FileReader(dosya, StandardCharsets.UTF_8));
            StringBuilder toplamHafiza = new StringBuilder();
            String satir;
            while ((satir = reader.readLine()) != null) {
                toplamHafiza.append("- ").append(satir).append("<br>");
            }
            reader.close();
            return toplamHafiza.toString();
        } catch (Exception e) {
            return "Hafıza okuma hatası.";
        }
    }

    private static void varsayilanBilgileriYukle() {
        File dosya = new File(HAFIZA_DOSYASI);
        if (!dosya.exists() || dosya.length() == 0) {
            hafizayaKaydet("Araba: Genellikle dört tekerlekli, motorlu kara ulaşım aracıdır.");
            hafizayaKaydet("Proje: Başarıyla sohbet ve internet yeteneğine kavuşturulmuş yerli yapay zekadır.");
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
                    + "    <title>Yapay Zeka Bilgi Motoru</title>"
                    + "    <style>"
                    + "        body { font-family: 'Segoe UI', sans-serif; background-color: #1a1c23; color: #fff; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 100vh; }"
                    + "        .chat-container { width: 100%; max-width: 650px; background: #222531; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.3); overflow: hidden; display: flex; flex-direction: column; height: 550px; border: 1px solid #2d3142; }"
                    + "        .chat-header { background: #2b2f41; padding: 15px 20px; font-size: 16px; font-weight: bold; border-bottom: 1px solid #343951; display: flex; justify-content: space-between; align-items: center; }"
                    + "        .chat-messages { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; }"
                    + "        .message { max-width: 85%; padding: 12px 16px; border-radius: 8px; font-size: 14px; line-height: 1.5; }"
                    + "        .message.ai { background: #2e344a; align-self: flex-start; color: #e2e8f0; border-bottom-left-radius: 2px; }"
                    + "        .message.user { background: #3b4261; align-self: flex-end; color: #fff; border-bottom-right-radius: 2px; }"
                    + "        .chat-input-area { padding: 15px; background: #1e212c; border-top: 1px solid #2d3142; display: flex; gap: 10px; }"
                    + "        input { flex: 1; background: #2a2e3f; border: 1px solid #3b4261; padding: 12px; border-radius: 6px; color: #fff; font-size: 14px; outline: none; }"
                    + "        button { background: #4c51bf; border: none; color: #fff; padding: 12px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; }"
                    + "        .hafiza-btn { background: #e53e3e; border: none; color: white; padding: 6px 12px; font-size: 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='chat-container'>"
                    + "        <div class='chat-header'>"
                    + "            <span>🤖 Yapay Zeka (Çift Yönlü Hafıza Aktif)</span>"
                    + "            <button class='hafiza-btn' onclick='hafizayiGoster()'>🧠 Hafızayı Oku</button>"
                    + "        </div>"
                    + "        <div class='chat-messages' id='chatBox'>"
                    + "            <div class='message ai'>"
                    + "                Mükemmel! Artık sistem hem internetten veri çekebilir hem de yazdıklarınızı kaydedebilir.<br><br>"
                    + "                <b>Kullanım Rehberi:</b><br>"
                    + "                1. Sayı Tahmini (XOR): Sadece sayı girin.<br>"
                    + "                2. Manuel Bilgi Öğretme: Tanımlayıcı kelime ve açıklama yazın.<br>"
                    + "                3. İnternetten Bilgi Çekme: Doğrudan internet linki yapıştırın (Örn: https://...)"
                    + "            </div>"
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

            // Akıllı Tahmin ve Karar İşleme Odası
            server.createContext("/predict", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response = "Hata.";
                
                if (query != null && query.contains("msg=")) {
                    try {
                        String msg = URLDecoder.decode(query.split("msg=")[1], StandardCharsets.UTF_8.name()).trim();
                        
                        if (msg.equals("HAFIZA_OKU")) {
                            response = hafizayiOku();
                        } 
                        // Senaryo 1: Kullanıcı link gönderdiyse internet motorunu tetikle
                        else if (msg.startsWith("http://") || msg.startsWith("https://")) {
                            String cekilenIcerik = internettenVeriCek(msg);
                            
                            if (cekilenIcerik.startsWith("❌")) {
                                response = cekilenIcerik;
                            } else {
                                response = "🌐 <b>İnternetten veri başarıyla çekildi ve beyin.txt'ye kaydedildi!</b><br><br><i>Çekilen Metin:</i><br>" + cekilenIcerik;
                                hafizayaKaydet("[Internet Verisi] (" + msg + "): " + cekilenIcerik);
                            }
                        } 
                        // Senaryo 2: Kullanıcı XOR mantığı için sayı girdiyse tahminde bulun
                        else if (Pattern.matches("\\d+", msg)) {
                            int sayi = Integer.parseInt(msg);
                            int tahmin = sayi ^ 1; // Basit XOR Yapay Sinir Ağı Mantığı
                            response = "🔢 <b>[XOR Tahmin Motoru]:</b> Girdiğiniz değer: " + sayi + " ➡️ Yapay Zeka Tahmini: " + tahmin;
                        } 
                        // Senaryo 3: Kullanıcı düz metin girdiyse önce hafızaya bak, yoksa kaydet
                        else {
                            String yerelTanim = hafizadaAra(msg);
                            if (yerelTanim != null) {
                                response = "🧠 <b>[Hafızamda Bulunan Net Bilgi]:</b> " + msg + ": " + yerelTanim;
                            } else {
                                response = "💾 Yeni bilgi öğrenildi ve hafızaya kaydedildi: " + msg;
                                hafizayaKaydet(msg);
                            }
                        }
                    } catch (Exception e) {
                        response = "İşlem hatası: " + e.getMessage();
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
            System.out.println("Başlatma Hatası: " + e.getMessage());
        }
    }
}

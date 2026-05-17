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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;

public class XorYapayZeka {
    private static final String HAFIZA_DOSYASI = "beyin.txt";

    public static void main(String[] args) {
        System.out.println("Türkçe Bilgi Motoru Aktif...");
        // İlk açılışta hafıza boş kalmasın diye temel Türkçe bilgiler yükleyelim
        varsayilanBilgileriYukle();
    }

    // İnternetten (DuckDuckGo/Vikipedi altyapısından) doğrudan NET ve TÜRKÇE bilgi çeken metod
    private static String internettenTurkceBilgiBul(String arananKelime) {
        try {
            // Türkçe karakter uyumluluğu için kelimeyi encode ediyoruz
            String urlAdresi = "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(arananKelime + " nedir", "UTF-8");
            URL url = new URL(urlAdresi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() != 200) return null;

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String satir;
            StringBuilder icerik = new StringBuilder();
            
            // Arama sonuçlarındaki ham metinleri süzüyoruz
            while ((satir = in.readLine()) != null) {
                if (satir.contains("result__snippet")) {
                    // HTML etiketlerini temizleyip saf Türkçe metni alıyoruz
                    String temizMetin = satir.replaceAll("<[^>]*>", "").trim();
                    if (temizMetin.length() > 20 && !temizMetin.contains("...")) {
                        icerik.append(temizMetin);
                        break; // İlk net tanımı bulduğumuzda duruyoruz
                    }
                }
            }
            in.close();
            conn.disconnect();

            return icerik.length() > 0 ? icerik.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // beyin.txt içinde akıllı Türkçe arama yapan motor
    private static String hafizadaTurkceAra(String arananKelime) {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            if (!dosya.exists()) return null;

            BufferedReader reader = new BufferedReader(new FileReader(dosya, StandardCharsets.UTF_8));
            String satir;
            StringBuilder bulunanCevaplar = new StringBuilder();

            while ((satir = reader.readLine()) != null) {
                // Büyük-küçük harf duyarlılığını kaldırıp kelime eşleşmesine bakıyoruz
                if (satir.toLowerCase().contains(arananKelime.toLowerCase())) {
                    bulunanCevaplar.append("• ").append(satir).append("<br>");
                }
            }
            reader.close();

            return bulunanCevaplar.length() > 0 ? bulunanCevaplar.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Gelen verileri beyin.txt'ye kaydeder
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

    // beyin.txt dosyasındaki her şeyi listeler
    private static synchronized String hafizayiOku() {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            if (!dosya.exists() || dosya.length() == 0) {
                return "Hafıza şu an boş. Kelime yazarak beni eğitin!";
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
            return "Hafıza okunurken hata oluştu.";
        }
    }

    private static void varsayilanBilgileriYukle() {
        File dosya = new File(HAFIZA_DOSYASI);
        if (!dosya.exists() || dosya.length() == 0) {
            hafizayaKaydet("Araba: Genellikle dört tekerlekli, motorlu kara ulaşım aracıdır.");
            hafizayaKaydet("Yapay Zeka: Bilgisayarların insan gibi düşünmesini ve öğrenmesini sağlayan teknolojidir.");
            hafizayaKaydet("Java: Dünya genelinde kullanılan, nesne yönelimli, güvenli bir programlama dilidir.");
        }
    }

    static {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Web Arayüzü
            server.createContext("/", exchange -> {
                String html = "<!DOCTYPE html>"
                    + "<html lang='tr'>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "    <title>Net Türkçe Yapay Zeka</title>"
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
                    + "        .hafiza-btn { background: #e53e3e; border: none; color: white; padding: 6px 12px; font-size: 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='chat-container'>"
                    + "        <div class='chat-header'>"
                    + "            <span>🤖 Net ve Türkçe Bilgi Motoru</span>"
                    + "            <button class='hafiza-btn' onclick='hafizayiGoster()'>🧠 Hafızayı Göster</button>"
                    + "        </div>"
                    + "        <div class='chat-messages' id='chatBox'>"
                    + "            <div class='message ai'>Merhaba! Bana öğrenmek istediğin herhangi bir kelimeyi yazabilirsin.<br>Örnek: <code>Araba</code> veya <code>Yapay Zeka</code> yazıp test et!</div>"
                    + "        </div>"
                    + "        <div class='chat-input-area'>"
                    + "            <input type='text' id='userInput' placeholder='Öğrenmek istediğiniz kelimeyi yazın...' onkeydown='if(event.key===\"Enter\") sendMessage()'>"
                    + "            <button onclick='sendMessage()'>Sor</button>"
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
                    + "                    chatBox.innerHTML += `<div class='message ai'>🧠 <b>Tüm Beyin Hafızası:</b><br>${data}</div>`;"
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

            // Gelen Mesajı İşleme ve Net Cevap Verme Noktası
            server.createContext("/predict", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response = "Anlaşılamadı.";
                
                if (query != null && query.contains("msg=")) {
                    try {
                        String msg = URLDecoder.decode(query.split("msg=")[1], StandardCharsets.UTF_8.name()).trim();
                        
                        if (msg.equals("HAFIZA_OKU")) {
                            response = hafizayiOku();
                        } else {
                            // 1. ADIM: Önce beyin.txt dosyasında bu kelime var mı diye bak
                            String hafizaSonucu = hafizadaTurkceAra(msg);
                            
                            if (hafizaSonucu != null) {
                                response = "🧠 <b>Hafızamda Bulunan Net Bilgi:</b><br>" + hafizaSonucu;
                            } else {
                                // 2. ADIM: Hafızada yoksa, internete çıkıp Türkçe bilgi ara!
                                response = "🔍 Bu bilgiyi hafızamda bulamadım, internetten araştırıyorum...<br>";
                                String internetSonucu = internettenTurkceBilgiBul(msg);
                                
                                if (internetSonucu != null) {
                                    // İnternetten bulduğu net Türkçe tanımı hafızaya kaydet
                                    hafizayaKaydet(msg + ": " + internetSonucu);
                                    response = "🌐 <b>İnternetten Yeni Öğrendiğim Türkçe Bilgi:</b><br>• " + internetSonucu;
                                } else {
                                    response = "❌ Üzgünüm, bu kelimeye dair internette net bir Türkçe tanım bulamadım. Kendiniz eklemek için kelimeyi ve açıklamasını yazabilirsiniz.";
                                }
                            }
                        }
                    } catch (Exception e) {
                        response = "Hata oluştu: Metin işlenemedi.";
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

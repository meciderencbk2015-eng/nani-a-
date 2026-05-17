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
        System.out.println("Gelişmiş Filtreli Zeka Köprüsü Aktif...");
        varsayilanBilgileriYukle();
    }

    private static String geminiIletisimMotoru(String kullaniciMesaji) {
        try {
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                return "⚠️ Hata: GEMINI_API_KEY bulunamadı. Lütfen Render panelinden Environment Variable olarak ekleyin.";
            }

            String urlAdresi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            URL url = new URL(urlAdresi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String temizMesaj = kullaniciMesaji.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");

            String jsonInputString = "{"
                + "\"contents\": [{"
                + "  \"role\": \"user\","
                + "  \"parts\": [{"
                + "    \"text\": \"" + temizMesaj + "\""
                + "  }]"
                + "}]"
                + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "❌ Yapay zeka şu an yoğun. Kod: " + responseCode;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String satir;
            StringBuilder response = new StringBuilder();
            while ((satir = in.readLine()) != null) {
                response.append(satir);
            }
            in.close();

            String rawJson = response.toString();
            if (rawJson.contains("\"text\": \"")) {
                String araMetin = rawJson.split("\"text\": \"")[1];
                String temizCevap = araMetin.split("\"")[0];
                temizCevap = temizCevap.replace("\\n", "<br>").replace("\\t", " ").replace("\\\"", "\"");
                return temizCevap;
            }
            return "Uygun bir cevap üretilemedi.";
        } catch (Exception e) {
            return "Bağlantı hatası: " + e.getMessage();
        }
    }

    private static String internettenVeriCek(String urlAdresi) {
        try {
            if (urlAdresi.contains("github.com") && !urlAdresi.contains("raw.githubusercontent.com")) {
                urlAdresi = urlAdresi.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/");
            }

            URL url = new URL(urlAdresi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return "❌ Siteden veri çekilemedi. Kod: " + conn.getResponseCode();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String satir;
            StringBuilder hamIcerik = new StringBuilder();
            while ((satir = in.readLine()) != null) {
                hamIcerik.append(satir).append(" ");
            }
            in.close();

            String htmlMetni = hamIcerik.toString()
                .replaceAll("<script[^>]*?>[\\s\\S]*?</script>", "")
                .replaceAll("<style[^>]*?>[\\s\\S]*?</style>", "")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ").trim();

            if (htmlMetni.length() > 200) {
                htmlMetni = htmlMetni.substring(0, 197) + "...";
            }
            return htmlMetni.isEmpty() ? "⚠️ Sayfa boş." : htmlMetni;
        } catch (Exception e) {
            return "❌ Hata: " + e.getMessage();
        }
    }

    private static String hafizadaAra(String arananKelime) {
        try {
            File dosya = new File(HAFIZA_DOSYASI);
            if (!dosya.exists()) return null;

            BufferedReader reader = new BufferedReader(new FileReader(dosya, StandardCharsets.UTF_8));
            String satir;
            while ((satir = reader.readLine()) != null) {
                if (satir.contains(":")) {
                    String anahtar = satir.split(":")[0].trim().toLowerCase();
                    if (arananKelime.toLowerCase().contains(anahtar)) {
                        reader.close();
                        return satir.substring(satir.indexOf(":") + 1).trim();
                    }
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
            if (!dosya.exists() || dosya.length() == 0) return "Hafıza şu an boş.";
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
            hafizayaKaydet("Proje: Başarıyla sohbet yeteneğine kavuşturulmuş yerli yapay zekadır.");
        }
    }

    static {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // HTML Web Arayüzü
            server.createContext("/", exchange -> {
                String html = "<!DOCTYPE html><html lang='tr'><head><meta charset='UTF-8'>"
                    + "<title>Yapay Zeka Bilgi Motoru</title>"
                    + "<style>"
                    + "body { font-family: 'Segoe UI', sans-serif; background-color: #1a1c23; color: #fff; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 100vh; }"
                    + ".chat-container { width: 100%; max-width: 650px; background: #222531; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.3); overflow: hidden; display: flex; flex-direction: column; height: 550px; border: 1px solid #2d3142; }"
                    + ".chat-header { background: #2b2f41; padding: 15px 20px; font-size: 16px; font-weight: bold; border-bottom: 1px solid #343951; display: flex; justify-content: space-between; align-items: center; }"
                    + ".chat-messages { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; }"
                    + ".message { max-width: 85%; padding: 12px 16px; border-radius: 8px; font-size: 14px; line-height: 1.5; }"
                    + ".message.ai { background: #2e344a; align-self: flex-start; color: #e2e8f0; border-bottom-left-radius: 2px; }"
                    + ".message.user { background: #3b4261; align-self: flex-end; color: #fff; border-bottom-right-radius: 2px; }"
                    + ".chat-input-area { padding: 15px; background: #1e212c; border-top: 1px solid #2d3142; display: flex; gap: 10px; }"
                    + "input { flex: 1; background: #2a2e3f; border: 1px solid #3b4261; padding: 12px; border-radius: 6px; color: #fff; font-size: 14px; outline: none; }"
                    + "button { background: #4c51bf; border: none; color: #fff; padding: 12px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; }"
                    + ".hafiza-btn { background: #e53e3e; border: none; color: white; padding: 6px 12px; font-size: 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }"
                    + "</style></head><body>"
                    + "<div class='chat-container'>"
                    + "  <div class='chat-header'><span>🤖 Akıllı Filtreli Yapay Zeka</span><button class='hafiza-btn' onclick='hafizayiGoster()'>🧠 Hafızayı Oku</button></div>"
                    + "  <div class='chat-messages' id='chatBox'>"
                    + "    <div class='message ai'>Sistem güncellendi! Artık normal sohbetler hafızayı kirletmez.<br><br><b>Öğretmek için:</b> 'Kelime: Açıklama' veya '... nedir?' şeklinde yazabilirsiniz.</div>"
                    + "  </div>"
                    + "  <div class='chat-input-area'>"
                    + "    <input type='text' id='userInput' placeholder='Cümle yazın, link yapıştırın veya sayı girin...' onkeydown='if(event.key===\"Enter\") sendMessage()'>"
                    + "    <button onclick='sendMessage()'>Gönder</button>"
                    + "  </div>"
                    + "</div>"
                    + "<script>"
                    + "function sendMessage() {"
                    + "  let input = document.getElementById('userInput'); let text = input.value.trim(); if(!text) return;"
                    + "  let chatBox = document.getElementById('chatBox'); chatBox.innerHTML += `<div class='message user'>${text}</div>`;"
                    + "  input.value = ''; chatBox.scrollTop = chatBox.scrollHeight;"
                    + "  fetch('/predict?msg=' + encodeURIComponent(text)).then(res => res.text()).then(data => {"
                    + "    chatBox.innerHTML += `<div class='message ai'>${data}</div>`; chatBox.scrollTop = chatBox.scrollHeight;"
                    + "  });"
                    + "}"
                    + "function hafizayiGoster() {"
                    + "  fetch('/predict?msg=HAFIZA_OKU').then(res => res.text()).then(data => {"
                    + "    let chatBox = document.getElementById('chatBox'); chatBox.innerHTML += `<div class='message ai'>🧠 <b>Hafıza Kayıtları:</b><br>${data}</div>`; chatBox.scrollTop = chatBox.scrollHeight;"
                    + "  });"
                    + "}"
                    + "</script></body></html>";

                byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
            });

            // Akıllı Karar Dağıtım Merkezi
            server.createContext("/predict", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response = "Hata.";

                if (query != null && query.contains("msg=")) {
                    try {
                        String msg = URLDecoder.decode(query.split("msg=")[1], StandardCharsets.UTF_8.name()).trim();

                        if (msg.equals("HAFIZA_OKU")) {
                            response = hafizayiOku();
                        }
                        // 1. FİLTRE: İnternet Linki mi?
                        else if (msg.startsWith("http://") || msg.startsWith("https://")) {
                            String cekilen = internettenVeriCek(msg);
                            if (cekilen.startsWith("❌")) {
                                response = cekilen;
                            } else {
                                response = "🌐 <b>İnternet Verisi Hafızaya Alındı:</b><br>" + cekilen;
                                hafizayaKaydet("[Internet] (" + msg + "): " + cekilen);
                            }
                        }
                        // 2. FİLTRE: XOR Sayı Tahmini mi?
                        else if (Pattern.matches("\\d+", msg)) {
                            int sayi = Integer.parseInt(msg);
                            response = "🔢 <b>[XOR Tahmini]:</b> Girdi: " + sayi + " ➡️ Çıktı: " + (sayi ^ 1);
                        }
                        // 3. FİLTRE: Kullanıcı Yeni Bir Bilgi mi Öğretiyor? (Örn: "Kalem: Yazı yazma aracı" veya "Ev nedir?")
                        else if (msg.contains(":") || msg.toLowerCase().endsWith("nedir") || msg.toLowerCase().endsWith("nedir?")) {
                            response = "💾 <b>[Yeni Bilgi Hafızaya Kaydedildi]:</b> " + msg;
                            hafizayaKaydet(msg);
                        }
                        // 4. FİLTRE: Normal Sohbet (Naber, nasılsın vb.) -> Önce hafızaya bak, yoksa Gemini'ye sor (ASLA KAYDETME)
                        else {
                            String yerelBilgi = hafizadaAra(msg);
                            if (yerelBilgi != null) {
                                response = "🧠 <b>[Hafızamdan]:</b> " + yerelBilgi;
                            } else {
                                response = geminiIletisimMotoru(msg);
                            }
                        }
                    } catch

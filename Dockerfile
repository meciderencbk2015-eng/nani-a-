FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac XorYapayZeka.java

# Render'ın "Port bulunamadı" diyerek çöktürmesini engellemek için arka planda küçük bir web dinleyicisi simüle ediyoruz
EXPOSE 8080

# Java'yı hem ekransız modda çalıştırıyoruz hem de çökmesini önlüyoruz
CMD ["java", "-Djava.awt.headless=true", "XorYapayZeka"]

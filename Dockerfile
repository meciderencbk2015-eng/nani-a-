FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac XorYapayZeka.java

# Java'ya başsız (ekransız) modda çalışmasını söylüyoruz
CMD ["java", "-Djava.awt.headless=true", "XorYapayZeka"]

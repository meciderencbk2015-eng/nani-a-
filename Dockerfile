FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac XorYapayZeka.java
CMD ["java", "XorYapayZeka"]

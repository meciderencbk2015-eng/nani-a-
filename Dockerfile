FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN javac XorYapayZekaWeb.java XorYapayZeka.java
EXPOSE 8080
CMD ["java", "XorYapayZekaWeb"]

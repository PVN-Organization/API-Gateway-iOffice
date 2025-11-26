# VXP API Gateway Docker Image
# Stage 1: Build với Maven
FROM maven:3.8-openjdk-8-slim AS builder

WORKDIR /build

# Copy pom.xml và dependencies
COPY ["JDK 1.8/SDKVXP_Example/pom.xml", "./"]
COPY ["JDK 1.8/SDKVXP_Example/lib", "./lib"]

# Copy source code
COPY ["JDK 1.8/SDKVXP_Example/src", "./src"]
COPY ["JDK 1.8/SDKVXP_Example/resources", "./resources"]

# Compile với Java 8
RUN mvn clean compile -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

# Copy compiled classes từ builder stage
COPY --from=builder /build/target/classes ./classes
COPY --from=builder /build/lib ./lib
COPY --from=builder /build/resources ./resources

# Install wget for health check
RUN apk add --no-cache wget

# Environment variables với default values
ENV GATEWAY_PORT=5002
ENV SYSTEM_ID=vxp.saas.03
ENV SECRET_KEY=A7TKG/r2gOKTEpe1MhWx6zw8O4JfzN0KkCRS6HZY81Ve
ENV VXP_ENDPOINT=http://vxpdungchung.vnpt.vn

# Organization Info (optional - nếu set thì không cần truyền qua API)
ENV ORG_ID=""
ENV ORG_IN_CHARGE=""
ENV ORG_NAME=""
ENV ORG_ADDRESS=""
ENV ORG_EMAIL=""
ENV ORG_TELEPHONE=""
ENV ORG_FAX=""
ENV ORG_WEBSITE=""

# Expose port
EXPOSE ${GATEWAY_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${GATEWAY_PORT}/api/health || exit 1

# Run gateway
ENTRYPOINT ["sh", "-c", "java -cp 'classes:lib/*' com.vpcp.gateway.SimpleVXPApiGateway ${GATEWAY_PORT} ${SYSTEM_ID} ${SECRET_KEY}"]


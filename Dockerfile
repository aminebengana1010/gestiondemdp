# ============================================================
# Gestion des Mots de Passe - Province de Safi
# Build multi-étapes : compilation Java puis image légère d'exécution
# ============================================================

# --- Étape 1 : compilation ---
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /build

# Copie des sources et des dépendances
COPY src/ src/
COPY lib/ lib/
COPY web/ web/
COPY sql/ sql/

# Compilation de tous les fichiers Java
RUN find src -name "*.java" > /tmp/sources.txt \
    && javac --add-modules jdk.httpserver \
       -cp "lib/mssql-jdbc-13.4.0.jre11.jar" \
       -d /build/out \
       @/tmp/sources.txt

# --- Étape 2 : image d'exécution ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Utilisateur non-root
RUN addgroup -S app && adduser -S app -G app

# Copie des classes compilées et des ressources
COPY --from=build --chown=app:app /build/out/ /app/out/
COPY --from=build --chown=app:app /build/lib/ /app/lib/
COPY --from=build --chown=app:app /build/web/ /app/web/
COPY --from=build --chown=app:app /build/sql/ /app/sql/

# Volume pour persister la clé AES (ne jamais perdre, sinon secrets indéchiffrables)
VOLUME /app/data

USER app

EXPOSE 8080

ENV PORT=8080 \
    WEB_DIR=/app/web \
    SQL_PATH=/app/sql/gestion_mots_de_passe.sql \
    AES_KEY_PATH=/app/data/.aes_key

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/login.html || exit 1

CMD ["java", "-Dfile.encoding=UTF-8", "--add-modules", "jdk.httpserver", \
     "-cp", "/app/out:/app/lib/mssql-jdbc-13.4.0.jre11.jar", \
     "ma.province.safi.passwordmanager.Main"]

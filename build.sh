#!/bin/bash
# ============================================================
# Script de compilation et d'exécution - Linux/Mac
# Gestion des Mots de Passe - Province de Safi
# ============================================================

PROJ_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJ_DIR/src"
OUT_DIR="$PROJ_DIR/out"
LIB_DIR="$PROJ_DIR/lib"
WEB_DIR="$PROJ_DIR/web"

# Trouver le fichier JDBC
JDBC_JAR=$(ls "$LIB_DIR"/mssql-jdbc-*.jar 2>/dev/null | head -1)
if [ -z "$JDBC_JAR" ]; then
    echo "[ERREUR] Fichier JDBC introuvable dans $LIB_DIR"
    exit 1
fi

echo "========================================"
echo "Gestion des Mots de Passe - Province de Safi"
echo "Compilation en cours..."
echo "========================================"

# Nettoyage
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Lister tous les fichiers Java
find "$SRC_DIR" -name "*.java" > /tmp/sources.txt

# Compilation de TOUS les fichiers Java
javac --add-modules jdk.httpserver \
    -cp "$JDBC_JAR" \
    -d "$OUT_DIR" \
    @/tmp/sources.txt

if [ $? -ne 0 ]; then
    echo "[ERREUR] Echec de la compilation"
    exit 1
fi

echo "[SUCCES] Compilation reussie."
echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║  Gestion des Mots de Passe - Province de Safi  ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║  Serveur demarre: http://localhost:8080         ║"
echo "║  Login admin:       admin / admin123            ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""
echo "========================================"
echo "Lancement de l'application..."
echo "========================================"
echo ""

java -Dfile.encoding=UTF-8 --add-modules jdk.httpserver \
    -cp "$OUT_DIR:$JDBC_JAR" \
    ma.province.safi.passwordmanager.Main

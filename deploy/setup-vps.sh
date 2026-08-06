#!/bin/bash
# ============================================================
# Préparation du VPS pour Gestion des Mots de Passe
# À exécuter UNE SEULE FOIS sur le serveur (en root ou sudo)
# ============================================================
set -euo pipefail

echo "==> Mise à jour du système"
apt-get update -y
apt-get upgrade -y

echo "==> Installation de Docker"
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker

echo "==> Installation de Docker Compose plugin"
apt-get install -y docker-compose-plugin

echo "==> Clonage du dépôt"
if [ ! -d /opt/gestiondemdp ]; then
    git clone https://github.com/aminebengana1010/gestiondemdp.git /opt/gestiondemdp
else
    cd /opt/gestiondemdp && git pull --ff-only
fi

echo "==> Création du fichier .env (à personnaliser !)"
cd /opt/gestiondemdp
if [ ! -f .env ]; then
    cp .env.example .env
    echo "   ⚠️  Éditez /opt/gestiondemdp/.env et changez DB_PASSWORD !"
fi

echo "==> Démarrage de la stack"
docker compose up -d --build

echo ""
echo "============================================"
echo "  Application : http://$(hostname -I | awk '{print $1}'):8080"
echo "  Login admin : admin / admin123"
echo "  ⚠️  N'oubliez pas de changer DB_PASSWORD"
echo "============================================"

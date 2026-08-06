# Gestion des Mots de Passe — Province de Safi  
**مدير كلمات السر — عمالة إقليم آسفي**

Application web de gestion centralisée et sécurisée des mots de passe et secrets informatiques pour la **Province de Safi** — Division **SSICTD**.  
تطبيق ويب مركزي وآمن لتسيير كلمات السر والمعلومات التقنية ديال الخدمات المعلوماتية لعمالة إقليم آسفي.

---

## Table des matières / الفهرس
1. [Présentation générale / نبذة عامة](#1-présentation-générale--نبذة-عامة)
2. [Architecture / الهندسة](#2-architecture--الهندسة)
3. [Fonctionnalités / المميزات](#3-fonctionnalités--المميزات)
4. [Explication détaillée du code / شرح الكود بالتفصيل](#4-explication-détaillée-du-code--شرح-الكود-بالتفصيل)
   - [4.1 Point d'entrée — Main.java](#41-point-dentrée---mainjava)
   - [4.2 Couche Configuration](#42-couche-configuration)
   - [4.3 Couche Modèle (Model)](#43-couche-modèle-model)
   - [4.4 Couche DAO (Data Access Object)](#44-couche-dao-data-access-object)
   - [4.5 Couche Service (Logique métier)](#45-couche-service-logique-métier)
   - [4.6 Couche Controller (Handlers HTTP)](#46-couche-controller-handlers-http)
   - [4.7 Couche Sécurité](#47-couche-sécurité)
   - [4.8 Couche Session](#48-couche-session)
   - [4.9 Utilitaires](#49-utilitaires)
   - [4.10 Frontend — Partie HTML/CSS/JS](#410-frontend--partie-htmlcssjs)
   - [4.11 Scripts de build](#411-scripts-de-build)
   - [4.12 Base de données (SQL)](#412-base-de-données-sql)
5. [Installation et démarrage / التنصيب والتشغيل](#5-installation-et-démarrage--التنصيب-والتشغيل)
6. [Sécurité / الأمان](#6-sécurité--الأمان)
7. [API REST / واجهة API](#7-api-rest--واجهة-api)

---

## 1. Présentation générale / نبذة عامة

**Français :** Cette application remplace la gestion manuelle des mots de passe (fichiers Excel, Post-its, fichiers texte) par une plateforme web chiffrée, tracée et multi-utilisateurs. Elle gère les serveurs, switches réseau, systèmes d'information internes et externes de la province de Safi.

**بالدارجة :** هاد التطبيق كايبدّل الطريقة القديمة ديال حفظ كلمات السر (Excel، ورقات، ملفات نصية) بمنصة ويب آمنة ومراقبة. كايخدم فـ عمالة آسفي باش يدير السيرڤورات والسويتشات والأنظمة المعلوماتية الداخلية والخارجية.

**Stack technique :**
- Java 17+ (JDK) avec serveur HTTP intégré `com.sun.net.httpserver`
- Microsoft SQL Server (via JDBC driver)
- Frontend HTML/CSS/JS vanilla (sans framework)
- Chiffrement AES-256-GCM pour les secrets / تشفير الأسرار
- Hachage PBKDF2-HMAC-SHA256 pour les mots de passe / تشفير كلمات السر
- Server-Sent Events (SSE) pour notifications temps réel / إشعارات فورية
- Export CSV et HTML (PDF via navigateur)

---

## 2. Architecture / الهندسة

```
gestiondemdp/
├── src/
│   ├── Main.java                                              # Point d'entrée / نقطة الدخول
│   ├── database/
│   │   └── ConnectionDB.java                                  # Ancienne connexion BD (non utilisée)
│   └── ma/province/safi/passwordmanager/
│       ├── config/
│       │   ├── DatabaseConnection.java                        # Connexion SQL Server
│       │   └── DatabaseInitializer.java                       # Création auto des tables
│       ├── model/                        # Modèles métier / النماذج
│       │   ├── EntiteAvecSecret.java                          # Interface pour entités avec secret
│       │   ├── CompteTechnique.java                           # Classe abstraite (id, nom, login, mdp chiffré)
│       │   ├── Serveur.java                                   # Serveur (adresseIP)
│       │   ├── SwitchReseau.java                              # Switch réseau (adresseMAC, emplacement)
│       │   ├── Systeme.java                                   # Classe abstraite (url)
│       │   ├── SystemeInterne.java                            # Système interne (idDivisionInterne)
│       │   ├── SystemeExterne.java                            # Système externe (idDivisionExterne)
│       │   ├── DivisionInterne.java                           # Division interne (nom, service)
│       │   ├── DivisionExterne.java                           # Division externe (type, sousType, caidat)
│       │   ├── TypeDivisionExterne.java                       # Enum AAL/Commune/Pashalik/District
│       │   ├── Utilisateur.java                               # Utilisateur (login, hash, sel, role)
│       │   ├── RoleUtilisateur.java                           # Enum des rôles
│       │   ├── LogAudit.java                                  # Trace d'audit
│       │   └── Notification.java                              # Notification
│       ├── dao/                           # Accès base de données / الوصول للبيانات
│       │   ├── CrudDAO.java                                   # Interface CRUD générique
│       │   ├── ServeurDAO.java                                # CRUD serveurs
│       │   ├── SwitchDAO.java                                 # CRUD switches
│       │   ├── SystemeInterneDAO.java                         # CRUD systèmes internes
│       │   ├── SystemeExterneDAO.java                         # CRUD systèmes externes
│       │   ├── DivisionDAO.java                               # CRUD divisions (internes + externes)
│       │   ├── UtilisateurDAO.java                            # CRUD utilisateurs
│       │   ├── NotificationDAO.java                           # CRUD notifications
│       │   └── AuditDAO.java                                  # CRUD audit logs
│       ├── service/                      # Logique métier / المنطق التجاري
│       │   ├── AuthService.java                               # Authentification, sessions
│       │   ├── ServeurService.java                            # Logique serveurs + chiffrement
│       │   ├── SwitchService.java                             # Logique switches + chiffrement
│       │   ├── SystemeInterneService.java                     # Logique systèmes internes
│       │   ├── SystemeExterneService.java                     # Logique systèmes externes
│       │   ├── NotificationService.java                       # Gestion notifications
│       │   ├── NotificationBroadcaster.java                   # Diffusion SSE temps réel
│       │   ├── RechercheService.java                          # Recherche globale
│       │   └── ExportService.java                             # Export CSV / HTML
│       ├── controller/                   # API REST / واجهة API
│       │   ├── LoginHandler.java                              # POST /api/login
│       │   ├── LogoutHandler.java                             # POST /api/logout
│       │   ├── SessionHandler.java                            # GET /api/session
│       │   ├── ServeurHandler.java                            # CRUD /api/serveurs
│       │   ├── SwitchHandler.java                             # CRUD /api/switches
│       │   ├── SystemeInterneHandler.java                     # CRUD /api/systemes-internes
│       │   ├── SystemeExterneHandler.java                     # CRUD /api/systemes-externes
│       │   ├── DivisionHandler.java                           # CRUD /api/divisions
│       │   ├── NotificationHandler.java                       # /api/notifications
│       │   ├── SseHandler.java                                # /api/notifications/sse
│       │   ├── AuditHandler.java                              # GET /api/audit
│       │   ├── RechercheHandler.java                          # GET /api/recherche
│       │   └── SchemaHandler.java                             # GET /api/schema
│       ├── security/                     # Sécurité / الأمان
│       │   ├── CryptoService.java                             # AES-256-GCM chiffrement/déchiffrement
│       │   ├── PasswordHasher.java                            # PBKDF2 hachage mots de passe
│       │   ├── SecurityInterceptor.java                       # Contrôle d'accès par rôle
│       │   └── CookieUtil.java                                # Gestion cookies HTTP
│       ├── session/                      # Sessions / الجلسات
│       │   ├── Session.java                                   # Objet session (token, user, role, expiration)
│       │   └── SessionManager.java                            # Gestionnaire de sessions (ConcurrentHashMap)
│       └── util/                         # Utilitaires / الأدوات المساعدة
│           ├── JsonUtil.java                                  # Générateur/parseur JSON sans bibliothèque
│           ├── ResponseUtil.java                              # Envoi réponses HTTP (json, html, csv, fichier)
│           ├── StaticFileHandler.java                         # Service fichiers statiques (HTML/CSS/JS/images)
│           └── AfficherTables.java                            # Utilitaire d'affichage des tables BD
├── web/                                    # Frontend / الواجهة الأمامية
│   ├── index.html                         # Tableau de bord
│   ├── login.html                         # Page de connexion
│   ├── serveurs.html                      # Gestion serveurs
│   ├── switches.html                      # Gestion switches
│   ├── systemes-internes.html             # Gestion systèmes internes
│   ├── systemes-externes.html             # Gestion systèmes externes
│   ├── divisions.html                     # Gestion divisions
│   ├── notifications.html                 # Centre notifications
│   ├── audit.html                         # Journal d'audit
│   ├── css/style.css                      # Style global + dark mode
│   ├── js/api.js                          # Client API + SSE + utilitaires UI
│   └── img/logo-province.svg              # Logo Province de Safi
├── sql/gestion_mots_de_passe.sql          # Script SQL de création BD
├── lib/mssql-jdbc-13.4.0.jre11.jar        # Pilote JDBC SQL Server
├── build.sh                               # Script compilation Linux/Mac
├── build.bat                              # Script compilation Windows
├── .gitignore                             # Fichiers ignorés par Git
├── .aes_key                               # Clé AES générée automatiquement (NE PAS COMMIT)
├── gestiondemdp.iml                       # Configuration IntelliJ IDEA
└── README.md
```

---

## 3. Fonctionnalités / المميزات

| Français | بالدارجة |
|---|---|
| **Gestion des serveurs** — Ajout, modification, suppression avec mots de passe chiffrés | **تسيير السيرڤورات** — زيد، بدّل، حيد مع كلمات السر مشفرة |
| **Gestion des switches réseau** — Inventaire + mots de passe des équipements | **تسيير السويتشات** — المخزون + كلمات السر ديال المعدات الشبكية |
| **Systèmes internes** — Applications internes liées aux divisions internes | **الأنظمة الداخلية** — التطبيقات الداخلية ديال الأقسام |
| **Systèmes externes** — Applications des divisions territoriales (AAL, Commune) | **الأنظمة الخارجية** — تطبيقات الجماعات الترابية والقيادات |
| **Divisions** — Internes (SSICTD) et Externes (AAL/Commune/Pashalik/District) | **الأقسام** — الداخلية والخارجية |
| **Recherche globale** — Unifiée dans tous les équipements | **البحث العام** — موحد فجميع المعدات والأنظمة |
| **Export CSV / PDF** — Données avec mots de passe en clair | **التصدير** — CSV و HTML مع كلمات السر بحال ما هي |
| **Notifications temps réel** — Alertes SSE push | **إشعارات فورية** — تنبيهات عبر SSE |
| **Audit complet** — Toutes les actions tracées | **سجل المراقبة** — جميع العمليات مسجلة |
| **Contrôle d'accès** — 3 rôles : Admin, Agent SSICTD, Consultation | **الصلاحيات** — 3 أدوار: مدير، وكيل، مستشار |
| **Thème sombre/clair** — Bascule en un clic | **الوضع الليلي/النهاري** — تبديل بنقرة واحدة |

---

## 4. Explication détaillée du code / شرح الكود بالتفصيل

### 4.1 Point d'entrée — Main.java

**Fichier :** `src/ma/province/safi/passwordmanager/Main.java`

```java
// === 1. Vérification connexion BD ===
if (!DatabaseConnection.testConnection()) {
    System.err.println("[ERREUR] Impossible de se connecter à SQL Server.");
    System.exit(1);
}
```
**بالدارجة :** كايختبر الاتصال بقاعدة البيانات. إلا كان ما وصلش، البرنامج كايتوقف.
**FR :** Teste la connexion SQL Server. Si elle échoue, le programme s'arrête.

```java
// === 1.5. Création automatique des tables ===
DatabaseInitializer.initialiser();
DatabaseInitializer.mettreAJourContrainteDivisionType();
DatabaseInitializer.mettreAJourColonneService();
```
**بالدارجة :** كايصنع الجداول فقاعدة البيانات إلا ما كانتش موجودة. كايحدّث القيود (constraints) والأعمدة.
**FR :** Crée automatiquement les tables si elles n'existent pas, met à jour les contraintes et colonnes.

```java
// === 2. Injection de dépendances ===
UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
ServeurDAO serveurDAO = new ServeurDAO();  // ...etc
SessionManager sessionManager = new SessionManager();
SecurityInterceptor security = new SecurityInterceptor(sessionManager);
```
**بالدارجة :** كايصنع جميع الكلاسات (DAO, Services, Security) ويربط بينهم. هادشي كايتسمى "Injection de dépendances" يدوي.
**FR :** Crée toutes les dépendances manuellement (pas de framework Spring). Chaque DAO, Service, Handler est instancié ici.

```java
// === Clé AES persistée dans .aes_key ===
java.nio.file.Path clePath = java.nio.file.Paths.get(".aes_key");
if (java.nio.file.Files.exists(clePath)) {
    aesKey = CryptoService.base64ToCle(...);  // Charger clé existante
} else {
    aesKey = CryptoService.genererCle();     // Générer nouvelle clé
    java.nio.file.Files.write(clePath, ...); // Sauvegarder
}
```
**بالدارجة :** المفتاح ديال تشفير كلمات السر كايتحفظ فـ ملف `.aes_key`. إلا كان الملف موجود، كايجيبو. إلا لا، كايصنع واحد جديد ويحفظو. هادشي كايضمن أن كلمات السر تبقى قابلة لفك التشفير حتى بعد إعادة تشغيل السيرڤور.
**FR :** La clé AES est persistée dans `.aes_key`. Si le fichier existe, on la charge ; sinon on en génère une nouvelle. Sans ça, redémarrer le serveur rendrait tous les mots de passe indéchiffrables.

```java
// === 4. Démarrer le serveur HTTP ===
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/api/serveurs", new ServeurHandler(...));
server.createContext("/api/switches", new SwitchHandler(...));
// ... 15+ routes API + fichiers statiques ...
server.setExecutor(Executors.newFixedThreadPool(10));
server.start();
```
**بالدارجة :** كايشغّل سيرڤور HTTP على المنفذ 8080. كايسجل جميع المسارات (routes) للـ API والملفات الثابتة. `newFixedThreadPool(10)` كايخلّي السيرڤور يقدر يخدم 10 طلبات ف وقت واحد.
**FR :** Démarre un serveur HTTP pur Java (sans Tomcat ni Spring Boot). 10 threads dans le pool, routage manuel des contextes.

---

### 4.2 Couche Configuration

#### DatabaseConnection.java

**Fichier :** `src/ma/province/safi/passwordmanager/config/DatabaseConnection.java`

```java
public final class DatabaseConnection {
    private static final String URL =
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=GestionMotsDePasse_safi;" +
        "encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "sa";

    private DatabaseConnection() {}  // Empêche l'instanciation

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```
**بالدارجة :** هاد كلاس كايخدم باش يتصل بقاعدة البيانات SQL Server. `encrypt=true` و `trustServerCertificate=true` كايسمحو بالاتصال المشفر حتى مع الشهادات الذاتية (self-signed). `getConnection()` ترجع اتصال جديد كل مرة (لازم تسكرو بعد الاستخدام).
**FR :** Classe utilitaire statique. Un constructeur privé empêche l'instanciation. `getConnection()` retourne une nouvelle connexion à chaque appel. `try-with-resources` est utilisé partout pour garantir la fermeture.

#### DatabaseInitializer.java

**Fichier :** `src/ma/province/safi/passwordmanager/config/DatabaseInitializer.java`

```java
public static void initialiser() throws IOException, SQLException {
    try (Connection cn = DatabaseConnection.getConnection()) {
        if (tableExiste(cn, "Role")) {
            System.out.println("[DB] Tables déjà présentes — initialisation ignorée.");
            return;
        }
    }
    String sql = Files.readString(Paths.get("sql/gestion_mots_de_passe.sql"));
    // Supprime les blocs CREATE DATABASE / USE
    sql = sql.replaceAll("(?is)IF\\s+DB_ID...", "");
    // Découper par GO et exécuter chaque bloc
    String[] instructions = sql.split("\\bGO\\b");
    ...
}
```
**بالدارجة :** كايقرا ملف SQL، وينفّذ التعليمات واحدة بواحدة. كايتجاوز الأخطاء لي كايقولو "already exists" باش ما يعطلش البرنامج.
**FR :** Lit le fichier SQL, découpe par `GO`, exécute chaque bloc. Ignore les erreurs "already exists" pour permettre les ré-exécutions.

```java
public static void mettreAJourContrainteDivisionType() throws SQLException {
    // Supprime les anciennes données incompatibles
    st.execute("DELETE FROM dbo.DivisionExterne WHERE TypeDivision NOT IN (N'AAL', N'Commune', N'Pashalik', N'District')");
    // Ajoute la contrainte CHECK
    st.execute("ALTER TABLE dbo.DivisionExterne ADD CONSTRAINT CK_DivisionExterne_Type CHECK (...))");
}
```
**بالدارجة :** كايحدّث قيد `CHECK` فجدول DivisionExterne باش يضمن أن الأنواع只能是 AAL, Commune, Pashalik, District.
**FR :** Met à jour la contrainte CHECK qui valide les types de divisions externes.

---

### 4.3 Couche Modèle (Model)

#### EntiteAvecSecret.java — Interface

```java
public interface EntiteAvecSecret {
    int getId();
    String getNom();
    String getLogin();
}
```
**بالدارجة :** Interface كاتجمع الخصائص المشتركة بين جميع الكيانات لي عندهم كلمة سر (ID، سمية، login).
**FR :** Interface que tous les modèles avec un secret doivent implémenter pour garantir un minimum de propriétés communes.

#### CompteTechnique.java — Classe abstraite mère

```java
public abstract class CompteTechnique implements EntiteAvecSecret {
    protected int id;                          // Identifiant unique
    protected String nom;                      // Nom du compte
    protected String login;                    // Identifiant de connexion
    protected String motPasseChiffre;          // Mot de passe CHIFFRÉ (AES)
    protected String vecteurInitialisation;    // IV pour AES-GCM
    protected LocalDateTime dateDernierChangement; // Date dernier changement
    protected String motDePasseClair;          // Mot de passe DÉCHIFFRÉ (transient, pas en BD)
}
```
**بالدارجة :** هاد كلاس مجرد (abstract) فيه جميع الخصائص لي مشتركة بين السيرڤور، السويتش، والأنظمة. `motPasseChiffre` و `vecteurInitialisation` كايتخزّنو فالباز. `motDePasseClair` ما كايتخزّنش فالباز، فقط كايكون ف الذاكرة باش نعرضو فالقائمة.
**FR :** Classe abstraite mutualisant tous les champs communs. `motDePasseClair` est un champ **transient** (non persisté) utilisé uniquement lors de l'affichage des listes après déchiffrement.

#### Serveur.java

```java
public class Serveur extends CompteTechnique {
    private String adresseIP;  // Adresse IP du serveur
}
```
**بالدارجة :** السيرڤور عندو `adresseIP` زيادة على الخصائص ديال `CompteTechnique`.
**FR :** Étend CompteTechnique en ajoutant l'adresse IP.

#### SwitchReseau.java

```java
public class SwitchReseau extends CompteTechnique {
    private String adresseMAC;    // Adresse MAC
    private String emplacement;   // Emplacement physique
}
```
**بالدارجة :** السويتش عندو عنوان MAC وموضع. الباقي (id، nom، كلمة السر...) كايجيو من `CompteTechnique`.
**FR :** Switch réseau avec adresse MAC et emplacement.

#### Systeme.java — Classe abstraite

```java
public abstract class Systeme extends CompteTechnique {
    protected String url;  // URL du système
}
**
**بالدارجة :** كلاس مجرد للأنظمة (الداخلية والخارجية) لي عندهم URL زيادة على `CompteTechnique`.
**FR :** Classe abstraite pour les systèmes, ajoute le champ `url`.

#### SystemeInterne.java

```java
public class SystemeInterne extends Systeme {
    private int idDivisionInterne;  // FK → DivisionInterne
}
```
**بالدارجة :** النظام الداخلي تابع لقسم داخلي (division interne).
**FR :** Système interne lié à une division interne via `idDivisionInterne`.

#### SystemeExterne.java

```java
public class SystemeExterne extends Systeme {
    private int idDivisionExterne;  // FK → DivisionExterne
}
```
**بالدارجة :** النظام الخارجي تابع لقسم خارجي (AAL، Commune، Pashalik، District).
**FR :** Système externe lié à une division externe.

#### DivisionInterne.java

```java
public class DivisionInterne {
    private int idDivisionInterne;  // PK
    private String nomDivision;     // Nom de la division
    private String service;         // Service (optionnel)
}
```
**بالدارجة :** قسم داخلي عندو اسم وخدمة.
**FR :** Division interne (ex: "SSICTD", "Ressources Humaines").

#### DivisionExterne.java

```java
public class DivisionExterne {
    private int idDivisionExterne;
    private String nomDivision;           // Nom auto-généré
    private TypeDivisionExterne type;     // Enum: AAL, COMMUNE, PASHALIK, DISTRICT
    private String sousType;              // District 1-3, Cercle, etc.
    private String caidatNom;             // Nom du caïdat (pour Commune)

    public String genererNom() {
        if (type == AAL) return "AAL - " + sousType;
        if (type == COMMUNE) return "Commune " + sousType + " - " + caidatNom;
        if (type == PASHALIK) return "Pashalik " + sousType;
        if (type == DISTRICT) return "District " + sousType;
    }
}
```
**بالدارجة :** القسم الخارجي عندو نوع (AAL، Commune، Pashalik، District) و"sous-type" (District 1، 2، 3 للـ AAL؛ Abda، Gzoula، Hrara للـ Commune). غير الـ Commune كايتطلب caïdat. السمية كاتتولد أوتوماتيكياً.
**FR :** Division externe avec hiérarchie complète. Le nom est généré automatiquement selon le type et sous-type.

#### TypeDivisionExterne.java — Enum

```java
public enum TypeDivisionExterne {
    AAL, COMMUNE, PASHALIK, DISTRICT;

    public List<String> sousTypes() {
        return switch (this) {
            case AAL -> List.of("District 1", "District 2", "District 3");
            case COMMUNE -> List.of("Abda", "Gzoula", "Hrara");  // cercles
            case PASHALIK -> List.of();
            case DISTRICT -> List.of("District 1", "District 2", "District 3");
        };
    }
}
```
**بالدارجة :** Enum فيه جميع أنواع التقسيمات الخارجية مع قائمة الـ sous-types ديالهم.
**FR :** Enum contenant les 4 types de divisions externes et leurs sous-types respectifs.

#### Utilisateur.java

```java
public class Utilisateur {
    private int idUtilisateur;
    private String nom;          // Nom complet
    private String email;        // Email
    private String login;        // Identifiant de connexion
    private String motPasseHash; // Hash PBKDF2 du mot de passe
    private String selMotPasse;  // Sel aléatoire (16 bytes)
    private int idRole;          // 1=Admin, 2=Agent SSICTD, 3=Consultation
    private boolean estActif;    // Compte actif ou désactivé
    private LocalDateTime dateCreation;

    public String getRoleName() {
        return switch (idRole) {
            case 1 -> "Administrateur";
            case 2 -> "Agent SSICTD";
            case 3 -> "Utilisateur consultation";
            default -> "Inconnu";
        };
    }
}
```
**بالدارجة :** المستخدم عندو login و `motPasseHash` (مشفر بـ PBKDF2) و `selMotPasse` (ملح عشوائي). `getRoleName()` كاترجّع اسم الدور حسب الرقم.
**FR :** Modèle utilisateur. Le mot de passe n'est JAMAIS stocké en clair, seulement le hash PBKDF2 et le sel.

#### RoleUtilisateur.java

```java
public enum RoleUtilisateur {
    Administrateur,
    Agent_SSICTD,
    Utilisateur_consultation;
}
```
**بالدارجة :** Enum ديال الأدوار.
**FR :** Énumération des rôles.

#### Notification.java

```java
public class Notification {
    private long idNotification;      // PK (bigint)
    private int idUtilisateur;        // FK → Utilisateur
    private String message;           // Message
    private String typeNotification;  // INFORMATION, CHANGEMENT_MDP, ALERTE_ROTATION, AJOUT_COMPTE
    private boolean lu;               // Lue ou non
    private LocalDateTime dateNotification; // Date de création
    private LocalDateTime dateLecture;      // Date de lecture (nullable)
}
```
**بالدارجة :** الإشعارات كاينة فالباز ومرتبطة بـ Utilisateur. `lu` كاتعرف واش مقروءة ولا لا.
**FR :** Notification stockée en BD, liée à un utilisateur. Supporte le marquage "lu/non lu".

#### LogAudit.java

```java
public class LogAudit {
    private long idLog;              // PK (bigint)
    private String action;           // CONSULTATION, AJOUT, MODIFICATION, SUPPRESSION, EXPORT, CONNEXION...
    private String entite;           // SERVEUR, SWITCH, SYSTEME_INTERNE, SYSTEME_EXTERNE...
    private Integer idCible;         // ID de l'entité ciblée (nullable)
    private String cible;            // Nom de la cible
    private String details;          // Détails
    private String adresseIP;        // Adresse IP d'origine
    private int idUtilisateur;       // FK → Utilisateur
    private LocalDateTime dateAction; // Date de l'action
}
```
**بالدارجة :** سجل المراقبة كايسجل جميع العمليات (شنو، فوقاش، شكون، شنو التفاصيل). `action` كاتعرف نوع العملية، `entite` كاتعرف على شنهوا كانت العملية.
**FR :** Journal d'audit complet avec action, entité, détails, IP et utilisateur.

---

### 4.4 Couche DAO (Data Access Object)

Chaque DAO suit le même pattern : **PreparedStatement** (protection SQL injection), **try-with-resources** (fermeture auto), **mapping manuel** ResultSet → Objet.

**بالدارجة :** جميع DAO كايتبعو نفس النمط : PreparedStatement (حماية من SQL Injection)، try-with-resources (إغلاق تلقائي)، تحويل يدوي من ResultSet للموديل.

#### ServeurDAO.java

```java
// === AJOUTER ===
public void ajouter(Serveur s, String secretChiffre, String iv) throws SQLException {
    String sql = """
        INSERT INTO dbo.Serveur (NomServeur, AdresseIP, LoginServeur,
                                 MotPasseChiffre, VecteurInitialisation, DateDernierChangement)
        VALUES (?, ?, ?, ?, ?, SYSUTCDATETIME())
        """;
    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, s.getNom());
        ps.setString(2, s.getAdresseIP());
        ps.setString(3, s.getLogin());
        ps.setString(4, secretChiffre);  // كلمة السر مشفرة
        ps.setString(5, iv);             // IV
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) s.setId(rs.getInt(1));  // جلب ID الجديد
        }
    }
}
```
**بالدارجة :** كاتسجل سيرڤور جديد. `Statement.RETURN_GENERATED_KEYS` كايخلّيها ترجع الـ ID لي تولد. `secretChiffre` و `iv` كايجيو من `CryptoService` (مشفّرين قبل ما يوصلو لـ DAO). `SYSUTCDATETIME()` كاتحط التاريخ الحالي أوتوماتيكياً.
**FR :** Insert un serveur. Remarquez que `secretChiffre` et `iv` sont déjà chiffrés par le service — le DAO ne connaît que les données chiffrées. `RETURN_GENERATED_KEYS` récupère l'ID auto-généré.

```java
// === MODIFIER (sans mot de passe) ===
public void modifier(Serveur s) throws SQLException {
    String sql = "UPDATE dbo.Serveur SET NomServeur=?, AdresseIP=?, LoginServeur=? WHERE IdServeur=?";
    ...
}

// === MODIFIER MOT DE PASSE SEULEMENT ===
public void mettreAJourSecret(int id, String secretChiffre, String iv) throws SQLException {
    String sql = "UPDATE dbo.Serveur SET MotPasseChiffre=?, VecteurInitialisation=?, DateDernierChangement=SYSUTCDATETIME() WHERE IdServeur=?";
    ...
}
```
**بالدارجة :** فين كاينة عملية التعديل، كلمة السر ما كاتتعدّلش. `mettreAJourSecret()` كاتبدّل غير كلمة السر والـ IV والتاريخ. هادشي كايخلّي التعديل العام أسرع (ما كايحتاجش تشفير).
**FR :** Deux méthodes distinctes : `modifier()` ne touche pas au mot de passe, `mettreAJourSecret()` met à jour uniquement le secret chiffré.

```java
// === LISTER (AVEC mots de passe chiffrés) ===
public List<Serveur> lister() throws SQLException {
    List<Serveur> list = new ArrayList<>();
    String sql = "SELECT IdServeur, NomServeur, AdresseIP, LoginServeur, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.Serveur ORDER BY NomServeur";
    try (...) {
        while (rs.next()) {
            Serveur s = new Serveur();
            s.setId(rs.getInt("IdServeur"));
            s.setMotPasseChiffre(rs.getString("MotPasseChiffre"));     // مشفر
            s.setVecteurInitialisation(rs.getString("VecteurInitialisation")); // IV
            list.add(s);
        }
    }
    return list;
}
```
**بالدارجة :** كاتجيب جميع السيرڤورات. هاد الدالة دابا كاتجيب `MotPasseChiffre` و `VecteurInitialisation` (قبل ما كانت تجيبهمش). هادشي ضروري باش نقدر نعرض كلمات السر فالقائمة.
**FR :** Retourne tous les serveurs AVEC les colonnes chiffrées. Le déchiffrement est fait dans la couche Service.

#### SwitchDAO.java, SystemeInterneDAO.java, SystemeExterneDAO.java

Même pattern que ServeurDAO. Chacun avec ses colonnes spécifiques :
- Switch : `NomSwitch`, `AdresseMAC`, `Emplacement`, `LoginSwitch`, `MotPasseChiffre`, `VecteurInitialisation`
- SystèmeInterne : `NomSysteme`, `UrlSysteme`, `IdDivisionInterne`, `LoginAdmin`, `MotPasseAdminChiffre`, `VecteurInitialisation`
- SystèmeExterne : `NomSysteme`, `UrlSysteme`, `IdDivisionExterne`, `LoginSysteme`, `MotPasseChiffre`, `VecteurInitialisation`

Tous ont aussi `rechercher(String texte)` avec recherche LIKE sur nom, IP/URL, login.

**بالدارجة :** جميع DAO كايتبعو نفس النمط. كل واحد عندو الجدول ديالو والحقول الخاصة بيه.

#### DivisionDAO.java

```java
// === Gère DEUX tables : DivisionInterne ET DivisionExterne ===
public void ajouterExterne(String nom, String type, String sousType, String caidatNom) {
    String sql = "INSERT INTO dbo.DivisionExterne (NomDivision, TypeDivision, SousType, CaidatNom) VALUES (?, ?, ?, ?)";
    ...
}
public void ajouterInterne(String nom, String service) { ... }
public void supprimerExterne(int id) { ... }
public void supprimerInterne(int id) { ... }
```
**بالدارجة :** DivisionDAO كايخدم على زوج الجداول (DivisionInterne و DivisionExterne) فكلاس واحد.
**FR :** Un seul DAO pour gérer les deux tables de divisions.

#### UtilisateurDAO.java

```java
public Utilisateur trouverParLogin(String login) throws SQLException {
    // SELECT avec WHERE Login = ? (protection SQL injection)
}

public void ajouter(Utilisateur u) throws SQLException {
    // INSERT avec RETURN_GENERATED_KEYS
}

public List<Utilisateur> lister() throws SQLException {
    // SELECT sans MotPasseHash ni SelMotPasse (sécurité)
}
```
**بالدارجة :** `trouverParLogin()` كاتجيب المستخدم باش تدير المصادقة. `lister()` ما كاتجيبش `MotPasseHash` و `SelMotPasse` باش نحافظو على الأمان.
**FR :** Notez que `lister()` n'inclut pas les colonnes sensibles `MotPasseHash` et `SelMotPasse`.

#### NotificationDAO.java

```java
public void creer(int idUtilisateur, String message, String type) throws SQLException { ... }
public List<Notification> listerNonLues(int idUtilisateur) throws SQLException { ... }
public List<Notification> listerToutes(int idUtilisateur) throws SQLException { ... }
public void marquerLue(long idNotification) throws SQLException { ... }
public int compterNonLues(int idUtilisateur) throws SQLException { ... }
```
**بالدارجة :** إدارة الإشعارات: صنع، جلب (مقروءة ولا كلها)، تعليم كمقروءة، عدد الغير مقروءة.
**FR :** CRUD complet pour les notifications.

#### AuditDAO.java

```java
public void enregistrer(String action, String entite, Integer idCible, String cible,
                        int idUtilisateur, String details) throws SQLException {
    String sql = "INSERT INTO dbo.LogAudit (Action, Entite, IdCible, Cible, Details, AdresseIP, IdUtilisateur) VALUES (?, ?, ?, ?, ?, ?, ?)";
    ps.setString(6, "127.0.0.1");  // Adresse IP statique (pourrait être récupérée depuis la requête)
}

public List<LogAudit> listerDerniers(int limit) throws SQLException {
    // SELECT ORDER BY DateAction DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
}
```
**بالدارجة :** تسجيل وتصفح سجل المراقبة. `enregistrer()` كاتسجل العملية بـ 7 حقول (شنو، فين، على شكون، التفاصيل، IP، شكون). `listerDerniers()` كاتجيب آخر 100 عملية.
**FR :** Enregistrement et consultation du journal d'audit avec pagination OFFSET/FETCH.

---

### 4.5 Couche Service (Logique métier)

#### AuthService.java

```java
public Session authentifier(String login, String motDePasse) throws Exception {
    // 1. جلب المستخدم من الباز
    Utilisateur utilisateur = utilisateurDAO.trouverParLogin(login);

    // 2. التحقق من وجود المستخدم
    if (utilisateur == null) throw new SecurityException("Identifiants incorrects");

    // 3. التحقق من أن الحساب نشط
    if (!utilisateur.isActif()) throw new SecurityException("Compte désactivé");

    // 4. التحقق من كلمة السر (PBKDF2)
    boolean valide = PasswordHasher.verifier(motDePasse,
        utilisateur.getMotPasseHash(), utilisateur.getSelMotPasse());

    // 5. إنشاء جلسة
    String token = sessionManager.creerSession(...);

    // 6. تسجيل فسجل المراقبة
    auditDAO.enregistrerOld("CONNEXION", "UTILISATEUR", ...);

    return sessionManager.obtenirSession(token);
}
```
**بالدارجة :** هاد الكلاس كايخدم المصادقة. 4 خطوات : جلب المستخدم ← التحقق من الحساب ← التحقق من كلمة السر ← صنع الجلسة. كايسجل كل محاولة دخول فالسجل.
**FR :** Service d'authentification en 4 étapes. Toute connexion est enregistrée dans le journal d'audit.

```java
public void creerPremierAdministrateur() throws Exception {
    if (utilisateurDAO.lister().isEmpty()) {
        // إلا كانت القاعدة خاوية، كايصنع admin افتراضي
        PasswordHasher.HashResult hr = PasswordHasher.hacher("admin123");
        Utilisateur u = new Utilisateur();
        u.setNom("Administrateur");
        u.setLogin("admin");
        u.setMotPasseHash(hr.hash());
        u.setSelMotPasse(hr.sel());
        u.setIdRole(1);  // Administrateur
        utilisateurDAO.ajouter(u);
        System.out.println("[INIT] Administrateur créé: admin / admin123");
    }
}
```
**بالدارجة :** فاش كايبدا البرنامج لأول مرة، هاد الدالة كاتصنع حساب admin افتراضي إلا ما كان حتى حساب فالباز.
**FR :** Bootstrap : crée le compte admin par défaut si la base est vide.

#### ServeurService.java (et SwitchService, SystemeInterneService, SystemeExterneService)

```java
// === AJOUT AVEC CHIFFREMENT ===
public void ajouter(Serveur serveur, String motDePasseClair, Session session) throws Exception {
    // 1. تشفير كلمة السر
    CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(motDePasseClair);

    // 2. تخزين فالباز (مشفرة)
    serveurDAO.ajouter(serveur, cr.secretChiffre(), cr.iv());

    // 3. تسجيل فسجل المراقبة
    auditDAO.enregistrerOld("AJOUT", "SERVEUR", serveur.getId(),
        session.getIdUtilisateur(), "Ajout du serveur: " + serveur.getNom());

    // 4. إشعار
    notificationDAO.creer(session.getIdUtilisateur(),
        "Nouveau serveur ajouté: " + serveur.getNom(), "INFORMATION");

    // 5. بث للإشعارات الفورية (SSE)
    broadcaster.diffuserNotification("Nouveau serveur ajouté: " + serveur.getNom(), "INFO");
}
```
**بالدارجة :** هاد الدالة كاتجمع 5 عمليات فخطوة وحدة : تشفير ← تخزين ← تسجيل ← إشعار ← بث.
**FR :** Cette méthode chaîne 5 opérations : chiffrement → persistance → audit → notification → broadcast SSE.

```java
// === CONSULTER SECRET (AVEC VÉRIFICATION) ===
public String consulterSecret(int id, Session session) throws Exception {
    verifierDroitConsultation(session);  // غير Admin و Agent SSICTD
    Serveur s = serveurDAO.trouverParId(id);
    if (s == null) throw new IllegalArgumentException("Serveur introuvable");
    if (s.getMotPasseChiffre() == null) return null;

    auditDAO.enregistrerOld("CONSULTATION", "SERVEUR", id, ...);
    broadcaster.diffuserNotification("🔑 Secret consulté - " + s.getNom(), "AUDIT");

    return cryptoService.dechiffrer(s.getMotPasseChiffre(), s.getVecteurInitialisation());
}
```
**بالدارجة :** `verifierDroitConsultation()` كاتمنع المستخدمين لي عندهم دور "Utilisateur consultation" من مشاهدة كلمات السر. أي استشارة كاتسجل فالسجل وكايبعت إشعار فوري.
**FR :** Vérification de droit AVANT déchiffrement. Seuls Administrateur et Agent SSICTD peuvent consulter un secret.

```java
// === LISTER AVEC DÉCHIFFREMENT ===
public List<Serveur> lister() throws SQLException {
    List<Serveur> list = serveurDAO.lister();
    for (Serveur s : list) {
        try {
            if (s.getMotPasseChiffre() != null) {
                s.setMotDePasseClair(
                    cryptoService.dechiffrer(s.getMotPasseChiffre(), s.getVecteurInitialisation())
                );
            }
        } catch (Exception e) { /* تجاهل الخطأ */ }
    }
    return list;
}
```
**بالدارجة :** كاتجيب القائمة من DAO، ومن بعد كاتفك تشفير كل كلمة سر. إلا فشل فك التشفير (مثلاً المفتاح تغيّر)، كاتجاوز الخطأ وما كاتعطلش القائمة.
**FR :** Déchiffre chaque mot de passe après la récupération. Les erreurs de déchiffrement sont silencieusement ignorées pour ne pas bloquer l'affichage.

```java
private void verifierForceMotDePasse(String mdp) {
    if (mdp == null || mdp.length() < 8)
        throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
}
```
**بالدارجة :** كاتتحقق من أن كلمة السر فيها 8 حروف على الأقل قبل ما تخزنها.
**FR :** Validation du mot de passe : minimum 8 caractères.

#### NotificationService.java

```java
public class NotificationService {
    public List<Notification> listerNonLues(int idUtilisateur) { ... }
    public List<Notification> listerToutes(int idUtilisateur) { ... }
    public int compterNonLues(int idUtilisateur) { ... }
    public void marquerLue(long idNotification) { ... }
    public void creer(int idUtilisateur, String message, String type) { ... }
}
```
**بالدارجة :** طبقة رقيقة (thin layer) كاتمرّر الاستدعاءات لـ NotificationDAO.
**FR :** Couche service simple qui délègue au DAO.

#### NotificationBroadcaster.java — SSE temps réel

```java
public class NotificationBroadcaster {
    private static final NotificationBroadcaster INSTANCE = new NotificationBroadcaster();
    private final List<SseClient> clients = new CopyOnWriteArrayList<>();  // Thread-safe

    public void diffuser(String event, String data) {
        String message = "event: " + event + "\ndata: " + data + "\n\n";
        for (SseClient client : clients) {
            client.envoyer(message);  // إرسال لجميع العملاء
        }
    }

    public void diffuserNotification(String message, String type) {
        diffuser("notification", "{\"message\":\"" + escaped + "\",\"type\":\"" + type + "\"}");
    }

    public static class SseClient {
        private HttpExchange exchange;
        private volatile boolean closed = false;

        public void attacher(HttpExchange exchange) {
            // تعيين الهيدرز المناسبة لـ SSE
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.getResponseHeaders().add("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);
            // إرسال حدث "connected" للتأكيد
        }

        public void envoyer(String message) {
            synchronized(this) {
                exchange.getResponseBody().write(message.getBytes());
                exchange.getResponseBody().flush();
            }
        }
    }
}
```
**بالدارجة :** هاد الكلاس كايخدم Pattern "Singleton" (مثيل واحد فالتطبيق كامل). `CopyOnWriteArrayList` كاتحل مشكلة التوافق (concurrent) فاش كايتزادو ولا كايتحيدو العملاء (clients). `diffuserNotification()` كاتبعث إشعار لجميع العملاء المتصلين. `SseClient` كايمثّل متصفح واحد متصل.
**FR :** Pattern Singleton. `CopyOnWriteArrayList` = thread-safe pour les accès concurrents. `SseClient` = une connexion navigateur. La méthode `diffuserNotification()` envoie à TOUS les clients connectés.

#### ExportService.java

```java
public String exporterServeursCSV(Session session) throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("Nom;Adresse IP;Login;Mot de passe;Dernier changement\n");
    List<Serveur> list = serveurService.lister();
    for (Serveur s : list) {
        sb.append(s.getNom()).append(";")
          .append(s.getAdresseIP()).append(";")
          .append(s.getLogin()).append(";")
          .append(s.getMotDePasseClair()).append(";")  // كلمة السر بحال ما هي !
          .append(s.getDateDernierChangement()).append("\n");
    }
    auditDAO.enregistrerOld("EXPORT", "SERVEUR", null, session.getIdUtilisateur(), "Export CSV serveurs");
    return sb.toString();
}
```
**بالدارجة :** كاتصدّر البيانات لـ CSV مع كلمة السر بحال ما هي (مفكوكة). التصدير كايتسجل فسجل المراقبة.
**FR :** Export CSV incluant les mots de passe en clair (déchiffrés via `lister()`). L'export est tracé dans le journal d'audit.

```java
// Export HTML (imprimable / PDF)
public String exporterServeursHTML(Session session) throws Exception {
    String html = htmlPage("Serveurs",
        new String[]{"Nom", "Adresse IP", "Login", "Mot de passe", "Dernier changement"},
        rows);
    return html;
}

private String htmlPage(String titre, String[] headers, String rows) {
    // Génère une page HTML complète avec style CSS pour l'impression
    sb.append("<style>")
      .append("@media print{th{background:#1a237e!important;color:white!important;")
      .append("-webkit-print-color-adjust:exact;print-color-adjust:exact;}}")
      .append("</style>");
}
```
**بالدارجة :** كاتصدّر HTML بستيل مخصص للطباعة. المستخدم كايقدر يحفظ كـ PDF من المتصفح (`Ctrl+P`).
**FR :** Export HTML prêt pour l'impression/PDF. Les styles `@media print` garantissent un rendu correct.

#### RechercheService.java

```java
public Map<String, Object> rechercher(String texte, String type) throws Exception {
    Map<String, Object> resultats = new LinkedHashMap<>();
    if (type == null || type.equals("SERVEUR"))
        resultats.put("serveurs", dechiffrerServeurs(serveurDAO.rechercher(texte)));
    if (type == null || type.equals("SWITCH"))
        resultats.put("switches", dechiffrerSwitches(switchDAO.rechercher(texte)));
    if (type == null || type.equals("SYSTEME_INTERNE"))
        resultats.put("systemesInternes", dechiffrerSystemesInternes(systemeInterneDAO.rechercher(texte)));
    if (type == null || type.equals("SYSTEME_EXTERNE"))
        resultats.put("systemesExternes", dechiffrerSystemesExternes(systemeExterneDAO.rechercher(texte)));
    return resultats;
}
```
**بالدارجة :** كاتبحث فجميع الجداول (سيرڤورات، سويتشات، أنظام داخلية وخارجية). إلا كان `type` ماشي فاضي، كاتبحث غير فهاد النوع. النتائج كاتفك تشفيرها قبل ما ترجع.
**FR :** Recherche globale dans toutes les tables. Filtre par type si spécifié. Déchiffre les mots de passe avant de retourner.

---

### 4.6 Couche Controller (Handlers HTTP)

Tous les handlers implémentent `HttpHandler` et suivent le même pattern :

```java
public void handle(HttpExchange exchange) throws IOException {
    try {
        String methode = exchange.getRequestMethod();       // GET/POST/PUT/DELETE
        String path = exchange.getRequestURI().getPath();   // /api/serveurs/5

        // 1. Vérification des cas spéciaux (export, consulter secret)
        // 2. Switch sur la méthode HTTP
        switch (methode) { case "GET" -> lister(exchange); ... }

    } catch (SecurityException e) {
        ResponseUtil.json(exchange, 403, json);  // 403 Forbidden
    } catch (IllegalArgumentException e) {
        ResponseUtil.json(exchange, 400, json);  // 400 Bad Request
    } catch (Exception e) {
        e.printStackTrace();
        ResponseUtil.json(exchange, 500, json);  // 500 Internal
    }
}
```

**بالدارجة :** جميع المتحكمين كايتبعو نفس النمط : 1. نقرا الطريقة والمسار 2. نتحقق من الصلاحية 3. نخدم العملية 4. نعطي الجواب.

#### LoginHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    // POST فقط
    if (!"POST".equals(exchange.getRequestMethod())) {
        ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
        return;
    }
    // قراءة الجسم (body)
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
    String body = JsonUtil.lireCorps(reader);
    Map<String, String> data = JsonUtil.parseObject(body);

    String login = data.get("login");
    String motDePasse = data.get("motDePasse");

    // المصادقة
    Session session = authService.authentifier(login, motDePasse);

    // إنشاء الكوكي (الجلسة)
    CookieUtil.ajouterCookie(exchange, "SESSION_ID", session.getToken(), 1800);

    // الجواب
    String json = JsonUtil.buildObject(
        JsonUtil.jsonString("succes", "true"),
        JsonUtil.jsonString("nomUtilisateur", session.getNomUtilisateur()),
        JsonUtil.jsonString("role", session.getRole())
    );
    ResponseUtil.json(exchange, 200, json);
}
```
**بالدارجة :** `CookieUtil.ajouterCookie()` كاتصنع كوكي `SESSION_ID` مع الخيارات : HttpOnly (مايمكنش لجافاسكريبت يقراه)، Path=/ (صالح لكل المسارات)، Max-Age=1800 (30 دقيقة)، SameSite=Lax (حماية من هجمات CSRF).
**FR :** Crée un cookie de session HttpOnly (sécurisé contre les attaques XSS), SameSite=Lax (anti-CSRF), durée 30 min.

#### LogoutHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    String token = CookieUtil.extraireCookie(exchange, "SESSION_ID");
    if (token != null) authService.deconnecter(token);
    CookieUtil.supprimerCookie(exchange, "SESSION_ID");  // حذف الكوكي
    ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Déconnexion réussie\"}");
}
```
**بالدارجة :** `CookieUtil.supprimerCookie()` كايحط `Max-Age=0` باش يحذف الكوكي من المتصفح. و كايسقط الجلسة من `SessionManager`.
**FR :** Supprime le cookie (Max-Age=0) et détruit la session côté serveur.

#### SessionHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    Session session = security.verifierAuthentification(exchange);
    if (session == null) return;  // 401 déjà envoyé par SecurityInterceptor

    String json = JsonUtil.buildObject(
        JsonUtil.jsonString("succes", "true"),
        JsonUtil.jsonString("nomUtilisateur", session.getNomUtilisateur()),
        JsonUtil.jsonString("role", session.getRole())
    );
    ResponseUtil.json(exchange, 200, json);
}
```
**بالدارجة :** كاتتحقق من صحة الجلسة وترجع السمية والدور. كاتستعمل فاش كاتتحقق الصفحة من الجلسة فاش كاتحمل.
**FR :** Vérifie la validité de la session et retourne le nom d'utilisateur et le rôle.

#### ServeurHandler.java — Gestion complète CRUD

```java
private void ajouter(HttpExchange exchange) throws Exception {
    // Vérification : Admin ou Agent SSICTD فقط
    Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
    if (session == null) return;

    // قراءة البيانات
    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
    Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

    Serveur s = new Serveur();
    s.setNom(data.get("nom"));
    s.setAdresseIP(data.get("adresseIP"));
    s.setLogin(data.get("login"));
    String motDePasse = data.get("motDePasse");

    serveurService.ajouter(s, motDePasse, session);
    ResponseUtil.json(exchange, 201, ...);
}
```
**بالدارجة :** كاتزود سيرڤور جديد. `security.verifierAcces()` كاتمنع المستخدمين لي عندهم دور "Utilisateur consultation" من الإضافة.
**FR :** Seuls Administrateur et Agent SSICTD peuvent ajouter un serveur.

```java
private void modifierOuSecret(HttpExchange exchange) throws Exception {
    String path = exchange.getRequestURI().getPath();
    if (path.endsWith("/secret")) {
        // PUT /api/serveurs/5/secret → تغيير كلمة السر فقط
        serveurService.modifierMotDePasse(id, nouveauMotDePasse, session);
    } else {
        // PUT /api/serveurs/5 → تعديل المعلومات (بدون كلمة السر)
        serveurService.modifier(id, nom, adresseIP, login, session);
    }
}
```
**بالدارجة :` كاتفرّق بين تعديل المعلومات العامة وتعديل كلمة السر. المسار `/secret` كايعني تغيير كلمة السر.
**FR :** Distingue deux endpoints : `PUT /{id}` pour les infos générales, `PUT /{id}/secret` pour le mot de passe seulement.

```java
private int extraireId(String path) {
    String[] parts = path.split("/");
    // /api/serveurs/5/consulter → parts = ["", "api", "serveurs", "5", "consulter"]
    for (int i = 0; i < parts.length; i++) {
        if ("serveurs".equals(parts[i]) && i + 1 < parts.length) {
            try { return Integer.parseInt(parts[i + 1]); }
            catch (NumberFormatException e) { /* تجاهل */ }
        }
    }
    return 0;
}
```
**بالدارجة :` كاتستخرج الرقم التعريفي من المسار. تقدر تجيب الـ ID حتى من مسار معقد بحال `/api/serveurs/5/consulter`.
**FR :** Extrait l'ID depuis le chemin URL. Fonctionne même avec des chemins complexes.

#### SwitchHandler.java, SystemeInterneHandler.java, SystemeExterneHandler.java

Même pattern que ServeurHandler avec leurs noms spécifiques. Tous ont :
- `lister(), ajouter(), modifierOuSecret(), supprimer()`
- Export CSV/HTML
- Consultation de secret
- `extraireId()` qui cherche le nom correspondant

#### DivisionHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    // GET /api/divisions/internes → liste divisions internes
    // GET /api/divisions/externes → liste divisions externes
    // POST /api/divisions/internes → ajouter division interne
    // POST /api/divisions/externes → ajouter division externe
    // PUT /api/divisions/internes/{id} → modifier interne
    // PUT /api/divisions/externes/{id} → modifier externe
    // DELETE /api/divisions/internes/{id} → supprimer interne
    // DELETE /api/divisions/externes/{id} → supprimer externe
}
```
**بالدارجة :` كايدير CRUD كامل على الجدولين (داخلي وخارجي) من خلال مسار واحد `/api/divisions`. كايفرّق بين `internes` و `externes`.
**FR :** Gère les deux types de divisions via un seul handler en distinguant par le chemin.

#### NotificationHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    // GET /api/notifications → toutes les notifications
    // GET /api/notifications/compteur → compteur non lues
    // PUT /api/notifications/{id}/lire → marquer comme lue
}
```
**بالدارجة :` كاتدير 3 عمليات : جلب الإشعارات، جلب العدد، تعليم كمقروءة.
**FR :** Trois opérations : lister, compter les non-lues, marquer comme lue.

#### SseHandler.java — Server-Sent Events

```java
public void handle(HttpExchange exchange) throws IOException {
    SseClient client = NotificationBroadcaster.getInstance().ajouterClient();
    client.attacher(exchange);

    // Boucle keep-alive : كايصبر 30 ثانية ويبعث حدث keepalive
    while (!client.isClosed()) {
        Thread.sleep(30000);
        client.envoyer(": keepalive\n\n");  // كايخلّي الاتصال حيّ
    }
}
```
**بالدارجة :` هاد الكلاس كايخلّي الاتصال مفتوح بين السيرڤور والمتصفح. كل 30 ثانية كايبعت "keepalive" باش ما يسكتش الاتصال. فاش كايتولد إشعار جديد، `NotificationBroadcaster` كايبعتو لجميع المتصلين عبر هاد الاتصال.
**FR :** SSE = connexion HTTP persistante. Le serveur envoie des événements au navigateur sans que le navigateur demande. Le `keepalive` évite la fermeture par les proxies/routeurs.

#### AuditHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    // فقط Admin
    if (security.verifierRoleAdministrateur(exchange) == null) return;
    List<LogAudit> list = auditDAO.listerDerniers(100);
    ResponseUtil.json(exchange, 200, auditToJson(list));
}
```
**بالدارجة :` `auditToJson()` كاتحول القائمة لـ JSON يدوي. كاتخلي غير الـ Admin يشوف السجل.
**FR :** Réservé à l'administrateur. Retourne les 100 dernières entrées du journal d'audit.

#### RechercheHandler.java

```java
public void handle(HttpExchange exchange) throws IOException {
    Session session = security.verifierAuthentification(exchange);
    // قراءة paramètres query : ?texte=xxx&type=SERVEUR
    String query = exchange.getRequestURI().getQuery();
    for (String param : query.split("&")) {
        String[] kv = param.split("=", 2);
        switch (kv[0]) { case "texte" -> texte = val; case "type" -> type = val; }
    }
    Map<String, Object> resultats = rechercheService.rechercher(texte, type.isEmpty() ? null : type);
    ResponseUtil.json(exchange, 200, json.toString());
}
```
**بالدارجة :` كاتقرا الـ query parameters (texte و type) من الـ URL، وتبحث فجميع الجداول. `listToJson()` كاتحول النتائج لـ JSON حسب نوع كل كيان.
**FR :** Lit les paramètres de requête `?texte=...&type=...` et retourne les résultats en JSON.

#### SchemaHandler.java

```java
public void handle(HttpExchange exchange) {
    // جلب ميتاداتا قاعدة البيانات عبر DatabaseMetaData
    DatabaseMetaData meta = cn.getMetaData();
    ResultSet tables = meta.getTables("GestionMotsDePasse_safi", "dbo", null, new String[]{"TABLE"});
    // لكل جدول : الأعمدة، الـ PK، الـ FK
}
```
**بالدارجة :` كايقرا هيكلة قاعدة البيانات (الجداول، الأعمدة، المفاتيح) ويعرضها كـ JSON. مفيدة للتشخيص.
**FR :** Outil de diagnostic qui expose le schéma complet de la BD via l'API.

---

### 4.7 Couche Sécurité

#### CryptoService.java — Chiffrement AES-256-GCM

```java
public class CryptoService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;  // 16 bytes de tag d'authentification
    private static final int IV_LENGTH = 12;         // 96 bits recommandé pour GCM
    private final SecretKey secretKey;

    public ChiffrementResultat chiffrer(String texteClair) throws Exception {
        // 1. صنع IV عشوائي (12 بايت)
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // 2. تهيئة Cipher في وضع التشفير
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        // 3. تشفير
        byte[] chiffre = cipher.doFinal(texteClair.getBytes(StandardCharsets.UTF_8));

        // 4. تحويل لـ Base64 (باش يتخزن فالباز كنص)
        return new ChiffrementResultat(
            Base64.getEncoder().encodeToString(chiffre),
            Base64.getEncoder().encodeToString(iv)
        );
    }

    public String dechiffrer(String texteChiffre, String ivBase64) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(
            GCM_TAG_LENGTH, Base64.getDecoder().decode(ivBase64));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] clair = cipher.doFinal(Base64.getDecoder().decode(texteChiffre));
        return new String(clair, StandardCharsets.UTF_8);
    }
}
```
**بالدارجة :**
- `AES/GCM/NoPadding` : AES = معيار التشفير، GCM = وضع التشفير (مع المصادقة)، NoPadding = ما كايزدش حشو.
- `GCM_TAG_LENGTH = 128` : GCM كايصنع "tag" ديال 16 بايت كايتحقق من سلامة البيانات (ما تبدّلتش).
- `IV_LENGTH = 12` : الـ IV (Initialization Vector) طولو 12 بايت (96 bits)، كايتصنع عشوائياً كل مرة.
- `chiffrer()` : كايصنع IV ← كايشفر ← كايعطي `(secretChiffre, iv)` فـ Base64.
- `dechiffrer()` : كاياخذ `(secretChiffre, iv)` ← كايحل Base64 ← كايحط IV فـ GCM ← كايفك التشفير.
- **ليما GCM ?** : GCM كايفرق بين التشفير والمصادقة. إلا بدّل حتى واحد النص المشفر، GCM كايديك "Tag mismatch" وما كايحاولش يفك التشفير. هادشي كايحمي من هجمات التلاعب بالبيانات.

**FR :** AES-256-GCM = Advanced Encryption Standard (256-bit key) + Galois/Counter Mode. C'est un chiffrement **authentifié** : en sortie du chiffrement, GCM produit un "tag" qui est vérifié au déchiffrement. Si le texte chiffré a été modifié, le tag ne correspond pas et une exception `AEADBadTagException` est levée. L'IV est DIFFÉRENT à chaque chiffrement (même avec le même texte clair), ce qui empêche les attaques par répétition.

```java
public static SecretKey genererCle() throws Exception {
    KeyGenerator generator = KeyGenerator.getInstance("AES");
    generator.init(256);  // 256 bits = clé forte
    return generator.generateKey();
}
```
**بالدارجة :** كايبني مفتاح تشفير AES بطول 256 bits. هاد المفتاح كايتحفظ فـ ملف `.aes_key`.
**FR :** Génère une clé AES-256. Sauvegardée dans `.aes_key` pour persister entre les redémarrages.

#### PasswordHasher.java — Hachage PBKDF2

```java
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;  // 600 ألف دورة
    private static final int KEY_LENGTH = 256;

    public static HashResult hacher(String motDePasse) throws Exception {
        // 1. صنع ملح (sel) عشوائي ديال 16 بايت
        SecureRandom random = new SecureRandom();
        byte[] sel = new byte[16];
        random.nextBytes(sel);

        // 2. تطبيق PBKDF2 (600,000 دورة)
        KeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return new HashResult(Base64.getEncoder().encodeToString(hash),
                              Base64.getEncoder().encodeToString(sel));
    }

    public static boolean verifier(String motDePasse, String hashBase64, String selBase64) throws Exception {
        byte[] sel = Base64.getDecoder().decode(selBase64);
        KeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash).equals(hashBase64);
    }
}
```
**بالدارجة :**
- `PBKDF2WithHmacSHA256` : خوارزمية بطيئة باش تصعّب هجمات brute-force.
- `ITERATIONS = 600_000` : 600 ألف دورة. كل محاولة باش تاكد كلمة السر كاتاخد وقت طويل.
- `sel` (ملح) : 16 بايت عشوائي، مختلف لكل مستخدم. كايمنع هجمات Rainbow Tables.
- `hacher()` : كاتاخذ كلمة السر → تصنع ملح → تطبق PBKDF2 600,000 مرة → ترجع (hash, sel).
- `verifier()` : كاتاخذ كلمة السر + hash + sel → تطبق PBKDF2 → تقارن مع hash المخزن.
- **مقارنة :** كاتستعمل `.equals()` (مقارنة بسيطة) — هادشي نقاش (vulnerable à timing attack) ولكن فهاد السياق مقبول.

**FR :** PBKDF2 = Password-Based Key Derivation Function 2. L'algorithme est délibérément LENT (600 000 itérations) pour ralentir les attaques par dictionnaire. Le sel aléatoire (16 octets) est stocké en clair à côté du hash. Même si deux utilisateurs ont le même mot de passe, leurs hash seront différents.

#### SecurityInterceptor.java — Contrôle d'accès

```java
public Session verifierAcces(HttpExchange exchange, String... rolesAutorises) throws IOException {
    // 1. جلب الـ token من الكوكي
    String token = CookieUtil.extraireCookie(exchange, "SESSION_ID");

    // 2. جلب الجلسة
    Session session = sessionManager.obtenirSession(token);
    if (session == null) {
        ResponseUtil.json(exchange, 401, "{\"erreur\":\"Session invalide ou expirée\"}");
        return null;  // → القيمة null كاتوقف العملية
    }

    // 3. التحقق من الدور
    List<String> roles = Arrays.asList(rolesAutorises);
    if (roles.contains(session.getRole())) return session;

    // 4. الدور ما عندوش الصلاحية → 403
    ResponseUtil.json(exchange, 403, "{\"erreur\":\"Accès refusé. Rôle insuffisant.\"}");
    return null;
}

public Session verifierAuthentification(HttpExchange exchange) throws IOException {
    // مجرد التحقق من صحة الجلسة (أي دور)
}

public Session verifierRoleAdministrateur(HttpExchange exchange) throws IOException {
    // خاص Admin فقط
    return verifierAcces(exchange, "Administrateur");
}
```
**بالدارجة :** هاد الكلاس كايتسمى قبل ما تنفذ أي عملية حساسة.
1. يجيب الـ SESSION_ID من الكوكي
2. يجيب الجلسة من SessionManager (كايتأكد من صلاحيتها)
3. إلا كانت الجلسة ماشي صالحة → كايبعت 401 (ما وصلتيش)
4. إلا كان الدور ماشي فالقائمة → كايبعت 403 (ما عندكش الصلاحية)
5. إلا كلشي مزيان → يرجع الجلسة (العملية كاتكمّل)

`return null` هو الإشارة لـ handler باش يوقف العملية (الجواب HTTP تما بعت).

**FR :** Fonctionne comme un middleware. Vérifie session + rôle AVANT l'opération. `return null` signifie "bloquer" (la réponse HTTP a déjà été envoyée avec 401/403).

#### CookieUtil.java

```java
public static String extraireCookie(HttpExchange exchange, String nom) {
    List<String> cookies = exchange.getRequestHeaders().get("Cookie");
    // تقشير الهيدر "Cookie: SESSION_ID=abc; theme=dark"
    for (String paire : cookie.split(";\\s*")) {
        String[] cleValeur = paire.split("=", 2);
        if (cleValeur[0].equals(nom)) return cleValeur[1];
    }
    return null;
}

public static void ajouterCookie(HttpExchange exchange, String nom, String valeur, int maxAgeSecondes) {
    exchange.getResponseHeaders().add("Set-Cookie",
        nom + "=" + valeur +
        "; HttpOnly" +           // ما يمكنش لـ JS يقراه (حماية XSS)
        "; Path=/" +             // صالح لكل المسارات
        "; Max-Age=" + maxAgeSecondes +  // 1800 = 30 دقيقة
        "; SameSite=Lax");       // حماية من CSRF
}
```
**بالدارجة :** `extraireCookie()` كاتقرا هيدر Cookie وتدور على الاسم. `ajouterCookie()` كاتصنع كوكي بالخيارات :
- `HttpOnly` : جافاسكريبت ما يقدرش يقرا الكوكي (حماية من XSS)
- `Path=/` : الكوكي صالح لكل الصفحات
- `Max-Age=1800` : كاينتهي بعد 30 دقيقة
- `SameSite=Lax` : كايمنع البعث فحالة الطلب من موقع آخر (حماية من CSRF)

---

### 4.8 Couche Session

#### Session.java

```java
public class Session {
    private final String token;           // UUID عشوائي
    private final int idUtilisateur;      // ID ديال المستخدم
    private final String nomUtilisateur;  // السمية
    private final String role;            // الدور
    private final long expiration;        // وقت الانتهاء (timestamp)

    public boolean estExpiree() {
        return System.currentTimeMillis() > expiration;  // واش الوقت دابا أكبر من وقت الانتهاء؟
    }
}
```
**بالدارجة :** كايمثّل جلسة المستخدم. `expiration` هو الوقت لي كاتنتهي فيه الجلسة. فاش كايتحقق من الصلاحية، `estExpiree()` كاتقارن الوقت الحالي مع وقت الانتهاء.

#### SessionManager.java

```java
public class SessionManager {
    private static final long DUREE_SESSION_MS = 30 * 60 * 1000;  // 30 دقيقة
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();  // آمن للخيوط

    public String creerSession(int idUtilisateur, String nomUtilisateur, String role) {
        String token = UUID.randomUUID().toString();     // صنع توكن عشوائي
        long expiration = System.currentTimeMillis() + DUREE_SESSION_MS;  // وقت الانتهاء
        Session session = new Session(token, idUtilisateur, nomUtilisateur, role, expiration);
        sessions.put(token, session);  // حفظ الجلسة
        return token;
    }

    public Session obtenirSession(String token) {
        Session session = sessions.get(token);
        if (session == null || session.estExpiree()) {
            sessions.remove(token);  // تنظيف الجلسات المنتهية
            return null;
        }
        return session;
    }

    public void detruireSession(String token) {
        sessions.remove(token);
    }

    private void nettoyerSessionsExpirees() {
        sessions.values().removeIf(Session::estExpiree);  // كايتحذف جميع الجلسات المنتهية
    }
}
```
**بالدارجة :**
- `ConcurrentHashMap` : كاتحل مشكلة الوصول المتزامن (خيوط متعددة). آمنة من دون `synchronized`.
- `UUID.randomUUID()` : كايدير توكن فريد من المستحيل توقّعو (128 bits عشوائي).
- `obtenirSession()` : كاتحذف الجلسات المنتهية تلقائياً فاش كاتحاول تجيبها.
- `nettoyerSessionsExpirees()` : كاتنظف الجلسات القديمة من الذاكرة.

**FR :** Les sessions sont stockées en mémoire (pas en BD). `ConcurrentHashMap` gère la concurrence. Durée : 30 minutes. Nettoyage automatique des sessions expirées.

---

### 4.9 Utilitaires

#### JsonUtil.java — Générateur/parseur JSON sans bibliothèque

```java
public class JsonUtil {
    // === توليد JSON ===
    public static String json(String key, String value) {
        return "{\"" + escape(key) + "\":\"" + escape(value) + "\"}";
    }
    public static String buildObject(String... paires) {
        return "{" + String.join(",", paires) + "}";
    }
    public static String buildArray(List<String> elements) {
        return "[" + String.join(",", elements) + "]";
    }

    // === تحليل JSON ===
    public static Map<String, String> parseObject(String json) {
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|true|false|null|\\d+)");
        Matcher m = p.matcher(json);
        while (m.find()) {
            map.put(m.group(1), m.group(2));
        }
        return map;
    }

    // === هروب الأحرف الخاصة ===
    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
```
**بالدارجة :** هاد كلاس كايصنع ويحلل JSON يدوي (بدون مكتبة خارجية). `buildObject()` كاياخذ أجزاء JSON ويدمجهم فـ `{}`. `parseObject()` كايستعمل Regular Expression باش يقرا `"key":"value"`. `escape()` كايحول الأحرف الخاصة (`\`, `"`, `\n`) باش ما يخرّبش JSON.
**FR :** Utilitaire JSON maison (zéro dépendance). `parseObject()` utilise une regex pour extraire les paires clé-valeur. `buildObject()` construit un objet JSON à partir de paires formatées.

#### ResponseUtil.java

```java
public class ResponseUtil {
    public static void json(HttpExchange exchange, int code, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    public static void html(HttpExchange exchange, int code, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ...
    }

    public static void csv(HttpExchange exchange, String csv, String nomFichier) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Disposition",
            "attachment; filename=\"" + nomFichier + "\"");  // تحميل كملف
        ...
    }
}
```
**بالدارجة :** كاتسهّل إرسال الردود HTTP. `json()` كاتحط `Content-Type: application/json`. `csv()` كاتحط `Content-Disposition: attachment` باش يحمّل المتصفح الملف.
**FR :** Simplifie l'envoi des réponses. `Content-Disposition: attachment` force le téléchargement pour les CSV.

#### StaticFileHandler.java

```java
public class StaticFileHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";  // Redirection racine

        // أمـــن : منع directory traversal
        Path normalizedPath = Paths.get(filePath).normalize();
        Path basePath = Paths.get(baseDirectory).normalize();
        if (!normalizedPath.startsWith(basePath)) {
            ResponseUtil.json(exchange, 403, "{\"erreur\":\"Accès interdit\"}");
            return;
        }

        // تحديد MIME type
        String extension = ...;  // html → text/html, css → text/css, etc.
        String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

        byte[] bytes = Files.readAllBytes(file.toPath());
        ResponseUtil.envoyerFichierStatique(exchange, bytes, mimeType);
    }
}
```
**بالدارجة :** كايخدم الملفات الثابتة (HTML، CSS، JS، صور). كايحمي من هجمات `../` (directory traversal) باش ما يقراش ملفات خارج المجلد المصرّح بيه. كايدعم SPA-like routing (فاش كايتلقلات صفحة، كايخدم index.html).
**FR :** Sert les fichiers statiques depuis `/web/`. Protection anti-directory traversal. Fallback vers index.html pour le routage SPA-like.

#### AfficherTables.java

```java
public static void main(String[] args) {
    // يعرض جميع الجداول مع الأعمدة والمفاتيح الأساسية والأجنبية
    // أداة تشخيص مستقلة (main method)
}
```
**بالدارجة :** أداة مستقلة (run separate) كاتعرض هيكلة قاعدة البيانات فـ console.
**FR :** Outil CLI autonome qui affiche le schéma BD en console.

---

### 4.10 Frontend — Partie HTML/CSS/JS

#### Structure des pages HTML

Chaque page HTML suit le même pattern :

```html
<!-- Navigation commune -->
<nav class="navbar">
    <a href="/index.html" class="navbar-brand"><img src="img/logo-province.png" alt="Logo">Gestion MDP</a>
    <div class="navbar-nav">
        <a href="/index.html">Dashboard</a>
        <a href="/serveurs.html">Serveurs</a>
        <!-- ... -->
    </div>
    <div class="navbar-right">
        <span id="nomUtilisateur"></span>  <!-- rempli par JS -->
        <button class="theme-toggle" onclick="toggleTheme()">🌙</button>
        <button class="btn-logout" onclick="deconnecter()">Déconnexion</button>
    </div>
</nav>

<div class="container">
    <!-- Alertes -->
    <div id="alertSucces" class="alert alert-success" style="display:none;"></div>
    <div id="alertErreur" class="alert alert-danger" style="display:none;"></div>
    <!-- Contenu -->
    <div class="card">
        <div class="card-header">Titre</div>
        <div class="card-body">
            <table> ... </table>
        </div>
    </div>
</div>

<!-- Modals (fenêtres popup) -->
<div id="modalAjout" class="modal-overlay">
    <div class="modal">
        <div class="modal-header">...</div>
        <div class="modal-body">...</div>
        <div class="modal-footer">...</div>
    </div>
</div>

<script src="js/api.js"></script>
<script>
    // Code spécifique à la page
    async function chargerListe() { ... }
    document.addEventListener('DOMContentLoaded', chargerListe);
</script>
```

#### index.html — Tableau de bord

```javascript
async function chargerStats() {
    // جلب جميع الإحصائيات ف نفس الوقت (Promise.all)
    const [srv, sw, si, se, notif] = await Promise.all([
        API.getServeurs(), API.getSwitches(),
        API.getSystemesInternes(), API.getSystemesExternes(),
        API.getCompteurNotifications()
    ]);
    // عرض فالبطاقات
    document.getElementById('statServeurs').textContent = srv.data.length;
    // رسم الجرافيك
    dessinerGraphiques(srv, sw, si, se);
}
```
**بالدارجة :** `Promise.all()` كايخلّي الطلبات الخمسة يتصرفو ف نفس الوقت (متوازي). هادا كايسرّع التحميل.

```javascript
function dessinerGraphiques(srv, sw, si, se) {
    // رسم بياني بالـ Canvas API (بدون Chart.js ولا مكتبة)
    // Barres : لكل قيمة، رسم مستطيل بنسبة للقيمة القصوى
    // Donut : arcs de cercle مع زاوية حسب النسبة
}
```
**بالدارجة :** الرسوم البيانية كاتّعمل يدوي بـ Canvas API (ما كايناش Chart.js). هادا كايقلّل حجم الصفحة.

#### login.html

```javascript
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();  // منع الإرسال العادي ديال الفورم
    const res = await fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            login: document.getElementById('login').value,
            motDePasse: document.getElementById('motDePasse').value
        })
    });
    if (res.ok && data.succes) {
        window.location.href = '/index.html';  // تحويل للوحة القيادة
    }
});
```

#### api.js — Client API central

```javascript
const API = {
    BASE: '',
    async request(method, path, data = null) {
        const options = {
            method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include'  // → كايبعث الكوكي مع كل طلب
        };
        if (data) options.body = JSON.stringify(data);

        const response = await fetch(this.BASE + path, options);
        const json = await response.json();

        if (!response.ok && !json.succes) {
            throw new Error(json.erreur || 'Erreur serveur');
        }
        return { status: response.status, data: json };
    },
    get(path) { return this.request('GET', path); },
    post(path, data) { return this.request('POST', path, data); },
    put(path, data) { return this.request('PUT', path, data); },
    del(path) { return this.request('DELETE', path); },
    // ~30 méthodes spécifiques pour chaque endpoint...
};
```
**بالدارجة :** `credentials: 'include'` هو المهم — كايخلي `fetch` يبعث الـ cookies (SESSION_ID) مع كل طلب. `request()` كاتدير التحويل لـ JSON ورمي الأخطاء. كل endpoint عندو method ديالو.
**FR :** `credentials: 'include'` = envoie les cookies HTTP avec chaque requête. Sans ça, l'authentification ne fonctionnerait pas.

##### Connexion SSE (temps réel)

```javascript
function connecterSSE() {
    sseSource = new EventSource('/api/notifications/sse');
    sseSource.addEventListener('notification', (e) => {
        const data = JSON.parse(e.data);
        afficherToast(data.message, data.type);
        mettreAJourBadge();
    });
    sseSource.addEventListener('error', () => {
        sseSource.close();
        setTimeout(connecterSSE, 5000);  // إعادة الاتصال بعد 5 ثواني
    });
}
```
**بالدارجة :** `EventSource` كايخلي المتصفح يبقى متصل بالسيرڤور ويستقبل الإشعارات فالحين. إلا قطع الاتصال، كايعاود الاتصال بعد 5 ثواني.
**FR :** EventSource = connexion SSE persistante. Reconnect automatique après 5 secondes en cas d'erreur.

##### Vérification session

```javascript
document.addEventListener('DOMContentLoaded', async function() {
    if (window.location.pathname.endsWith('login.html')) return;
    try {
        const res = await API.checkSession();
        if (res.data.succes) {
            document.getElementById('nomUtilisateur').textContent = res.data.nomUtilisateur;
            window.utilisateurRole = res.data.role;  // تخزين الدور عالمياً
            connecterSSE();
        }
    } catch (e) {
        window.location.href = '/login.html';  // إعادة توجيه لصفحة الدخول
    }
});
```
**بالدارجة :** فاش كاتحمل أي صفحة، كاتتحقق من صحة الجلسة. إلا كانت الجلسة ماشي صالحة، كايتحول للمستخدم لصفحة الدخول. `window.utilisateurRole` كايتخزّن عالمياً باش كل صفحة تقدر تعرف واش تخلي `Voir` كلمة السر ولا لا.
**FR :** Vérification de session au chargement de chaque page. Redirection vers login si session invalide.

##### Dark mode

```javascript
(function initTheme() {
    const theme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', theme);
})();

function toggleTheme() {
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);  // حفظ التفضيل
}
```
**بالدارجة :** `localStorage` كايحفظ تفضيل الوضع (ليلي/نهاري) حتى بعد إعادة فتح المتصفح.
**FR :** Le thème est persisté dans `localStorage`.

#### style.css

Points clés (450+ lignes) :

```css
:root {
    --primary: #1a3a5c;           /* أزرق كحلي */
    --danger: #dc3545;            /* أحمر */
    --success: #28a745;           /* أخضر */
    --shadow: 0 2px 10px rgba(0,0,0,0.1);
}

/* الوضع الليلي : تغيير جميع المتغيرات */
[data-theme="dark"] {
    --primary: #4fc3f7;           /* أزرق فاتح */
    --light: #2d2d30;             /* رمادي غامق */
    --border: #404040;
}
```
**بالدارجة :** الوضع الليلي كايشتغل بـ CSS variables. `[data-theme="dark"]` كايبدّل جميع الألوان.

```css
.navbar {
    background: var(--primary);
    position: sticky; top: 0; z-index: 100;  /* الشريط كايبقى فوق */
}

.stat-card .number { font-size: 32px; font-weight: 700; color: var(--primary); }

.pwd-masked { font-family: monospace; letter-spacing: 3px; color: var(--gray); }
/* كلمات السر كاتعرض بخط monospace و فراغات باش تخليهم غير قابلين للقراءة من بعيد */

.modal-overlay {
    position: fixed; top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,0.5);
    justify-content: center; align-items: center;
}
.modal-overlay.active { display: flex; }
/* فقط فاش كايزاد class="active" كايظهر الـ modal */

/* تأثير الظهور */
@keyframes toastIn {
    from { transform: translateX(100%); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
}
```

---

### 4.11 Scripts de build

#### build.bat (Windows)

```batch
@echo off
chcp 65001 >nul   REM → UTF-8
set SRC_DIR=%PROJ_DIR%src
set OUT_DIR=%PROJ_DIR%out
set JDBC_JAR=%LIB_DIR%\mssql-jdbc-13.4.0.jre11.jar

REM Compilation
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\sources.txt"
javac --add-modules jdk.httpserver -cp "%JDBC_JAR%" -d "%OUT_DIR%" @"%TEMP%\sources.txt"

REM Exécution
java -Dfile.encoding=UTF-8 --add-modules jdk.httpserver ^
    -cp "%OUT_DIR%;%JDBC_JAR%" ma.province.safi.passwordmanager.Main
```
**بالدارجة :** `chcp 65001` كايخلي console تقرا UTF-8. `--add-modules jdk.httpserver` كايضيف وحدة HTTP server (Java 9+). `@fichier.txt` كايقرا قائمة ملفات Java من ملف نصي.
**FR :** `--add-modules jdk.httpserver` est nécessaire car `com.sun.net.httpserver` n'est plus chargé par défaut en Java 9+.

#### build.sh (Linux/Mac)

Même logique que build.bat mais avec `find` pour lister les fichiers `.java` et `:` comme séparateur de classpath.

---

### 4.12 Base de données (SQL)

Tables principales :

| Table | PK | Contenu |
|---|---|---|
| `dbo.Utilisateur` | `IdUtilisateur INT` | Utilisateurs avec hash PBKDF2 |
| `dbo.Role` | `IdRole INT` | Rôles (Admin, Agent, Consultation) |
| `dbo.Serveur` | `IdServeur INT` | Serveurs (IP, login, mdp chiffré) |
| `dbo.SwitchReseau` | `IdSwitch INT` | Switches (MAC, emplacement, login, mdp chiffré) |
| `dbo.SystemeInterne` | `IdSystemeInterne INT` | Systèmes internes (URL, division, login, mdp chiffré) |
| `dbo.SystemeExterne` | `IdSystemeExterne INT` | Systèmes externes (URL, division, login, mdp chiffré) |
| `dbo.DivisionInterne` | `IdDivisionInterne INT` | Divisions internes (nom, service) |
| `dbo.DivisionExterne` | `IdDivisionExterne INT` | Divisions externes (type, sous-type, caïdat) |
| `dbo.Notification` | `IdNotification BIGINT` | Notifications (message, type, lue/non lue) |
| `dbo.LogAudit` | `IdLog BIGINT` | Journal d'audit (action, entité, détails, IP, date) |

**بالدارجة :** القاعدة عندها 10 جداول. المفاتيح الأساسية إما INT (للجداول الصغيرة) أو BIGINT (للجداول لي ممكن يكبرو بزاف بحال Notifications و LogAudit). `MotPasseChiffre` فجميع جداول المعدات كايخزّن النص المشفر (وليس الـ hash). `VecteurInitialisation` هو الـ IV الخاص بـ AES-GCM.

---

## 5. Installation et démarrage / التنصيب والتشغيل

### Prérequis / المتطلبات
- JDK 17+
- Microsoft SQL Server (en cours d'exécution / شغّال)
- SQL Server JDBC driver (inclus dans `lib/` / موجود فالمجلد)

### Étapes / الخطوات

**1. Base de données / قاعدة البيانات**
```bash
# Exécutez le script SQL dans SSMS ou via sqlcmd
sqlcmd -S localhost -U sa -P sa -i sql/gestion_mots_de_passe.sql
```
**بالدارجة :** شغّل ملف SQL باش تنشئ قاعدة البيانات والجداول.

**2. Configuration / الإعدادات**
- Fichier : `src/ma/province/safi/passwordmanager/config/DatabaseConnection.java`
- Modifiez l'URL si votre SQL Server est sur une autre machine
- **بالدارجة :** إلا كان SQL Server عندك فجهاز آخر، بدّل `localhost` فـ URL.

**3. Compilation et exécution / التجميع والتشغيل**
```bash
# Linux/Mac
./build.sh

# Windows
build.bat
```

**4. Accès / الدخول**
- URL : http://localhost:8080
- Login : `admin`
- Mot de passe : `admin123`

### Déploiement Docker / النشر عبر Docker

L'application est fournie avec une stack Docker complète (`Dockerfile`, `docker-compose.yml`) : application Java + SQL Server 2022 dans des conteneurs séparés, volume pour la persistance de la clé AES et des données.

**Prérequis / المتطلبات** : Docker + plugin Docker Compose.

**Étapes / الخطوات**

```bash
# 1. Personnaliser le mot de passe SQL Server (obligatoire pour la prod)
cp .env.example .env
#    éditez .env → changez DB_PASSWORD

# 2. Lancer la stack (build + démarrage SQL Server puis application)
docker compose up -d --build
```

- L'application attend que SQL Server soit **healthy** avant de démarrer (`depends_on: condition: service_healthy`).
- Le premier démarrage crée la base `GestionMotsDePasse_safi` automatiquement (`DB_CREATE=true`), applique le schéma `sql/gestion_mots_de_passe.sql`, puis crée l'admin `admin / admin123`.
- La clé AES est persistée dans le volume `app-data` (`/app/data/.aes_key`) : **ne supprimez jamais ce volume**, sinon les secrets chiffrés deviendraient illisibles.
- Accès : http://localhost:8080

**Commandes utiles / أوامر مفيدة**
```bash
docker compose ps            # état des conteneurs
docker compose logs -f app   # logs applicatifs
docker compose logs -f db    # logs SQL Server
docker compose down          # arrêt (données conservées)
docker compose down -v       # arrêt + suppression des volumes (perte des données !)
```

**Déploiement VPS** : `deploy/setup-vps.sh` automatise l'installation de Docker, le clonage et le démarrage sur un serveur Ubuntu frais. Le workflow GitHub `.github/workflows/deploy.yml` déploie automatiquement sur `main` via SSH.

---

## 6. Sécurité / الأمان

| Mesure | Description | بالدارجة |
|---|---|---|
| **PBKDF2** | 600 000 itérations, HMAC-SHA256, sel 16 octets | كلمات السر مشفرة بـ 600 ألف دورة + ملح |
| **AES-256-GCM** | Chiffrement authentifié des secrets en BD | الأسرار مشفرة بـ AES مع مصادقة |
| **Clé persistante** | Clé AES dans `.aes_key` (hors Git) | المفتاح كايتحفظ فملف خارج Git |
| **Sessions HttpOnly** | Cookies protégés contre les attaques XSS | الكوكيز محمية من هجمات XSS |
| **SameSite=Lax** | Protection anti-CSRF | حماية من هجمات CSRF |
| **Timeout 30 min** | Expiration automatique des sessions | الجلسات كاتنتهى بعد 30 دقيقة |
| **Contrôle d'accès** | 3 rôles : Admin, Agent, Consultation | 3 أدوار: مدير، وكيل، مستشار |
| **Audit complet** | Toutes les actions sont tracées | كل العمليات مسجلة |
| **Directory traversal** | Protection dans StaticFileHandler | منع قراءة الملفات خارج المجلد المسموح |
| **SQL Injection** | PreparedStatement partout | حماية من SQL Injection فجميع DAO |

---

## 7. API REST / واجهة API

### Authentification / المصادقة

| Méthode | Endpoint | Description | بالدارجة |
|---|---|---|---|
| POST | `/api/login` | Se connecter | تسجيل الدخول |
| POST | `/api/logout` | Se déconnecter | تسجيل الخروج |
| GET | `/api/session` | Vérifier la session | التحقق من الجلسة |

### Serveurs / السيرڤورات

| Méthode | Endpoint | Rôle requis |
|---|---|---|
| GET | `/api/serveurs` | Tous |
| POST | `/api/serveurs` | Admin, Agent SSICTD |
| PUT | `/api/serveurs/{id}` | Admin, Agent SSICTD |
| DELETE | `/api/serveurs/{id}` | Admin seulement |
| GET | `/api/serveurs/{id}/consulter` | Admin seulement |
| PUT | `/api/serveurs/{id}/secret` | Admin, Agent SSICTD |
| GET | `/api/serveurs/export` | Admin, Agent SSICTD |

### Switches / السويتشات

Même structure que Serveurs : `/api/switches`

### Systèmes internes / الأنظمة الداخلية

Même structure : `/api/systemes-internes`

### Systèmes externes / الأنظمة الخارجية

Même structure : `/api/systemes-externes`

### Divisions / الأقسام

| Méthode | Endpoint |
|---|---|
| GET | `/api/divisions/internes` |
| GET | `/api/divisions/externes` |
| POST | `/api/divisions/internes` |
| POST | `/api/divisions/externes` |
| PUT | `/api/divisions/internes/{id}` |
| PUT | `/api/divisions/externes/{id}` |
| DELETE | `/api/divisions/internes/{id}` |
| DELETE | `/api/divisions/externes/{id}` |

### Notifications / الإشعارات

| Méthode | Endpoint |
|---|---|
| GET | `/api/notifications` |
| GET | `/api/notifications/compteur` |
| GET | `/api/notifications/sse` (SSE temps réel) |
| PUT | `/api/notifications/{id}/lire` |

### Audit / سجل المراقبة

| Méthode | Endpoint | Rôle |
|---|---|---|
| GET | `/api/audit` | Admin seulement |

### Recherche / البحث

| Méthode | Endpoint |
|---|---|
| GET | `/api/recherche?texte=xxx&type=SERVEUR` |

`type` optionnel : `SERVEUR`, `SWITCH`, `SYSTEME_INTERNE`, `SYSTEME_EXTERNE` (par défaut : tous)

### Schéma BD / هيكلة قاعدة البيانات

| Méthode | Endpoint |
|---|---|
| GET | `/api/schema` |

---

**Développé pour la Province de Safi — Division SSICTD**  
**طور لعمالة إقليم آسفي — قسم SSICTD**

**Auteur :** Amine Bengana  
**Technologie :** Java, SQL Server, HTML/CSS/JS

---

> **Note :** Le fichier `.aes_key` est généré automatiquement au premier démarrage et contient la clé AES-256. **Ne pas commiter ce fichier** — il contient la clé de déchiffrement de tous les mots de passe. Si ce fichier est perdu, les mots de passe stockés deviennent indéchiffrables.  
> **ملاحظة :** ملف `.aes_key` كايتولد أوتوماتيكياً فاش كايبدا البرنامج لأول مرة. **دير بالك ما ترفعوش لـ Git** — فيه مفتاح تشفير جميع كلمات السر. إلا ضاع هاد الملف، كلمات السر لي فالباز غادي يبقاو مشفرين وما تقدرش تفكهم.

# Gestion des Mots de Passe — Province de Safi

**Application de gestion centralisée des mots de passe et des secrets informatiques**  
Développée pour la **Province de Safi** — Division **SSICTD** (Service des Systèmes d'Information, de la Communication et des Télécommunications et de la Documentation).

---

## Français

### Présentation

Cette application permet à la **Province de Safi** de gérer de manière centralisée et sécurisée l'ensemble des mots de passe et des secrets techniques de son infrastructure informatique. Elle remplace la gestion manuelle (fichiers Excel, post-its, fichiers texte) par une plateforme web chiffrée, tracée et multi-utilisateurs.

### Fonctionnalités principales

| Fonctionnalité | Description |
|---|---|
| **Gestion des serveurs** | Ajout, modification, suppression et consultation des mots de passe des serveurs |
| **Gestion des switches réseau** | Inventaire et mots de passe des équipements réseau |
| **Systèmes d'information internes** | Applications et plateformes internes (liées aux divisions internes) |
| **Systèmes d'information externes** | Applications externes (AAL, Caïdat) liées aux divisions externes |
| **Gestion des divisions** | Divisions internes (SSICTD) et externes (AAL, Caïdat) |
| **Recherche globale** | Recherche unifiée dans tous les équipements et systèmes |
| **Export CSV / PDF** | Export des données au format CSV ou HTML (imprimable) |
| **Notifications** | Alertes pour changements de mot de passe, rotation, ajouts de comptes |
| **Audit complet** | Traçage de toutes les actions (connexion, consultation, ajout, modification, suppression, export) |
| **Authentification sécurisée** | Mots de passe hachés avec PBKDF2 (600 000 itérations), sessions HTTP avec timeout 30 min |
| **Chiffrement AES-256/GCM** | Tous les secrets (mots de passe serveurs, switches, systèmes) sont chiffrés en base de données |

### Architecture technique

- **Langage** : Java (JDK 17+)
- **Serveur HTTP embarqué** : `com.sun.net.httpserver.HttpServer` (pas de framework web externe)
- **Base de données** : Microsoft SQL Server
- **Chiffrement des secrets** : AES-256/GCM (chiffrement des mots de passe en base)
- **Hachage des mots de passe utilisateurs** : PBKDF2-HMAC-SHA256 (600 000 itérations)
- **Frontend** : HTML/CSS/JavaScript vanilla (pas de framework JS)
- **Export** : CSV et HTML (imprimable / PDF via navigateur)
- **Notifications temps réel** : Server-Sent Events (SSE)
- **Pilote JDBC** : Microsoft JDBC Driver pour SQL Server

### Sécurité

1. **Mots de passe utilisateurs** : hachés avec PBKDF2-HMAC-SHA256 + sel aléatoire (16 octets), 600 000 itérations
2. **Secrets métier** : chiffrés avec AES-256-GCM (chiffrement authentifié) avant stockage en base
3. **Sessions** : tokens UUID aléatoires, expiration après 30 minutes d'inactivité
4. **Contrôle d'accès** : 3 rôles (Administrateur, Agent SSICTD, Utilisateur consultation)
5. **Audit** : toutes les actions sont tracées dans la table LogAudit
6. **Notifications** : alertes en temps réel via SSE (Server-Sent Events)

### Prérequis techniques

- Java Development Kit (JDK) 17 ou supérieur
- Microsoft SQL Server (local ou distant)
- Pilote JDBC SQL Server (inclus dans `lib/`)

### Installation et démarrage

#### 1. Base de données

Exécuter le script SQL `sql/gestion_mots_de_passe.sql` sur votre instance SQL Server pour créer la base `GestionMotsDePasse_safi` et toutes les tables.

#### 2. Configuration

Les paramètres de connexion à la base de données se trouvent dans :
`src/ma/province/safi/passwordmanager/config/DatabaseConnection.java`

- URL : `jdbc:sqlserver://localhost:1433;databaseName=GestionMotsDePasse_safi`
- Utilisateur : `sa`
- Mot de passe : `sa`

#### 3. Compilation et exécution

**Linux/Mac :**
```bash
./build.sh
```

**Windows :**
```batch
build.bat
```

L'application compile tous les fichiers Java, puis démarre un serveur HTTP sur `http://localhost:8080`.

#### 4. Connexion

Compte administrateur par défaut :
- **Login** : `admin`
- **Mot de passe** : `admin123`

### Structure du projet

```
gestiondemdp/
├── src/                              # Code source Java
│   ├── Main.java                     # Point d'entrée
│   ├── database/
│   │   └── ConnectionDB.java
│   └── ma/province/safi/passwordmanager/
│       ├── config/
│       │   └── DatabaseConnection.java    # Connexion SQL Server
│       ├── controller/                     # Handlers HTTP (API REST)
│       │   ├── AuditHandler.java
│       │   ├── DivisionHandler.java
│       │   ├── LoginHandler.java
│       │   ├── LogoutHandler.java
│       │   ├── NotificationHandler.java
│       │   ├── RechercheHandler.java
│       │   ├── SessionHandler.java
│       │   ├── SseHandler.java
│       │   └── ...
│       ├── dao/                            # Accès aux données (DAO)
│       ├── model/                          # Modèles métier
│       ├── security/                       # Chiffrement, hachage, sécurité
│       ├── service/                        # Logique métier
│       ├── session/                        # Gestion des sessions
│       └── util/                           # Utilitaires (JSON, fichiers statiques)
├── web/                                    # Frontend (HTML/CSS/JS)
├── sql/                                    # Scripts SQL
├── lib/                                    # Bibliothèques (JDBC)
├── out/                                    # Classes compilées
├── build.sh                               # Script de build Linux/Mac
├── build.bat                              # Script de build Windows
└── README.md
```

### API REST

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/login` | Authentification |
| POST | `/api/logout` | Déconnexion |
| GET | `/api/session` | Vérification de session |
| GET | `/api/serveurs` | Liste des serveurs |
| POST | `/api/serveurs` | Ajouter un serveur |
| PUT | `/api/serveurs/{id}` | Modifier un serveur |
| DELETE | `/api/serveurs/{id}` | Supprimer un serveur |
| GET | `/api/switches` | Liste des switches |
| POST | `/api/switches` | Ajouter un switch |
| PUT | `/api/switches/{id}` | Modifier un switch |
| DELETE | `/api/switches/{id}` | Supprimer un switch |
| GET | `/api/systemes-internes` | Liste des systèmes internes |
| POST | `/api/systemes-internes` | Ajouter un système interne |
| PUT | `/api/systemes-internes/{id}` | Modifier un système interne |
| DELETE | `/api/systemes-internes/{id}` | Supprimer un système interne |
| GET | `/api/systemes-externes` | Liste des systèmes externes |
| POST | `/api/systemes-externes` | Ajouter un système externe |
| PUT | `/api/systemes-externes/{id}` | Modifier un système externe |
| DELETE | `/api/systemes-externes/{id}` | Supprimer un système externe |
| GET | `/api/divisions` | Liste des divisions |
| GET | `/api/notifications` | Liste des notifications |
| GET | `/api/notifications/sse` | SSE (notifications temps réel) |
| PUT | `/api/notifications/{id}/lu` | Marquer une notification comme lue |
| GET | `/api/audit` | Journal d'audit |
| GET | `/api/recherche` | Recherche globale |

### Rôles utilisateurs

1. **Administrateur** — Accès complet à l'application, gestion de tous les comptes et équipements
2. **Agent SSICTD** — Gestion opérationnelle des secrets et des équipements
3. **Utilisateur consultation** — Consultation contrôlée selon les droits accordés

---

## الدارجة المغربية (Darija)

### شنو هي هاد التطبيق؟

هاد التطبيق هو **مدير ديال كلمات السر** (Password Manager) لي طوراتو **عمالة إقليم آسفي** — قسم **SSICTD**. الهدف ديالو هو تجميع و تأمين جميع كلمات السر والمعلومات التقنية ديال الخدمات المعلوماتية فمكان واحد.

### شنو كيقدّم هاد التطبيق؟

- **تسيير السيرڤورات (Serveurs)** : زيد، بدّل، حيد، وشوف كلمات السر ديال السيرڤورات
- **تسيير السويتشات (Switches réseau)** : المخزون ديال المعدات الشبكية وكلمات السر ديالهم
- **الأنظمة الداخلية (Systèmes internes)** : التطبيقات والمنصات الديال الإدارة الداخلية
- **الأنظمة الخارجية (Systèmes externes)** : التطبيقات ديال الجماعات الترابية (AAL) والقيادات
- **تسيير الأقسام (Divisions)** : الأقسام الداخلية والخارجية
- **البحث العام (Recherche globale)** : بحث موحد فجميع المعدات والأنظمة
- **التصدير (Export)** : تصدير البيانات بصيغة CSV ولا HTML (قابلة للطباعة)
- **الإشعارات (Notifications)** : تنبيهات فالحين على تغييرات كلمات السر والإضافات الجديدة
- **سجل المراقبة (Audit)** : تسجيل جميع العمليات (دخول، إضافة، تعديل، حذف، تصدير)

### كيفاش كتخدم الأمان؟

1. **كلمات سر المستخدمين** : مشفرة بـ PBKDF2-HMAC-SHA256 مع ملح عشوائي (16 بايت)، 600,000 دورة
2. **الأسرار التقنية** : مشفرة بـ AES-256-GCM قبل التخزين في قاعدة البيانات
3. **الجلسات (Sessions)** : توكن عشوائي UUID، تنتهي بعد 30 دقيقة من عدم النشاط
4. **الصلاحيات** : 3 أدوار (مدير عام، وكيل SSICTD، مستشار)
5. **التتبع (Audit)** : كل عملية مسجلة في سجل المراقبة

### كيفاش تنصب وتشغل التطبيق؟

#### 1. قاعدة البيانات

شغّل ملف `sql/gestion_mots_de_passe.sql` على SQL Server باش تنشئ قاعدة البيانات `GestionMotsDePasse_safi` وجميع الجداول.

#### 2. الإعدادات

معلومات الاتصال بقاعدة البيانات كاينة فـ :
`src/ma/province/safi/passwordmanager/config/DatabaseConnection.java`

- URL : `jdbc:sqlserver://localhost:1433;databaseName=GestionMotsDePasse_safi`
- المستخدم : `sa`
- كلمة السر : `sa`

#### 3. التجميع والتشغيل

**Linux/Mac :**
```bash
./build.sh
```

**Windows :**
```batch
build.bat
```

التطبيق غادي يجمع جميع ملفات Java ويشغل السيرڤور على `http://localhost:8080`.

#### 4. الدخول للتطبيق

الحساب الافتراضي ديال المدير العام :
- **Login** : `admin`
- **Mot de passe** : `admin123`

### التقنيات المستعملة

- **لغة البرمجة** : Java (JDK 17+)
- **السيرڤور** : HTTP server مدمج فـ Java (بلا إطار خارجي)
- **قاعدة البيانات** : Microsoft SQL Server
- **تشفير الأسرار** : AES-256-GCM
- **تشفير كلمات السر** : PBKDF2-HMAC-SHA256
- **الواجهة الأمامية** : HTML/CSS/JavaScript (بلا إطار)
- **التنبيهات الفورية** : Server-Sent Events (SSE)
- **التصدير** : CSV و HTML (قابل للطباعة)

### هيكلة المشروع

```
gestiondemdp/
├── src/                              # الكود المصدري (Java)
│   ├── Main.java                     # نقطة الدخول
│   └── ma/province/safi/passwordmanager/
│       ├── config/                   # إعدادات قاعدة البيانات
│       ├── controller/               # معالجات API (REST)
│       ├── dao/                      # الوصول للبيانات
│       ├── model/                    # النماذج
│       ├── security/                 # التشفير والأمان
│       ├── service/                  # المنطق التجاري
│       ├── session/                  # إدارة الجلسات
│       └── util/                     # الأدوات المساعدة
├── web/                              # الواجهة الأمامية (HTML/CSS/JS)
├── sql/                              # سكريبتات SQL
├── lib/                              # المكتبات (JDBC)
├── out/                              # الكومپيل (classes)
├── build.sh                          # سكريبت البناء (Linux/Mac)
├── build.bat                         # سكريبت البناء (Windows)
└── README.md
```

### API REST

| الطريقة | المسار | الوصف |
|---|---|---|
| POST | `/api/login` | تسجيل الدخول |
| POST | `/api/logout` | تسجيل الخروج |
| GET | `/api/session` | التحقق من الجلسة |
| GET | `/api/serveurs` | قائمة السيرڤورات |
| POST | `/api/serveurs` | إضافة سيرڤور |
| PUT | `/api/serveurs/{id}` | تعديل سيرڤور |
| DELETE | `/api/serveurs/{id}` | حذف سيرڤور |
| GET | `/api/switches` | قائمة السويتشات |
| POST | `/api/switches` | إضافة سويتش |
| PUT | `/api/switches/{id}` | تعديل سويتش |
| DELETE | `/api/switches/{id}` | حذف سويتش |
| GET | `/api/systemes-internes` | قائمة الأنظمة الداخلية |
| POST | `/api/systemes-internes` | إضافة نظام داخلي |
| PUT | `/api/systemes-internes/{id}` | تعديل نظام داخلي |
| DELETE | `/api/systemes-internes/{id}` | حذف نظام داخلي |
| GET | `/api/systemes-externes` | قائمة الأنظمة الخارجية |
| POST | `/api/systemes-externes` | إضافة نظام خارجي |
| PUT | `/api/systemes-externes/{id}` | تعديل نظام خارجي |
| DELETE | `/api/systemes-externes/{id}` | حذف نظام خارجي |
| GET | `/api/divisions` | قائمة الأقسام |
| GET | `/api/notifications` | قائمة الإشعارات |
| GET | `/api/notifications/sse` | الإشعارات الفورية (SSE) |
| PUT | `/api/notifications/{id}/lu` | تحديد إشعار كمقروء |
| GET | `/api/audit` | سجل المراقبة |
| GET | `/api/recherche` | البحث العام |

### الأدوار والصلاحيات

1. **Administrateur (مدير عام)** — صلاحية كاملة على التطبيق، تسيير جميع الحسابات والمعدات
2. **Agent SSICTD (وكيل SSICTD)** — تسيير العمليات ديال الأسرار والمعدات
3. **Utilisateur consultation (مستشار)** — اطلاع مراقب حسب الصلاحيات الممنوحة

---

Développé pour la **Province de Safi** — Division **SSICTD**  
طور لعمالة إقليم آسفي — قسم **SSICTD**

---

## شرح التطبيق بالدارجة + français — étape par étape et code ligne par ligne

### 1. شنو هو هاد المشروع؟ / De quoi s'agit-il ?

**بالدارجة :** هاد مشروع هو site web كايخدم فـ localhost باش تدير كلمات السر ديال السيرڤورات والسويتشات والأنظمة المعلوماتية ديال عمالة آسفي. الناس لي عندهم صلاحية يقدر يدخلو يشوفو كلمات السر، يزيدو، يعدلو، ويحيدو. الأمان فيه قوي: كلمات السر مخزنة مشفرة (AES-256) وكلمات السر ديال المستخدمين مخزنة مشفرة بـ PBKDF2.

**En français :** Projet web (serveur HTTP intégré Java sans framework) pour gérer les mots de passe de l'infrastructure IT de la province de Safi. Chiffrement AES-256/GCM des secrets, hachage PBKDF2 des mots de passe utilisateurs, sessions HTTP, notifications en temps réel (SSE).

---

### 2. هيكلة المشروع / Structure du projet

```
gestiondemdp/
├── src/Main.java                     # نقطة الدخول — point d'entrée
├── src/ma/province/safi/passwordmanager/
│   ├── Main.java                     # même fichier (copie)
│   ├── config/DatabaseConnection.java  # الاتصال بقاعدة البيانات
│   ├── model/                          # النماذج (موديلات)
│   │   ├── EntiteAvecSecret.java
│   │   ├── CompteTechnique.java
│   │   ├── Serveur.java
│   │   ├── SwitchReseau.java
│   │   ├── Systeme.java
│   │   ├── SystemeInterne.java
│   │   ├── SystemeExterne.java
│   │   ├── DivisionInterne.java
│   │   ├── DivisionExterne.java
│   │   ├── TypeDivisionExterne.java
│   │   ├── Utilisateur.java
│   │   ├── RoleUtilisateur.java
│   │   ├── LogAudit.java
│   │   └── Notification.java
│   ├── dao/                           # الوصول للبيانات (SQL)
│   │   ├── ServeurDAO.java
│   │   ├── SwitchDAO.java
│   │   ├── SystemeInterneDAO.java
│   │   ├── SystemeExterneDAO.java
│   │   ├── DivisionDAO.java
│   │   ├── UtilisateurDAO.java
│   │   ├── NotificationDAO.java
│   │   └── AuditDAO.java
│   ├── service/                       # المنطق التجاري (service)
│   │   ├── ServeurService.java
│   │   ├── SwitchService.java
│   │   ├── SystemeInterneService.java
│   │   ├── SystemeExterneService.java
│   │   ├── AuthService.java
│   │   ├── ExportService.java
│   │   ├── NotificationService.java
│   │   └── RechercheService.java
│   ├── controller/                    # API REST (handlers)
│   │   ├── ServeurHandler.java
│   │   ├── SwitchHandler.java
│   │   ├── SystemeInterneHandler.java
│   │   ├── SystemeExterneHandler.java
│   │   ├── LoginHandler.java
│   │   ├── LogoutHandler.java
│   │   ├── SessionHandler.java
│   │   ├── DivisionHandler.java
│   │   ├── NotificationHandler.java
│   │   ├── SseHandler.java
│   │   ├── AuditHandler.java
│   │   └── RechercheHandler.java
│   ├── security/
│   │   ├── CryptoService.java           # تشفير AES-256-GCM
│   │   ├── PasswordHasher.java          # هاش كلمات السر PBKDF2
│   │   └── SecurityInterceptor.java     # التحقق من الصلاحية
│   ├── session/
│   │   ├── Session.java
│   │   └── SessionManager.java
│   └── util/
│       ├── JsonUtil.java
│       ├── ResponseUtil.java
│       └── StaticFileHandler.java
├── web/                                # Frontend (HTML/CSS/JS)
├── sql/gestion_mots_de_passe.sql       # قاعدة البيانات
├── lib/mssql-jdbc-13.4.0.jre11.jar     # مكتبة SQL Server
├── build.sh / build.bat                # سكريبتات البناء
└── README.md
```

---

### 3. الخطوة 1 : قاعدة البيانات / Étape 1 : Base de données

**sql/gestion_mots_de_passe.sql**

```sql
CREATE TABLE dbo.Serveur (
    IdServeur              INT IDENTITY(1,1) NOT NULL,
    NomServeur             NVARCHAR(100) NOT NULL,
    AdresseIP              VARCHAR(45) NOT NULL,
    LoginServeur           NVARCHAR(100) NOT NULL,
    MotPasseChiffre        NVARCHAR(MAX) NOT NULL,
    VecteurInitialisation  NVARCHAR(255) NULL,
    DateDernierChangement  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    DateCreation           DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Serveur PRIMARY KEY CLUSTERED (IdServeur)
);
```

**بالدارجة :** هاد الجدول كايخزن السيرڤورات. `IdServeur` هو الرقم التعريفي (ماشي فاضي). `NomServeur` فيه سمية السيرڤور. `AdresseIP` فيه الـ IP. `LoginServeur` فيه اسم المستخدم. `MotPasseChiffre` فيه كلمة السر مشفرة. `VecteurInitialisation` هو المفتاح الإضافي ديال التشفير. `DateDernierChangement` هو التاريخ ديال آخر تغيير.

**En français :** La colonne `MotPasseChiffre` stocke le mot de passe CHIFFRÉ (pas en clair). `VecteurInitialisation` stocke l'IV (initialization vector) AES. Ces deux colonnes sont nécessaires pour déchiffrer. `DateDernierChangement` est mise à jour automatiquement à chaque modification du mot de passe avec `SYSUTCDATETIME()`.

---

### 4. الخطوة 2 : النقطة ديال الدخول / Étape 2 : Point d'entrée (Main.java)

**src/ma/province/safi/passwordmanager/Main.java**

```java
package ma.province.safi.passwordmanager;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        // Test connexion BD
        if (!DatabaseConnection.testConnection()) { System.exit(1); }

        // Injection de dépendances
        ServeurDAO serveurDAO = new ServeurDAO();
        SwitchDAO switchDAO = new SwitchDAO();
        // ... autres DAO ...

        // Clé AES persistée dans .aes_key
        SecretKey aesKey = chargerOuCreerCleAES();
        CryptoService cryptoService = new CryptoService(aesKey);

        // Services
        ServeurService serveurService = new ServeurService(serveurDAO, auditDAO, notificationDAO, cryptoService);
        // ... autres services ...

        // Serveur HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/serveurs", new ServeurHandler(serveurService, security, exportService));
        server.createContext("/api/switches", new SwitchHandler(switchService, security, exportService));
        // ... autres routes ...
        server.start();
    }
}
```

**بالدارجة :** هاد هو المكان لي كاين فيه بداية البرنامج. كايبدا بـ :
1. كايختبر الاتصال بقاعدة البيانات SQL Server
2. كايصنع جميع الـ DAO (وصلات البيانات) و الـ Services (المنطق التجاري)
3. كايشوف واش كاين ملف `.aes_key` فيه المفتاح ديال التشفير. إلا كان كاين كايجيبو، إلا لا كايصنع واحد جديد ويحفظو
4. كايصنع السيرڤور HTTP على المنفذ 8080 ويسجل جميع المسارات (Routes)
5. كايبدا السيرڤور

**En français :** Ce fichier est le point d'entrée. Il :
1. Teste la connexion SQL Server
2. Crée les dépendances (DAO, Services, Security)
3. Charge ou crée la clé AES persistante
4. Configure et démarre le serveur HTTP sur le port 8080

---

### 5. شرح الطبقة الأولى (Model) / Couche Model

#### EntiteAvecSecret.java — Interface

```java
public interface EntiteAvecSecret {
    int getId();
    String getNom();
    String getLogin();
}
```

**بالدارجة :** هاد interface كاتجمع الحوايج المشتركة بين جميع الكيانات لي عندهم كلمة سر. كل كيان عندو ID وسمية وLogin.

**En français :** Interface que tous les modèles avec secret doivent implémenter. Garantit qu'ils ont getId(), getNom(), getLogin().

#### CompteTechnique.java — Classe abstraite

```java
public abstract class CompteTechnique implements EntiteAvecSecret {
    protected int id;
    protected String nom;
    protected String login;
    protected String motPasseChiffre;      // مشفر
    protected String vecteurInitialisation; // IV
    protected LocalDateTime dateDernierChangement;
    protected String motDePasseClair;       // غير محفوظ فالباز

    // Getters et setters...
}
```

**بالدارجة :** هاد كلاس مجرد (ma9houd) كايجمع الخصائص المشتركة بين السيرڤورات والسويتشات والأنظمة. `motPasseChiffre` فيه كلمة السر مشفرة. `vecteurInitialisation` هو IV لي كايدخل فالتشفير. `motDePasseClair` فيه كلمة السر بحال ما هي (هاد الخانة ما كاتتحفظش فالباز، كاتكون فقط فاش كايتعرض البيانات).

**En français :** Classe abstraite mutualisant les champs communs. `motDePasseClair` est un champ **transient** (non persisté en BD) utilisé seulement pour afficher le mot de passe déchiffré dans la liste. `motPasseChiffre` contient le mot de passe chiffré AES.

#### Serveur.java

```java
public class Serveur extends CompteTechnique {
    private String adresseIP;

    public Serveur(String nom, String adresseIP, String login) {
        this.nom = nom;
        this.adresseIP = adresseIP;
        this.login = login;
    }
}
```

**بالدارجة :** السيرڤور عندو إضافة وحدة هي `adresseIP`. الباقي كايجيو من `CompteTechnique` (سمية، login، كلمة السر مشفرة، IV، تاريخ التغيير).

**En français :** Hérite de CompteTechnique, ajoute uniquement l'adresse IP.

#### SwitchReseau.java

```java
public class SwitchReseau extends CompteTechnique {
    private String adresseMAC;
    private String emplacement;
}
```

#### Systeme.java

```java
public abstract class Systeme extends CompteTechnique {
    protected String url;
}
```

#### SystemeInterne.java

```java
public class SystemeInterne extends Systeme {
    private int idDivisionInterne;   // كل système interne تابع لـ division
}
```

#### SystemeExterne.java

```java
public class SystemeExterne extends Systeme {
    private int idDivisionExterne;
    private Integer idSystemeInterneLie;  // يمكن أن يكون null
}
```

**بالدارجة :** `SystemeExterne` عندو `idSystemeInterneLie` لي يمكن يكون فارغ (null). هادشي كايعني أن النظام الخارجي يمكن يكون مربوط بنظام داخلي ولا لا.

---

### 6. شرح طبقة الوصول للبيانات (DAO) / Couche DAO

**ServeurDAO.java — lister()**

```java
public List<Serveur> lister() throws SQLException {
    List<Serveur> list = new ArrayList<>();
    String sql = "SELECT IdServeur, NomServeur, AdresseIP, LoginServeur,
                  MotPasseChiffre, VecteurInitialisation, DateDernierChangement
                  FROM dbo.Serveur ORDER BY NomServeur";
    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            Serveur s = new Serveur();
            s.setId(rs.getInt("IdServeur"));
            s.setNom(rs.getString("NomServeur"));
            s.setAdresseIP(rs.getString("AdresseIP"));
            s.setLogin(rs.getString("LoginServeur"));
            s.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
            s.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
            s.setDateDernierChangement(
                rs.getTimestamp("DateDernierChangement") != null
                ? rs.getTimestamp("DateDernierChangement").toLocalDateTime()
                : null);
            list.add(s);
        }
    }
    return list;
}
```

**بالدارجة :** هاد الدالة كاتجيب جميع السيرڤورات من قاعدة البيانات. ستخدم `PreparedStatement` باش تحمي من SQL Injection. `try-with-resources` كايضمن أن الاتصال والإجابة يسكرو حتى إلا طاحت مشكلة. كايفة مهمة : هاد الدالة دابا كاتجيب `MotPasseChiffre` و `VecteurInitialisation` (قبل كنتا ما كاتجيبهمش). هاد التغيير ضروري باش نقدر نعرض كلمة السر فالقائمة.

**En français :** La méthode `lister()` récupère TOUS les serveurs. On utilise `try-with-resources` pour fermer automatiquement les connexions. La requête inclut maintenant `MotPasseChiffre` et `VecteurInitialisation` pour permettre le déchiffrement des mots de passe côté service.

**ServeurDAO.java — ajouter()**

```java
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
            if (rs.next()) s.setId(rs.getInt(1));
        }
    }
}
```

**بالدارجة :** كاتسجّل سيرڤور جديد فالباز. لاحظ بلي `secretChiffre` و `iv` هوما لي تيتم دوزهم من `CryptoService` (بعيد ما تكلّى التشفير). `Statement.RETURN_GENERATED_KEYS` كايخليك تجيب الرقم التعريفي الجديد (IdServeur) لي تولّدوه القاعدة.

---

### 7. شرح التشفير / Le chiffrement

**CryptoService.java**

```java
public class CryptoService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;

    public ChiffrementResultat chiffrer(String texteClair) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] chiffre = cipher.doFinal(texteClair.getBytes(StandardCharsets.UTF_8));

        return new ChiffrementResultat(
            Base64.getEncoder().encodeToString(chiffre),
            Base64.getEncoder().encodeToString(iv)
        );
    }

    public String dechiffrer(String texteChiffre, String ivBase64) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(
            GCM_TAG_LENGTH,
            Base64.getDecoder().decode(ivBase64)
        );
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] clair = cipher.doFinal(Base64.getDecoder().decode(texteChiffre));
        return new String(clair, StandardCharsets.UTF_8);
    }
}
```

**بالدارجة :** هاد الكلاس هو قلب الأمان ديال التطبيق.
- **التشفير (chiffrer) :** أول حاجة كايصنع IV عشوائي طولو 12 بايت. من بعد كاياخذ كلمة السر الفارغة (texteClair) ويشفرها بـ AES-256-GCM. النتيجة هي byte[] كايتحول لـ Base64 باش يقدّر يتخزن فالباز (نص).
- **فك التشفير (dechiffrer) :** العكس. كاياخذ النص المشفر (Base64) ويحولو لـ byte[]، ومن بعد مع IV (حتى هو Base64) كايفك التشفير ويعطي كلمة السر الفارغة.
- **لماذا GCM ?** GCM كايفرق بين "التشفير" و"المصادقة" (authentication tag). هاد الشي كايحمي من التلاعب بالبيانات. إلا كان حتى واحد بدّل النص المشفر، GCM كايديك "Tag mismatch" وما كايحاولش يفك التشفير.

**En français :** AES-256-GCM est un chiffrement **authentifié** : en plus de chiffrer, il ajoute un tag qui permet de vérifier que les données n'ont pas été modifiées. Le `IV` (12 bytes aléatoires) est nécessaire pour le déchiffrement, il est stocké en clair à côté du mot de passe chiffré. Le `tag mismatch` se produit si la clé a changé ou si les données ont été altérées.

---

### 8. شرح طبقة الخدمات (Service) / Couche Service

**ServeurService.java — ajouter()**

```java
public void ajouter(Serveur serveur, String motDePasseClair, Session session) throws Exception {
    // 1. تشفير كلمة السر
    CryptoService.ChiffrementResultat cr = cryptoService.chiffrer(motDePasseClair);

    // 2. تخزين فالباز (كلمة السر مشفرة)
    serveurDAO.ajouter(serveur, cr.secretChiffre(), cr.iv());

    // 3. تسجيل فالسجل (audit)
    auditDAO.enregistrerOld("AJOUT", "SERVEUR", serveur.getId(),
        session.getIdUtilisateur(), "Ajout du serveur: " + serveur.getNom());

    // 4. إشعار للمستخدمين
    notificationDAO.creer(session.getIdUtilisateur(),
        "Nouveau serveur ajouté: " + serveur.getNom(), "INFORMATION");
}
```

**بالدارجة :** فاش كايزيد سيرڤور جديد :
1. كلمة السر الفارغة كاتدخل لـ `cryptoService.chiffrer()` لي كايحولها لـ مشفرة + IV
2. النتيجة (المشفرة + IV) كايتخزنو فالباز عن طريق DAO
3. العملية كاتسجل فسجل المراقبة (audit)
4. إشعار كايبعت لجميع المستخدمين المتصلين

**ServeurService.java — lister()**

```java
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

**بالدارجة :** هاد الدالة كاتجيب جميع السيرڤورات، ومن بعد كاتفك تشفير كل كلمة سر. إلا كان فشل فك التشفير (مثلاً المفتاح تغيّر)، كاتجاوز الخطأ بصمت وما كاتعطلش القائمة. `motDePasseClair` كايتحط فالموديل باش يتعرض فالواجهة.

**En français :** Le service `lister()` récupère la liste depuis le DAO, puis **déchiffre** chaque mot de passe individuellement. Si le déchiffrement échoue (clé AES différente), l'erreur est ignorée silencieusement pour ne pas bloquer l'affichage de la liste. Le mot de passe en clair est stocké dans le champ temporaire `motDePasseClair`.

---

### 9. شرح طبقة التحكم (Controller / Handler) / Couche Controller

**ServeurHandler.java — handle()**

```java
@Override
public void handle(HttpExchange exchange) throws IOException {
    try {
        String methode = exchange.getRequestMethod();     // GET, POST, PUT, DELETE
        String path = exchange.getRequestURI().getPath();  // /api/serveurs/1

        // --- Export CSV/PDF ---
        if ("GET".equals(methode) && path.endsWith("/export")) {
            Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
            if (session == null) return;
            // ... export ...
        }

        // --- Consultation du mot de passe (Voir) ---
        if ("GET".equals(methode) && path.contains("/consulter")) {
            Session session = security.verifierRoleAdministrateur(exchange);
            if (session == null) return;
            int id = extraireId(path);
            String secret = serveurService.consulterSecret(id, session);
            ResponseUtil.json(exchange, 200, JsonUtil.buildObject(
                JsonUtil.jsonString("succes", "true"),
                JsonUtil.jsonString("motDePasse", secret != null ? secret : "")
            ));
            return;
        }

        // --- التوجيه حسب الطريقة (GET, POST, PUT, DELETE) ---
        switch (methode) {
            case "GET" -> lister(exchange);
            case "POST" -> ajouter(exchange);
            case "PUT" -> modifierOuSecret(exchange);
            case "DELETE" -> supprimer(exchange);
        }
    } catch (Exception e) {
        e.printStackTrace();
        ResponseUtil.json(exchange, 500,
            JsonUtil.json("erreur", e.getMessage() != null ? e.getMessage() : "Erreur interne"));
    }
}
```

**بالدارجة :** هاد هو المتحكم الرئيسي للسيرڤورات. كايقرا المسار (path) والطريقة (method) ويقرر شنهوا دير :
- **GET /api/serveurs** → جيب جميع السيرڤورات
- **POST /api/serveurs** → زيد سيرڤور جديد
- **PUT /api/serveurs/1** → عدّل السيرڤور رقم 1
- **PUT /api/serveurs/1/secret** → بدّل كلمة السر ديال السيرڤور 1
- **DELETE /api/serveurs/1** → حذف السيرڤور 1
- **GET /api/serveurs/1/consulter** → شوف كلمة السر (مشروطة بالصلاحية)

**extraireId()** — كاتجيب الرقم التعريفي من المسار

```java
private int extraireId(String path) {
    String[] parts = path.split("/");
    // parts = ["", "api", "serveurs", "1", "consulter"]
    for (int i = 0; i < parts.length; i++) {
        if ("serveurs".equals(parts[i]) && i + 1 < parts.length) {
            try { return Integer.parseInt(parts[i + 1]); }
            catch (NumberFormatException e) { /* تجاهل */ }
        }
    }
    return 0;
}
```

**بالدارجة :** هاد الدالة كاتشريط المسار (split على /) ودور على "serveurs" وترجع لي بعدها. مثال : `/api/serveurs/5/consulter` → ترجع 5.

**serveursToJson()** — تحويل السيرڤورات لـ JSON

```java
private String serveursToJson(List<Serveur> serveurs) {
    StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (Serveur s : serveurs) {
        if (!first) sb.append(",");
        first = false;
        sb.append("{")
          .append(JsonUtil.jsonInt("id", s.getId())).append(",")
          .append(JsonUtil.jsonString("nom", s.getNom())).append(",")
          .append(JsonUtil.jsonString("adresseIP", s.getAdresseIP())).append(",")
          .append(JsonUtil.jsonString("login", s.getLogin())).append(",")
          .append(JsonUtil.jsonString("motDePasse",
              s.getMotDePasseClair() != null ? s.getMotDePasseClair() : "")).append(",")
          .append(JsonUtil.jsonString("dateDernierChangement",
              s.getDateDernierChangement() != null
              ? s.getDateDernierChangement().toString() : ""))
          .append("}");
    }
    sb.append("]");
    return sb.toString();
}
```

**بالدارجة :** هاد الدالة كاتحول لـ JSON يدوي (بدون مكتبة خارجية). `JsonUtil.jsonString` و `JsonUtil.jsonInt` هوما دوال مساعدة كايصنعو `"key":"value"` مع الهروب (escaping) من `\` و `"` و `\n`. `motDePasse` هنا كايجي من `getMotDePasseClair()` لي كايكون مشفر قبل ويعطيه فارغ.

---

### 10. شرح الواجهة الأمامية (Frontend) / Le Frontend

**web/serveurs.html — chargerListe()**

```javascript
async function chargerListe() {
    try {
        const res = await API.getServeurs();
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        res.data.forEach(s => {
            tbody.innerHTML += '<tr>' +
                '<td>' + s.nom + '</td>' +
                '<td>' + (s.adresseIP||'') + '</td>' +
                '<td>' + (s.login||'') + '</td>' +
                '<td class="pwd-masked">' + (s.motDePasse||'') + '</td>' +
                '<td>' + (s.dateDernierChangement
                    ? new Date(s.dateDernierChangement).toLocaleDateString('fr-FR')
                    : '') + '</td>' +
                '<td class="action-btns">' +
                    (window.utilisateurRole === 'Administrateur'
                        ? '<button class="btn btn-sm btn-info" onclick="consulterSecret(' + s.id + ')">🔑 Voir</button>'
                        : '') +
                    '<button class="btn btn-sm btn-warning" onclick="ouvrirModif(...)">✏️ Modifier</button>' +
                    '<button class="btn btn-sm btn-secondary" onclick="ouvrirMdp(' + s.id + ')">🔒 Changer MDP</button>' +
                    '<button class="btn btn-sm btn-danger" onclick="supprimer(' + s.id + ')">🗑️ Supprimer</button>' +
                '</td></tr>';
        });
    } catch (e) { afficherErreur('Erreur chargement'); }
}
```

**بالدارجة :** هاد الدالة كاتجيب السيرڤورات من الـ API وتحطهم فالجـدول (tableau). كل سيرڤور كايولّي row فيه :
- السمية
- IP
- login
- كلمة السر (تما كاتعرض)
- تاريخ آخر تغيير
- الأزرار : Voir (إلا كان المستخدم Admin)، Modifier، Changer MDP، Supprimer

**API.consulterSecretServeur()**

```javascript
async function consulterSecret(id) {
    try {
        const res = await API.consulterSecretServeur(id);
        alert('Mot de passe: ' + (res.data.motDePasse || '(aucun)'));
    } catch (e) { afficherErreur(e.message); }
}
```

**بالدارجة :** فاش كايضغط على "Voir"، كايبعت طلب GET لـ `/api/serveurs/{id}/consulter`. السيرڤور كايفك التشفير ويرجع كلمة السر. إلا كان فيها مشكل، كايبان الخطأ فالشاشة.

**api.js — request()**

```javascript
async request(method, path, data = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'  // سيلة الكوكي (session)
    };
    if (data) options.body = JSON.stringify(data);

    const response = await fetch(this.BASE + path, options);
    const json = await response.json();

    if (!response.ok && !json.succes) {
        throw new Error(json.erreur || 'Erreur serveur');
    }
    return { status: response.status, data: json };
}
```

**بالدارجة :** `credentials: 'include'` غادي تبعث الـ cookie ديال الجلسة مع كل طلب. السيرڤور كايتأكد من صلاحية الجلسة قبل ما يخدم أي طلب. `fetch` كايخدمها باش نبعثو الطلبات (بدون jQuery ولا Axios).

---

### 11. الأمان / La sécurité

#### تشفير كلمات السر المستخدمين (PasswordHasher.java)

```java
public class PasswordHasher {
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;

    public static String hacher(String motDePasse, String sel) {
        PBEKeySpec spec = new PBEKeySpec(
            motDePasse.toCharArray(),
            sel.getBytes(StandardCharsets.UTF_8),
            ITERATIONS,
            KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}
```

**بالدارجة :** هاد الكلاس كايحول كلمات السر ديال المستخدمين لـ hash بـ 600,000 دورة من PBKDF2. 600,000 دورة كايعني أن أي محاولة اختراق (brute force) غادي تكون بطيئة بزاف. الـ `sel` (الملح) كايحمي من هجمات rainbow tables.

#### التحقق من الصلاحية (SecurityInterceptor.java)

```java
public Session verifierAcces(HttpExchange exchange, String... rolesAutorises) {
    String token = CookieUtil.extraireCookie(exchange, "SESSION_ID");
    Session session = sessionManager.obtenirSession(token);
    if (session == null) {
        ResponseUtil.json(exchange, 401, "{\"erreur\":\"Session invalide\"}");
        return null;
    }
    List<String> roles = Arrays.asList(rolesAutorises);
    if (roles.contains(session.getRole())) return session;

    ResponseUtil.json(exchange, 403, "{\"erreur\":\"Accès refusé\"}");
    return null;
}
```

**بالدارجة :** هاد الدالة كاتحقق من صلاحية المستخدم قبل ما يقوم بأي عملية :
1. تجيب الـ token من الكوكي
2. تجيب الجلسة من `SessionManager`
3. إلا كانت الجلسة ماشي صالحة → 401
4. إلا كان الدور (role) ماشي فالقائمة → 403
5. إلا كلشي مزيان → سمح بالعملية

**En français :** Le `SecurityInterceptor` est appelé AVANT chaque opération sensible. Il extrait le cookie `SESSION_ID`, récupère la session, vérifie le rôle. Retourne `null` pour bloquer le flux (la réponse HTTP a déjà été envoyée avec 401/403).

---

### 12. Base de données SQL Server

```sql
-- Constraint CHECK exemple
CONSTRAINT CK_DivisionExterne_Type CHECK (TypeDivision IN (N'AAL', N'Caïdat'))

-- Index pour performance
CREATE INDEX IX_Serveur_NomServeur
    ON dbo.Serveur (NomServeur)
    INCLUDE (AdresseIP, LoginServeur, DateDernierChangement);
```

**بالدارجة :** القاعدة كاتضمن تكامل البيانات :
- `CHECK` كايمنع تسجيل نوع ماشي AAL ولا Caïdat
- `UNIQUE` كايحمي من تكرار عناوين IP و MAC والبيانات
- `INDEX` كايخلي البحث سريع حتى مع الآلاف ديال السجلات

**En français :** Contraintes et index optimisent l'intégrité et la performance.

---

### 13. كيفاش كايخدم التطبيق من البداية حتى النهاية؟

1. **المستخدم كايدخل لـ http://localhost:8080** → `LoginHandler.java` كايتأكد من الإسم وكلمة السر
2. **من بعد الدخول** → `SessionHandler.java` كايصنع جلسة وكوكي
3. **فاش كايتنقل لـ Serveurs** → `ServeurHandler.lister()` → `ServeurService.lister()` → `ServeurDAO.lister()` → SQL → تفك التشفير
4. **فاش كايزيد سيرڤور** → `ServeurHandler.ajouter()` → `ServeurService.ajouter()` → `CryptoService.chiffrer()` → `ServeurDAO.ajouter()`
5. **فاش كايضغط على Voir** → `ServeurHandler` (consulter) → `ServeurService.consulterSecret()` → `CryptoService.dechiffrer()` → كلمة السر فـ alert
6. **فاش كايبحث** → `RechercheHandler` → `RechercheService` → جميع DAO → تفك التشفير → JSON

**بالدارجة :** هاد هو المسار الكامل. من الفتحة حتى النتيجة.

**En français :** Ceci est le flux complet, de l'authentification à l'affichage d'un secret.

---

### 14. ملخص التعديلات لي درناه فهاد الجلسة / Résumé des modifications de cette session

| التاريخ | التعديل | الوصف |
|---------|---------|-------|
| 2026-07-15 | إضافة عمود كلمة السر | Servers, Switches, Systèmes Internes/Externes — كلمة السر كاتعرض فالقائمة مباشرة |
| 2026-07-15 | DAO lister() | جلب `MotPasseChiffre` و `VecteurInitialisation` فجميع القوائم |
| 2026-07-15 | Service lister() | تفك تشفير كلمات السر فاش كايتجيبو القوائم |
| 2026-07-15 | تصدير (Export) | إضافة عمود "Mot de passe" فـ CSV و HTML |
| 2026-07-15 | enum TypeDivisionExterne | AAL و CAIDAT ولاّ enum بدل string |
| 2026-07-15 | Clé AES persistante | تخزين المفتاح فـ `.aes_key` باش ما يضيعش فاش كايعاود السيرڤور |
| 2026-07-15 | تحسين عرض الأخطاء | الأخطاء كايظهرو فالشاشة بدل "Erreur interne" |
| 2026-07-15 | Logo | تغيير logo ديال Province de Safi |
| 2026-07-15 | README.md | هاد الشرح الكامل |

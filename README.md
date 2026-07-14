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

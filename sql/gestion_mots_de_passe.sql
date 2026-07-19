/* ============================================================
   Base : GestionMotsDePasse_safi
   SGBD : Microsoft SQL Server
   Remarque sécurité : les secrets métier sont chiffrés par
   l'application (AES) avant l'insertion. Les mots de passe des
   utilisateurs sont hachés (PBKDF2), jamais chiffrés ni en clair.
   ============================================================ */

IF DB_ID(N'GestionMotsDePasse_safi') IS NULL
    CREATE DATABASE GestionMotsDePasse_safi;
GO

USE GestionMotsDePasse_safi;
GO

/* =========================
   1. Rôles et utilisateurs
   ========================= */
CREATE TABLE dbo.Role (
    IdRole       INT IDENTITY(1,1) NOT NULL,
    NomRole      NVARCHAR(50) NOT NULL,
    Description  NVARCHAR(255) NULL,
    CONSTRAINT PK_Role PRIMARY KEY CLUSTERED (IdRole),
    CONSTRAINT UQ_Role_NomRole UNIQUE (NomRole)
);
GO

CREATE TABLE dbo.Utilisateur (
    IdUtilisateur       INT IDENTITY(1,1) NOT NULL,
    Nom                 NVARCHAR(100) NOT NULL,
    Email               NVARCHAR(150) NOT NULL,
    Login               NVARCHAR(50) NOT NULL,
    MotPasseHash        NVARCHAR(500) NOT NULL,
    SelMotPasse         NVARCHAR(255) NULL,
    IdRole              INT NOT NULL,
    EstActif            BIT NOT NULL CONSTRAINT DF_Utilisateur_EstActif DEFAULT (1),
    DateCreation        DATETIME2(0) NOT NULL CONSTRAINT DF_Utilisateur_DateCreation DEFAULT (SYSUTCDATETIME()),
    DerniereConnexion   DATETIME2(0) NULL,
    CONSTRAINT PK_Utilisateur PRIMARY KEY CLUSTERED (IdUtilisateur),
    CONSTRAINT UQ_Utilisateur_Email UNIQUE (Email),
    CONSTRAINT UQ_Utilisateur_Login UNIQUE (Login),
    CONSTRAINT FK_Utilisateur_Role FOREIGN KEY (IdRole)
        REFERENCES dbo.Role(IdRole)
);
GO

/* =========================
   2. Réseau
   ========================= */
CREATE TABLE dbo.Serveur (
    IdServeur              INT IDENTITY(1,1) NOT NULL,
    NomServeur             NVARCHAR(100) NOT NULL,
    AdresseIP              VARCHAR(45) NOT NULL,
    LoginServeur           NVARCHAR(100) NOT NULL,
    MotPasseChiffre        NVARCHAR(MAX) NOT NULL,
    VecteurInitialisation  NVARCHAR(255) NULL,
    DateDernierChangement  DATETIME2(0) NOT NULL CONSTRAINT DF_Serveur_DateChangement DEFAULT (SYSUTCDATETIME()),
    DateCreation           DATETIME2(0) NOT NULL CONSTRAINT DF_Serveur_DateCreation DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_Serveur PRIMARY KEY CLUSTERED (IdServeur),
    CONSTRAINT UQ_Serveur_AdresseIP UNIQUE (AdresseIP)
);
GO

CREATE TABLE dbo.SwitchReseau (
    IdSwitch               INT IDENTITY(1,1) NOT NULL,
    NomSwitch              NVARCHAR(100) NOT NULL,
    Emplacement            NVARCHAR(150) NULL,
    AdresseMAC             VARCHAR(17) NOT NULL,
    LoginSwitch            NVARCHAR(100) NOT NULL,
    MotPasseChiffre        NVARCHAR(MAX) NOT NULL,
    VecteurInitialisation  NVARCHAR(255) NULL,
    DateDernierChangement  DATETIME2(0) NOT NULL CONSTRAINT DF_Switch_DateChangement DEFAULT (SYSUTCDATETIME()),
    DateCreation           DATETIME2(0) NOT NULL CONSTRAINT DF_Switch_DateCreation DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_SwitchReseau PRIMARY KEY CLUSTERED (IdSwitch),
    CONSTRAINT UQ_SwitchReseau_AdresseMAC UNIQUE (AdresseMAC)
);
GO

/* =========================
   3. Système d'information
   ========================= */
CREATE TABLE dbo.DivisionInterne (
    IdDivisionInterne  INT IDENTITY(1,1) NOT NULL,
    NomDivision        NVARCHAR(100) NOT NULL,
    Service            NVARCHAR(255) NULL,
    CONSTRAINT PK_DivisionInterne PRIMARY KEY CLUSTERED (IdDivisionInterne)
);
GO

CREATE UNIQUE NONCLUSTERED INDEX UQ_DivisionInterne_NomService ON dbo.DivisionInterne (NomDivision, Service) WHERE Service IS NOT NULL;
GO

CREATE TABLE dbo.DivisionExterne (
    IdDivisionExterne  INT IDENTITY(1,1) NOT NULL,
    NomDivision        NVARCHAR(100) NOT NULL,
    TypeDivision       NVARCHAR(20) NOT NULL,
    SousType           NVARCHAR(50) NULL,
    CaidatNom          NVARCHAR(255) NULL,
    CONSTRAINT PK_DivisionExterne PRIMARY KEY CLUSTERED (IdDivisionExterne),
    CONSTRAINT UQ_DivisionExterne_Nom UNIQUE (NomDivision),
    CONSTRAINT CK_DivisionExterne_Type CHECK (TypeDivision IN (N'AAL', N'Commune', N'Pashalik', N'District'))
);
GO

CREATE TABLE dbo.SystemeInterne (
    IdSystemeInterne        INT IDENTITY(1,1) NOT NULL,
    NomSysteme              NVARCHAR(100) NOT NULL,
    UrlSysteme              NVARCHAR(255) NULL,
    LoginAdmin              NVARCHAR(100) NOT NULL,
    MotPasseAdminChiffre    NVARCHAR(MAX) NOT NULL,
    VecteurInitialisation   NVARCHAR(255) NULL,
    IdDivisionInterne       INT NOT NULL,
    DateDernierChangement   DATETIME2(0) NOT NULL CONSTRAINT DF_SystemeInterne_DateChangement DEFAULT (SYSUTCDATETIME()),
    DateCreation            DATETIME2(0) NOT NULL CONSTRAINT DF_SystemeInterne_DateCreation DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_SystemeInterne PRIMARY KEY CLUSTERED (IdSystemeInterne),
    CONSTRAINT FK_SystemeInterne_DivisionInterne FOREIGN KEY (IdDivisionInterne)
        REFERENCES dbo.DivisionInterne(IdDivisionInterne)
);
GO

CREATE TABLE dbo.SystemeExterne (
    IdSystemeExterne        INT IDENTITY(1,1) NOT NULL,
    NomSysteme              NVARCHAR(100) NOT NULL,
    UrlSysteme              NVARCHAR(255) NULL,
    LoginSysteme            NVARCHAR(100) NOT NULL,
    MotPasseChiffre         NVARCHAR(MAX) NOT NULL,
    VecteurInitialisation   NVARCHAR(255) NULL,
    IdDivisionExterne       INT NOT NULL,
    /* Nullable : le cahier des charges indique qu'un système
       externe peut être lié à un système interne. */
    IdSystemeInterne        INT NULL,
    DateDernierChangement   DATETIME2(0) NOT NULL CONSTRAINT DF_SystemeExterne_DateChangement DEFAULT (SYSUTCDATETIME()),
    DateCreation            DATETIME2(0) NOT NULL CONSTRAINT DF_SystemeExterne_DateCreation DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_SystemeExterne PRIMARY KEY CLUSTERED (IdSystemeExterne),
    CONSTRAINT FK_SystemeExterne_DivisionExterne FOREIGN KEY (IdDivisionExterne)
        REFERENCES dbo.DivisionExterne(IdDivisionExterne),
    CONSTRAINT FK_SystemeExterne_SystemeInterne FOREIGN KEY (IdSystemeInterne)
        REFERENCES dbo.SystemeInterne(IdSystemeInterne)
);
GO

/* =========================
   4. Notifications et audit
   ========================= */
CREATE TABLE dbo.Notification (
    IdNotification   BIGINT IDENTITY(1,1) NOT NULL,
    Message          NVARCHAR(500) NOT NULL,
    TypeNotification NVARCHAR(50) NOT NULL CONSTRAINT DF_Notification_Type DEFAULT (N'INFORMATION'),
    DateNotification DATETIME2(0) NOT NULL CONSTRAINT DF_Notification_Date DEFAULT (SYSUTCDATETIME()),
    Lu               BIT NOT NULL CONSTRAINT DF_Notification_Lu DEFAULT (0),
    DateLecture      DATETIME2(0) NULL,
    IdUtilisateur    INT NOT NULL,
    CONSTRAINT PK_Notification PRIMARY KEY CLUSTERED (IdNotification),
    CONSTRAINT FK_Notification_Utilisateur FOREIGN KEY (IdUtilisateur)
        REFERENCES dbo.Utilisateur(IdUtilisateur),
    CONSTRAINT CK_Notification_Type CHECK (TypeNotification IN (N'INFORMATION', N'CHANGEMENT_MDP', N'ALERTE_ROTATION', N'AJOUT_COMPTE'))
);
GO

CREATE TABLE dbo.LogAudit (
    IdLog         BIGINT IDENTITY(1,1) NOT NULL,
    Action        NVARCHAR(30) NOT NULL,
    Entite        NVARCHAR(50) NOT NULL,
    IdCible       INT NULL,
    Cible         NVARCHAR(255) NULL,
    Details       NVARCHAR(MAX) NULL,
    AdresseIP     VARCHAR(45) NULL,
    DateAction    DATETIME2(0) NOT NULL CONSTRAINT DF_LogAudit_DateAction DEFAULT (SYSUTCDATETIME()),
    IdUtilisateur INT NOT NULL,
    CONSTRAINT PK_LogAudit PRIMARY KEY CLUSTERED (IdLog),
    CONSTRAINT FK_LogAudit_Utilisateur FOREIGN KEY (IdUtilisateur)
        REFERENCES dbo.Utilisateur(IdUtilisateur),
    CONSTRAINT CK_LogAudit_Action CHECK (Action IN (N'CONSULTATION', N'AJOUT', N'MODIFICATION', N'SUPPRESSION', N'EXPORT', N'CONNEXION', N'DECONNEXION'))
);
GO

/* =========================
   5. Index de performance
   Les contraintes UNIQUE créent déjà des index sur :
   Role.NomRole, Utilisateur.Email/Login, Serveur.AdresseIP,
   SwitchReseau.AdresseMAC et les noms de division.
   ========================= */

-- Connexion, gestion des comptes et filtrage par rôle
CREATE INDEX IX_Utilisateur_IdRole_EstActif
    ON dbo.Utilisateur (IdRole, EstActif);
GO

-- Recherche des équipements réseau
CREATE INDEX IX_Serveur_NomServeur
    ON dbo.Serveur (NomServeur)
    INCLUDE (AdresseIP, LoginServeur, DateDernierChangement);
GO

CREATE INDEX IX_SwitchReseau_Nom_Emplacement
    ON dbo.SwitchReseau (NomSwitch, Emplacement)
    INCLUDE (AdresseMAC, LoginSwitch, DateDernierChangement);
GO

-- Listes et filtres des systèmes par division
CREATE INDEX IX_SystemeInterne_Division_Nom
    ON dbo.SystemeInterne (IdDivisionInterne, NomSysteme)
    INCLUDE (UrlSysteme, LoginAdmin, DateDernierChangement);
GO

CREATE INDEX IX_SystemeExterne_Division_Nom
    ON dbo.SystemeExterne (IdDivisionExterne, NomSysteme)
    INCLUDE (UrlSysteme, LoginSysteme, IdSystemeInterne, DateDernierChangement);
GO

CREATE INDEX IX_SystemeExterne_SystemeInterne
    ON dbo.SystemeExterne (IdSystemeInterne)
    WHERE IdSystemeInterne IS NOT NULL;
GO

-- Centre de notifications : chargement rapide des non-lues
CREATE INDEX IX_Notification_Utilisateur_Lu_Date
    ON dbo.Notification (IdUtilisateur, Lu, DateNotification DESC)
    INCLUDE (Message, TypeNotification);
GO

-- Historique et audit par utilisateur, date et cible
CREATE INDEX IX_LogAudit_Utilisateur_Date
    ON dbo.LogAudit (IdUtilisateur, DateAction DESC)
    INCLUDE (Action, Entite, IdCible, Cible);
GO

CREATE INDEX IX_LogAudit_Entite_Cible_Date
    ON dbo.LogAudit (Entite, IdCible, DateAction DESC);
GO

/* =========================
   6. Données initiales
   ========================= */
INSERT INTO dbo.Role (NomRole, Description)
VALUES
    (N'Administrateur', N'Administration complète de l''application et des comptes.'),
    (N'Agent SSICTD', N'Gestion opérationnelle des secrets et des équipements.'),
    (N'Utilisateur consultation', N'Consultation contrôlée selon les droits accordés.');
GO

INSERT INTO dbo.DivisionInterne (NomDivision)
VALUES (N'SSICTD');
GO

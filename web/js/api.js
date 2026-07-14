// ============================================================
// API Client - Gestion des Mots de Passe
// ============================================================

const API = {
    BASE: '',

    async request(method, path, data = null) {
        const options = {
            method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include'
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

    // --- Auth ---
    async login(login, motDePasse) {
        return this.post('/api/login', { login, motDePasse });
    },
    async logout() {
        return this.post('/api/logout');
    },
    async checkSession() {
        return this.get('/api/session');
    },

    // --- Serveurs ---
    getServeurs() { return this.get('/api/serveurs'); },
    ajouterServeur(data) { return this.post('/api/serveurs', data); },
    modifierServeur(id, data) { return this.put('/api/serveurs/' + id, data); },
    supprimerServeur(id) { return this.del('/api/serveurs/' + id); },
    consulterSecretServeur(id) { return this.get('/api/serveurs/' + id + '/consulter'); },
    modifierMotDePasseServeur(id, data) { return this.put('/api/serveurs/' + id + '/secret', data); },
    exporterServeursCSV() { window.open('/api/serveurs/export?format=csv', '_blank'); },
    exporterServeursPDF() { window.open('/api/serveurs/export', '_blank'); },

    // --- Switches ---
    getSwitches() { return this.get('/api/switches'); },
    ajouterSwitch(data) { return this.post('/api/switches', data); },
    modifierSwitch(id, data) { return this.put('/api/switches/' + id, data); },
    supprimerSwitch(id) { return this.del('/api/switches/' + id); },
    consulterSecretSwitch(id) { return this.get('/api/switches/' + id + '/consulter'); },
    modifierMotDePasseSwitch(id, data) { return this.put('/api/switches/' + id + '/secret', data); },
    exporterSwitchesCSV() { window.open('/api/switches/export?format=csv', '_blank'); },
    exporterSwitchesPDF() { window.open('/api/switches/export', '_blank'); },

    // --- Systèmes internes ---
    getSystemesInternes() { return this.get('/api/systemes-internes'); },
    ajouterSystemeInterne(data) { return this.post('/api/systemes-internes', data); },
    modifierSystemeInterne(id, data) { return this.put('/api/systemes-internes/' + id, data); },
    supprimerSystemeInterne(id) { return this.del('/api/systemes-internes/' + id); },
    consulterSecretSystemeInterne(id) { return this.get('/api/systemes-internes/' + id + '/consulter'); },
    modifierMotDePasseSystemeInterne(id, data) { return this.put('/api/systemes-internes/' + id + '/secret', data); },
    exporterSystemesInternesCSV() { window.open('/api/systemes-internes/export?format=csv', '_blank'); },
    exporterSystemesInternesPDF() { window.open('/api/systemes-internes/export', '_blank'); },

    // --- Systèmes externes ---
    getSystemesExternes() { return this.get('/api/systemes-externes'); },
    ajouterSystemeExterne(data) { return this.post('/api/systemes-externes', data); },
    modifierSystemeExterne(id, data) { return this.put('/api/systemes-externes/' + id, data); },
    supprimerSystemeExterne(id) { return this.del('/api/systemes-externes/' + id); },
    consulterSecretSystemeExterne(id) { return this.get('/api/systemes-externes/' + id + '/consulter'); },
    modifierMotDePasseSystemeExterne(id, data) { return this.put('/api/systemes-externes/' + id + '/secret', data); },
    exporterSystemesExternesCSV() { window.open('/api/systemes-externes/export?format=csv', '_blank'); },
    exporterSystemesExternesPDF() { window.open('/api/systemes-externes/export', '_blank'); },

    // --- Divisions ---
    getDivisionsInternes() { return this.get('/api/divisions/internes'); },
    getDivisionsExternes() { return this.get('/api/divisions/externes'); },
    ajouterDivisionInterne(data) { return this.post('/api/divisions/internes', data); },
    ajouterDivisionExterne(data) { return this.post('/api/divisions/externes', data); },
    modifierDivisionInterne(id, data) { return this.put('/api/divisions/internes/' + id, data); },
    modifierDivisionExterne(id, data) { return this.put('/api/divisions/externes/' + id, data); },
    supprimerDivisionInterne(id) { return this.del('/api/divisions/internes/' + id); },
    supprimerDivisionExterne(id) { return this.del('/api/divisions/externes/' + id); },

    // --- Notifications ---
    getNotifications() { return this.get('/api/notifications'); },
    getNotificationsNonLues() { return this.get('/api/notifications'); },
    getCompteurNotifications() { return this.get('/api/notifications/compteur'); },
    marquerNotificationLue(id) { return this.put('/api/notifications/' + id + '/lire'); },

    // --- Audit ---
    getAudit() { return this.get('/api/audit'); },

    // --- Recherche ---
    rechercher(texte, type) {
        let url = '/api/recherche?texte=' + encodeURIComponent(texte);
        if (type) url += '&type=' + encodeURIComponent(type);
        return this.get(url);
    }
};

// ============================================================
// SSE - Notifications push en temps réel
// ============================================================

let sseSource = null;
let toastTimer = null;

function connecterSSE() {
    if (sseSource) { sseSource.close(); }
    sseSource = new EventSource('/api/notifications/sse');

    sseSource.addEventListener('connected', () => {
        console.log('SSE connecté');
    });

    sseSource.addEventListener('notification', (e) => {
        try {
            const data = JSON.parse(e.data);
            afficherToast(data.message, data.type || 'INFO');
            mettreAJourBadge();
        } catch (err) { /* ignorer */ }
    });

    sseSource.addEventListener('error', () => {
        sseSource.close();
        setTimeout(connecterSSE, 5000);
    });
}

function afficherToast(message, type) {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    const cls = 'toast toast-' + (type || 'info').toLowerCase();
    toast.className = cls;
    toast.innerHTML = '<span style="flex:1;">' + message + '</span><button class="toast-close" onclick="fermerToast(this.parentElement)">&times;</button>';
    container.appendChild(toast);

    setTimeout(() => fermerToast(toast), 6000);
}

function fermerToast(toast) {
    if (!toast || !toast.parentElement) return;
    toast.style.animation = 'toastOut 0.3s ease-in forwards';
    setTimeout(() => { if (toast.parentElement) toast.remove(); }, 300);
}

async function mettreAJourBadge() {
    try {
        const res = await API.getCompteurNotifications();
        const badge = document.getElementById('notifBadge');
        if (badge) {
            const count = res.data.nonLues || 0;
            if (count > 0) { badge.textContent = count; badge.style.display = 'inline'; }
            else { badge.style.display = 'none'; }
        }
    } catch (e) { /* ignorer */ }
}

// ============================================================
// Utilitaires UI
// ============================================================

function afficherErreur(msg) {
    const el = document.getElementById('alertErreur');
    if (el) { el.textContent = msg; el.style.display = 'block'; setTimeout(() => el.style.display = 'none', 4000); }
}

function afficherSucces(msg) {
    const el = document.getElementById('alertSucces');
    if (el) { el.textContent = msg; el.style.display = 'block'; setTimeout(() => el.style.display = 'none', 3000); }
}

function afficherMessage(id, msg, type) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = msg;
        el.className = 'alert alert-' + type;
        el.style.display = 'block';
        setTimeout(() => el.style.display = 'none', 4000);
    }
}

// --- Dark Mode ---
(function initTheme() {
    const theme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', theme);
})();

function toggleTheme() {
    const html = document.documentElement;
    const current = html.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
    const icon = document.getElementById('themeIcon');
    if (icon) icon.textContent = next === 'dark' ? '☀️' : '🌙';
}
window.toggleTheme = toggleTheme;

function ouvrirModal(id) { document.getElementById(id).classList.add('active'); }
function fermerModal(id) { document.getElementById(id).classList.remove('active'); }

// Fermer modal en cliquant sur l'overlay
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});

// ============================================================
// Vérification session au chargement
// ============================================================

document.addEventListener('DOMContentLoaded', async function() {
    // Vérifier si on est sur la page de login
    if (window.location.pathname.endsWith('login.html')) return;

    try {
        const res = await API.checkSession();
        if (res.data.succes) {
            const el = document.getElementById('nomUtilisateur');
            if (el) el.textContent = res.data.nomUtilisateur;
            window.utilisateurRole = res.data.role;
            connecterSSE();
        }
    } catch (e) {
        // Rediriger vers login si pas de session
        window.location.href = '/login.html';
    }
});

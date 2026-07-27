#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Import des systemes SI depuis login_mot_de_passe_SI .xlsx + Comptes SI au niveau AALS.xlsx

Systemes Internes (Feuil1) → POST /api/systemes-internes
Systemes Externes (AALS sheets) → POST /api/systemes-externes
Divisions → POST /api/divisions[/internes]
"""

import sys
sys.stdout.reconfigure(encoding='utf-8')

import re, openpyxl, http.client, json

API_HOST = "localhost"
API_PORT = 8080

def api(method, path, body=None, cookie=None):
    conn = http.client.HTTPConnection(API_HOST, API_PORT, timeout=15)
    h = {'Content-Type': 'application/json'}
    if cookie: h['Cookie'] = cookie
    b = json.dumps(body, ensure_ascii=False).encode('utf-8') if body else None
    conn.request(method, path, b, headers=h)
    r = conn.getresponse()
    d = r.read().decode('utf-8')
    conn.close()
    return r.status, d

# --- Login ---
conn = http.client.HTTPConnection(API_HOST, API_PORT, timeout=10)
conn.request('POST', '/api/login', json.dumps({'login':'admin','motDePasse':'admin123'}).encode(), {'Content-Type':'application/json'})
r = conn.getresponse() ; r.read()
SC = r.getheader('Set-Cookie','').split(';')[0]
conn.close()
print('Login OK')

ok = 0 ; fail = 0

# ============ PART 1: Systemes Internes (Feuil1) ============
print('\n============ 1. Systemes Internes ============')

wb = openpyxl.load_workbook('F:/login_mot_de_passe_SI .xlsx')
ws = wb['Feuil1']
rows = []
for r in range(1, ws.max_row+1):
    rows.append([ws.cell(r, c).value for c in range(1, ws.max_column+1)])

# 1.1 Creer/recuperer divisions internes
div_map = {}  # nom -> id
current_div = None
current_url = None

# Get existing divisions internes
s, d = api('GET', '/api/divisions/internes', None, SC)
if s == 200:
    for x in json.loads(d):
        if x.get('nom'):
            div_map[x['nom'].upper()] = x['id']

def get_or_create_int_div(name, service=''):
    key = name.upper().strip()
    if key in div_map:
        return div_map[key]
    norm = {'SPS':'SPS','DAEC':'DAEC','DAI':'DAI','DPE':'DPE','DAS':'DAS','DCT':'DCT','CHIKAYA':'CHIKAYA'}.get(key, name.title())
    st, dd = api('POST', '/api/divisions/internes', {'nom': norm, 'service': name}, SC)
    if st == 201:
        # re-fetch to get id
        s2, d2 = api('GET', '/api/divisions/internes', None, SC)
        if s2 == 200:
            for x2 in json.loads(d2):
                if x2.get('nom', '').upper() == norm.upper():
                    div_map[key] = x2['id']
                    print(f'  Div interne creee: {norm} (id={x2["id"]})')
                    return x2['id']
    print(f'  Div interne: {norm} -> id=0 (fallback)')
    div_map[key] = 0
    return 0

# 1.2 Importer les entrees Feuil1
imp_si_count = 0
for ridx, row in enumerate(rows[1:], 2):
    div_raw = str(row[0] or '').strip()
    url_raw = str(row[1] or '').strip()
    login_raw = str(row[2] or '').strip()
    pwd_raw = str(row[3] or '').strip()
    obs = str(row[4] or '').strip()

    if div_raw:
        current_div = div_raw.upper()
    if url_raw:
        current_url = url_raw.strip()
        # Normalize URL: remove leading newline
        current_url = current_url.replace('\n', '').strip()

    if not login_raw or not pwd_raw:
        continue
    login = login_raw.strip()
    pwd = pwd_raw.strip()
    if len(pwd) < 3 or len(login) < 2:
        continue
    if 'login :' in login.lower() or 'mot de passe' in pwd.lower() or pwd == '111':
        continue
    if not current_div:
        continue

    div_id = get_or_create_int_div(current_div)
    domain = re.sub(r'https?://', '', current_url).split('/')[0].replace('www.', '') if current_url else current_div.lower()
    nom_sys = f"{current_div}-{domain.split('.')[0]}"
    # Truncate to avoid overly long names
    if len(nom_sys) > 50:
        nom_sys = nom_sys[:50]

    payload = {'nom': nom_sys, 'url': current_url, 'login': login, 'motDePasse': pwd, 'idDivision': str(div_id)}
    st, dd = api('POST', '/api/systemes-internes', payload, SC)
    if st == 201:
        imp_si_count += 1
        ok += 1
        print(f'  OK #{json.loads(dd).get("id","?")} {nom_sys}')
    elif st == 400 and ('existe' in dd.lower() or 'contrainte' in dd.lower()):
        print(f'  EXISTS {nom_sys}')
    else:
        fail += 1
        print(f'  FAIL {nom_sys}: HTTP {st} {dd[:100]}')

print(f'\n  => {imp_si_count} systemes internes importes')

# ============ PART 2A: Divisions Externes (AALS) ============
print('\n============ 2. Divisions Externes AALS ============')

# Load existing
ext_divs = {}
s, d = api('GET', '/api/divisions', None, SC)
if s == 200:
    for x in json.loads(d):
        ext_divs[x['nom'].upper()] = x['id']

def get_or_create_ext_div(nom, type_div='AAL', sous_type='', caidat=''):
    key = nom.upper().strip()
    if key in ext_divs:
        return ext_divs[key]
    st, dd = api('POST', '/api/divisions', {'nom': nom, 'type': type_div, 'sousType': sous_type, 'caidatNom': caidat}, SC)
    if st == 201:
        s2, d2 = api('GET', '/api/divisions', None, SC)
        if s2 == 200:
            for x2 in json.loads(d2):
                if x2['nom'].upper() == key:
                    ext_divs[key] = x2['id']
                    print(f'  Div ext creee: {nom} id={x2["id"]}')
                    return x2['id']
    print(f'  ! Div ext {nom}: HTTP {st} {dd[:60]}')
    ext_divs[key] = 0
    return 0

# ============ PART 2B: Feuil2 - MDP passeport ============
print('\n============ 3. Comptes passeport (Feuil2) ============')
ws2 = wb['Feuil2']
div_feuil2 = get_or_create_ext_div('Passeport-Safi', 'AAL', 'Passeport', '')
for r in range(4, ws2.max_row+1):
    login = str(ws2.cell(r, 1).value or '').strip()
    pwd = str(ws2.cell(r, 2).value or '').strip()
    if login and pwd and len(login) > 2:
        # Create as SystemeExterne
        payload = {'nom': f"Passeport-{login}", 'url': '', 'login': login, 'motDePasse': pwd, 'idDivision': str(div_feuil2)}
        st, dd = api('POST', '/api/systemes-externes', payload, SC)
        if st == 201:
            ok += 1
            print(f'  OK Passeport-{login}')
        elif st == 400 and 'existe' in dd.lower():
            print(f'  EXISTS Passeport-{login}')
        else:
            fail += 1
            print(f'  FAIL Passeport-{login}: {dd[:60]}')

# ============ PART 2C: AALS Associations ============
print('\n============ 4. Comptes Association ============')
for r in range(9, 35):
    login = str(ws2.cell(r, 5).value or '').strip()
    pwd = str(ws2.cell(r, 6).value or '').strip()
    if login and pwd and len(login) > 1:
        div_id = get_or_create_ext_div('Association', 'AAL', 'Association', '')
        payload = {'nom': f"Assoc-{login}", 'url': 'https://association.mi-app.ma/Association/', 'login': login, 'motDePasse': pwd, 'idDivision': str(div_id)}
        st, dd = api('POST', '/api/systemes-externes', payload, SC)
        if st == 201: ok += 1
        elif st == 400 and 'existe' in dd.lower(): pass
        else: fail += 1 ; print(f'  FAIL Assoc {login}: {dd[:60]}')

print(f'\n=== RESUME ===')
print(f'OK: {ok}, FAIL: {fail}')
print('Termine')
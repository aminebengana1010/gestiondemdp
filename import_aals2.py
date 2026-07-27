#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Import restant AALS - avec re-login automatique si session expire"""

import sys, openpyxl, http.client, json, time
sys.stdout.reconfigure(encoding='utf-8')

HOST = "localhost"; PORT = 8080

def _login():
    co = http.client.HTTPConnection(HOST, PORT, timeout=10)
    co.request('POST','/api/login', json.dumps({'login':'admin','motDePasse':'admin123'}).encode(), {'Content-Type':'application/json'})
    r = co.getresponse(); r.read()
    s = r.getheader('Set-Cookie','').split(';')[0]; co.close()
    return s

SC = _login()

def api(m, p, b=None, retry=1):
    global SC
    for attempt in range(retry + 1):
        try:
            co = http.client.HTTPConnection(HOST, PORT, timeout=15)
            h = {'Content-Type':'application/json', 'Cookie': SC}
            bo = json.dumps(b, ensure_ascii=False).encode('utf-8') if b else None
            co.request(m, p, bo, headers=h)
            r = co.getresponse()
            d = r.read().decode('utf-8')
            co.close()
            if r.status == 401 and attempt < retry:
                SC = _login()
                time.sleep(0.5)
                continue
            return r.status, d
        except Exception as e:
            if attempt < retry:
                SC = _login()
                time.sleep(1)
                continue
            return 0, str(e)

def get_divs():
    s, d = api('GET', '/api/divisions')
    dd = {}
    if s == 200:
        for x in json.loads(d):
            dd[x['nom'].upper().strip()] = x['id']
    return dd

ext_divs = get_divs()

def gocd(nom, tp='AAL', ss='', caidat=''):
    k = nom.upper().strip()
    if k in ext_divs: return ext_divs[k]
    s, d = api('POST', '/api/divisions', {'nom':nom,'type':tp,'sousType':ss,'caidatNom':caidat})
    if s == 201:
        dd2 = get_divs()
        ext_divs.update(dd2)
        k2 = nom.upper().strip()
        if k2 in ext_divs: return ext_divs[k2]
    return 0

ok = 0; ok2 = 0
wb = openpyxl.load_workbook('F:/Comptes SI au niveau AALS.xlsx')

# ============ Comptes SGDA (remaining) ============
print('\n============ SGDA ============')
ws = wb['Comptes SGDA']
for r in range(5, ws.max_row + 1):
    caidat = str(ws.cell(r,2).value or '').strip()
    login = str(ws.cell(r,3).value or '').strip()
    pwd   = str(ws.cell(r,4).value or '').strip()
    if not login or not pwd or 'اسم' in login: continue
    did = gocd(f'SGDA-{login}', 'AAL', 'SGDA', caidat[:50])
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'SGDA-{login}', 'url': 'http://sgda.mi-app.ma/SGDA/',
        'login': login, 'motDePasse': pwd, 'idDivision': str(did)
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL SGDA {login}: {d[:60]}')
print(f'  SGDA: {ok} nouveaux, {ok2} existants')

# ============ Comptes HAJJ -BO ============
print('\n============ HAJJ -BO ============')
ws = wb['Comptes HAJJ -BO']
for r in range(4, ws.max_row + 1):
    ent = str(ws.cell(r,1).value or '').strip()
    for lc, pc in [(7,8),(13,14)]:
        login = str(ws.cell(r,lc).value or '').strip()
        pwd   = str(ws.cell(r,pc).value or '').strip()
        if not login or not pwd or 'mot de' in pwd.lower() or '123***' in pwd: continue
        dn = ent if ent else 'Hajj-Safi'
        did = gocd(f'Hajj-{dn[:40]}', 'AAL', 'Hajj', ent[:40])
        s, d = api('POST', '/api/systemes-externes', {
            'nom': f'Hajj-{login}', 'url': '',
            'login': login, 'motDePasse': pwd, 'idDivision': str(did)
        })
        if s == 201: ok += 1
        elif 'existe' in d.lower(): ok2 += 1
        else: print(f'  FAIL Hajj {login}: {d[:60]}')
print(f'  HAJJ: {ok} nouveaux (cumul)')

# ============ Compte SSCOV ============
print('\n============ SSCOV ============')
ws = wb['Compte SSCOV']
for r in range(3, ws.max_row + 1):
    an = str(ws.cell(r,3).value or '').strip()
    login = str(ws.cell(r,4).value or '').strip()
    pwd   = str(ws.cell(r,5).value or '').strip()
    if not login or not pwd or 'Login' in login: continue
    did = gocd(f'SSCOV-{login}', 'AAL', 'SSCOV', an[:50])
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'SSCOV-{login}', 'url': '',
        'login': login, 'motDePasse': pwd, 'idDivision': str(did)
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL SSCOV {login}: {d[:60]}')
print(f'  SSCOV: {ok} nouveaux (cumul)')

# ============ tadamon Provinciaux ============
print('\n============ Tadamon Provinciaux ============')
ws = wb['tadamon Provinciaux']
for r in range(6, ws.max_row+1):
    login = str(ws.cell(r,4).value or '').strip()
    pwd   = str(ws.cell(r,5).value or '').strip()
    obs   = str(ws.cell(r,6).value or '').strip()
    if not login or not pwd or 'desactive' in obs.lower(): continue
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'Tadamon-{login}', 'url': 'http://amotadamon.mi-app.ma/',
        'login': login, 'motDePasse': pwd, 'idDivision': '0'
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL TadaProv {login}: {d[:60]}')
print(f'  TadaProv: {ok} nouveaux (cumul)')

# ============ all caidat_tadamon ============
print('\n============ Tadamon Caidats ============')
ws = wb['all caidat_tadamon']
for r in range(5, ws.max_row+1):
    ent = str(ws.cell(r,1).value or '').strip()
    login = str(ws.cell(r,2).value or '').strip()
    pwd   = str(ws.cell(r,4).value or '').strip()
    if not login or not pwd or 'LOGIN' in login: continue
    dn = f'Tadamon-{ent[:30]}' if ent else 'Tadamon-Caidat'
    did = gocd(dn[:40], 'AAL', 'Tadamon', ent[:50])
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'Tadamon-{login}', 'url': 'http://amotadamon.mi-app.ma/',
        'login': login, 'motDePasse': pwd, 'idDivision': str(did)
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL TadCaid {login}: {d[:60]}')
print(f'  TadCaid: {ok} nouveaux (cumul)')

# ============ compte tajnid ============
print('\n============ Tajnid ============')
ws = wb['compte tajnid']
for r in range(1, ws.max_row+1):
    caidat = str(ws.cell(r,2).value or '').strip()
    login = str(ws.cell(r,6).value or '').strip()
    pwd   = str(ws.cell(r,7).value or '').strip()
    if not login or not pwd or 'login' in login.lower() or 'mot de' in pwd.lower(): continue
    if not caidat and not login: continue
    dn = f'Tajnid-{caidat[:25]}' if caidat else f'Tajnid-{login}'
    did = gocd(dn[:40], 'AAL', 'Tajnid', caidat[:50])
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'Tajnid-{login}', 'url': 'http://tajnid.mi-app.ma/',
        'login': login, 'motDePasse': pwd, 'idDivision': str(did)
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL Tajnid {login}: {d[:60]}')
print(f'  Tajnid: {ok} nouveaux (cumul)')

# ============ comptes SgPanier ============
print('\n============ SgPanier ============')
ws = wb['comptes SgPanier']
for r in range(6, ws.max_row+1):
    caidat = str(ws.cell(r,2).value or '').strip()
    login = str(ws.cell(r,6).value or '').strip()
    pwd   = str(ws.cell(r,7).value or '').strip()
    if not login or not pwd or 'Nom' in login: continue
    dn = f'SgPanier-{caidat[:25]}' if caidat else f'SgPanier-{login}'
    did = gocd(dn[:40], 'AAL', 'SgPanier', caidat[:50])
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'SgPanier-{login}', 'url': 'https://sgpanierfmv.mi-app.ma/',
        'login': login, 'motDePasse': pwd, 'idDivision': str(did)
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL SgPanier {login}: {d[:60]}')
print(f'  SgPanier: {ok} nouveaux (cumul)')

# ============ compte Le-SGE ============
print('\n============ Le-SGE ============')
ws = wb['compte Le-SGE']
for r in range(1, ws.max_row+1):
    aal = str(ws.cell(r,1).value or '').strip()
    login = str(ws.cell(r,2).value or '').strip()
    pwd   = str(ws.cell(r,3).value or '').strip()
    if not login or not pwd or 'Login' in login: continue
    dn = f'LeSGE-{aal[:25]}' if aal else f'LeSGE-{login}'
    did = gocd(dn[:40], 'AAL', 'Le-SGE', aal[:50])
    s, d = api('POST', '/api/systemes-externes', {
        'nom': f'LeSGE-{login}', 'url': '',
        'login': login, 'motDePasse': pwd, 'idDivision': str(did)
    })
    if s == 201: ok += 1
    elif 'existe' in d.lower(): ok2 += 1
    else: print(f'  FAIL LeSGE {login}: {d[:60]}')
print(f'  LeSGE: {ok} nouveaux (cumul)')

print(f'\n=== FINAL: {ok} nouveaux importes, {ok2} deja existants ===')
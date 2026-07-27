#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Re-import AALS sheets (after timeout), with re-login per sheet"""

import sys, openpyxl, http.client, json
sys.stdout.reconfigure(encoding='utf-8')

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
    return r.status, d, r.getheader('Set-Cookie')

def login():
    conn = http.client.HTTPConnection(API_HOST, API_PORT, timeout=10)
    conn.request('POST', '/api/login', json.dumps({'login':'admin','motDePasse':'admin123'}).encode(), {'Content-Type':'application/json'})
    r = conn.getresponse(); r.read()
    sc = r.getheader('Set-Cookie','').split(';')[0]
    conn.close()
    return sc

SC = login(); print('Login OK')
ok = 0; fail = 0

# Reload existing divisions on each sheet
def refresh_divs():
    s, d, _ = api('GET', '/api/divisions', None, SC)
    dd = {}
    if s == 200:
        for x in json.loads(d):
            dd[x['nom'].upper().strip()] = x['id']
    return dd

ext_divs = refresh_divs()

def get_or_create_div(nom, type_div='AAL', ss='', caidat=''):
    k = nom.upper().strip()
    if k in ext_divs: return ext_divs[k]
    s, d, _ = api('POST', '/api/divisions', {'nom':nom,'type':type_div,'sousType':ss,'caidatNom':caidat}, SC)
    if s == 201:
        s2, d2, _ = api('GET', '/api/divisions', None, SC)
        if s2 == 200:
            for x in json.loads(d2):
                if x['nom'].upper().strip() == k:
                    ext_divs[k] = x['id']
                    return x['id']
    return 0

wb = openpyxl.load_workbook('F:/Comptes SI au niveau AALS.xlsx')

def import_sheet(sheet_name, get_records):
    global SC, ok, fail
    print(f'\n============ {sheet_name} ============')
    # Check login
    s, d, sc2 = api('GET', '/api/divisions', None, SC)
    if s != 200:
        SC = login()
        ext_divs.clear()
        ext_divs.update(refresh_divs())
        print('  Re-logged in')
    for rec in get_records():
        if rec is None: continue
        nom, url, login, pwd, div_id = rec
        s, d, _ = api('POST', '/api/systemes-externes', {
            'nom': nom, 'url': url, 'login': login, 'motDePasse': pwd, 'idDivision': str(div_id)
        }, SC)
        if s == 201: ok += 1
        elif 'existe' in d.lower(): pass
        else:
            fail += 1
            if fail <= 10: print(f'  FAIL {login}: {d[:60]}')

# Association
def assoc_rows():
    ws = wb['Nouveau compte Association']
    for r in range(9, ws.max_row+1):
        l = str(ws.cell(r,5).value or '').strip()
        p = str(ws.cell(r,6).value or '').strip()
        if not l or not p: continue
        yield (f"Assoc-{l}", 'https://association.mi-app.ma/Association/', l, p, get_or_create_div('Association','AAL','Association',''))

import_sheet('Association', assoc_rows)
print(f'  => {ok} OK, {fail} FAIL')

# SGDA
def sgda_rows():
    ws = wb['Comptes SGDA']
    for r in range(5, ws.max_row+1):
        caidat = str(ws.cell(r,2).value or '').strip()
        l = str(ws.cell(r,3).value or '').strip()
        p = str(ws.cell(r,4).value or '').strip()
        if not l or not p: continue
        yield (f"SGDA-{l}", 'http://sgda.mi-app.ma/SGDA/', l, p, get_or_create_div(f"SGDA-{l}","AAL","SGDA",caidat[:50]))

import_sheet('SGDA', sgda_rows)
print(f'  => {ok} OK, {fail} FAIL')

# HAJJ
def hajj_rows():
    ws = wb['Comptes HAJJ -BO']
    for r in range(4, ws.max_row+1):
        ent = str(ws.cell(r,1).value or '').strip()
        for lc, pc in [(7,8),(13,14)]:
            l = str(ws.cell(r,lc).value or '').strip()
            p = str(ws.cell(r,pc).value or '').strip()
            if not l or not p or 'mot de' in p.lower() or len(p)<4: continue
            dn = ent if ent else 'Hajj-Safi'
            yield (f"Hajj-{l}", '', l, p, get_or_create_div(f"Hajj-{dn[:40]}","AAL","Hajj",ent[:40]))

import_sheet('HAJJ', hajj_rows)
print(f'  => {ok} OK, {fail} FAIL')

# SSCOV
def sscov_rows():
    ws = wb['Compte SSCOV']
    for r in range(3, ws.max_row+1):
        an = str(ws.cell(r,3).value or '').strip()
        l = str(ws.cell(r,4).value or '').strip()
        p = str(ws.cell(r,5).value or '').strip()
        if not l or not p: continue
        yield (f"SSCOV-{l}", '', l, p, get_or_create_div(f"SSCOV-{l}","AAL","SSCOV",an[:50]))

import_sheet('SSCOV', sscov_rows)
print(f'  => {ok} OK, {fail} FAIL')

# Tadamon Provinciaux
def tada_prov_rows():
    ws = wb['tadamon Provinciaux']
    for r in range(6, ws.max_row+1):
        l = str(ws.cell(r,4).value or '').strip()
        p = str(ws.cell(r,5).value or '').strip()
        obs = str(ws.cell(r,6).value or '').strip()
        if not l or not p or 'desactive' in obs.lower(): continue
        yield (f"Tadamon-{l}", 'http://amotadamon.mi-app.ma/', l, p, 0)

import_sheet('Tadamon Provinciaux', tada_prov_rows)
print(f'  => {ok} OK, {fail} FAIL')

# Tadamon Caidats
def tada_caid_rows():
    ws = wb['all caidat_tadamon']
    for r in range(5, ws.max_row+1):
        ent = str(ws.cell(r,1).value or '').strip()
        l = str(ws.cell(r,2).value or '').strip()
        p = str(ws.cell(r,4).value or '').strip()
        if not l or not p: continue
        dn = f"Tadamon-{ent[:30]}" if ent else 'Tadamon-Caidat'
        yield (f"Tadamon-{l}", 'http://amotadamon.mi-app.ma/', l, p, get_or_create_div(dn[:40],"AAL","Tadamon",ent[:50]))

import_sheet('Tadamon Caidats', tada_caid_rows)
print(f'  => {ok} OK, {fail} FAIL')

# Tajnid
def tajnid_rows():
    ws = wb['compte tajnid']
    for r in range(1, ws.max_row+1):
        caidat = str(ws.cell(r,2).value or '').strip()
        l = str(ws.cell(r,6).value or '').strip()
        p = str(ws.cell(r,7).value or '').strip()
        if not l or not p or 'login' in l.lower() or 'mot de' in p.lower(): continue
        dn = f"Tajnid-{caidat[:25]}" if caidat else f"Tajnid-{l}"
        yield (f"Tajnid-{l}", 'http://tajnid.mi-app.ma/', l, p, get_or_create_div(dn[:40],"AAL","Tajnid",caidat[:50]))

import_sheet('Tajnid', tajnid_rows)
print(f'  => {ok} OK, {fail} FAIL')

# SgPanier
def sgpanier_rows():
    ws = wb['comptes SgPanier']
    for r in range(6, ws.max_row+1):
        caidat = str(ws.cell(r,2).value or '').strip()
        l = str(ws.cell(r,6).value or '').strip()
        p = str(ws.cell(r,7).value or '').strip()
        if not l or not p: continue
        dn = f"SgPanier-{caidat[:25]}" if caidat else f"SgPanier-{l}"
        yield (f"SgPanier-{l}", 'https://sgpanierfmv.mi-app.ma/', l, p, get_or_create_div(dn[:40],"AAL","SgPanier",caidat[:50]))

import_sheet('SgPanier', sgpanier_rows)
print(f'  => {ok} OK, {fail} FAIL')

# Le-SGE
def lesge_rows():
    ws = wb['compte Le-SGE']
    for r in range(1, ws.max_row+1):
        aal = str(ws.cell(r,1).value or '').strip()
        l = str(ws.cell(r,2).value or '').strip()
        p = str(ws.cell(r,3).value or '').strip()
        if not l or not p: continue
        dn = f"LeSGE-{aal[:25]}" if aal else f"LeSGE-{l}"
        yield (f"LeSGE-{l}", '', l, p, get_or_create_div(dn[:40],"AAL","Le-SGE",aal[:50]))

import_sheet('Le-SGE', lesge_rows)
print(f'  => {ok} OK, {fail} FAIL')

print(f'\n=== RESUME FINAL: OK={ok} FAIL={fail} ===')
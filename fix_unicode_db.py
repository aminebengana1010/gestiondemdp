#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix: ré-envoyer avec ensure_ascii=False pour que Java reçoive les vrais caractères"""

import http.client
import json
import sys
sys.stdout.reconfigure(encoding='utf-8')

API_BASE = "localhost"
API_PORT = 8080

def api(method, path, body=None, cookie=None, ensure_ascii=True):
    conn = http.client.HTTPConnection(API_BASE, API_PORT, timeout=10)
    h = {'Content-Type': 'application/json'}
    if cookie:
        h['Cookie'] = cookie
    body_bytes = json.dumps(body, ensure_ascii=ensure_ascii).encode('utf-8')
    conn.request(method, path, body=body_bytes, headers=h)
    r = conn.getresponse()
    d = r.read().decode('utf-8')
    conn.close()
    return r.status, d

# login
status, data = api('POST', '/api/login', {'login': 'admin', 'motDePasse': 'admin123'})
# get session cookie manually
conn = http.client.HTTPConnection(API_BASE, API_PORT, timeout=10)
conn.request('POST', '/api/login', json.dumps({'login':'admin','motDePasse':'admin123'}).encode(), {'Content-Type':'application/json'})
r = conn.getresponse()
r.read()
sc = r.getheader('Set-Cookie','').split(';')[0]
conn.close()
print('Login OK')

# Correct values for all entries
switch_fixes = {
    18: {'nom': "Switch Bureau d'ordre 1", 'emplacement': 'Rez-de-chaussée', 'login': 'admin'},
    19: {'nom': "Switch Bureau d'ordre 1", 'emplacement': 'Rez-de-chaussée', 'login': ''},
    20: {'nom': 'Switch PN', 'emplacement': 'Annexe PN', 'login': 'admin'},
    17: {'nom': 'Switch ancien bureau Bennour tour externe', 'emplacement': '1er étage', 'login': ''},
    16: {'nom': 'Switch ancien bureau Bennour tour interne', 'emplacement': '1er étage', 'login': ''},
    21: {'nom': "Switch bureaux côté chef Daec", 'emplacement': 'Annexe DAEC', 'login': 'cisco'},
    22: {'nom': "Switch bureaux côté chef DAS", 'emplacement': 'Annexe DAEC', 'login': 'admin'},
    3:  {'nom': 'Switch Fédérateur', 'emplacement': '2ème étage', 'login': ''},
    13: {'nom': 'Switch Fédérateur', 'emplacement': '2ème étage', 'login': ''},
    15: {'nom': 'Switch salle passeports tour externe', 'emplacement': '2ème étage', 'login': ''},
    14: {'nom': 'Switch salle saisie tour interne', 'emplacement': '2ème étage', 'login': ''},
    2:  {'nom': 'Switch-Main', 'emplacement': 'Salle serveur', 'login': 'admin'},
}

count = 0
for sid, vals in switch_fixes.items():
    # Get current adresseMac first
    conn = http.client.HTTPConnection(API_BASE, API_PORT, timeout=10)
    conn.request('GET', '/api/switches', None, {'Cookie': sc})
    r = conn.getresponse()
    d = r.read().decode('utf-8')
    all_sw = json.loads(d)
    conn.close()

    current = None
    for sw in all_sw:
        if sw['id'] == sid:
            current = sw
            break

    if not current:
        continue

    payload = {
        'nom': vals['nom'],
        'adresseMac': current.get('adresseMac', ''),
        'emplacement': vals['emplacement'],
        'login': vals['login']
    }

    st2, d2 = api('PUT', '/api/switches/' + str(sid), payload, sc, ensure_ascii=False)
    if st2 == 200:
        print('OK #%d %s (empl=%s)' % (sid, payload['nom'], payload['emplacement']))
        count += 1
    else:
        print('FAIL #%d HTTP %d %s' % (sid, st2, d2[:80]))

print('\nSwitches corrigés: %d' % count)
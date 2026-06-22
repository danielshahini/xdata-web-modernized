#!/usr/bin/env python3
"""Backend API bug scan across roles. Flags 500s, permission leaks, unauth issues."""
import json, urllib.request, urllib.error

BASE = "http://localhost:8080/api/v1"

def login(u, p):
    body = json.dumps({"loginId": u, "password": p}).encode()
    req = urllib.request.Request(BASE + "/auth/login", body, {"Content-Type": "application/json"})
    try:
        return json.load(urllib.request.urlopen(req))["token"]
    except Exception as e:
        return None

def call(method, path, token=None, body=None):
    url = BASE + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    if token: req.add_header("Authorization", "Bearer " + token)
    if data is not None: req.add_header("Content-Type", "application/json")
    try:
        r = urllib.request.urlopen(req, timeout=60)
        return r.status
    except urllib.error.HTTPError as e:
        return e.code
    except Exception as e:
        return 0

tokens = {
    "admin": login("admin1", "admin1"),
    "instr": login("lehrer1", "lehrer123"),
    "stud":  login("schueler1", "schueler1"),
    "none":  None,
}
print("tokens:", {k: ("ok" if v else "FAIL") for k, v in tokens.items()})

# (method, path, body) ; access expectation handled in analysis
GETS = [
    ("GET", "/student/dashboard"),
    ("GET", "/student/submissions"),
    ("GET", "/student/assignments/1/questions"),
    ("GET", "/student/questions/1/attempts"),
    ("GET", "/assignments?courseId=DB1-2026"),
    ("GET", "/assignments/1"),
    ("GET", "/assignments/1/questions"),
    ("GET", "/assignments/1/stats"),
    ("GET", "/assignments/1/export"),
    ("GET", "/schemas"),
    ("GET", "/schemas/4/metadata"),
    ("GET", "/schemas/course/DB1-2026"),
    ("GET", "/schemas/datasets"),
    ("GET", "/announcements"),
    ("GET", "/admin/courses"),
    ("GET", "/admin/courses/1/members"),
    ("GET", "/admin/users"),
    ("GET", "/admin/users/unassigned"),
    ("GET", "/admin/audit-logs"),
    ("GET", "/admin/questions/1"),
    ("GET", "/db-connections"),
    ("GET", "/instructor/connections"),
    ("GET", "/system/status"),
    ("GET", "/evaluation/submissions/1"),
    ("GET", "/evaluation/plagiarism/1"),
]
# admin/instructor-only paths: student MUST NOT get 2xx
ADMIN_INSTR_ONLY = ["/admin/courses", "/admin/courses/1/members", "/admin/users",
                    "/admin/users/unassigned", "/admin/audit-logs", "/db-connections",
                    "/instructor/connections", "/assignments/1/stats", "/assignments/1/export",
                    "/evaluation/plagiarism/1"]
ADMIN_ONLY = ["/system/status"]

rows = []
for method, path in GETS:
    res = {r: call(method, path, tokens[r]) for r in tokens}
    rows.append((method, path, res))

# POST checks (safe)
pm_body = {"pattern": "SELECT 1", "student": "SELECT 1", "schemaId": 4, "params": {}}
post_rows = []
for r in ("admin", "instr", "stud"):
    post_rows.append(("POST /evaluation/playground/partial-marking", r,
                      call("POST", "/evaluation/playground/partial-marking", tokens[r], pm_body)))

print("\n## GET matrix (status per role)\n")
print(f"{'path':52} admin instr stud none")
for method, path, res in rows:
    print(f"{path:52} {res['admin']!s:5} {res['instr']!s:5} {res['stud']!s:5} {res['none']!s:5}")

print("\n## ANOMALIES\n")
anomalies = []
for method, path, res in rows:
    base = path.split("?")[0]
    for role, st in res.items():
        if st == 500:
            anomalies.append(f"500 SERVER ERROR: {role} {method} {path}")
        if st == 0:
            anomalies.append(f"NO-RESPONSE/timeout: {role} {method} {path}")
    # permission leaks
    if base in ADMIN_INSTR_ONLY and 200 <= res["stud"] < 300:
        anomalies.append(f"PERM-LEAK: student got {res['stud']} on admin/instr-only {path}")
    if base in ADMIN_ONLY and 200 <= res["instr"] < 300:
        anomalies.append(f"PERM-LEAK: instructor got {res['instr']} on admin-only {path}")
    if base in ADMIN_ONLY and 200 <= res["stud"] < 300:
        anomalies.append(f"PERM-LEAK: student got {res['stud']} on admin-only {path}")
    # unauth should not get 2xx
    if 200 <= res["none"] < 300:
        anomalies.append(f"UNAUTH-ACCESS: anonymous got {res['none']} on {path}")
for p in post_rows:
    if p[2] == 500: anomalies.append(f"500 SERVER ERROR: {p[1]} {p[0]}")
    if p[2] == 0: anomalies.append(f"NO-RESPONSE: {p[1]} {p[0]}")
print("POST partial-marking:", post_rows)
if anomalies:
    for a in anomalies: print(" -", a)
else:
    print("None found.")

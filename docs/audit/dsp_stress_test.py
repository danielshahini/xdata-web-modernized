#!/usr/bin/env python3
"""
Dataset Playground — killing-data generation stress/semantic test (report-only).

Drives POST /playground/generate-killing-data across many schema structures,
queries, all selectable mutation types (single + combined), and custom mutants.
Semantically verifies each result in a throwaway PostgreSQL DB:
  - generated INSERTs are valid & loadable against the schema
  - the reference query returns >= 1 row on the generated data (non-empty goal)
  - distinguishing power vs. a constructed representative mutant (kill / no-kill)
Also probes stability (repeat runs) and robustness (edge-case schemas).

No application code is changed. Output: a markdown report on stdout.
"""
import json, subprocess, urllib.request, urllib.error, time, sys

BASE = "http://localhost:8080/api/v1"
VDB = "dsp_verify"
ALL_TYPES = ["SELECTION", "EQUIVALENCE", "NONEQUIJOIN", "AGG", "DISTINCT", "EXTRAGROUPBY", "HAVING"]

def login():
    body = json.dumps({"loginId": "lehrer1", "password": "lehrer123"}).encode()
    req = urllib.request.Request(BASE + "/auth/login", body, {"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req))["token"]

def gen(token, query, schema_id, mutation_types, mutant=None):
    body = {"query": query, "schemaId": schema_id, "mutationTypes": mutation_types}
    if mutant:
        body["mutantQuery"] = mutant
    req = urllib.request.Request(BASE + "/playground/generate-killing-data",
                                 json.dumps(body).encode(),
                                 {"Content-Type": "application/json", "Authorization": "Bearer " + token})
    try:
        r = json.load(urllib.request.urlopen(req, timeout=120))
        return {"http": 200, **r}
    except urllib.error.HTTPError as e:
        return {"http": e.code, "success": False, "message": e.read().decode()[:200], "inserts": []}
    except Exception as e:
        return {"http": 0, "success": False, "message": str(e)[:200], "inserts": []}

def upload_schema(token, name, ddl):
    path = f"/tmp/dsp_{name}.sql"
    open(path, "w").write(ddl)
    out = subprocess.run(["curl", "-s", "-X", "POST", BASE + "/playground/upload-schema",
                          "-H", "Authorization: Bearer " + token, "-F", f"file=@{path}"],
                         capture_output=True, text=True).stdout
    try:
        return json.loads(out).get("schemaId")
    except Exception:
        return None

def psql(sql, db=VDB, capture=True):
    return subprocess.run(
        ["docker", "compose", "exec", "-T", "db", "psql", "-U", "postgres", "-d", db,
         "-v", "ON_ERROR_STOP=1", "-A", "-t", "-F", "|", "-c", sql],
        cwd="/Users/Daniel/IdeaProjects/xdata-web", capture_output=True, text=True)

def psql_stdin(sqltext, db=VDB):
    return subprocess.run(
        ["docker", "compose", "exec", "-T", "db", "psql", "-U", "postgres", "-d", db, "-v", "ON_ERROR_STOP=1", "-f", "/dev/stdin"],
        cwd="/Users/Daniel/IdeaProjects/xdata-web", input=sqltext, capture_output=True, text=True)

def reset_db(ddl):
    psql("DROP SCHEMA public CASCADE; CREATE SCHEMA public;")
    return psql_stdin(ddl)

def run_query(q):
    r = psql(q)
    if r.returncode != 0:
        return None, r.stderr.strip()[:160]
    rows = [ln for ln in r.stdout.split("\n") if ln != ""]
    return sorted(rows), None

# ---------------------------------------------------------------- corpus
SCHEMAS = {
 "single": """
CREATE TABLE nums (id INT PRIMARY KEY, val INT, label VARCHAR(20));
""",
 "uni": """
CREATE TABLE students (id INT PRIMARY KEY, name VARCHAR(50), age INT, major VARCHAR(30), gpa NUMERIC(3,2));
CREATE TABLE courses (cid INT PRIMARY KEY, title VARCHAR(50), credits INT);
CREATE TABLE enroll (sid INT, cid INT, grade NUMERIC(3,2), PRIMARY KEY (sid,cid));
""",
 "types": """
CREATE TABLE mix (id INT PRIMARY KEY, name VARCHAR(40), amount NUMERIC(8,2), active BOOLEAN, created DATE, note VARCHAR(60));
""",
 "emp": """
CREATE TABLE dept (did INT PRIMARY KEY, dname VARCHAR(40));
CREATE TABLE emp (eid INT PRIMARY KEY, ename VARCHAR(40), salary INT, did INT, hired DATE);
""",
 "nullable": """
CREATE TABLE people (id INT PRIMARY KEY, name VARCHAR(40), age INT, city VARCHAR(40), score INT);
""",
 "wide": """
CREATE TABLE wide (id INT PRIMARY KEY, a INT, b INT, c INT, d VARCHAR(20), e NUMERIC(6,2), f INT, g VARCHAR(20), h INT, i INT, j INT);
""",
}

# Per schema: (reference query, [(mutation_type, representative_mutant_query)])
CASES = {
 "single": [
   ("SELECT id FROM nums WHERE val > 20", [("SELECTION", "SELECT id FROM nums WHERE val >= 20")]),
   ("SELECT id FROM nums WHERE val > 20 AND label = 'x'", [("SELECTION", "SELECT id FROM nums WHERE val > 20 OR label = 'x'")]),
   ("SELECT DISTINCT label FROM nums", [("DISTINCT", "SELECT label FROM nums")]),
 ],
 "uni": [
   ("SELECT name FROM students WHERE age > 22", [("SELECTION", "SELECT name FROM students WHERE age > 21")]),
   ("SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid", [("EQUIVALENCE", "SELECT s.name FROM students s JOIN enroll e ON s.id = e.cid")]),
   ("SELECT major, COUNT(*) FROM students GROUP BY major", [("AGG", "SELECT major, SUM(age) FROM students GROUP BY major"), ("EXTRAGROUPBY", "SELECT major, age, COUNT(*) FROM students GROUP BY major, age")]),
   ("SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1", [("HAVING", "SELECT major FROM students GROUP BY major HAVING COUNT(*) >= 1")]),
 ],
 "types": [
   ("SELECT id FROM mix WHERE amount > 100.00", [("SELECTION", "SELECT id FROM mix WHERE amount >= 100.00")]),
   ("SELECT id FROM mix WHERE active = true", [("SELECTION", "SELECT id FROM mix WHERE active = false")]),
   ("SELECT name FROM mix WHERE created > DATE '2020-01-01'", [("SELECTION", "SELECT name FROM mix WHERE created >= DATE '2020-01-01'")]),
 ],
 "emp": [
   ("SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary > 50000", [("SELECTION", "SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary >= 50000")]),
   ("SELECT did, AVG(salary) FROM emp GROUP BY did", [("AGG", "SELECT did, MAX(salary) FROM emp GROUP BY did")]),
   ("SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2", [("HAVING", "SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 1")]),
 ],
 "nullable": [
   ("SELECT name FROM people WHERE age > 30", [("SELECTION", "SELECT name FROM people WHERE age >= 30")]),
   ("SELECT name FROM people WHERE city = 'Berlin'", [("SELECTION", "SELECT name FROM people WHERE city <> 'Berlin'")]),
   ("SELECT name FROM people WHERE score > 50 AND age < 40", [("SELECTION", "SELECT name FROM people WHERE score > 50 OR age < 40")]),
 ],
 "wide": [
   ("SELECT id FROM wide WHERE a > 5 AND f > 5", [("SELECTION", "SELECT id FROM wide WHERE a > 5 OR f > 5")]),
   ("SELECT d, COUNT(*) FROM wide GROUP BY d", [("AGG", "SELECT d, SUM(a) FROM wide GROUP BY d")]),
 ],
}

def main():
    token = login()
    # verify DB exists
    subprocess.run(["docker", "compose", "exec", "-T", "db", "psql", "-U", "postgres", "-d", "postgres",
                    "-c", f"CREATE DATABASE {VDB}"], cwd="/Users/Daniel/IdeaProjects/xdata-web",
                   capture_output=True, text=True)

    schema_ids = {}
    for name, ddl in SCHEMAS.items():
        sid = upload_schema(token, name, ddl)
        schema_ids[name] = sid
        print(f"- schema **{name}** -> schemaId={sid}", file=sys.stderr)

    results = []  # dicts
    for name, cases in CASES.items():
        sid = schema_ids[name]
        ddl = SCHEMAS[name]
        for ref, mutants in cases:
            # --- A) all selectable types individually: validity + non-empty + runnable
            for mt in ALL_TYPES:
                resp = gen(token, ref, sid, [mt])
                inserts = [i for i in resp.get("inserts", []) if not i.strip().startswith("--")]
                rec = {"schema": name, "ref": ref, "mode": f"type:{mt}", "http": resp["http"],
                       "success": resp.get("success"), "n": len(inserts), "load_ok": None,
                       "ref_nonempty": None, "killed": None, "msg": resp.get("message", "")[:80]}
                if inserts:
                    le = reset_db(ddl)
                    li = psql_stdin(";\n".join(inserts) + ";")
                    rec["load_ok"] = (le.returncode == 0 and li.returncode == 0)
                    if rec["load_ok"]:
                        rr, err = run_query(ref)
                        rec["ref_nonempty"] = bool(rr) and len(rr) > 0
                    else:
                        rec["load_err"] = (li.stderr or le.stderr).strip().split("\n")[-1][:120]
                results.append(rec)

            # --- B) semantic distinguishing power vs constructed mutant (use matching type)
            for mt, mq in mutants:
                resp = gen(token, ref, sid, [mt])
                inserts = [i for i in resp.get("inserts", []) if not i.strip().startswith("--")]
                rec = {"schema": name, "ref": ref, "mode": f"kill:{mt}", "http": resp["http"],
                       "success": resp.get("success"), "n": len(inserts), "load_ok": None,
                       "ref_nonempty": None, "killed": None, "mutant": mq, "msg": resp.get("message", "")[:80]}
                if inserts:
                    le = reset_db(ddl); li = psql_stdin(";\n".join(inserts) + ";")
                    rec["load_ok"] = (le.returncode == 0 and li.returncode == 0)
                    if rec["load_ok"]:
                        rr, e1 = run_query(ref); mr, e2 = run_query(mq)
                        rec["ref_nonempty"] = bool(rr)
                        if e1 or e2:
                            rec["killed"] = None; rec["q_err"] = (e1 or e2)
                        else:
                            rec["killed"] = (rr != mr)
                    else:
                        rec["load_err"] = (li.stderr or le.stderr).strip().split("\n")[-1][:120]
                results.append(rec)

            # --- C) combined all-types run (robustness)
            resp = gen(token, ref, sid, ALL_TYPES)
            inserts = [i for i in resp.get("inserts", []) if not i.strip().startswith("--")]
            results.append({"schema": name, "ref": ref, "mode": "type:ALL", "http": resp["http"],
                            "success": resp.get("success"), "n": len(inserts), "load_ok": None,
                            "ref_nonempty": None, "killed": None, "msg": resp.get("message", "")[:80]})

            # --- D) custom-mutant path (is mutantQuery honored?) — compare inserts with/without mutant
            base = gen(token, ref, sid, ["SELECTION"])
            withm = gen(token, ref, sid, ["SELECTION"], mutant=mutants[0][1])
            same = [i for i in base.get("inserts", []) if not i.startswith("--")] == \
                   [i for i in withm.get("inserts", []) if not i.startswith("--")]
            results.append({"schema": name, "ref": ref, "mode": "custom-mutant", "http": withm["http"],
                            "success": withm.get("success"), "n": len([i for i in withm.get('inserts',[]) if not i.startswith('--')]),
                            "load_ok": None, "ref_nonempty": None, "killed": None,
                            "msg": ("mutantQuery IGNORED (identical output)" if same else "mutantQuery changed output")})

    # ---- stability: repeat one case 5x
    sid = schema_ids["uni"]; ref0 = "SELECT name FROM students WHERE age > 22"
    stab = []
    for _ in range(5):
        r = gen(token, ref0, sid, ["SELECTION"])
        stab.append(r.get("http") == 200 and len([i for i in r.get('inserts',[]) if not i.startswith('--')]) > 0)

    # ------------------------------------------------------------- report
    out = []
    out.append("# Dataset Playground — Härtetest-Report (semantisch)\n")
    out.append(f"Datum: 2026-06-20 · Fälle gesamt: **{len(results)}** · Schemas: {len(SCHEMAS)} · Mutationstypen: {len(ALL_TYPES)}\n")
    tot = len(results)
    http_ok = sum(1 for r in results if r["http"] == 200)
    no5xx = sum(1 for r in results if r["http"] != 500 and r["http"] != 0)
    nonempty = [r for r in results if r["mode"].startswith("type:") or r["mode"].startswith("kill:")]
    produced = sum(1 for r in nonempty if r["n"] > 0)
    load_runs = [r for r in results if r.get("load_ok") is not None]
    load_ok = sum(1 for r in load_runs if r["load_ok"])
    refne = [r for r in results if r.get("ref_nonempty") is not None]
    refne_ok = sum(1 for r in refne if r["ref_nonempty"])
    kills = [r for r in results if r.get("killed") is not None]
    killed = sum(1 for r in kills if r["killed"])
    out.append("## Kennzahlen\n")
    out.append(f"- HTTP 200: **{http_ok}/{tot}** · kein 5xx/Verbindungsfehler: **{no5xx}/{tot}**")
    out.append(f"- Nicht-leere INSERTs erzeugt (type/kill-Läufe): **{produced}/{len(nonempty)}**")
    out.append(f"- INSERTs valide & ladbar: **{load_ok}/{len(load_runs)}**")
    out.append(f"- Referenz-Query liefert ≥1 Zeile auf generierten Daten: **{refne_ok}/{len(refne)}**")
    out.append(f"- Konstruierter Mutant **gekillt** (Daten unterscheiden Referenz≠Mutant): **{killed}/{len(kills)}**")
    out.append(f"- Stabilität (5× Wiederholung, nicht-leer): **{sum(stab)}/5**\n")
    out.append("## Auffällige Fälle\n")
    for r in results:
        flag = []
        if r["http"] != 200: flag.append(f"HTTP {r['http']}")
        if (r["mode"].startswith("type:") or r["mode"].startswith("kill:")) and r["n"] == 0: flag.append("LEER")
        if r.get("load_ok") is False: flag.append("LOAD-FEHLER: " + r.get("load_err", ""))
        if r.get("ref_nonempty") is False: flag.append("Referenz LEER auf Daten")
        if r.get("killed") is False: flag.append("NICHT gekillt")
        if r.get("q_err"): flag.append("Query-Fehler: " + r["q_err"])
        if flag:
            out.append(f"- `{r['schema']}` / {r['mode']} — `{r['ref'][:70]}` → {'; '.join(flag)}")
    out.append("\n## custom-mutant (mutantQuery honored?)\n")
    for r in results:
        if r["mode"] == "custom-mutant":
            out.append(f"- `{r['schema']}` `{r['ref'][:60]}` → {r['msg']}")
    print("\n".join(out))

if __name__ == "__main__":
    main()

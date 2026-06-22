import json, urllib.request, urllib.error
BASE="http://localhost:8080/api/v1"
def login(u,p):
    r=urllib.request.Request(BASE+"/auth/login",json.dumps({"loginId":u,"password":p}).encode(),{"Content-Type":"application/json"})
    return json.load(urllib.request.urlopen(r))["token"]
def call(m,path,tok,body=None):
    data=json.dumps(body).encode() if body is not None else None
    r=urllib.request.Request(BASE+path,data=data,method=m)
    if tok: r.add_header("Authorization","Bearer "+tok)
    if data is not None: r.add_header("Content-Type","application/json")
    try:
        resp=urllib.request.urlopen(r,timeout=60); return resp.status, resp.read()[:120].decode('utf-8','ignore')
    except urllib.error.HTTPError as e: return e.code, e.read()[:120].decode('utf-8','ignore')
    except Exception as e: return 0, str(e)[:120]
A=login("admin1","admin1"); I=login("lehrer1","lehrer123"); S=login("schueler1","schueler1")
# (label, method, path, token, body, expected-status-set)
T=[
 ("login wrong pw","POST","/auth/login",None,{"loginId":"admin1","password":"x"},{401,403}),
 ("create user missing fields","POST","/admin/users",A,{"username":""},{400,422}),
 ("create user dup loginId","POST","/admin/users",A,{"username":"Dup","loginId":"admin1","password":"pw12345","role":"STUDENT","email":"d@d.de","courseIds":[]},{400,409,422}),
 ("question invalid SQL","POST","/admin/questions",I,{"name":"bad","marks":5,"instructorQuery":"NOT SQL ###","assignmentId":1},{400,422}),
 ("assignment bad body","POST","/assignments?courseId=DB1-2026",I,{"name":""},{400,422,200}),
 ("get assignment 99999","GET","/assignments/99999",A,None,{404}),
 ("get question 99999","GET","/admin/questions/99999",A,None,{404}),
 ("get question non-numeric","GET","/admin/questions/abc",A,None,{400,404}),
 ("members bad course","GET","/admin/courses/99999/members",A,None,{200,404}),
 ("db-conn system url","POST","/db-connections",I,{"name":"sys","url":"jdbc:postgresql://db:5432/xdatadb","user":"postgres","password":"1709","courseId":"DB1-2026"},{400,422}),
 ("db-conn bad course access","POST","/db-connections",I,{"name":"x","url":"jdbc:postgresql://h/d","user":"u","password":"p","courseId":"NOPE-9999"},{400,403,422}),
 ("submit bad questionId","POST","/student/submit",S,{"questionId":99999,"query":"SELECT 1"},{400,403,404}),
 ("submit missing body","POST","/student/submit",S,{},{400,422,500}),
 ("assign-course bad","POST","/admin/users/admin1/assign-course?courseId=NOPE",A,None,{400,404,422}),
 ("reset-pw nonexistent","POST","/admin/users/ghost999/reset-password",A,{"password":"newpw123"},{400,404}),
 ("delete user nonexistent","DELETE","/admin/users/ghost999",A,None,{400,404}),
 ("toggle nonexistent","PATCH","/admin/users/ghost999/toggle-status",A,None,{400,404}),
 ("export bad assignment","GET","/assignments/99999/export",I,None,{404,400}),
 ("gen-killing bad schema","POST","/playground/generate-killing-data",I,{"query":"SELECT 1","schemaId":99999,"mutationTypes":["SELECTION"]},{200,404,422}),
]
print(f"{'label':32} {'status':6} expected   {'verdict'}")
bugs=[]
for label,m,path,tok,body,exp in T:
    st,bodytext=call(m,path,tok,body)
    ok = st in exp
    verdict = "ok" if ok else "<<< UNEXPECTED"
    if st==500 or st==0: verdict="<<< 500/ERROR BUG"; 
    if not ok or st in (500,0): bugs.append((label,st,exp,bodytext))
    print(f"{label:32} {st!s:6} {str(sorted(exp)):10} {verdict}")
print("\n## BUGS / UNEXPECTED")
if bugs:
    for b in bugs: print(f" - {b[0]}: got {b[1]}, expected {sorted(b[2])} | body: {b[3]}")
else: print("None.")

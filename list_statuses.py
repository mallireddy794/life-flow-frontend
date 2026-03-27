import MySQLdb
db = MySQLdb.connect(host="localhost", user="root", passwd="", db="lifeflow_db")
cur = db.cursor()
cur.execute("SELECT DISTINCT status FROM blood_requests")
rows = cur.fetchall()
for r in rows:
    print(r[0])
cur.close()
db.close()

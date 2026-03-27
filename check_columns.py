import MySQLdb
from MySQLdb.cursors import DictCursor

db = MySQLdb.connect(host="localhost", user="root", passwd="", db="lifeflow_db")
cur = db.cursor(DictCursor)

cur.execute("DESCRIBE blood_requests")
rows = cur.fetchall()
for r in rows:
    print(r)

cur.close()
db.close()

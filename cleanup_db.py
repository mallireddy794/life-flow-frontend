import MySQLdb

db = MySQLdb.connect(host="localhost", user="root", passwd="", db="lifeflow_db")
cur = db.cursor()

dummy_groups = ('g', 'y', 'f', 'v', 'e', 's', 'd', 'sh', 'rhxb', 'xjt', 'eg')
query = f"DELETE FROM blood_requests WHERE blood_group IN {dummy_groups}"

try:
    cur.execute(query)
    db.commit()
    print(f"Deleted {cur.rowcount} dummy blood requests.")
except Exception as e:
    print(f"Error: {e}")
    db.rollback()

cur.close()
db.close()

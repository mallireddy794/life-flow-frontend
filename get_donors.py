import MySQLdb
try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    cursor.execute("SELECT id, user_id FROM donors LIMIT 10")
    print(cursor.fetchall())
    conn.close()
except Exception as e:
    print(e)

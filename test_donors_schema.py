import MySQLdb
try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    cursor.execute("DESCRIBE donors")
    print(cursor.fetchall())
    conn.close()
except Exception as e:
    print(e)

import MySQLdb

try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    
    # Force ALL donors to be O+, available, and eligible for testing
    cursor.execute("""
        UPDATE donors 
        SET blood_group = 'O+', 
            is_available = 1, 
            is_eligible = 1
    """)
    
    conn.commit()
    print("All donors fixed to O+ and available.")
    conn.close()
except Exception as e:
    print(e)

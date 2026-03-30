import MySQLdb
import random

try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    
    blood_groups = ["A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"]
    
    cursor.execute("SELECT id FROM donors")
    donor_ids = [row[0] for row in cursor.fetchall()]
    
    print(f"Restoring diverse blood groups for {len(donor_ids)} donors...")
    
    for d_id in donor_ids:
        bg = random.choice(blood_groups)
        cursor.execute("UPDATE donors SET blood_group = %s WHERE id = %s", (bg, d_id))
        
    conn.commit()
    print("Done! Blood groups are now diverse again.")
    conn.close()
except Exception as e:
    print(e)

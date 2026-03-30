import MySQLdb
import random

try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM donors")
    donor_ids = [row[0] for row in cursor.fetchall()]
    
    print(f"Updating {len(donor_ids)} donors with diverse performance data...")
    
    for d_id in donor_ids:
        acc_rate = round(random.uniform(0.60, 0.99), 2)
        resp_time = random.randint(2, 45)
        
        cursor.execute("""
            UPDATE donors 
            SET past_acceptance_rate = %s, 
                response_time_avg = %s
            WHERE id = %s
        """, (acc_rate, resp_time, d_id))
        
    conn.commit()
    print("Done! Performance data has been randomized for testing.")
    conn.close()
except Exception as e:
    print(e)

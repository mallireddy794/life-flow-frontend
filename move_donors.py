import MySQLdb
import random

try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM users WHERE role='donor'")
    user_ids = [row[0] for row in cursor.fetchall()]
    
    print(f"Updating {len(user_ids)} donors to be near the patient for testing...")
    
    base_lat = 12.9249
    base_lng = 80.1318
    
    for u_id in user_ids:
        # Give them random locations within ~2km
        lat = base_lat + random.uniform(-0.02, 0.02)
        lng = base_lng + random.uniform(-0.02, 0.02)
        
        cursor.execute("""
            UPDATE users 
            SET latitude = %s, longitude = %s
            WHERE id = %s
        """, (lat, lng, u_id))
        
    conn.commit()
    print("Done! Donors are now nearby.")
    conn.close()
except Exception as e:
    print(e)

import MySQLdb

db = MySQLdb.connect(host="localhost", user="root", passwd="", db="lifeflow_db")
cur = db.cursor()

try:
    print("Migrating blood_requests table...")
    # Add new columns
    cur.execute("ALTER TABLE blood_requests ADD COLUMN IF NOT EXISTS patient_name VARCHAR(255)")
    cur.execute("ALTER TABLE blood_requests ADD COLUMN IF NOT EXISTS hospital_name VARCHAR(255)")
    cur.execute("ALTER TABLE blood_requests ADD COLUMN IF NOT EXISTS contact_number VARCHAR(20)")
    
    # Increase blood_group length
    cur.execute("ALTER TABLE blood_requests MODIFY COLUMN blood_group VARCHAR(10)")
    cur.execute("ALTER TABLE users MODIFY COLUMN blood_group VARCHAR(10)")
    cur.execute("ALTER TABLE donors MODIFY COLUMN blood_group VARCHAR(10)")
    cur.execute("ALTER TABLE patients MODIFY COLUMN blood_group VARCHAR(10)")
    
    # Change Enum to Varchar to prevent restricted values error
    cur.execute("ALTER TABLE blood_requests MODIFY COLUMN urgency_level VARCHAR(50)")
    cur.execute("ALTER TABLE blood_requests MODIFY COLUMN status VARCHAR(50) DEFAULT 'Pending'")
    
    db.commit()
    print("Migration successful!")
except Exception as e:
    print(f"Migration failed: {e}")
    db.rollback()

cur.close()
db.close()

import MySQLdb

try:
    conn = MySQLdb.connect(host="localhost", user="root", password="", db="lifeflow_db")
    cursor = conn.cursor()
    
    # 1. Ensure rating columns exist in donors table
    columns = [
        ("avg_rating", "DECIMAL(3, 2) DEFAULT 0.0"),
        ("sentiment_score", "DECIMAL(3, 2) DEFAULT 0.5"),
        ("total_reviews", "INT DEFAULT 0")
    ]
    
    for col_name, col_type in columns:
        try:
            cursor.execute(f"ALTER TABLE donors ADD COLUMN {col_name} {col_type}")
            print(f"Added {col_name} to donors.")
        except Exception as e:
            if "Duplicate column name" not in str(e):
                print(f"Error adding {col_name}: {e}")

    # 2. Ensure donor_reviews table exists
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS donor_reviews (
            id INT AUTO_INCREMENT PRIMARY KEY,
            donor_id INT,
            patient_id INT,
            rating INT,
            review_text TEXT,
            sentiment_score DECIMAL(3, 2),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    print("donor_reviews table is ready.")

    # 3. Force ALL donors to be O+, available, and eligible for testing
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
    print(f"CRITICAL ERROR: {e}")

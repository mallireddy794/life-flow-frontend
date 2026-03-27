import MySQLdb
from MySQLdb.cursors import DictCursor

db = MySQLdb.connect(host="localhost", user="root", passwd="", db="lifeflow_db")
cur = db.cursor(DictCursor)

lat, lng = 13.0286511, 80.0345559
radius = 50000.0

query = f"""
    SELECT * FROM (
        SELECT
          u.id,
          u.name,
          d.is_eligible,
          u.latitude,
          u.longitude,
          (
            6371 * 2 * ASIN(
              SQRT(
                GREATEST(0, POWER(SIN((RADIANS(u.latitude - {lat})) / 2), 2) +
                COS(RADIANS({lat})) *
                COS(RADIANS(u.latitude)) *
                POWER(SIN((RADIANS(u.longitude - {lng})) / 2), 2))
              )
            )
          ) AS distance_km
        FROM users u
        JOIN donors d ON d.user_id = u.id
        WHERE u.role = 'donor'
          AND d.is_eligible = 1
          AND u.latitude IS NOT NULL
          AND u.longitude IS NOT NULL
    ) AS sub
    WHERE sub.distance_km <= {radius}
    ORDER BY sub.distance_km ASC
"""

print("Executing query...")
cur.execute(query)
rows = cur.fetchall()
print(f"Rows found: {len(rows)}")
for r in rows:
    print(r)

cur.close()
db.close()

import math

def haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = math.sin(dlat / 2)**2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2)**2
    c = 2 * math.asin(math.sqrt(a))
    return R * c

lat1, lon1 = 13.0286444, 80.0345369
lat2, lon2 = 13.0286511, 80.0345559

dist = haversine(lat1, lon1, lat2, lon2)
print(f"Distance: {dist} km")

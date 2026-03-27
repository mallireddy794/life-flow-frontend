import json
import urllib.request
import urllib.parse

base_url = "http://localhost:5000/donor/requests/nearby"
params = {"lat": 13.0286444, "lng": 80.0345369, "radius": 100.0}
url = f"{base_url}?{urllib.parse.urlencode(params)}"

try:
    with urllib.request.urlopen(url) as response:
        print(f"Status: {response.status}")
        print(json.dumps(json.loads(response.read().decode()), indent=2))
except Exception as e:
    print(f"Failed: {e}")

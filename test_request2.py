import urllib.request
import urllib.error
import json

data = {
    "patient_id": 1,
    "donor_id": 1,
    "blood_group": "A+",
    "units_required": 1,
    "urgency_level": "HIGH",
    "city": "Chennai"
}
req = urllib.request.Request("http://localhost:5000/patient/send_request")
req.add_header('Content-Type', 'application/json; charset=utf-8')
jsondata = json.dumps(data).encode('utf-8')
req.add_header('Content-Length', len(jsondata))

try:
    response = urllib.request.urlopen(req, jsondata)
    print("STATUS", response.getcode())
    print("RESP", response.read().decode())
except urllib.error.HTTPError as e:
    print("HTTP ERROR", e.code)
    print("ERR_RESP", e.read().decode())
except Exception as e:
    print("OTHER ERROR", e)

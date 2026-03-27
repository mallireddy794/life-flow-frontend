import requests
import json

data = {
    "patient_id": 1,
    "donor_id": 1,
    "blood_group": "A+",
    "units_required": 1,
    "urgency_level": "HIGH",
    "city": "Chennai"
}

r = requests.post("http://localhost:5000/patient/send_request", json=data)
print(r.status_code)
print(r.text)

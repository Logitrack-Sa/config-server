import json
import time
import urllib.request

base = "http://127.0.0.1:8888"
for attempt in range(60):
    try:
        with urllib.request.urlopen(base+"/actuator/health", timeout=2) as response:
            assert json.load(response) == {"status": "UP"}
        break
    except (OSError, AssertionError):
        time.sleep(2)
else:
    raise SystemExit("Config Server no inició con Git local")
for app in ("shipment-service", "courier-service", "security-service"):
    for profile in ("dev", "prod"):
        with urllib.request.urlopen(f"{base}/{app}/{profile}/main", timeout=5) as response:
            result = json.load(response)
        source = {}
        for entry in reversed(result["propertySources"]):
            source.update(entry["source"])
        assert source["fixture.service"] == app
        assert source["fixture.profile"] == profile
        assert len(result["version"]) == 40
print("OK: imagen sin privilegios, health mínimo y seis perfiles desde Git local")

"""Static project checks. Not a substitute for Android compilation or device testing."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
xmls = sorted(root.rglob('*.xml'))
for path in xmls:
    ET.parse(path)

def strings(folder):
    doc = ET.parse(root / f'app/src/main/res/{folder}/strings.xml')
    return {node.attrib['name'] for node in doc.getroot() if node.tag == 'string'}

base = strings('values')
fa = strings('values-fa')
assert base == fa, f'Mismatched translations: {base ^ fa}'
code = '\n'.join(path.read_text() for path in root.rglob('*.kt'))
used = set(re.findall(r'R\.string\.([\w]+)', code))
assert not (used - base - {'variant_label'}), f'Missing strings: {used - base}'
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
assert 'android.permission.INTERNET' not in manifest
assert 'android:allowBackup="false"' in manifest
assert 'android.permission.BIND_DEVICE_ADMIN' in manifest
assert 'wipeData(0)' in code and 'wipeDevice(0)' in code
assert 'one_tap_enabled' in code
assert 'isDeviceOwnerApp' in code
assert 'isAdminActive' in code
assert 'SecurityException' in code
print(f'PASS: parsed {len(xmls)} XML files, checked localization, reset gates and manifest basics')

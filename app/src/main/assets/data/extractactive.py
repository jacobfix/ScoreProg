import json
import sys

with open(sys.argv[1], 'r') as f:
    d = json.load(f)

l = []
for key in d:
    l.append(key)

with open(sys.argv[1] + '-list', 'w') as f:
    json.dump(l, f);

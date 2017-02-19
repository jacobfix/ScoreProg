import json
import sys
import random

if len(sys.argv) < 2:
    print "Usage: python set"
    sys.exit(1)

zero = (len(sys.argv) == 3 and (sys.argv[2] == "zero" or sys.argv[2] == "0"))

f = open(sys.argv[1], 'r')
d = json.load(f)
f.close()
f = open(sys.argv[1], 'w')
for key in d:
    d[key]["home"]["score"]["T"] = random.randint(0, 45) if not zero else 0
    d[key]["away"]["score"]["T"] = random.randint(0, 45) if not zero else 0
json.dump(d, f)
f.close()

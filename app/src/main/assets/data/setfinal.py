import json
import sys
import random

if len(sys.argv) < 3:
    print "Usage: python setfinal.py <json filename> <Final/Pregame/InProgress>"
    sys.exit(1)

if sys.argv[2] not in ['Final', 'Pregame', 'InProgress']:
    print "Valid arguments are Final, Pregame, or InProgress"
    sys.exit(1)
    
f = open(sys.argv[1], 'r')
d = json.load(f)
f.close()
f = open(sys.argv[1], 'w')
for key in d:
    if sys.argv[2] == 'InProgress':
        d[key]['qtr'] = random.randint(1, 4)
        d[key]['clock'] = str(random.randint(0, 14)) + ":" + str(random.randint(0, 59))
    else:
        d[key]['qtr'] = sys.argv[2]
json.dump(d, f)
f.close()

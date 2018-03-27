import json
import random
import string

def random_string(n):
    return ''.join(random.choice(string.ascii_uppercase + string.ascii_lowercase) for _ in range(n))

user_ids = [str(i) for i in range(10)]

_json = {}
for uid in user_ids:
    user_ids_copy = user_ids[:]
    user_ids_copy.remove(uid)
    details = {'uid': uid,
               'username': random_string(random.randint(6, 14)),
               'email': random_string(10),
               'friends': user_ids_copy
               }
    _json[uid] = details

with open('users-local.json', 'w') as f:
    json.dump(_json, f)


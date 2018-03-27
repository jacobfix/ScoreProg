import json
import random

game_ids = ["2016092200",
             "2016092500",
             "2016092501",
             "2016092502",
             "2016092503",
             "2016092504",
             "2016092505",
             "2016092506",
             "2016092507",
             "2016092508",
             "2016092509",
             "2016092510",
             "2016092511",
             "2016092512",
             "2016092513",
             "2016092600"]

user_ids = [str(i) for i in range(10)]

_json = {}
for uid in user_ids:
    predictions = {}
    for gid in game_ids:
        predictions[gid] = {'away': random.randint(0, 45), 'home': random.randint(0, 45)}

    _json[uid] = predictions
    
with open('predictions-local.json', 'w') as f:
    json.dump(_json, f)
    

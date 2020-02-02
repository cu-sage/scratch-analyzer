from pymongo import MongoClient
from bson.objectid import ObjectId
import urllib.parse
from poisson_path import generate_hints

username = urllib.parse.quote_plus('sage-node')
password = urllib.parse.quote_plus('sage-node')
uri = 'mongodb://%s:%s@ds161210.mlab.com/sage-node' % (username, password)
print(uri)
client = MongoClient(uri, 61210)
db = client['sage-node']
games = db.games
new_hint = generate_hints()
print(new_hint)
obj = games.find_one_and_update({
    "_id": ObjectId("5a5e749dd8f533f43ad2994c")},
    {'$set': {'hint': new_hint}}
)

print (
    games.find_one({
        "_id": ObjectId("5a5e749dd8f533f43ad2994c")})
)
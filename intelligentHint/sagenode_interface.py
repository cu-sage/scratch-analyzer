from pymongo import MongoClient
from bson.objectid import ObjectId
import urllib.parse
import pprint
import argparse
import json

class SageDb:
	"""
	Attributes:
		self.db: 'sage-node' database on mlab
		self.games: 'games' documents from 'sage-node' (db.games)
		self.client: mongodb client
	"""
	def __init__(self):
		"""
		Sets attributes: self.db, self.games, self.client
		"""
		username = urllib.parse.quote_plus('sage-node')
		password = urllib.parse.quote_plus('sage-node')
		uri = 'mongodb://%s:%s@ds161210.mlab.com/sage-node' % (username, password)
		self.client = MongoClient(uri, 61210)
		self.db = self.client['sage-node']
		self.games = self.db.games

	def read(self, id):
		"""
		Takes in game `id` and outputs corresponding game object
		"""
                # # old hardcoded version:
		# obj = self.games.find_one({"_id": ObjectId("5acdb0beec89a84ad5873ee2")})
		obj = self.games.find_one({"_id": ObjectId(id)})
		# print(obj)
		return obj

	def save(self, hint):
		"""
		Takes in `hint` object, creates new game object, returns game id created
		"""
		obj = {"HintType" : "Poisson", "Hint" : hint}
		game_id = self.games.insert_one(obj).inserted_id
		return game_id

if __name__ == '__main__':
	db = SageDb()
	pprint.pprint(db.read())
	db.save(['setGraphicEffect:to:', 'doUntil', '=', 'getAttribute:of:'])

parser = argparse.ArgumentParser()
parser.add_argument('--gameid', type=int, help='sage-node -> games -> gameid')
parser.add_argument('--hint', type=str, help='string json')
args = parser.parse_args()
print(args.hint)
db = SageDb()
# hint = json.loads(args.hint)
# pprint.pprint(db.read())
# db.save(hint)

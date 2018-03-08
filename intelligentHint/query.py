class Database:
    # user: word
    map1 = {}
    map2 = {}

    def query(self, user, word):
        highScore = 0
        res = []
        if word not in self.map2.keys():
            return 0, res

        submap = self.map2[word]

        for k, v in submap.items():
            if (v > highScore):
                highScore = v
                res.append(k)
            elif (v == highScore):
                res.append(k)

        return (highScore, res)

    def insert(self, user, word):
        if (user not in self.map1.keys()):
            self.map1[user] = []

        for w in self.map1[user]:
            if (w not in self.map2.keys()):
                self.map2[w] = {}
            if (word not in self.map2[w].keys()):
                self.map2[w][word] = 0
            self.map2[w][word] += 1

            if (word not in self.map2.keys()):
                self.map2[word] = {}
            if (w not in self.map2[word].keys()):
                self.map2[word][w] = 0
            self.map2[word][w] += 1

        # print(self.map1)
        # print(self.map2)
        self.map1[user].append(word)

n = int(input())
db = Database()
put = []
for i in range(n):
    user, word = input().split(" ")
    put.append((user, word))

for i in range(n):
    result = []
    # print(i)
    user, word = put[i]
    (high, result) = db.query(user, word)

    print(high, end = ' ')
    for s in sorted(result):
        print(s, end = ' ')
    print('')
    db.insert(user, word)

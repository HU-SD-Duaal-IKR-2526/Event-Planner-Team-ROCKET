db = db.getSiblingDB('userprofile');
db.createUser({ user: 'userprofile', pwd: 'userprofile', roles: [{ role: 'readWrite', db: 'userprofile' }] });
db.createCollection('profiles');
db.profiles.createIndex({ accountId: 1 }, { unique: true });
db.profiles.createIndex({ updatedAt: -1 });
print('MongoDB: userprofile database ready');

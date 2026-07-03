db = db.getSiblingDB('userprofile');
db.createUser({ user: 'userprofile', pwd: 'userprofile', roles: [{ role: 'readWrite', db: 'userprofile' }] });
db.createCollection('profiles');
// Indexes worden door de applicatie zelf aangemaakt (spring.data.mongodb.auto-index-creation).
// Hier geen createIndex() — dat gaf een IndexOptionsConflict (zelfde keys, andere naam)
// met de door Spring gegenereerde indexes bij het opstarten.
print('MongoDB: userprofile database ready');

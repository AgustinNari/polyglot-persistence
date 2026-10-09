// Initialize only an unconfigured replica set; preserve existing data on reruns.
try {
    rs.status();
} catch (error) {
    if (error.code !== 94) throw error; // NotYetInitialized
    const result = rs.initiate({
        _id: 'rs0',
        members: [{ _id: 0, host: 'mongodb:27017' }]
    });
    if (result.ok !== 1) throw new Error('Replica set initialization failed');
}

const config = rs.conf();
if (config._id !== 'rs0' || config.members.length !== 1 || config.members[0].host !== 'mongodb:27017') {
    throw new Error('Unexpected replica set configuration; inspect the local volume before continuing');
}

// Wait for primary election rather than assuming a fixed startup duration.
let ready = false;
for (let attempt = 0; attempt < 120; attempt++) {
    if (db.hello().isWritablePrimary) {
        ready = true;
        break;
    }
    sleep(1000);
}
if (!ready) throw new Error('MongoDB did not become writable within 120 seconds');

db = db.getSiblingDB('tpo');
for (const collection of ['products', 'product_history']) {
    if (!db.getCollectionNames().includes(collection)) db.createCollection(collection);
}
db.products.createIndex({ nombre: 1 });
db.product_history.createIndex({ productId: 1, timestamp: -1 });

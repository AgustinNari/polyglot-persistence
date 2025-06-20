db = db.getSiblingDB('tpo');
db.createCollection('products');
db.createCollection('product_history');
db.products.createIndex({ nombre: 1 });
db.product_history.createIndex({ productId: 1, timestamp: -1 });

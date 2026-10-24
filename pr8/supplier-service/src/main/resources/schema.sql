CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0)
);

INSERT INTO suppliers (id, name, active) VALUES
    (1, 'Northwind Components', TRUE),
    (2, 'Volga Logistics Supply', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, supplier_id, sku, name, price, available_quantity) VALUES
    (1, 1, 'MON-27', 'Monitor 27', 28990.00, 15),
    (2, 1, 'KEY-MECH', 'Mechanical keyboard', 8490.00, 40),
    (3, 2, 'DOCK-USBC', 'USB-C docking station', 12990.00, 0)
ON CONFLICT (id) DO NOTHING;

SELECT setval('suppliers_id_seq', GREATEST((SELECT MAX(id) FROM suppliers), 1));
SELECT setval('products_id_seq', GREATEST((SELECT MAX(id) FROM products), 1));

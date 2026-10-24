CREATE TABLE IF NOT EXISTS customer_orders (
    id BIGSERIAL PRIMARY KEY,
    client_request_id VARCHAR(100) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_price NUMERIC(12, 2) NOT NULL,
    customer_name VARCHAR(160) NOT NULL,
    delivery_address VARCHAR(300) NOT NULL,
    status VARCHAR(40) NOT NULL,
    request_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

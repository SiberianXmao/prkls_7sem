CREATE TABLE customer_orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(120) NOT NULL,
    delivery_address VARCHAR(300) NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_amount NUMERIC(14, 2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(32) NOT NULL CHECK (status IN ('NEW', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_customer_orders_status ON customer_orders (status);

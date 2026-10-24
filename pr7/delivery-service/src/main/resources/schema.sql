CREATE TABLE IF NOT EXISTS deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_name VARCHAR(120) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    courier VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (
        status IN ('CREATED', 'ACCEPTED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')
    ),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_deliveries_order_id ON deliveries (order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries (status);

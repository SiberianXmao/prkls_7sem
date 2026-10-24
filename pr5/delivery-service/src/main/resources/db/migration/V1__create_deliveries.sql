CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    customer_name VARCHAR(120) NOT NULL,
    delivery_address VARCHAR(300) NOT NULL,
    courier_name VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL
        CHECK (status IN ('CREATED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')),
    estimated_delivery_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_deliveries_status ON deliveries (status);

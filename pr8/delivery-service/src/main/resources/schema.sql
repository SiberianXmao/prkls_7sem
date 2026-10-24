CREATE TABLE IF NOT EXISTS deliveries (
    id BIGSERIAL PRIMARY KEY,
    source_event_id UUID NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE,
    customer_name VARCHAR(160) NOT NULL,
    delivery_address VARCHAR(300) NOT NULL,
    order_total NUMERIC(12, 2) NOT NULL,
    courier VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL,
    request_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS delivery_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    request_id VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    UNIQUE (order_id, status)
);

CREATE INDEX IF NOT EXISTS idx_delivery_history_order ON delivery_status_history(order_id, id);

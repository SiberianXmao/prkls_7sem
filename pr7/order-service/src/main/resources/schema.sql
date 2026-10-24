CREATE TABLE IF NOT EXISTS customer_orders (
    id BIGINT PRIMARY KEY,
    customer_name VARCHAR(120) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(32) NOT NULL CHECK (status IN ('NEW', 'CONFIRMED', 'CANCELLED'))
);

INSERT INTO customer_orders (id, customer_name, delivery_address, total_amount, status)
VALUES
    (1, 'Анна Смирнова', 'Москва, ул. Тверская, 10', 2490.00, 'CONFIRMED'),
    (2, 'Иван Петров', 'Москва, Ленинский проспект, 25', 5790.00, 'CONFIRMED'),
    (3, 'Ольга Кузнецова', 'Москва, ул. Большая Дмитровка, 7', 1350.00, 'CANCELLED')
ON CONFLICT (id) DO NOTHING;

DROP TABLE IF EXISTS orders;

CREATE TABLE IF NOT EXISTS orders (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
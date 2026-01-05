
-- 1. AUTH DATABASE (vsms_auth_db)


-- Admin (password: admin123)
INSERT INTO app_users (email, password_hash, phone, role, status, created_at) VALUES 
('admin@vsms.com', '$2a$10$rO7pR3qKsWmKnMNQlY.dWeQo7T7YQjTmGfZO0FqnCrF0KsL0zFkYe', '9999999999', 'ADMIN', 'ACTIVE', NOW())
ON CONFLICT (email) DO NOTHING;
INSERT INTO admins (user_id, first_name, last_name, is_super_admin) 
SELECT id, 'System', 'Admin', true FROM app_users WHERE email = 'admin@vsms.com'
ON CONFLICT DO NOTHING;

-- Customers (password: password123)
INSERT INTO app_users (email, password_hash, phone, role, status, created_at) VALUES 
('customer@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543210', 'CUSTOMER', 'ACTIVE', NOW()),
('priya@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543211', 'CUSTOMER', 'ACTIVE', NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO customers (user_id, first_name, last_name, address, city, state, zip_code)
SELECT id, 'Rahul', 'Sharma', '123 MG Road', 'Bangalore', 'Karnataka', '560001' FROM app_users WHERE email = 'customer@vsms.com'
ON CONFLICT DO NOTHING;
INSERT INTO customers (user_id, first_name, last_name, address, city, state, zip_code)
SELECT id, 'Priya', 'Patel', '456 Brigade Road', 'Bangalore', 'Karnataka', '560002' FROM app_users WHERE email = 'priya@vsms.com'
ON CONFLICT DO NOTHING;

-- Technicians
INSERT INTO app_users (email, password_hash, phone, role, status, created_at) VALUES 
('tech@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543212', 'TECHNICIAN', 'ACTIVE', NOW()),
('tech2@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543213', 'TECHNICIAN', 'ACTIVE', NOW()),
('pending.tech@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543214', 'TECHNICIAN', 'PENDING', NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO technicians (user_id, first_name, last_name, specialization, experience_years, on_duty, current_workload, max_capacity)
SELECT id, 'Arjun', 'Kumar', 'ENGINE', 5, true, 1, 5 FROM app_users WHERE email = 'tech@vsms.com'
ON CONFLICT DO NOTHING;
INSERT INTO technicians (user_id, first_name, last_name, specialization, experience_years, on_duty, current_workload, max_capacity)
SELECT id, 'Vikram', 'Singh', 'ELECTRICAL', 3, true, 0, 5 FROM app_users WHERE email = 'tech2@vsms.com'
ON CONFLICT DO NOTHING;
INSERT INTO technicians (user_id, first_name, last_name, specialization, experience_years, on_duty, current_workload, max_capacity)
SELECT id, 'Amit', 'Verma', 'AC', 2, false, 0, 5 FROM app_users WHERE email = 'pending.tech@vsms.com'
ON CONFLICT DO NOTHING;

-- Managers (employee_id is required)
INSERT INTO app_users (email, password_hash, phone, role, status, created_at) VALUES 
('manager@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543215', 'MANAGER', 'ACTIVE', NOW()),
('inventory@vsms.com', '$2a$10$FH.jE1z5rWYmQ7rEv6VoZufU0fSxZGjQaYf7g5rvqN5DmV3IzVp6i', '9876543216', 'INVENTORY_MANAGER', 'ACTIVE', NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO managers (user_id, first_name, last_name, employee_id, department)
SELECT id, 'Suresh', 'Menon', 'MGR001', 'SERVICE_BAY' FROM app_users WHERE email = 'manager@vsms.com'
ON CONFLICT DO NOTHING;
INSERT INTO managers (user_id, first_name, last_name, employee_id, department)
SELECT id, 'Deepak', 'Reddy', 'MGR002', 'INVENTORY' FROM app_users WHERE email = 'inventory@vsms.com'
ON CONFLICT DO NOTHING;

-- 2. VEHICLE DATABASE (vsms_vehicle_db)


INSERT INTO vehicles (customer_id, plate_number, brand, model, year, fuel_type, vehicle_type, created_at) VALUES
(1, 'KA01AB1234', 'Maruti Suzuki', 'Swift', 2020, 'PETROL', 'FOUR_WHEELER', NOW()),
(1, 'KA05CD5678', 'Honda', 'City', 2022, 'PETROL', 'FOUR_WHEELER', NOW()),
(2, 'KA03EF9012', 'Toyota', 'Innova', 2021, 'DIESEL', 'FOUR_WHEELER', NOW()),
(2, 'KA07GH3456', 'Hyundai', 'Creta', 2023, 'DIESEL', 'FOUR_WHEELER', NOW())
ON CONFLICT (plate_number) DO NOTHING;



-- 3. SERVICE REQUEST DATABASE (vsms_service_db)
-

INSERT INTO service_requests (customer_id, vehicle_id, technician_id, bay_number, service_type, description, priority, status, created_at) VALUES
(1, 1, NULL, NULL, 'REGULAR_SERVICE', 'Regular service and oil change', 'NORMAL', 'PENDING', NOW()),
(1, 2, 1, 1, 'REPAIR', 'Engine knocking sound', 'URGENT', 'ASSIGNED', NOW()),
(2, 3, 1, 2, 'REPAIR', 'AC not cooling properly', 'NORMAL', 'IN_PROGRESS', NOW() - INTERVAL '2 hours'),
(2, 4, 2, 3, 'REPAIR', 'Brake pad replacement', 'URGENT', 'COMPLETED', NOW() - INTERVAL '1 day')
ON CONFLICT DO NOTHING;



-- 4. INVENTORY DATABASE (vsms_inventory_db)
-

INSERT INTO parts (part_number, name, description, category, quantity, unit_price, reorder_level, created_at) VALUES
('ENG-OIL-5W30', 'Engine Oil 5W30', 'Fully synthetic engine oil 5L', 'FLUIDS', 50, 1500.00, 10, NOW()),
('ENG-FILT-01', 'Oil Filter', 'Universal oil filter', 'FILTERS', 30, 450.00, 5, NOW()),
('BRK-PAD-FR', 'Front Brake Pads', 'Ceramic brake pads - Front set', 'BRAKES', 3, 2500.00, 5, NOW()),
('BRK-DISC-FR', 'Front Brake Disc', 'Ventilated brake disc - Front pair', 'BRAKES', 8, 4500.00, 3, NOW()),
('AC-GAS-R134', 'AC Gas R134a', 'Refrigerant gas 500g can', 'OTHER', 2, 800.00, 5, NOW()),
('ELE-BAT-45A', 'Battery 45Ah', '12V 45Ah maintenance free battery', 'ELECTRICAL', 12, 5500.00, 4, NOW())
ON CONFLICT (part_number) DO NOTHING;

INSERT INTO part_requests (part_id, service_request_id, technician_id, requested_quantity, status, notes, created_at) VALUES
(3, 3, 1, 2, 'PENDING', 'Required for brake pad replacement', NOW()),
(5, 3, 1, 2, 'PENDING', 'AC refill required', NOW())
ON CONFLICT DO NOTHING;

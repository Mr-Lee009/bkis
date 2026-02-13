# mock data for courses table
INSERT INTO courses (title, description, teacher_id, price, tag, created_by, updated_by)
VALUES ('Introduction to Python', 'Learn the basics of Python programming.', 'teacher-uuid-1', 49.99,
        '#programming,#python', 'admin-uuid', 'admin-uuid')
     , ('Web Development with JavaScript', 'Build dynamic websites using JavaScript.', 'teacher-uuid-2', 59.99,
        '#webdev,#javascript', 'admin-uuid', 'admin-uuid')
     , ('Data Science Fundamentals', 'An introduction to data science concepts and tools.', 'teacher-uuid-3', 79.99,
        '#datascience,#python', 'admin-uuid', 'admin-uuid')
     , ('Machine Learning Basics', 'Understand the fundamentals of machine learning.', 'teacher-uuid-4', 89.99,
        '#machinelearning,#ai', 'admin-uuid', 'admin-uuid')
     , ('Java Programming for Beginners', 'Start your journey with Java programming.', 'teacher-uuid-5', 39.99,
        '#programming,#java', 'admin-uuid', 'admin-uuid')
     , ('Java advanced Programming', 'Deep dive into advanced Java topics.', 'teacher-uuid-5', 69.99,
        '#programming,#java,#advanced', 'admin-uuid', 'admin-uuid')
;


select * from courses;

-- mock data for product table
insert product (name, slug, description) values
       ('iphone-15', 'iphone-15', 'The latest iPhone model with advanced features'),
       ('samsung-galaxy-s23', 'samsung-galaxy-s23', 'A flagship smartphone from Samsung'),
       ('macbook-pro', 'macbook-pro', 'A high-performance laptop for professionals'),
       ('airpods', 'airpods', 'Wireless earbuds with excellent sound quality');

select * from product;
-- mock data for product_variant table
insert product_variant (product_id, sku, price, stock_quantity)
values (1, 'iphone-15-128gb', 999.99, 50),
       (1, 'iphone-15-256gb', 1199.99, 30),
       (2, 'samsung-galaxy-s23-128gb', 899.99, 40),
       (2, 'samsung-galaxy-s23-256gb', 1099.99, 20),
       (3, 'macbook-pro-13', 1299.99, 25),
       (3, 'macbook-pro-16', 2399.99, 15),
       (4, 'airpods-gen3', 199.99, 100),
       (4, 'airpods-pro', 249.99, 80);

select * from `option`;
select * from `option_value`;
select * from `product_variant_option_value`;
 -- mock data for option table
insert `option` (name) values ('Color'), ('Storage'), ('Size');
-- mock data for option_value table
insert `option_value` (option_id, value) values
            (1, 'Black'),
            (1, 'White'),
            (1, 'Blue'),
            (2, '128GB'),
            (2, '256GB'),
            (3, '13-inch'),
            (3, '16-inch');
-- mock data for product_variant_option_value table
insert product_variant_option_value (product_variant_id, option_value_id) values
            (1, 1), -- iPhone 15 128GB - Black
            (1, 4), -- iPhone 15 128GB - 128GB
            (2, 2), -- iPhone 15 256GB - White
            (2, 5), -- iPhone 15 256GB - 256GB
            (3, 1), -- Samsung Galaxy S23 128GB - Black
            (3, 4), -- Samsung Galaxy S23 128GB - 128GB
            (4, 2), -- Samsung Galaxy S23 256GB - White
            (4, 5), -- Samsung Galaxy S23 256GB - 256GB
            (5, 1), -- MacBook Pro 13-inch - Black
            (5, 6), -- MacBook Pro 13-inch - 13-inch
            (6, 1), -- MacBook Pro 16-inch - Black
            (6, 7), -- MacBook Pro 16-inch - 16-inch
            (7, 1), -- AirPods Gen3 - Black
            (8, 2); -- AirPods Pro - White

-- select statements to verify the inserted data
select p.name,
       pv.sku,
       pv.price,
       pv.stock_quantity
from product p
         inner join product_variant pv
                    on p.id = pv.product_id
         inner join product_variant_option_value pvov
                    on pv.id = pvov.product_variant_id
where p.id in (1);

select * from product_variant;
select * from product_variant_option_value;

-- mock data for users table
insert users (id,username, email, password_hash, role, created_by, updated_by)
values ('7397449d-a742-11f0-8f5e-0242ac130002','student1', 'ducla@gmail.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'STUDENT', 'admin-uuid', 'admin-uuid'),
       ('73974a1e-a742-11f0-8f5e-0242ac130003', 'admin', 'admin@gmail.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'ADMIN', 'admin-uuid', 'admin-uuid');

-- bycrypt hash for 'admin'
-- $2a$10$7EqJtq98hPqEX7fNZaFWoOa5f6c5H8b6j5eF6b6j5eF6b6j5eF6b6
-- generate UUID: select uuid();
select * from users
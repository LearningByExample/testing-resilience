DROP TABLE IF EXISTS offers;
CREATE TABLE offers (
  id serial primary key,
  name varchar(40) not null,
  price float
);

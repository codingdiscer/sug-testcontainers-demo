CREATE TABLE game (
  game_id serial PRIMARY KEY,
  game_name text,
  complexity integer
);

insert into game (game_name, complexity) values ('Sorry', 0);
insert into game (game_name, complexity) values ('King Of New York', 1);
insert into game (game_name, complexity) values ('Mage Knight', 2);
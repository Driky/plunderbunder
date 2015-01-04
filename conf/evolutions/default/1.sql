# --- !Ups

CREATE TABLE sde_maintenance (data_set varchar(100), last_import datetime);

CREATE TABLE sde_regions (region_id integer primary key,
    region_name varchar(100),
    x real, y real, z real, x_min real, x_max real, y_min real, y_max real, z_min real, z_max real, 
    faction_id integer, radius real);
    
CREATE TABLE sde_solarsystems (solar_system_id integer primary key,
    solar_system_name varchar(100),
    region_id integer,
    faction_id integer,
    radius real,
    luminosity real,
    sun_type_id integer,
    constellation_id integer,
    x real,
    y real,
    z real,
    security real,
    security_class varchar(2),
    border boolean,
    constellation boolean,
    corridor boolean,
    fringe boolean,
    hub boolean,
    international boolean,
    regional boolean);
    
CREATE TABLE sde_inventorytypes(
    id integer primary key,
    base_price real,
    capacity real,
    chance_of_duplicating real,
    description text,
    group_id integer,
    market_group_id integer,
    mass real,
    portion_size integer,
    published integer,
    race_id integer,
    name varchar(100),
    volume real);
    
CREATE TABLE sde_blueprint(
    id integer primary key,
    type_id integer,
    max_production_limit integer
);

CREATE TABLE sde_blueprint_activity (
    id integer primary key auto_increment,
    blueprint_id integer,
    activity_type integer,
    activity_time integer
);

CREATE TABLE sde_blueprint_activity_materials(
    blueprint_activity_id integer,
    type_id integer,
    quantity integer
);

CREATE TABLE sde_blueprint_activity_skills(
    blueprint_activity_id integer,
    type_id integer,
    level integer
);

CREATE TABLE sde_blueprint_activity_products(
    blueprint_activity_id integer,
    type_id integer,
    quantity integer,
    probability real
);

-- need to create an index on bp_materials.blueprint

CREATE TABLE sde_blueprint_
    
# --- !Downs

DROP TABLE IF EXISTS sde_regions;
DROP TABLE IF EXISTS sde_solarsystems;
DROP TABLE IF EXISTS sde_maintenance;
DROP TABLE IF EXISTS sde_inventorytypes;
DROP TABLE IF EXISTS sde_blueprint;
DROP TABLE IF EXISTS sde_blueprint_activity;
DROP TABLE IF EXISTS sde_blueprint_activity_materials;
DROP TABLE IF EXISTS sde_blueprint_activity_skills;
DROP TABLE IF EXISTS sde_blueprint_activity_products;
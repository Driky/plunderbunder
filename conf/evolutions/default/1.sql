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
    radius decimal,
    luminosity decimal,
    sun_type_id integer,
    constellation_id integer,
    x decimal,
    y decimal,
    z decimal,
    security decimal,
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
    base_price decimal,
    capacity decimal,
    chance_of_duplicating decimal,
    description text,
    group_id integer,
    market_group_id integer,
    mass decimal,
    portion_size integer,
    published integer,
    race_id integer,
    name varchar(100),
    volume decimal);
    
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
    probability decimal
);

CREATE TABLE sde_stations(
    station_id int primary key,
    constellation_id int,
    corporation_id int, 
    docking_cost_per_volume decimal,
    max_ship_volume_dockable decimal,
    office_rental_cost int,
    operation_id int,
    region_id int,
    reprocessing_efficiency decimal,
    reprocessing_hangar_flag int,
    reprocessing_stations_take decimal,
    security decimal,
    solar_system_id int,
    station_name varchar(255),
    station_type_id int,
    x decimal,
    y decimal,
    z decimal
);

CREATE TABLE sde_marketgroups (
    description varchar(255),
    has_types boolean,
    icon_id int,
    market_group_id int,
    market_group_name varchar(255),
    parent_group_id int
);

CREATE TABLE plunderbunder_users (
    id integer primary key auto_increment,
    eve_id integer,
    character_name varchar(64),
    character_id integer,
    api_key_id integer,
    api_key_vcode char(64),
    access_mask integer,
    email_address varchar(255)
);



-- need to create an index on bp_materials.blueprint
    
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
DROP TABLE IF EXISTS sde_stations;
DROP TABLE IF EXISTS sde_marketgroups;

DROP TABLE IF EXISTS plunderbunder_users;
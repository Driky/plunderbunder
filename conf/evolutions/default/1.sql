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
    regional boolean)
    
# --- !Downs

DROP TABLE sde_regions;
DROP TABLE sde_solarsystems;
DROP TABLE sde_maintenance;
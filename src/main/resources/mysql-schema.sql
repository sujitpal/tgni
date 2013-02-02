-- $Id$
-- $Source$
create table oid_name (
  oid integer not null,
  name varchar(255) not null,
  pri char(1) not null
) type=innodb;

create table oid_nid (
  oid integer not null,
  nid integer not null
) type=innodb;

-- create before running load relationships
create index ux1_oid on oid_nid(oid);

-- create after running load relationships
create index ax1_oid_pri on oid_name(oid,pri);

create index ax1_oid on oid_name(oid);

create index ax1_name on oid_name(name);


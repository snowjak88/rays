create table render (uuid varchar(64) not null, completed timestamp, created timestamp, decomposed boolean not null, film_json varchar(255) not null, height integer not null, percent_complete integer, png_base64 clob(4194304), renderer_json varchar(255) not null, sampler_json varchar(255) not null, spp integer not null, offsetx integer not null, offsety integer not null, submitted timestamp, version bigint not null, width integer not null, parent_uuid varchar(64), scene_id bigint not null, primary key (uuid));
create table scene (id bigint generated by default as identity (start with 1), json clob(1048576) not null, nickname varchar(255), version bigint not null, primary key (id));
alter table render add constraint FK_render_parent foreign key (parent_uuid) references render;
alter table render add constraint FK_render_scene foreign key (scene_id) references scene;
create table render_setup (id bigint not null auto_increment, version bigint not null, created timestamp, scene_id bigint not null, film_json varchar(1024) not null, renderer_json varchar(1024) not null, sampler_json varchar(1024) not null, camera_json varchar(1024) not null, primary key (id) ) engine=MyISAM;

alter table render drop foreign key FK_render_scene;
alter table render_setup add constraint FK_render_setup_scene foreign key (scene_id) references scene (id);

alter table render add temp_render_setup_id bigint null;
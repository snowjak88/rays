alter table render add render_setup_id bigint null;
alter table render add constraint FK_render_render_setup foreign key (render_setup_id) references render_setup (id);

update render set render_setup_id = temp_render_setup_id;
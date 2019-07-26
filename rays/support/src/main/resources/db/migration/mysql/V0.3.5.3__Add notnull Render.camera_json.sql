alter table render add camera_json varchar(255) null;

update render set camera_json = temp_camera_json;
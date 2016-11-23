alter table friends add resource_app_id int;
create index resource_app_id on friends(resource_app_id);
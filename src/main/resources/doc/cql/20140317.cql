create KEYSPACE faceshow with replication = {'class' : 'SimpleStrategy','replication_factor':2};

#嘟嘟小秘书记录
insert into users(id,nickname,user_type,mobile_verify,app_id,create_time,avatar) values('88888888888888888888888888888888','ChatGame',1,1,0,'2014-03-11 13:40:14+0800','robot');
insert into user_session_indexes(user_id,app_id,session_id) values('88888888888888888888888888888888',0,'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx');
insert into user_sessions(session_id,user_id) values('xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx','88888888888888888888888888888888');


#
alter table user_mobiles add countrycode text;
alter table fileinfo add ext text;

alter table phone_books add name text;


#插入默认用户头像数据(本地库)
insert into fileinfo(id,bucketname,creator,date,region,service,ext) values('default','/opt/faceshow/data/file/',null,null,'US_WEST_1','amazon s3','.png');

#插入默认小秘书头像数据(本地库)
insert into fileinfo(id,bucketname,creator,date,region,service,ext) values('robot','/opt/faceshow/data/file/',null,null,'US_WEST_1','amazon s3','.jpg');



#插入默认用户头像数据(s3)
insert into fileinfo(id,bucketname,creator,date,region,service,ext) values('default','handwin1',null,null,'US_WEST_1','amazon s3','.png');

#插入默认小秘书头像数据(s3)
insert into fileinfo(id,bucketname,creator,date,region,service,ext) values('robot','handwin1',null,null,'US_WEST_1','amazon s3','.jpg');



#补加小秘书为好友
insert into friends(user_id,app_id,friend_id,contact_name,update_time) values ('8c42bb50c07f11e381823f6a5f510a36',0,'88888888888888888888888888888888','',1397789623132);
insert into friends(user_id,app_id,friend_id,contact_name,update_time) values ('226bea30c09311e39b7ddfbde848fa27',0,'88888888888888888888888888888888','',1397789623132);




#版本默认数据
insert into version_manager (device_type, app_id , cert, download_url,update_time,version_desc) values (1,0,'4','1.0.5') ;
insert into version_manager (device_type, app_id , cert, download_url,update_time,version_desc) values (1,0,'3','itms-services:///?action=download-manifest&url=https://d.v5.cn/store/chatgame/chatgame_1.0.13.plist',2014030610,'ios private') ;
insert into version_manager (device_type, app_id , cert, download_url,update_time,version_desc) values (2,0,'0','http://us.v5.cn',2014030610,'android') ;
select * from version_manager ;

insert into version_manager (device_type, app_id , cert,client_version) values (1,0,'4','1.0.3') ;
insert into version_manager (device_type, app_id , cert,client_version) values (1,0,'3','1.0.3') ;
insert into version_manager (device_type, app_id , cert,client_version) values (2,0,'0','1.0.3') ;


 #
insert into server_pools (country_code, addr ) values ('0001','114.215.193.49:7000');
insert into server_pools (country_code, addr ) values ('0086','114.215.193.49:7000');

insert into server_pools (country_code, addr ) values ('0001','54.187.6.98:7000,54.186.79.170:7000');
insert into server_pools (country_code, addr ) values ('0086','114.215.183.224:7000');
cqlsh:faceshow_ok> select * from server_pools ;


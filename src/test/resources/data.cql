insert into users(id,nickname,app_id,countrycode,mobile,account) values('0cb6d440a4f811e3ac7abfd52f77bd17','user2',0,'0086','099891fc266cad3d6dae315d9518dd32','13800000000abc');
insert into users(id,nickname,app_id,countrycode,mobile) values('0cb6d442a4f811e3ac7abfd52f77bd17','user3',0,'0086','9412413345a0b706a74e7f0694132859');
insert into users(id,nickname,app_id,countrycode,mobile) values('0cb6d444a4f811e3ac7abfd52f77bd17','user4',0,'0086','4c3937ee17250c507f4669290033bcac');
insert into users(id,nickname,app_id,countrycode,mobile) values('0cb6d443a4f811e3ac7abfd52f77bd17','user5',0,'0086','1f4483b21850816720f47450bdd50d3f');
insert into users(id,nickname,app_id,countrycode,mobile,mobile_verify) values('0cb6d443a4f811e3ac7abfd52f77bd18','user6',0,'0086','a2b8f8bb63c66f44e0dde0140f7c06b6',1);
insert into users(id,nickname,app_id,countrycode,mobile,mobile_verify) values('0cb6d443a4f811e3ac7abfd52f77bd19','user7',0,'0086','9992a81e87ca5d0e915d69d9b377a526',0);

insert into user_sessions(session_id,user_id,app_id) values('0','0cb6d440a4f811e3ac7abfd52f77bd17',0);
insert into user_sessions(session_id,user_id,app_id) values('1','0cb6d443a4f811e3ac7abfd52f77bd18',0);
insert into user_sessions(session_id,user_id,app_id) values('2','0cb6d443a4f811e3ac7abfd52f77bd19',0);


insert into account_indexes(account,user_id) values('13800000000abc','0cb6d440a4f811e3ac7abfd52f77bd17');



insert into user_mobiles(mobile,countrycode,user_id) values('099891fc266cad3d6dae315d9518dd32','0086','0cb6d440a4f811e3ac7abfd52f77bd17');
insert into user_mobiles(mobile,countrycode,user_id) values('9412413345a0b706a74e7f0694132859','0086','0cb6d442a4f811e3ac7abfd52f77bd17');
insert into user_mobiles(mobile,countrycode,user_id) values('4c3937ee17250c507f4669290033bcac','0086','0cb6d444a4f811e3ac7abfd52f77bd17');
insert into user_mobiles(mobile,countrycode,user_id) values('1f4483b21850816720f47450bdd50d3f','0086','0cb6d443a4f811e3ac7abfd52f77bd17');

insert into phone_new_books(mobile, country_code, userid, create_time, name) values('099891fc266cad3d6dae315d9518dd32', '0086', '0cb6d442a4f811e3ac7abfd52f77bd17', 1415091504197, 'user3');
insert into phone_new_books(mobile, country_code, userid, create_time, name) values('099891fc266cad3d6dae315d9518dd32', '0086', '0cb6d444a4f811e3ac7abfd52f77bd17', 1415091504197, 'user4');
insert into phone_new_books(mobile, country_code, userid, create_time, name) values('099891fc266cad3d6dae315d9518dd32', '0086', '0cb6d443a4f811e3ac7abfd52f77bd17', 1415091504197, 'user5');

insert into friends(user_id, app_id, friend_id, contact_name, resource_app_id, update_time) values ('0cb6d440a4f811e3ac7abfd52f77bd17', 0, '0cb6d442a4f811e3ac7abfd52f77bd17', 'Ethernet', 0, 1);
insert into friends(user_id, app_id, friend_id, contact_name, resource_app_id, update_time) values ('0cb6d442a4f811e3ac7abfd52f77bd17|FOLLOWERS', 0, '0cb6d440a4f811e3ac7abfd52f77bd17', '', 0, 1);

insert into user_messages(user_id,  message_id, message,content) values ('0cb6d443a4f811e3ac7abfd52f77bd18', 0,  '{"id":"0","content":"0"}',0x1234);
insert into user_messages(user_id,  message_id, message,content) values ('0cb6d443a4f811e3ac7abfd52f77bd18', 1,  '{"id":"1","content":"1"}',0x1234);
insert into user_messages(user_id,  message_id, message,content) values ('0cb6d443a4f811e3ac7abfd52f77bd18', 2,  '{"id":"2","content":"2"}',0x1234);
insert into user_messages(user_id,  message_id, message,content) values ('0cb6d440a4f811e3ac7abfd52f77bd18', 3,  '{"id":"3","content":"3"}',0xFFFF);
insert into user_messages(user_id,  message_id, message,content) values ('0cb6d443a4f811e3ac7abfd52f77bd18', 5,  '{"id":"5","content":"5"}',0xCCCC);

update user_localmsg_counter set counter=counter+2 where user_id='0cb6d443a4f811e3ac7abfd52f77bd18';
alter table users add avatar_url text;

CREATE TABLE client_versions(
    version_desc text,
    download_url text,
    device_type int,
    cert text,
    app_id int,
    PRIMARY KEY(device_type, cert, app_id)
) WITH COMMENT = 'Create table for entity "cn.v5.entity.AppVersion"';

insert into client_versions (device_type, cert, app_id, version_desc, download_url) values (2, '1', 0, 'latest version of ChatGame', 'http://play.google.com/chatgame_latest.apk,http://d.v5.cn/store/chatgame/chatgame_1.0.54.apk');
insert into client_versions (device_type, cert, app_id, version_desc, download_url) values (1, '3', 0, 'latest version of ChatGame', 'itms-services:///?action=download-manifest&url=https://d.v5.cn/store/chatgame/chatgame_1.0.37.plist');
insert into client_versions (device_type, cert, app_id, version_desc, download_url) values (1, '4', 0, 'latest version of ChatGame', 'itms-services:///?action=download-manifest&url=https://d.v5.cn/store/chatgame/chatgame_1.0.37.plist');
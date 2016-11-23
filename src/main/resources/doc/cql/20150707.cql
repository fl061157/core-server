alter table contact_request add source text;

alter table friends add source text;

INSERT INTO app_key_info (app_key, app_secret ) VALUES ( 100000,'dadcseqd13131cdadfa1');

CREATE TABLE cg_third_app_users(
    user_id text,
    type text,
    third_app_user_id text,
    PRIMARY KEY(user_id, type)
) WITH COMMENT = 'cg user_id -->third app user id';

CREATE TABLE third_app_users_cg(
    type text,
    third_app_user_id text,
    user_id text,
    access_token text,
    PRIMARY KEY((type, third_app_user_id),user_id)
) WITH COMMENT = 'cg user_id -->third app user id';

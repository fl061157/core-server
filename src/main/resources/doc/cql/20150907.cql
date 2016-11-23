CREATE TABLE user_call_sm_send(
    receiver_id text,
    from_id text,
    count int,
    update_time text,
    PRIMARY KEY((receiver_id), from_id)
) WITH COMMENT = 'user receive sm for calling';
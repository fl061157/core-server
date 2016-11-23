在keyspace op_manager_cn和op_manager_us上创建表

  --测试环境
  --中国区增加 op_manager_cn
  CREATE KEYSPACE op_manager_cn WITH replication = {'class': 'NetworkTopologyStrategy', 'ALIYUN_TEST': '1'}  AND durable_writes = true;

  --美国区增加op_manager_us
  CREATE KEYSPACE op_manager_us WITH replication = {'class': 'NetworkTopologyStrategy', 'AWS_TEST': '1'}  AND durable_writes = true;


  正式环境
  --中国区增加 op_manager_cn
     CREATE KEYSPACE op_manager_cn WITH replication = {'class': 'NetworkTopologyStrategy', 'cn-north-1': '3'}  AND durable_writes = true;
  --美国区增加 op_manager_us
     CREATE KEYSPACE op_manager_us WITH replication = {'class': 'NetworkTopologyStrategy', 'us-west-2': '3'}  AND durable_writes = true;

CREATE TABLE app_key_info(
    app_key int,
    app_secret text,
    status text,
    PRIMARY KEY(app_key)
) WITH COMMENT = 'app key info';

insert into app_key_info('app_key','app_secret')values(100000,'ddfcaqe23413cdfdac1343ad');

CREATE TABLE app_user_cg_user(
    app_key int,
    app_user_id text,
    user_id text,
    PRIMARY KEY((app_key,app_user_id))
) WITH COMMENT = 'app user-->cg user';


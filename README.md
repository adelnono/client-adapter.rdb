## Forked from Alibaba，魔改内容
魔改配置文件：conf/rdb/mytest_user.yml文件
* 魔改 1：新增同步表限制，只同步配置的表
* 魔改 2：新增同步表数据限制，只同步匹配的数据

注：去掉了 es 和 hbase 内容，还不需要

## 四、关系型数据库适配器

RDB adapter 用于适配mysql到任意关系型数据库(需支持jdbc)的数据同步及导入

### 4.1 修改启动器配置: application.yml, 这里以oracle目标库为例
```
server:
  port: 8081
logging:
  level:
    com.alibaba.otter.canal.client.adapter.rdb: DEBUG
......

canal.conf:
  canalServerHost: 127.0.0.1:11111
  srcDataSources:
    defaultDS:
      url: jdbc:mysql://127.0.0.1:3306/mytest?useUnicode=true
      username: root
      password: 121212
  canalInstances:
  - instance: example
    groups:
    - outAdapters:
      - name: rdb                                               # 指定为rdb类型同步
        key: oracle1                                            # 指定adapter的唯一key, 与表映射配置中outerAdapterKey对应
        properties:
          jdbc.driverClassName: oracle.jdbc.OracleDriver        # jdbc驱动名, jdbc的jar包需要自行放致lib目录下
          jdbc.url: jdbc:oracle:thin:@localhost:49161:XE        # jdbc url
          jdbc.username: mytest                                 # jdbc username
          jdbc.password: m121212                                # jdbc password
          threads: 5                                            # 并行执行的线程数, 默认为1
          commitSize: 3000                                      # 批次提交的最大行数
```
其中 outAdapter 的配置: name统一为rdb, key为对应的数据源的唯一标识需和下面的表映射文件中的outerAdapterKey对应, properties为目标库jdb的相关参数
adapter将会自动加载 conf/rdb 下的所有.yml结尾的表映射配置文件

### 4.2 适配器表映射文件
修改 conf/rdb/mytest_user.yml文件:
```
dataSourceKey: defaultDS        # 源数据源的key, 对应上面配置的srcDataSources中的值
destination: example            # cannal的instance或者MQ的topic
outerAdapterKey: oracle1        # adapter key, 对应上面配置outAdapters中的key
concurrent: true                # 是否按主键hase并行同步, 并行同步的表必须保证主键不会更改及主键不能为其他同步表的外键!!

# 魔改 start
# 限定的数据，表包含以下字段，则匹配上了才同步
shopCode: ${SHOP_CODE}
entId: ${ENT_ID}
erpCode: ${ERP_CODE}

# 限定同步的表
syncTables: 
  - table1
  - table2
  - table3
# 魔改 end

dbMapping:
  database: mytest              # 源数据源的database/shcema
  table: user                   # 源数据源表名
  targetTable: mytest.tb_user   # 目标数据源的库名.表名
  targetPk:                     # 主键映射
    id: id                      # 如果是复合主键可以换行映射多个
#  mapAll: true                 # 是否整表映射, 要求源表和目标表字段名一模一样 (如果targetColumns也配置了映射,则以targetColumns配置为准)
  targetColumns:                # 字段映射, 格式: 目标表字段: 源表字段, 如果字段名一样源表字段名可不填
    id:
    name:
    role_id:
    c_time:
    test1: 
```
导入的类型以目标表的元类型为准, 将自动转换

### 4.3 启动RDB数据同步
#### 将目标库的jdbc jar包放入lib文件夹, 这里放入ojdbc6.jar

#### 启动canal-adapter启动器
```
bin/startup.sh
```
#### 验证
修改mysql mytest.user表的数据, 将会自动同步到Oracle的MYTEST.TB_USER表下面, 并会打出DML的log

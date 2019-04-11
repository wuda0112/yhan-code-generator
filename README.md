# 简介
Mysql Mybatis代码生成工具。

```
1. 根据 Mysql create table DDL 生成表对应的实体，Mybatis Mapper , 表名列名常量，Sql Builder
2. 生成单个表的insert , batch insert , deleteByPrimaryKey , updateByPrimaryKey ,selectByPrimaryKey , selectByIndex 方法
```

# 快速体验
有完整的Test代码

- [GeneratorTestBase.java](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/GeneratorTestBase.java/)　中先指定代码生成后的包名，以及在你电脑上存放的位置，比如放在E://code目录下
- [EntityGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/EntityGeneratorTest.java/)  生成实体类
- [MybatisMapperGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/MybatisMapperGeneratorTest.java/)  生成Mybatis Mapper
- [SqlBuilderGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/SqlBuilderGeneratorTest.java/)  生成SqlBuilder
- [TableMetaInfoGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/TableMetaInfoGeneratorTest.java/)  生成表的常量
- [TableTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/TableTest.java/)  Create Table DDL解析


# 主要解决的问题
Mybatis　ORM 代码生成工具很多，为什么还要写这个呢？因为，很多生成工具生成

- select方法都是select所有字段，假如一个表有很多字段，或者有文章内容这样的大字段，查询所有字段就不好了
- 新增和修改也是作用在所有字段，有人会说，新增和修改的时候也有【!=null && !=""】这样的判断，这样就不是所有字段了，但是这样判断对吗？假如表中某个字段就是要update成【null 或者 ""】呢？这个需求很正常！
- select方法只有根据主键查询，正常理解，有索引的字段都可以生成对应的查询方法吧？
- XML真的不方便

# 对应的解决方案
### 举例用的表和生成的代码在文章后面，可以对照着看

- insert单条记录时，只有【实体中调用过set方法的属性】对应的列才会放到insert语句中，不会把表中所有列都列出来。如果一个实体没有任何字段调用过set方法，则不会执行update语句。举例，假如只有username字段调用过set方法，而nickname字段没有调用过set方法，那么update语句是

```
insert into my_schema.user_basic (username) values(#{username})
```

- update记录时，只有【实体中调用过set方法的属性】对应的列才会放到update语句中，不会把表中所有列都列出来。如果一个实体没有任何字段调用过set方法，则不会执行update语句。举例，假如只有username字段调用过set方法，而nickname字段没有调用过set方法，那么update语句是

```
UPDATE my_schema.user_basic SET username = #{username} WHERE id = #{id}
```


- 所有的select方法，都不会默认去返回所有列，必须指定返回哪些列，举例，【retrieveColumns】用于指定需要返回的列，放心，不用去写很多字符串，直接使用【UserBasicMetaInfo】中的各种常量即可

```
UserBasic selectByPrimaryKey(@Param("id") Long id,
                               @Param("retrieveColumns") String[] retrieveColumns);
```

- 不仅可以根据主键查询，当表中有索引时，也会生成根据索引查询的方法。当索引是唯一索引时，生成的查询方法，返回值是表对应的实体；其他索引时，生成的查询方法则返回实体的集合，并且输入参数有分页参数
。

比如【username】字段上有唯一索引，则生成的查询方法是，注意返回值和输入参数

```
@SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectByUsername"
  )
  UserBasic selectByUsername(@Param("username") String username,
                             @Param("retrieveColumns") String[] retrieveColumns);
```
比如【nickname】字段是有普通索引，则生成的查询方法是，注意返回值和输入参数

```
@SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectByNickname"
  )
  List<UserBasic> selectByNickname(@Param("nickname") String nickname, @Param("offset") int offset,
                                   @Param("rowCount") int rowCount, @Param("retrieveColumns") String[] retrieveColumns);
```

- 不使用XML文件

# 举例
如果有如下 Create table DDL

```
CREATE TABLE `my_schema`.`user_basic` (
	`id` INT(10) UNSIGNED NOT NULL,
	`username` VARCHAR(50) NULL DEFAULT NULL,
	`nickname` VARCHAR(50) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `idx_username` (`username`),
	INDEX `idx_nickname` (`nickname`)
)
```

## 实体类

```
@Table(
    schema = "my_schema",
    name = "user_basic"
)
public final class UserBasic implements TableEntity, Serializable {
  @Column(
      name = "id",
      length = 10,
      columnDefinition = "INT(10) UNSIGNED"
  )
  private Long id;

  @IsSetField(
      referenceField = "id"
  )
  private boolean idIsSet;

  @Column(
      name = "username",
      length = 50,
      columnDefinition = "VARCHAR(50)"
  )
  private String username;

  @IsSetField(
      referenceField = "username"
  )
  private boolean usernameIsSet;

  @Column(
      name = "nickname",
      length = 50,
      columnDefinition = "VARCHAR(50)"
  )
  private String nickname;

  @IsSetField(
      referenceField = "nickname"
  )
  private boolean nicknameIsSet;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id=id;
    this.idIsSet=true;
  }

  public boolean getIdIsSet() {
    return idIsSet;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username=username;
    this.usernameIsSet=true;
  }

  public boolean getUsernameIsSet() {
    return usernameIsSet;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname=nickname;
    this.nicknameIsSet=true;
  }

  public boolean getNicknameIsSet() {
    return nicknameIsSet;
  }
}
```
## 元数据信息

```
public final class UserBasicMetaInfo {
  public static final String SCHEMA = "my_schema";

  public static final String TABLE = "user_basic";

  public static final String SCHEMA_DOT_TABLE = "`my_schema`.`user_basic`";

  public static final String[] PRIMARY_KEY = new String[]{"id"};

  public static final String ID = "id";

  public static final String ID_AS = "id AS id";

  public static final String ID_CONCAT_TABLE_AS = "id AS userBasicId";

  public static final String USERNAME = "username";

  public static final String USERNAME_AS = "username AS username";

  public static final String USERNAME_CONCAT_TABLE_AS = "username AS userBasicUsername";

  public static final String NICKNAME = "nickname";

  public static final String NICKNAME_AS = "nickname AS nickname";

  public static final String NICKNAME_CONCAT_TABLE_AS = "nickname AS userBasicNickname";
}
```
## Mybatis Mapper

```
@Mapper
public interface UserBasicMapper {
  @InsertProvider(
      type = UserBasicSqlBuilder.class,
      method = "insert"
  )
  int insert(UserBasic userBasic);

  @Insert("<script>\r\n"
          + "INSERT INTO my_schema.user_basic(id,username,nickname)\r\n"
          + " VALUES\r\n"
          + "<foreach item='entity' collection='list' open='' separator=',' close=''>\r\n"
          + "(#{entity.id},#{entity.username},#{entity.nickname})\r\n"
          + "</foreach>\r\n"
          + "</script>")
  int batchInsert(@Param("list") List<UserBasic> list);

  @DeleteProvider(
      type = UserBasicSqlBuilder.class,
      method = "deleteByPrimaryKey"
  )
  int deleteByPrimaryKey(@Param("id") Long id);

  @UpdateProvider(
      type = UserBasicSqlBuilder.class,
      method = "updateByPrimaryKey"
  )
  int updateByPrimaryKey(UserBasic userBasic);

  @SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectByPrimaryKey"
  )
  UserBasic selectByPrimaryKey(@Param("id") Long id,
                               @Param("retrieveColumns") String[] retrieveColumns);

  @SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectByUsername"
  )
  UserBasic selectByUsername(@Param("username") String username,
                             @Param("retrieveColumns") String[] retrieveColumns);

  @SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectByNickname"
  )
  List<UserBasic> selectByNickname(@Param("nickname") String nickname, @Param("offset") int offset,
                                   @Param("rowCount") int rowCount, @Param("retrieveColumns") String[] retrieveColumns);
}
```

## 为Mapper提供对应语句的SqlProvider

```
public final class UserBasicSqlBuilder {
  public static String insert(UserBasic userBasic) {
    SqlProviderUtils.validate(userBasic);
    Map<String, String> setterCalledFieldToColumnMap = TableEntityUtils.fieldToColumn(userBasic,true);
    SqlProviderUtils.setterCalledFieldValidate(userBasic, setterCalledFieldToColumnMap);
    SQL sql = new SQL();
    sql.INSERT_INTO(UserBasicMetaInfo.SCHEMA_DOT_TABLE);
    SqlProviderUtils.insertColumnsAndValues(sql,setterCalledFieldToColumnMap);
    return sql.toString();
  }

  public static String deleteByPrimaryKey(@Param("id") Long id) {
    SQL sql = new SQL();
    sql.DELETE_FROM(UserBasicMetaInfo.SCHEMA_DOT_TABLE);
    SqlProviderUtils.whereConditions(sql, UserBasicMetaInfo.PRIMARY_KEY);
    return sql.toString();
  }

  public static String updateByPrimaryKey(UserBasic userBasic) {
    SqlProviderUtils.validate(userBasic);
    Map<String, String> setterCalledFieldToColumnMap = TableEntityUtils.fieldToColumn(userBasic,true);
    SqlProviderUtils.setterCalledFieldValidate(userBasic,setterCalledFieldToColumnMap);
    SQL sql = new SQL();
    sql.UPDATE(UserBasicMetaInfo.SCHEMA_DOT_TABLE);
    SqlProviderUtils.exclusiveUpdateColumns(setterCalledFieldToColumnMap, UserBasicMetaInfo.PRIMARY_KEY);
    SqlProviderUtils.updateSetColumnsAndValues(sql,setterCalledFieldToColumnMap);
    SqlProviderUtils.whereConditions(sql, UserBasicMetaInfo.PRIMARY_KEY);
    return sql.toString();
  }

  public static String selectByPrimaryKey(@Param("id") Long id,
      @Param("retrieveColumns") String[] retrieveColumns) {
    SqlProviderUtils.selectColumnsValidate(retrieveColumns);
    SQL sql = new SQL();
    sql.SELECT(retrieveColumns);
    sql.FROM(UserBasicMetaInfo.SCHEMA_DOT_TABLE);
    SqlProviderUtils.whereConditions(sql, UserBasicMetaInfo.PRIMARY_KEY);
    StringBuilder builder = new StringBuilder();
    sql.usingAppender(builder);
    return builder.toString();
  }

  public static String selectByUsername(@Param("username") String username,
      @Param("retrieveColumns") String[] retrieveColumns) {
    SqlProviderUtils.selectColumnsValidate(retrieveColumns);
    SQL sql = new SQL();
    sql.SELECT(retrieveColumns);
    sql.FROM(UserBasicMetaInfo.SCHEMA_DOT_TABLE);
    SqlProviderUtils.whereConditions(sql, "username");
    StringBuilder builder = new StringBuilder();
    sql.usingAppender(builder);
    return builder.toString();
  }

  public static String selectByNickname(@Param("nickname") String nickname,
      @Param("offset") int offset, @Param("rowCount") int rowCount,
      @Param("retrieveColumns") String[] retrieveColumns) {
    SqlProviderUtils.selectColumnsValidate(retrieveColumns);
    SQL sql = new SQL();
    sql.SELECT(retrieveColumns);
    sql.FROM(UserBasicMetaInfo.SCHEMA_DOT_TABLE);
    SqlProviderUtils.whereConditions(sql, "nickname");
    StringBuilder builder = new StringBuilder();
    sql.usingAppender(builder);
    SqlProviderUtils.appendPaging(builder);
    return builder.toString();
  }
}
```

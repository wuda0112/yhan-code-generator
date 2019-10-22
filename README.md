# 简介
Mysql Mybatis代码生成工具。

1. 根据 Mysql create table DDL 生成表对应的实体，Mybatis Mapper , 表名列名常量，Sql Builder
2. 整合 [mybatis-dynamic-sql](https://github.com/mybatis/mybatis-dynamic-sql) ，支持动态Sql
3. 【Mybatis官方插件】自动代码生成的所有功能都支持，除此之外还供了更多的功能，比如 for update 语句，批量插入功能，select语句选择性返回字段等等

# 使用

```
<dependency>
  <groupId>io.github.wuda0112</groupId>
  <artifactId>yhan-code-generator-core</artifactId>
  <version>VERSION</version>
</dependency>

<dependency>
  <groupId>io.github.wuda0112</groupId>
  <artifactId>yhan-code-generator-ddl-parser-mysql</artifactId>
  <version>VERSION</version>
</dependency>

<dependency>
  <groupId>io.github.wuda0112</groupId>
  <artifactId>yhan-code-generator-lang</artifactId>
  <version>VERSION</version>
</dependency>
```

## Create Table DDL 解析

```
String ddl = "CREATE TABLE `my_schema`.`user_basic` (\n" +
                "\t`id` INT(10) UNSIGNED NOT NULL,\n" +
                "\t`username` VARCHAR(50) NULL DEFAULT NULL,\n" +
                "\t`nickname` VARCHAR(50) NULL DEFAULT NULL,\n" +
                "\tPRIMARY KEY (`id`),\n" +
                "\tUNIQUE INDEX `idx_username` (`username`),\n" +
                "\tINDEX `idx_nickname` (`nickname`)\n" +
                ")";

```

```
                
MySqlCreateTableStatementParser parser = new MySqlCreateTableStatementParser();
List<Table> tables = parser.parse(ddl);
```
## 生成实体

```
EntityGenerator entityGenerator = new EntityGenerator();
JavaFile javaFile = entityGenerator.genJavaFile(table, packageName);
```
## 生成表和列的常量

```
TableMetaInfoGenerator generator = new TableMetaInfoGenerator();
JavaFile javaFile = generator.genJavaFile(table, packageName);
```

## 生成Mybatis Mapper

```
MyBatisMapperGenerator myBatisMapperGenerator = new MyBatisMapperGenerator();
JavaFile javaFile = myBatisMapperGenerator.genJavaFile(table, packageName);
```
## 生成Sql Builder
```
SqlBuilderGenerator generator = new SqlBuilderGenerator();
JavaFile javaFile = generator.genJavaFile(table, packageName);
```

## 生成 [mybatis-dynamic-sql](https://github.com/mybatis/mybatis-dynamic-sql) 所需的SqlTable


```
SqlTableGenerator generator = new SqlTableGenerator();
JavaFile javaFile = generator.genJavaFile(table, packageName);
```


# 快速体验
有完整的Test代码

- [GeneratorTestBase.java](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/GeneratorTestBase.java/)　中先指定代码生成后的包名，以及在你电脑上存放的位置，比如放在E://code目录下
- [EntityGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/EntityGeneratorTest.java/)  生成实体类
- [MybatisMapperGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/MybatisMapperGeneratorTest.java/)  生成Mybatis Mapper
- [SqlBuilderGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/SqlBuilderGeneratorTest.java/)  生成SqlBuilder
- [TableMetaInfoGeneratorTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/TableMetaInfoGeneratorTest.java/)  生成表的常量
- [SqlTableGeneratorTest]()  生成mybatis-dynamic-sql所需的SqlTable
- [TableTest](https://github.com/wuda0112/yhan-code-generator/blob/master/yhan-code-generator-core/src/test/java/com/wuda/code/generator/db/mysql/TableTest.java/)  Create Table DDL解析

# 特点
- insert,update语句默认都是操作selective模式
- 支持批量insert,并且能返回自增的主键ID

```
@InsertProvider(
      type = UserBasicSqlBuilder.class,
      method = "batchInsertUseGeneratedKeys"
  )
  @Options(
      keyProperty = "id",
      useGeneratedKeys = true
  )
  int batchInsertUseGeneratedKeys(@Param("list") List<UserBasic> list);
```

- 支持for update 语句

```
@SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectByPrimaryKeyForUpdate"
  )
  UserBasic selectByPrimaryKeyForUpdate(@Param("id") Long id,
      @Param("retrieveColumns") String[] retrieveColumns);
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

- 支持【[mybatis-dynamic-sql](https://github.com/mybatis/mybatis-dynamic-sql)】，提供动态sql能力

```
@SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectOneByExample"
  )
  UserBasic selectOneByExample(
      @Param("whereClauseProvider") WhereClauseProvider whereClauseProvider,
      @Param("retrieveColumns") String[] retrieveColumns);

  @SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "selectListByExample"
  )
  List<UserBasic> selectListByExample(
      @Param("whereClauseProvider") WhereClauseProvider whereClauseProvider,
      @Param("offset") int offset, @Param("rowCount") int rowCount,
      @Param("retrieveColumns") String[] retrieveColumns);

  @SelectProvider(
      type = UserBasicSqlBuilder.class,
      method = "countByExample"
  )
  int countByExample(@Param("whereClauseProvider") WhereClauseProvider whereClauseProvider);
```


- 不使用XML文件，使用Java引用的方式，有错误能更容易发现

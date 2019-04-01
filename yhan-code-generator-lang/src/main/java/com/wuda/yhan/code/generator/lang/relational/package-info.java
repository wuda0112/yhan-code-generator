/**
 * 描述数据库表信息的一些类.
 * 实现方式主要是通过包装其他框架中相似的类,然后再进行一些扩展.
 * 比如{@link com.wuda.yhan.code.generator.lang.relational.Table}就是
 * 包装了{@link io.debezium.relational.Table},然后扩展了索引信息.
 * 使用的<i>DDL</i>解析框架主要是<a href="https://github.com/debezium/debezium">debezium</a>
 * 和<a href="https://github.com/alibaba/druid">druid</a>.
 */
package com.wuda.yhan.code.generator.lang.relational;
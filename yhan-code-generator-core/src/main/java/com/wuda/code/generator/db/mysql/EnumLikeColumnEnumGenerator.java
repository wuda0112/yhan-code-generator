package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;
import com.wuda.code.generator.PairEnumGenerator;
import com.wuda.yhan.code.generator.lang.util.StringUtils;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * 为"类枚举"的Column生成枚举.
 *
 * @author wuda
 */
public class EnumLikeColumnEnumGenerator {

    /**
     * 生成java class文件.
     *
     * @param table       表的基本信息
     * @param packageName 生成的类所属的包
     * @return java file,一个表中可能有多个enum-like的列,因此返回的是list
     */
    public List<JavaFile> genJavaFile(Table table, String packageName) {
        EnumLikeColumnCommentParser commentParser = new EnumLikeColumnCommentParser();
        PairEnumGenerator pairEnumGenerator = new PairEnumGenerator();
        String finalPackageName = PackageNameUtil.getEnumPackageName(packageName, table.id().schema());
        List<Column> columns = table.columns();
        List<JavaFile> list = new ArrayList<>();
        for (Column column : columns) {
            if (commentParser.accept(column.getComment())) {
                Class javaType = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
                List<PairEnumGenerator.EnumElement> enumElements = commentParser.parse(javaType, column.getComment());
                String className = getEnumClassName(table, column);
                JavaFile enumClass = pairEnumGenerator.genJavaFile(finalPackageName, className, enumElements);
                list.add(enumClass);
            }
        }
        return list;
    }

    private String getEnumClassName(Table table, Column column) {
        String className = EntityGeneratorUtil.toFieldName(column.name());
        return EntityGeneratorUtil.toClassName(table.id().table()) + StringUtils.firstCharToUpperCase(className) + "Enum";
    }
}

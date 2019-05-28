package com.wuda.code.generator;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 生成枚举.有两个属性,其中一个作为'code',另外一个当做'description'.
 * 生成的枚举还有一个根据'code'获取枚举元素的方法.举例:
 * <pre>
 *  public enum ExampleEnum {
 *      ONE(1,"我是元素一"),
 *      TWO(2,"我是元素二");
 *
 *      public static ExampleEnum getByCode(Integer code){
 *          // 假设code=1
 *          return ONE;
 *      }
 *  }
 * </pre>
 *
 * @param <C> 生成的枚举中code的类型
 * @param <D> 生成的枚举中description的类型
 */
public class PairEnumGenerator<C, D> {

    private static final String CODE = "code";
    private static final String DESCRIPTION = "description";
    private static final String GET_BY_CODE = "getByCode";

    /**
     * 代表一个枚举元素.
     */
    public static class EnumElement<C, D> implements Map.Entry<C, D> {

        /**
         * 作为枚举元素的名称.
         */
        private String name;
        private C code;
        private D desc;

        public EnumElement(String name, C code, D desc) {
            this.name = name;
            this.code = code;
            this.desc = desc;
        }

        @Override
        public C getKey() {
            return code;
        }

        @Override
        public D getValue() {
            return desc;
        }

        @Override
        public D setValue(D value) {
            D old = desc;
            this.desc = value;
            return old;
        }

        /**
         * 返回元素名称.
         *
         * @return 名称
         */
        public String getName() {
            return name;
        }
    }

    /**
     * 根据给定的参数,生成枚举.
     *
     * @param packageName 包名
     * @param className   　类名
     * @param elements    　枚举中的元素
     * @return 代码生成
     */
    public JavaFile genJavaFile(String packageName, String className, List<PairEnumGenerator.EnumElement> elements) {

        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(className);
        enumBuilder.addModifiers(Modifier.PUBLIC);

        if (elements != null && elements.size() > 0) {
            PairEnumGenerator.EnumElement arbitrary = elements.get(0);
            Class codeClass = arbitrary.code.getClass();
            Class descClass = arbitrary.desc.getClass();

            Iterable<FieldSpec> fieldSpecs = genFieldSpecs(codeClass, descClass);
            enumBuilder.addFields(fieldSpecs);

            enumBuilder.addMethods(genGetter(fieldSpecs));
            enumBuilder.addMethod(genConstructor(codeClass, descClass));
            enumBuilder.addMethod(genGetByCodeMethod(codeClass, packageName, className));

            String codeMark = getJavaPoetMark(codeClass);
            String descMark = getJavaPoetMark(descClass);
            for (PairEnumGenerator.EnumElement element : elements) {
                enumBuilder.addEnumConstant(element.name, TypeSpec.anonymousClassBuilder(codeMark + "," + descMark, element.code, element.desc).build());
            }
        } else {
            enumBuilder.addEnumConstant("UNKNOWN");
        }

        return JavaFile.builder(packageName, enumBuilder.build()).build();
    }

    private String getJavaPoetMark(Class clazz) {
        if (clazz.equals(Integer.class)
                || clazz.equals(Long.class)
                || clazz.equals(Boolean.class)) {
            return "$L";
        } else if (clazz.equals(String.class)) {
            return "$S";
        }
        throw new UnsupportedOperationException("clazz=" + clazz + ",暂时不支持该数据类型");
    }

    private Iterable<FieldSpec> genFieldSpecs(Class codeClass, Class descClass) {
        FieldSpec code = FieldSpec.builder(ClassName.get(codeClass), PairEnumGenerator.CODE, Modifier.PRIVATE).build();
        FieldSpec description = FieldSpec.builder(ClassName.get(descClass), PairEnumGenerator.DESCRIPTION, Modifier.PRIVATE).build();
        return Arrays.asList(code, description);
    }

    private ParameterSpec genCodeParameterSpec(Class codeClass) {
        return ParameterSpec.builder(ClassName.get(codeClass), PairEnumGenerator.CODE).build();
    }

    private ParameterSpec genDescParameterSpec(Class descClass) {
        return ParameterSpec.builder(ClassName.get(descClass), PairEnumGenerator.DESCRIPTION).build();
    }

    private MethodSpec genConstructor(Class codeClass, Class descClass) {
        return MethodSpec.constructorBuilder()
                .addParameter(genCodeParameterSpec(codeClass))
                .addParameter(genDescParameterSpec(descClass))
                .addStatement("this.$L=$L", PairEnumGenerator.CODE, PairEnumGenerator.CODE)
                .addStatement("this.$L=$L", PairEnumGenerator.DESCRIPTION, PairEnumGenerator.DESCRIPTION)
                .build();
    }

    private MethodSpec genGetByCodeMethod(Class codeClass, String packageName, String className) {
        TypeName typeName = ClassName.get(packageName, className);
        ParameterSpec codeParameterSpec = genCodeParameterSpec(codeClass);
        return MethodSpec.methodBuilder(PairEnumGenerator.GET_BY_CODE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(typeName)
                .addParameter(codeParameterSpec)
                .addStatement("$T[] values=$T.values()", typeName, typeName)
                .beginControlFlow("for ($T value : values)", typeName)
                .addStatement("if (value.$L.equals($L)) return value", PairEnumGenerator.CODE, codeParameterSpec.name)
                .endControlFlow()
                .addStatement("return null")
                .build();
    }

    /**
     * 为给定的field生成getter.
     *
     * @param fieldSpecs field
     * @return getter
     */
    private Iterable<MethodSpec> genGetter(Iterable<FieldSpec> fieldSpecs) {
        List<MethodSpec> list = new ArrayList<>();
        for (FieldSpec fieldSpec : fieldSpecs) {
            list.add(MethodSpecUtil.genGetter(fieldSpec));
        }
        return list;
    }
}

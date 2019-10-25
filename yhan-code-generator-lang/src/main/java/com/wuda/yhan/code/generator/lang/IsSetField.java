package com.wuda.yhan.code.generator.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在POJO中,如何判断一个属性是否比外部调用者设值(是否有调用过set方法)呢?
 * 很多是用特殊值,比如<code>null</code>,-1等等,如果等于这些特殊值,就代表没有设置值.
 * 但是这是不严谨的,如果调用者明确的调用过set方法,并且碰巧就是设置了这些特殊值呢?
 * 一种解决方式就是: 为属性添加一个对应的isSet属性,当属性调用set方法时,它对应的isSet属性被设置为true.
 * 比如:
 * <pre>
 *     class A {
 *         String name;
 *         boolean nameIsSet=false;
 *
 *         void setName(String name){
 *             this.name = name;
 *             this.nameIsSet = true;
 *         }
 *     }
 * </pre>
 * 对于属性<i>name</i>,它有一个对应的属性<i>nameIsSet</i>,当调用<i>setName</i>方法时,<i>nameIsSet = true</i>.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IsSetField {

    /**
     * 属性的后缀.
     */
    String suffix = "IsSet";

    /**
     * 关联的field.
     *
     * @return 关联的field
     */
    String referenceField();
}

package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

/**
 * ClassName:UserEntity
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/14 10:39
 * author:ck
 */

/**
 * @Data： 注解在类上，相当于同时使用了@ToString、@EqualsAndHashCode、@Getter、@Setter和@RequiredArgsConstrutor这些注解，对于POJO类十分有用
 */
@Data
/**
 * @NoArgsConstructor, @RequiredArgsConstructor and @AllArgsConstructor：用在类上，自动生成无参构造和使用所有参数的构造函数以及把所有@NonNull属性作为参数的构造函数，
 * 如果指定staticName = “of”参数，同时还会生成一个返回类对象的静态工厂方法，比使用构造函数方便很多
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class UserEntity {

    private String username;

    private String password;

    private String individualUserID;

    private int expiredIn;

}

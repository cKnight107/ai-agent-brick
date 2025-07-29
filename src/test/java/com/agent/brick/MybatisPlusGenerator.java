package com.agent.brick;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.BaseService;
import com.agent.brick.base.mapper.CustomBaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

/**
 * @since 2025/6/15
 *
 * @author cKnight
 */
public class MybatisPlusGenerator {
    public static void main(String[] args) {
        //数据库地址
        String url = "jdbc:postgresql://<YOU_POSTGRE_HOST>/<YOU_POSTGRE_DATABASE>?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true";
        //数据库用户名
        String username = "<YOU_POSTGRE_USER_NAME>";
        //数据库密码
        String password = "<YOU_POSTGRE_PASSWORD>";
        //生成类作者
        String author = "autoCode";
        //项目模块
        String module = "ai-agent-brick";
        //生成类父级包名
        String packageName = "com.agent.brick";
        //需要生成的表名
        String[] tables = {"ai_chat","ai_chat_record","ai_chat_record_msg"};

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author(author) // 设置作者
                            .outputDir(STR."\{System.getProperty("user.dir")}/src/main/java/")// 输出目录
                            .dateType(DateType.ONLY_DATE)
                            .disableOpenDir();//禁止打开输出目录，默认打开
                })
                .packageConfig(builder -> {
                    builder.parent(packageName) // 设置父包名
                            .entity("model") // 设置实体类包名
                            .mapper("mapper") // 设置 Mapper 接口包名
                            .service("service") // 设置 Service 接口包名
                            .serviceImpl("service.impl") // 设置 Service 实现类包名
                            .xml("mapper"); // 设置 Mapper XML 文件包名
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tables) // 设置需要生成的表名

                            .entityBuilder()
                            .enableLombok() // 启用 Lombok
                            .superClass(BaseDO.class)
                            .enableTableFieldAnnotation() // 启用字段注解
                            .disableSerialVersionUID()
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)

                            .mapperBuilder()
                            .superClass(CustomBaseMapper.class)   //设置父类
                            .formatMapperFileName("%sMapper")   //格式化 mapper 文件名称
                            .formatXmlFileName("%sMapper") //格式化 Xml 文件名称
                            .enableBaseColumnList()
                            .enableBaseResultMap()

                            .serviceBuilder()
                            .formatServiceFileName("%sService") //格式化 service 接口文件名称，%s进行匹配表名，如 UserService
                            .formatServiceImplFileName("%sServiceImpl") //格式化 service 实现类文件名称，%s进行匹配表名，如 UserServiceImpl
                            .superServiceImplClass(BaseService.class)

                            .controllerBuilder()
                            .enableRestStyle(); // 启用 REST 风格
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 Freemarker 模板引擎
                .execute(); // 执行生成
    }
}

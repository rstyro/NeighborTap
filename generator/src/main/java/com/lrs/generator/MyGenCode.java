package com.lrs.generator;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.lrs.generator.base.BaseGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MyGenCode extends BaseGenerator {

    public static void main(String[] args) {
        Map<String,Object> param = new HashMap<>();
        param.put("custName","测试注入参数");

        // 代码生成位置
        String outPath = System.getProperty("user.dir")+"/generator/";
        AutoGenerator autoGenerator =  new AutoGenerator(getDataSourceConfig().build());
        //配置相关
        GlobalConfig globalConfig = getGlobalConfig(outPath).build();
        PackageConfig packageConfig = getPackageConfig(outPath).build();
        param.put("basePackage", packageConfig.getParent());
        StrategyConfig strategyConfig = getStrategyConfig(getTables(scannerNext("请输入表名，多个英文逗号分隔？所有输入 all")), outPath).build();
        InjectionConfig injectionConfig = getInjectionConfig(param).build();
        autoGenerator.global(globalConfig);
        autoGenerator.packageInfo(packageConfig);
        autoGenerator.strategy(strategyConfig);
        // 注入配置，可生成自定义 页面、VO 、DTO 等
        autoGenerator.injection(injectionConfig);
        autoGenerator.execute(new FreemarkerTemplateEngine());

    }



}

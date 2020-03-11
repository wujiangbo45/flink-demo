package com.navinfo.opentsp.flink.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * MyBatis的工具类
 *
 */
public class MyBatisUtil {

    private static SqlSessionFactory sqlSessionFactory = null;

    private static final Logger logger = LoggerFactory.getLogger(MyBatisUtil.class);

    /**
     * 初始化Session工厂
     * @throws IOException
     */
    private static void initialFactory() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    /**
     * 获取Session
     * @return
     */
    public static SqlSession getSession() {
        if(sqlSessionFactory == null) {
            try {
                initialFactory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sqlSessionFactory.openSession();
    }



}

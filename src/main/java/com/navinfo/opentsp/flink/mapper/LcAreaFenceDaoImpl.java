package com.navinfo.opentsp.flink.mapper;

import com.navinfo.opentsp.flink.pojo.AreaFenceWhiteList;
import com.navinfo.opentsp.flink.pojo.LcAreaFence;
import com.navinfo.opentsp.flink.utils.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wujiangbo
 * @Create: 2018/04/02 上午9:26
 */
public class LcAreaFenceDaoImpl implements LcAreaFenceMapper {

    @Override
    public List<LcAreaFence> selectAll() {
        SqlSession session = MyBatisUtil.getSession();
        List<LcAreaFence> list = new ArrayList<>();
        try {
            list = session.selectList("com.navinfo.opentsp.flink.mapper.LcAreaFenceMapper.selectAll");
            return list;
        } catch (Exception e){
            e.printStackTrace();
            return list;
        } finally {
            session.commit();
            session.close();
        }
    }

    @Override
    public List<AreaFenceWhiteList> selectWhiteList() {
        SqlSession session = MyBatisUtil.getSession();
        List<AreaFenceWhiteList> list = new ArrayList<>();
        try {
            list = session.selectList("com.navinfo.opentsp.flink.mapper.LcAreaFenceMapper.selectWhiteList");
            return list;
        } catch (Exception e){
            e.printStackTrace();
            return list;
        } finally {
            session.commit();
            session.close();
        }
    }
}

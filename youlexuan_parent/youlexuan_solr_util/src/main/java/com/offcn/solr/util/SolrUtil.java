package com.offcn.solr.util;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring-*.xml")
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void importData(){
        //先从tb_Item表中查需要导入的item列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        System.out.println("====商品列表====");
        for (TbItem item : tbItems) {
            Map<String,String> map = JSON.parseObject(item.getSpec(), Map.class);

            Map<String,String> newMap = new HashMap<>();

            for (String key:map.keySet()){
                newMap.put(Pinyin.toPinyin(key,"").toLowerCase(),map.get(key));
            }

            item.setSpecMap(newMap);
        }

        solrTemplate.saveBeans("core1",tbItems);
        solrTemplate.commit();


        System.out.println("==结束==");
    }


    @Test
    public void delData(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete("core1",query);
        solrTemplate.commit();
    }

}

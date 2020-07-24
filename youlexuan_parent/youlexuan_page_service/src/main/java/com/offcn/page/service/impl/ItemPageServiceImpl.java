package com.offcn.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper tbGoodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper tbItemMapper;


    //传递一个商品id 就是用freemarker根据模板生成一个页面

    @Override
    public boolean createHtml(Long goodsId) {
        System.out.println(goodsId);
        FileWriter out = null;
        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map map = new HashMap();

            //goods信息
            TbGoods goods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            //goods_desc信息
            TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);

            //获取3级分类的名字
            String cat1Name = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String cat2Name = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String cat3Name = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            //对应的sku表
            TbItemExample ex = new TbItemExample();
            TbItemExample.Criteria c = ex.createCriteria();
            c.andGoodsIdEqualTo(goodsId);
            //1表示正常状态
            c.andStatusEqualTo("1");
            //按照是否默认列 ，降序排列，保证默认的sku在列表的第一个
            ex.setOrderByClause("is_default desc");

            List<TbItem> itemList = tbItemMapper.selectByExample(ex);

            map.put("goods",goods);
            map.put("goodsDesc",goodsDesc);
            map.put("itemCat1",cat1Name);
            map.put("itemCat2",cat2Name);
            map.put("itemCat3",cat3Name);

            map.put("itemList",itemList);

            out = new FileWriter(pagedir+goodsId+".html");
            template.process(map,out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean deleteHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

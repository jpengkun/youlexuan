package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout = 100000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map searchMap) {

        //对关键字空格，进行过滤（空格对solr对分词有干扰）
        String keywords = (String) searchMap.get("keywords");
        System.out.println(keywords);
        String newKeywords = keywords.replaceAll(" ","");
        searchMap.put("keywords",newKeywords);

        Map<String,Object> map = new HashMap();

        //高亮查询：需要设置三部分信息
        hiSearch(searchMap, map);
        // 根据关键字进行分类查询
        categoryListSearch(searchMap, map);
        //当页面传递过来分类时 就按照该分类进行品牌.规格获取
        String cat = (String) searchMap.get("category");
        if (!"".equals(cat)){
            brandAndSpecSearch(cat,map);
        }else {
            //否则默认取1个分类对应的品牌.规格获取
            List<String> catList = (List<String>) map.get("categoryList");
            if (catList.size()>0) {
                //但分类有多个时默认取0
                brandAndSpecSearch(catList.get(0), map);
            }
        }

        return map;
    }

    /**
     * 将item表导入solr数据库
     * @param itemList
     */
    @Override
    public void importList(List<TbItem> itemList) {
        for (TbItem item : itemList) {
            Map<String,String> map = JSON.parseObject(item.getSpec(), Map.class);
            Map<String,String> newMap = new HashMap<>();
            for (String key:map.keySet()){
                newMap.put(Pinyin.toPinyin(key,"").toLowerCase(),map.get(key));
            }
            item.setSpecMap(newMap);
        }

        solrTemplate.saveBeans("core1",itemList);
        solrTemplate.commit();
    }

    /**
     * 根据商品id删除solr中的商品信息
     * @param ids
     */
    @Override
    public void deleteList(Long[] ids) {

        Query query = new SimpleQuery();
        Criteria c = new Criteria("item_goodsid");
        c.in(ids);
        query.addCriteria(c);

        solrTemplate.delete("core1",query);
        solrTemplate.commit();
    }

    /**
     * 根据分类名获取对应的品牌，规格列表
     * @param catName
     * @param map
     */
    private void brandAndSpecSearch(String catName, Map<String, Object> map) {

        Long typeId = (Long) redisTemplate.boundHashOps("itemCatList").get(catName);
        //从redis缓存中获取品牌，规格列表
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);

        map.put("brandList",brandList);
        map.put("specList",specList);

    }

    /**
     * 根据关键字进行分类查询
     * @param searchMap
     * @param map
     */
    private void categoryListSearch(Map searchMap, Map<String, Object> map) {

        List<String> catList = new ArrayList<>();

        Query query = new SimpleQuery();
        //关键字查询
        Criteria c = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(c);

        //因为分类有多个，对于sorl而言，可以进行分组去重
        GroupOptions options = new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);

        GroupPage<TbItem> page = solrTemplate.queryForGroupPage("core1",query, TbItem.class);
        //根据分组的列，获取结果
        GroupResult<TbItem> item_category = page.getGroupResult("item_category");

        //得到基于分组后的结果
        Page<GroupEntry<TbItem>> groupEntries = item_category.getGroupEntries();

        for (GroupEntry<TbItem> tbItemGroupEntry : groupEntries.getContent()) {
            catList.add(tbItemGroupEntry.getGroupValue());
        }

        map.put("categoryList",catList);
    }

    /**
     * 关键字 + 高亮查询
     * @param searchMap
     * @param map
     */
    private void hiSearch(Map searchMap, Map<String, Object> map) {

        HighlightQuery query = new SimpleHighlightQuery();

        if (!"".equals(searchMap.get("price"))){
            String[] price = ((String) searchMap.get("price")).split("-");

            if(!price[0].equals("0")){//如果区间起点不等于0
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if(!price[1].equals("*")){//如果区间终点不等于*
                Criteria filterCriteria=new  Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //6.分页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");


        //分页的起始位置
        query.setOffset((pageNo-1)*pageSize);
        //分页的容量
        query.setRows(pageSize);


        //7.排序
        String sortField = (String) searchMap.get("sortField");
        String sortStr = (String) searchMap.get("sort");

        if (!"".equals(sortField)){
            Sort sort=null;
            if ("desc".equals(sortStr)){
               sort = new Sort(Sort.Direction.DESC,sortField);
            }else {
               sort = new Sort(Sort.Direction.ASC,sortField);
            }
            query.addSort(sort);
        }

        // 关键字查询
        // 高亮查询：创建一个高亮查询对象，需要设置如下3部分信息：
        // 高亮的列
        // 高亮的前缀
        // 高亮的后缀
        //1.关键字加高亮
        HighlightOptions options = new HighlightOptions();
        options.addField("item_title");
        options.setSimplePrefix("<span style='color:red'>");
        options.setSimplePostfix("</span>");

        query.setHighlightOptions(options);

        Criteria c = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(c);

        //2.分类的过滤
        String category = (String) searchMap.get("category");
        if (!category.equals("")){
            FilterQuery catFq = new SimpleFacetQuery();
            Criteria catCri = new Criteria("item_category").is(category);
            catFq.addCriteria(catCri);

            query.addFilterQuery(catFq);
        }

        //3.品牌的过滤
        String brand = (String) searchMap.get("brand");
        if (!brand.equals("")){
            FilterQuery brandFq = new SimpleFacetQuery();
            Criteria brandCri = new Criteria("item_brand").is(brand);
            brandFq.addCriteria(brandCri);

            query.addFilterQuery(brandFq);
        }

        //4.规格的过滤
        Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
        if (specMap !=null){

            for (String key : specMap.keySet()) {

                FilterQuery fq = new SimpleFacetQuery();
                Criteria cri = new Criteria("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase()).is(specMap.get(key));
                fq.addCriteria(cri);

                query.addFilterQuery(fq);
            }
        }

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage("core1",query, TbItem.class);
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();

        for (HighlightEntry<TbItem> entry : highlighted) {
            TbItem item = entry.getEntity();
            if(entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());
        //返回总页数
        map.put("totalPages", page.getTotalPages());
        //返回总记录数
        map.put("total", page.getTotalElements());


    }
}

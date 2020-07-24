package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param keywords
     * @return
     */
    public Map<String,Object> search(Map searchMap);


    public  void importList(List<TbItem> itemList);

    /**
     * ids时商品sku的id
     * @param ids
     */
    public void deleteList(Long[] ids);
}

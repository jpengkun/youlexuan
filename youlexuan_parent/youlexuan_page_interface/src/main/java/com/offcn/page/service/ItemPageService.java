package com.offcn.page.service;

public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     */
    public boolean createHtml(Long goodsId);

    /**
     * 删除商品详细页
     * @param goodsIds
     * @return
     */
    public boolean deleteHtml(Long[] goodsIds);



}

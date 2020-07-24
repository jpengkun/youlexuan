package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * goods服务实现层
 * @author senqi
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		// 先插入goods表
		goodsMapper.insert(goods.getGoods());

		// 插入goods_desc表
		// 设置goods_desc和 goods表的关联
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());

		goodsDescMapper.insert(goods.getGoodsDesc());
		//启用规格
		saveItem(goods);
	}

	private void setItem(Goods goods, TbItem item) {
		// image: 默认取spu的第一张
		List<Map> maps = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (maps.size() > 0) {
			item.setImage(maps.get(0).get("url") + "");
		}

		// categoryId: 取spu的第3级分类
		item.setCategoryid(goods.getGoods().getCategory3Id());
		// createTime
		item.setCreateTime(new Date());
		// updateTime
		item.setUpdateTime(new Date());

		// goodsId
		item.setGoodsId(goods.getGoods().getId());

		// sellerId
		item.setSellerId(goods.getGoods().getSellerId());

		// brand： 品牌名字, 通过品牌id查询
		String brandName = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName();
		item.setBrand(brandName);

		// category: 分类名字， 冗余字段
		String catName = itemCatMapper.selectByPrimaryKey(item.getCategoryid()).getName();
		item.setCategory(catName);

		// seller: 商家名字，冗余字段
		String sellerName = sellerMapper.selectByPrimaryKey(item.getSellerId()).getNickName();
		item.setSeller(sellerName);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改goods
		goodsMapper.updateByPrimaryKey(goods.getGoods());

		//修改goods—_desc
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//修改sku表
		//先刪在該
		//查item表
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria criteria = ex.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		List<TbItem> itemsLsit = itemMapper.selectByExample(ex);
		itemMapper.deleteByExample(ex);

		//再插入
		saveItem(goods);


	}

	private void saveItem(Goods goods) {
		//启用规格
		if (goods.getGoods().getIsEnableSpec().equals("1")) {
			// 插入sku列表
			for (TbItem item : goods.getItemList()) {
				// title
				Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
				String title = goods.getGoods().getGoodsName();
				for (String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);

				// image: 默认取spu的第一张
				setItem(goods, item);

				itemMapper.insert(item);
			}
			//不启用，就按照spu的信息插入数据1条
		} else {
			TbItem item = new TbItem();
			//价格
			item.setPrice(goods.getGoods().getPrice());
			//状态
			item.setStatus("1");
			//是否默认
			item.setIsDefault("1");
			//库存数量
			item.setNum(99999);
			item.setSpec("{}");

			//商品KPU+规格描述串作为SKU名称
			item.setTitle(goods.getGoods().getGoodsName());

			setItem(goods, item);

			itemMapper.insert(item);

		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//查goods表
		TbGoods goods = goodsMapper.selectByPrimaryKey(id);

		//查goodsDesc表
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);

		//查item表
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria criteria = ex.createCriteria();
		criteria.andGoodsIdEqualTo(id);

		List<TbItem> itemsLsit = itemMapper.selectByExample(ex);

		return new Goods(goods,goodsDesc,itemsLsit);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//重新定义删除 逻辑删除 屏蔽

			TbGoods Goods = goodsMapper.selectByPrimaryKey(id);
			//1表示删除
			Goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(Goods);


			//将sku的状态改成3
			TbItemExample ex = new TbItemExample();
			TbItemExample.Criteria criteria = ex.createCriteria();
			criteria.andGoodsIdEqualTo(id);

			List<TbItem> itemsLsit = itemMapper.selectByExample(ex);

			for (TbItem item : itemsLsit) {
				//3表示删除
				item.setStatus("3");
				itemMapper.updateByPrimaryKey(item);
			}

		}
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		if(goods != null){			
						if(goods.getSellerId() != null && goods.getSellerId().length() > 0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName() != null && goods.getGoodsName().length() > 0){
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}			if(goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0){
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}			if(goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0){
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}			if(goods.getCaption() != null && goods.getCaption().length() > 0){
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}			if(goods.getSmallPic() != null && goods.getSmallPic().length() > 0){
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}			if(goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0){
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}			if(goods.getIsDelete() != null && goods.getIsDelete().length() > 0){
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}
		}
		//屏蔽已经删除的商品
		criteria.andIsDeleteIsNull();
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
			for(Long id:ids){
				TbGoods goods = goodsMapper.selectByPrimaryKey(id);
				goods.setAuditStatus(status);
				goodsMapper.updateByPrimaryKey(goods);
			}
	}

	@Override
	public List<TbItem> findItemListByGoodsId(Long[] ids, String status) {
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria criteria = ex.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(ex);
	}


}

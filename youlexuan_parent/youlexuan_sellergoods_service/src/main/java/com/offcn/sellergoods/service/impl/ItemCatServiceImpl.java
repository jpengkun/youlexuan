package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.pojo.TbItemCat;
import com.offcn.pojo.TbItemCatExample;
import com.offcn.pojo.TbItemCatExample.Criteria;
import com.offcn.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * item_cat服务实现层
 * @author senqi
 *
 */
@Service(timeout = 10000)
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public String delete(Long[] ids) {
		StringBuilder sb = new StringBuilder("");
		for(Long id:ids){

			//看看id 下面又没有子分类
			List<TbItemCat> sonList = findByParentId(id);

			if(sonList !=null&&sonList.size()>0){
				sb.append(id+",");
			}else {
				//没有子分类，直接删除
				itemCatMapper.deleteByPrimaryKey(id);
			}

		}
		return sb.toString();
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat != null){			
						if(itemCat.getName() != null && itemCat.getName().length() > 0){
				criteria.andNameLike("%" + itemCat.getName() + "%");
			}
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbItemCat> findByParentId(Long parentId) {

		TbItemCatExample ex = new TbItemCatExample();
		Criteria c = ex.createCriteria();
		c.andParentIdEqualTo(parentId);

		//将分类表的数据添加缓存
		//目的：为了在搜索的时候，可以根据分类名快速获取typeID

		List<TbItemCat> tbItemCats = itemCatMapper.selectByExample(null);
		for (TbItemCat tbItemCat : tbItemCats) {
			//以分类名作为小健，一模板id作为值
			redisTemplate.boundHashOps("itemCatList").put(tbItemCat.getName(),tbItemCat.getTypeId());
		}



		return itemCatMapper.selectByExample(ex);
	}

}

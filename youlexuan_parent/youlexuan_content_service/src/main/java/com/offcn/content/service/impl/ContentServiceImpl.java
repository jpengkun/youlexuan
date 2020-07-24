package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * content服务实现层
 * @author senqi
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		System.out.println("从缓存中删除添加的类型数据");

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//数据库还没有被修改的广告
		TbContent conDb = contentMapper.selectByPrimaryKey(content.getId());

		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		System.out.println("从缓存中删除添加的类型数据");

		if (conDb.getCategoryId() != content.getCategoryId()){
			redisTemplate.boundHashOps("content").delete(conDb.getCategoryId());
		}

		contentMapper.updateByPrimaryKey(content);


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先查获取分类广告id
			TbContent con = contentMapper.selectByPrimaryKey(id);

			redisTemplate.boundHashOps("content").delete(con.getCategoryId());
			System.out.println("从缓存中删除添加的类型数据");

			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content != null){			
						if(content.getTitle() != null && content.getTitle().length() > 0){
				criteria.andTitleLike("%" + content.getTitle() + "%");
			}			if(content.getUrl() != null && content.getUrl().length() > 0){
				criteria.andUrlLike("%" + content.getUrl() + "%");
			}			if(content.getPic() != null && content.getPic().length() > 0){
				criteria.andPicLike("%" + content.getPic() + "%");
			}			if(content.getStatus() != null && content.getStatus().length() > 0){
				criteria.andStatusLike("%" + content.getStatus() + "%");
			}
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findContentByCatId(Long catId) {
		//先从缓存中取
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(catId);
		//如果没有在redis取到
		if (contentList == null) {

			TbContentExample ex = new TbContentExample();
			Criteria c = ex.createCriteria();
			c.andCategoryIdEqualTo(catId);

			//1表示启用
			c.andStatusEqualTo("1");
			//添加排序字段。参数表示按照那一列排序 asc升序 desc降序
			ex.setOrderByClause("sort_order asc");

			contentList = contentMapper.selectByExample(ex);
			System.out.println("保存广告列表");

			//存到缓存中
			redisTemplate.boundHashOps("content").put(catId,contentList);

		}else {
			System.out.println("redis有广告列表");
		}
		return contentList;
	}

}

package com.offcn.content.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbContentCategory;

import java.util.List;

/**
 * content_category服务层接口
 * @author jinpengkun
 *
 */
public interface ContentCategoryService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbContentCategory> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbContentCategory contentCategory);


	/**
	 * 修改
	 */
	public void update(TbContentCategory contentCategory);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbContentCategory findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbContentCategory contentCategory, int pageNum, int pageSize);
	
}

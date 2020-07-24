package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * specification服务实现层
 * @author senqi
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {

		//插入规格:注意规格插入后，应该将id的回显
		specificationMapper.insert(specification.getSpecification());

		//插入规格选项
		for (TbSpecificationOption specOption : specification.getSpecificationOptionList()) {
			//设置specId的关联
			specOption.setSpecId(specification.getSpecification().getId());

			specificationOptionMapper.insert(specOption);
		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){

		//修改规格
		specificationMapper.updateByPrimaryKey(specification.getSpecification());

		//修改规格选项
		//先删除
		TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = ex.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());

		specificationOptionMapper.deleteByExample(ex);

		//插入规格选项
		for (TbSpecificationOption specOption : specification.getSpecificationOptionList()) {
			//设置specId的关联
			specOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(specOption);
		}

		//specificationMapper.updateByPrimaryKey(specification);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){

		//根据id查规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

		//根据id查询规格选项：条件查询
		TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = ex.createCriteria();
		criteria.andSpecIdEqualTo(id);

		List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(ex);

		return new Specification(tbSpecification,tbSpecificationOptions);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除规格
			specificationMapper.deleteByPrimaryKey(id);

			//删除规格选项
			TbSpecificationOptionExample ex = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = ex.createCriteria();
			criteria.andSpecIdEqualTo(id);

			specificationOptionMapper.deleteByExample(ex);

		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification != null){			
						if(specification.getSpecName() != null && specification.getSpecName().length() > 0){
				criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
			}
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 列表数据
	 */
	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}

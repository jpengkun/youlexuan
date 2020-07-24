package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;
import com.offcn.sellergoods.service.GoodsService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * goodscontroller
 * @author senqi
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	@Qualifier("importSolrQueue")
	private ActiveMQQueue importSolrQueue;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("delSolrQueue")
	private ActiveMQQueue delSolrQueue;

	@Autowired
	@Qualifier("createPageTopic")
	private ActiveMQTopic createPageTopic;

	@Autowired
	@Qualifier("delPageTopic")
	private ActiveMQTopic delPageTopic;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}


	/**
	 * 更新状态
	 * @param ids
	 * @param status
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			if ("1".equals(status)){
				List<TbItem> itemListByGoodsId = goodsService.findItemListByGoodsId(ids, status);
				if (itemListByGoodsId.size()>0){
					//itemSearchService.importList(itemListByGoodsId);
					jmsTemplate.send(importSolrQueue, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							String jsonStr = JSON.toJSONString(itemListByGoodsId);
							System.out.println("发送了导入solr》improt的消息");
							return session.createTextMessage(jsonStr);
						}
					});
				}
				/*for (Long id: ids) {
					itemPageService.createHtml(id);
				}*/

				jmsTemplate.send(createPageTopic, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});
				System.out.println("send create page message");
			}else {
				//将sorl库中对应的商品删除
				//itemSearchService.deleteList(ids);
				jmsTemplate.send(delSolrQueue, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						System.out.println("发送了导入solr》del的消息");
						return session.createObjectMessage(ids);
					}
				});

				//删除已经生成的详情页面
				jmsTemplate.send(delPageTopic, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});
				System.out.println("send del page message");
			}
			goodsService.updateStatus(ids, status);
			return new Result(true, "审核成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "审核失败");
		}
	}




	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int size){
		return goodsService.findPage(goods, page, size);		
	}
	
}

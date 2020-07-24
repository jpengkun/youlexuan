//seckill_goods服务层
app.service('seckillGoodsService', function($http){


	// 查询单个实体
	this.findSeckillGoods = function() {
		return $http.get('/seckillGoods/findSeckillGoods.do');
	}

	this.findOne = function (id) {
		return $http.get('/seckillGoods/findOne.do?id='+id)
	}

	this.submitOrder = function (skId) {
		return $http.get('/seckillOrder/submitOrder.do?skId='+skId)
	}
});
app.service('cartService', function($http){
	this.findCartList = function() {
		return $http.get('/cart/findCartList.do');
	}

	this.changNum = function (itemId,num) {
		return $http.get("/cart/addCart.do?skuId="+itemId+'&num='+num);
	}
	this.submitOrder = function (order) {
		return $http.post("/order/add.do",order);
	}
});
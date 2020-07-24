//search控制层
app.controller('cartController' ,function($scope,cartService,addrService){

	$scope.findCartList = function () {
		cartService.findCartList().success(function (resp) {
			if (resp){
				$scope.cartList = resp;

				$scope.total = {totalNum:0, totalPrice:0.00 }
				//计算商品的总价格，总价数
				for (var i = 0;i<$scope.cartList.length;i++){

					var cart = $scope.cartList[i];

					for (var j = 0;j<cart.orderItemList.length;j++){
						$scope.total.totalNum += cart.orderItemList[j].num;
						$scope.total.totalPrice +=cart.orderItemList[j].totalFee;
					}
				}
			}
		})
	}
	//改变购物车数量
	$scope.changNum = function (itemId,num) {
		cartService.changNum(itemId,num).success(function (resp) {
			if (resp.success){
				$scope.findCartList();
			}else {
				alert(resp.message)
			}
		})
	}

	//进入结算页面，获取登录人的地址
	$scope.findAddList = function () {
		addrService.findAddList().success(function (resp) {
			if (resp){
				$scope.addrList = resp;
				//进入页面 加载默认的地址
				for (var i = 0;i<$scope.addrList.length;i++){
					if ($scope.addrList[i].isDefault==1){
						$scope.selectAddr = $scope.addrList[i];
						break;
					}
				}
			}
		})
	}
	//选择收货地址
	$scope.selectAddress = function(addr){
		//将选中后的地址传递到selectAddr 提交订单是带走
		$scope.selectAddr = addr;
	}
	$scope.order={paymentType:'1'};

	//选择支付方式
	$scope.selectPayType=function(type){
		$scope.order.paymentType= type;
	}

	//提交订单
	$scope.submitOrder = function () {

		$scope.order.receiverAreaName=$scope.selectAddr.address;//地址
		$scope.order.receiverMobile=$scope.selectAddr.mobile;//手机
		$scope.order.receiver=$scope.selectAddr.contact;//联系人

		cartService.submitOrder($scope.order).success(function (resp) {
			if (resp.success){
				if ($scope.order.paymentType==1){
					location.href = "pay.html";
				}else {
					location.href = "pay-offline.html";
				}
			}else {
				alert(resp.message);
			}

		})
	}
	
	
});


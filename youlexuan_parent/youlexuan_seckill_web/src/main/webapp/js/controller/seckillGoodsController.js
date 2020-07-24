//seckill_goods控制层 
app.controller('seckillGoodsController' ,function($scope,$interval,$location,seckillGoodsService){
	

	// 搜索
	$scope.findSeckillGoods = function(){
		seckillGoodsService.findSeckillGoods().success(function(resp){
			if (resp){
				$scope.list = resp;
			}
		}
		);
	}


	$scope.findOne = function(){
		var id = $location.search()['id'];
		seckillGoodsService.findOne(id).success(function (resp) {
			if (resp){
				$scope.entity = resp;

				var second = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime())/1000);
				$scope.timeStr = convertTimeString(second);
				timer = $interval(function () {
					if (second>0){
						second = second-1;
						$scope.timeStr = convertTimeString(second);
					} else {
						$interval.cancel(timer);
						$("#subBtn").prop("disabled","disabled")
					}
				},1000);
			}
		})
	}


	//转换秒为天小时分钟秒，格式：XXX天 10:22:33
	convertTimeString = function(allsecond){
		var days= Math.floor(allsecond/(60*60*24));//天数
		var hours= Math.floor((allsecond-days*60*60*24)/(60*60));//小时数
		var minutes= Math.floor((allsecond -days*60*60*24 - hours*60*60)/60);//分钟数
		var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
		var timeString="";
		if(days>0){
			timeString=days+"天 ";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
	
	
	$scope.submitOrder = function (skId) {
		seckillGoodsService.submitOrder(skId).success(function (resp) {
			if (resp.success){
				location.href = "pay.html"
			}else {
				alert("当前宝贝太火爆了，请刷新后重试")
			}
		})
	}

});	

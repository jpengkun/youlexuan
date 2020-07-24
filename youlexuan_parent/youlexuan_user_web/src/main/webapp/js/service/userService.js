//user服务层
app.service('userService', function($http){
	// 保存、修改
	this.save = function(entity,code) {
		var methodName = 'add'; 	// 方法名称
		if (entity.id != null) { 	// 如果有ID
			methodName = 'update'; 	// 则执行修改方法
		}
		return $http.post('/user/' + methodName + '.do?code='+code, entity);
	}

	//发送短信验证码
	this.sendCode = function (phone) {
		return $http.get("/user/sendCode.do?phone="+phone);
	}


});
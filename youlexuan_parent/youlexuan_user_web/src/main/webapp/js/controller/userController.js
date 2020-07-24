//user控制层 
app.controller('userController' ,function($scope, userService){
	
	// 保存
	$scope.save = function() {
		userService.save($scope.entity,$scope.code).success(function(response) {
			if (response.success) {
				location.href = "login.html";
			} else {
				alert(response.message);
			}
		});
	}
	//发送短信验证码
	$scope.sendCode = function(){
		userService.sendCode($scope.entity.phone).success(function (resp) {
			if (resp.success){
				if ($scope.passwd!=$scope.entity.password){
					alert("登陆密码与确认密码不一致")
					return;
				}
				var time = 180;

				$("#smsBtn").prop("disabled", "disabled");
				$("#smsBtn").prop("value", time + "秒后重新发送");

				var t = setInterval(function(){
					time--;
					if(time < 1) {
						$("#smsBtn").prop("disabled", "");
						$("#smsBtn").prop("value", "获取短信验证码");
						clearInterval(t);
						return;
					}

					$("#smsBtn").prop("value", time + "秒后重新发送");
				}, "1000");
			}else {
				alert(resp.message)
			}
		})
	}

    
});	

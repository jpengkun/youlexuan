//goods服务层
app.service('uploadService', function($http){
	this.uploadFile = function () {
		//创建一个空表单对象
		var formData =  new FormData();

		formData.append("file",file.files[0]);

		return $http({
			method : 'POST',
			url : "../upload.do",
			data : formData,
			headers : {
				'Content-Type' : undefined
			},
			transformRequest : angular.identity
		});

	}

});
//content服务层
app.service('contentService', function($http){

	// 查询单个实体
	this.findContentByCatId = function(catId) {
		return $http.get('../content/findContentByCatId.do?catId=' + catId);
	}


});
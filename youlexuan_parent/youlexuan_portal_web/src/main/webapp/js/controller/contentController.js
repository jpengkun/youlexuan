//content控制层 
app.controller('contentController' ,function($scope,contentService){

	$scope.findContentByCatId = function (catId) {
		contentService.findContentByCatId(catId).success(function (resp) {
			if (resp){
				$scope.contentList = resp;
			}
		})
		
	}
	$scope.toSearch = function () {
		location.href = "http://localhost:9007/search.html#?keywords="+$scope.keywords;
	}

});	

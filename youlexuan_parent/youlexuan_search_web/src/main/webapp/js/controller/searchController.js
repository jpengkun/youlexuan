//search控制层
app.controller('searchController' ,function($scope, $location,searchService){

	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};//搜索对象

	//添加搜索项
	$scope.addSearchItem=function(key, val){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]=val;
		}else{
			$scope.searchMap.spec[key]=val;
		}
		$scope.search();
	}
	// 构建分页标签(totalPages为总页数)
	buildPageLabel = function() {
		// 新增分页栏属性
		$scope.pageLabel = [];
		// 得到最后页码
		var maxPageNo = $scope.resultMap.totalPages;
		// 开始页码
		var firstPage = 1;
		// 截止页码
		var lastPage = maxPageNo;

		//前面有点
		$scope.firstDot=true;
		//后边有点
		$scope.lastDot=true;


		// 如果总页数大于5页,显示部分页码
		if (maxPageNo > 5) {
			if ($scope.searchMap.pageNo <= 3) {
				// 前5页
				lastPage = 5;
				//前面没点
				$scope.firstDot=false;
				// 如果当前页大于等于最大页码-2
			} else if ($scope.searchMap.pageNo >= maxPageNo - 2) {
				// 后5页
				firstPage = maxPageNo - 4;
				//后边没点
				$scope.lastDot=false;
				// 显示当前页为中心的5页
			} else {
				firstPage = $scope.searchMap.pageNo - 2;
				lastPage = $scope.searchMap.pageNo + 2;
			}
		}else {
			//前面无点
			$scope.firstDot=false;
			//后边没点
			$scope.lastDot=false;
		}
		// 循环产生页码标签
		for (var i = firstPage; i <= lastPage; i++) {
			$scope.pageLabel.push(i);
		}
	}

	//根据页码查询
	$scope.queryByPage=function(pageNo){

		pageNo = parseInt(pageNo);
		//页码验证
		if(pageNo<1 || pageNo > $scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}



	$scope.removeSearch = function(key){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key];
		}
		$scope.search();
	}


	$scope.search=function(){
		searchService.search($scope.searchMap).success(function(response){
			if (response) {
				//后台返回的是一个map,其中有个键rows
				$scope.resultMap = response;

				buildPageLabel();//调用
			}
		});
	}

	//设置排序规则
	$scope.addSort=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	//进入页面加载关键字
	$scope.loadKeywords = function () {
			var k = $location.search()["keywords"];
				$scope.searchMap.keywords = k;
				$scope.search();


	}

});


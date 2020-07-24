//item_cat控制层 
app.controller('itemCatController' ,function($scope, $controller, itemCatService,typeTemplateService){
	
	// 继承
	$controller("baseController", {
		$scope : $scope
	});
	
	// 保存
	$scope.save = function() {
		//将记录的父id赋值给entity
		$scope.entity.parentId = $scope.pId;
		itemCatService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.findByParentId($scope.pId);
			} else {
				alert(response.message);
			}
		});
	}
	
	//查询实体 
	$scope.findOne = function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		itemCatService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.pId)
					$scope.selectIds=[];
				}
				alert(response.message)
			}
		);				
	}
	
	// 定义搜索对象 
	$scope.searchEntity = {};
	// 搜索
	$scope.search = function(page,size){			
		itemCatService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}

	//进入页面 根据父id为0 查询子分类
	$scope.findByParentId = function (parentId) {

		//记录当前分类的级别 作为新增时的父id
		$scope.pId = parentId;

		itemCatService.findByParentId(parentId).success(function (resp) {
			if(resp) {
				$scope.list = resp;
			}
		})
	}

	//进入页面就是第一级
	$scope.grade = 1;
	$scope.setGrade = function (val) {
		$scope.grade = val;
	}

	$scope.selectList = function (pEntity) {
		//记录面包屑导航的变量值

		if ($scope.grade == 1){
			$scope.entity1 = null;
			$scope.entity2 = null;
		}

		if ($scope.grade == 2){
			$scope.entity1 = pEntity;
			$scope.entity2 = null;
		}

		if ($scope.grade == 3){
			$scope.entity2 = pEntity;
		}

		$scope.findByParentId(pEntity.id)
	}

	//分类中关联的模板下拉框
	// $scope.typeTemplateList = {"data":[{"id":1,"text":"手机"},{"id":2,"text":"电脑"}]}
	$scope.findTypeTemplateList = function () {
		typeTemplateService.findTypeTemplateList().success(function (resp) {
			if (resp) {
				$scope.typeTemplateList = {data:resp}
			}
		})
	}


});	

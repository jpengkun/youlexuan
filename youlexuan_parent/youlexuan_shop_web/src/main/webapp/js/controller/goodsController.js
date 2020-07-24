//goods控制层 
app.controller('goodsController' ,function($scope,$location, $controller, goodsService,uploadService,itemCatService,typeTemplateService){
	
	// 继承
	$controller("baseController", {
		$scope : $scope
	});
	
	// 保存
	$scope.save = function() {
		//将附文本编辑器的内容，赋值给entity对应的属性
		$scope.entity.goodsDesc.introduction = editor.html();

		goodsService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.entity = $scope.entity={goods:{isEnableSpec:0},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
				//location.href="goods.html";
				//清空复文本编辑器的内容
				editor.html("");
			} else {
				alert(response.message);
			}
		});
	}
	
	//查询实体 
	$scope.findOne = function(){
		//接受一个从goods.html页面传递过来的id（可能有）
		//根据id是否有值，决定要不要继续执行findOne
		var id = $location.search()["id"];

		if (id==null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);

				//显示图片列表
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);

				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);

				//规格
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);


				//SKU列表的每个对象中的spec属性规格列转换
				for( var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
				}

			}
		);				
	}
	
	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		goodsService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.reloadList();
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	// 定义搜索对象 
	$scope.searchEntity = {};
	// 搜索
	$scope.search = function(page,size){			
		goodsService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}


	$scope.entity={goods:{isEnableSpec:0},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	//保存图片到图片列表
	$scope.saveImage = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	//移除图片列表中的某个图片
	$scope.removeImg = function (idx) {
		$scope.entity.goodsDesc.itemImages.splice(idx,1);
	}
	//文件上传
    $scope.upload = function () {
		uploadService.uploadFile().success(function (resp) {
			if(resp){
				$scope.image_entity.url=resp.message;
			}else {
				alert(resp.message)
			}
		})
	}
	//一级下拉框
	$scope.findItemCat1List = function (pId) {
		itemCatService.findByParentId(pId).success(function (resp) {
			if (resp) {
				$scope.ItemCat1List = resp;
			}
		})

	}


	//监听1一分类，获取2级分类
	$scope.$watch("entity.goods.category1Id",function (newVal,oldVal) {

		if (newVal) {
			//开始获取2级分类
			itemCatService.findByParentId(newVal).success(function (resp) {
				if (resp) {
					$scope.ItemCat2List = resp;
					//当一级分类发生改变 ，清空3级分类
					$scope.ItemCat3List = [];
					//当1级分类发生改变 ，模板id
					$scope.entity.goods.typeTemplateId="";
				}
			});
		}
		
	})


	//监听2一分类，获取3级分类
	$scope.$watch("entity.goods.category2Id",function (newVal,oldVal) {

		if (newVal) {
			//开始获取2级分类
			itemCatService.findByParentId(newVal).success(function (resp) {
				if (resp) {
					$scope.ItemCat3List = resp;
					//当2级分类发生改变 ，模板id
					$scope.entity.goods.typeTemplateId="";
				}
			});
		}

	})


	//监听3一分类，获取模板id
	$scope.$watch("entity.goods.category3Id",function (newVal,oldVal) {
		//根据3级分类的变化，获取模板id
		if (newVal) {
			itemCatService.findOne(newVal).success(function (resp) {
				$scope.entity.goods.typeTemplateId = resp.typeId;
			})
		}

	})

	//监听模板id 获取品牌
	$scope.$watch("entity.goods.typeTemplateId",function (newVal,oldVal) {
		//根据3级分类的变化，获取模板id
		if (newVal) {
			typeTemplateService.findOne(newVal).success(function (resp) {
				if (resp) {
					$scope.typeTemplate = resp;
					//进行json格式的转化
					$scope.typeTemplate.brandIds= JSON.parse($scope.typeTemplate.brandIds);//品牌列表

					if ($location.search()["id"] == null) {
						$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
					}
				}
			})
			//当模板id发生变化的时候，通过模板id获取规格，一级模板选项
			typeTemplateService.findSpecAndOptionList(newVal).success(function (resp) {
				if (resp){
					$scope.specList = resp;
				}
			})

		}else {
			$scope.typeTemplate.brandIds = [];
		}

	})

	//点击规格选项后，跟新记录
	$scope.updateSpecAttribute = function($event,specName,optionName){
		//需要添加的属性：entity.goodsDesc.specificationItems

		var obj = $scope.searchObjByKey($scope.entity.goodsDesc.specificationItems,"attributeName",specName);

		if (obj){
			if ($event.target.checked){
				//勾选
				obj.attributeValue.push(optionName);
			}else {
				//取消勾选
				obj.attributeValue.splice(obj.attributeValue.indexOf(optionName),1)
				if (obj.attributeValue.length == 0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj),1)
				}
			}
		}else {
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":specName,attributeValue:[optionName]});

		}
	}

	//创建SKU列表
	$scope.createItemList=function(){
		//初始值：每次点击规格选项都重新生成，这样不管取消、选中规格选项都可以及时更改列表
		// itemList和后台goods组合属性对应
		$scope.entity.itemList=[{spec:{},price:0,num:999,status:'1',isDefault:'0'}];
		// 避免直接使用太长，找个变量替换一下
		// 格式：[{"attributeName":"网络","attributeValue":["移动3G"]},{"attributeName":"机身内存","attributeValue":["32G","16G"]}]
		var items=  $scope.entity.goodsDesc.specificationItems;
		for(var i=0;i< items.length;i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}
	//添加列值：不使用$scope,表示是该controller中的私有方法，页面不能调用
	addColumn=function(list, columnName, conlumnValues){
		var newList=[];//新的集合
		for(var i=0;i<list.length;i++){
			var oldRow= list[i];
			for(var j=0;j<conlumnValues.length;j++){
				var newRow= JSON.parse( JSON.stringify( oldRow ) );//深克隆
				newRow.spec[columnName]=conlumnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	//商品状态的显示
	$scope.statusName=['未审核','已审核','审核未通过','关闭'];
	//分类名字
	$scope.itemCatName = [];

	//商品列表页面，显示分类的名字
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(function (resp) {
			for(var i = 0;i<resp.length;i++){
				$scope.itemCatName[resp[i].id] = resp[i].name;
			}
		})
	}
	//判断商品时，判断规格选项是否应该被勾选
	$scope.isChecked = function(specName,optionName){

		var obj= $scope.searchObjByKey($scope.entity.goodsDesc.specificationItems,'attributeName',specName);
		if(obj==null){
			return false;
		}else{
			if(obj.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}



	$scope.updateStatus = function(status) {
		goodsService.updateStatus($scope.selectIds, status).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];//清空ID集合
				}else{
					alert(response.message);
				}
			}
		);
	}




});	

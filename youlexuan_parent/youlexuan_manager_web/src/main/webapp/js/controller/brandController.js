app.controller("brandController",function ($scope,$controller,brandService) {
    //继承
    $controller("baseController",{
        $scope:$scope
    });

    $scope.findAll = function () {
        brandService.findAll().success(function (resp) {
            $scope.list = resp;
        });

    }

    //刚进入页面，为防止查询条件为空时，提交null至后台，而查不出结果
    $scope.searchEntity = {};
    $scope.search = function(page, size) {
        //post提交，page、size属性和之前相同，将searchEntity提交至后台@RequestBody对应的属性
        brandService.search(page,size,$scope.searchEntity).success(
            function(response){
                $scope.paginationConf.totalItems = response.total;
                $scope.list = response.rows;
            }
        );
    }
    // 分页方法
    $scope.findPage = function(page, size) {
        brandService.findPage(page,size).success(function (resp) {
            $scope.paginationConf.totalItems = resp.total;
            $scope.list = resp.rows;
        });
    }

    //批量删除
    $scope.dele = function(){

        if(confirm("确认删除吗？"+$scope.selectIds)){
            //获取选中的复选框
            brandService.dele($scope.selectIds).success(function (response) {
                if (response.success){
                    // 基于分页刷新列表
                    $scope.reloadList();
                    //清空选中的id
                    $scope.selectIds =[];
                    //删除之后恢复复选框
                    $("#selall").prop("checked", false);
                }else {
                    alert(response.message);
                }
            })
        }
    }

    //查询实体
    $scope.findOne=function(id){
        brandService.findOne(id).success(
            function(response){
                $scope.entity = response;
            }
        );
    }


    //保存
    $scope.save = function(){
        brandService.save($scope.entity).success(
            function(response){
                // 新增成功
                if(response.success){
                    //重新查询
                    $scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
                }else{
                    alert(response.message);
                }
            }
        );
    }


});
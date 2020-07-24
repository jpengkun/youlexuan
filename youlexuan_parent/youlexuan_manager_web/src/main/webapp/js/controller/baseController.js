//同用的js代码 相当与java中的父类
app.controller("baseController",function ($scope) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        // 总记录：刚进入页面写的10是个假的
        // 随着onChange的触发，最终被替换为 正确的 总记录
        totalItems: 10,

        itemsPerPage: 5,

        perPageOptions: [5, 10, 20, 30],

        // 当 页码发生变化的时候，自动触发，按照自己写的代码，请求后台，获取最新的分页信息
        // 第一次进入页面会立即触发一次： 相当于页面从 0 变为 1
        onChange: function(){
            // $scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
            $scope.reloadList();
            $scope.selectIds = [];
        }

    }

    //刷新列表
    $scope.reloadList = function(){
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }



    $scope.selectIds = [];//选中的Id的集合

    //跟新复选
    $scope.updateSelection = function($event, id){
        // 选中
        if ($event.target.checked){//如果被选中，则增加到数组
            $scope.selectIds.push(id);
        }else {
            // 将id从selectIds数组中移除
            // 第一个参数：要删除元素的索引
            // 第二参数：要删除的个数
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);//删除
        }
    }


    //全选
    $scope.selectAll = function($event){
        var state = $event.target.checked;

        $(".eachbox").each(function (idx,obj) {
            obj.checked = state;
            //通过jquery的方法，获取每个选择框的id值
            var  id = parseInt($(obj).parent().next().text());
            if (state){
                $scope.selectIds.push(id);
            }else {
                var idx = $scope.selectIds.indexOf(id);
                $scope.selectIds.splice(idx,1);//删除
            }
        })
    }


    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    }




});
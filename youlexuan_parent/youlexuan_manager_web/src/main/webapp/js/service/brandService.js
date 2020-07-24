app.service("brandService",function ($http) {
    this.findAll = function () {
        return $http.get("../brand/findAll.do")
    }

    //分页方法
    this.findPage = function (page,size) {
        return $http.get("../brand/findPage.do?page=" + page + "&size=" + size);
    }
    //保存，修改
    this.save = function (entity) {
        var methodName = "add";
        if(entity.id != null) {
            methodName = "update";
        }
        return $http.post("../brand/" + methodName + ".do", entity);
    }

    //根据id获取单个品牌
    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    }

    // 删除
    this.dele = function (selectIds) {
        return $http.get("../brand/delete.do?ids=" + selectIds);
    }

    // 条件查询
    this.search = function (page, size, searchEntity) {
        return $http.post("../brand/search.do?page=" + page + "&size=" + size, searchEntity);
    }
    //下拉列表数据
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do');
    }

})
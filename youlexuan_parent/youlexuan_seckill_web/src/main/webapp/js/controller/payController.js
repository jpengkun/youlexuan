//search控制层
app.controller('payController' ,function($scope,$location,payService){

    $scope.createCode = function () {
        payService.createCode().success(function (resp) {
            if (resp) {
                $scope.out_trade_no = resp.out_trade_no;
                $scope.total_fee = resp.total_fee;
                var qr = new QRious({
                    element:document.getElementById('mycode'),
                    size:250,
                    level:'H',
                    value:resp.qrcode
                });

                queryPayStatus($scope.out_trade_no);
            }
        })
    }

    //检测支付结果的方法
    queryPayStatus = function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(function (resp) {
            if (resp.success){
                location.href="paysuccess.html#?money="+$scope.total_fee;
            }else {
                if(resp.message == '交易超时二维码过期'){
                    location.href = "pay-timeout.html";
                }else{
                    alert(resp.message)
                }
            }
        })
    }

    //进入pausuccess页面 获取支付金额
    $scope.getMoney = function () {
        $scope.money = $location.search()["money"];
    }

});


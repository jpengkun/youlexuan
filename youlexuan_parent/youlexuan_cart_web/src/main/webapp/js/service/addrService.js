app.service('addrService', function($http){
	this.findAddList = function() {
		return $http.get('/address/findByUserId.do');
	}

});
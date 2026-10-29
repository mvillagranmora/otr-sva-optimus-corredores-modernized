angular.module('ModuloPlataforma').controller('MonitorCtrl',function($scope,$timeout,$rootScope,bcsComunes){	
	$scope.CPU=0;
	$scope.WATCHERS=0;
	//wrapper digest
	if ($rootScope.backupDigest === undefined) {
		var scopePrototype = Object.getPrototypeOf($rootScope);
		$rootScope.backupDigest = scopePrototype.$digest;	
		$rootScope.DigestCycleNum = 0;
		$rootScope.DigestCycleTotal = 0;
		$rootScope.DigestCycle=0;
		$rootScope.DigestCycleAvg=0;
		$rootScope.DigestCycleRate=0;
		$rootScope.DigestCycleRatePRC=0;
		$rootScope.DigestCycleTime=(new Date()).getTime();
		scopePrototype.$digest = function $digest() {		  
		  tdigest=(new Date()).getTime();		 
		  $rootScope.backupDigest.apply(this, arguments);
		  $rootScope.DigestCycle=(new Date()).getTime()-tdigest;
		  //rate & avg
		  trated=(new Date()).getTime()-$rootScope.DigestCycleTime;
		  $rootScope.DigestCycleNum++;
		  $rootScope.DigestCycleTotal+= $rootScope.DigestCycle;				  
		  if (trated>1000) {
			$rootScope.DigestCycleRate=$rootScope.DigestCycleNum;
			$rootScope.DigestCycleAvg=Math.round($rootScope.DigestCycleTotal/$rootScope.DigestCycleNum);
			$rootScope.DigestCycleRatePRC=Math.round(100*($rootScope.DigestCycleRate * $rootScope.DigestCycleAvg / 1000));
			//reset
			$rootScope.DigestCycleNum = 0;
			$rootScope.DigestCycleTotal = 0;	
			$rootScope.DigestCycleTime=(new Date()).getTime();	
		  }
		};
	}
	//collect	
	$scope.D=(new Date()).getTime();
	$scope.J=0;
	$scope.BASE=250;
	$scope.STOP=false;
    $scope.calcula=function() {
    	//CPU
    	var l = 1;
	   	$scope.D = (new Date()).getTime() - $scope.D - $scope.BASE;	   	
	   	if ($scope.D<0) $scope.D=0;	   	
	   	$scope.DELAY=$scope.D;
	   	if ($scope.D>750)      $scope.CPU=100;
	   	else if ($scope.D>600) $scope.CPU=75;
	   	else if ($scope.D>500) $scope.CPU=50;
	   	else if ($scope.D>250)  $scope.CPU=25;	
	   	else if ($scope.D>200)  $scope.CPU=15;   
	   	else if ($scope.D>100)  $scope.CPU=10;   		
	   	else if ($scope.D>50)  $scope.CPU=5;   			   	
	    else $scope.CPU=0;
    	//watchers	    
    	$scope.WATCHERS=bcsComunes.getWatchers().length;    	
    	//siguiente ciclo
    	$scope.D=(new Date()).getTime();   
    	if ($scope.STOP==false) $timeout( $scope.calcula ,$scope.BASE); 	 	
    };	
    //Inicial
	$timeout( $scope.calcula ,$scope.BASE);	 
    $scope.$on('$destroy', function() {
    	$scope.STOP=true;
    });	
});


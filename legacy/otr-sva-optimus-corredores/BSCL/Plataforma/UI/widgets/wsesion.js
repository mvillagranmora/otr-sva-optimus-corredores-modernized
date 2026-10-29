'use strict';
angular
  .module('ngBcsComponents')
  .directive('wsesion', function () {
      var controller = function ($scope, $interval, bcsServicioPlataforma) { 
    	  
			//SISTEMA
			$scope.FechaSistemaUTC = '#';
			$scope.FechaSistemaLOCAL = '#';
			$scope.FechaHoraSistemaUTC = '#';
			$scope.FechaHoraSistemaLOCAL = '#';
			//Fecha
			$scope.FechaHoraUTC = '#';
			$scope.FechaHoraLOCAL ='#';					
        	//Fecha PC
        	$scope.FechaPCUTC   = '#'; 
        	$scope.FechaPCLOCAL = '#'; 
    	  
			$scope.successFecha = function(result)
			{
				//SISTEMA
				$scope.FechaSistemaUTC = result.P_OUT_FECHASISTEMAUTC;
				$scope.FechaSistemaLOCAL = result.P_OUT_FECHASISTEMALOCAL;
				$scope.FechaHoraSistemaUTC = result.P_OUT_FECHAHORASISTEMAUTC;
				$scope.FechaHoraSistemaLOCAL = result.P_OUT_FECHAHORASISTEMALOCAL;
				//Fecha
				$scope.FechaHoraUTC = result.P_OUT_FECHAHORAUTC;
				$scope.FechaHoraLOCAL = result.P_OUT_FECHAHORALOCAL;					
	        	//Fecha PC
	        	$scope.FechaPCUTC = moment().utc().format('YYYY-MM-DD HH:mm:ss'); 
	        	$scope.FechaPCLOCAL = moment().local().format('YYYY-MM-DD HH:mm:ss'); 
			};
			$scope.errorFecha = function(result)
			{
				//SISTEMA
				$scope.FechaSistemaUTC = '#';
				$scope.FechaSistemaLOCAL = '#';
				$scope.FechaHoraSistemaUTC = '#';
				$scope.FechaHoraSistemaLOCAL = '#';
				//Fecha
				$scope.FechaHoraUTC = '#';
				$scope.FechaHoraLOCAL ='#';					
	        	//Fecha PC
	        	$scope.FechaPCUTC   = '#'; 
	        	$scope.FechaPCLOCAL = '#'; 
			};
			    	  
	        //Obtiene datos desde la BD
	        $scope.timer = $interval(function(){
	        	//Fecha de Sistema / Local BD
				var datos = {};			
				datos.p_IdEmpresa = $scope.SesionIdEmpresa;       	
	        	bcsServicioPlataforma.Fechas(datos,$scope.successFecha,$scope.errorFecha);	        	
	        }, 60000);
	        //Destruye el contador
	        $scope.$on('$destroy', function() {
	          $interval.cancel($scope.timer);
	        });	
      };
      return {
          restrict: 'EA',
          scope: true,
          controller: controller,
          templateUrl: 'widgets/wsesion.html'
      };
});

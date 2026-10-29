		angular.module('confirmar', ['ui.bootstrap', 'ui.router','ngResource' ,'kendo.directives']);
		angular.module('confirmar').run(function($rootScope) {		
		    $rootScope.safeApply = function(fn) {
		        var phase = $rootScope.$$phase;
		        if (phase === '$apply' || phase === '$digest') {
		            if (fn && (typeof(fn) === 'function')) {
		                fn();
		            }
		        } else {
		            this.$apply(fn);
		        }
		    };		
		});
		angular.module('confirmar').config(['$stateProvider','$urlRouterProvider', function($stateProvider,$urlRouterProvider) {
			$urlRouterProvider.otherwise('/');
			$stateProvider.state('confirmar', {
		        url: '/',
		        templateUrl: 'ConfirmarNotificacionIngreso.html'
		    });				
			
		}]);
		angular.module('confirmar').factory('bcsServicioConfirmar',['$resource',function($resource) {
			var urlValidarConfirmarNotificacion = "/BSCL/Plataforma/XSREST/ValidaConfirmarNotificacion.xsjs";
	 		return $resource("", {},
	 		{
	  			'ValidarConfirmarNotificacion': { method: "POST", url: urlValidarConfirmarNotificacion},      
	   		});
		}]);
		
		
		
		
		angular.module('confirmar').controller('ConfirmarCtrl',function($scope,$window,bcsServicioConfirmar){			
			$scope.alerts = [ ];
			$scope.data = {};
			var token ;
			token = get('token');
		  	$scope.CerrarAlerta = function(index) {
		    	$scope.alerts.splice(index, 1);
		  	};
		  	
		  	function get(name){
		  	   if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
		  	      return decodeURIComponent(name[1]);
		  	};
		  	   
		  	
		  	
		  	
			$scope.ValidarConfirmarNotificacion = function()
			{
				var datos = {};
				var rut = "00001";
				if($scope.p_Rut){
					rut =$scope.p_Rut;
				}
				 
				datos.p_Token = token;
				datos.p_Rut = rut;
				     
					
 				bcsServicioConfirmar.ValidarConfirmarNotificacion(datos,
					function(respuesta){
						console.log(respuesta);
						
						if (respuesta.P_SALIDA==-2) {
							//Existen Problemas
							$window.location.href = "http://"+$window.location.host+"/BSCL/Plataforma/UI/EVT/Confirmar/EventoNoDisponible.html"	
 						}
						if (respuesta.P_SALIDA==-1) {
							//Existen Problemas
							 $scope.alerts.push({msg: respuesta.P_MENSAJESALIDA});	
 						}
						if(respuesta.P_SALIDA==1){
							      
							$window.location.href = "http://"+$window.location.host+"/BSCL/Plataforma//UI/EVT/Confirmar/ConfirmarNotificacionDescripcion.html?token="+datos.p_Token;
							
						}
					},
					function(respuesta){
						 //Error
						 console.log(respuesta.p_MENSAJESALIDA);
					});
				 
			};			
 			
			$scope.ValidarConfirmarNotificacion();
 			
			
			
		});		
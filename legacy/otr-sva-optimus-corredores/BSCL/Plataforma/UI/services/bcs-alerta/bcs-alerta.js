angular.module('ModuloPlataforma').controller('alertasController',function($scope,$modalInstance,mensaje,tipo,OPCIONES_BCSALERTA) {

	$scope.mensaje = mensaje;
	$scope.tipo = tipo;

	$scope.aceptar = function(){
    	$modalInstance.close(OPCIONES_BCSALERTA.ACEPTAR);
    };
    
    $scope.cancelar = function(){
    	$modalInstance.close(OPCIONES_BCSALERTA.CANCELAR);
    };
    
    $scope.si = function(){
    	$modalInstance.close(OPCIONES_BCSALERTA.SI);
    };
    
    $scope.no = function(){
    	$modalInstance.close(OPCIONES_BCSALERTA.NO);
    };

});

angular.module('ModuloPlataforma').factory('bcsAlerta',function($uibModal,bcsComunes) {
	var obj = {};
	
	obj.crear = function(tipo,mensaje,idPadre)
  	{
		
		//MODO NUEVO
		if (idPadre > 0) {                     
			var prom = bcsComunes.ventanaAbrir('VentanaAlerta',{ mensaje : mensaje, tipo : tipo},false,false,idPadre,false,{ title : false });
			return bcsComunes.ventanaPromesa(prom);
		}
		else {
			//MODO ANTIGUO
			return $uibModal.open({
				animation: true,
		      	backdrop: 'static',
		      	windowTopClass : 'toptoptop',
		  		templateUrl: 'services/bcs-alerta/bcs-alerta.html',
		  		controller: 'alertasController',
		  		size: 'md',
		      	resolve: {
		        	mensaje: function() {
		        		return mensaje;
		        	},
		        	tipo: function() {
		        		return tipo;
		        	}
		        }
	      	});
			
			
		}
		
		
	};
	
	return obj;
});
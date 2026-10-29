angular.module('ModuloFFM').directive('bcsFormaPagoMoneda', function(bcsComunes,$resource,$rootScope,$q,bcsConfiguracionFFM,$translate) {
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idVentana : '=?',
			idPortafolio : '=?',
			idTipoOperacion : '=?',
			idMoneda : '=?',
			seleccionado : '=?',
			idSeleccionado : '=?',
			usaCuentaInversion : '=?'
		},
		templateUrl: 'directives/bcs-forma-pago-moneda/bcs-forma-pago-moneda.html', 
    	link: function (scope, element, attrs, fn) {
    		scope.$watchGroup(['idPortafolio','idTipoOperacion','idMoneda'],function(newval,oldval) {
    			scope.seleccionado = undefined;
    			scope.idSeleccionado = '';
    			scope.matrizFormaPago = {};
    			scope.usaCuentaInversion = true;
	    		scope.check = {};
	    		var conf = bcsConfiguracionFFM.getDataSourceFormasPago();
	    		if(newval[0] && newval[1] && scope.idPortafolio && scope.idTipoOperacion && scope.idPortafolio != '-1' && scope.idMoneda){
					var filter = [
							{ "field"   : "IdPortafolio","operator": "eq", "value"   :  scope.idPortafolio},
							{ "field"   : "IdTipoOperacion","operator": "eq", "value"   :  scope.idTipoOperacion},
							{ "field"   : "IdMoneda","operator": "eq", "value"   :  scope.idMoneda}
					];
					console.log(scope.idPortafolio);
					console.log(scope.idTipoOperacion);
					console.log(scope.idMoneda);
					conf.query({filter:filter}).then(function(){
						if (conf.data().length<1)
						{
							console.log("No existen medios de pago");
						}
						else
						{
							var datos = conf.data();
							var formaDefault = false;
							$.each(datos,function(key,value){
								scope.matrizFormaPago[value.IdPreferenciaPagoPortafolio] = value;
							}); 
						}
					});
	    		}
	    			
	    		scope.$watchCollection('check', function (newValue, oldValue){
					if(oldValue && newValue && JSON.stringify(newValue)!=JSON.stringify(oldValue)) {
						$.each(oldValue,function(key,value){
							if(value && scope.check[key]){
								scope.check[key] = false;
								return false;
							}
						});
						$.each(scope.check,function(key,value){
							if(value){
								scope.usaCuentaInversion = false;
								scope.idSeleccionado = scope.matrizFormaPago[key].IdMedioDePago;
								scope.seleccionado = scope.matrizFormaPago[key];
								return false;
							}
						});
					}
				});
	    		
	    		scope.$watch('usaCuentaInversion', function (newValue, oldValue){
					if(newValue) {
						scope.usaCuentaInversion = true;
						$.each(scope.check,function(key,value){
							scope.check[key] = false;
						});
					}else{
						scope.usaCuentaInversion = false;
					}
				});
	    	 });
    	}
	}
});
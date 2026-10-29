angular.module('ModuloOrdenes').directive('barraFiltros', function(bcsComunes,$rootScope) {
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			funcionBuscar : '=?',
			limpiarFiltros : '=?',
			modo : '@?',
			
			instrumentoDefecto : '=?',
			instrumento : '=?',
			
			idTipoInstrumentoDefecto: '=?',
			idTipoInstrumento: '=?',
			tipoInstrumento : '=?',
			
			accionOperacionDefecto : '=?',
			accionOperacion : '=?',
			operacion : '=?',
			
			idClasificacionTipoInstrumentoDefecto : '=?',
			idClasificacionTipoInstrumento : '=?',
			clasificacionTipoInstrumento : '=?',
			
			idLibroOrdenesDefecto : '=?',
			idLibroOrdenes : '=?',
			libroOrdenes : '=?',
			
			idCondicionDeLiquidacionDefecto : '=?',
			idCondicionDeLiquidacion : '=?',
			condicionDeLiquidacion : '=?',
			
			idBolsaDefecto : '=?',
			idBolsa : '=?',
			bolsa : '=?',
			
			ocultaEsFungible : '=?',
			esFungibleDefecto : '=?',
			esFungible : '=?',
			
			esVentaCortaDefecto : '=?',
			esVentaCorta : '=?',
			
			fechaOperacionDefecto: '=?',
			fechaOperacion: '=?',
			
			
			
		},
		templateUrl: '../../Orden/UI/barraFiltro/barraFiltros.html', 
    	link: function (scope, element, attrs, fn) {
    		scope.esFungible = false;
    		if (scope.esFungibleDefecto !== undefined) {
    			scope.esFungibleDeshabilitado = true;
    			scope.esFungible = scope.esFungibleDefecto?true:false;
    		}
    		else {
    			scope.esFungibleDeshabilitado = false;
    		}
    		
    		scope.esVentaCorta = false;
    		if (scope.esVentaCortaDefecto !== undefined) {
    			scope.esVentaCortaDeshabilitado = true;
    			scope.esVentaCorta = scope.esVentaCortaDefecto?true:false;
    		}
    		else {
    			scope.esVentaCortaDeshabilitado = false;
    		}
    		 
    		if (scope.fechaOperacionDefecto) {
    			scope.fechaOperacion = scope.fechaOperacionDefecto;
    			scope.fechaDeshabilitada = true;
    		}
    		else {
    			scope.fechaOperacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    		}
    		
    		scope.filtrosTipoInstrumento = {
    				IdClasificacionTipoInstrumento : {
    					field   : "IdClasificacionTipoInstrumento",
			        	operator: "eq",
			        	value   : ""
				}	
    		};
    		
    		scope.filtrosCondicionDeLiquidacion = {
    				IdTipoOperacion : {
    					field   : "IdTipoOperacion",
			        	operator: "eq",
			        	value   : ""
				}	
    		};
    		
    		scope.filtrosOperacion = {
    				IdTipoInstrumento : {
    					field   : "IdTipoInstrumento",
			        	operator: "eq",
			        	value   : ""
				}	
    		};
    		
    		if (scope.modo === "CONSULTARTRANSACCIONES") {
    			scope.filtrosOperacion.EsTransaccion = {
    					field   : "EsTransaccion",
			        	operator: "eq",
			        	value   : "S"
				};
    		}
    		
    		if (scope.modo === "CONSULTARORDENES") {
    			scope.filtrosOperacion.EsOrden = {
    					field   : "EsOrden",
			        	operator: "eq",
			        	value   : "S"
				};
    		}
    		
    		
    	}
	}
});
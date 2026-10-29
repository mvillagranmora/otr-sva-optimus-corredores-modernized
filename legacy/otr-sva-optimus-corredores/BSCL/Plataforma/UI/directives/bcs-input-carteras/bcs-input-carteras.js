angular.module('ModuloPlataforma').directive('bcsInputCarteras', function(bcsComunes,$resource,$rootScope) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			//salidas
			folioCartera 					: '=?',
			cartera 						: '=?',
			
			//entradas
			idPortafolio 					: '=?',
			idClasificacionTipoInstrumento	: '=?',
			esFungible						: '=?',
			idInstrumento					: '=?',
			instrumento						: '=?',
			tasa							: '=?',
			fechaConsulta					: '=?',
			idEmisor						: '=?',
			idTipoReajuste					: '=?',
			fechaVencimiento				: '=?',
			
			//otros
			activado	 					: '=?',
			idVentana	 					: '=?'
		},
		templateUrl: 'directives/bcs-input-carteras/bcs-input-carteras.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				if (!scope.cartera) return false;
				return scope.cartera.FolioCartera;
			}
		},		
		link: function(scope, element, attrs, fn) {
			scope.pensando = false;
			
			scope.fnBusquedaCarteras = function() {
				
				if (!scope.fechaConsulta) scope.fechaConsulta = util.dateDS($rootScope.SesionFechaSistema,"FechaSistema","CALL");
				
				var parametros = {
						IdPortafolio					: scope.idPortafolio,
						IdInstrumento 					: scope.idInstrumento,
						Instrumento 					: scope.instrumento,
						FechaLiquidacion 				: scope.fechaConsulta,
						IdEmisor 						: scope.idEmisor,
						Tasa 							: scope.tasa,
						IdTipoReajuste 					: scope.idTipoReajuste,
						FechaVencimiento 				: scope.fechaVencimiento,
						EsFungible 						: scope.esFungible,
						IdClasificacionTipoInstrumento 	: scope.idClasificacionTipoInstrumento,
					};

					var id = bcsComunes.ventanaAbrir("SelectorCarteras",parametros,false,false,scope.idVentana);
					bcsComunes.ventanaPromesa(id).then(function(data){
						if (data[0]) {
							scope.cartera = data[0];
							scope.folioCartera = data[0].FolioCartera;
						}
					});
				}
				
			

			
			
			
		}
	};
});

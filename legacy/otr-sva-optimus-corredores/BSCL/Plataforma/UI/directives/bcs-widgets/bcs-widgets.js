angular.module('ModuloPlataforma').directive('bcsWidgetConflictos', function(bcsComunes) {
	return {
		restrict: 'E',
		replace: false,
		scope : {
			
		},
		templateUrl: 'directives/bcs-widgets/bcs-widget-conflictos.html', 
    link: function (scope, iElement, iAttrs, controller) {
    	scope.$watch(bcsComunes.getConflictos,function(newValue, oldValue){
    		scope.conflictos = newValue;
    	});
    	
    }
  };
});

angular.module('ModuloPlataforma').directive('bcsWidgetInstrumento', function($rootScope,bcsComunes,bcsServicioPlataforma) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
		},
		templateUrl: 'directives/bcs-widgets/bcs-widget-instrumento.html',
		link: function(scope, element, attrs, fn) {
			scope.t = {
					otros: false
			};
			scope.buscar = function() {
				
				if (!scope.nemo) return;
				
				scope.nemo = scope.nemo.toUpperCase();
				var datos = {
					p_FiltroBolsa: $rootScope.SesionBolsaDefectoSistema,
					p_FiltroClasificacion: undefined,
					p_FiltroEmisor: undefined,
					p_FiltroEstado: undefined,
					p_FiltroInstrumento: scope.nemo.toUpperCase(),
					p_FiltroMoneda: undefined,
					p_FiltroSubyacente: undefined,
					p_FiltroTipoInstrumento: undefined,
					p_IdEmpresa: $rootScope.SesionIdEmpresa,
					p_MuestraPrecio: "S",
				}
				
				scope.loading = true;
				bcsServicioPlataforma.BolsaInstrumentoPrecioActual(datos).then(function(result){
					var found = false;
					$.each(result.P_OUT_BOLSAINSTRUMENTOPRECIOS,function(key,value) {
						if (value.Nemotecnico === scope.nemo) {
							scope.instrumento = {
								ultimaOfertaCompra : value.MejorPrecioCompra || 0,
								nemotecnico : value.Nemotecnico || '',
								ultimaOfertaVenta : value.MejorPrecioVenta || 0,
								precioMercado : value.UltimoPrecioTransado || 0,
								variacion : value.VariacionDiaria || 0,
								corteMinimo : value.CorteMinimo || 0
							};
							found = true;
							return false;
						}
					});
					fechaUltimaConsulta = new Date();
					scope.loading = false;
					
				},function(result){
					console.log(result);
				});
				
			};
		}
	}
});
angular.module('ModuloPlataforma').directive('bcsWidgetSaldoCtainv', function(bcsComunes,$resource,$rootScope) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
		},
		templateUrl: 'directives/bcs-widgets/bcs-widget-saldo-ctainv.html',
		link: function(scope, element, attrs, fn) {
			
			
			var urlConsultaCtainv = "/BSCL/Plataforma/GWJAVA/PLA/call?t=26";
			var r = $resource("", {},
			    {
					'ConsultaCtainv': { method: "POST", url: urlConsultaCtainv}
				});
			
			scope.filtrosPortafolio = {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
			};
			
			scope.buscar = function(clienteSeleccionado, portafolioSeleccionado) {
				
				if (!portafolioSeleccionado || !portafolioSeleccionado.IdCliente) return;
				
				var datos = {
					p_IdEmpresa: $rootScope.SesionIdEmpresa,
					p_IdUsuario: $rootScope.SesionIdUsuario,
					p_UsaReglasNegocio: 'N',
					p_IdModulo: 'CTA',
					p_Uso 		: 'ORDENES_DE_COMPRA',
					p_FechaDesde : $rootScope.SesionFechaSistema,
					p_FechaHasta : $rootScope.SesionFechaSistema,
					p_PortafolioAConsultar : [{ Identificador : 0, IdPortafolio: portafolioSeleccionado.IdPortafolio, IdMoneda : null}],
					
				};
				
				scope.loading = true;
				r.ConsultaCtainv(datos).$promise.then(function(result){
					scope.cuentas = {};
					scope.docident = clienteSeleccionado.DocumentoIdentidadDefecto;
					scope.portafolio = portafolioSeleccionado.Proposito;
					scope.cliente = clienteSeleccionado.NombreCompleto;
					if (result.P_OUT_FLUJOCAJA["0"] && result.P_OUT_FLUJOCAJA["0"].IdPortafolio)
					{
						$.each(result.P_OUT_FLUJOCAJA,function(key,value) {
							scope.cuentas[key] = {
									IdMoneda : value.IdMoneda,
									Saldo :  value.Saldo
							};
						});
					}
					scope.loading = false;
				},function(result){
					console.log(result);
				});
			};
			
			
			
			
		}
	}
});

angular.module('ModuloPlataforma').directive('bcsWidgetSaldoCustodia', function(bcsComunes,bcsServicioPlataforma, $rootScope) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
		},
		templateUrl: 'directives/bcs-widgets/bcs-widget-saldo-custodia.html',
		link: function(scope, element, attrs, fn) {
			
			scope.filtrosPortafolio = {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
			};
			
			scope.buscar = function(clienteSeleccionado, portafolioSeleccionado,instrumento) {
				
				if (!portafolioSeleccionado || !portafolioSeleccionado.IdCliente || !instrumento.IdInstrumento) return;
				
				var datos = {
					p_IdEmpresa 	: $rootScope.SesionIdEmpresa, 
					p_Uso        	:'CUSTODIA',
					p_IdPais     	: $rootScope.SesionIdPaisSistema,
					p_tblInstrumentos : [{
						IdClasificacionTipoInstrumento : instrumento.IdClasificacionTipoInstrumento,
						IdTipoInstrumento : instrumento.IdTipoInstrumento,
						IdInstrumento : instrumento.IdInstrumento,
						IdEmisor : null,
						FechaVencimiento : null,
						IdTipoReajuste : null,
						EsFungible : null,
						IdBoveda : null,

					}],
					p_FechaSaldo : $rootScope.SesionFechaSistema,
					p_DiasProyeccion : 0,
					p_tblCarteras : [{
						FolioCartera : null,
						FolioGarantia : null,
						FolioSubyacente : null,
						IdCliente : portafolioSeleccionado.IdCliente,
						IdPortafolio : portafolioSeleccionado.IdPortafolio,
					}],
					p_EsAgrupadoPorFolioCartera : 'S',
					p_IdBoveda : undefined,
				}

				scope.loading = true;
				bcsServicioPlataforma.ConsultarFlujoCustodia(datos).then(function(result){
					
					$.each(result.P_OUT_FLUJODETALLADORESUMIDO,function(key,value){
						if (value.TipoFlujo === 'CUSTODIA') {
							scope.cantidad = value.CantidadDisponible
							scope.monto = value.MontoDisponible
						}
					});
					scope.loading = false;
				},function(result){
					console.log(result);
				});
			}
		}
	}
});
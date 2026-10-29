
angular.module('ModuloOrdenes').directive('bcsDetalleCobros', function(bcsConsultasOrdenes,bcsConfiguracionOrdenesV2) {
	return {
		restrict: 'E',
		scope : {
			cobros: '=?',
			idOrden: '=?',
			idAsignacion: '=?'
				
		},
		templateUrl: '../../Orden/UI/templates/bcsDetalleCobros.html',
		link: function(scope, element, attrs, fn) {
			var columns = [
						{field: "CodigoCobroClasificacion",			title: 'UI_TIPO',	width: "80px"},
						{field: "IdCobro",							title: 'UI_CODIGO',	width: "120px"},
						{field: "NombreCobro",						title: 'UI_NOMBRE',	width: "180px"},
						{field: "NombreFormaCobro",					title: 'UI_FORMA',	width: "110px"},
						{field: "ValorCobroAplicado",				title: 'UI_VALOR',	width: "75px", template: "# if (NombreFormaCobro === 'VARIABLE') { # #: (util.bcs_formato(ValorCobroAplicado,'GEN','Tasa',IdMonedaLiquidacionOrden,'','')).valor # # } else { # #: (util.bcs_formato(ValorCobroAplicado,'GEN','Monto', IdMonedaLiquidacionOrden,'','')).valor # # } #"},
						{field: "MontoCobroCalculadoLiquidacion",	title: 'UI_MONTO', width: "75px",template :" #:  (util.bcs_formato(MontoCobroCalculadoLiquidacion,'GEN','Monto', IdMonedaLiquidacionOrden, '','')).valor #"},
						{field: "IdMonedaLiquidacionOrden"	,		title: 'UI_MONEDA',	width: "80px"},
					];
				
			var ds = {};
			if (scope.idOrden) {
				ds = {
					type : "odata",
					definition : {
						serverFiltering:true,
						schema : bcsConfiguracionOrdenesV2.getSchemaCobrosOrden(),
						transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarCobrosOrden'),
							dataType: "json"
							}
						},
						filter: { field: "IdOrden", operator: "eq", value: Number(scope.idOrden) }
					}
				};
				
				scope.configuracionCobros = {
					allowCopy: true,
					autoBind: true,
					selectable: "multiple, row",
					dataSource: new bcs.DataSource(ds),
					columns : columns
				};
			} 
			else if (scope.idAsignacion) {
				ds = {
						type : "odata",
						definition : {
							serverFiltering:true,
							schema : bcsConfiguracionOrdenesV2.getSchemaCobrosAsignacion(),
							transport: {
							read: {
								url: bcsConsultasOrdenes('ConsultarCobrosAsignacion'),
								dataType: "json"
								}
							},
							filter: { field: "IdAsignacion", operator: "eq", value: Number(scope.idAsignacion) }
						}
				};
				scope.configuracionCobros = {
					allowCopy: true,
					autoBind: true,
					selectable: "multiple, row",
					dataSource: new bcs.DataSource(ds),
					columns : columns
				};
			} 
			else {

				var cobros = [];
				if (scope.cobros && scope.cobros.length > 0) {
					for (var i = 0; i < scope.cobros.length ; i++) {
						cobros.push($.extend({},scope.cobros[i]));
					}
					
					ds = {
						type : "array",
						definition : {
							schema : schema,
							data : cobros,
						}
					};

					scope.configuracionCobros = {
						allowCopy: true,
						autoBind: true,
						//height:parametros.simultanea.ordenes.length * 50,
						selectable: "multiple, row",
						dataSource: new bcs.DataSource(ds),
						columns : columns
					};
				}
			}			
		}
			
		
}});
angular.module('ModuloOrdenes').directive('bcsDetalleMovimientosFacturar', function() {
	return {
		restrict: 'E',
		scope : {
			movimientos:"=?"
		},
		templateUrl: '../../Orden/UI/templates/bcsDetalleMovimientosFacturar.html',
}});
angular.module('ModuloOrdenes').directive('bcsDetalleComprobantes', function() {
	return {
		restrict: 'E',
		scope : {
			comprobantes:"=?",
			informacionCliente : "=?"
		},
		templateUrl: '../../Orden/UI/templates/bcsDetalleComprobantes.html',
}});
angular.module('ModuloOrdenes').directive('bcsDetalleOrdenes', function() {
	return {
		restrict: 'E',
		scope : {
			ordenes:"=?",
			informacionCliente : "=?"
		},
		templateUrl: '../../Orden/UI/templates/bcsDetalleOrdenes.html',
}});
angular.module('ModuloOrdenes').directive('bcsDetalleAsignaciones', function() {
	return {
		restrict: 'E',
		scope : {
			asignaciones:"=?",
			informacionCliente : "=?"
		},
		templateUrl: '../../Orden/UI/templates/bcsDetalleAsignaciones.html',
}});

angular.module('ModuloOrdenes').directive('bcsResumenOrden', function() {
	return {
		restrict: 'E',
		scope : {
			ngChangePorcentaje			: '&?',
			ngChangeMonto				: '&?',
			cantidad 					: '=?',
			montoOperacion			 	: '=?',
			montoOperacionLiquidacion 	: '=?',
			totalCobros 				: '=?',
			porcentajeCobros 			: '=?',
			porcentajeCobrosUltimaOrden : '=?',
			totalOrden 					: '=?',
			idMonedaLiquidacion 		: '=?',
			idMonedaOperacion 			: '=?',
			montoAllin                  : '=?',
			valorAllin 					: '=?',
			usaAllin 					: '=?',
			usaAllinMonto				: '=?'
		},
		templateUrl: '../../Orden/UI/templates/bcsResumenOrden.html',
		link: function(scope, element, attrs, fn) {
			
		}
}});

angular.module('ModuloOrdenes').directive('bcsExigibilidad', function($rootScope,bcsServicioPlataforma,bcsServicioOrden) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			operaciones		: '=?',
			moneda			: '=?'
		},
		templateUrl: '../../Orden/UI/templates/bcsExigibilidad.html',
		link: function(scope, element, attrs, fn) {
				
			var limpiar = function() {
				scope.exigibilidadSimulacionEntidad = 0;
				scope.exigibilidadSimulacionCorredora = 0;	

				scope.exigibilidadPortafolioEntidad = 0;
				scope.exigibilidadPortafolioCorredora = 0;

				scope.exigibilidadTotalEntidad = 0;
				scope.exigibilidadTotalCorredora = 0;
			};
			
			//scope.IdMonedaSistema = $rootScope.SesionIdMonedaSistema;
			scope.operaciones = scope.operaciones || [];
			
			scope.$watch('moneda',function(newval,oldval){
				scope.IdMonedaSistema = scope.moneda?scope.moneda:$rootScope.SesionIdMonedaSistema;
			},true);
			
			scope.$watch('operaciones',function(newval,oldval){

				var operaciones = [];
				var data = {};
				
				if (scope.operaciones.length === 0) {
					limpiar();
					//return;
				}

				for (var i = 0;i<scope.operaciones.length;i++) {
					if (i === 0) {
						data.EsVentaCorta 		= scope.operaciones[i].EsVentaCorta;
						data.IdTipoInstrumento 	= scope.operaciones[i].IdTipoInstrumento;
						data.IdBolsa 			= scope.operaciones[i].IdBolsa;
						data.IdPortafolio		= scope.operaciones[i].IdPortafolio;
						data.IdInstrumento		= scope.operaciones[i].IdInstrumentoExige;
						data.SesionLocale		= scope.operaciones[i].SesionLocale;
					}
					if (scope.operaciones[i].Monto === 0) continue;
					operaciones.push( {
						IdInterno		: i,
						IdTipoInstrumento :scope.operaciones[i].EsDerivado?'DER':scope.operaciones[i].IdTipoInstrumento,
						IdInstrumento : scope.operaciones[i].IdInstrumento,
						Cantidad : scope.operaciones[i].Cantidad, 
						Precio : scope.operaciones[i].Precio, 
						Monto: scope.operaciones[i].Monto, 
						IdBolsa: scope.operaciones[i].IdBolsa,
					});
				}
				
				go(data,operaciones);
			},true);


			var go = function(data,operaciones) {
				
				var datosExig = {
						p_IdEmpresa: $rootScope.SesionIdEmpresa,
						p_IdUsuario: $rootScope.SesionIdUsuario,
						p_IdModulo: 'ORD',
						p_simularOperacion: 'N',
						p_UsaReglasNegocio: 'N',
						p_TableExigibilidadSimulacion:[],
						p_EsVentaCorta : data.EsVentaCorta?'S':'N',
						p_IdTipoInstrumento : data.IdTipoInstrumento,
						p_IdPortafolio : data.IdPortafolio,
						p_IdBolsa : data.IdBolsa,
						p_IdInstrumento : data.IdInstrumento,
						p_IdMoneda	: scope.IdMonedaSistema,
				};

				datosExig.p_TableExigibilidadSimulacion = operaciones;
				
				scope.loading = true;
				var post = bcsServicioOrden.CalcularExigibilidadOrdenes(datosExig);
				
				post.$promise.then(function(result){
					scope.loading = false;
					var exigSimEntidad = 0;
					var exigSimCorredora = 0;

					$.each(result.p_out_TableExigibilidadSimulacion,function(key,value) {
						exigSimEntidad+=Number(value.MontoExigibilidadEntidad);
						exigSimCorredora+=Number(value.MontoExigibilidadCorredora);
					});

					scope.exigibilidadSimulacionEntidad = exigSimEntidad;
					scope.exigibilidadSimulacionCorredora = exigSimCorredora;	
					
					if (result && result.p_out_TableExigibilidadPortafolio && result.p_out_TableExigibilidadPortafolio[0]) {
						scope.exigibilidadPortafolioEntidad = Number(result.p_out_TableExigibilidadPortafolio[0].MontoExigibilidadEntidad); 
						scope.exigibilidadPortafolioCorredora = Number(result.p_out_TableExigibilidadPortafolio[0].MontoExigibilidadCorredora);
					}
					scope.CustodiaPeruReporteOculto = false;
					if(data.SesionLocale != undefined){
						if (data.SesionLocale === 'PE' && (data.IdTipoInstrumento === 'RRV' || data.IdTipoInstrumento === 'RRF')){//Reporte
							scope.exigibilidadPortafolioEntidad = 0;
							scope.exigibilidadPortafolioCorredora = 0;
							scope.CustodiaPeruReporteOculto = true;
						}	
					}
					scope.exigibilidadTotalEntidad = scope.exigibilidadSimulacionEntidad + scope.exigibilidadPortafolioEntidad;
					scope.exigibilidadTotalCorredora = scope.exigibilidadSimulacionCorredora + scope.exigibilidadPortafolioCorredora;

				},function(result){
					scope.loading = false;
				});
			};
			
		}
	}
});

angular.module('ModuloOrdenes').directive('bcsCobertura', function($rootScope,bcsServicioPlataforma,bcsServicioOrden) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			operaciones : '=?',
			moneda : '=?'
		},
		templateUrl: '../../Orden/UI/templates/bcsCobertura.html',
		link: function(scope, element, attrs, fn) {
			
			scope.operaciones = scope.operaciones || [];

			var limpiar = function() {
				scope.coberturaSimulacionEntidad = 0;
				scope.coberturaSimulacionCorredora = 0;	
			
				scope.coberturaPortafolioEntidad = 0;
				scope.coberturaPortafolioCorredora = 0;
				
				scope.coberturaTotalEntidad = 0;
				scope.coberturaTotalPortafolio = 0;
			};
			
			limpiar();
			//scope.IdMonedaSistema = $rootScope.SesionIdMonedaSistema;
			
			scope.$watch('moneda',function(newval,oldval){
				scope.IdMonedaSistema = scope.moneda?scope.moneda:$rootScope.SesionIdMonedaSistema;
			},true);
			scope.$watch('operaciones',function(newval,oldval){
				var operaciones = [];
				var data = {};

				if (scope.operaciones.length === 0) {
					limpiar();
					//return;
				}

				for (var i = 0;i<scope.operaciones.length;i++) {
					if (i === 0) {
						data.IdPortafolio = scope.operaciones[i].IdPortafolio;
						data.IdBolsa = scope.operaciones[i].IdBolsa;
						data.IdInstrumento = scope.operaciones[i].EsVentaCorta ? 0 : scope.operaciones[i].IdInstrumentoExige;
						data.EsVentaCorta = scope.operaciones[i].EsVentaCorta;
						data.IdTipoInstrumento = scope.operaciones[i].IdTipoInstrumentoExige;
						data.SesionLocale = scope.operaciones[i].SesionLocale;
					}
					
					if (scope.operaciones[i].Monto === 0) continue;
					operaciones.push( {
						IdReferencial		: i,
						IdInstrumento: scope.operaciones[i].EsVentaCorta ? scope.operaciones[i].IdInstrumentoExige : scope.operaciones[i].IdInstrumento,
						Cantidad : 0, 
						PrecioMercado : scope.operaciones[i].Precio,
						Monto: scope.operaciones[i].Monto,
						MonedaDestino : scope.operaciones[i].MonedaDestino, 
					});
				}
				
				go(data,operaciones);
			},true);

			var go = function(data,operaciones) {
				
				var datosCobertura = {
						p_IdEmpresa: $rootScope.SesionIdEmpresa,
    					p_IdUsuario: $rootScope.SesionIdUsuario,
    					p_IdModulo: 'ORD',
    					p_UsaReglasNegocio: 'S',
    					
    					p_IdPortafolio: data.IdPortafolio,
    					p_IdBolsa: data.IdBolsa,
    					p_IdInstrumento: data.IdInstrumento,
    					p_IdTipoInstrumento : data.IdTipoInstrumento,
    					p_EsVentaCorta: data.EsVentaCorta? 'S':'N',
    					p_IdMoneda	: scope.IdMonedaSistema,
    					p_Simulacion: operaciones || []
    					    						
				};
				
				scope.loading =true;
				var p = bcsServicioOrden.CalcularCoberturaOrdenes(datosCobertura);
				p.$promise.then(function(result){
					scope.loading=false;
					
					if (result.p_out_ErroresContador> 0) {
						limpiar();
						return;
					}

					var coberturaSimEntidad = 0;
					var coberturaSimCorredora = 0;
					
					$.each(result.p_out_TableCoberturaSimulacion,function(key,value) {
						coberturaSimEntidad+=Number(value.MontoCoberturaEntidad);
						coberturaSimCorredora+=Number(value.MontoCoberturaCorredora);
					});
					
					scope.coberturaSimulacionEntidad = coberturaSimEntidad;
					scope.coberturaSimulacionCorredora = coberturaSimCorredora;	
					
					if (result.p_out_TableCoberturaPortafolio[0]) {
						scope.coberturaPortafolioEntidad = Number(result.p_out_TableCoberturaPortafolio[0].MontoCoberturaEntidad); 
						scope.coberturaPortafolioCorredora = Number(result.p_out_TableCoberturaPortafolio[0].MontoCoberturaCorredora);
					}
					
					scope.CustodiaPeruReporteOculto = false;
					if(data.SesionLocale != undefined){
						if (data.SesionLocale === 'PE' && (data.IdTipoInstrumento === 'RRV' || data.IdTipoInstrumento === 'RRF')){//Reporte
							scope.coberturaPortafolioEntidad = 0;
							scope.coberturaPortafolioCorredora = 0;
							scope.CustodiaPeruReporteOculto = true;
						}	
					}
					
					scope.coberturaTotalEntidad = scope.coberturaSimulacionEntidad + scope.coberturaPortafolioEntidad;
					scope.coberturaTotalPortafolio = scope.coberturaSimulacionCorredora + scope.coberturaPortafolioCorredora;
					
				},function(result){
					scope.loading = false;
				});
			};
			
		}
	}
});


angular.module('ModuloOrdenes').directive('bcsLineaRiesgo', function($rootScope,bcsServicioOrden) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			operaciones 					: '=?',
				
		},
		templateUrl: '../../Orden/UI/templates/bcsLineaRiesgo.html',
		link: function(scope, element, attrs, fn) {
			
			var limpiar = function() {
				scope.lineaClienteSimulacion = 0;
				scope.lineaClienteCarteras = 0;
				scope.lineaClienteDisponible = 0;

				scope.lineaAgenteSimulacion = 0;
				scope.lineaAgenteCarteras = 0;
				scope.lineaAgenteDisponible = 0;

				scope.lineaCorredoraSimulacion = 0;
				scope.lineaCorredoraCarteras = 0;
				scope.lineaCorredoraDisponible = 0;
			}
			
			limpiar();
			scope.operaciones = scope.operaciones || [];
			scope.IdMonedaSistema = $rootScope.SesionIdMonedaSistema;
			
			scope.$watch('operaciones',function(newval,oldval){
				var operaciones = [];

				for (var i = 0; i<scope.operaciones.length ; i++) {
					var t= {
						IdInterno			: i,
						IdPortafolio		: scope.operaciones[i].IdPortafolio,
						IdTipoOperacion		: scope.operaciones[i].IdTipoOperacion,
						EsVentaCorta		: scope.operaciones[i].EsVentaCorta? 'S':'N',
						EsODCuentaPropia	: scope.operaciones[i].EsODCuentaPropia? 'S':'N',
						IdInstrumento		: scope.operaciones[i].IdInstrumento,
						Monto 				: scope.operaciones[i].Monto,
						Tasa				: scope.operaciones[i].Tasa,
						PlazoEnDias			: scope.operaciones[i].PlazoEnDias,
						IdBolsa				: scope.operaciones[i].IdBolsa,
						IdTipoCanalIngreso	: $rootScope.SesionTipoCanalIngreso
					};
					operaciones.push(t);
				}
				go(operaciones);
			},true);
			
			
				
			var go = function(operaciones) {

				
				if (operaciones.length === 0) return;
				var datos = {
						p_IdEmpresa				: $rootScope.SesionIdEmpresa,
    					p_IdUsuario				: $rootScope.SesionIdUsuario,
    					p_IdModulo				: 'ORD',
    					p_tablaOperacion		: operaciones,					
 				};
 				scope.loading = true;
				var p = bcsServicioOrden.CalcularLineaRiesgoOrdenes(datos).$promise;
				p.then(function(result){
					scope.loading = false;

					limpiar();

					if (!result.P_OUT_LIMITES || result.P_OUT_LIMITES.length === 0) return;
					for (var i = 0;i<result.P_OUT_LIMITES.length ; i++) {
						if (result.P_OUT_LIMITES[i].EsCuentaPropia === 'S') {
							scope.lineaCorredoraSimulacion = result.P_OUT_LIMITES[i].MontoSimulacion;
							scope.lineaCorredoraDisponible = result.P_OUT_LIMITES[i].MontoDisponible;
							if (scope.lineaCorredoraDisponible <= 0) scope.lineaCorredoraDisponible = 0;
							scope.lineaCorredoraCarteras = result.P_OUT_LIMITES[i].MontoCarteras;
						}
						else {
							scope.lineaClienteSimulacion = result.P_OUT_LIMITES[i].MontoSimulacion;
							scope.lineaClienteDisponible= result.P_OUT_LIMITES[i].MontoDisponible;
							if (scope.lineaClienteDisponible <= 0) scope.lineaClienteDisponible = 0;
							scope.lineaClienteCarteras = result.P_OUT_LIMITES[i].MontoCarteras;


							scope.lineaAgenteSimulacion = "-";
							scope.lineaAgenteCarteras = "-";
							scope.lineaAgenteDisponible = "-";
							
						}
					}
					scope.loading = false;
				});
			};
			
			
		}
	}
});


angular.module('ModuloOrdenes').directive('kendoReverseGrid', function(bcsServicioOrden,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			fnDobleClick 	: '=?',
			api 			: '=?',
			seleccionado 	: '=?',
			configuracion 	: '=?',
			
			
		},
		template: '<div id="kendo-pew"></div>',
		link: function(scope, element, attrs, fn) {
			
			var t = bcsConfiguracionOrdenesV2.getConfiguracionConsultarOrdenes().options;
			t.dataBound = function() {
				scope.apiGrid = $("kendo-pew").data("kendoGrid");
			};
			delete t.columns;
			delete t.dataSource;

			t.dataSource =  new kendo.data.DataSource({
					type : "odata",
					serverFiltering: true,
					transport: {
						read: {
							url:bcsConsultasOrdenes('ConsultarOrdenesUI'),
							dataType: "json"
						}
					},
					schema : bcsConfiguracionOrdenesV2.getSchemaOrden()
				});
			t.columns = bcsConfiguracionOrdenesV2.getConfiguracionConsultarOrdenes().options.columns;
			var ff = $("#kendo-pew").kendoGrid(t);
			scope.api = $("#kendo-pew").data("kendoGrid");
			




		}		
}});

angular.module('ModuloOrdenes').directive('bcsDetalleReportantes', function(bcsConsultasOrdenes) {
	return {
		
		restrict: 'E',
		scope : {
			
			idOrden: '=?'
			
				
		},
		
		templateUrl: '../../Orden/UI/templates/bcsDetalleReportantes.html',
		link: function(scope, element, attrs, fn) {
			var columns = [
						{field: "IdOrdenAsociada",					title: 'UI_SECUENCIA',	width: "80px"},
						{field: "NombreCompletoReportante",			title: 'UI_CLIENTE',	width: "80px"},
						{field: "CodigoPortafolioReportante",		title: 'UI_PORTAFOLIO',	width: "80px"},
						{field: "TasaReportante",					title: 'UI_TASA',		width: "80px"},
						{field: "CantidadAsociada",					title: 'UI_CANTIDAD',	width: "80px",template : " <label> #:  (util.bcs_formato(CantidadAsociada,'ORD','Cantidad',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
						{field: "MontoAsociado",					title: 'UI_MONTO',		width: "80px",template : " <label> #:  (util.bcs_formato(MontoAsociado,'ORD','Monto',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
						{field: "IdMonedaOrdenReportante",			title: 'UI_MONEDA',		width: "80px"},
						
					];
			
			var schema = {
				model : {
					fields : {
						
						IdOrdenAsociada				: {type: "number"},
						Secuencia					: {type: "string"},
						NombreCompletoReportante	: {type: "string"},
						CodigoPortafolioReportante	: {type: "string"},
						TasaReportante	  			: {type: "number"},
						CantidadAsociada			: {type: "number"},
						MontoAsociado				: {type: "number"},
						IdMonedaOrdenReportante	  	: {type: "string"},
						IdTipoInstrumento			: {type: "string"}
						
					}
				}
			};
		
			var ds ={
					type : "odata",
					definition : {
							serverFiltering:true,
							schema : schema,
							transport: {
							read: {
								url: bcsConsultasOrdenes('ConsultarOrdenAsociada'),
								dataType: "json"
								}
							},
							filter: { field: "IdOrdenReportado", operator: "eq", value: Number(scope.idOrden) }
						}
					};
			
			scope.configuracionReportantes = {
					allowCopy: true,
					autoBind: true,
					selectable: "multiple, row",
					dataSource: new bcs.DataSource(ds),
					columns : columns
				};
				
			
					
		}
			
		
}});

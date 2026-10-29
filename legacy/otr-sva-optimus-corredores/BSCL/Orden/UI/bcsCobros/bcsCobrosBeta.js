angular.module('ModuloOrdenes').directive('bcsCobrosBeta', function(bcsComunes,$resource,$rootScope,bcsServicioOrden,$translate,bcsConsultasOrdenes) {
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idVentana : '=?',
			
			//portafolio : '=?',
			//canal : '=?',
			//bolsa : '=?',
			//tipoProducto : '=?',
			//tipoOperacion : '=?',
			//diasPlazo : '=?',
			//tasa : '=?',
			//tipoCambio : '=?',
			//modo : '=?',      //U (una operacion) o M (muchas operaciones)
			calculando : '=?',
			//corredorCorresponsal : '=?',
			
			//entrada para modo U
			//montoInicial : '=?',
			//monedaLiquidacionOperacion : '=?',
			//monedaLiquidacionCobros : '=?',
			//cantidad : '=?',
			//precio : '=?',
			//instrumento : '=?',
			//salida para modo u
			//montoOperacionCalculado: '=?',
			//montoLiquidacionCalculado: '=?',
			//cantidadCalculada : '=?',
			//totalCobros : '=?',
			//totalOrden : '=?',
			

			cabecera 	: '=?',
			operaciones : '=?', // montoNetoMonedaOperacion,monedaOperacion,monedaLiquidacionOperacion,monedaLiquidacionCobros, cantidad,precio,corte
			
			//salida para modo m
			operacionesSalida : '=?', //montoNetoMonedaOperacion,montoNetoMonedaLocal montoNetoMonedaLiquidacion cantidadCalculada
			
			//SALIDA para ambos modos
			matrizCobros					: '=?matrizCobros',				//matriz de cobros
			matrizCobrosModificadosInterno	: '=?matrizCobrosModificados',	//vector de cobros modificados	(dinamico)
				
			//SOLO LECTURA
			soloLectura : '=?',
			matrizSoloLectura : '=?', //LA QUE VUELVE DEL SP DE CREACION
			
			
		},
		templateUrl: '../../Orden/UI/bcsCobros/bcsCobrosBeta.html', 
    	link: function (scope, element, attrs, fn) {
    					
			var ConsultarMatrizOperacionalPortafolios = $resource("", {},
		    {
				'go': { method: "POST", url: bcsConsultasOrdenes('ConsultarCobrosPortafolios')}
			});
			
			var cleanVars = function() {
				scope.matrizCobros = [];
				scope.matrizCobrosModificados = {};
				scope.matrizCobrosModificadosInterno = [];
				scope.matrizMontos = {};
				scope.montoNetoMonedaLiquidacionCalculado = 0;
				scope.montoCobrosMonedaLiquidacionCalculado = 0;
				scope.montoTotal = 0;
			};
			
			//organizacion
			//matrizCobros-matrizCobrosModificados-matrizMontos
			
			scope.cargado = false;
			scope.matrizCobros = [];
			scope.matrizCobrosModificados = {};
			scope.matrizCobrosModificadosInterno = [];
			scope.matrizMontos = {};
			
			scope.operacionesSalida = [];
			
			//CARGA INICIAL DE LOS COBROS
    		scope.$watch('cabecera',function(newval,oldval) {
    			
    			if (scope.cabecera === undefined) {
    				cleanVars();
    				return;
    			}
    			
    			if (_.isMatch(oldval, newval)) {
    				return;
    			}
    			
    			var datos = {
    					p_IdEmpresa: $rootScope.SesionIdEmpresa,
						p_IdUsuario: $rootScope.SesionIdUsuario,
						p_IdModulo: 'ORD',

	    				p_Portafolios : [{
	    					IdInterno 				: 0,
	    					IdPortafolio 			: scope.cabecera.IdPortafolio,
	    					IdTipoProducto 			: scope.cabecera.IdTipoProducto,
	    					IdTipoOperacion 		: scope.cabecera.IdTipoOperacion,
	    					IdCanalIngreso 			: scope.cabecera.IdCanalIngreso,
	    					IdBolsa 				: scope.cabecera.IdBolsa,
	    					IdCorredorCorresponsal	: scope.cabecera.IdCorredorCorresponsal || null
	    				}]
    			};
    			
    			scope.calculando = true;
    			var call = ConsultarMatrizOperacionalPortafolios.go(datos);
    			call.$promise.then(function(result){
    				
    				cleanVars();
					
					scope.calculando = false;
    				scope.cargado = true;
					if (!result.P_OUT_MATRIZOPERACIONAL[0]){
    					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_PORTAFOLIO_SIN_COBROS_CONFIGURADOS"));
    					return;	
    				}
					$.each(result.P_OUT_MATRIZOPERACIONAL,function(key,value) {
						value.Decimales = util.bcs_formato(0,'ORD','Monto',value.IdMoneda,'','').cantidadDecimales;
						value.DecimalesPorcentaje = util.bcs_formato(0,'ORD','Tasa',value.IdMoneda,'','').cantidadDecimales;
						scope.matrizCobros.push(value);
						scope.matrizCobrosModificados[value.IdCobro] = Number(value.ValorCobro);
						scope.matrizMontos[value.IdCobro] = {};
						scope.matrizMontos[value.IdCobro]["MontoCobroMonedaLocal"] = 0;
						scope.matrizMontos[value.IdCobro]["MontoCobroMonedaOrden"] = 0;
						scope.matrizMontos[value.IdCobro]["MontoCobroMonedaLiquidacion"] = 0;
						
					});
					actualizaCobros();
					
    			},function(result){
    				scope.calculando = false;
    			});
    			
    		},true);
			
    		scope.$watch('operaciones',function(newval,oldval){
    			if (!scope.cabecera)
    				return;
    			if (!scope.operaciones || scope.operaciones.length === 0) {
    				limpiarResultados();
    				return;
    			}
    			
    			actualizaCobros();
    			
    			
    		},true)
    		
    		scope.$watch('matrizCobrosModificados',function(newval,oldval) {
    			actualizaCobros();
    		},true);
    		
    		var limpiarResultados = function() {
    			scope.operacionesSalida = [];
				$.each(scope.matrizMontos,function(key,val){
					//val = {};
					val["IdMonedaCobro"] = '';
					val["MontoCobroMonedaLocal"] = 0;
					val["MontoCobroMonedaOrden"] = 0;
					val["MontoCobroMonedaLiquidacion"] = 0;
				});
    		};
    		
    		var actualizaCobros = function() {
				
				if (scope.operaciones && scope.operaciones.length === 0) {
					return;
				}

				$.each(scope.operaciones,function(key,value) {
					if (!value.PrecioOrden) {
						return;
					}
				});

    			var i = 0;
    			var datos = {	
					p_IdEmpresa: $rootScope.SesionIdEmpresa,
					p_IdUsuario: $rootScope.SesionIdUsuario,
					p_IdModulo: 'ORD',
										
					p_Ordenes : [],
					p_CobrosModificados : []
				};
    			
    			$.each(scope.operaciones,function(key,value) {
    				var orden = {
						IdInterno  						: key,
						IdPortafolio 					: scope.cabecera.IdPortafolio,
						IdTipoProducto  				: scope.cabecera.IdTipoProducto,
						IdTipoOperacion					: scope.cabecera.IdTipoOperacion,
						IdTipoCanalIngreso				: scope.cabecera.IdCanalIngreso,
						IdBolsa							: scope.cabecera.IdBolsa,
						IdCorredorCorresponsal			: scope.cabecera.IdCorredorCorresponsal || null,
						IdInstrumento  					: value.IdInstrumento || 0,
						DiasPlazo 						: value.DiasPlazo || 0,
						CantidadOrden   				: value.CantidadOrden || 0,
						MontoOrden						: value.MontoOrden || 0,	//para compra simultanea
						ValorRescateOrden				: value.ValorRescateOrden || 0,
						EsFungible  					: value.EsFungible || 'N',
						TasaSimultanea  				: value.TasaSimultanea || 0,
						TasaRFIF  						: 0,
						TasaVentaCorta  				: 0,
						IdMonedaLiquidacionOrden        : value.IdMonedaLiquidacionOrden,
						PrecioOrden 					: value.PrecioOrden || 0,
						TipoCambio 						: value.TipoCambio || null,
						MontoTotal						: value.MontoTotal || 0,
						EsPorMonto 						: value.MontoTotal > 0 && !value.CantidadOrden? 'S' : 'N',
					};
					datos.p_Ordenes.push(orden);
    			})

    			var cobrosModificados = [];
				$.each(scope.matrizCobros,function(key,value) {
					if (scope.matrizCobrosModificados[value.IdCobro] != scope.matrizCobros[key].ValorCobro && scope.matrizCobrosModificados[value.IdCobro] >= 0){
						var cobro = scope.matrizCobrosModificados[value.IdCobro];
						/*if (scope.matrizCobrosModificados[value.IdCobro] > scope.matrizCobros[key].ValorCobro){
							bcsComunes.msg("error","Error",$translate.instant("UI_COBRO_MAYOR_AL_DEFINIDO"));
							cobro = Number(scope.matrizCobros[key].ValorCobro);
							scope.matrizCobrosModificados[value.IdCobro] = cobro;
						}*/
						$.each(datos.p_Ordenes,function(key,val){
							cobrosModificados.push( { IdInterno: val.IdInterno ,IdCobro: value.IdCobro, ValorCobroAplicado: cobro } );
						})
					}
						
				});
    			
				datos.p_CobrosModificados = cobrosModificados;
    			
    			
    			scope.matrizCobrosModificadosInterno = cobrosModificados;

    			scope.calculando = true;
				var p = bcsServicioOrden.CalcularMontoYCobrosOrden(datos);

    			p.$promise.then(function(result) {
    				limpiarResultados();
    				if (result.p_out_Ordenes && result.p_out_Ordenes[0]) {
						$.each(result.p_out_Cobros,function(key,value) {
							scope.matrizMontos[value.IdCobro]["IdMonedaLiquidacionOrden"] = result.p_out_Ordenes[0].IdMonedaLiquidacionOrden;
							scope.matrizMontos[value.IdCobro]["IdMonedaCobro"] = value.IdMonedaCobro;
							scope.matrizMontos[value.IdCobro]["MontoCobroMonedaLocal"] += Number(value.MontoCobroCalculadoLocal);
							scope.matrizMontos[value.IdCobro]["MontoCobroMonedaOrden"] += Number(value.MontoCobroCalculado);
							scope.matrizMontos[value.IdCobro]["MontoCobroMonedaLiquidacion"] += Number(value.MontoCobroCalculadoLiquidacion);

						});
								
						scope.operacionesSalida = [{
							montoOperacionCalculado		: Number(result.p_out_Ordenes[0].MontoOperacion),
							montoLocalCalculado			: Number(result.p_out_Ordenes[0].MontoOperacionLocal),
							montoLiquidacionCalculado	: Number(result.p_out_Ordenes[0].MontoOperacionLiquidacion),
							totalCobros					: Number(result.p_out_Ordenes[0].TotalCobrosLiquidacion),
							totalOrden					: Number(result.p_out_Ordenes[0].MontoTotalLiquidacion),
							cantidadCalculada			: Number(result.p_out_Ordenes[0].CantidadOperacion)
						}]
						
    				}

    				scope.calculando = false;
				},function(result){
					console.log(result);
					bcsComunes.msg("error","UI_ERROR","UI_ERROR_CALCULAR_COBROS_OPERACION");
					scope.calculando = false;
				});
    		}    		
    	}
	}
});
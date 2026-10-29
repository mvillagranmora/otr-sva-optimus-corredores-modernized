angular.module('ModuloOrdenes').directive('bcsCobrosOrden', function(bcsComunes,$resource,$rootScope,bcsServicioOrden,$translate,bcsConsultasOrdenes,$q) {
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idVentana : '=?',
			calculando : '=?',
			operaciones : '=?',
			muestraColumnaContadoPlazo	: '=?',
			operacionesSalida	: '=?',
			deferred			: '=?',
			
			matrizCobros					: '=?matrizCobros',				//matriz de cobros
			matrizCobrosModificados			: '=?matrizCobrosModificados',	//vector de cobros modificados	(dinamico)
			
			
			//funciones
			api					: '=?',
			soloApi				: '=?',
			
			//SOLO LECTURA
			soloLectura 		: '=?',
			usaAllin			: '=?',
			valorAllin			: '=?',
			montoAllin			: '=?',
			matrizSoloLectura 	: '=?', //LA QUE VUELVE DEL SP DE CREACION
			
			
		},
		templateUrl: '../../Orden/UI/bcsCobros/bcsCobrosOrden.html', 
    	link: function (scope, element, attrs, fn) {
    		
    		scope.SesionLocale = $rootScope.SesionLocale;
    		scope.llaveActual = "";
    		if (scope.muestraColumnaContadoPlazo === undefined) {
    			scope.muestraColumnaContadoPlazo = "N";
    		}
			var ConsultarMatrizOperacionalPortafolios = $resource("", {},
		    {
				'go': { method: "POST", url: bcsConsultasOrdenes('ConsultarCobrosPortafolios')}
			});
			
			//organizacion
			//matrizCobros-matrizCobrosModificados-matrizMontos
			
			scope.cargado = false;
			scope.matrizCobros = [];
			if (scope.matrizCobrosModificados === undefined) {
				scope.matrizCobrosModificados = {};	
			}
			scope.matrizMontos = {};
			
			scope.operacionesSalida = [];
			if (scope.api === undefined) {
				scope.api = {};	
			}
			
			scope.api.calcularAllIn = function(valorAllin) {
				
				$.each(scope.matrizCobros,function(key,value) {
					
					if (value.Otros_CheckAllIn === "S") {
						if (scope.matrizCobrosModificados[value.IdCobro]  !== undefined && scope.matrizCobrosModificados[value.IdCobro] !== valorAllin) {
							scope.matrizCobrosModificados[value.IdCobro] = valorAllin;
						}
					}
					
				});
			}
			
			scope.api.calcularCobros = function(operaciones,calculaPorAllin) {
				
				if (operaciones && operaciones.length > 0) {
					scope.operaciones = operaciones;
				}
				
				if (!scope.operaciones || scope.operaciones.length === 0) {
    				limpiarResultados();
    				return;
    			}
    			if (calculaPorAllin === undefined) {
    				actualizaCobros(true);	
    			}
    			else {
    				actualizaCobros(calculaPorAllin);
    			}
    			
			}
			
			if (!scope.soloApi) {
				scope.$watch('operaciones',function(newval,oldval){
	    			if (!scope.operaciones || scope.operaciones.length === 0) {
	    				limpiarResultados();
	    				return;
	    			}
	    			
	    			actualizaCobros(true);
	    			
	    			
	    		},true);
				scope.$watch('matrizCobrosModificados',function(newval,oldval) {
	    			if (!scope.operaciones || scope.operaciones.length === 0) {
	    				limpiarResultados();
	    				return;
	    			}
	    		},true);
			}
			
			scope.actualizaCobroModificado = function() {
				actualizaCobros(false);
			}
			    		
    		
    		
    		var limpiarResultados = function() {
    			scope.operacionesSalida = [];
				scope.matrizCobros = [];
				scope.matrizCobrosModificados = {};
				scope.matrizMontos = {};
    		};
    		
    		var actualizaCobros = function(calculaPorAllin) {
				
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
						CantidadOrden   				: value.CantidadOrden || 0,
						DiasPlazo 						: value.DiasPlazo || 0,
						EsFungible  					: value.EsFungible || 'N',
						IdBolsa							: value.IdBolsa,
						IdCorredorCorresponsal			: value.IdCorredorCorresponsal || null,
						IdInterno  						: key,
						IdInstrumento  					: value.IdInstrumento || 0,
						IdInstrumentoSubyacente			: value.IdInstrumentoSubyacente || 0,
						IdMonedaLiquidacionOrden        : value.IdMonedaLiquidacionOrden,
						IdPortafolio 					: value.IdPortafolio,
						IdTipoProducto  				: value.IdTipoProducto,
						IdTipoOperacion					: value.IdTipoOperacion,
						IdTipoCanalIngreso				: value.IdCanalIngreso,
						FechaOperacion					: value.FechaOperacion,
						MontoOrden						: value.MontoOrden || 0,	//para compra simultanea
						PrecioOrden 					: value.PrecioOrden || 0,
						TasaSimultanea  				: value.TasaSimultanea || 0,
						TasaRFIF  						: 0,
						TasaVentaCorta  				: 0,
						TipoCambio 						: value.TipoCambio || null,
						ValorRescateOrden				: value.ValorRescateOrden || 0,
						PlazoSimultanea					: value.PlazoSimultanea||0,
						PlazoVentaCorta					: value.PlazoVentaCorta||0,
						PlazoIF							: value.PlazoIF||0,
						IdEmisor						: value.IdEmisor || '',
						EsAPlazo						: value.EsAPlazo || 'N',
						IdMonedaOrden					: value.IdMonedaOrden,
						MontoTotal						: value.MontoTotal || 0,
						EsPorMonto 						: value.MontoTotal > 0 && !value.CantidadOrden? 'S' : 'N',
						TasaReporte						: value.TasaReporte || 0,
						TasaReportado					: value.TasaReportado || 0,
						PlazoReporte					: value.PlazoReporte || 0,
						BaseTasa						: value.BaseTasa || 0,
						TipoInteres						: value.TipoInteres,
						
						PorcentajeAllIn					: value.ValorAllIn,
						MontoAllIn						: value.MontoAllIn,
						ValorAllIn						: value.ValorAllIn
					};
					datos.p_Ordenes.push(orden);
    			})

				if (calculaPorAllin === false) {
					datos.p_Ordenes[0].PorcentajeAllIn = undefined;
					datos.p_Ordenes[0].MontoAllIn = undefined;
				}
	
				var nuevaLlave = scope.operaciones[0].IdPortafolio + "" +(scope.operaciones[0].IdCorredorCorresponsal || "")  + scope.operaciones[0].IdTipoOperacion + 
											scope.operaciones[0].IdBolsa + scope.operaciones[0].IdTipoProducto + scope.operaciones[0].IdCanalIngreso;
				
				var cobrosModificados = [];
				
				if (calculaPorAllin === false) {
					$.each(datos.p_Ordenes,function(key1,val1){
						$.each(scope.matrizCobrosModificados,function(key2,value2) {
							cobrosModificados.push( { IdInterno: val1.IdInterno ,IdCobro: key2, ValorCobroAplicado: value2 } );
						});
					});
				}
				datos.p_CobrosModificados = cobrosModificados;
				
    			scope.calculando = true;
				var p = bcsServicioOrden.CalcularMontoYCobrosOrden(datos);
				
    			p.$promise.then(function(result) {
    				scope.calculando = false;
    				var contador = 0;
    				console.log(result);
    				
    				if (result.p_out_Cobros && scope.matrizCobros.length > 0) {
    					$.each(result.p_out_Cobros,function(key,cobros) {
    						$.each(scope.matrizCobros,function(key,cobrosMatriz) {
    							if(cobrosMatriz.IdCobro === cobros.IdCobro){
    								contador++;
    							}
        					});
    					});
    				}
    				
    				//clean vars
    				if (scope.llaveActual === "" || scope.llaveActual != nuevaLlave) {
    					scope.matrizCobros = [];
						scope.matrizCobrosModificados = {};
						scope.matrizMontos = {};
					} else if (contador != scope.matrizCobros.length) {
						scope.matrizCobros = [];
						scope.matrizCobrosModificados = {};
						scope.matrizMontos = {};
					}
					
					scope.llaveActual = scope.operaciones[0].IdPortafolio + "" + (scope.operaciones[0].IdCorredorCorresponsal || "") + scope.operaciones[0].IdTipoOperacion + 
											scope.operaciones[0].IdBolsa + scope.operaciones[0].IdTipoProducto + scope.operaciones[0].IdCanalIngreso;
    				
    				//limpiarResultados();

					if (result.P_OUT_ERRORESCONTADOR > 0){
    					bcsComunes.msg("error","UI_ERROR",result.P_OUT_MENSAJES[0].descripcion);
    					return;	
    				}

    				if (result.p_out_Cobros && result.p_out_Cobros.length === 0){
    					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_PORTAFOLIO_SIN_COBROS_CONFIGURADOS"));
    					scope.cargado = false;
    					return;	
    				}

    				if (result.p_out_Ordenes && result.p_out_Ordenes[0]) {
						
    					var allIn = false;
    					var valorAllIn = undefined;
    					var montoAllIn = undefined;
						$.each(result.p_out_Cobros,function(key,value) {
							value.Decimales = util.bcs_formato(0,'GEN','Monto',value.IdMonedaCobro,'','').cantidadDecimales;
							value.DecimalesPorcentaje = util.bcs_formato(0,'GEN','Tasa',value.IdMonedaCobro,'','').cantidadDecimales;
							
							if (!scope.matrizMontos[value.IdCobro]) {
								scope.matrizMontos[value.IdCobro] = {};
								scope.matrizCobros.push(value);
							}
							
							scope.matrizMontos[value.IdCobro]["IdMonedaLiquidacionOrden"] = result.p_out_Ordenes[0].IdMonedaLiquidacionOrden;
							scope.matrizMontos[value.IdCobro]["IdMonedaCobro"] = value.IdMonedaCobro;
							scope.matrizMontos[value.IdCobro]["MontoCobroMonedaLocal"] = Number(value.MontoCobroCalculadoLocal);
							scope.matrizMontos[value.IdCobro]["MontoCobroMonedaOrden"] = Number(value.MontoCobroCalculado);
							scope.matrizMontos[value.IdCobro]["MontoCobroMonedaLiquidacion"] = Number(value.MontoCobroCalculadoLiquidacion);
							//PARA QUE NO VUELVA A ENTRAR EN EL WATCH
							if (scope.matrizCobrosModificados[value.IdCobro] === undefined) {
								scope.matrizCobrosModificados[value.IdCobro] = Number(value.ValorCobroAplicado);
							}
							
							//PARA MONTO MINIMO
							if (Number(value.MontoMinimo) !== 0 && Number(value.MontoCobroCalculado) < Number(value.MontoMinimo)) {
								bcsComunes.msg("warning","warning",$translate.instant("UI_APLICA_MONTO_MINIMO").replace("XXNOMBRECOBROXX",value.NombreCobro)); 
							}
							scope.matrizCobrosModificados[value.IdCobro] = Number(value.ValorCobroFinal);
							
							scope.matrizMontos[value.IdCobro]["ValorCobroFinal"] = value.ValorCobroFinal;
							scope.matrizMontos[value.IdCobro]["ValorCobroPerfil"] = value.ValorCobroPerfil;
							
							//ALL IN PERU
							if (value.Otros_CheckAllIn === "S") {
								allIn = true;
								valorAllIn = value.ValorCobroAplicado;
								montoAllIn = result.p_out_Ordenes[0].MontoAllIn;
							}
							if (value.MontoFijoPorUnidad_AllInCheckValor === "S") {
								allIn = true;
								valorAllIn = result.p_out_Ordenes[0].PorcentajeAllIn;
								montoAllIn = result.p_out_Ordenes[0].MontoAllIn;
							}
							
						});
						
						//ALL IN PERU
						scope.usaAllin = allIn;
						if (scope.usaAllin) {
							scope.valorAllin = valorAllIn;
							scope.montoAllin = montoAllIn;
						}

						scope.operacionesSalida = [{
							idMonedaLiquidacionOrden	: result.p_out_Ordenes[0].IdMonedaLiquidacionOrden,
							montoOperacionCalculado		: Number(result.p_out_Ordenes[0].MontoOperacion),
							montoLocalCalculado			: Number(result.p_out_Ordenes[0].MontoOperacionLocal),
							montoLiquidacionCalculado	: Number(result.p_out_Ordenes[0].MontoOperacionLiquidacion),
							totalCobros					: Number(result.p_out_Ordenes[0].TotalCobrosLiquidacion),
							totalCobrosLocal			: Number(result.p_out_Ordenes[0].TotalCobrosLocal),
							totalOrden					: Number(result.p_out_Ordenes[0].MontoTotalLiquidacion),
							porcentajeCobros			: result.p_out_Ordenes[0].MontoOperacionLiquidacion !== 0?100*result.p_out_Ordenes[0].TotalCobrosLiquidacion/result.p_out_Ordenes[0].MontoOperacionLiquidacion:0,
							porcentajeCobrosUltimaOrden	: Number(result.p_out_Ordenes[0].PorcentajeCobrosUltimaOrden),
							cantidadCalculada			: Number(result.p_out_Ordenes[0].CantidadOperacion)
						}]
						
    				}
    				

				},function(result){
					console.log(result);
					bcsComunes.msg("error","UI_ERROR","UI_ERROR_CALCULAR_COBROS_OPERACION");
					scope.calculando = false;
				});
    			
    		}
    		
    		if (scope.deferred) {
    			scope.deferred.resolve(scope.api);	
    		}
    		
    	}
	}
});
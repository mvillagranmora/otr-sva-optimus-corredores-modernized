angular.module('ModuloPlataforma').directive('bcsInputInstrumentos', function(bcsComunes,$resource,$rootScope,$translate,$q,$timeout,bcsServicioOrden,bcsServicioPlataforma) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			muestraPrecio : '=?',
			consultaPrecio: '=?',
			datosInstrumento : '=?',
			buscarInstrumento : '=?',
			buscarIdInstrumento : '=?',
			
			filtroTipoInstrumento: '=?',
			filtroTipoInstrumentoEspecifico: '=?',
			filtroMercadoLocal: '=?',
			filtroSoloVigentes: '=?',
			filtroReporte: '=?',
			filtroEstado: '=?', //  "'INA'", "'ACT'","['ACT']", "['ACT','INA']", default ACT 
			filtroInstrumentoNegociacion: '=?', // Valor "S" para filtrar y traer aquellos instrumentos cuyo IdInstrumentoRaiz es != 0
			filtroClasificacion: '=?',
			filtroEmisor: '=?',
			filtroMoneda: '=?',
			kNgDisabled : '=?',
			
			filtroBolsa: '=?', 					//PARA PRECIOS
			filtroCondicionDeLiquidacion: '=?', //PARA PRECIOS Y PUNTAS
			
			creaOpcion : '=?', //en S, crea la opcion de forma automatica si no la encuentra (Opciones Peru)
			modo : '@',					//Flujo Custodia
			filtroFlujoCustodia : '@',	//Flujo Custodia	
			
			//informacion para traer solo los instrumentos que tiene el portafolio en custodia (ingreso de orden)
			validaFlujoCustodia: '=?',
			idBoveda: '=?',
			idTipoOperacion: '=?',
			idPortafolio: '=?',
			
			idUso: '=?',//Usos del criterio seleccion de tasas.
			pensando:'=?'
			
		},
		templateUrl: 'directives/bcs-input-instrumentos/bcs-input-instrumentos.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return ($scope.datosInstrumento && $scope.datosInstrumento.IdInstrumento);
			}
		},		
		link: function(scope, element, attrs, fn) {
			scope.pensando = false;
			scope.idVentana = element.closest("[winkey]").attr("ventanaid");
			scope.blur = function(event) {
				buscarInstrumentoPorNemo();
            };
			element.bind('keypress', function (event) {
                    if(event.keyCode == 13 && !event.shiftKey){
                        event.stopPropagation();
  						scope.eventPromise = scope.deferred.promise;
                        buscarInstrumentoPorNemo();
                        scope.eventPromise.then(function(result){
                        	var e = jQuery.Event("keypress");
							e.which = 13; // # Some key code value
							$timeout(function() {element.trigger(e)});
                        });
                        event.preventDefault();
                    }
                });
			scope.$watch('buscarInstrumento',function(newval,oldval) {
				if (newval === undefined || newval === "") return;
				if (newval === "-1") {
					scope.datosInstrumento = undefined;
					scope.buscarInstrumento = "";
					scope.nemotecnico = "";
					return;
				}
				scope.nemotecnico = newval;
				scope.buscarInstrumento = "";
				buscarInstrumentos(newval);
			});
			scope.$watch('buscarIdInstrumento',function(newval,oldval) {
				if (!newval) return;
				scope.buscarIdInstrumento = "";
				buscarInstrumentoPorId(newval);
			});
			
			//FUNCION QUE BUSCA EN VENTANA
			scope.fnBusquedaInstrumento = function() {
				if (scope.kNgDisabled) return;

				scope.deferred = $q.defer();
				var id1 = bcsComunes.ventanaAbrir('busquedaInstrumentos',{
					dataSourceInstrumentos : getInstrumentosDataSource(),
					dataSourceFlujoCustodiaOrden : scope.validaFlujoCustodia === 'S'? getFlujoCustodiaDataSource() : undefined
				},false,false,scope.idVentana);
				bcsComunes.ventanaPromesa(id1).then(function(data){
					if (!data || !data.IdInstrumento) return;
					var datosInstrumento = $.extend({},data);
					//scope.datosInstrumento = datosInstrumento;
					//scope.nemotecnico = datosInstrumento.Nemotecnico;
					completarInformacionInstrumento(datosInstrumento);
					//scope.deferred.resolve();
				});
				
			};
			
			//FUNCION QUE BUSCA LO ESCRITO

			var isACTorINA = function(filtro) {
				switch (filtro) {
					case 'ACT':
						return 'ACT';
						break;
					case 'INA':
						return 'INA';
					default:
						return 'ACT'
				}
			}

			var buscarInstrumentos = function(nemo,idInstrumento) {
				
				var r = $resource("",{} , {
					'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21025"}
				});
				
				var instrumentos = [
					{ Correlativo : 0,IdInstrumento : idInstrumento ,Nemotecnico : nemo}
				]; 
				
				if (esBonoReconocimiento(nemo)) {
					instrumentos[0].Nemotecnico = esBonoReconocimiento(nemo).nemoBase;
				} 
				else if (esCuponEmision(nemo)) {
					instrumentos[0].Nemotecnico = esCuponEmision(nemo).nemotecnicoBolson;
				}
				
				var tipoInstrumentos = [];
				if (angular.isArray(scope.filtroTipoInstrumento)) {			
					$.each(scope.filtroTipoInstrumento,function(key,value) {
						tipoInstrumentos.push({Correlativo : 0,IdTipoInstrumento : value});
					});
				}
				else if (scope.filtroTipoInstrumento) {
					tipoInstrumentos.push({Correlativo : 0,IdTipoInstrumento : scope.filtroTipoInstrumento});
				}
				
			
				var clasificacionTipoInstrumentos = [];
				if (angular.isArray(scope.filtroClasificacion)) {			
					$.each(scope.filtroClasificacion,function(key,value) {
						clasificacionTipoInstrumentos.push({Correlativo : 0,IdClasificacionTipoInstrumento : value});
					});
				}
				else if (scope.filtroClasificacion) {
					clasificacionTipoInstrumentos.push({Correlativo : 0,IdClasificacionTipoInstrumento : scope.filtroClasificacion});
				}

				var filtroEstado = [];
				var filtroInstrumentoNegociacion = 0;
				if(scope.filtroEstado) {
					if(scope.filtroEstado.length >= 1 && typeof scope.filtroEstado !== 'string') {
						filtroEstado = scope.filtroEstado;
					}else {
						filtroEstado.push(isACTorINA(scope.filtroEstado));
					}
				}else {
					filtroEstado.push('ACT');
				}
				
				if(scope.filtroInstrumentoNegociacion === 'S'){
					filtroInstrumentoNegociacion = 1;
				}

				var datos = {
						p_IdEmpresa : $rootScope.SesionIdEmpresa,
						p_IdUsuario: $rootScope.SesionIdUsuario,
						p_IdModulo: "PLA",
						p_CreaOpcionAutomatica : scope.creaOpcion !== undefined ? scope.creaOpcion : 'N',
						p_IdTipoInstrumentos: tipoInstrumentos,
						p_IdClasificacionTipoInstrumentos: clasificacionTipoInstrumentos,
						p_Instrumentos: instrumentos,
						p_filtroEstado: filtroEstado,
						p_filtroInstrumentoNegociacion: filtroInstrumentoNegociacion //flag que activa el filtro
				}
				
				scope.pensando = true;
				scope.deferred = $q.defer();
				r.Consultar(datos).$promise.then(function(result){
					scope.pensando = false;
					var found = false;
					var datosInstrumento = undefined;
					
					if (result.P_OUT_INSTRUMENTOS.length === 0) {
						scope.nemotecnico = "";
						bcsComunes.msg("error","UI_ERROR","UI_INSTRUMENTO_NO_EXISTE");
						scope.datosInstrumento = undefined;
						return;
					}					
					
					datosInstrumento = result.P_OUT_INSTRUMENTOS[0];	
					datosInstrumento.FechaVencimiento = util.dateDS(datosInstrumento.FechaVencimiento,"FechaSistema","CALL"); 
			
					//ARREGLAR NEMOS BOLSON
					if (nemo !== undefined) {
						var datosBono = esBonoReconocimiento(nemo);
						if (datosBono) {
							datosInstrumento.Nemotecnico = datosBono.nemotecnico;
							datosInstrumento.FechaVencimiento = datosBono.fechaVencimiento;
							datosInstrumento.FechaEmision = datosBono.fechaEmision;
						}

						var datosCuponEmision = esCuponEmision(nemo);
						if (datosCuponEmision) {
							datosInstrumento.Nemotecnico = datosCuponEmision.nemotecnico;
							datosInstrumento.FechaVencimiento = datosCuponEmision.fechaVencimiento;
						}
					}
					
					completarInformacionInstrumento(datosInstrumento);
				},function(result){
					scope.pensando = false;
					console.log(result);
				});
			}
			
			//DATASOURCE FLUJO CUSTODIA 
			var getFlujoCustodiaDataSource = function() {
				
				if (!scope.idPortafolio || !scope.idBoveda)
    				return;
				
				var FiltrosBusqueda = [];				
				if (angular.isArray(scope.filtroTipoInstrumento)) {					
					$.each(scope.filtroTipoInstrumento,function(key,value){
						FiltrosBusqueda.push({
							IdClasificacionTipoInstrumento 	: null,
							IdTipoInstrumento 				: value,
							IdInstrumento 					: null,
							IdEmisor 						: null,
							FechaVencimiento 				: null,
							IdTipoReajuste 					: null,
							EsFungible 						: null,
							IdBoveda 						: scope.idBoveda || null,
						});
					})
				} else {
					if (angular.isArray(scope.filtroClasificacion)) {
						$.each(scope.filtroClasificacion,function(key,value){
							FiltrosBusqueda.push({
								IdClasificacionTipoInstrumento 	: value,
								IdTipoInstrumento 				: scope.filtroTipoInstrumento !== null ? scope.filtroTipoInstrumento : null,
								IdInstrumento 					: null,
								IdEmisor 						: null,
								FechaVencimiento 				: null,
								IdTipoReajuste 					: null,
								EsFungible 						: null,
								IdBoveda 						: scope.idBoveda || null,
							});
						})
					} else {
						FiltrosBusqueda.push({
							IdClasificacionTipoInstrumento 	: scope.filtroClasificacion !== null ? scope.filtroClasificacion : null,
							IdTipoInstrumento 				: scope.filtroTipoInstrumento !== null ? scope.filtroTipoInstrumento : null,
							IdInstrumento 					: null,
							IdEmisor 						: null,
							FechaVencimiento 				: null,
							IdTipoReajuste 					: null,
							EsFungible 						: null,
							IdBoveda 						: scope.idBoveda || null,
						});
					};
				};
				if ( FiltrosBusqueda.length === 0 ){
					FiltrosBusqueda.push({
						IdClasificacionTipoInstrumento 	: null,
						IdTipoInstrumento 				: null,
						IdInstrumento 					: null,
						IdEmisor 						: null,
						FechaVencimiento 				: null,
						IdTipoReajuste 					: null,
						EsFungible 						: null,
						IdBoveda 						: scope.idBoveda || null,
					});
				};
				
    			var datos = {
					p_IdEmpresa 			: $rootScope.SesionIdEmpresa,
					p_IdUsuario 			: $rootScope.SesionIdUsuario,
					p_IdModulo 				: 'PLA',
					p_FechaSaldo 			: $rootScope.SesionFechaSistema,
					p_IdPais     			: $rootScope.SesionIdPaisSistema,						
					p_DiasProyeccion 		: 5,
					p_DiaHabil				: 'S',
					p_ValorizarFlujo		: 'S',
					p_Uso : [{
						UsoTipoOperacion 			: scope.modo !== null ? scope.modo : '',
						IdCliente 					: null,
						IdPortafolio 				: scope.idPortafolio,
						BusquedaEspecifica 			: scope.filtroFlujoCustodia !== null ? scope.filtroFlujoCustodia : '',
						IdTipoInstrumentoEspecifico : scope.filtroTipoInstrumentoEspecifico !== null ? scope.filtroTipoInstrumentoEspecifico : '',
					}],    			
					p_FiltrosBusqueda : FiltrosBusqueda,
					p_EsAgrupadoPor : [{
						ClasificacionTipoInstrumento	: null,
						TipoInstrumento					: null,
						MercadoLocal					: null,
						Instrumento						: 'S',
						Emisor							: null,
						Nemotecnico						: null,
						NemoSVS							: null,
						FechaEmisionInstrumento			: null,
						FechaVencimientoInstrumento		: null,
						FechaLiquidacionInstrumento		: null,
						TipoReajuste					: null,
						EsFungible						: null,
						CodigoIFRS						: null,
						
						Boveda							: scope.idBoveda != null ? 'S' : null,
						Bolsa							: null,
						TipoOperacion					: null,
						Mesa							: null,
						FolioRegistro					: null,
						ItemOrigen						: null	
					}]
				}
    			
    			return new bcs.DataSource({
					type : "call",
		  			definition : {
						serverFiltering 	: false, // Filtrado de datos en el servidor.
						serverPaging 		: false, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
						callParameters      : datos,
						callTransaction 	: 14000,
						callResultSet 		: "p_out_FlujoTotalizado",
						schema: {
			        		model: {
		    	        		fields: {
		    	        			IdPortafolio					: {type : "number"},
		    	        			IdBoveda 						: {type : "string"},
		    	        			FechaSaldo						: {type : "sysdate"},
		    	        			Dias							: {type : "number"},
		    	        			IdInstrumento					: {type : "number"},
		    	        			Cantidad						: {type : "number"},
		    	        			IdMonedaInstrumento				: {type : "string"},
									PrecioMercado					: {type : "number"},
		    	        			MontoAMercadoMonedaSistema		: {type : "number"},
		    	        			MontoAMercadoMonedaInstrumento	: {type : "number"}
			        			}
			        		}
			        	}
					}
				});
			}
			
			//DATASOURCE PRECIOS
			var getPreciosDataSource = function(idInstrumento) {
				return new bcs.DataSource({
					type : "call",
		  			definition : {
						serverFiltering 	: false, // Filtrado de datos en el servidor.
						serverPaging 		: false, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
						callParameters      : {
							p_IncluyeNemoNegociacion: "S",
							p_Instrumentos : [{ 
								IdEmpresa 					: $rootScope.SesionIdEmpresa,
								IdInstrumento				: idInstrumento,
								IdBolsa 					: scope.filtroBolsa || $rootScope.SesionBolsaDefectoSistema , 
								Fecha 						: $rootScope.SesionFechaSistema,
								IdCondicionDeLiquidacion 	: scope.filtroCondicionDeLiquidacion || 'T2'
								
								}
							]
						},
						callTransaction 	: 3252,
						callResultSet 		: "P_OUT_INSTRUMENTO",
						schema: {
			        		model: {
		    	        		fields: {
		    	        			IdInstrumento	: { type : "number"},
		    		    			Fecha 			: { type : "string"},
		    		    			IdBolsa 		: { type : "string"},
		    		    			PrecioCierre 	: { type : "number"},		    		    			
		    		    			PrecioMercado	: { type : "number"},
		    					 	TasaMercado		: { type : "number"},
			        			}
			        		}
			        	}
					}
				});
			}
			
			scope.$watchGroup(['filtroBolsa'],function(newval,oldval){
				if (scope.filtroBolsa === undefined || 
					scope.datosInstrumento === undefined || scope.datosInstrumento.IdInstrumento === undefined ||
					scope.consultaPrecio === "N"
					)	
					return;
				completarInformacionInstrumento(scope.datosInstrumento);
			});
			
			scope.$watchGroup(['filtroCondicionDeLiquidacion'],function(newval,oldval){
				if (scope.filtroCondicionDeLiquidacion === undefined || 
					scope.datosInstrumento === undefined || scope.datosInstrumento.IdInstrumento === undefined ||
					scope.consultaPrecio === "N"
					)	
					return;
				completarInformacionInstrumento(scope.datosInstrumento);
			});
			
			//DATASOURCE INSTRUMENTOS
			var getInstrumentosDataSource = function(nemo,idInstrumento) {
				var filtros = {
					filters : [],
					logic : "and"
				};
				
				if (nemo) {
					if (esBonoReconocimiento(nemo)) {
						filtros.filters.push({ field: "Nemotecnico", operator: "eq", value: esBonoReconocimiento(nemo).nemoBase  });
					} 
					else if (esCuponEmision(nemo)) {
						filtros.filters.push({ field: "Nemotecnico", operator: "eq", value:esCuponEmision(nemo).nemotecnicoBolson  });
					}
					else {
						filtros.filters.push({ field: "Nemotecnico", operator: "eq", value:nemo  });
					}
				}
				if (idInstrumento)
					filtros.filters.push({ field: "IdInstrumento", operator: "eq", value:idInstrumento  });
				
				if (angular.isArray(scope.filtroClasificacion)) {
					var filtrosClasificacion = [];
					$.each(scope.filtroClasificacion,function(key,value){
						filtrosClasificacion.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value:value });
					})
					filtros.filters.push({logic : "or",filters : filtrosClasificacion});
				}
				else if (scope.filtroClasificacion) {
					filtros.filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value:scope.filtroClasificacion  });
				}
				
				
				if (angular.isArray(scope.filtroTipoInstrumento)) {
					var filtrosTipoInstrumento = [];
					$.each(scope.filtroTipoInstrumento,function(key,value){
						filtrosTipoInstrumento.push({ field: "IdTipoInstrumento", operator: "eq", value:value });
					})
					filtros.filters.push({logic : "or",filters : filtrosTipoInstrumento});
				}
				else if (scope.filtroTipoInstrumento)
					filtros.filters.push({ field: "IdTipoInstrumento", operator: "eq", value:scope.filtroTipoInstrumento  });
				
				if (angular.isArray(scope.filtroMercadoLocal)) {
					var filtrosMercadoLocal = [];
					$.each(scope.filtroMercadoLocal,function(key,value){
						filtrosMercadoLocal.push({ field: "MercadoLocal", operator: "eq", value:value });
					})
					filtros.filters.push({logic : "or",filters : filtrosMercadoLocal});
				}
				else if (scope.filtroMercadoLocal)
					filtros.filters.push({ field: "MercadoLocal", operator: "eq", value:scope.filtroMercadoLocal  });
				
				
				if (scope.filtroEstado)
					filtros.filters.push({ field: "IdEstado", operator: "eq", value:scope.filtroEstado  });
				if (scope.filtroInstrumentoNegociacion  != 'S')
					filtros.filters.push({ field: "IdInstrumentoRaiz", operator: "eq", value: 0 });
				if (scope.filtroEmisor)
					filtros.filters.push({ field: "IdEmisor", operator: "eq", value:scope.filtroEmisor  });
				if (scope.filtroMoneda)
					filtros.filters.push({ field: "IdMoneda", operator: "eq", value:scope.filtroMoneda  });
				if (scope.filtroSoloVigentes === 'S')
					filtros.filters.push({
						logic: "or",
						filters : [
							{field: "FechaVencimiento", operator: "eq", value: "0001-01-01"  },
							{field: "FechaVencimiento", operator: "ge", value: $rootScope.SesionFechaSistema  }
						]
					});
				if (scope.filtroReporte === 'S')
					filtros.filters.push({ field: "TipoInstrumentoBolsa", operator: "eq", value: "REP"  }
				);	
				return new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						filter: filtros,
						pageSize : 200,
						serverPaging : true,
						serverSorting : true,
						sort : { field : "Nemotecnico",dir : "asc"},
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3251",
								dataType: "json"
							}
						},
		    		    schema: {
		    		    	model : {
		    		    		fields : {
		    		    			IdInstrumento 			: {type : "number"},
		    		    			Nemotecnico 			: { type : "string"},
		    		    			FechaVencimiento 		: { type : "sysdate"},
		    		    			RazonSocial 			: { type : "string"},
		    		    			ISIN 					: { type : "string"},
		    		    			IdMoneda 				: { type : "string"},
		    		    			PorcentajeCastigo		: {type	 : "number"},
		    		    			IdEmisor				:{type:"string"},
		    		    			CorteMinimo				:{type:"number"},
									Emitido					:{type:"string"},
									IdTipoReajuste			:{type:"string"},
									ValorClasificacionRiesgo:{type:"string"},
									Colocacion				:{type:"string"},
									Tasa					:{type:"number"},
									FrecuenciaCupon			:{type:"string"},
									FechasCalled			:{type:"string"},
									IdMonedaInstrumento		:{type:"string"},
									
									TasaEmision				:{type:"string"},
									CallConfirmado			:{type:"string"},
									

									SaldoT0					:{type:"number"},
									SaldoT1					:{type:"number"},
									SaldoT2					:{type:"number"},
									SaldoT3					:{type:"number"},
									SaldoT4					:{type:"number"},
									SaldoT5					:{type:"number"},
									PrecioMercado			:{type:"number"},
									InstrumentoAcreditado	:{type:"string"},
									InstrumentoBolsa		:{type:"string"},
		    		    		}
		    		    	
		    		    	}
		    		    }
					}
				});
			}
			
			
			
			//BUSQUEDA POR ID
			var buscarInstrumentoPorId = function(idInstrumento) {
				buscarInstrumentos(undefined,idInstrumento);
			};
			//BUSQUEDA POR NEMO
			var buscarInstrumentoPorNemo = function() {
				if (!scope.nemotecnico || scope.nemotecnico.length <= 0) {
					scope.datosInstrumento = undefined;
					if (!scope.deferred) scope.deferred = $q.defer();
					scope.deferred.resolve();
					return;
				}
				scope.nemotecnico = (scope.nemotecnico || '').toUpperCase();
				buscarInstrumentos(scope.nemotecnico);
			};
			
			//ULTIMO METODO ANTES DE VOLVER
			var completarInformacionInstrumento = function(datosInstrumento) {
				scope.nemotecnico = datosInstrumento.Nemotecnico;

				if (scope.muestraPrecio === 'S' || scope.consultaPrecio === 'S') {
					var dsPrecios = getPreciosDataSource(datosInstrumento.IdInstrumento);
					scope.pensando = true;

					dsPrecios.fetch().then(function(){	
						var precios = dsPrecios.data();
						if (precios.length > 0){
							_.extend(datosInstrumento,precios[0]);
						}

						//Si tiene idUso, entonces llamar a EntregarPrecioTasas y obtener el precio.
						if(scope.idUso!==undefined){
							var datosPrecio = {
								p_IdEmpresa : scope.$root.SesionIdEmpresa,
								p_IdUsuario: scope.$root.SesionIdUsuario,
								p_IdModulo: 'CUS',
								p_IdPais: scope.$root.SesionIdPaisSistema,
								p_DatosInstrumentos:[{
									IdRegistro: 0,
									IdTrabajo : 0,
									TipoCartera : 0,
									IdUso : scope.idUso,
									IdTipoCriterioTasa : 0,
									IdClasificacionTipoInstrumento	: datosInstrumento.IdClasificacionTipoInstrumento,
									IdTipoInstrumento 				: datosInstrumento.IdTipoInstrumento,
									IdInstrumento 					: datosInstrumento.IdInstrumento,
									Nemotecnico						: datosInstrumento.Nemotecnico,
									IdMoneda 						: datosInstrumento.IdMoneda,
									FechaConsulta 					: scope.$root.SesionFechaSistema,
									PlazoResidual					: 0
								}]
							};
							bcsServicioPlataforma.EntregarPreciosTasas(datosPrecio, function(respuesta){
								if (respuesta.P_OUT_PRECIOSTASAS && respuesta.P_OUT_PRECIOSTASAS[0]){
									_.extend(datosInstrumento, respuesta.P_OUT_PRECIOSTASAS[0]);
									datosInstrumento.PrecioMercado = datosInstrumento.Precio;
									datosInstrumento.PrecioCierre = datosInstrumento.Precio;
								}else{
									scope.datosInstrumento.Precio = 0;
									scope.datosInstrumento.PrecioMercado = 0;
									scope.datosInstrumento.PrecioCierre = 0;
								}
								scope.pensando = false;
								scope.datosInstrumento = datosInstrumento;
								scope.deferred.resolve();
							}, function(err){
								scope.pensando = false;
								scope.datosInstrumento.Precio = 0;
								scope.deferred.resolve();
							});

						}else{
							scope.pensando = false;
							scope.datosInstrumento = datosInstrumento;
							scope.deferred.resolve();	
						}
					})

				}else {
					scope.datosInstrumento = datosInstrumento;
					scope.nemotecnico = datosInstrumento.Nemotecnico;
					scope.deferred.resolve();
				}				
			}
			
			//para los BR
			var esBonoReconocimiento = function(nemo) {
				
				if (!nemo) {
					return false;
				}
				
				var codigosMesEmisionBonoReconocimiento = {
					"1" : "01",
					"2" : "02",
					"3" : "03",
					"4" : "04",
					"5" : "05",
					"6" : "06",
					"7" : "07",
					"8" : "08",
					"9" : "09",
					"0" : "10",
					"A" : "11",
					"B" : "12"
				}

				var codigosAgnoEmisionBonoReconocimiento = {
					"1" : "1981",	"2" : "1982",	"3" : "1983",	"4" : "1984",
					"5" : "1985",	"6" : "1986",	"7" : "1987",	"8" : "1988",
					"9" : "1989",	"0" : "1990",	"A" : "1991",	"B" : "1992",
					"C" : "1993",	"D" : "1994",	"E" : "1995",	"F" : "1996",
					"G" : "1997",	"H" : "1998",	"I" : "1999",	"J" : "2000"
				}
				
				var nemosReconocimiento = ["BR","AE","A3","B1","B2","B3","BA","BC","BD","BE","BL","BX","CB3","CBR","D3","DE","E1","E2","E3","F3"];
				
				var result = {
					nemotecnico : undefined,
					fechaVencimiento : undefined,
					fechaEmision : undefined
				}
				
				var nemoBase = undefined;
				for (var i = 0; i<nemosReconocimiento.length ;  i++) {
					if (nemo.startsWith(nemosReconocimiento[i])) {
						nemoBase = nemosReconocimiento[i];
						break;
					}
				}
				
				if (!nemoBase) {
					return undefined;
				}
				
				var largoNemo = nemoBase.length;
				var largoCodigoMes = 1;
				var largoCodigoAgno = 1;
				var largoDiaVencimiento = 2;
				var largoMesVencimiento = 2;
				var largoAgnoVencimiento = 2;
				if (codigosMesEmisionBonoReconocimiento[nemo.substring(largoNemo,largoNemo+largoCodigoMes)] === undefined) {
					return undefined;
				}
				if (codigosAgnoEmisionBonoReconocimiento[nemo.substring(largoNemo+largoCodigoMes,largoNemo+largoCodigoMes+largoCodigoAgno)] === undefined) {
					return undefined;
				}
				var dia = nemo.substring(largoNemo+largoCodigoMes+largoCodigoAgno,largoNemo+largoCodigoMes+largoCodigoAgno+largoDiaVencimiento);
				var mes = nemo.substring(largoNemo+largoCodigoMes+largoCodigoAgno+largoDiaVencimiento,largoNemo+largoCodigoMes+largoCodigoAgno+largoDiaVencimiento+largoMesVencimiento);
				var agno = Number(nemo.substring(largoNemo+largoCodigoMes+largoCodigoAgno+largoDiaVencimiento+largoMesVencimiento,largoNemo+largoCodigoMes+largoCodigoAgno+largoDiaVencimiento+largoMesVencimiento+largoAgnoVencimiento));
				if (agno > 49) {
					agno = 1900+agno+"";
				}
				else {
					agno = 2000+agno+"";
				}
				var fechaVencimiento = agno+"-"+mes+"-"+dia;
				if (!util.esFechaValida(fechaVencimiento,"YYYY-MM-DD")) {
					return undefined;
				}
				
				var fechaEmision = codigosAgnoEmisionBonoReconocimiento[nemo.substring(largoNemo+largoCodigoMes,largoNemo+largoCodigoMes+largoCodigoAgno)]+"-"+codigosMesEmisionBonoReconocimiento[nemo.substring(largoNemo,largoNemo+largoCodigoMes)]+"-01";

				result.nemoBase = nemoBase;
				result.nemotecnico = nemo;
				result.fechaVencimiento = util.dateDS(fechaVencimiento,"FechaSistema","CALL");
				result.fechaEmision = util.dateDS(fechaEmision,"FechaSistema","CALL");
				return result;
					
					
			}

			//PARA XERO, ZERO Y CERO
			var esCuponEmision = function(nemo) {
				
				if (!nemo) {
					return false;
				}
				
				var result = {
					nemotecnico : undefined,
					nemotecnicoBolson : undefined,
					fechaVencimiento : undefined
				}
			
				if (nemo.length != 10) {
					return undefined;
				}
				if (nemo.substring(0,4) !== "XERO" && nemo.substring(0,4) !== "CERO" && nemo.substring(0,4) !== "ZERO") {
					return undefined;
				}
				
				result.nemotecnicoBolson = nemo.substring(0,4);
				
				var dia = nemo.substring(4,6);
				var mes = nemo.substring(6,8);
				var agno = Number(nemo.substring(8,10));
				if (agno > 49) {
					agno = 1900+agno+"";
				}
				else {
					agno = 2000+agno+"";
				}
				var fechaVencimiento = agno+"-"+mes+"-"+dia;
				if (!util.esFechaValida(fechaVencimiento,"YYYY-MM-DD")) {
					return undefined;
				}
				
				result.nemotecnico = nemo;
				result.fechaVencimiento = util.dateDS(fechaVencimiento,"FechaSistema","CALL");
				return result;
					
					
			}
			
			
			
			
			
		}
	};
});

angular.module('ModuloPlataforma').directive('bcsInputInstrumentosGa', function(bcsComunes,$resource,$rootScope,$translate,$q,$timeout,bcsServicioOrden) {
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
			filtroSoloVigentes: '=?',
			filtroEstado: '=?',
			filtroClasificacion: '=?',
			filtroEmisor: '=?',
			filtroMoneda: '=?',
			filtroIdPerfilProductos:  '=?',
			//filtroIdTipoProducto:	'=?',
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
			idPortafolio: '=?'
		},
		templateUrl: 'directives/bcs-input-instrumentos-ga/bcs-input-instrumentos-ga.html',
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
				var id1 = bcsComunes.ventanaAbrir('busquedaInstrumentosGa',{
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
			var buscarInstrumentos = function(nemo,idInstrumento) {
				
				var r = $resource("",{} , {
					'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21025"}
				});
				
				var instrumentos = [
					{ Correlativo : 0,IdInstrumento : idInstrumento ,Nemotecnico : nemo, IdTipoInstrumento:scope.filtroTipoInstrumento}
				]; 
				
				var TipoInstrumento = [
					{ Correlativo : 0,IdTipoInstrumento:scope.filtroTipoInstrumento}
				]; 
					
				var datos = {
						p_IdEmpresa : $rootScope.SesionIdEmpresa,
						p_IdUsuario: $rootScope.SesionIdUsuario,
						p_IdModulo: "PLA",
						p_CreaOpcionAutomatica : scope.creaOpcion !== undefined ? scope.creaOpcion : 'N',
						p_Instrumentos: instrumentos,
						p_IdTipoInstrumentos: TipoInstrumento
						
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
						IdTipoInstrumentoEspecifico : '',
					}],    			
					p_FiltrosBusqueda : FiltrosBusqueda,
					p_EsAgrupadoPor : [{
						ClasificacionTipoInstrumento	: null,
						TipoInstrumento					: null,
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
						filtros.filters.push({ field: "Nemotecnico", operator: "eq", value:"BR"  });
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
				
				if (scope.filtroIdPerfilProductos)
					{
					filtros.filters.push({ field: "IdPerfilProductos", operator: "eq", value:scope.filtroIdPerfilProductos  });
					}
				//filtroIdTipoProducto
				/*if (scope.filtroIdTipoProducto)
				{
				filtros.filters.push({ field: "IdTipoProducto", operator: "eq", value:scope.filtroIdTipoProducto  });
				}*/
				
				if (angular.isArray(scope.filtroTipoInstrumento)) {
					var filtrosTipoInstrumento = [];
					$.each(scope.filtroTipoInstrumento,function(key,value){
						filtrosTipoInstrumento.push({ field: "IdTipoInstrumento", operator: "eq", value:value });
					})
					filtros.filters.push({logic : "or",filters : filtrosTipoInstrumento});
				}
				else if (scope.filtroTipoInstrumento)
					filtros.filters.push({ field: "IdTipoInstrumento", operator: "eq", value:scope.filtroTipoInstrumento  });
				
				
				if (scope.filtroEstado)
					filtros.filters.push({ field: "IdEstado", operator: "eq", value:scope.filtroEstado  });
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6069",
								dataType: "json"
							}
						},
		    		    schema: {
		    		    	model : {
		    		    		fields : {
		    		    			
		    		    		//Funciona x tipo instrumento
		    		    			IdInstrumento						: {type: "number"},
		    		    			Nemotecnico							: {type: "string"},
		    		    			ISIN								: {type: "string"},
		    		    			IdMoneda							: {type: "string"},
		    		    			IdGrupoActivos 						: {type: "string"},
		    		    			RazonSocial 						: {type: "string"},
		    		    			DescripcionGrupoActivos				: {type: "string"},
		    		    			IdTipoGrupoActivos 					: {type: "string"},
		    		    			FechaCreacion 						: {type: "sysdate"},
		    		    			FechaModificacion 					: {type: "sysdate"},
		    		    			IdEstado 							: {type: "string"},
		    		    			PorDefecto							: {type: "string"},
		    		    			IdEmpresa							: {type: "number"},
		    		    			IdDetalleGrupoActivos				: {type: "number"},
		    		    			IdTipoInstrumento					: {type: "string"},
		    		    			CodigoTipoInstrumento				: {type: "string"},
		    		    			NombreTipoInstrumento				: {type: "string"},
		    		    			FechaCreacionDetalle				: {type: "sysdate"},
		    		    			IdEstadoDetalle						: {type: "string"},
		    		    			EstadoDetalle						: {type: "string"},
		    		    			IdPerfilProductos					: {type: "string"},
		    		    			DescripcionPerfilProductos			: {type: "string"},
		    		    			//IdCategoriaInversionista			: {type: "string"},
		    		    			EstadoPerfilProductos				: {type: "string"},
		    		    			EsPerfilPersonalizado				: {type: "string"},
		    		    			//IdDetallePerfilProductos			: {type: "number"},
		    		    			//IdTipoProducto						: {type: "string"},
		    		    			IdCategoriaInversionistaPortafolio	: {type: "string"},
		    		    			//TipoProducto						: {type: "string"},
		    		    			//IdTipoOperacion						: {type: "string"},
		    		    			//TipoOperacion						: {type: "string"},
		    		    			//IdCanalIngreso						: {type: "string"},
		    		    			//CanalIngreso						: {type: "string"},
		    		    			//EstadoProducto						: {type: "string"},
		    		    			//NombreEstadoProducto				: {type: "string"}		    		    		
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
				
				if (scope.muestraPrecio === 'S' || scope.consultaPrecio === 'S') {
					var dsPrecios = getPreciosDataSource(datosInstrumento.IdInstrumento);
							
					scope.pensando = true;
					dsPrecios.fetch().then(function(){
						scope.pensando = false;

						var precios = dsPrecios.data();
						if (precios.length === 1){
							_.extend(datosInstrumento,precios[0]);

						}
						scope.datosInstrumento = datosInstrumento;
						scope.nemotecnico = datosInstrumento.Nemotecnico;
						scope.deferred.resolve();

					})

				}
				else {
					scope.datosInstrumento = datosInstrumento;
					scope.nemotecnico = datosInstrumento.Nemotecnico;
					scope.deferred.resolve();
				}
				
			}
			
			//para los BR
			var esBonoReconocimiento = function(nemo) {
				
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
				
				var result = {
					nemotecnico : undefined,
					fechaVencimiento : undefined,
					fechaEmision : undefined
				}

				if (nemo.length != 10) {
					return undefined;
				}
				if (nemo.substring(0,2) !== "BR") {
					return undefined;
				}
				if (codigosMesEmisionBonoReconocimiento[nemo.substring(2,3)] === undefined) {
					return undefined;
				}
				if (codigosAgnoEmisionBonoReconocimiento[nemo.substring(3,4)] === undefined) {
					return undefined;
				}
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
				
				var fechaEmision = codigosAgnoEmisionBonoReconocimiento[nemo.substring(3,4)]+"-"+codigosMesEmisionBonoReconocimiento[nemo.substring(2,3)]+"-01";

				result.nemotecnico = nemo;
				result.fechaVencimiento = util.dateDS(fechaVencimiento,"FechaSistema","CALL");
				result.fechaEmision = util.dateDS(fechaEmision,"FechaSistema","CALL");
				return result;
					
					
			}

			//PARA XERO, ZERO Y CERO
			var esCuponEmision = function(nemo) {
				
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

angular.module('ModuloPlataforma').directive('bcsInputInstrumentosDv', function(bcsComunes,$resource,$rootScope,$translate,$q,$timeout,bcsServicioOrden,bcsServicioPlataforma) {
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
			filtroCodigoTipoInstrumento: '=?',
			filtroSoloVigentes: '=?',
			filtroEstado: '=?',
			filtroClasificacion: '=?',
			filtroEmisor: '=?',
			filtroMoneda: '=?',
			kNgDisabled : '=?',
			
			filtroMercadoOp : '=?',
			filtroFamiliaOp : '=?',
			filtroSubyacenteOp : '=?',
			filtroModoOp : '=?',		// Ent. Fisica, Compensacion
			filtroTipoOp : '=?',		// Call, Put
			filtroEstiloOp : '=?',		// Americana Europea
			filtroFechaVencimientoOp : '=?',		// Año/Mes de vencimiento instrumento
			
			filtroGrupo: '=?', // [FU, OP]

			filtroBolsa: '=?', 					//PARA PRECIOS
			filtroCondicionDeLiquidacion: '=?', //PARA PRECIOS Y PUNTAS
			
			
			//informacion para traer solo los instrumentos que tiene el portafolio en custodia (ingreso de orden)
			validaFlujoCustodia: '=?',
			fechaFlujo: '=?',
			idBoveda: '=?',
			idTipoOperacion: '=?',
			idPortafolio: '=?',
			idUso: '=?'//Usos del criterio seleccion de tasas.
		},
		templateUrl: 'directives/bcs-input-instrumentos-dv/bcs-input-instrumentos-dv.html',
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
				var id1 = bcsComunes.ventanaAbrir('busquedaInstrumentosDV',{
					dataSourceInstrumentos : getInstrumentosDataSource(),
					dataSourceFlujoCustodiaOrden : scope.validaFlujoCustodia === 'S'? getFlujoCustodiaDataSource() : undefined,
					filtroGrupo : scope.filtroGrupo,
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
				var urlConsultaInstrumentos = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3251";
				
				var ds = getInstrumentosDataSource(nemo,idInstrumento);
				
				scope.pensando = true;
				scope.deferred = $q.defer();
				ds.fetch().then(function(){
					scope.pensando = false;
					var found = false;
					var result = ds.data();
					var datosInstrumento = undefined;
					
					if (result.length === 0) {
						scope.nemotecnico = "";
						bcsComunes.msg("error","UI_ERROR","UI_INSTRUMENTO_NO_EXISTE");
						scope.datosInstrumento = undefined;
						return;
					}
					
					datosInstrumento = result[0];

					//ARREGLAR NEMOS BOLSON
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
					
					completarInformacionInstrumento(datosInstrumento);
					/*scope.datosInstrumento = datosInstrumento;
					scope.nemotecnico = datosInstrumento.Nemotecnico;
					scope.deferred.resolve();*/
					
				},function(result){
					scope.pensando = false;
					console.log(result);
				});
			}
			
			//DATASOURCE FLUJO CUSTODIA INGRESO ORDEN
			var getFlujoCustodiaDataSource = function() {
				
				return new bcs.DataSource({
					type : "call",
		  			definition : {
						serverFiltering 	: false, // Filtrado de datos en el servidor.
						serverPaging 		: false, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
						callParameters      : {
							p_IdEmpresa 		: $rootScope.SesionIdEmpresa,
							p_IdUsuario 		: $rootScope.SesionIdUsuario,
							p_IdModulo 			: 'ORD',						
							p_FechaSaldo 		: util.dateSPdate(scope.fechaFlujo),
							p_DiasProyeccion 	: 0,
												
							p_Ordenes : [{
								IdTipoOperacion : scope.idTipoOperacion,
								IdPortafolio 	: scope.idPortafolio,
								IdBoveda 		: scope.idBoveda === 0?null:scope.idBoveda
							}],
						},
						callTransaction 	: 1686,
						callResultSet 		: "P_OUT_SALIDA",
						schema: {
			        		model: {
		    	        		fields: {
		    	        			IdInstrumento	: {type : "number"},
		    	        			Cantidad		: {type : "number"},
							Precio			: {type : "number"},
		    	        			Monto			: {type : "number"}
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
				
				scope.MesInicio = 0;
				scope.MesFin = 0;

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
					filtros.filters.push({logic : "and",filters : [{ field: "IdClasificacionTipoInstrumento", operator: "eq", value:scope.filtroClasificacion  }]});
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

				if(scope.filtroGrupo){
					filtros.filters.push({ field: "IdGrupoProducto", operator: "eq", value:scope.filtroGrupo  });
				}
				
				
				
//CELCONDOR-705
				if (angular.isArray(scope.filtroCodigoTipoInstrumento)) {
					var filtrosCodigoTipoInstrumento = [];
					$.each(scope.filtroCodigoTipoInstrumento,function(key,value){
						filtrosCodigoTipoInstrumento.push({ field: "IdTipoInstrumento", operator: "eq", value:value });
					})
					filtros.filters.push({logic : "or",filters : filtrosCodigoTipoInstrumento});
				}
				else if (scope.filtroCodigoTipoInstrumento)
					filtros.filters.push({ field: "IdTipoInstrumento", operator: "eq", value:scope.filtroCodigoTipoInstrumento  });
//CELCONDOR-705				

				if (scope.filtroMercadoOp)
					filtros.filters.push({ field: "CodigoClasificacionDV", operator: "eq", value:scope.filtroMercadoOp  });
				if (scope.filtroFamiliaOp)
					filtros.filters.push({ field: "IdTipoInstrumento", operator: "eq", value:scope.filtroFamiliaOp  });
//CELCONDOR-705				
				if (scope.filtroFamiliaOpc)
					filtros.filters.push({ field: "CodigoTipoInstrumento", operator: "eq", value:scope.filtroFamiliaOpc  });
//CELCONDOR-705				
				
				if (scope.filtroSubyacenteOp)
					filtros.filters.push({ field: "RazonSocial", operator: "eq", value:scope.filtroSubyacenteOp  });
				if (scope.filtroModoOp)
					filtros.filters.push({ field: "IdModoOpcion", operator: "eq", value:scope.filtroModoOp  });
				if (scope.filtroEstiloOp)
					filtros.filters.push({ field: "IdEstilo", operator: "eq", value:scope.filtroEstiloOp  });
				if (scope.filtroTipoOp)
					filtros.filters.push({ field: "IdTipoOpcion", operator: "eq", value:scope.filtroTipoOp  });
				if (scope.filtroFechaVencimientoOp && scope.filtroFechaVencimientoOp > "") {
                    
                    scope.DateFiltroIni = new Date(util.date(scope.filtroFechaVencimientoOp).add(1,'day').toDate());
                    scope.FechaFiltroIni = util.dateToFormat(scope.DateFiltroIni,"YYYY-MM-DD");
					//scope.DiaInicio = Number(scope.DateFiltroIni.getDay());
					//scope.MesInicio = Number(scope.DateFiltroIni.getMonth()) + 1;
					//scope.FechaFiltroIni = scope.DateFiltroIni.getUTCFullYear() + "-"; 
					//scope.FechaFiltroIni += scope.MesInicio > 9 ? scope.MesInicio  : "0" + scope.MesInicio;					
					//scope.FechaFiltroIni = scope.DateFiltroIni.getUTCFullYear() + "-";
					//scope.FechaFiltroIni += scope.DiaInicio > 9 ? scope.DiaInicio  : "0" + scope.DiaInicio;
					//scope.FechaFiltroIni += "-01" ;
					//scope.FechaFiltroIni = util.dateInicioDia(scope.DateFiltroIni).format("YYYY-MM-DD");
										
					scope.FechaFiltroFin = util.dateAddMonths(scope.DateFiltroIni,1);
					//scope.FechaFiltroFin = util.dateToFormat(scope.DateFiltroFin,"YYYY-MM-DD");
					//scope.MesFin = Number(scope.DateFiltroFin.getMonth()) + 1;
					//scope.FechaFiltroFin = scope.DateFiltroFin.getUTCFullYear() + "-";
					//scope.FechaFiltroFin +=	scope.MesFin > 9 ? scope.MesFin : "0" + scope.MesFin;
				 	//scope.FechaFiltroFin += "-01" ;
					
					filtros.filters.push({
						logic: "and",
						filters : [
							{field: "FechaVencimiento", operator: "gt", value: scope.FechaFiltroIni  },
							{field: "FechaVencimiento", operator: "lt", value: scope.FechaFiltroFin  }
						]
					});
				};
				
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
							{field: "FechaVencimiento", operator: "gt", value: $rootScope.SesionFechaSistema  }
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
								// url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3251",
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8010",								
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
									FechaVencimiento		:{type:"date"},
									CorteMinimo				:{type:"number"},
									Emitido					:{type:"string"},
									IdTipoReajuste			:{type:"string"},
									ClasificacionDeRiesgo	:{type:"string"},
									Colocacion				:{type:"string"},
									Tasa					:{type:"number"},
									FrecuenciaCupon			:{type:"string"},
									FechasCalled			:{type:"string"},
		
									Cantidad				:{type:"number"},
									Monto					:{type:"number"},
									Precio					:{type:"number"},
									InstrumentoAcreditado	:{type:"string"},

									IdTipoOpcion			:{type:"string"},
									IdEstiloOpcion			:{type:"string"},
									IdModoOpcion			:{type:"string"},
									IdModalidadEjercicio	:{type:"string"},

									TipoOpcion				:{type:"string"},
									EstiloOpcion			:{type:"string"},
									ModoOpcion				:{type:"string"},
									LotePadron				:{type:"number"},
									PrecioStrike			:{type:"number"},
									ModalidadEjercicio		:{type:"string"},
									CodigoClasificacionDV	:{type:"string"},
									
									
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
				console.log(scope.idUso);
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

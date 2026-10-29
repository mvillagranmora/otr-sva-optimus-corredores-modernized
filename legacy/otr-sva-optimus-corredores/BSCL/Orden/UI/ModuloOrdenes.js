angular.module('ModuloOrdenes', []);

angular.module('ModuloOrdenes').factory('bcsConfiguracionOrdenes',function($rootScope,bcsServicioReportes,bcsConsultasOrdenes,bcsComunes,$q,bcsConfiguracionOrdenesV2) {
	var obj = {};
	
	var CONFIGURACION = {};
	var UI = {};
	var FUNCIONES = {};
	var CONSTANTES = {
			IdTipoCanalDistribucionTrader	: 'RUT',
			IdTipoCanalDistribucionAgente	: 'RUA',
			IdTipoCanalDistribucionRetail	: 'RUR',
			IdTipoCanalDistribucionManual	: 'MAN',
			IdSistemaBursatilIngresoManual	: 'MANUAL',
			
			//tipo vigencia
			IdTipoVigenciaSoloHoy			: 'SH',
			IdTipoVigenciaPermanente		: 'OP',
			IdTipoVigenciaSemanal			: 'SE',
			IdTipoVigenciaFechaFija			: 'FF',
				
			//tipo precio
			IdTipoPrecioMercado				: 'M',
			IdTipoPrecioLimite				: 'L',
			TipoOpcion      				: 'PUT'
	}

	CONFIGURACION.defer = $q.defer();
	CONFIGURACION.promise = CONFIGURACION.defer.promise;
	var cargarConfiguracion = function() {
		var ds = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true,
				filter : { field : "IdEmpresa", operator : "eq", value : $rootScope.SesionIdEmpresa},
				transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarConfiguracionOrdenes'),
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							IdEmpresa 		: { type : "number"},
							CodigoParametro : { type : "string"},
							Descripcion 	: { type : "string"},
							Valor 			: { type : "string"},
								
						}
					}
				}
			}
		});
		
		ds.fetch().then(function(){
			var data = ds.data();
			$.each(data,function(key,value){
				CONFIGURACION[value.CodigoParametro] = value.Valor;
			})
			CONFIGURACION.defer.resolve();
		},function() {
			console.error("ERROR AL CARGAR CONFIGURACION ORDENES");
		})
		
		
	};
	cargarConfiguracion();
		
	UI.getFiltrosCombo = function(controllerName,tipoCombo){
		var map = {};
		
		map.OrdenesPorNegociarCtrl = {
				'combobox-tipo-instrumento' : {
	    			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
	    			IdTipoInstrumento : {"field": "IdTipoInstrumento","operator": "neq","value": "FWD"}    		
	    		},
	    		'combobox-canal-distribucion' : {
	    			IdTipoCanalDistribucion : {"field": "IdTipoCanalDistribucion","operator": "eq","value": CONSTANTES.IdTipoCanalDistribucionAgente}, //solo ruteo agente
	    			IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $rootScope.SesionIdEmpresa},
	    		},
	    		'combobox-operacion' : {
	    			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
					EsOrden : { "field"   : "EsOrden","operator": "eq", "value"   : "S" }
	    		},
	    		'combobox-operador' : {
	    			//X : {"field": "X","operator": "eq","value": ""}    		
	    		},
	    		'combobox-operador-cartera-inversion' : {
	    			IdEmpresa : {"field": "IdEmpresa","operator": "eq","value": $rootScope.SesionIdEmpresa} ,
	    			CodigoOperador : {"field": "CodigoOperador","operator": "eq","value": "false"} 
	    		},
	    		'combobox-operador-cartera-ejecutivo' : {
	    			IdEmpresa : {"field": "IdEmpresa","operator": "eq","value": $rootScope.SesionIdEmpresa} ,
	    			CodigoOperador : {"field": "CodigoOperador","operator": "eq","value": "false"} 
	    		},
				'combobox-moneda-negociacion' : {
	    			IdMoneda : {"field": "IdMoneda","operator": "eq","value": ""} 
				}
		};
		
		map.ConsultarTransaccionesCtrl = {
				'combobox-tipo-instrumento' : {
	    			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
	    			IdTipoInstrumento : {"field": "IdTipoInstrumento","operator": "neq","value": "FWD"}
	    		},
	    		'combobox-operacion' : {
	    			EsTransaccion : {"field": "EsTransaccion","operator": "eq","value": "S"}    		
	    		},
	    		'combobox-condicion-liquidacion' : {
	    			AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": ""}    		
	    		}
		};
		
		map.ConsultarAsignacionesCtrl = {
				'combobox-tipo-instrumento' : {
	    			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
	    			IdTipoInstrumento : {"field": "IdTipoInstrumento","operator": "neq","value": "FWD"}    		
	    		},
	    		'combobox-operacion' : {
	    			EsOrden : {"field": "EsOrden","operator": "eq","value": "S"}    		
	    		},
	    		'combobox-condicion-liquidacion' : {
	    			AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": ""}    		
	    		}
		};
		
		map.EnvioFacturacionMasivoCtrl = {
				'combobox-tipo-instrumento' : {
	    			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""}    		
	    		},
	    		'combobox-operacion' : {
	    			logic : "or",
	    			C : { field : "AccionOperacion" , operator : "eq" , value : "COMPRA"},
	    			V : { field : "AccionOperacion" , operator : "eq" , value : "VENTA"},
	    		},
	    		'combobox-condicion-liquidacion' : {
	    			AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": ""}    		
	    		},
		};
		
		if (map[controllerName] && map[controllerName][tipoCombo])
			return map[controllerName][tipoCombo]
		else 
			return {};	
		
		
	}
	
	//arreglo de ids de portafolio
	FUNCIONES.getInformacionPortafolios = function(portafolios) {
		var defer = $q.defer();

		var idsPortafolio =  [];
		$.each(portafolios,function(key,value){
			idsPortafolio.push({field : "IdPortafolio", operator : "eq", value : value});
		});
		var dataSourcedDefIndicadores = bcsConfiguracionOrdenesV2.getDataSourceInformacionPortafolio();
		dataSourcedDefIndicadores.definition.filter = {
				logic : "or",
				filters : idsPortafolio
		};
		var dsIndicadores = new bcs.DataSource(dataSourcedDefIndicadores);
		dsIndicadores.fetch().then(function(){
			defer.resolve(dsIndicadores.data());
		},function(){
			defer.reject();
		});

		return defer.promise;
	}
	
	FUNCIONES.getGrupoPorMarca = function(marca) {
		var defer = $q.defer();

		var dataSourcedDefGrupos = bcsConfiguracionOrdenesV2.getDataSourceGruposPortafolios();
		dataSourcedDefGrupos.definition.filter = { field : "Codigo" , operator : "eq" , value : marca };
		var dsGrupos = new bcs.DataSource(dataSourcedDefGrupos);
		dsGrupos.fetch().then(function(){
			defer.resolve(dsGrupos.data());
		},function(){
			defer.reject();
		});

		return defer.promise;
	}
	
	FUNCIONES.generarPdf417 = function(documentoIdentidad,fechaOperacionOrden,secuenciaOrden,montoOperacion){
		return documentoIdentidad+""+util.dateSPseconddate(fechaOperacionOrden)+""+documentoIdentidad+""+secuenciaOrden + ""+montoOperacion;
	}
	
	FUNCIONES.verComprobante = function(idEmpresa,documentoIdentidad,fechaOperacionOrden,secuenciaOrden,montoOperacion,idVentana){
		var ConfiguracionTemplate = new bcs.DataSource({
			type: "odata",
			definition:{
				serverFiltering:true,
				sort: { field: "ReportVersion", dir: "desc" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015&c=1",
						dataType: "json",
						data:{
							$select: "\"CompanyId\",\"Id\",\"ReportName\",\"ReportVersion\""
						}
					}
				},
				schema: {
					model:
					{
						fields : {
							Id: {type: "string"},
							ReportName: {type: "string"},
							ReportVersion: {type: "string"}
						}
					}
				}
			}
		});
				
		var filtro = new Array();
		filtro.push({ field: "CompanyId", operator: "eq", value: Number(idEmpresa) });
		filtro.push({ field: "ReportName", operator: "eq", value: "COMPROBANTE_ORDEN" });
		
		ConfiguracionTemplate.query({filter:filtro}).then(function() {
			if(ConfiguracionTemplate.data().length > 0){
				var pdf417 = FUNCIONES.generarPdf417(documentoIdentidad,fechaOperacionOrden,secuenciaOrden,montoOperacion);
				var data = {
					"P_IDEMPRESA" :  Number(idEmpresa),
					"P_IDMODULO" : "ORD",
					"P_IDORDEN" : secuenciaOrden,
					"$schema":[{"field":"LogoCorredora", "type":"blob"}]
				};	
				bcsServicioReportes.descargarReportePdf417(ConfiguracionTemplate.data()[0].Id, 3007, "COMPROBANTE_ORDEN", pdf417, "PDF", data, idVentana, false);

			}else{
				bcsComunes.msg("error","Error", $translate.instant("UI_ERROR_OBTENER_REPORTE"));
			}
		});
	},
	
	
	
	
	
	
	obj.CONFIGURACION = CONFIGURACION;
	obj.FUNCIONES = FUNCIONES;
	obj.CONSTANTES = CONSTANTES;
	obj.UI = UI;
	return obj;
		
});


angular.module('ModuloOrdenes').factory('bcsConsultasOrdenes',function() {
	var url_base = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=";
		var consultas = {
				ConsultarOrdenesUI : 1658,
				ConsultarAsignacionesUI : 1200,
				ConsultarTransaccionesUI : 1201,
				ConsultarTransaccionesAnuModUI : 4255,
				
				ConsultarOrdenesEnNegociacion : 1204, 
				ConsultarOrdenesPorNegociar : 1203,	
				ConsultarOrdenesPorAsignar : 1202,
				ConsultarTransaccionesPorAsignar : 1720,
				ClientesLibroOrdenes : 1205,
				ConsultarTransaccionesBloqueadas : 1206,
				ConsultarOrdenesBloqueadas : 1207,
				ConsultarCobrosAsignacion : 1208,
				ConsultarCobrosOrden : 1209,
				ConsultarCobrosPortafolio : 3017,
				ConsultarGrupo : 1210,
				ConsultarPortafoliosGrupo : 1211,
				ConsultarEstadoGrupoPortafolio : 1212,
				
				ConsultarLibroOrdenes : 1213,
				ConsultarLibroOrdenesOperadores : 1214,
				ConsultarLibroOrdenesSegmento : 1215,
				ConsultarLibroOrdenesMarcasNegociacion : 1216,
				ConsultarLibroPorDefecto : 1217,
				
				ConsultarMovimientosFacturar : 1235,
				ConsultarCarterasAsignacion : 1403,
				ConsultarPreferenciasPagoPortafolio : 1667,
				ConsultarEventoBitacoraUI : 1707,
				ConsultarEventoLiquidarUI: 4290653,
				ConsultarOrden : 3002,
				ConsultarCorredorCorresponsal : 3009,
				
				ConsultarTBSOLICITUDES : 3060,
				ConsultarTBRESPUESTAS : 3061,
				ConsultarClientesLibroOrdenesGrupo : 3237,
				ConsultarConfiguracionOrdenes : 3564,
				ConsultarIndicadoresPortafolioOrdenes : 3587,
				ConsultarInformacionPortafolioOrdenes : 21033,
				ConsultarOrdenAsociada:4164,
				
				ConsultarOrdenesReportes : 4149,
				ConsultarTransaccionesReporteUI : 4201,
				ConsultarAsignacionesReporteUI : 4202,
				
				ConsultarOrdenPorNegociarReportes : 4203,
				ConsultarOrdenEnNegociacionReportes : 4204,
				
				ConsultarGarantias : 12500/*1012*/,
				
				ConsultarEmpleadoUI : 13017,
				ConsultarDetallePlantillaEmpleado : 1567,
				ConsultarDetallePerfilProductos : 1328,
				ConsultarProductoBovedaCorredorCorresponsal : 600075,
				ConsultarTransaccionesHistorico: 700019,
				ConsultarInstruccion: 700329,
				ConsultarDocumentoInstruccion: 700339,
				ConsultarMonedaFiltroOrdenes: 700000010,
				ConsultarPreferenciaPagoPortafolio: 1672
				
		}
		return function(nombreConsulta) {
			return "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + consultas[nombreConsulta];
		};
});

angular.module('ModuloOrdenes').factory('bcsServicioOrden',['$resource',function($resource) {
		
		var generarResource = function() {
			var r = {};
			$.each(resources,function(key,value) {
				r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&a=" + value.nombre  + "&t="+value.id  };
				
			});
			return r;
		};		                 
		
		var resources = [
			{ nombre : 'CrearTransaccion', id : "4" },
            { nombre : 'CrearAsignacion', id : "5" },
  			{ nombre : 'CrearMovtoFacturarAsignacion', id : "10" },
  			{ nombre : 'CalcularMontoyCantidadOrden', id : "20" },
  			{ nombre : 'CrearOrdenPostrera', id : "66" },
  			{ nombre : 'OfertarOrdenes', id : "99" },
  			{ nombre : 'ActivarAsignacionCronologica', id : "105" },
  			{ nombre : 'DesactivarAsignacionCronologica', id : "106" },
  			{ nombre : 'RetirarOfertasOrdenes', id : "107" },
  			{ nombre : 'AnularAsignacionMasiva', id : "109" },
  			{ nombre : 'AnularTransaccionMasiva', id : "113" },
  			{ nombre : 'AnularOrdenMasiva', id : "114" },
  			{ nombre : 'AsignacionCronologicaPorLibro', id : "140" },
  			{ nombre : 'ActivarLibro', id : "150" },
  			{ nombre : 'DesactivarLibro', id : "151" },
  			{ nombre : 'ModificarTransaccion', id : "158" },
  			{ nombre : 'CrearLoteAsignacion', id : "188" },
  			{ nombre : 'CrearGrupo', id : "195" },
  			{ nombre : 'ActualizarGrupoPortafolio', id : "200" },
  			{ nombre : 'CrearLoteOrdenPostrera', id : "204" },
  			{ nombre : 'CrearSimulacionAsignacionPreProm', id : "214" },
  			{ nombre : 'BloqueoDesbloqueoOrden', id : "235" },
  			{ nombre : 'BloqueoDesbloqueoTrasnsaccion', id : "236" },
  			{ nombre : 'CrearAsignacionMasiva', id : "245" },
  			{ nombre : 'ObtenerDatosDesdeArchivoAsignacion', id : "246" },
  			{ nombre : 'CalcularNetoYCobrosMM', id : "252" },
  			{ nombre : 'ObtenerDetalleRefacturacion', id : "296" },
  			{ nombre : 'RefacturarMovimiento', id : "297" },
  			{ nombre : 'CrearMovFacturarAsignacionMM', id : "317" },
  			{ nombre : 'ObtenerDatosDesdeArchivoOPA', id : "337" },
  			{ nombre : 'CalcularMontoYCobrosOrden', id : "20021" },
  			{ nombre : 'CalcularLineaRiesgoOrdenes', id : "1373" },
  			{ nombre : 'CrearSimulacionAsignacionCartera', id : "1412" },
  			{ nombre : 'CalcularExigibilidadOrdenes', id : "1459" },
  			{ nombre : 'CalcularCoberturaOrdenes', id : "1461" },
  			{ nombre : 'ConsultarCtaInversionGarantia', id : "1673" },
  			{ nombre : 'ConsultarCustodiaSimultaneable', id : "1683" },
  			{ nombre : 'ConsultarCustodiaIngresoOrden', id : "1686" },
  			{ nombre : 'CantarOrdenes', id : "1717" },
  			{ nombre : 'RutearOrdenes', id : "1718" },
  			{ nombre : 'ConsultarInformacionEjecutarAsignacionPorHora', id : "1721" },
  			{ nombre : 'EjecutarAsignacionPorHora', id : "1722" },
  			{ nombre : 'CrearCorredorCorresponsal'		, id : "3011" },
  			{ nombre : 'ModificarCorredorCorresponsal'	, id : "3012" },
  			{ nombre : 'ProcesarMensajesNegociacion'	, id : "3062" },
  			{ nombre : 'ConsultarInformacionEnvioFacturacionMasivo', id : "21032" },
  			{ nombre : 'EnvioFacturacionMasivo', id : "21031" },
  			{ nombre : 'CrearOrdenes', id : "20019" },
  			{ nombre : 'ConsultarCustodiaGarantizableSimultanea', id : "3207" },
  			{ nombre : 'ConsultarRequisitosBasicosCrearOrdenes', id : "3525" },
  			{ nombre : 'ConsultarCustodiaGarantizableReportes', id : "4198" },
  			{ nombre : 'PrevisualizarDesasignacion', id : "20008" },
  			{ nombre : 'EjecutarDesasignacion', id : "20009" },
  			{ nombre : 'CrearOrdenesPostreras', id : "20024" },
			{ nombre : 'ConsultarOrdenesConSobregiroCustodia', id : "702168" },
			{ nombre : 'PrepararOrdenesPostrerasJob', id : "702169" },
  			{ nombre : 'validaConsultaContratoYFirmaBlockChain', id : "601132"},
  			{ nombre : 'CrearAsignaciones', id : "20025" },
  			{ nombre : 'CrearAsociaciones', id: "20026"},
  			{ nombre : 'AnularAsociaciones', id: "20027"},
  			{ nombre : 'ConfirmarDesconfirmarOrdenes', id: "20029"},
  			{ nombre : 'ModificarTasaPromedioOrdenes', id: "20030"},
  			{ nombre : 'ModificarOrdenes', id: "20031"},
  			{ nombre : 'EnviarAsignacionesFacturar', id: "20032"},
  			{ nombre : 'CrearSimulacionAsignacion', id : "20044" },
  			{ nombre : 'CargaAsignacionesPostreras', id : "20045" },
  			{ nombre : 'ReservarLiberarTransacciones', id : "20046" },
  			{ nombre : 'PrevisualizarLiberacionMasiva', id : "702184" },
  			{ nombre : 'EjecutarLiberacionMasiva', id : "702185" },
  			{ nombre : 'ConsultarFormaDePagoUltimaOrden', id : "20047" },
  			{ nombre : 'ConsultarPosicion', id : "20049" },  			
  			{ nombre : 'ConsultarComprobantesRefacturacion', id : "20054" },
  			{ nombre : 'ConsultarSaldoTransaccionesRefacturar', id : "20055" },
  			{ nombre : 'CrearTransacciones', id : "20059" },
  			{ nombre : 'ConsultarDemanda', id : "20060" },
  			{ nombre : 'ConsultarDemandaAsignacion', id : "20064" },
  			{ nombre : 'ConsultarPosicionAsignacion', id : "20063" },
  			{ nombre : 'InscribirProcesoBatch', id : "20043" },
  			{ nombre : 'AnticiparOrden', id : "100019" }, 
  			{ nombre : 'ResumenOperacionesDiarias', id:"600009"},
  			{ nombre : 'ConsultarCtaCustodioDefectoPortafolio', id : "600018" },
  			{ nombre : 'ConsultarAsignaciones', id:"1200"}, 
  			{ nombre : 'ConsultarOrdenesPorNegociar', id:"1203"},
  			{ nombre : 'ConsultaOrdenesEnNegociacion', id:"13041"},
  			{ nombre : 'AprobarRechazarSolicitudAnulacionOrdenes', id:"20075"},
  			{ nombre : 'ReprocesarTransaccion', id:"700026"},
  			{ nombre : 'ConsultarClientesLibroOrdenes', id:"700163"},  
  			{ nombre : 'AsignacionMasiva',id:"20044"},
  			{ nombre : 'ValidacionCalce', id:"700162"},
  			{ nombre : 'EnvioAFacturar', id:"20032"},
  			{ nombre : 'ConsultarInformacionPortafolioOrdenes', id:"700173"},
  			{ nombre : 'CrearOrdenesMasiva', id:"700180"},
  			{ nombre : 'EliminarCargaMasiva', id:"2004"},
  			{ nombre : 'CrearCargaMasiva', id:"2005"},
  			{ nombre : 'ProcesarCargaMasiva', id:"2007"},
  			{ nombre : 'AsignacionMasivaPorArchivo', id:"700185"},
			{ nombre : 'CrearInstrucciones', id:"700325"},
			{ nombre : 'ConfirmarInstruccionMasivo', id:"700334"},
			{ nombre : 'LiquidarInstruccion', id:"700340"},
			{ nombre : 'RechazarInstruccionMasivo', id:"700335"},
			{ nombre : 'AdjuntarDocumentoInstruccion', id:"700341"},
			{ nombre : 'ObtenerDocumentoInstruccion', id:"700351"},
			{ nombre : 'BorrarDocumentoInstruccion', id:"700352"},
			{ nombre : 'AnularInstruccionMasivo', id: "700337"},
			{ nombre : 'AceptaCancelacionInstruccionMasivo', id: "700383"},
			{ nombre : 'RechazaCancelacionInstruccionMasivo', id: "700384"},
			{ nombre : 'consultaIndicadoresSP', id: "275"},
			{ nombre : 'GuardarConfiguracionAsignacionDirecta', id: "1900"},
			{ nombre : 'saveConfiguracionShortName', id: "1903"},
			{ nombre : 'updateConfiguracionShortName', id: "1904"},
			{ nombre : 'ConsultarNemoNegociacion', id: "70000000"},
			{ nombre : 'ConsultarTransaccionesAsignacionPrecioPromedio', id: "702180"},
			{ nombre : 'AjustarTipoCambioAsignacion', id: "101010384" }
  		];
					
 		return $resource("", {},generarResource());
	}]);


angular.module('ModuloOrdenes').factory('bcsServicioOrdenV2',function($resource,bcsComunes) {
	var obj = {};
	
	obj.transaccionesEstanCombinadas	= function(transacciones, parametros) {
		var fechaOperacion = _.groupBy(transacciones, function(num){ return util.dateSPdate(num.FechaOperacionTransaccion); });
		if (Object.keys(fechaOperacion).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_FECHA");
			return true;
		}
		var libroOrdenes = _.groupBy(transacciones, function(num){ return num.IdLibroOrdenes; });
		if (Object.keys(libroOrdenes).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_LIBRO_ORDENES");
			return true;
		}
		/*
		var clasificacion = _.groupBy(transacciones, function(num){ return num.IdClasificacionTipoInstrumento; });
		if (Object.keys(clasificacion).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_CLASIFICACION");
			return true;
		}
		var tipoOperacion = _.groupBy(transacciones, function(num){ return num.IdTipoOperacion; });
		if (Object.keys(tipoOperacion).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_TIPO_OPERACION");
			return true;
		}
		/*if (tipoOperacion['COMSIM'] || tipoOperacion['VENSIM'] ) {
			var subyacente = _.groupBy(transacciones, function(num){ return num.IdInstrumentoSubyacente; });
			if (Object.keys(subyacente).length > 1) {
				bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_SUBYACENTE");
				return true;
			}
		}
		var bolsa = _.groupBy(transacciones, function(num){ return num.IdBolsa; });
		if (Object.keys(bolsa).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_BOLSA");
			return true;
		}
		var ventaCorta = _.groupBy(transacciones, function(num){ return num.EsVentaCorta; });
		if (Object.keys(ventaCorta).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_VENTA_CORTA");
			return true;
		}*/
		
		var condLiq = _.groupBy(transacciones, function(num){ return num.IdCondicionDeLiquidacion; });
		if (Object.keys(condLiq).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_CONDICION_LIQUIDACION");
			return true;
		}
		return false;
	};
	
	obj.ConsultarTasa = obj.ObtenerLimitesRangosMontoPlazo = function(filtros)
	{
		var filters = new Array();
		if (filtros.IdLimite)
			filters.push({ field: "IdLimite", operator: "eq", value: Number(filtros.IdLimite) });
		if (filtros.IdTipoLimite)
			filters.push({ field: "IdTipoLimite", operator: "eq", value: filtros.IdTipoLimite });
		if (filtros.IdTipoOperacion)
			filters.push({ field: "IdTipoOperacion", operator: "eq", value: filtros.IdTipoOperacion });
		if (filtros.IdTipoInstrumento)
			filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.IdTipoInstrumento });
		if (filtros.IdInstrumento)
			filters.push({ field: "IdInstrumento", operator: "eq", value: filtros.IdInstrumento });
		if (filtros.PlazoDesde)
			filters.push({ field: "PlazoDesde", operator: "lte", value: filtros.PlazoDesde });
		if (filtros.MontoDesde)
			filters.push({ field: "MontoDesde", operator: "lte", value: filtros.MontoDesde });
		
		var margenDatasource = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			serverSorting:true,
    		transport: {
    			read: {
    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1551",
    				dataType: "json"
    		    }
    		},
    		sort: [
				  	{ field: "PlazoHasta", dir: "asc" },
				  	{ field: "MontoHasta", dir: "asc" }
				  ],
			filter: filters//,
		});
		
		return margenDatasource.query().then(function(){
			var fila = -1;
			var filaSel = -1;
			
			if (margenDatasource.data().length == 0) {
				console.log("Limite no configurado");
				return false;
			}
			else
			{
				var data = [];
				
				$.each(margenDatasource.data(),function(key,value){
					 /*obj2 = {"MontoHasta": Number(value.MontoHasta),
							 	 "PlazoHasta": Number(value.PlazoHasta),
							 	 "Tasa": Number(value.Tasa)};*/
					 data.push(value);
				});
				
			return {
					"data": data
				};
			}
		});
	};
	
	
	return obj;
});
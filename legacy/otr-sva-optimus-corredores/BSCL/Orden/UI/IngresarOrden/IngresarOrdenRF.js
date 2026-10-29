/***********************************************
 * Purpose	: Ingresar Orden Renta Fija (PERU)
 * Author	: Jgarces
 * Date		: 17/05/2017
 ***********************************************/
(function(){
	angular.module('ModuloOrdenes').controller('IngresarOrdenRFCtrl', 
		function($http,$scope,bcsServicioReportes,bcsServiciosPlataformaMercado,$translate,bcsComunes,
				bcsServicioOrden,bcsServicioPlataforma,$timeout,bcsConfiguracionOrdenes,$interval,bcsConsultasOrdenes){
		/*********************
		 * VARIABLES
		 *********************/
   		console.log('%cINICIA CONTROLLER: IngresarOrdenRFCtrl','background:#222;color:#bada55;font-size:16px');
		var ordenRF 			= this;
		var controllerName 		= 'IngresarOrdenRFCtrl';
		var idVentana 			= bcsComunes.ventanaWinId($scope);
		ordenRF.idVentana 		= bcsComunes.ventanaWinId($scope); 
		ordenRF.datos 			= {}; //datos de la orden.
		ordenRF.datosDef 		= {}; //datos de la orden por defecto
		ordenRF.datosConfig		= {}; //configuracion de datos como decimales,simbolo,etc.
		ordenRF.datosResumen	= {}; //el resumen de la operacion
		ordenRF.seleccionado 	= {}; //objetos seleccionados del formulario.
		ordenRF.filtros 		= {}; //filtros en el formulario.
		ordenRF.activado 		= {}; //activar o no combos.
		ordenRF.win 			= {}; //funciones y variables de comportamiento pantalla.
		ordenRF.parametros 		= bcsComunes.ventanaParams(idVentana);
		var parmorden 			= undefined;
		ordenRF.habilitarBoton 	= true;
		
		if(ordenRF.parametros&&ordenRF.parametros.orden){
			var parmorden = ordenRF.parametros.orden;
			console.info("RECIBO PARAMETROS");
		}
		
		bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
			if (bcsConfiguracionOrdenes.CONFIGURACION.VigenciaDefectoOrdenRF) {
				ordenRF.datosDef.idTipoVigencia = bcsConfiguracionOrdenes.CONFIGURACION.VigenciaDefectoOrdenRF;
			}
		});

		var initDatos = function(){
			ordenRF.datos.idPortafolio 				= undefined;
			ordenRF.datos.idAutorizadoDarOrdenes 	= undefined;
			ordenRF.datos.idMecanismoDeCaptacion 	= undefined;
			ordenRF.datos.idLibroOrdenes 			= undefined;
			ordenRF.datos.idOperacion 				= undefined;
			ordenRF.datos.instrumentoBuscar 		= undefined; //directiva instrumento
			ordenRF.datos.idBolsaDestino 			= undefined;
			ordenRF.datos.idSistemaBursatil 		= undefined; //falta combo
			ordenRF.datos.cantidad 					= undefined;
			ordenRF.datos.corteMinimo 				= undefined;
			ordenRF.datos.valorNominal 				= undefined;
			ordenRF.datos.precio 					= undefined;
			ordenRF.datos.tir 						= undefined;
			ordenRF.datos.tir360 					= undefined;
			ordenRF.datos.idCondicionLiquidacion 	= undefined;
			ordenRF.datos.fechaLiquidacion 			= undefined;
			ordenRF.datos.idTipoPrecio 				= 'L';
			ordenRF.datos.idTipoVigencia 			= bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy;
			ordenRF.datos.fechaVigencia 			= undefined;
			ordenRF.datos.idMonedaLiquidacion 		= undefined;
			ordenRF.datos.valorAllin				= undefined;
			//datos de valorizacion
			ordenRF.datos.tirMedia 					= '';
			ordenRF.datos.precioMedio 				= '';
			ordenRF.datos.duration 					= '';
			ordenRF.datos.valorTieneCall 			= '';
			ordenRF.datos.tirEmision 				= '';
			ordenRF.datos.clasifRiesgo 				= '';
			ordenRF.datos.duracion 					= '';
			ordenRF.datos.convexidad 				= '';
			if ($scope.SesionLocale === 'PE') {
				ordenRF.datos.esVentaCorta 				= false;
			} else {
				ordenRF.datos.esVentaCorta 				= true;
			}
			
			//
			ordenRF.datos.valorValorizado 			= undefined;
			ordenRF.datos.interesesCorridos 		= undefined;
			ordenRF.datos.reajustesVacCorrido		= undefined;
			ordenRF.datos.valorActualizado 			= undefined;
			ordenRF.datos.precioSucio 				= undefined;
			ordenRF.datos.tirNeta 					= undefined;
			//saldos
			
			//ordenRF.datos.fechaLiquidacion 		= undefined;
			ordenRF.datos.idTipoCanalDistribucion 	= undefined;
			ordenRF.datos.fechaRecepcion 			= undefined;
			ordenRF.datos.comentario 				= undefined;
			ordenRF.datos.tipoCambio 				= undefined;
			ordenRF.datos.paridadReferencia 		= undefined;
			ordenRF.datos.idPorcentajeVisibilidad 	= undefined;
			ordenRF.datos.idCorredorCorresponsal 	= undefined;
			ordenRF.datos.idBoveda 					= undefined;
			ordenRF.datos.idFormaCustodia 			= undefined;
			ordenRF.datos.esOrdenEspecial 			= false;
			//			
			ordenRF.datos.montoOperacionCalculado 	= 0;
			ordenRF.datos.cantidadCalculada 		= 0;
			ordenRF.datos.totalCobros 				= 0;
			ordenRF.datos.totalOrden 				= 0;
			ordenRF.win.modo 						= 'INGRESO';
			ordenRF.win.muestraCobros 				= false;
			ordenRF.win.calculandoAlgo 				= false;
			ordenRF.win.habilitaVC 					= false;
			ordenRF.datos.showVentaCorta 			= true;
			ordenRF.datos.pagoCobroCuentaInversion 	= true;
			//DATOS DEFAULT:
			ordenRF.datosDef.idPortafolio 					= undefined;
			ordenRF.datosDef.idAutorizadoDarOrdenes 		= undefined;
			ordenRF.datosDef.idMecanismoDeCaptacion 		= undefined;
			ordenRF.datosDef.idLibroOrdenes	 				= undefined;
			if ($scope.SesionLocale === 'PE') {
				ordenRF.datosDef.idOperacion 					= 'COMPRA';
			} else {
				ordenRF.datosDef.idOperacion 					= 'VENTA';
			}
			ordenRF.datosDef.idBolsaDestino 				= undefined;
			ordenRF.datosDef.idSistemaBursatil 				= undefined;
			ordenRF.datosDef.idTipoPrecio 					= 'L';
			ordenRF.datosDef.idTipoVigencia 				= bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy;
			ordenRF.datosDef.idMonedaLiquidacion 			= undefined;
			ordenRF.datosDef.idClasificacionTipoInstrumento = undefined;
			ordenRF.datosDef.idBolsa 						= undefined;
			ordenRF.datosDef.validaFlujoCustodia 			= undefined;
		};
		
		var initDatosResumen = function initDatosResumen(){
			ordenRF.datosResumen.fechaOperacionOrden 		= '';
			ordenRF.datosResumen.secuenciaOrden 			= '';
			ordenRF.datosResumen.operacion 					= '';
			ordenRF.datosResumen.instrumento 				= '';
			ordenRF.datosResumen.idMonedaOrden 				= '';
			ordenRF.datosResumen.decimalesPrecio 			= '';
			ordenRF.datosResumen.precio 					= '';
			ordenRF.datosResumen.tipoPrecio 				= '';
			ordenRF.datosResumen.decimalesCantidad 			= '';
			ordenRF.datosResumen.cantidad 					= '';
			ordenRF.datosResumen.idMoneda 					= '';
			ordenRF.datosResumen.decimales 					= '';
			ordenRF.datosResumen.montoOperacion 			= '';
			ordenRF.datosResumen.totalCobros 				= '';
			ordenRF.datosResumen.montoTotal 				= '';
			ordenRF.datosResumen.nombreEjecutivoPortafolio 	= '';
		};
		
		var initDatosContraorden = function initDatosContraorden(){
			bcsComunes.ventanaOverlay(idVentana,true);//para pintar datos
			ordenRF.datos.precio = parmorden.PrecioOrden;
			ordenRF.datos.tir = parmorden.TasaRFIF;
			console.info(JSON.stringify(parmorden));
			ordenRF.datos.esOrdenEspecial = false;
			ordenRF.datos.idPortafolio = 0;
			ordenRF.datosDef.idPortafolio = 0;
			ordenRF.datos.idAutorizadoDarOrdenes = parmorden.IdPersonaAutorizadoDarOrdenes;
			ordenRF.datosDef.idAutorizadoDarOrdenes = parmorden.IdPersonaAutorizadoDarOrdenes;
			if (parmorden.AccionOrden === 'COMPRA') {
				ordenRF.datosDef.idOperacion='VENTA';
				ordenRF.datos.idOperacion='VENTA';
			}else{
				ordenRF.datosDef.idOperacion='COMPRA';
				ordenRF.datos.idOperacion='COMPRA';
			}
			ordenRF.datos.instrumentoBuscar = parmorden.Instrumento;
			ordenRF.datos.idBolsaDestino = parmorden.IdBolsa;
			ordenRF.datosDef.idBolsaDestino = parmorden.IdBolsa;
			ordenRF.datos.cantidad = parmorden.CantidadOrden;
			//ordenRF.datos.corteMinimo//Definido via watch
			ordenRF.datos.valorNominal//Definido via watch
			ordenRF.datos.idCondicionLiquidacion = parmorden.IdCondicionDeLiquidacion;
			ordenRF.datos.idTipoPrecio = parmorden.IdTipoPrecio;
			ordenRF.datosDef.idTipoPrecio = parmorden.IdTipoPrecio;
			ordenRF.datos.idTipoVigencia = parmorden.IdTipoVigenciaOrden;
			ordenRF.datosDef.idTipoVigencia = parmorden.IdTipoVigenciaOrden;
			if(parmorden.IdTipoVigenciaOrden=='FF')ordenRF.datos.fechaVigencia=parmorden.FechaVigencia;
			ordenRF.datos.idMonedaLiquidacion=parmorden.IdMonedaLiquidacionOrden;
			ordenRF.datosDef.idMonedaLiquidacion=parmorden.IdMonedaLiquidacionOrden;
			var tiempoEspera = 0;
			var intervalo = $interval(function(){
				tiempoEspera=tiempoEspera+200;
				if(ordenRF.datos.precio!=undefined&&ordenRF.datos.precio!=null&&ordenRF.datos.precio!=''
				&& ordenRF.datos.tir!=undefined&&ordenRF.datos.tir!=null&&ordenRF.datos.tir!=''){
					console.log("valorizar a tasa una vez cargados los datos. Tiempo de espera carga datos: "+tiempoEspera);
					ordenRF.win.valorizar("TASA");
					$interval.cancel(intervalo);
				} 
				bcsComunes.ventanaOverlay(idVentana,false);//para pintar datos
			},200); 
		};
		
		var initDatosConfig = function initDatosConfig(){
			//decimales
			ordenRF.datosConfig.cantidadDecimales 			= 0;
			ordenRF.datosConfig.corteMinimoDecimales 		= 0;
			ordenRF.datosConfig.valorNominalDecimales 		= 0;
			ordenRF.datosConfig.precioDecimales 			= 0;
			ordenRF.datosConfig.tirDecimales 				= 0;
			ordenRF.datosConfig.valorValorizadoDecimales 	= 0;
			ordenRF.datosConfig.interesesCorridosDecimales 	= 0;
			ordenRF.datosConfig.reajustesVacCorridoDecimales= 0;
			ordenRF.datosConfig.valorActualizadoDecimales 	= 0;
			ordenRF.datosConfig.precioSucioDecimales 		= 0;
			ordenRF.datosConfig.tirNetaDecimales 			= 0;
			ordenRF.datosConfig.tipoCambioDecimales 		= 0;
			//simbolo
			ordenRF.datosConfig.cantidadSimbolo 			= undefined;
			ordenRF.datosConfig.corteMinimoSimbolo 			= undefined;
			ordenRF.datosConfig.valorNominalSimbolo 		= undefined;
			ordenRF.datosConfig.precioSimbolo 				= undefined;
			ordenRF.datosConfig.tirSimbolo 					= undefined;
			ordenRF.datosConfig.valorValorizadoSimbolo 		= undefined;
			ordenRF.datosConfig.interesesCorridosSimbolo 	= undefined;
			ordenRF.datosConfig.reajustesVacCorridoSimbolo 	= undefined;
			ordenRF.datosConfig.valorActualizadoSimbolo 	= undefined;
			ordenRF.datosConfig.precioSucioSimbolo 			= undefined;
			ordenRF.datosConfig.tirNetaSimbolo 				= undefined;
			ordenRF.datosConfig.tipoCambioSimbolo 			= 'CLP';
			//otros
			ordenRF.datosConfig.fechaInicial 				= $scope.SesionFechaSistema;
			
		};
		
		var initSeleccionado = function(){
			ordenRF.seleccionado.portafolio 			= undefined;
			ordenRF.seleccionado.autorizadoDarOrdenes 	= undefined;
			ordenRF.seleccionado.mecanismoDeCaptacion 	= undefined;
			ordenRF.seleccionado.libroOrdenes 			= undefined;
			ordenRF.seleccionado.operacion 				= undefined;
			ordenRF.seleccionado.tipoOperacion 			= undefined;
			ordenRF.seleccionado.tipoInstrumento 		= undefined;
			ordenRF.seleccionado.bolsaDestino 			= undefined;
			ordenRF.seleccionado.sistemaBursatil 		= undefined;
			ordenRF.seleccionado.condicionLiquidacion 	= undefined;
			ordenRF.seleccionado.tipoPrecio 			= undefined;
			ordenRF.seleccionado.tipoVigencia 			= undefined;
			ordenRF.seleccionado.monedaLiquidacion 		= undefined;
			ordenRF.seleccionado.canalDistribucion 		= undefined;
			ordenRF.seleccionado.porcentajeVisibilidad 	= undefined;
			ordenRF.seleccionado.corredorCorresponsal 	= undefined;
			ordenRF.seleccionado.boveda 				= undefined;
			ordenRF.seleccionado.formaCustodia 			= undefined;
		};
		
		/*********************
		 * COMBOBOX
		 *********************/
			var initFiltros = function(){
				ordenRF.filtros.portafolio = {
					IdEmpresa:{"field":"IdEmpresa","operator":"eq","value":$scope.$root.SesionIdEmpresa},
					IdCliente:{"field":"IdCliente","operator":"eq","value":0}
				};
				ordenRF.filtros.autorizadoDarOrdenes = {
					IdPortafolio:{"field":"IdPortafolio","operator":"eq","value":-1},
					IdEstado:{"field":"IdEstado","operator":"eq","value":'ACT'}
				};
				ordenRF.filtros.mecanismoDeCaptacion = {
					filtros:{
						CodigoTipoIndicador:{"field":"CodigoTipoIndicador","operator":"eq","value":"ORDMODALIDAD"},
						IdEntidad:{"field":"IdEntidad","operator":"eq","value": "" }
					},
					itemPostFiltro : '1'
				};
				ordenRF.filtros.libroOrdenes = {
					IdEmpresa:{"field":"IdEmpresa","operator":"eq","value":$scope.$root.SesionIdEmpresa}
				};
				ordenRF.filtros.operacion = ($scope.SesionLocale === 'PE') ? {
					AccionOrden:{"field":"NombreAccionOrden"}
				}:{
					AccionOrden:{"field":"NombreAccionOrden","operator":"eq","value": "VENTA"}
				};
				ordenRF.filtros.boveda = {
						IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
				};
				
				ordenRF.filtros.instrumento = {
					clasificacion:['RF',],
					//tipoInstrumento:['ACN',],
					//bolsa:[]?
				};
				ordenRF.filtros.bolsaDestino = {
					IdInstrumento:{"field":"IdInstrumento","operator": "eq", "value"   : 0 }
				};
				ordenRF.filtros.sistemaBursatil = {
					IdEmpresa:{"field":"IdEmpresa","operator":"eq","value":$scope.$root.SesionIdEmpresa},
					IdClasificacionTipoInstrumento:{"field":"IdClasificacionTipoInstrumento","operator":"eq","value":'RF'},
				}; 
				ordenRF.filtros.tipoPrecio = {
					IdEstado:{"field":"IdEstado","operator":"eq","value":"ACT"}
				};
				ordenRF.filtros.tipoVigencia = {
					IdEstado:{field:"IdEstado",operator:"eq",value:"ACT"},
					IdTipoVigenciaOrden:{field:"IdTipoVigenciaOrden",operator:"neq",value:"OP"}
				};
				ordenRF.filtros.monedaLiquidacion = {
					IdTipoInstrumento:{"field":"IdTipoInstrumento","operator":"eq","value":""},
					IdMonedaInstrumento:{"field":"IdMoneda","operator":"eq","value":""}
				};
				ordenRF.filtros.canalDistribucion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-canal-distribucion');
				
				ordenRF.filtros.porcentajeVisibilidad = {
					IdEstado:{"field":"IdEstado","operator":"eq","value":"ACT"}
				};
				ordenRF.filtros.corredorCorresponsal = ($scope.SesionLocale === 'PE') ? {
					IdEmpresa:{"field":"IdEmpresa","operator":"eq","value":$scope.SesionIdEmpresa},
				} : {
					IdPais:{"field":"IdPais","operator":"eq","value":""},
					IdEmpresa	: { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa }
				};
				ordenRF.filtros.cuentaCustodio = ($scope.SesionLocale === 'PE')?{
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
					IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" },
					//IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value"   : "" }
				}:{
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
					IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					//IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" }
				};
				ordenRF.filtros.formaCustodia = {
					IdEstado:{"field":"IdEstado","operator":"eq","value":"ACT"}
				};
				ordenRF.filtros.preferenciaPagoPortafolio = {
					IdMedioDePago 	:{field:"IdMedioDePago",operator:"neq",value:"CIN"},
					IdPortafolio 	:{field:"IdPortafolio",operator:"eq",value:""},
					IdMoneda 		:{field:"IdMoneda",operator:"eq",value:""},
					IdTipoOperacion :{field:"IdTipoOperacion",operator:"eq",value:""}
				};
			};
			initFiltros();
			
	
		
		
		
		if ($scope.SesionLocale === 'PE') {
			ordenRF.filtros.boveda.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value"   :  "" };
		}
		
		/*OPTIMUSPE-1215*/
		var defCorresponsalBoveda = new bcs.DataSource({
			type: "odata",
			definition 	: {
				serverFiltering : true,
				serverPaging 	: true,
				serverSorting 	: true,
				schema 	: {
					model	: {
	                    fields	: {
	                    	IdCorredorCorresponsal	: { type: "number"},
	                    	IdTipoProducto			: { type: "string"},
	                    	NombreTipoProducto		: { type: "string"},
	                    	IdBoveda				: { type: "string"}
	                    }
	           		}
				},
				transport : {
					read : {
						url :bcsConsultasOrdenes('ConsultarProductoBovedaCorredorCorresponsal'),
						dataType : "json"
					}
				}
			}	
		});
		
		/*********************
		 * FUNCIONES INTERNAS
		 *********************/	
		var setConfigDecimales = function(varcambio,instrumento){
			console.info('fn:setConfigDecimales,watch:'+varcambio);
			var instrumento 		= ordenRF.datos.instrumento;
			var decimalesCANTIDAD 	= util.bcs_formato(0,'GEN','Cantidad',instrumento.IdMoneda,instrumento.IdTipoInstrumento,'').cantidadDecimales;
			var decimalesPRECIO 	= util.bcs_formato(0,'GEN','Precio',instrumento.IdMoneda,instrumento.IdTipoInstrumento,'').cantidadDecimales;
			var decimalesMONTO 		= util.bcs_formato(0,'GEN','Monto',instrumento.IdMoneda,instrumento.IdTipoInstrumento,'').cantidadDecimales;
			var decimalesTIPOCAMBIO = util.bcs_formato(0,'GEN','TipoCambio',instrumento.IdMoneda,instrumento.IdTipoInstrumento,'').cantidadDecimales;
			var decimalesTASA 		= util.bcs_formato(0,'GEN','Tasa',instrumento.IdMoneda,instrumento.IdTipoInstrumento,'').cantidadDecimales;
			ordenRF.datosConfig.cantidadDecimales 				= decimalesCANTIDAD;
			ordenRF.datosConfig.corteMinimoDecimales 			= 0;
			ordenRF.datosConfig.valorNominalDecimales 			= decimalesMONTO;
			ordenRF.datosConfig.precioDecimales 				= decimalesPRECIO;
			ordenRF.datosConfig.tirDecimales 					= decimalesTASA;
			ordenRF.datosConfig.valorValorizadoDecimales 		= decimalesMONTO;
			ordenRF.datosConfig.interesesCorridosDecimales 		= decimalesMONTO;
			ordenRF.datosConfig.reajustesVacCorridoDecimales 	= decimalesMONTO;
			ordenRF.datosConfig.valorActualizadoDecimales 		= decimalesMONTO;
			ordenRF.datosConfig.precioSucioDecimales 			= decimalesPRECIO;
			ordenRF.datosConfig.tirNetaDecimales 				= decimalesTASA;
			ordenRF.datosConfig.tipoCambioDecimales 			= decimalesTIPOCAMBIO;
		};

		var setConfigSimbolo = function(){
			if(ordenRF.datos.instrumento&&ordenRF.datos.instrumento.IdInstrumento){
				ordenRF.datosConfig.cantidadSimbolo 			= undefined;
				ordenRF.datosConfig.corteMinimoSimbolo 			= ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosConfig.valorNominalSimbolo 		= ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosConfig.precioSimbolo 				= undefined;
				ordenRF.datosConfig.tirSimbolo 					= undefined;
				ordenRF.datosConfig.valorValorizadoSimbolo 		= ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosConfig.interesesCorridosSimbolo 	= ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosConfig.reajustesVacCorridoSimbolo 	= ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosConfig.valorActualizadoSimbolo 	= ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosConfig.precioSucioSimbolo 			= undefined;
				ordenRF.datosConfig.tirNetaSimbolo 				= undefined;
				ordenRF.datosConfig.tipoCambioSimbolo 			= ordenRF.datos.instrumento.IdMoneda;
			}else{
				ordenRF.datosConfig.cantidadSimbolo 			= undefined;
				ordenRF.datosConfig.corteMinimoSimbolo 			= undefined;
				ordenRF.datosConfig.valorNominalSimbolo 		= undefined;
				ordenRF.datosConfig.precioSimbolo 				= undefined;
				ordenRF.datosConfig.tirSimbolo 					= undefined;
				ordenRF.datosConfig.valorValorizadoSimbolo 		= undefined;
				ordenRF.datosConfig.interesesCorridosSimbolo 	= undefined;
				ordenRF.datosConfig.reajustesVacCorridoSimbolo 	= undefined;
				ordenRF.datosConfig.valorActualizadoSimbolo 	= undefined;
				ordenRF.datosConfig.precioSucioSimbolo 			= undefined;
				ordenRF.datosConfig.tirNetaSimbolo 				= undefined;
				ordenRF.datosConfig.tipoCambioSimbolo 			= undefined;
			}
		};
		
		var consultarRequisitosBasicosCrearOrden = function(varcambio){
			console.info('fn:consultarRequisitosBasicosCrearOrden,watch:'+varcambio);
			if(ordenRF.datos.instrumento&&ordenRF.datos.idPortafolio){
				var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoOperacion	: ordenRF.datos.idTipoOperacion,
					P_IdPortafolio		: ordenRF.datos.idPortafolio
				};
				bcsServicioOrden.ConsultarRequisitosBasicosCrearOrdenes(datos).$promise.then(function(result){
					if (result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.ventanaMsg({
							conflictos : util.parseServerResponse(result)
						},idVentana);
					}
				},function(error){
					console.error("error al ConsultarRequisitosBasicosCrearOrdenes");
				});
			}
		};
		
		var validaFlujoCustodia = function(varcambio){
			console.info('fn:validaFlujoCustodia,watch:'+varcambio);
			if (ordenRF.datos.idPortafolio>0&&ordenRF.datos.idOperacion==='VENTA'&&ordenRF.datos.idFormaCustodia==='CUS'){
				ordenRF.datosDef.validaFlujoCustodia = 'S';
			}else {
				ordenRF.datosDef.validaFlujoCustodia = 'N';
			}
		};

		var setOperacionesCobros = function(varcambio){
			console.log('fn:setOperacionesCobros,watch:'+varcambio);
			if (ordenRF.datos.instrumento&&ordenRF.datos.instrumento.IdInstrumento>0&&ordenRF.datos.idMonedaLiquidacion&&ordenRF.datos.idPortafolio
				&&ordenRF.datos.idTipoOperacion&&ordenRF.datos.idBolsaDestino&&ordenRF.seleccionado.tipoOperacion&&ordenRF.datos.valorValorizado!=undefined){
				var operacion = [{
					IdInstrumento  			: ordenRF.datos.instrumento.IdInstrumento,
					FechaOperacion 			: $scope.SesionFechaSistema,
					CantidadOrden   		: ordenRF.datos.valorNominal || 0,
					PrecioOrden   			: ordenRF.datos.precio || 0,
					MontoOrden				: ordenRF.datos.valorActualizado || 0,
					IdMonedaLiquidacionOrden: ordenRF.datos.idMonedaLiquidacion,
					IdPortafolio 			: ordenRF.datos.idPortafolio,
					IdTipoProducto 			: ordenRF.seleccionado.tipoOperacion.IdTipoProducto,
					IdTipoOperacion 		: ordenRF.datos.idTipoOperacion,
					IdCanalIngreso 			: $scope.SesionTipoCanalIngreso,
					IdBolsa 				: ordenRF.datos.idBolsaDestino,
					IdCorredorCorresponsal	: ordenRF.datos.idCorredorCorresponsal,
				}];
				ordenRF.datos.operaciones = operacion;
			}else{
				ordenRF.datos.operaciones = [];
			}
		};
				
		var redimensionarVentana = function(varcambio){
			console.log('fn:redimensionarVentana,watch:'+varcambio);
			$timeout(function(){bcsComunes.ventanaTamanoAutomatico(idVentana);},10);
		}
		
		var obtenerParidades = function(varcambio){
			console.log('fn:obtenerParidades,watch:'+varcambio);
			if (!ordenRF.datos.instrumento || !ordenRF.datos.instrumento.IdMoneda || !ordenRF.datos.monedaLiquidacionOrdenSeleccionada 
					|| !ordenRF.datos.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion){
				return;
			}
			var idMonedaOrigen = ordenRF.datos.instrumento.IdMoneda;
			var idMonedaDestino = ordenRF.datos.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion;
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdTrabajo : 0,
				p_CalculosEntrada : [{
					IdTrabajo 		: 0,
					IdInterno 		: 0,
					Fecha 			: $scope.SesionFechaSistema,
					IdMonedaOrigen 	: idMonedaOrigen,
					IdMonedaDestino : idMonedaDestino,
					ValorCambioLocalModificado : 0,
					MontoOrigen 	: 1,
					IdUso 			: 'ORDENES'
				}]
			};
			var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
			promise.then(function(result){
				if (result.p_out_CalculosSalida[0].Resultado === 'RECHAZO'){
					ordenRF.datosForm.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
				}else{
					ordenRF.datosForm.paridadReferencia = idMonedaOrigen + " - " + idMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				}	 
			});		
		};
		
		var obtieneFechaVigencia = function(varcambio){
			console.log('fn:obtieneFechaVigencia,watch:'+varcambio);
			var tv = ordenRF.seleccionado.tipoVigencia;
			if (tv&&tv.IdTipoVigenciaOrden){
				if(tv.IdTipoVigenciaOrden ===  bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy){
					ordenRF.datos.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				}else if(tv.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaPermanente){
					ordenRF.datos.fechaVigencia = util.dateDS("0001-01-01","FechaSistema","CALL");
				}else if(tv.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaFechaFija){
					ordenRF.datos.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				}else if(tv.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSemanal){
					var datos = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo : 'ORD',
						p_UsaReglasNegocio : 'S',
						p_Fecha : $scope.SesionFechaSistema,
						p_NumeroDias : 5,
						p_EsHabil : 'S',
						p_IdPais : $scope.SesionIdPaisSistema
					};
					//PASAR A VARIABLE DEL ROOT
					bcsServicioPlataforma.FechaSiguiente(datos,function(result){
						ordenRF.datos.fechaVigencia = util.dateDS(result.P_OUT_FECHASIGUIENTE,"FechaSistema","CALL");
					}
					,function(result){console.log(result);});
				}else {
					ordenRF.datos.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				}
				calcularPlazos(ordenRF.datos.fechaVigencia);
			}
		};

		var calcularPlazos = function(fechaVigencia){
			console.log(fechaVigencia);
		}
		
		var setPrecio = function(varcambio){
			console.log('fn:setPrecio,watch:'+varcambio);
			if (ordenRF.datos.instrumento && ordenRF.datos.tipoPrecioSeleccionado){
				if (ordenRF.datos.tipoPrecioSeleccionado.IdTipoPrecioOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado && ordenRF.datos.instrumento.PrecioCierre){
					ordenRF.datos.precio = Number(ordenRF.datos.instrumento.PrecioCierre);
				}
			}
		};
		
		var setIdTipoOperacion = function(varcambio){
			console.log('fn:setIdTipoOperacion,watch:'+varcambio);
			if(ordenRF.datos.instrumento&&ordenRF.datos.instrumento.IdInstrumento&&ordenRF.datos.idOperacion){
				
				var tipoOperacion = undefined;
				
				$.each(BCS_OPERACION_INSTRUMENTO,function(key,value){
			
					if (value.IdTipoInstrumento === ordenRF.datos.instrumento.IdTipoInstrumento && value.AccionOrden === ordenRF.datos.idOperacion) {
						tipoOperacion = value;
						return false;
					}

				})


				if(tipoOperacion === undefined) {
					console.log("error");
					var p=bcsComunes.ventanaMsg($translate.instant('UI_INSTRUMENTO_NO_CONFIGURADO'),idVentana);
					bcsComunes.ventanaPromesa(p).then(function(){
						bcsComunes.ventanaCerrar(idVentana);
					}); 
				}

				ordenRF.seleccionado.tipoOperacion = tipoOperacion;
				if(ordenRF.seleccionado.tipoOperacion&&ordenRF.seleccionado.tipoOperacion.IdTipoOperacion){
					ordenRF.datos.idTipoOperacion = ordenRF.seleccionado.tipoOperacion.IdTipoOperacion;
					console.log("actual idTipoOperacion:"+ordenRF.datos.idTipoOperacion);
				}else ordenRF.datos.idTipoOperacion = undefined;
			}
		};
		
		var setCorteMinimo = function(varcambio){
			console.log('fn:setCorteMinimo,watch:'+varcambio);
			var i=ordenRF.datos.instrumento; //check
			if(i&&i.CorteMinimo!==undefined){
				ordenRF.datos.corteMinimo = (i.CorteMinimo===0)?1:i.CorteMinimo;
			}else{
				ordenRF.datos.corteMinimo = undefined;
			}
		};
		
		var setIdMonedaLiquidacion = function(varcambio){
			console.log('fn:setIdMonedaLiquidacion,watch:'+varcambio);
			var i=ordenRF.datos.instrumento; //check
			if(i&&i.IdMoneda!==undefined){
				ordenRF.datos.idMonedaLiquidacion = i.IdMoneda;
				ordenRF.datosDef.idMonedaLiquidacion = i.IdMoneda;
			}else{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_SIN_MONEDA"));
				ordenRF.datos.instrumento 		= undefined;
				ordenRF.datos.instrumentoBuscar = '-1';
			}
		};
		
		var calculaValorNominal = function(varcambio){
			console.log('fn:calculaValorNominal,watch:'+varcambio);
			if(ordenRF.datos.cantidad&&ordenRF.datos.corteMinimo>0){
				ordenRF.datos.valorNominal = ordenRF.datos.cantidad*ordenRF.datos.corteMinimo;
			}
		}
		var calculaCantidad = function(varcambio){
			if(ordenRF.win.calculandoCantidad)return;
			ordenRF.win.calculandoCantidad=true;
			console.log('fn:calculaCantidad,watch:'+varcambio);
			if(ordenRF.datos.valorNominal&&ordenRF.datos.corteMinimo>0){
				var valorNominal=ordenRF.datos.valorNominal;
				if(valorNominal<ordenRF.datos.corteMinimo){
					valorNominal=ordenRF.datos.corteMinimo;
					console.log("valor nominal menor al corte minimo");
				}
				var moduloValorNominal=valorNominal%ordenRF.datos.corteMinimo;
				if(moduloValorNominal!==0){
					valorNominal=valorNominal-moduloValorNominal;
				}
				ordenRF.datos.valorNominal = valorNominal;
				ordenRF.datos.cantidad = valorNominal/ordenRF.datos.corteMinimo;
			}
			ordenRF.win.calculandoCantidad=false;
		}

		
		var limpiarDatosValorizacion = function(limpiaTodos){
			//---- DATOS DUMMY - A LA ESPERA DE ENTREGA DE INFORMACION EN INSTRUMENTO ---
			ordenRF.datos.tirMedia 				= '';
			ordenRF.datos.precioMedio 			= '';
			ordenRF.datos.duration 				= '';
			ordenRF.datos.valorTieneCall 		= '';
			ordenRF.datos.tirEmision 			= '';
			ordenRF.datos.clasifRiesgo 			= '';
			//-------------------------------------------------------
			ordenRF.datos.duracion 				= undefined;
			ordenRF.datos.convexidad 			= undefined;
			ordenRF.datos.valorValorizado 		= undefined;
			ordenRF.datos.interesesCorridos 	= undefined;
			ordenRF.datos.reajustesVacCorrido 	= undefined;
			ordenRF.datos.valorActualizado 		= undefined;
			//ordenRF.datos.tirNeta 			= undefined;
			ordenRF.datos.precioSucio			= undefined;
			ordenRF.datos.tirNeta				= undefined;
			ordenRF.datos.operaciones			= [];
			ordenRF.datos.operacionesSalida		= [];
			ordenRF.datos.tir360				= undefined;
			
			if (limpiaTodos) {
				ordenRF.datos.tir					= undefined;
				ordenRF.datos.precio				= undefined;	
			}
		}
		
		
		/*********************
		 * WATCH
		 *********************/
		if ($scope.SesionLocale === 'PE') {
		$scope.$watch('ordenRF.datos.valorAllin',function(key,value){
			if (ordenRF.datos.valorAllin === undefined)
				return;
			if (ordenRF.datos.valorAllin < 0)
				return;
			
			ordenRF.datos.apiCobros.calcularAllIn(ordenRF.datos.valorAllin);
		});
		}

		if ($scope.SesionLocale === 'PE') {
			$scope.$watch('ordenRF.datos.idOperacion',function(key,value){
				if (ordenRF.datos.idOperacion === 'VENTA'){
					ordenRF.datos.showVentaCorta = false;
				} else { 
					ordenRF.datos.showVentaCorta = true;
				}
			});
		}
		
		$scope.$watch('ordenRF.datos.cantidad',function(newval,oldval){
			var varcambio='ordenRF.datos.cantidad';
			calculaValorNominal(varcambio);
		});

		$scope.$watch('ordenRF.datos.fechaVigencia',function(newval,oldval){
			var varcambio='ordenRF.datos.fechaVigencia';
			calcularPlazos(varcambio);
		});

		$scope.$watch('ordenRF.datos.corteMinimo',function(newval,oldval){
			var varcambio='ordenRF.datos.corteMinimo';
			calculaValorNominal(varcambio);
		});
		
		$scope.$watch('ordenRF.datos.valorNominal',function(newval,oldval){
			var varcambio='ordenRF.datos.valorNominal';
			calculaCantidad(varcambio);
		});
		
		$scope.$watch('ordenRF.datos.precio',function(newval,oldval){
			var varcambio='ordenRF.datos.valorNominal';
			calculaCantidad(varcambio);
			if(newval){
				setOperacionesCobros(varcambio);
			}
			if(newval!=oldval&ordenRF.cambiandoTasa!=true&&ordenRF.valorizando!=true){
				if(newval!=undefined){
					limpiarDatosValorizacion();
					ordenRF.cambiandoTasa=true;
					ordenRF.datos.tir=undefined;
					ordenRF.datos.tir360=undefined;
					$timeout(function(){ordenRF.cambiandoTasa=undefined;},500);
				}else if(newval==undefined&&ordenRF.datos.tir!=undefined){
					limpiarDatosValorizacion();
				}
			}
		});
		
		$scope.$watch('ordenRF.datos.idCondicionLiquidacion',function(newval,oldval){
			if(newval!=undefined){
				limpiarDatosValorizacion(true);
			}
		});
		
		$scope.$watch('ordenRF.datos.tir',function(newval,oldval){
			var varcambio='ordenRF.datos.tir';
			if(newval!=oldval&ordenRF.cambiandoPrecio!=true&&ordenRF.valorizando!=true){
				if(newval!=undefined){
					limpiarDatosValorizacion();
					ordenRF.cambiandoPrecio=true;
					ordenRF.datos.precio=undefined;
					$timeout(function(){ordenRF.cambiandoPrecio=undefined;},500);
				}else if(newval==undefined&&ordenRF.datos.precio!=undefined){
					limpiarDatosValorizacion();
				}
			}
		});
		
		
		$scope.$watch('ordenRF.datos.valorActualizado',function(newval,oldval){
			var varcambio='ordenRF.datos.valorActualizado';
			setOperacionesCobros(varcambio);
		});
		
		$scope.$watch('ordenRF.datos.instrumento',function(newval,oldval){
			var varcambio='ordenRF.datos.instrumento';
			if(JSON.stringify(newval)===JSON.stringify(oldval))return;
			setIdTipoOperacion(varcambio);
			if (newval&&newval.IdInstrumento){
				ordenRF.filtros.monedaLiquidacion.IdMonedaInstrumento.value=ordenRF.datos.instrumento.IdMoneda;
				ordenRF.filtros.monedaLiquidacion.IdTipoInstrumento.value=ordenRF.datos.instrumento.IdTipoInstrumento;
				ordenRF.filtros.preferenciaPagoPortafolio.IdTipoOperacion.value = ordenRF.datos.idTipoOperacion;
				ordenRF.datos.idMonedaLiquidacion = ordenRF.datos.instrumento.IdMoneda;
				ordenRF.datosDef.idMonedaLiquidacion = ordenRF.datos.instrumento.IdMoneda;
				ordenRF.filtros.bolsaDestino.IdInstrumento.value = ordenRF.datos.instrumento.IdInstrumento;
				setConfigSimbolo();
				setConfigDecimales(varcambio);
				consultarRequisitosBasicosCrearOrden(varcambio);
				setOperacionesCobros(varcambio);
				obtenerParidades(varcambio);
				setCorteMinimo(varcambio);
				setIdMonedaLiquidacion(varcambio);
				FormaNegociacion();
			}
			setPrecio(varcambio);
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenRF.seleccionado.bolsaDestino !== undefined && ordenRF.datos.idBolsaDestino === 'OUT' 
					&& ordenRF.IdBovedaCorresponsal !== undefined && ordenRF.filtros.cuentaCustodio.IdBoveda !== undefined)
				ordenRF.filtros.cuentaCustodio.IdBoveda.value = ordenRF.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/ 
		});
		
		$scope.$watch('ordenRF.datos.idPortafolio',function(newval,oldval){
			var varcambio='ordenRF.datos.idPortafolio';
			if(newval){
				consultarRequisitosBasicosCrearOrden(varcambio);
				validaFlujoCustodia(varcambio);
				setOperacionesCobros(varcambio);
				
				ordenRF.filtros.mecanismoDeCaptacion.filtros.IdEntidad.value = newval;
			}else {
				ordenRF.filtros.mecanismoDeCaptacion.filtros.IdEntidad.value = -1;
			}
		});



		
		$scope.$watch('ordenRF.datos.idPortafolio',function(newval,oldval) {
			if (ordenRF.datos.idPortafolio) {
				
				bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenRF.datos.idPortafolio]).then(function(result){
					var indicadores = result[0];
					if (indicadores.OperaCustodiaRFIF === "S") {
						ordenRF.datos.idFormaCustodia = "CUS";
					}
					else {
						ordenRF.datos.idFormaCustodia = "NOM";
					}
					
					if (indicadores.OperaCuentaInversion === "S") {
						ordenRF.datos.pagoCobroCuentaInversion = true;
						ordenRF.datos.pagoCobroCuentaInversionDisabled = false;
					} else {
						ordenRF.datos.pagoCobroCuentaInversion = false;
						ordenRF.datos.pagoCobroCuentaInversionDisabled = true;
					}
					
					if (result[0].IdPersonaBrokerExtranjero !== undefined && result[0].IdPersonaBrokerExtranjero > 0 ) {
						ordenRF.datos.paraClienteFinalDisabled = false;
						ordenRF.datos.esParaClienteFinal = true;
					} 
					else if (result[0].EsClienteFinal === 'S' ) {
						ordenRF.datos.paraClienteFinalDisabled = true;
						ordenRF.datos.esParaClienteFinal = true;
					}
					else {
						ordenRF.datos.paraClienteFinalDisabled = true;
						ordenRF.datos.esParaClienteFinal = false;
					}
					
				});
				/*OPTIMUSPE-1215*/
				if($scope.SesionLocale === 'PE' && ordenRF.seleccionado.bolsaDestino !== undefined && ordenRF.datos.idBolsaDestino === 'OUT' 
					&& ordenRF.IdBovedaCorresponsal !== undefined && ordenRF.filtros.cuentaCustodio.IdBoveda !== undefined)
					ordenRF.filtros.cuentaCustodio.IdBoveda.value = ordenRF.IdBovedaCorresponsal;
				/*OPTIMUSPE-1215*/  
			}else {
		     	    ordenRF.filtros.mecanismoDeCaptacion.filtros.IdEntidad.value = -1;
		          }
		});
		
		
		
		$scope.$watch('ordenRF.datos.idOperacion',function(newval,oldval){
			var varcambio='ordenRF.datos.idOperacion';
			ordenRF.datos.cantidadInicial = undefined;
			setIdTipoOperacion(varcambio);
			if(newval){
				if (newval==="VENTA"){
					ordenRF.win.habilitaVC = true;
				}else{
					ordenRF.datos.esVentaCorta = undefined;
					ordenRF.win.habilitaVC = false;
				}
				
				consultarRequisitosBasicosCrearOrden(varcambio);
				validaFlujoCustodia(varcambio);
			}
		});
		
		$scope.$watch('ordenRF.datos.idFormaCustodia',function(newval,oldval){
			var varcambio='ordenRF.datos.idFormaCustodia';
			if(newval){
				validaFlujoCustodia(varcambio);
			}
		});
		
		$scope.$watch('ordenRF.datos.idBolsaDestino',function(newval,oldval){
			var varcambio='ordenRF.datos.idBolsaDestino';
			if(newval){
				validaFlujoCustodia(varcambio);
				
				if (ordenRF.datos.idBolsaDestino) {
		   			if ((ordenRF.seleccionado.bolsaDestino != undefined && (ordenRF.seleccionado.bolsaDestino.EsExtranjera === 'N'|| ordenRF.seleccionado.bolsaDestino.UsaBovedaLocal ==='S') &&  ($scope.SesionLocale === 'PE'))) {
		   				ordenRF.filtros.cuentaCustodio.IdModeloCustodia.value = "LOCAL";
		   			}
		   			else if (ordenRF.seleccionado.bolsaDestino != undefined && ordenRF.seleccionado.bolsaDestino.EsExtranjera === 'S'  &&  ($scope.SesionLocale === 'PE')) {
		   				ordenRF.filtros.cuentaCustodio.IdModeloCustodia.value = "EXTERIOR";
		   				/*OPTIMUSPE-1215*/
		   				if($scope.SesionLocale === 'PE' && ordenRF.seleccionado.bolsaDestino !== undefined && ordenRF.datos.idBolsaDestino === 'OUT' 
		   					&& ordenRF.IdBovedaCorresponsal !== undefined && ordenRF.filtros.cuentaCustodio.IdBoveda !== undefined)
		   					ordenRF.filtros.cuentaCustodio.IdBoveda.value = ordenRF.IdBovedaCorresponsal;
		   				/*OPTIMUSPE-1215*/   
		   			}		   			
		   		}
				
				if (ordenRF.seleccionado.bolsaDestino != undefined && ordenRF.seleccionado.bolsaDestino.TieneFormaNegociacion === 'N') {
					ordenRF.datos.idSistemaBursatil = undefined;
					ordenRF.activado.idSistemaBursatil = false;
				} else {
					ordenRF.activado.idSistemaBursatil = true;
					ordenRF.filtros.sistemaBursatil.read = true;
				};
			}
		});
		
		/*$scope.$watch('ordenRF.datos.idCorredorCorresponsal',function(newval,oldval){
			var varcambio='ordenRF.datos.idCorredorCorresponsal';
			if(newval){

			}
		});*/
		
		$scope.$watch('ordenRF.datos.tipoCambio',function(newval,oldval){
			var varcambio='ordenRF.datos.tipoCambio';
			if(newval){
				setOperacionesCobros(varcambio);
			}
		});
		
		$scope.$watch('ordenRF.datos.montoInicial',function(newval,oldval){ //JG: chequear esta variable!
			var varcambio='ordenRF.datos.montoInicial';
			if(newval){
				setOperacionesCobros(varcambio);
			}
			if(newval&&!oldval){
				ordenRF.datos.cantidadInicial = undefined;
			}
		});
		
		$scope.$watch('ordenRF.datos.cantidadInicial',function(newval,oldval){ //JG: chequear esta variable!
			var varcambio='ordenRF.datos.cantidadInicial';
			if(newval){
				setOperacionesCobros(varcambio);
			}
			if(newval&&!oldval){
				ordenRF.datos.montoInicial = undefined;
			}
		});
		
		
		$scope.$watch('ordenRF.datos.idSistemaBursatil',function(newval,oldval){
			FormaNegociacion();
		});		
		var FormaNegociacion = function(){
			if (ordenRF.datos.idSistemaBursatil === 'NEGOCIACIONCONTINUA') {
				if (ordenRF.datos.instrumento === undefined) return;
				
				if (ordenRF.datos.instrumento.IdTipoInstrumento === 'D4') {
					ordenRF.datosConfig.filtrosCondicionDeLiquidacion = ['T0'];
				} else {
				ordenRF.datosConfig.filtrosCondicionDeLiquidacion = ['T1'];
				}				
			}
			else if (ordenRF.datos.idSistemaBursatil === "MERCADODELDINERO") {
				ordenRF.datosConfig.filtrosCondicionDeLiquidacion = ['T0','T1','T2'];
			}
			else {
				ordenRF.datosConfig.filtrosCondicionDeLiquidacion = undefined;
			}
		};
		
		
		$scope.$watch('ordenRF.datos.idMonedaLiquidacion',function(newval,oldval){
			var varcambio='ordenRF.datos.idMonedaLiquidacion';
			if(newval){
				setOperacionesCobros(varcambio);
			}
			obtenerParidades(varcambio);
		});

		$scope.$watch('ordenRF.datos.idTipoVigencia',function(newval,oldval){
			var varcambio='ordenRF.datos.idTipoVigencia';
			obtieneFechaVigencia(varcambio);
		});
		
		$scope.$watch('ordenRF.datos.tipoPrecio',function(newval,oldval){
			var varcambio='ordenRF.datos.tipoPrecio';
			setPrecio(varcambio);
		});
		
		$scope.$watch('ordenRF.win.muestraCobros',function(newval,oldval){
			var varcambio='ordenRF.win.muestraCobros';
			//redimensionarVentana(varcambio);
		});
		
		$scope.$watch('ordenRF.datos.operacionesSalida[0].totalOrden',function(newval,oldval){
			if(!newval)return;
			ordenRF.win.calcularSucio();
		});

		//Watch Rut Interno
		$scope.$watchGroup(['ordenRF.seleccionado.portafolio.IdPortafolio','ordenRF.seleccionado.bolsaDestino.IdBolsa'], function(newval,oldval) {
			ordenRF.habilitarBoton = true;
			if($scope.SesionLocale === 'PE') {
				if (newval[0] !== undefined) {
					if (newval[1] !== undefined && newval[1] !== 'FRD') {
						var esRutInterno = ordenRF.seleccionado.portafolio.EsRutInterno;
						if (esRutInterno === 'S') {
							ordenRF.habilitarBoton = false;
							bcsComunes.ventanaMsg({
								conflictos : {
									erroresFatales: [],
									errores: [{
										Codigo : "",
										Descripcion : "Cliente con Rut Interno, seleccione un Mercado OTC."
									}],
									advertencias: [],
									autorizaciones: [],
									informaciones: [],
									erroresFatalesContador: 0,
									erroresContador: 1,
									autorizacionesContador: 0,
									advertenciasContador: 0,
									informacionesContador: 0
								}
							},idVentana);
						}
					}
				}else {
				     	ordenRF.filtros.mecanismoDeCaptacion.filtros.IdEntidad.value = -1;
				      }
				
				/*OPTIMUSPE-1215*/
				if($scope.SesionLocale === 'PE' && ordenRF.seleccionado.bolsaDestino !== undefined && ordenRF.datos.idBolsaDestino === 'OUT' 
					&& ordenRF.IdBovedaCorresponsal !== undefined && ordenRF.filtros.cuentaCustodio.IdBoveda !== undefined)
					ordenRF.filtros.cuentaCustodio.IdBoveda.value = ordenRF.IdBovedaCorresponsal;
				/*OPTIMUSPE-1215*/   
			}
		});
		
		$scope.$watch('ordenRF.seleccionado.corredorCorresponsal',function(newval,oldval){
			if(!newval)return;
			/* OPTIMUSPE-1215 - Boveda por defecto cuando es Bolsa Extranjera*/	
			if($scope.SesionLocale === 'PE'){
				if(ordenRF.seleccionado.bolsaDestino != undefined && ordenRF.datos.idBolsaDestino === 'OUT' &&
						ordenRF.seleccionado.tipoOperacion !== undefined && ordenRF.seleccionado.corredorCorresponsal !== undefined){
					var ConsultarCorresponsalBoveda = new bcs.DataSource(defCorresponsalBoveda);
					var filtro = Array();
					filtro.push({ field : "IdCorredorCorresponsal", operator : "eq", value : ordenRF.seleccionado.corredorCorresponsal.IdCorredorCorresponsal == false ? null : ordenRF.seleccionado.corredorCorresponsal.IdCorredorCorresponsal});
					filtro.push({ field : "IdTipoProducto", operator : "eq", value : ordenRF.seleccionado.tipoOperacion.IdTipoProducto});
					ConsultarCorresponsalBoveda.query({filter: filtro}).then(function(){
						if(ConsultarCorresponsalBoveda.data() != undefined && ConsultarCorresponsalBoveda.data().length > 0 && ConsultarCorresponsalBoveda.data()[0] != undefined){					
							ordenRF.IdBovedaCorresponsal = ConsultarCorresponsalBoveda.data()[0].IdBoveda == null ? "" : ConsultarCorresponsalBoveda.data()[0].IdBoveda;
							ordenRF.filtros.cuentaCustodio.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value" : ordenRF.IdBovedaCorresponsal };
						}else{
							ordenRF.IdBovedaCorresponsal = "";
						}
					});	
				}	
			}
			/*OPTIMUSPE-1215*/
		});
		
		
		/*********************
		 * FUNCIONES PANTALLA
		 *********************/
		ordenRF.win.cleanData = function(){
			ordenRF.datos.idPortafolio 				= false;
			ordenRF.datos.idAutorizadoDarOrdenes 	= undefined;
			ordenRF.datos.instrumento 				= undefined;
			ordenRF.datos.instrumentoBuscar 		= '-1';
			ordenRF.datos.cantidad 					= undefined;
			ordenRF.datos.corteMinimo 				= undefined;
			ordenRF.datos.valorNominal 				= undefined;
			ordenRF.datos.precio 					= undefined;
			ordenRF.datos.tir 						= undefined;
			ordenRF.datos.tir360 					= undefined;
			ordenRF.datos.idMonedaLiquidacion 		= undefined;
			ordenRF.datos.tirMedia 					= '';
			ordenRF.datos.precioMedio 				= '';
			ordenRF.datos.duration 					= '';
			ordenRF.datos.valorTieneCall 			= '';
			ordenRF.datos.tirEmision 				= '';
			ordenRF.datos.clasifRiesgo 				= '';
			ordenRF.datos.duracion 					= '';
			ordenRF.datos.convexidad 				= '';
			ordenRF.datos.valorValorizado 			= undefined;
			ordenRF.datos.interesesCorridos 		= undefined;
			ordenRF.datos.reajustesVacCorrido 		= undefined;
			ordenRF.datos.valorActualizado 			= undefined;
			ordenRF.datos.precioSucio 				= undefined;
			ordenRF.datos.tirNeta 					= undefined;
			ordenRF.datos.pagoCobroCuentaInversion 	= true;
			ordenRF.datos.idPreferenciaPagoPortafolio= undefined;
			ordenRF.datos.fechaRecepcion 			= undefined;
			ordenRF.datos.comentario 				= undefined;
			ordenRF.datos.tipoCambio 				= undefined;
			ordenRF.datos.paridadReferencia 		= undefined;
			ordenRF.datos.idMecanismoDeCaptacion 	= undefined;
			ordenRF.datos.esOrdenEspecial 			= false;
			ordenRF.datos.esVentaCorta  			= undefined;
			ordenRF.datos.operaciones 				= [];
			ordenRF.datos.operacionesSalida 		= [];
			initDatosResumen();
		};
		
		ordenRF.win.volver = function(){
			ordenRF.win.tipoInstrumentoActivado = true;
			ordenRF.win.modo = 'INGRESO';
			ordenRF.win.cleanData();
			redimensionarVentana();
		};
		
		ordenRF.win.verComprobante = function(){
			bcsConfiguracionOrdenes.FUNCIONES.verComprobante(
				$scope.SesionIdEmpresa,
				$scope.ordenRF.datosResumen.documentoIdentidad,
				$scope.ordenRF.datosResumen.fechaOperacionOrden,
				$scope.ordenRF.datosResumen.secuenciaOrden,
				$scope.ordenRF.datosResumen.montoOperacion,
				idVentana
			);
		}
		
		ordenRF.win.verDetalle = function(){
			bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : ordenRF.datosResumen.secuenciaOrden} );
		}
		
		ordenRF.win.ingresarOrden = function(a){
			var cobrosModificados = [];
			$.each(ordenRF.datos.cobrosModificados,function(key,value){
				cobrosModificados.push({ IdInterno: 0 ,IdCobro : value.IdCobro, ValorCobroAplicado : value.ValorCobroAplicado  });
			});
			var prefPago = ordenRF.seleccionado.preferenciaPagoPortafolio;
			var autorizOrd = ordenRF.seleccionado.autorizadoDarOrdenes;
			var datosIngresoOrden = {
					p_IdEmpresa 					: $scope.SesionIdEmpresa,
					p_IdUsuario 					: $scope.SesionIdUsuario,
					p_IdModulo 						: 'ORD',
					p_simularOperacion 				: 'N',
					p_UsaReglasNegocio 				: 'S',
					p_NuevoIngreso 					: 'S',
					p_Ordenes : [{
						IdInterno 					: 0,
					    FechaRecepcion 				: (ordenRF.datos.fechaRecepcion)? util.dateSPseconddate(ordenRF.datos.fechaRecepcion) : '0001-01-01 00:00:00',
						FechaVigencia  				: (ordenRF.datos.fechaVigencia)? util.dateSPdate(ordenRF.datos.fechaVigencia) :  '0001-01-01',
						FechaEmision 				: '0001-01-01',
						FechaOperacion 				: '0001-01-01', //se calcula en el server
						IdCondicionDeLiquidacion 	: ordenRF.datos.idCondicionLiquidacion,
						IdMecanismoCaptacion		: (ordenRF.seleccionado.mecanismoDeCaptacion)?ordenRF.seleccionado.mecanismoDeCaptacion.ValorIndicador:'',
						IdTipoOperacion 			: ordenRF.datos.idTipoOperacion,
						IdTipoVigencia 				: ordenRF.datos.idTipoVigencia,
						Glosa 						: ordenRF.datos.comentario || '',
						Cantidad 					: ordenRF.datos.valorNominal,
						MontoNeto 					: ordenRF.datos.valorActualizado||ordenRF.datos.valorNominal, //que hacer cuando no valorice?
						ValorRescate 				: 0,
						IdBoveda 				: ordenRF.seleccionado.boveda.IdBoveda,
						IdFormaCustodia 			: ordenRF.datos.idFormaCustodia,
						IdPorcentajeVisibilidad 	: ordenRF.datos.idPorcentajeVisibilidad,
						PrecioLimite 				: Number(ordenRF.datos.precioMedio),
						PrecioMercado 				: Number(ordenRF.datos.instrumento.PrecioMercado),
						EsFacturaInmediata 			: 'N',
						IdTipoPrecio 				: ordenRF.datos.idTipoPrecio,
						IdPortafolio 				: ordenRF.datos.idPortafolio,
						IdTipoCanalIngreso 			: $scope.SesionTipoCanalIngreso,
						IdTipoReajuste 				: '',
						IdEmisor					: ordenRF.datos.instrumento.IdEmisor,
						IdBolsa 					: ordenRF.datos.idBolsaDestino,
						EspecificaBolsa 			: (ordenRF.datos.idBolsaDestino===0)?'N':'S',
						IdMonedaLiquidacionOrden 	: ordenRF.datos.idMonedaLiquidacion,
						IdMonedaLiquidacionCobros 	: ordenRF.datos.idMonedaLiquidacion,
						TipoCambio 					: ordenRF.datos.tipoCambio || null,
						IdInstrumento 				: ordenRF.datos.instrumento.IdInstrumento,
						IdInstrumentoSubyacente 	: 0,
						IdCondicionDeLiquidacionPlazo : '',
						TasaVentaCorta 				: (ordenRF.datos.esVentaCorta)? ordenRF.datos.tasaVentaCorta:0,
						PlazoVentaCorta 			: (ordenRF.datos.esVentaCorta)?  10 :ordenRF.datos.PlazoVentaCorta,
						EsVentaCorta 				: (ordenRF.datos.esVentaCorta)?'S':'N',
						TasaSimultanea 				: 0,
						PlazoSimultanea 			: 0,
						PlazoIF 					: 0,
						TasaRFIF					: ordenRF.datos.tir,
						EsOD						: 'N',
						EsFungible 					: 'N',
						MontoPlazo 					: 0,
						IdTipoLibroOrden	 		: 'NORMAL',
						IdLibroOrdenes	 			: ordenRF.datos.idLibroOrdenes||null,
						IdCuentaCustodioPortafolio 	: ordenRF.datos.cuentaCustodioSeleccionada? ordenRF.datos.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio : null,
						EsPorMonto 					: 'N',				
						IdCorredorCorresponsal 		: ordenRF.datos.idCorredorCorresponsal || null,
						IdPersonaAutorizadoDarOrdenes : (autorizOrd&&autorizOrd.IdPersona)?autorizOrd.IdPersona:null,
						IdTipoCanalDistribucion 	: ordenRF.datos.idTipoCanalDistribucion,
						EsOrdenEspecial 			: (ordenRF.datos.esOrdenEspecial==true)?'S':'N',
						PagoCobroCuentaInversion	: (ordenRF.datos.pagoCobroCuentaInversion==true)?'S':'N',
						IdPreferenciaPagoPortafolio : (prefPago&&prefPago.IdPreferenciaPagoPortafolio)?prefPago.IdPreferenciaPagoPortafolio:null,
						BaseTasa 					: 0,
						TipoInteres 				: "",
						InstrumentoAcreditado		: ordenRF.datos.instrumento.InstrumentoAcreditado||'N',
						EsClienteFinal				: ordenRF.datos.esParaClienteFinal === true?"S":"N",
						IdSistemaBursatil			: 'BCSTELERENTA',
						IngresoDesde		 		: "COR"
						
					}],
					p_CobrosModificados : cobrosModificados					
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
				promise.then(function(respuesta){
		   			bcsComunes.ventanaOverlay(idVentana,false);
					var r=respuesta.P_OUT_RESULTADO[0];
					if (r!==undefined&&r.IdOrden>0&&respuesta.P_OUT_ERRORESCONTADOR===0) {
						ordenRF.win.modo = 'RESUMEN';
						ordenRF.datosResumen = {
							fechaOperacionOrden 		: util.dateDS(r.FechaOperacionOrden,"FechaHoraSistema","CALL"),
							secuenciaOrden				: r.IdOrden,
							tipoInstrumento 			: r.NombreTipoInstrumento,
							operacion 					: r.AccionOrden,
							tipoInstrumento_operacion  	: r.AccionOrden + ' ' + r.NombreTipoInstrumento,
							cantidad 					: r.Cantidad,
							instrumento 				: r.Instrumento,
							precio 						: Number(r.PrecioOrden),
							esVentaCorta				: (ordenRF.datos.esVentaCorta)?'S':'N',
							tipoPrecio 					: r.NombreTipoPrecio,
							precio_tipoPrecio			: Number(r.PrecioOrden) + ' ' + r.NombreTipoPrecio,
							montoOperacion 				: r.MontoLiquidacionOperacion,
							totalCobros 				: Number(r.TotalCobros),
							montoTotal 					: Number(r.MontoTotal),
							idMoneda 					: r.IdMonedaLiquidacionOrden,
							idMonedaOrden				: r.IdMonedaOrden,
							documentoIdentidad			: r.DocumentoIdentidad,
							nombreEjecutivoPortafolio 	: r.NombreEjecutivoPortafolio
						};
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + r.IdOrden,
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);				
						ordenRF.datosForm.tipoInstrumentoActivado = false;
						ordenRF.datosResumen.decimales = util.bcs_formato(0,'GEN','Monto',r.IdMonedaLiquidacionOrden,r.IdTipoInstrumento,'').cantidadDecimales;
						ordenRF.datosResumen.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',r.IdMonedaLiquidacionOrden,r.IdTipoInstrumento,'').cantidadDecimales;
						ordenRF.datosResumen.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',r.IdMonedaLiquidacionOrden,r.IdTipoInstrumento,'').cantidadDecimales;
						//ordenRF.datosResumen.nombreEjecutivoPortafolio = NombreEmpleadoPortafolio;
						$timeout(function(){bcsComunes.ventanaTamanoAutomatico(idVentana);},10);
					}else{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
				    }	
		   		},function(respuesta){
		   			bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
		   		}
		   	);
	   	}; 

		function llamarValorizador(params, fnSuccess, fnError) {
			var url = "/BSCL/Plataforma/GWJAVA/SERVLETS/valo2/valorizar";
			$http({
				method : 'GET',
				url : url,
				params : params
			}).success(fnSuccess).error(fnError);
		}
		
	   	ordenRF.win.valorizar = function(modo){	
			if(ordenRF.valorizando==true)return;
			ordenRF.valorizando=true;
			limpiarDatosValorizacion();
			
			if (ordenRF.valorizando){
				if (typeof ordenRF.datos.precio !== 'undefined' && ordenRF.datos.precio > 0  && typeof ordenRF.datos.tir !== 'undefined' && ordenRF.datos.tir > 0){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INDICAR_CANTIDAD_O_PRECIO"));
					return;
				};
					
				if (typeof ordenRF.datos.precio !== 'undefined' && ordenRF.datos.precio > 0)
					cambio = 'Precio';
				else if (typeof ordenRF.datos.tir !== 'undefined' && ordenRF.datos.tir > 0)
					cambio = 'Tasa';
				else {
					ordenRF.valorizando = false;
					return;
				}
			};
					
			switch (cambio){
				case "Tasa"		: ordenRF.datos.precio = undefined; break;
				case "Precio"	: ordenRF.datos.tir = undefined; break;
			}
			switch ($scope.SesionLocale){
				case "CL":	Cantidad = ordenRF.datos.cantidad; break; 
			}
			var continuar=true;
			if(!ordenRF.datos.instrumento||!ordenRF.datos.instrumento.IdInstrumento)
				{continuar=false;}
			
			if(cambio==='Tasa'){
				if (ordenRF.datos.precio!==undefined&&ordenRF.datos.precio!==null&&ordenRF.datos.precio!==''){
					ordenRF.datos.tir 		= undefined;
					ordenRF.datos.tir360 	= undefined;
				}
			}else if(cambio==='Precio'){
				
				if (ordenRF.datos.tir!==undefined&&ordenRF.datos.tir!==null&&ordenRF.datos.tir!==''){
					ordenRF.datos.precio = undefined;
				};
			}else continuar=false;
			
			if(continuar== false){
				bcsComunes.ventanaOverlay(idVentana,false);
				ordenRF.valorizando=undefined;
				return;
			}
			if ($scope.SesionLocale === 'PE') {
				var params = {
					nemo: ordenRF.datos.instrumento.Nemotecnico,
					fechaValorizacion: $scope.SesionFechaSistema,
					valorNominal: ordenRF.datos.valorNominal,
					precio: ordenRF.datos.precio,
					tasa: ordenRF.datos.precio === undefined ? ordenRF.datos.tir : null,
				}

				llamarValorizador(params, function(data) {
					if (data.codigoRetorno && data.codigoRetorno !== 0) {
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						var mensaje = data.mensaje ? data.mensaje : "Error al valorizar";
						bcsComunes.msg("error", "UI_ERROR", mensaje);
					} else {
						// Recuerda Borrar los console.log antes de subir a produccion
						console.log("Resultado valorización", data);
						// Valorizador Normal
						ordenRF.datos.precio = data.precio;
						ordenRF.datos.precioMedio = data.precio;
						ordenRF.datos.tir = data.tasa;
						ordenRF.datos.tirMedia = data.tasa;
						ordenRF.datos.tir360 = data.tasa360 ? data.tasa360 : null;
						ordenRF.datos.IdMonedaOrden = data.monedaPago ? data.monedaPago : data.moneda;
						ordenRF.datos.idMonedaLiquidacion = data.monedaPago ? data.monedaPago : null;
						ordenRF.datos.valorValorizado  = data.montoCapital;
						ordenRF.datos.interesesCorridos = data.montoIntereses;
						ordenRF.datos.reajustesVacCorrido = data.montoReajuste;
						ordenRF.datos.valorActualizado = data.montoTotal;
						ordenRF.datos.duracion = data.duration;
						ordenRF.datos.convexidad = data.convexidad;
						ordenRF.datos.fechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
						ordenRF.datos.valorTieneCall = data.cupones && data.cupones[0] ? data.cupones[0].call : null;
						$timeout(function(){ordenRF.valorizando=undefined},200)

						console.log("Parametros ordenRF.datos", ordenRF.datos);
					}
				}, function(result) {
					$timeout(function(){ordenRF.valorizando=undefined},200)
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					bcsComunes.msg("error", "UI_ERROR",
							"UI_ERROR_AL_VALORIZAR_INSTRUMENTO");
				});
			} else {
				var Instrumento =ordenRF.datos.instrumento || undefined
				//LOG:
				var vgroup = "VALORIZAR "+modo;
				var vtime = "Tiempo ejecución";
				
				bcsComunes.ventanaOverlay(idVentana,true);
				var datos = {
					p_IdEmpresa 			: $scope.$root.SesionIdEmpresa,
					p_IdUsuario 			: $scope.$root.SesionIdUsuario,
					p_IdModulo 				: 'CUS',
					p_IdPais 				: $scope.$root.SesionIdPaisSistema,
					p_OrigenProcesamiento 	: 'ORDEN_UI',
					p_IdMonedaAdicional		:ordenRF.datos.IdMonedaOrden,
					p_Instrumentos			: [{
						IdValorizacion					: 1,
						FechaOperacion					: $scope.SesionFechaSistema,
						FechaVencimientoOperacion 		: util.dateSPdate(ordenRF.datos.fechaVigencia),
						PlazoEnDiasOperacion 			: 0, //Solo para operaciones a plazo
						FechaValorizacion				: util.dateSPdate(ordenRF.datos.fechaLiquidacion), 		
						IdClasificacionTipoInstrumento	: Instrumento.IdClasificacionTipoInstrumento, 
						IdTipoInstrumento				: Instrumento.IdTipoInstrumento, 
						IdInstrumento					: Instrumento.IdInstrumento, 
						Nemotecnico						: Instrumento.Nemotecnico, 
						FechaVencimiento				: util.dateSPdate(Instrumento.FechaVencimiento),
						FechaEmision					: util.dateSPdate(util.dateDS(Instrumento.FechaEmision,"FechaSistema","CALL")),
						IdMonedaInstrumento				: Instrumento.IdMoneda,
						IdTipoReajusteInstrumento		: Instrumento.IdTipoReajuste,
						Cantidad						: (ordenRF.datos.cantidad!=null&&ordenRF.datos.cantidad!=undefined)?ordenRF.datos.cantidad:null, 
						ValorRescate					: null,
						PrecioCompra					: (ordenRF.datos.precio!=null&&ordenRF.datos.precio!=undefined)?ordenRF.datos.precio:null,
						TasaCompra						: (ordenRF.datos.tir!=null&&ordenRF.datos.tir!=undefined)?ordenRF.datos.tir:null,
						MontoCompra						: null,//en moneda del instrumento		
						PrecioSubyacente				: null, 
						TasaSubyacente					: null, 
						PrecioGarantia					: null, 
						TasaGarantia					: null, 
						TasaTIF							: null,		
						TieneCorteDeCupon				: null,
						IdCliente						: null, 
						IdPortafolio					: null,
						TablaOrigen						: "CUSTODIA",	
						IdUso							: null, 
						TipoCartera						: null, 
						IdTipoCriterioTasa				: null,
						IdMonedaEmisionInstrumento		: Instrumento.IdMonedaEmision,
						Emisor							: Instrumento.IdEmisor,
						IdCondicionDeLiquidacion		:ordenRF.datos.idCondicionLiquidacion||null
					}],
				};
				
				bcsServicioPlataforma.ValorizarInstrumento(datos,function(response){
					bcsComunes.ventanaOverlay(idVentana,false);
					if (response.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("warning",'Valorizador',$translate.instant('UI_ERROR_RUTINA_VALORIZACION'));
					}
					else if(response.P_OUT_INSTRUMENTOSVALORIZADOS.length==0){
						var p=bcsComunes.ventanaMsg($translate.instant('UI_SIN_DATOS_VALORIZADOS'),idVentana);
						bcsComunes.ventanaPromesa(p).then(function(){
						});
					}else{
						var itemval = response.P_OUT_INSTRUMENTOSVALORIZADOS[0];
						$.each(response.P_OUT_INSTRUMENTOSVALORIZADOS,function(key,value) {
							if (value.DimensionTasa === 'COMP' && value.DimensionTiempo === 'AC' && value.IdValorizacion === 1){	//A Tasa Compra
								ordenRF.datos.tir 					= value.Tasa || 0;
								ordenRF.datos.tir360 				= value.Tasa360 || 0;
								ordenRF.datos.precio 				= value.Precio || 0;
								ordenRF.datos.valorValorizado 		= value.MontoCapitalCorteCuponMonedaInstrumento || 0;
								ordenRF.datos.interesesCorridos 	= value.MontoInteresCorteCuponMonedaInstrumento || 0;
								ordenRF.datos.reajustesVacCorrido 	= value.MontoReajusteCorteCuponMonedaInstrumento || 0;
								ordenRF.datos.valorActualizado 		= value.MontoMonedaInstrumento;
							}

							if (value.DimensionTasa === 'COMP' && value.DimensionTiempo === 'AC' && value.IdValorizacion === 2){	//A Tasa Emision
								ordenRF.datos.tirEmision 			= value.TasaEmision || 0;
								ordenRF.datos.clasifRiesgo 			= value.ValorClasificacionRiesgo;
								ordenRF.datos.duracion 				= value.Duration || 0;
								ordenRF.datos.convexidad 			= value.Convexidad || 0;							
							}

							if (value.DimensionTasa === 'MERC' && value.DimensionTiempo === 'AC' && value.IdValorizacion === 1){
								ordenRF.datos.tirMedia 				= value.Tasa || 0;
								ordenRF.datos.precioMedio 			= value.Precio || 0;
								ordenRF.datos.duration 				= value.Duration || 0;
								ordenRF.datos.valorTieneCall 		= value.ValorTieneCall === 'S' ? true : false;							
							}
						});
					}
					$timeout(function(){ordenRF.valorizando=undefined},200)
				},function(response){
					$timeout(function(){ordenRF.valorizando=undefined},200)
					var p=bcsComunes.ventanaMsg($translate.instant('UI_ERROR_EN_COMUNICACION'),idVentana);
					bcsComunes.ventanaPromesa(p).then(function(){
						bcsComunes.ventanaOverlay(idVentana,false);
					});
				});
			}
	   	}
	   	
	   	ordenRF.win.calcularSucio = function(){
	   		if(!ordenRF.datos.operacionesSalida||!ordenRF.datos.operacionesSalida[0]||!ordenRF.datos.operacionesSalida[0].totalOrden)return;
	   		if(!ordenRF.datos.instrumento||!ordenRF.datos.instrumento.IdInstrumento)return;
	   		//if(!ordenRF.datos.valorValorizado)return;
	   		var vgroup = "VALORIZAR POR MONTO";//LOG:
	   		var vtime = "Tiempo ejecución";
			var datos = {
				p_IdEmpresa 			: $scope.$root.SesionIdEmpresa,
				p_IdUsuario 			: $scope.$root.SesionIdUsuario,
				p_IdModulo 				: 'CUS',
				p_IdPais 				: $scope.$root.SesionIdPaisSistema,
				p_OrigenProcesamiento 	: 'ORDEN_UI',
				p_Instrumentos			: [{
					IdValorizacion					: 3,
					FechaOperacion					: $scope.SesionFechaSistema,
					FechaVencimientoOperacion 		: util.dateSPdate(ordenRF.datos.fechaVigencia),
					PlazoEnDiasOperacion 			: 0, //Solo para operaciones a plazo
					FechaValorizacion				: util.dateSPdate(ordenRF.datos.fechaLiquidacion), 		
					IdClasificacionTipoInstrumento	: ordenRF.datos.instrumento.IdClasificacionTipoInstrumento, 
					IdTipoInstrumento				: ordenRF.datos.instrumento.IdTipoInstrumento, 
					IdInstrumento					: ordenRF.datos.instrumento.IdInstrumento, 
					Nemotecnico						: ordenRF.datos.instrumento.Nemotecnico, 
					FechaVencimiento				: util.dateSPdate(ordenRF.datos.instrumento.FechaVencimiento),
					FechaEmision					: util.dateSPdate(util.dateDS(ordenRF.datos.instrumento.FechaEmision,"FechaSistema","CALL")),
					IdMonedaInstrumento				: ordenRF.datos.instrumento.IdMoneda,
					IdTipoReajusteInstrumento		: ordenRF.datos.instrumento.IdTipoReajuste,
					Cantidad						: ordenRF.datos.valorNominal||null, 
					ValorRescate					: null,
					PrecioCompra					: null,
					TasaCompra						: null,
					MontoCompra						: ordenRF.datos.operacionesSalida[0].totalOrden,//en moneda del instrumento		
					PrecioSubyacente				: null, 
					TasaSubyacente					: null, 
					PrecioGarantia					: null, 
					TasaGarantia					: null, 
					TasaTIF							: null,		
					TieneCorteDeCupon				: null,
					IdCliente						: null, 
					IdPortafolio					: null,
					TablaOrigen						: null,	
					IdUso							: null, 
					TipoCartera						: null, 
					IdTipoCriterioTasa				: null,
					
				}],
			};
			bcsServicioPlataforma.ValorizarInstrumento(datos,function(response){
				var itemval = response.P_OUT_INSTRUMENTOSVALORIZADOS[0];
				ordenRF.datos.tirNeta 		= itemval.Tasa;
				ordenRF.datos.precioSucio 	= itemval.Precio;
	   		},function(response){
	   			console.error(response);
	   		});
			
	   	}

		   $('div#Win_IngresarOrdenRF').css('height', '790px')

		   $scope.tabClick = function (){
			var ventana = $("[ventanaid=" + idVentana +"]").closest(".k-window");
			ventana.css('height', 'auto');
		}
		//INICIALIZACION DE LOS DATOS:
		initDatosConfig();
		initSeleccionado();
		if(parmorden){
			console.info("generar datos contraorden");
			ordenRF.win.modo = 'INGRESO';
			initDatosContraorden();			
		}else{
			initDatos();
			initDatosResumen();
		}
	});
}());
//------------------------- FIN CONTROLLER --------------------------	
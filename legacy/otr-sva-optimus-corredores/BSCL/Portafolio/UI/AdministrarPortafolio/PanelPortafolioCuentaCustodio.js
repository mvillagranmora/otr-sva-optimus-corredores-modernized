/*************************************************************
 * Purpose	: Panel Portafolio Cuenta Custodio.
 * Author	: JG - BCS
 * Date		: 30/06/2016 
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioCuentaCustodioCtrl',
	function ($scope,bcsComunes, bcsServicioPlataforma,$translate,$resource) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		const pPortCcust 			= this;
		pPortCcust.idVentana 		= bcsComunes.ventanaWinId($scope);
		pPortCcust.parametros 		= bcsComunes.ventanaParams(pPortCcust.idVentana);
		pPortCcust.datosAntes		= {};
		pPortCcust.datosDespues		= {};
		pPortCcust.cmbbx			= {};
		pPortCcust.respuesta 		= {};
		pPortCcust.CodigoFormulario = {};
		pPortCcust.modoEditar 		= false;

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		pPortCcust.cmbbx 	= pPortCcust.cmbbx 	|| {};
		pPortCcust.fn 		= pPortCcust.fn 	|| {};
		pPortCcust.win 		= pPortCcust.win 	|| {};	
		pPortCcust.grid		= pPortCcust.grid	|| {};

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
	
		$scope.admPort.socioNegocioPromise.then(function () {
			pPortCcust.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['CUENTA_CUSTODIO'];
		});

		pPortCcust.win.abrirPopUp = function(cuentaCustodio){
			const datosEnviarModal = {
				IdPortafolio		 	: $scope.admPort.datos.IdPortafolio,
				CuentaCustodioObj	 	: cuentaCustodio,
				CuentaCustodioTodosObj 	: undefined,
				CodigoFormulario		: pPortCcust.CodigoFormulario};
			const winModal = bcsComunes.ventanaAbrir("ModalPortafolioCuentaCustodio", datosEnviarModal, false,false,pPortCcust.idVentana);
			const promesa = bcsComunes.ventanaPromesa(winModal);                               
            promesa.then(function(){
            	dsCuentaCustodio.read();
            	pPortCcust.cuentaCustodioSeleccionado = undefined;
            	
            });
		};

		pPortCcust.win.solicitarAutorizacion = function(cuentaCustodio){

			const parametrosSP = {};
			const eventoFila = {};
			const concepto='NPCCUS';
			const modulo='SOC';
			const sesionIdEmpresa = ($scope.$root && $scope.$root.SesionIdEmpresa) || $scope.SesionIdEmpresa || (typeof BCS_SESION !== 'undefined' ? BCS_SESION.IdEmpresa : null);
			const sesionIdUsuario = ($scope.$root && $scope.$root.SesionIdUsuario) || $scope.SesionIdUsuario || (typeof BCS_SESION !== 'undefined' ? BCS_SESION.IdUsuario : null);
			const sesionFechaSistemaRaw = ($scope.$root && $scope.$root.SesionFechaSistema) || $scope.SesionFechaSistema || (typeof BCS_SESION !== 'undefined' ? BCS_SESION.FechaSistema : null);
			const sesionFechaSistema = (sesionFechaSistemaRaw && typeof sesionFechaSistemaRaw === 'string' && sesionFechaSistemaRaw.length >= 10) ? sesionFechaSistemaRaw.substring(0,10) : sesionFechaSistemaRaw;

			parametrosSP.P_IDEMPRESA = Number(sesionIdEmpresa);
			parametrosSP.P_IDUSUARIO = Number(sesionIdUsuario);
			parametrosSP.P_IDMODULO = modulo;
			parametrosSP.P_SIMULAROPERACION = "N";
			parametrosSP.P_USAREGLASNEGOCIO = "N";
			parametrosSP.P_TABLAEVENTOS = [];
			parametrosSP.P_DETALLE = [];

			eventoFila.IdTipoEvento = concepto;
			eventoFila.IdModuloSolicita = modulo;
			eventoFila.IdUsuarioSolicita = sesionIdUsuario;
			eventoFila.Fecha = sesionFechaSistema;
			eventoFila.IdConcepto = concepto;
			eventoFila.NumeroOperacionConcepto = String(cuentaCustodio.IdCuentaCustodioPortafolio);
			eventoFila.FechaOperacionConcepto = sesionFechaSistema;
			eventoFila.IdCliente = 0;
			eventoFila.Glosa = "Cuenta Custodio: " + cuentaCustodio.Codigo;

			parametrosSP.P_TABLAEVENTOS.push(eventoFila);

			var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=130";
			var r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
			var post = r.urlConsulta(parametrosSP);

			post.$promise.then(function(result){
				if (result.P_OUT_ERRORESCONTADOR && Number(result.P_OUT_ERRORESCONTADOR) > 0){
					bcsComunes.ventanaMsg({ mensaje: "La operacion tiene errores",
						conflictos: util.parseServerResponse(result)}, pPortCcust.idVentana);
				}else if (result.P_OUT_ADVERTENCIASCONTADOR && Number(result.P_OUT_ADVERTENCIASCONTADOR) > 0){
					bcsComunes.ventanaMsg({ mensaje: "La operacion tiene advertencias",
						conflictos: util.parseServerResponse(result)}, pPortCcust.idVentana);
				}else{
					bcsComunes.msg("info","info","Solicitud de autorizacion enviada");
				}
			},function(){
				bcsComunes.msg("error","Error","La operacion tiene errores");
			});
		};
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		const dsCuentaCustodio = new bcs.DataSource({
			type : "odata",
			definition : {
				pageSize : 100,
				serverFiltering: true,
	        	serverPaging: true,
				transport : {
					read : {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1263",
						dataType : "json"
					}
				},
				filter : { field: "IdPortafolio", operator: "eq", value: Number($scope.admPort.datos.IdPortafolio)},
				schema : {
					model : {
						fields : {
							IdCuentaCustodioPortafolio	: { type: "number"},
							IdPortafolio				: { type: "number"},
							IdBoveda					: { type: "string"},
							NombreBoveda				: { type: "string"},
							Codigo						: { type: "string"},
							FechaIngreso				: { type: "date"},
							FechaCreacion				: { type: "date"},
							FechaModificacion			: { type: "date"},
							IdUsuarioCreador			: { type: "string"},
							IdEstado					: { type: "string"},
							NombreEstado				: { type: "string"},
							IdEstadoAutorizacion		: { type: "string"},
							NombreEstadoAutorizacion	: { type: "string"},

							NombreModeloCustodia			: { type: "string"},
							NombreTipoCustodia				: { type: "string"},
							NombreTipoCuentaCustodia		: { type: "string"},
							CodigoBancoCustodio				: { type: "string"},
							CodigoBancoBancoLiquidador		: { type: "string"},
							
							LiquidaVencimientosUI			: { type: "string"},
							LiquidaCuponesUI				: { type: "string"},
							LiquidaDividendosUI				: { type: "string"},
							LiquidaPrepagosUI				: { type: "string"},
							
							
						}
					}
				},
			}
		});
		
		pPortCcust.setGridCuentaCustodio = function(){
			const conf = {
				dataSource : dsCuentaCustodio,
				columns: [
				    {field: "NombreBoveda", title: $translate.instant('UI_BOVEDA'),width: "200px"},
				    {field: "Codigo", title: $translate.instant('UI_CODIGO'), width: "90px"},
				    {field: "NombreEstado", title: $translate.instant('UI_ESTADO'),width: "60px"},
				    {field: "NombreEstadoAutorizacion", title: $translate.instant('UI_ESTADO_AUTORIZACION'),width: "100px"},
				],
				sortable			: true,
				pageable			: true,
				autoBind			: true,
				height				: 280,
			    selectable 			: "row"
			}
			
			if ($scope.SesionLocale === 'PE') {
				conf.columns.push({field: "NombreModeloCustodia",			title: $translate.instant('UI_MODELO_CUSTODIA'),width: "120px"});
				conf.columns.push({field: "NombreTipoCustodia", 			title: $translate.instant('UI_TIPO_CUSTODIA'),width: "120px"});
				conf.columns.push({field: "NombreTipoCuentaCustodia", 		title: $translate.instant('UI_TIPO_CUENTA'),width: "120px"});
				conf.columns.push({field: "CodigoBancoCustodio", 			title: $translate.instant('UI_BANCO_CUSTODIO'),width: "120px"});
				conf.columns.push({field: "CodigoBancoBancoLiquidador", 	title: $translate.instant('UI_BANCO_LIQUIDADOR'),width: "120px"});
				conf.columns.push({field: "LiquidaVencimientosUI", 			title: $translate.instant('UI_LIQUIDA_VENCIMIENTOS'),width: "120px"});
				conf.columns.push({field: "LiquidaCuponesUI", 				title: $translate.instant('UI_LIQUIDA_CUPONES'),width: "120px"});
				conf.columns.push({field: "LiquidaDividendosUI", 			title: $translate.instant('UI_LIQUIDA_DIVIDENDOS'),width: "120px"});
				conf.columns.push({field: "LiquidaPrepagosUI", 				title: $translate.instant('UI_LIQUIDA_PREPAGOS'),width: "120px"});
				
			}
			
			return conf;
		};

		/*********************************************
		 * GO!
		 *********************************************/
		pPortCcust.gridCuentaCustodio = pPortCcust.setGridCuentaCustodio();
	});
}());
//------------------------- FIN CONTROLLER --------------------------
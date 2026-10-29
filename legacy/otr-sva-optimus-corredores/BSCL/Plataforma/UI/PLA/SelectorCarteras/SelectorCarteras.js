angular.module('ModuloPlataforma').controller('SelectorCarterasCtrl',function ($scope,$translate,bcsComunes) {

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.parametrosForm = {};
	
	$scope.parametrosForm.idPortafolio	 						= parametros.IdPortafolio;
	
	$scope.parametrosForm.idTipoOperacion					 	= parametros.IdTipoOperacion;
	$scope.parametrosForm.idClasificacionTipoInstrumento	 	= parametros.IdClasificacionTipoInstrumento;
	$scope.parametrosForm.idTipoInstrumento	 					= parametros.IdTipoInstrumento;
	$scope.parametrosForm.esFungible	 						= parametros.EsFungible;
	$scope.parametrosForm.idInstrumento	 						= parametros.IdInstrumento?Number(parametros.IdInstrumento):null;
	$scope.parametrosForm.instrumento	 						= parametros.Instrumento;
	$scope.parametrosForm.tasa 									= parametros.Tasa;
	$scope.parametrosForm.fechaLiquidacion 						= parametros.FechaLiquidacion
	$scope.parametrosForm.idEmisor 								= parametros.IdEmisor
	$scope.parametrosForm.idTipoReajuste 						= parametros.IdTipoReajuste;
	$scope.parametrosForm.fechaVencimiento 						= parametros.FechaVencimiento;
	
	$scope.parametrosBusqueda = {};
	
	$scope.parametrosBusqueda.filtrosCliente = {
		IdEmpresa 		: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
		EstadoCliente 	: { "field"   : "EstadoCliente","operator": "eq", "value"   :  'ACT' },
		EsCliente 		: {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
	};
	
	$scope.parametrosBusqueda.filtrosPortafolio = {
		IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
	};
	
	$scope.parametrosForm.buscarCarteras = function(){
		var tblInstrumentos = [{
			"IdClasificacionTipoInstrumento"	:$scope.parametrosForm.idClasificacionTipoInstrumento?$scope.parametrosForm.idClasificacionTipoInstrumento:null,
			"IdTipoInstrumento"					:$scope.parametrosForm.idTipoInstrumento?$scope.parametrosForm.idTipoInstrumento:null,
			"IdInstrumento"						:$scope.parametrosForm.idInstrumento?$scope.parametrosForm.idInstrumento:null,
			"IdEmisor"							:$scope.parametrosForm.idEmisor?$scope.parametrosForm.idEmisor:null,
			"FechaVencimiento"					:$scope.parametrosForm.idClasificacionTipoInstrumento === 'IF'?$scope.parametrosForm.fechaVencimiento : null,
			"IdTipoReajuste"					:$scope.parametrosForm.idClasificacionTipoInstrumento === 'IF'?$scope.parametrosForm.idTipoReajuste :null,
			"EsFungible"						:$scope.parametrosForm.idClasificacionTipoInstrumento === 'IF'?$scope.parametrosForm.esFungible:"N",
			"IdBoveda"							:null
		}];
		
		var portafolio = null;
		var cliente = null;
		
		if ($scope.parametrosForm.idPortafolio > 0) {
			portafolio = $scope.parametrosForm.idPortafolio;
			cliente = null;
		}
		else {
			if ($scope.parametrosBusqueda.portafolioSeleccionado && $scope.parametrosBusqueda.portafolioSeleccionado.IdPortafolio) {
				portafolio = $scope.parametrosBusqueda.portafolioSeleccionado.IdPortafolio;
				cliente = $scope.parametrosBusqueda.portafolioSeleccionado.IdCliente;
			}
		}

		var tblCarteras = [{
			"IdTipoOperacion"					:$scope.parametrosForm.idTipoOperacion,
			"IdCliente"							:cliente,
			"IdPortafolio"						:portafolio,
		}];
		
		var datosLlamada = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'ORD',
			p_Instrumentos : tblInstrumentos,
			p_Carteras: tblCarteras,
			p_FechaSaldo : util.dateSPdate($scope.parametrosForm.fechaLiquidacion)
		};
		
		ds.setCallParameters(datosLlamada);
		
	};
	
	var columnsDS = [];
	if (!$scope.parametrosForm.idPortafolio) {
		columnsDS.push({field: "DocumentoIdentidad",			title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),	width: "90px"});
		columnsDS.push({field: "NombreCompleto",				title: $translate.instant('UI_CLIENTE'),				width: "200px"});
		columnsDS.push({field: "CodigoPortafolio",				title: $translate.instant('UI_PORTAFOLIO'),				width: "75px"});
	}
	
	columnsDS.push({field: "FolioCartera",						title: $translate.instant('UI_FOLIO_CARTERA'),			width: "70px"});
	columnsDS.push({field: "Instrumento",						title: $translate.instant('UI_INSTRUMENTO'),			width: "100px"});
	columnsDS.push({field: "IdTipoInstrumento",					title: $translate.instant('UI_TIPO_INSTRUMENTO'),		width: "75px"});
	columnsDS.push({field: "IdClasificacionTipoInstrumento",	title: $translate.instant('UI_CLASIFICACION'),			width: "30px"});
	columnsDS.push({field: "TasaCompra",						title: $translate.instant('UI_TASA_COMPRA'),			width: "45px",template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa',IdMoneda,'','')).valor #</label>"});
	columnsDS.push({field: "TasaMercado",						title: $translate.instant('UI_TASA_MERCADO'),			width: "45px",template : " <label> #:  (util.bcs_formato(TasaMercado,'GEN','Tasa',IdMoneda,'','')).valor #</label>"});
	columnsDS.push({field: "CorteMinimo",						title: $translate.instant('UI_CORTE_MINIMO'),			width: "100px",template : " <label> #:  (util.bcs_formato(CorteMinimo,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"});
	columnsDS.push({field: "CantidadDisponible",				title: $translate.instant('UI_SALDO'),					width: "100px",template : " <label> #:  (util.bcs_formato(CantidadDisponible,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"});
	columnsDS.push({field: "NemoSVS",							title: $translate.instant("UI_NEMOTECNICO_SVS"),		width: "75px"});
	
	$scope.gridConfigBusquedaCartera = {
			autoBind			: false,
			filterable			: false,
			height				: 400,
			pageable			: false,
			selectable 			: "multiple,row",
			sortable			: true,
			
			columns : columnsDS,
				 		 		
		 	dataSource : new bcs.DataSource({
		 		type 		: "call",
				definition 	: {
					callTransaction : "1403",
					callResultSet   : "P_OUT_CARTERAS",		 		
		 			schema : {
						model: {
							fields : {
								DocumentoIdentidad 				: { type: "string"},
								NombreCompleto 					: { type: "string"},
								IdCliente						: { type: "number"},
								IdPortafolio					: { type: "number"},
								CodigoPortafolio 				: { type: "string"},
								IdInstrumento					: { type: "number"},
								FolioCartera					: { type: "number"},
								Instrumento 					: { type: "string"},
								IdTipoInstrumento 				: { type: "string"},
								IdClasificacionTipoInstrumento 	: { type: "string"},
								TasaCompra						: { type: "number"},
								TasaMercado						: { type: "number"},
								CorteMinimo						: { type: "number"},
								CantidadDisponible				: { type: "number"},
								IdMoneda 						: { type: "string"},
								FechaLiquidacion 				: { type: "sysdate"},
								IdEmisor 						: { type: "string"},
								FechaVencimiento 				: { type: "sysdate"},
								NemoSVS 						: { type: "string"},
								IdTipoReajuste 					: { type: "string"},
								EsFungible 						: { type: "string"}
							}
						}	 
					},
				}
			})
	};
	
	var ds = $scope.gridConfigBusquedaCartera.dataSource;

	
	$scope.seleccionarCartera = function(datos) {
		bcsComunes.ventanaData(idVentana,datos);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function(datos) {
		bcsComunes.ventanaData(idVentana,{});
		bcsComunes.ventanaCerrar(idVentana);
	};
});
angular.module('ModuloOrdenes').controller('AsignacionMasivaPrecioPromedioDetalleCtrl',function ($scope,$rootScope,$q,bcsComunes,bcsServicioOrden,
$translate,bcsServicioPlataforma, $translate, bcsAlerta, OPCIONES_BCSALERTA,$timeout) {
	
	//TIENEN QUE LLEGAR TRANSACCIONES DE UN LIBRO, UNA CLASIFICACION, Y UNA MARCA (O MARCA VACIA)
	let defer = $q.defer();
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	let parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.transacciones = parametros.TransaccionesDetalle;
	$scope.IdBolsa = parametros.bolsa;
	$scope.IdBoveda = parametros.boveda;
	$scope.IdModeloCustodia = parametros.modeloCustodia;
	$scope.IdClasificacionTipoInstrumento = parametros.mercado;
	$scope.formData = {};
	$scope.cliente = {};
	
	$scope.asignaciones = {};
	
	$scope.datosForm = {};
	$scope.datosBusqueda = {};
	$scope.transaccionesAgrupadas = [];
	
	$scope.columnasGrilla = [
			{field: "FolioTransaccion",					title: $translate.instant('UI_FOLIO_TRANSACCION'),	width: "30px"},
			{field: "FechaOperacionTransaccion",		title: $translate.instant('UI_FECHA_TRANSACCION'),	width: "60px"},
			{field: "IdClasificacionTipoInstrumento",	title: $translate.instant('UI_MERCADO'),	width: "30px"},
			{field: "AccionTipoOperacion",				title: $translate.instant('UI_TIPO_OPERACION'),	width: "30px"},
		 	{field: "InstrumentoUI",					title: $translate.instant('UI_NEMOTECNICO'),	width: "30px"},
			{field: "CantidadTransaccion",				title: $translate.instant('UI_CANTIDAD'),	width: "30px"},
			{field: "PrecioTransaccion",				title: $translate.instant('UI_PRECIO'),	width: "30px",	template : " <label> #:  (util.bcs_formato(PrecioTransaccion,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			{field: "CantidadDisponibleTransaccion",	title: $translate.instant('UI_SALDO_DISPONIBLE'),	width: "30px",  footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Cantidad','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			{field: "MontoDisponibleTransaccion",		title: $translate.instant('UI_MONTO_DISPONIBLE'),	width: "45px",	template : " <label> #:  (util.bcs_formato(MontoDisponibleTransaccion,'GEN','Monto',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>",  footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			{field: "NombreCondicionDeLiquidacion",		title: $translate.instant('UI_CONDICION_LIQUIDACION'),	width: "45px"},
			{field: "MarcaNegociacion",					title: $translate.instant('UI_MARCA_NEGOCIACION'),	width: "40px"},
			{field: "NombreBolsa",						title: $translate.instant('UI_BOLSA'),	width: "30px"},
			{field: "IdMonedaInstrumento",				title: $translate.instant('UI_MONEDA'),	width: "30px", hidden: "true"}
		];
		
	let configGrilla = {
		editable: false,
		//selectable: "row",
		autoBind: true,
		groupable : false,
  		scrollable: true,
 		height:400,
 		resizable: true,
 		userSettings: false,
 		contextMenu: true,
 		pageable: true,
 		navigatable: true,
 		dataSource : new bcs.DataSource({
 			type : "array",
 			definition : {
 				data : $scope.transacciones,
 				sort: { field: "FechaOperacionTransaccion", dir: "desc" },
				schema: {
					model: {
	                    fields: {
	                    	FolioTransaccion				: { type: "string"},
	                        FechaOperacionTransaccion		: { type: "datetime"},
	                        IdClasificacionTipoInstrumento	: { type: "string"},
	                        AccionTipoOperacion				: { type: "string"},
	                        InstrumentoUI					: { type: "string"},
	                        CantidadTransaccion				: { type: "number"},
	                        PrecioTransaccion				: { type: "number"},
	                        CantidadDisponibleTransaccion	: { type: "number"},
	                        MontoDisponibleTransaccion		: { type: "number"},
	                        NombreCondicionDeLiquidacion	: { type: "string"},
	                        MarcaNegociacion				: { type: "string"},
	                        NombreBolsa						: { type: "string"},
	                        IdMonedaInstrumento				: { type: "string"},
	                        IdTipoInstrumento				: { type: "string"},
                        }
                   }
				},
		        aggregate: [
		            { field: "CantidadDisponibleTransaccion", aggregate: "sum" },
		            { field: "MontoDisponibleTransaccion", aggregate: "sum"}
		        ]  
 			}
 		}),
 		columns : [
			{field: "FolioTransaccion",					title: $translate.instant('UI_FOLIO_TRANSACCION'),	width: "30px"},
			{field: "FechaOperacionTransaccion",		title: $translate.instant('UI_FECHA_TRANSACCION'),	width: "60px"},
			{field: "IdClasificacionTipoInstrumento",	title: $translate.instant('UI_MERCADO'),	width: "30px"},
			{field: "AccionTipoOperacion",				title: $translate.instant('UI_TIPO_OPERACION'),	width: "30px"},
		 	{field: "InstrumentoUI",					title: $translate.instant('UI_NEMOTECNICO'),	width: "30px"},
			{field: "CantidadTransaccion",				title: $translate.instant('UI_CANTIDAD'),	width: "30px",  template : " <label> #:  (util.bcs_formato(CantidadDisponibleTransaccion,'GEN','Cantidad',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			{field: "PrecioTransaccion",				title: $translate.instant('UI_PRECIO'),	width: "30px",	template : " <label> #:  (util.bcs_formato(PrecioTransaccion,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			{field: "CantidadDisponibleTransaccion",	title: $translate.instant('UI_SALDO_DISPONIBLE'),	width: "30px",  template : " <label> #:  (util.bcs_formato(CantidadDisponibleTransaccion,'GEN','Cantidad',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>", footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Cantidad','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			{field: "MontoDisponibleTransaccion",		title: $translate.instant('UI_MONTO_DISPONIBLE'),	width: "30px",	template : " <label> #:  (util.bcs_formato(MontoDisponibleTransaccion,'GEN','Monto',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>", footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			{field: "NombreCondicionDeLiquidacion",		title: $translate.instant('UI_CONDICION_LIQUIDACION'),	width: "30px"},
			{field: "MarcaNegociacion",					title: $translate.instant('UI_MARCA_NEGOCIACION'),	width: "30px"},
			{field: "NombreBolsa",						title: $translate.instant('UI_BOLSA'),	width: "30px"},
			{field: "IdMonedaInstrumento",				title: $translate.instant('UI_MONEDA'),	width: "30px", hidden: "true"}
		],
 		gridCreated : function (e){
			$('#gridTransaccionesDetalle .k-grid-footer' ).css("display","block").removeAttr("style");
			bcsComunes.ventanaAutoHeight($scope.idventana);
		} 
	};
		
	$scope.formData.optionsGrillaTransaccionesDetalle = configGrilla;
	
	
	
	$scope.cerrar = function () {
		bcsComunes.ventanaCerrar($scope.idVentana); 
	};
});

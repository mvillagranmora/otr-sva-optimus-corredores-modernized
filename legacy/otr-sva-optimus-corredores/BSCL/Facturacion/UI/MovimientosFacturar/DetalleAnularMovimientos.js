angular.module('ModuloFacturacion').controller('DetalleAnularMovimientosCtrl',function ($scope,bcsComunes) {
	
	let idVentana = bcsComunes.ventanaWinId($scope);
	let parametros = bcsComunes.ventanaParams(idVentana);

	$scope.apiGrid;

	$scope.grid = {};

	$scope.grid.columnasDetalleMovimientos= function() {
		return  [
	   	    {field: "SecuenciaMovimiento",              title: "UI_SECUENCIA_MOVIMIENTO",   width: "75px" },
			{field: "IdClasificacionTipoInstrumento",   title: "UI_MERCADO",            	width: "75px" },
			{field: "TipoComprobante",              	title: "UI_TIPO_COMPROBANTE",       width: "75px" },
			{field: "Concepto",              			title: "UI_CONCEPTO",           	width: "150px" },
			{field: "Estado",              				title: "UI_ESTADO",            		width: "150px" },
			{field: "FolioTransaccion",              	title: "UI_FOLIO_TRANSACCION",      width: "150px" },
			{field: "FechaTransaccion",              	title: "UI_FECHA_TRANSACCION",      width: "150px" },
			{field: "SecuenciaOrden",              		title: "UI_SECUENCIA_ORDEN",        width: "150px" },
			{field: "Cliente",              			title: "UI_CLIENTE",            	width: "150px" },
			{field: "CodigoPortafolio",              	title: "UI_CODIGO_PORTAFOLIO",      width: "75px" },
			{field: "Operacion",              			title: "UI_OPERACION",            	width: "150px" },
			{field: "TipoOperacion",              		title: "UI_TIPO_OPERACION",         width: "150px" },
			{field: "Nemotecnico",              		title: "UI_NEMOTECNICO",            width: "150px" },
			{field: "Emisor",              				title: "UI_EMISOR",            		width: "150px" },
			{field: "Cantidad",              			title: "UI_CANTIDAD",            	width: "150px" ,template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,IdTipoInstrumento,'')).valor #</label>"},
			{field: "CondicionLiquidacion",             title: "UI_CONDICION_LIQUIDACION",  width: "150px" },
			{field: "MarcaAsignacion",              	title: "UI_MARCA_ASIGNACION",       width: "75px" },
			{field: "IdAsignacionMasiva",              	title: "UI_ID_ASIGNACION_MASIVA",   width: "75px" },
		];
	};

	$scope.grid.datasourceDetalleMovimientos = new bcs.DataSource({
		type : "array",
		definition : {
			data : parametros.detalleMovimientos,
			schema : {
				model : {
					fields : {	
						SecuenciaMovimiento 			: {type : "number"},
						IdClasificacionTipoInstrumento	: {type : "string"},
						TipoComprobante 				: {type : "string"},
						Concepto 						: {type : "string"},
						Estado 							: {type : "string"},
						FolioTransaccion 				: {type : "string"},
						FechaTransaccion 				: {type : "string"},
						SecuenciaOrden 					: {type : "number"},
						Cliente 						: {type : "string"},
						CodigoPortafolio			 	: {type : "string"},
						Operacion 						: {type : "string"},
						TipoOperacion					: {type : "string"},
						Nemotecnico						: {type : "string"},
						Emisor							: {type : "string"},
						Cantidad						: {type : "number"},
						CondicionLiquidacion			: {type : "string"},
						MarcaAsignacion                 : {type : "string"}, 	
						IdAsignacionMasiva              : {type : "number"}, 							
					}
				}
			}
		}
	})

	$scope.grid.configuracionDetalleMovimientosGrid = {
		dataSource			: $scope.grid.datasourceDetalleMovimientos,
		columns				: $scope.grid.columnasDetalleMovimientos(),
		sortable			: true,	
		pageable			: false,
		height				: 200,
		width				: 2000,
		selectable 			: "multiple, row",
		autoBind            : true                            
	};
	
	$scope.aceptar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};		
});
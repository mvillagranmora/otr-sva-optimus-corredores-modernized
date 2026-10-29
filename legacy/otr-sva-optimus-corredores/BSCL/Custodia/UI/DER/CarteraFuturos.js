angular.module('ModuloCustodia').controller('CarteraFuturosCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	$scope.datos = {}
	
	$scope.datos.portafolioSeleccionado = null;
	$scope.datos.fecha = $scope.SesionFechaSistema;
	
	$scope.datos.disableBtn = true;
	
	$scope.$watch('datos.fecha', function(valorFecha){

		if(valorFecha != null){
			$scope.datos.disableBtn = true;
		}
		else{
			$scope.datos.disableBtn = false;
		}
	});

	let getColumns = function(){
		return [
			{field: 'FolioCartera', title: 'UI_FOLIO_CARTERA', width: '70px'},
			{field: 'DocumentoIdentidad', title: 'UI_DOCUMENTO_IDENTIDAD', width: '70px', groupable: true},
			{field: 'RazonSocial', title: 'UI_RAZON_SOCIAL', width: '70px'},
			{field: 'CodigoPortafolio', title: 'UI_PORTAFOLIO', width: '70px'},
			{field: 'Instrumento', title: 'UI_INSTRUMENTO', width: '70px', groupable: true},
			{field: 'TipoInstrumento', title: 'UI_TIPO_INSTRUMENTO', width: '70px'},
			{field: 'Tipo', title: 'UI_COMPRA_VENTA', width: '70px', groupable: true,  groupHeaderTemplate: function(dataItem){
				if(dataItem.value === 'COMPRA')
					return '<span style="color:green;">COMPRA</span>';
				if(dataItem.value === 'VENTA')
					return '<span style="color:red;">VENTA</span>';
				
			}},
			{field: 'Cantidad', title: 'UI_CONTRATOS', width: '70px'},
			{field: 'FechaOperacion', title: 'UI_FECHA', width: '70px', template: "#= kendo.toString(FechaOperacion, 'dd-MM-yyyy') #"},
			{field: 'FechaVencimientoOperacion', title: 'UI_FECHA_VENCIMIENTO', width: '70px', template: "#= kendo.toString(FechaVencimientoOperacion, 'dd-MM-yyyy') #"},
			{field: 'PrecioCompra', title: 'UI_PRECIO_COMPRA', width: '70px'},
			{field: 'TasaCompra', title: 'UI_TASA_COMPRA', width: '70px'},
			{field: 'TasaMercado', title: 'UI_TASA_MERCADO', width: '70px'},
			{field: 'MontoCompra', title: 'UI_MONTO_COMPRA', width: '70px'},
			{field: 'ValorMercado', title: 'UI_VALOR_MERCADO', width: '70px'},
			{field: 'FolioTransaccion', title: 'UI_FOLIO_TRANSACCION', width: '70px'},
			{field: 'Liquidacion', title: 'UI_LIQUIDACION', width: '70px'},
			{field: 'FechaLiquidacion', title: 'UI_FECHA_LIQUIDACION', width: '70px', template: "#= kendo.toString(FechaLiquidacion, 'dd-MM-yyyy') #"},
			{field: 'Agente', title: 'UI_AGENTE', width: '70px'},
			{field: 'NombreSucursal', title: 'UI_SUCURSAL', width: '70px'},
			{field: 'BolsaOrigen', title: 'UI_BOLSA', width: '70px'},
			{field: 'Documento', title: 'UI_TIPO_DOCUMENTO', width: '70px'},
			{field: 'FolioComprobante', title: 'UI_FOLIO_COMPROBANTE', width: '70px'},
			{field: 'MargenExigido', title: 'UI_MARGEN_EXIGIDO', width: '70px'},
			{field: 'PrecioBaseFuturos', title: 'UI_PRECIO_BASE_FUTUROS', width: '70px'},
			{field: 'ResultadoDiario', title: 'UI_RESULTADO_DIARIO', width: '70px'},
			{field: 'ResultadoAcumulado', title: 'UI_RESULTADO_ACUMULADO', width: '70px'},

		];
	}
	
	let getSechma = function(){
		return {
			model: {
				fields: {
					DocumentoIdentidad: {type: 'string'},
					RazonSocial: {type: 'string'},
					CodigoPortafolio: {type: 'string'},
					Instrumento: {type: 'string'},
					TipoInstrumento: {type: 'string'},
					Tipo: {type: 'string'},
					Cantidad: { type: 'number'},
					FechaOperacion: {type: 'date'},
					FechaVencimientoOperacion: {type: 'date'},
					PrecioCompra: { type: 'number'},
					TasaCompra: { type: 'number'},
					TasaMercado: { type: 'number'},
					MontoCompra: { type: 'number'},
					ValorMercado: { type: 'number'},
					FolioTransaccion: { type: 'number'},
					FechaLiquidacion: { type: 'date'},
					Agente: { type: 'string'},
					NombreSucursal: { type: 'string'},
					BolsaOrigen: { type: 'string'},
					Documento: { type: 'string'},
					FolioComprobante: { type: 'number'},
					PrecioBaseFuturos: { type: 'number'},
					Liquidacion: { type: 'string'},
					MargenExigido: { type: 'number'},
					ResultadoDiario: { type: 'number' },
					ResultadoAcumulado: { type: 'number' },
					FolioCartera: { type: 'number'}
				}
			}
		}
	}

	let dataSourceCompras = new bcs.DataSource({
		type : "call",
		definition : {
			callResultSet	: "P_OUT_CARTERA_FUTURO",
			callTransaction	: "700142",
			serverFiltering : true,			
			schema : getSechma(),
			group: [
				//{field: 'TipoPosicion'},
				//{field: 'Instrumento'},
				{field: 'Tipo'}
			]
		}
	});

	$scope.buscar = function(){			
		
		parametrosSP = {};
		
		parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;

		parametrosSP.p_IdModulo = 'CUS';
		parametrosSP.p_tipoFecha = 'ADQ';
		parametrosSP.p_FechaConsulta =  util.date($scope.datos.fecha);
		
		parametrosSP.p_IdClasificacionTipoInstrumento = ""; 
		parametrosSP.p_IdTipoInstrumento = "";
		parametrosSP.p_IdInstrumento = 0;			
		
		parametrosSP.p_IdCliente = $scope.datos.portafolioSeleccionado.IdCliente;
		parametrosSP.p_IdPortafolio =  $scope.datos.portafolioSeleccionado.IdPortafolio;
		parametrosSP.p_TipoDerivado = 'FU';	

		dataSourceCompras.setCallParameters(parametrosSP);
		
	}

	$scope.confCompras = {
		dataSource: dataSourceCompras,
		columns: getColumns(),		
		allowCopy: { delimeter: "\t" },
		pageable: true,
		sortable: true,
		groupable: true,
		filterable: true,
		height: 600,
		excel: {allPages: true,},
		selectable: "row",		
	};
	
});
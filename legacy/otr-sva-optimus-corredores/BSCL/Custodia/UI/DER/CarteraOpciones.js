angular.module('ModuloCustodia').controller('CarteraOpcionesCtrl',function($scope,bcsComunes,$timeout,$compile){
	
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
			{field: 'SocioNegocio', title: 'UI_SOCIO_NEGOCIO', width: '70px', groupable: true},
			{field: 'CodigoPortafolio', title: 'UI_PORTAFOLIO', width: '70px', groupable: true},
			{field: 'FolioCartera', title: 'UI_FOLIO_CARTERA', width: '70px', hidden: true},
			{field: 'Instrumento', title: 'UI_INSTRUMENTO', width: '70px', groupable: true},
			{field: 'Tipo', title: 'UI_COMPRA_VENTA', width: '70px', groupable: true,  groupHeaderTemplate: function(dataItem){
				if(dataItem.value === 'COMPRA')
					return '<span style="color:green;">POSICIONES QUE OTORGAN DERECHO (COMPRA)</span>';
				if(dataItem.value === 'VENTA')
					return '<span style="color:red;">POSICIONES QUE OTORGAN OBLIGACIONES (VENTA)</span>';
			}},
			{field: 'Contratos', title: 'UI_CONTRATOS', width: '70px'},
			{field: 'TipoOpcion', title: 'UI_TIPO_OPCION', width: '70px', groupable: true, template: function(dataItem){
				if(dataItem.TipoOpcion === 'CALL')
					return '<div style="color:green;">' + dataItem.TipoOpcion + '</div>';
				if(dataItem.TipoOpcion === 'PUT')
					return '<div style="color:red;">' + dataItem.TipoOpcion + '</div>';				
			}},
			{field: 'Mercado', title: 'UI_MERCADO', width: '70px'},
			{field: 'Familia', title: 'UI_FAMILIA', width: '70px'},
			{field: 'Subyacente', title: 'UI_SUBYACENTE', width: '70px'},
			{field: 'Estilo', title: 'UI_ESTILO_OPCION', width: '70px', groupable: true},
			{field: 'ModalidadLiquidacion', title: 'UI_MODALIDAD', width: '70px', groupable: true},
			{field: 'FechaVencimiento', title: 'UI_FECHA_VENCIMIENTO', width: '70px', template: "#= kendo.toString(FechaVencimiento, 'dd-MM-yyyy') #"},
			{field: 'PrecioEjercicio', title: 'UI_PRECIO_EJERCICIO', width: '70px'},
			{field: 'PrecioSubyacente', title: 'UI_PRECIO_SUBYACENTE', width: '70px'},
			{field: 'FechaInver', title: 'UI_FECHA_INVER', width: '70px', template: "#= kendo.toString(FechaInver, 'dd-MM-yyyy') #"},
			{field: 'ContratosEjercidosCCLV', title: 'UI_CONTRATO_EJERCIDOS', width: '70px'},
			{field: 'ContratosSorteados', title: 'UI_CONTRATO_SORTEADOS', width: '70px'},
			{field: 'TipoPosicion', title: 'UI_TIPO_POSICION', width: '70px', groupable: true, groupHeaderTemplate: function(dataItem){				
				if(dataItem.value === 'TRANSACCION')
					return 'TRANSACCIONES DEL DIA';
				if(dataItem.value === 'POSICION')
					return 'POSICIONES (INICIO DEL DIA)';
			}, hidden: true},
			{field: 'Cuadratura', title: 'UI_CUADRATURA', width: '70px', template: function(dataItem){
				if(dataItem.Cuadratura === 'OK')
					return '<div style="color:green;">' + dataItem.Cuadratura + '</div>';
				else if(dataItem.Cuadratura === 'PEN')
					return '<div>' + dataItem.Cuadratura + '</div>';
				else{
					return '<div style="color:red;">' + dataItem.Cuadratura + '</div>';				
				}
			}}
			
		];
	}
	
	let getSechma = function(){
		return {
			model: {
				fields: {
					DocumentoIdentidad: { type: 'string'},
					SocioNegocio: {type: 'string'},
					CodigoPortafolio: {type: 'string'},
					FolioCartera: { type: 'number'},
					Instrumento: { type: 'string'},
					Tipo: { type: 'string'},
					Contratos: { type: 'number'},
					TipoOpcion: { type: 'string'},
					Mercado: { type: 'string'},
					Familia: { type: 'string'},
					Subyacente: { type: 'string'},
					Estilo: { type: 'string'},
					ModalidadLiquidacion: { type: 'string'},
					FechaVencimiento: {type: 'date'},
					PrecioEjercicio: {type: 'number'},
					PrecioSubyacente: {type: 'number'},
					FechaInver: {type: 'date'},
					ContratosEjercidosCCLV: { type: 'number'},
					ContratosSorteados: { type: 'number'},
					TipoPosicion: {type: 'string'},
					Cuadratura: {type: 'string'},
					FolioCartera: { type: 'number'}
				}
			}
		}
	}

	let dataSourceCompras = new bcs.DataSource({
		type : "call",
		definition : {
			callResultSet	: "P_OUT_CARTERA",
			callTransaction	: "700142",
			serverFiltering : true,			
			schema : getSechma(),
			group: [
				{field: 'TipoPosicion'},
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
		parametrosSP.p_TipoDerivado = 'OP';	

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
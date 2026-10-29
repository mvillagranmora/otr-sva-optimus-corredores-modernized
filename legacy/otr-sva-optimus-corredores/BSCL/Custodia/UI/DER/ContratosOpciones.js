angular.module('ModuloCustodia').controller('ContratosOpcionesCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	$scope.datos = {}
	
	$scope.datos.portafolioSeleccionado = null;
	$scope.datos.fecha = util.dateInicioDia($scope.SesionFechaSistema);
	
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
			{field: 'DocumentoIdentidad', title: 'UI_DOCUMENTO_IDENTIDAD', width: '70px'},
			{field: 'PortafolioDerivados', title: 'UI_PORTAFOLIO', width: '70px'},
			{field: 'Serie', title: 'UI_SERIE', width: '150px'},
			{field: 'IdTipoOperacion', title: 'UI_TIPO_OPERACION', width: '70px'},
			{field: 'ContratosEjercidos', title: 'UI_CONTRATOS', width: '70px'},
			
			{field: 'EsPutEsCall', title: 'UI_TIPO', width: '70px', groupable: true, template: function(dataItem){
				if(dataItem.EsPutEsCall === 'CALL')
					return '<div style="color:green;">' + dataItem.EsPutEsCall + '</div>';
				if(dataItem.EsPutEsCall === 'PUT')
					return '<div style="color:red;">' + dataItem.EsPutEsCall + '</div>';				
			}},
			
			{field: 'CodigoClasificacion', title: 'UI_MERCADO', width: '100px'},
			{field: 'IdTipoInstrumento', title: 'UI_FAMILIA	', width: '160px'},
			{field: 'Subyacente', title: 'UI_SUBYACENTE', width: '70px'},
			{field: 'EstiloOpcion', title: 'UI_ESTILO', width: '70px'},
			{field: 'ModalidadLiquidacion', title: 'UI_MODALIDAD_LIQ', width: '70px'},
			{field: 'FechaVencimiento', title: 'UI_FECHA_VENCIMIENTO', width: '90px', template: "#= kendo.toString(FechaVencimiento, 'dd-MM-yyyy') #"},
			{field: 'Strike', title: 'UI_PRECIO', width: '70px'},
			{field: 'PrecioSubyacente', title: 'UI_PRECIO_SUBYACENTE', width: '120px'},
			{field: 'ContratosSorteados', title: 'UI_CONTRATOS_SORTEADOS', width: '120px'},
			{field: 'PerdidaGanancia', title: 'UI_PERDIDA_GANANCIA', width: '120px'},
			{field: 'Observacion', title: 'UI_OBSERVACION', width: '300px'},
		];
	}
	
	let getSechma = function(){
		return {
			model : {
				fields : {
					DocumentoIdentidad: { type: 'string'},
					PortafolioDerivados:{ type: 'number'},
					Serie: 				{ type: 'string'},
					IdTipoOperacion:	{ type: 'string'},
					ContratosEjercidos: { type: 'number'},
					EsPutEsCall:		{ type: 'string'},
					CodigoClasificacion:{ type: 'string'},
					IdTipoInstrumento:	{ type: 'string'},
					Subyacente: 		{ type: 'string'},
					EstiloOpcion:		{ type: 'string'},
					ModalidadLiquidacion:{ type: 'string'},
					FechaVencimiento: 	{ type: 'date'},
					Strike: 			{ type: 'number'},
					PrecioSubyacente: 	{ type: 'number'},
					ContratosSorteados: { type: 'number'},
					PerdidaGanancia: 	{ type: 'number'},
					Observacion: 		{ type: 'string'},
				}
			}
		}
	}

	let dataSourceCompras = new bcs.DataSource({
		type : "call",
		definition : {
			callResultSet	: "P_OUT_CONTRATOS",
			callTransaction	: "700155",
			serverFiltering : true,			
			schema : getSechma(),
			group: [
				{field: 'ModalidadLiquidacion'}
			]
		}
	});

	$scope.buscar = function(){			
		
		parametrosSP = {};
		
		parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;		
		parametrosSP.p_IdModulo = 'CUS';
		parametrosSP.p_tipoFecha = 'ADQ';
		parametrosSP.p_FechaConsulta =  util.dateSPdate($scope.datos.fecha);
		
		parametrosSP.p_IdClasificacionTipoInstrumento = ""; 
		parametrosSP.p_IdTipoInstrumento = "";
		parametrosSP.p_IdInstrumento = 0;	
		
		parametrosSP.p_IdProductoAgrupador = 0;
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
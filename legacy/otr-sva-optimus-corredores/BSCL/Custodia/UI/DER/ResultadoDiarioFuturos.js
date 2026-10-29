angular.module('ModuloCustodia').controller('ResultadoDiarioFuturosCtrl',function($scope,bcsComunes,$timeout,$compile){
	
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
	
	$scope.columnasResultadoDiario = function(){
		return [
			{field: 'DocumentoIdentidad', 	title: 'UI_DOCUMENTO_IDENTIDAD', 	width: '120px', sortable : false},
			{field: 'CodigoPortafolio', 	title: 'UI_PORTAFOLIO', 			width: '70px'},
			{field: 'Nombre', 				title: 'UI_NOMBRE', 				width: '280px'},
			{field: 'Nemotecnico', 			title: 'UI_NEMOTECNICO', 			width: '100px'},
			{field: 'FechaVencimiento', 	title: 'UI_FECHA_VENCIMIENTO', 		width: '90px', template: "#= kendo.toString(FechaVencimiento, 'dd-MM-yyyy') #"},			
			{field: 'Fecha', 				title: 'UI_FECHA', 					width: '90px', template: "#= kendo.toString(Fecha, 'dd-MM-yyyy') #"},			
			{field: 'InteresAbierto', 		title: 'UI_INTERES_ABIERTO', 		width: '100px'},
			{field: 'CantidadContratos',	title: 'UI_CANTIDAD_CONTRATOS', 	width: '100px',template : " <label> #:  (util.bcs_formato(CantidadContratos,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'TamanoContratos', 		title: 'UI_TAMANO_CONTRATOS', 		width: '100px',template : " <label> #:  (util.bcs_formato(TamanoContratos,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'PrecioAnterior', 		title: 'UI_PRECIO_ANTERIOR', 		width: '100px',template : " <label> #:  (util.bcs_formato(PrecioAnterior,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'PrecioBase', 			title: 'UI_PRECIO_BASE', 			width: '100px',template : " <label> #:  (util.bcs_formato(PrecioBase,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'Resultado', 			title: 'UI_RESULTADO', 				width: '100px',template : " <label> #:  (util.bcs_formato(Resultado,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'TipoMovimiento', 		title: 'UI_TIPO_MOVIMIENTO', 		width: '100px'},			
			{field: 'Abono', 				title: 'UI_ABONO', 					width: '100px',template : " <label> #:  (util.bcs_formato(Abono,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'Cargo', 				title: 'UI_CARGO', 					width: '100px',template : " <label> #:  (util.bcs_formato(Cargo,'GEN','Cantidad','CLP', '','')).valor #</label>"},
			{field: 'Saldo', 				title: 'UI_SALDO', 					width: '100px',template : " <label> #:  (util.bcs_formato(Saldo,'GEN','Cantidad','CLP', '','')).valor #</label>"},			
			{field: 'DescMovimiento', 		title: 'UI_DESC_MOVIMIENTO', 		width: '300px'},
		];
	}

	var dataSourceResultadoDiario = new bcs.newDataSource({
		type : "call",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			pageSize			: 200,
			callTransaction		: 4290612,
			callResultSet	: "P_OUT_RESULTADO_DIARIO_FUTUROS",
			schema : {
				model : {
					fields : {
						DocumentoIdentidad: { type: 'string'},
						PortafolioDerivados:{ type: 'number'},
						CodigoPortafolio:	{ type: 'string'},
						Nombre:				{ type: 'string'},
						Nemotecnico: 		{ type: 'string'},
						FechaVencimiento: 	{ type: 'date'},
						Fecha: 				{ type: 'date'},
						InteresAbierto:		{ type: 'string'},
						CantidadContratos: 	{ type: 'number'},
						TamanoContratos: 	{ type: 'number'},
						PrecioAnterior: 	{ type: 'number'},
						PrecioBase: 		{ type: 'number'},
						Resultado: 			{ type: 'number'},					
						TipoMovimiento:		{ type: 'string'},					
						Abono: 				{ type: 'number'},
						Cargo: 				{ type: 'number'},
						Saldo: 				{ type: 'number'},
						DescMovimiento:		{ type: 'string'}
					}
				}
			}
		}
	});
	$scope.buscar = function(){			
		
		parametrosSP = {};
		parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;		
		parametrosSP.p_IdModulo = 'CUS';
		parametrosSP.p_FechaConsulta =  util.dateSPdate($scope.datos.fecha);
		
		parametrosSP.p_IdPortafolio =  $scope.datos.portafolioSeleccionado.IdPortafolio || 0;
		dataSourceResultadoDiario.setCallParameters(parametrosSP);
		
	}
	
	$scope.limpiarFiltros = function(){
		$scope.datos.fecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.ordenOP.datosForm.configuracionPortafolio.idActual = false;
		dataSourceResultado.removeAll();
	};	

	$scope.confResultadoDiario = {
		dataSource: dataSourceResultadoDiario,
		columns		: $scope.columnasResultadoDiario(),
		sortable	: true,
		pageable	: true,   //pagina localmente
		filterable 	: true,
		selectable 	: "row",
		reorderable	: true,
		userSettings : true,
		groupable	: true,
		height		: 600,
		allowCopy	: { delimeter: "\t" },
		resizable	: true,
	    autoBind	: false,
	    excel : {
	        allPages: true,
	      },

	};
	
});
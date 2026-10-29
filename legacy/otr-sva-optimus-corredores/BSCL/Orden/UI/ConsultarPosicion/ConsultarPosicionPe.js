angular.module('ModuloOrdenes').controller('ConsultarPosicionPeCtrl',function($scope,bcsComunes,bcsServicioOrden){
	var controllerName = 'ConsultarPosicionPeCtrl';
	
	$scope.datosForm = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.datosForm.filtrosClasificacion = {
			logic : "or",
			RV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
			RF : 	{"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
			IF : 	{"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
			DV : 	{"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"}
			
	};
	
	$scope.datosForm.consultarDemanda = function() {
		
		if (!$scope.datosForm.seleccionado || !$scope.datosForm.seleccionado[0])
			return;
		
		var obj = {
				FechaConsulta 		: $scope.SesionFechaSistema,
				IdInstrumento		: $scope.datosForm.seleccionado[0].IdInstrumento,
				EsVentaCorta		: $scope.datosForm.seleccionado[0].EsVentaCorta,
				IdTipoInstrumento	: $scope.datosForm.seleccionado[0].IdTipoInstrumento,
		}
		
		bcsComunes.ventanaAbrir("ConsultarDemanda", obj ,false,false,$scope.idVentana);
		
	}
	
	$scope.datosForm.buscar = function() {
		$scope.datosForm.apiGridPosicion.dataSource.data();
		var datos = {
				p_IdEmpresa 	: $scope.SesionIdEmpresa,
				p_IdUsuario	 	: $scope.SesionIdUsuario,
				p_IdModulo 		: "ORD",
				p_FechaConsulta : $scope.SesionFechaSistema,
				p_IdClasificacionTipoInstrumento 	: $scope.datosForm.IdClasificacionTipoInstrumento || 'RV',
		}
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.ConsultarPosicionPe(datos).$promise.then(function(result){
			$scope.datosForm.apiGridPosicion.dataSource.data(result.P_OUT_POSICION);
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);	
		});
		
		
	}
	
	$scope.datosForm.confGridPosicion = {
			autoBind: false,
      		allowCopy: true,
      		filterable: true,
			groupable: true,
			height:380,
      		reorderable: true,
			resizable: true,
      		selectable: "multiple, row",
      		scrollable: true,
      		sortable: true,
      		userSettings:true,
			navigatable: true,
			gridCreated : function(grid) {
				$scope.datosForm.apiGridPosicion = grid;
				$scope.datosForm.buscar();
			},
      		 
			dataSource : new bcs.DataSource({
				type : "array",
				
				definition : {
					serverSorting : false,
					sort : [

						{ field: "IdTipoInstrumento", dir: "asc" },
						{ field: "Nemotecnico", dir: "asc" },
						{ field: "EsVentaCorta", dir: "asc" }
					],
				
					data : [],
					schema : {
						model : {
							fields : {	
								IdClasificacionTipoInstrumento	: {type : "string"},
								IdTipoInstrumento				: {type : "string"},
								NombreTipoInstrumento			: {type : "string"},
								IdInstrumento 					: {type : "number"},
								Nemotecnico 					: {type : "string"},
								EsVentaCorta 					: {type : "string"},
								EsVentaCortaUI 					: {type : "string"},
								OrdenadoCompra 					: {type : "number"},
								OrdenadoVenta 					: {type : "number"},
								TransadoCompra 					: {type : "number"},
								TransadoVenta			 		: {type : "number"},
								PorComprar 						: {type : "number"},
								PorVender						: {type : "number"},
								IdMoneda						: {type : "string"},
								
							}
						}
					}
				}
				
			}),
  			columns: [
  				{field: "NombreTipoInstrumento",					title: 'UI_TIPO_INSTRUMENTO',	width: "100px",},
  				{field: "Nemotecnico",							title: 'UI_NEMOTECNICO',	width: "125px",},
  				{field: "EsVentaCortaUI",							title: 'UI_ES_VENTA_CORTA',	width: "75px",},

  				{field: "OrdenadoCompra",		title: 'UI_ORDENADO_COMPRA',	width: "100px",template : " <label> #:  (util.bcs_formato(OrdenadoCompra,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
  				{field: "OrdenadoVenta",		title: 'UI_ORDENADO_VENTA',	width: "100px",template : " <label> #:  (util.bcs_formato(OrdenadoVenta,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
  				{field: "TransadoCompra",		title: 'UI_TRANSADO_COMPRA',	width: "100px",template : " <label> #:  (util.bcs_formato(TransadoCompra,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
  				{field: "TransadoVenta",		title: 'UI_TRANSADO_VENTA',	width: "100px",template : " <label> #:  (util.bcs_formato(TransadoVenta,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
  				{field: "PorComprar",			title: 'UI_POR_COMPRAR',	width: "100px",template : " <label> #:  (util.bcs_formato(PorComprar,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
  				{field: "PorVender",			title: 'UI_POR_VENDER',	width: "100px",template : " <label> #:  (util.bcs_formato(PorVender,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
			],
			
		};
	
});

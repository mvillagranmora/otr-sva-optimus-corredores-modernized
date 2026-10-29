angular.module('ModuloOrdenes').controller('ConsultarDemandaAsignacionCtrl',function($scope,bcsComunes,bcsServicioOrden){
	var controllerName = 'ConsultarDemandaAsignacionCtrl';
	
	$scope.datosForm = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	
	$scope.datosForm.buscar = function() {
		$scope.datosForm.apiGridDemanda.dataSource.data();
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdUsuario	 		: $scope.SesionIdUsuario,
				p_IdModulo 			: "ORD",
				
				p_FechaConsulta 	: parametros.FechaConsulta,
				p_IdInstrumento		: parametros.IdInstrumento,
				p_EsVentaCorta		: parametros.EsVentaCorta,
				p_IdTipoInstrumento	: parametros.IdTipoInstrumento
		}
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.ConsultarDemandaAsignacion(datos).$promise.then(function(result){
			$scope.datosForm.apiGridDemanda.dataSource.data(result.P_OUT_DEMANDA);
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);	
		});
		
		
	}
	
	$scope.datosForm.confGridDemanda = {
			autoBind: false,
      		allowCopy: true,
      		filterable: true,
			groupable: true,
			height:400,
      		reorderable: true,
			resizable: true,
      		selectable: "multiple, row",
      		scrollable: true,
      		sortable: true,
      		userSettings:true,
			navigatable: true,
			gridCreated : function(grid) {
				$scope.datosForm.apiGridDemanda = grid;
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
								NombreTipoInstrumento			: {type : "string"},
								AccionOrden 					: {type : "string"},
								Nemotecnico 					: {type : "string"},
								Cantidad	 					: {type : "number"},
								NombreTipoPrecio	 			: {type : "string"},
								Precio	 						: {type : "number"},
								IdMoneda						: {type : "string"},
								
							}
						}
					}
				}
				
			}),
  			columns: [
  				{field: "NombreTipoInstrumento",	title: 'UI_TIPO_INSTRUMENTO',	width: "100px"},
  				{field: "Nemotecnico",				title: 'UI_NEMOTECNICO',		width: "125px"},
  				{field: "AccionOrden",				title: 'UI_COMPRA_VENTA',		width: "75px"},
  				{field: "Cantidad",					title: 'UI_CANTIDAD',			width: "75px",template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
  				{field: "Precio",					title: 'UI_PRECIO',				width: "75px",template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
  				{field: "IdMoneda",					title: 'UI_MONEDA',				width: "75px"}
  				
			],
			
		};
	
});

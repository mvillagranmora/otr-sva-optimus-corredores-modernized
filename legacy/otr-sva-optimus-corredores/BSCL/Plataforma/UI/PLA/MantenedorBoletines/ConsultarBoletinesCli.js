angular.module('ModuloPlataforma').controller('ConsultarBoletinesCliCtrl', 
		function($scope,bcsComunes,$translate,$http){
	$scope.data = {};
	$scope.parametros = {};

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	bcsComunes.ventanaAutoHeight(idVentana);
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	$scope.confGridBoletin = {
		dataSource			: new bcs.DataSource({
								type 		: "odata", //Los tipos posibles son: odata, call, array y object
								definition 	: {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.
									groupable 			: true, 
									pageSize 			: 25, // Número de registros por página.
									sort 				: {field: "FechaModificacion", dir: "desc"},
									filter :  // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
										[
										 	{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
										 ]
									,
									transport 			: {
															read : {
																url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3710",
																dataType : "json",
																data:{
																	$select: "\"CodigoArchivo\",\"Descripcion\",\"FechaModificacion\",\"CodigoUsuario\", \"TipoBinario\",\"TipoPresentacion\""
																}
															}
														},
									schema 				: {
										model: {
											fields : {
												CodigoArchivo : {type: "string"},
												CodigoUsuario: {type: "string"},
												Descripcion : {type: "string"},
												FechaModificacion : {type: "date"},
												IdUsuario : {type: "string"},
												Descargar : {type: "string"}
											}
										}
									}
								}
							}),
		
		columns				: [
		       				   	
								{field: "Descripcion",title: "UI_NOMBRE",width: "150px", hidden:false},
								{field: "Descargar",title: "UI_DESCARGAR",width: "100px", hidden:false, template: kendo.template("<a class='fileDownload' >Descargar</a>")},
								
						],
		gridCreated: function(grid) {
			$scope.data.mainGrid = grid;
		},
		
		userSettings		: true ,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		sortable			: true,
		pageable			: true,
		filterable			: true,
		groupable           : true,
		autoBind			: true,
		height				: 300,
		selectable 			: "row",
		dataBound			: function(e) {
			var grid = $scope.data.mainGrid;
			if (grid) {
				grid.tbody.find(".fileDownload").each(function() {
					$(this).on("click", function() {
						var dataItem = grid.dataItem($(this).closest("tr"));
						$scope.downloadBoletin(dataItem);
					});
				});
			}
		},
	};
	
	$scope.search = function(){
		var filters = new Array();
		
		if($scope.data.ReportName!= ""  && $scope.data.ReportName)
			filters.push({ field: "toupper(Descripcion)", operator: "contains", value: $scope.data.ReportName });
		
		$scope.data.mainGrid.dataSource.filters(filters);
	};
	

	
	

	$scope.downloadBoletin = function(dataItem){

		bcsComunes.ventanaOverlay(idVentana,true);
		var config = {
			responseType: "arraybuffer",
			params:{
				"filename": dataItem.CodigoArchivo
			}
		}
		
		$http.get('/BSCL/Plataforma/GWJAVA/PLA/Boletines', config)
		  .success(function (response) {
			   var file = new Blob([response], {type: dataItem.TipoPresentacion});
			   //var fileURL = URL.createObjectURL(file);
			   saveAs(file, dataItem.Descripcion+"."+dataItem.TipoBinario);
			   $scope.data.mainGrid.dataSource.read();
			   bcsComunes.ventanaOverlay(idVentana,false);
		}).error(function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
		
	};


	
});
angular.module('ModuloPlataforma').controller('ConsultarTemplatesCtrl', 
		function($scope,bcsComunes,$translate,$http){
	$scope.data = {};
	$scope.parametros = {};

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	$scope.confGridTemplate = {
		dataSource			: new bcs.DataSource({
								type 		: "odata", //Los tipos posibles son: odata, call, array y object
								definition 	: {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.
									groupable 			: true, 
									pageSize 			: 25, // Número de registros por página.
									filter :  // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
										[
										 	{ field: "CompanyId", operator: "eq", value: $scope.SesionIdEmpresa},
										 ]
									,
									transport 			: {
															read : {
																url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
																dataType : "json",
																data:{
																	$select: "\"Id\",\"ReportName\",\"ReportVersion\""
																}
															}
														},
									schema 				: {
										model: {
											fields : {
												Id : {type: "string"},
												ReportName : {type: "string"},
												ReportVersion : {type: "string"}
											}
										}
									}
								}
							}),
		columns				: [
		       				   	{field: "Id",title: "UI_ID",width: "150px", hidden:true},
								{field: "ReportName",title: "UI_NOMBRE",width: "150px", hidden:false},
								{field: "ReportVersion",title: "UI_VERSION",width: "150px", hidden:false}
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
		selectable 			: "row"
	};
	
	$scope.search = function(){
		var filters = new Array();
		filters.push({ field: "CompanyId", operator: "eq", value: $scope.data.IdEmpresa });
		
		if($scope.data.ReportName && $scope.data.ReportName != "")
			filters.push({ field: "toupper(ReportName)", operator: "contains", value: $scope.data.ReportName });
		
		$scope.data.mainGrid.dataSource.filters(filters);
	};
	
	
	$scope.uploadTemplate = function(){
		var data = {operacion: "CREATE"};
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("OperarTemplateModal", data, true, false);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			$scope.data.mainGrid.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.updateTemplate = function(){
		var data = $scope.seleccionTemplate;
		data.operacion = "UPDATE";
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("OperarTemplateModal", data, true, false);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			$scope.seleccionImagen = null;
			$scope.data.mainGrid.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.downloadTemplate = function(){
		bcsComunes.ventanaOverlay(idVentana,true);

		var parameters = {
			"reportId" : $scope.seleccionTemplate.Id,
			"debug": true
		  };
		//var config = {responseType: "arraybuffer"}
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?rpt', parameters, {responseType: "arraybuffer"})
		  .success(function (response) {
			   var file = new Blob([response], {type: ""});
			   //var fileURL = URL.createObjectURL(file);
			   saveAs(file, $scope.seleccionTemplate.ReportName+".rpt");
			   $scope.seleccionTemplate = null;
			   bcsComunes.ventanaOverlay(idVentana,false);
		}).error(function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	
	
	
});
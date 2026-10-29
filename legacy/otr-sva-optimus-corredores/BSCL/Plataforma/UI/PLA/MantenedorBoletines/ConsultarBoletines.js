angular.module('ModuloPlataforma').controller('ConsultarBoletinesCtrl', 
		function($scope,bcsComunes,$translate,$http,bcsAlerta, OPCIONES_BCSALERTA){
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
								{field: "FechaModificacion",title: "UI_FECHA",width: "150px", hidden:false},
								{field: "CodigoUsuario",title: "UI_USUARIO",width: "75px", hidden:false},
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
		
		if($scope.data.ReportName!= "" && $scope.data.ReportName)
			filters.push({ field: "toupper(Descripcion)", operator: "contains", value: $scope.data.ReportName });
		
		$scope.data.mainGrid.dataSource.filters(filters);
	};
	
	
	$scope.uploadBoletin = function(){
		var data = {operacion: "CREATE"};
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("OperarBoletinModal", data, true, false);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			//$scope.data.mainGrid.dataSource.read();
			$scope.search();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
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
			   saveAs(file, dataItem.Descripcion+"."+dataItem.TipoBinario);
			   $scope.data.mainGrid.dataSource.read();
			   bcsComunes.ventanaOverlay(idVentana,false);
		}).error(function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
		
	};

	$scope.eliminar = function(){
		var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_PREGUNTA_ELIMINAR_BOLETIN'), idVentana);
				t.then(function(result){
    				if (result === OPCIONES_BCSALERTA.SI) {
						$scope.deleteBoletin();
						$scope.seleccionBoletin = undefined;
   			 }
		});
	};

	$scope.deleteBoletin = function(){
		var archivos = $scope.seleccionBoletin;
		console.log("archivos : ", archivos);
		if(archivos){
			console.log("Entré");
			bcsComunes.ventanaOverlay(idVentana,true);

			var formData = new FormData();


			formData.append('OP', 'DELETE');
			formData.append('IdEmpresa', $scope.SesionIdEmpresa);
			formData.append('IdUsuario', $scope.SesionIdUsuario);
			formData.append('code', $scope.seleccionBoletin.CodigoArchivo);
						
			$.ajax({
				method: "POST",
	        	url: "/BSCL/Plataforma/GWJAVA/PLA/Boletines",
	        	data: formData,
	        	contentType : false,
	        	processData: false,
	        	cache: false
	        }).done(function( msg ) {
	        	if(msg==="OK"){
	        		bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_ELIMINADO_CORRECTAMENTE"));
					$scope.data.mainGrid.dataSource.read();

	        	}else{
	        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_ELIMINADO_INCORRECTAMENTE"));
	        	}
	        	bcsComunes.ventanaOverlay(idVentana,false);
        	}).error(function(msg){
        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_ELIMINADO_INCORRECTAMENTE"));
				bcsComunes.ventanaOverlay(idVentana,false);
        	});
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
	};

	
});
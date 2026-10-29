angular.module('ModuloPlataforma').controller('ConsultarImagenesCtrl', 
		function($scope,bcsComunes,$translate,$resource,$http){
	$scope.data = {};
	$scope.parametros = {};

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	$scope.confGridImagenes = {
		dataSource			: new bcs.DataSource({
								type 		: "odata", //Los tipos posibles son: odata, call, array y object
								definition 	: {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.
									groupable 			: true, 
									//sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
									pageSize 			: 25, // Número de registros por página.
									filter :  // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
										[
										 	{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
										 ]
									,
									transport 			: {
															read : {
																url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3010",
																dataType : "json",
																data:{
																	$select: "\"IdImagen\",\"NombreImagen\",\"Tipo\", \"Extension\",\"Version\", \"RutaPublica\""
																}
															}
														},
									schema 				: {
										model: {
											fields : {
												IdImagen : {type: "string"},
												NombreImagen : {type: "string"},
												Tipo : {type: "string"},
												Extension : {type: "string"},
												Version : {type: "string"},
												RutaPublica : {type: "string"},
											}
										}
									}
								}
							}),
		columns				: [
		       				   	{field: "IdImagen",title: "UI_ID",width: "150px", hidden:true},
								{field: "NombreImagen",title: "UI_NOMBRE",width: "150px", hidden:false},
								{field: "Extension",title: "UI_EXTENSION",width: "150px", hidden:false},
								{field: "Version",title: "UI_VERSION",width: "150px", hidden:false},
								{field: "RutaPublica",title: "UI_RUTA_PUBLICA",width: "250px", hidden:false},
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
		pageable			: false,
		filterable			: true,
		groupable           : true,
		autoBind			: true,
		height				: 300,
		selectable 			: "row"
	};
	
	$scope.search = function(){
		var filters = new Array();
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });
		
		if($scope.data.nombrArchivo && $scope.data.nombrArchivo != "")
			filters.push({ field: "toupper(NombreImagen)", operator: "contains", value: $scope.data.nombrArchivo });
		
		$scope.data.mainGrid.dataSource.filters(filters);
	};
		
	$scope.uploadImage = function(){
		var data = {operacion: "CREATE"};
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("OperarImagenModal", data, true, false);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			$scope.data.mainGrid.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.updateImage = function(){
		var data = $scope.seleccionImagen;
		data.operacion = "UPDATE";
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("OperarImagenModal", data, true, false);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			$scope.seleccionImagen = undefined;
			$scope.data.mainGrid.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.downloadImage= function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var config = {
			responseType: "arraybuffer",
			params:{
				"file": $scope.seleccionImagen.IdImagen	
			}
		}
		
		$http.get('/BSCL/Plataforma/GWJAVA/PLA/ImageManager', config)
		  .success(function (response) {
			   var file = new Blob([response], {type: $scope.seleccionImagen.Tipo});
			   //var fileURL = URL.createObjectURL(file);
			   saveAs(file, $scope.seleccionImagen.NombreImagen+"."+$scope.seleccionImagen.Extension);
			   $scope.seleccionImagen = undefined;
			   bcsComunes.ventanaOverlay(idVentana,false);
		}).error(function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.viewImage = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var config = {
			responseType: "arraybuffer",
			params:{
			"file": $scope.seleccionImagen.IdImagen	
			}
		}
				
		$http.get('/BSCL/Plataforma/GWJAVA/PLA/ImageManager', config).success(function (response) {
			var titulo = "UI_IMAGEN";
			var file = new Blob([response], {type: $scope.seleccionImagen.Tipo});
			var fileURL = URL.createObjectURL(file);
			const reader = new FileReader();
			reader.onloadend = function () {
				var id1 = bcsComunes.ventanaAbrir("ModalRecursosContratos",{title: titulo, file: fileURL,type: 'image/jpeg'}, false,false);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
				promesa.then(function(data){ 
					bcsComunes.ventanaOverlay(idVentana,false);
				});
			};
			reader.readAsDataURL(file);
		}).error(function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	
	
	
});
angular.module('ModuloPlataforma').controller('CargaEntidadVectorPrecioCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){
	
	var ventanaId = bcsComunes.ventanaWinId($scope);
	$scope.configData = {};
	$scope.recordData = {};
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);
		
	var dSResultados = function(){
		return new bcs.DataSource({
			type: "array",
			definition:{
					data:[],
					serverFiltering 	: false, // Filtrado de datos en el servidor.
					serverPaging 		: false, // Paginado de los datos en el servidor.
					serverSorting 		: false, // Ordenamiento de los datos en el servidor.
					sortable 			: true,
					pageSize 			: 50,
					schema:{
						model:{
							fields:{
								FECHA : {type: "string"},
								NEMOTECNICO : {type: "string"},
								ENTIDAD : {type: "string"},
								TIPOREGISTRO : {type: "string"},
								VALORREGISTRO : {type: "number"},
								RATING : {type: "string"},
								MENSAJEVALIDACION : {type: "string"}
							}
						}
					}
				}
		})
	};
	
	$scope.configResultados = {
		dataSource: dSResultados(),
		columns: [
			{field: 'FECHA', title:$translate.instant('UI_FECHA'), width: "100px" },
			{field: 'NEMOTECNICO', title:$translate.instant('UI_NEMO'), width: "100px" },
			{field: 'ENTIDAD', title:$translate.instant('UI_ENTIDAD'), width: "100px" },
			{field: 'TIPOREGISTRO', title:$translate.instant('UI_TIPO_REGISTRO'), width: "100px" },
			{field: 'VALORREGISTRO', title:$translate.instant('UI_VALOR_REGISTRO'), width: "150px" },
			{field: 'RATING', title:$translate.instant('UI_RATING'), width: "100px" },
			{field: 'MENSAJEVALIDACION', title:$translate.instant('UI_ESTADO'), width: "200px"}

		],
		gridCreated: function(grid) {
			$scope.mainGrid = grid;
		},
		sortable: true,
		pageable: true,
		filterable: true,
		height: 400,
		//autoBind : true
	};
	
	$scope.cargarArchivo = function(){
		bcsComunes.ventanaOverlay($scope.ventanaId, true);
		
		var datosArchivo = {codigoArchivo: $scope.recordData.archivoVector};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false);
		var promesa=bcsComunes.ventanaPromesa(id1);

		$scope.mainGrid.dataSource.data([]);		                          
		promesa.then(function(data){
			bcsComunes.ventanaOverlay($scope.ventanaId, false);
			if(!data || !data.fileData){
				return false;
			}else{
				bcsComunes.ventanaOverlay($scope.ventanaId,true);
				
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
				parametrosSP.p_IdModulo = 'PLA';
				parametrosSP.p_CodigoArchivo = $scope.recordData.archivoVector;
				parametrosSP.p_Archivo = JSON.stringify(data.fileData);

	    		var sp = bcsServicioPlataforma.Servicios().CargaEntidadVectorPrecio(parametrosSP);
	    		$scope.mainGrid.dataSource.data([]);
	    		sp.$promise.then(function(result) {
	    			var ventanaPromesa = bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_PROCESO_REALIZADO'),
						conflictos : util.parseServerResponse(result)}, $scope.ventanaId);
	    			bcsComunes.ventanaPromesa(ventanaPromesa).then(function(){
						bcsComunes.ventanaOverlay($scope.ventanaId,false);
					});
	    			$scope.mainGrid.dataSource.data(result.p_Resultado);	    			
					
				},function(result){
					console.log(JSON.stringify(result));
					var p=bcsComunes.ventanaMsgErr($translate.instant('UI_OPERACION_TIENE_ERRORES'),$scope.ventanaId);
					bcsComunes.ventanaPromesa(p).then(function(){
						bcsComunes.ventanaOverlay($scope.ventanaId,false);
					});
				});
			}
		}); 
	};
	
	
		
});

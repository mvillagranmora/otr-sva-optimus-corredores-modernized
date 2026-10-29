angular.module('ModuloContabilidad').controller('BlotterMantenedorMetadataCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource,bcsMantenedorEventosGrid,bcsServicioContabilidad) {
	$scope.data = {};
	$scope.formInfo = {};
	var idVentana=bcsComunes.ventanaWinId($scope);
	var columnas = function() {
		return  {
			CodigoMetadata : 	{ field: "CodigoMetadata"				,title: 'UI_CODIGO_METADATA'		, width: "117px"},
			DescripcionMetadata : 	{ field: "DescripcionMetadata"			,title: 'UI_DESCRIPCION_METADATA'	, width: "117px"}    
		};
	};
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!columnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, columnas()[campo], extras);
	};
	
	$scope.defaultOptions = {
		userSettings:true,
		sortable: true,
  		reorderable: true,
  		filterable: true,
		groupable: true,
		pageable: true,
		selectable: "multiple, row",
		reorderable: true,
		allowCopy: true,
		resizable: true,
  		autoBind: true,
  		scrollable: true,
  		height:450,

		dataSource: bcsMantenedorEventosGrid.getDataSourceConfiguracionMetadata(),
	
		columns: [
		           getConfiguracionColumna("CodigoMetadata")
		          ,getConfiguracionColumna("DescripcionMetadata")
		]
	};
	
	$scope.agregarMetadata = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var id = bcsComunes.ventanaAbrir("AgregarMetadata",{},false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
        	bcsComunes.ventanaOverlay(idVentana,false);
        	$scope.defaultOptions.dataSource.read();
        	$scope.data.kNgModel = undefined;
        });
	}
	
	$scope.editarMetadataContabilidad = function(metadatas){
		if(!metadatas || metadatas.length === 0) return
		var id = bcsComunes.ventanaAbrir("EditarMetadata",{Metadata:metadatas[0]},false);
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
        	bcsComunes.ventanaOverlay(idVentana,false);
        	$scope.defaultOptions.dataSource.read();
        	$scope.data.kNgModel = undefined;
        });
	}
		
	$scope.eliminarMetadataContabilidad = function(metadatas){
		if(!metadatas || metadatas.length === 0) return
		bcsComunes.ventanaOverlay(idVentana,true);
		var mt = metadatas.map(function(item){
			return item.IdMetadataCuentaContable
		})
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'CON',
				p_IdMetadataCuentaContable: mt
		}
		bcsServicioContabilidad.EliminarMetadata(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result ){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
					}
					$scope.defaultOptions.dataSource.read();
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
				});
	}
	
	
})
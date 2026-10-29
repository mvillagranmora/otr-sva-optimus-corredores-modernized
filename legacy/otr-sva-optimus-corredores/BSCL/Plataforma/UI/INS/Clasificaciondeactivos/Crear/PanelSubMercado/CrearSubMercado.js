angular.module('ModuloPlataforma').controller('CrearSubMercadoCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){
	
	$scope.filtros = {};
	$scope.data = {};
	$scope.formData = {};
	
	
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-mercados-todoscreados",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
$scope.crearSubMercado = function(){
	console.log($scope.formData.NombreMercado);
	
	var urlCrearMercado = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601173";
	var r = $resource("", {}, {'CrearMercado': { method: "POST", url: urlCrearMercado}});
	
	var datosCrearMercado = {
			
		p_Descripcion : $scope.formData.NombreSubMercado, 	  //Nombre del sub mercado
		p_Padre :$scope.filtros.clasificacionTipoInstrumento  // Id del mercado
	};
	//console.log(JSON.stringify(datosCrearCliente));
	var post = r.CrearMercado(datosCrearMercado);
	post.$promise.then(function(result){
		if(result.P_OUT_MENSAJES.length > 0)
		{
			//var titulo = (result.P_OUT_ERRORESCONTADOR>0) ? "UI_DETALLE_TITULO" : "UI_DETALLE_TITULO";
			var titulo ="UI_DETALLE_TITULO";
			bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
				conflictos:util.parseServerResponse(result)},$scope.ventanaId);
		}
		
		bcsComunes.setConflictos(util.parseServerResponse(result));
	},function(result){
		bcsComunes.ventanaOverlay($scope.ventanaId,false);
		$scope.etapa='';
		bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
			conflictos:util.parseServerResponse(result)},$scope.ventanaId);
		bcsComunes.setConflictos(util.parseServerResponse(result));
		//console.log(JSON.stringify(result));
	});
};
	
	
	
	
	
	
});

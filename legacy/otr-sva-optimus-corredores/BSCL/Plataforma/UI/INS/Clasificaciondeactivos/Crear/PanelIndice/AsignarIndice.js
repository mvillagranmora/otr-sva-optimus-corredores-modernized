angular.module('ModuloPlataforma').controller('AsignarIndiceCtrl', 
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
	$scope.data.oConfiguracionClasificacionSubMercado={
			tipo : "combobox-submercados-creados",			
			configuracion : {},
			filtros : {
				IdArbolClasificacionActivo 	: { "field" : "Padre", "operator": "eq", "value": -1}			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.oConfiguracionClasificacionIndice={
			tipo : "combobox-indices",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
$scope.AsignarIndice = function(){
	
	console.log($scope.formData.NombreMercado);
	
	var urlAsignarIndice = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700001";
	var r = $resource("", {}, {'AsignarIndice': { method: "POST", url: urlAsignarIndice}});
	
	let padre=""
	if($scope.filtros.clasificacionSubMercado==false)
	  {
		 padre = $scope.filtros.clasificacionTipoInstrumento;
	  }
	else
	{
			padre = $scope.filtros.clasificacionSubMercado;
	}
	
	var datosAsignacion = {
			
		Padre       : padre,								
		Indice      : $scope.filtros.clasificacionIndice
	};
	
	
	var post = r.AsignarIndice(datosAsignacion);
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

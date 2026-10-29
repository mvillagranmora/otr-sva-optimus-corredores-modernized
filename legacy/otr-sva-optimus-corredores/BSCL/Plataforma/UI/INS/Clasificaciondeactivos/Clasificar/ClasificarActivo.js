angular.module('ModuloPlataforma').controller('ClasificarActivoCtrl', 
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
	
	$scope.data.oConfiguracionInstrumentosAsignados={
			tipo :"combobox-instrumentos-clasificados",			
			configuracion : {},
			filtros : {
				IdArbolClasificacionActivo 	: { "field" : "IdArbolClasificacionActivo", "operator": "eq", "value": -1},			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.oTipoClasificacionInstrumentoAsignar={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.oClasificacionInstrumentoAsignar={
			tipo : "combobox-tipo-instrumento",			
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value": undefined}			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	
	$scope.data.oInstrumentoAsignar={
			tipo : "combobox-instrumento",			
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value": undefined},
				IdTipoInstrumento 	: { "field" : "IdTipoInstrumento", "operator": "eq", "value": undefined}
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.oConfiguracionClasificacionInstrumento={
			tipo : "combobox-clasificacion-instrumento",			
			configuracion : {},
			filtros : {
				IdArbolClasificacionActivo 	: { "field" : "Padre", "operator": "eq", "value": -1}			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.AsignarInstrumento = function(){

		var urlClasificar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601179";
		var r = $resource("", {}, {'Clasificar': { method: "POST", url: urlClasificar}});
		
		var arbol ;
		
		if($scope.filtros.clasificacionTipoInstrumento!="" && $scope.filtros.clasificacionSubMercado==false && $scope.filtros.clasificacionInstrumento==false )
		{
			arbol=$scope.filtros.clasificacionTipoInstrumento;
		}
		else if ($scope.filtros.clasificacionTipoInstrumento!="" && $scope.filtros.clasificacionSubMercado!="" && $scope.filtros.clasificacionInstrumento==false)
		{
			arbol=$scope.filtros.clasificacionSubMercado;
		}
		else if($scope.filtros.clasificacionTipoInstrumento!="" && $scope.filtros.clasificacionSubMercado!="" && $scope.filtros.clasificacionInstrumento!="")
		{
			arbol=$scope.filtros.clasificacionInstrumento;
		}
		
			var urlClasificar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601179";
			var r = $resource("", {}, {'Clasificar': { method: "POST", url: urlClasificar}});
			
			var datosClasificar = {
					
					IdRelacionClasificacion        	: 0,
					IdArbolClasificacionActivo		: arbol,
					
					IdClasificacionTipoInstrumento	:$scope.filtros.clasificacionTipoInstrumentoAsignar,
					IdTipoInstrumento              	:$scope.filtros.clasificacionInstrumentoAsignar,
					IdInstrumento 				   	:$scope.filtros.InstrumentoAsignar
				};
				var post = r.Clasificar(datosClasificar);
				post.$promise.then(function(result){

					if(result.P_OUT_MENSAJES[0].tipo=='OK')
					{
						var titulo ="UI_DETALLE_TITULO";
						bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
							conflictos:util.parseServerResponse(result)},$scope.ventanaId);
					}
					else if(result.P_OUT_MENSAJES[0].tipo =="ERROR")
					{
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
		
	//CLASIFICACION EXISTENTE 
};
	
});

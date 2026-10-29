angular.module('ModuloPlataforma').controller('EliminarMercadoCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){
	$scope.filtros = {};
	$scope.data = {};
	$scope.formData = {};
	
	
	$scope.data.oConfiguracionMercadosEliminar={
			tipo : "combobox-mercados-creados",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	
	$scope.EliminarMercado = function(){
		
		var urlCrearMercado = "/BSCL/Plataforma/GWJAVA/PLA/call?t=54";
		var r = $resource("", {}, {'CrearMercado': { method: "POST", url: urlCrearMercado}});
		
		var datosCrearMercado = {
				
			p_NombreSubMercado : $scope.formData.NombreSubMercado,
			p_NombreMercado    : $scope.filtros.clasificacionTipoInstrumentoObj.Padre
		};
		//console.log(JSON.stringify(datosCrearCliente));
		var post = r.CrearMercado(datosCrearMercado);
		post.$promise.then(function(result){
			if(result.P_OUT_ERRORESCONTADOR==0&&result.P_OUT_REFIDCLIENTE&&result.P_OUT_REFIDCLIENTE>0)
			{
			}
			else
			{
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_CLIENTE'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
			}
			bcsComunes.setConflictos(util.parseServerResponse(result));
		},function(result){
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			$scope.etapa='';
			bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_CLIENTE'),
				conflictos:util.parseServerResponse(result)},$scope.ventanaId);
			bcsComunes.setConflictos(util.parseServerResponse(result));
			//console.log(JSON.stringify(result));
		});
	};

});

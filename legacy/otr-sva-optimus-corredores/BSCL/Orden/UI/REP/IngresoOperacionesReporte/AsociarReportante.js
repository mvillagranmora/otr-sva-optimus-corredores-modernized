angular.module('ModuloOrdenes').controller('AsociarReportantesCtrl',function($scope,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,$timeout){
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.datos = $scope.parametros.reportante;
	console.log("Datos:"+JSON.stringify($scope.datos));
	$scope.datosReportado = $scope.parametros.reportado;
	console.log("DatosReportado:"+JSON.stringify($scope.datosReportado));
	if($scope.datos.MontoDisponibleAsociar>=$scope.datosReportado.MontoDisponibleAsociar)
		$scope.formData.MontoAsociar =$scope.datosReportado.MontoDisponibleAsociar;  //$scope.datos.MontoDisponibleAsociar;
	else
		$scope.formData.MontoAsociar=$scope.datos.MontoDisponibleAsociar;
	
	$scope.formData.asociarOrdenReportante = function(){
		
		if($scope.formData.MontoAsociar<=$scope.datosReportado.MontoDisponibleAsociar && $scope.datosReportado.PrecioOrden<=$scope.formData.MontoAsociar)
		{
			bcsComunes.ventanaOverlay($scope.winID,true);
			var datos = {
					p_IdEmpresa   : $scope.SesionIdEmpresa,
					p_IdUsuario   : $scope.SesionIdUsuario,
					p_IdModulo 	  : 'ORD',
					p_simularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Asociaciones : [{
						IdInterno : 0,
						IdOrdenN : $scope.datosReportado.IdOrden,
						IdOrdenM : $scope.datos.IdOrden,
						Precio	 : $scope.datosReportado.PrecioOrden,
						ModalidadCalculo : $scope.datosReportado.IdTipoInstrumento === 'RRF'?'R':'M',
						Monto : $scope.formData.MontoAsociar,
						Cantidad: 0,
						Tasa :  $scope.datos.Tasa,
			}]
		}

		bcsServicioOrden.CrearAsociaciones(datos).$promise.then(function(respuesta){
			if(respuesta.P_OUT_ERRORESCONTADOR == 0){
				var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(id);                               
			     $.when(promesa).then(function(data){
			    	 $scope.datosReportado.MontoAsociado = $scope.datosReportado.MontoAsociado + $scope.formData.MontoAsociar;
			    	 $scope.datosReportado.MontoDisponibleAsociar = $scope.datosReportado.MontoDisponibleAsociar - $scope.formData.MontoAsociar;
			    	 $timeout(function() {
			    		 bcsComunes.ventanaCerrar($scope.winID);
			    	 },20);
			     });
			}else{
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
	     	$scope.formData.reportantesSeleccionadas = undefined;
		},function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			$scope.formData.reportantesSeleccionadas = undefined;
		});
		}
	
		else 
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"),conflictos:''},$scope.winID);
		
	}
		
});
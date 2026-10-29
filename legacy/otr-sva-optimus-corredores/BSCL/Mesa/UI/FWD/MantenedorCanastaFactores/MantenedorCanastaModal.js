(function() {
	angular.module('ModuloMesa').controller('MantenedorCanastaModal',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.oDesplegar = {};
		var data = { Resultado : ''};
	
		//Moneda Principal
		$scope.data.ConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado"         ,"operator": "eq", "value" : "ACT" }
			}
		};
		
		//Canasta
		$scope.data.ConfiguracionCanasta={
			filtros : {
				IdEmpresa 	: { "field"   : "IdEmpresa","operator": "eq", "value" : $scope.SesionIdEmpresa }
			}
		};
		
		var error = function(respuesta){
			data = { Resultado : 'ERROR'};
			bcsComunes.msg("error","error",$translate.instant('UI_CANASTA_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		var successIngreso = function(respuesta){
			
			//si vuelve ref id, se creo la asignacion
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{	
				data = { Resultado : 'OK'};
				bcsComunes.ventanaData(idVentana,data);
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				//$scope.buscarMovimientos();
			}
			else
			{			
				data = { Resultado : 'ERROR'};
				bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		$scope.ValidoDatos = function(){
			if($scope.data.InstrumentoSeleccionado === false)
				return false
			
			if($scope.data.MonedaPrincipalSeleccionado === false)
				return false
				
			if($scope.data.CanastaSeleccionado === false)
				return false
			
			return true
			
		};
		
		//Ingresar Instrumento Canasta
		$scope.IngresarCanasta = function(){
			$scope.ValidoDatos();
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo : 'MFW',
					p_UsaReglasNegocio : 'N',
					p_tablaCanastaMoneda	: [{
							 IdCanasta		: $scope.data.CanastaSeleccionado.IdCanasta
							,IdMoneda		: $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal
												    }]
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.CrearCanasta(datos).$promise.then(successIngreso).catch(error);
		};
		
		
	});
}());


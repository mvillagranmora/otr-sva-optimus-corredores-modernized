(function() {
	angular.module('ModuloMesa').controller('MantenedorFactoresModal',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.oDesplegar = {};
		var data = { Resultado : ''};
		
		$scope.data.kngHasta = 0;	
		$scope.data.kngFactor = 0;
			
		//Canasta
		$scope.data.ConfiguracionCanasta={
			filtros : {
				IdEmpresa 	: { "field"   : "IdEmpresa","operator": "eq", "value" : "1" }
			}
		};
		
		var error = function(respuesta){
			data = { Resultado : 'ERROR'};
			bcsComunes.msg("error","error",$translate.instant('UI_FACTOR_ERROR'));
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
				bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.ValidoDatos = function(){
			if($scope.data.CanastaSeleccionado === false)
				return false
			
			if($scope.data.kngHasta <= 0 || $scope.data.kngHasta === undefined)
				return false
				
			if($scope.data.kngFactor <= 0 || $scope.data.kngFactor === undefined)
				return false

			return true
		};
		
		//Ingresar Instrumento Canasta
		$scope.IngresarFactorCanasta = function(){
			$scope.ValidoDatos();
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo : 'MFW',
					p_UsaReglasNegocio : 'N',
					p_tablaFactorCanasta	: [{
							 IdCanasta	: $scope.data.CanastaSeleccionado.IdCanasta
							,Hasta		: $scope.data.kngHasta
							,Factor		: $scope.data.kngFactor
												    }]
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.CrearFactorCanasta(datos).$promise.then(successIngreso).catch(error);
		};
		
	});
}());


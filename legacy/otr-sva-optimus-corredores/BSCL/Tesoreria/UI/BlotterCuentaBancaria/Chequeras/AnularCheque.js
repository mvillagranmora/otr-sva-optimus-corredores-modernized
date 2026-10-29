angular.module('ModuloTesoreria').controller('AnularChequeCtrl',
		function($scope
				,$state
				,$timeout
				,$modal
				,$translate
				,bcsComunes 
				,bcsAlerta 
				,bcsServiciosTesoreria
				) {


	$scope.data = {};
	$scope.grid = {};
	$scope.filtros = {};
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	$scope.Parametros = bcsComunes.ventanaParams($scope.IdVentana);

	$scope.NroChequeActual = $scope.Parametros.IdSerieActual;
	
	// PROCESOS COMUNES 
	var success = function(respuesta)
	{
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
		{		
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));

			$scope.Data = {};
			$scope.Data.Accion = 'ANULAR';
			$scope.Data.NuevaSerie = respuesta.P_OUT_SERIE[0].SerieActual;
			$scope.Data.NuevoDisponible = respuesta.P_OUT_SERIE[0].Disponibles;
		
			// Cierra ventana
			bcsComunes.ventanaData($scope.IdVentana, $scope.Data);
			bcsComunes.ventanaCerrar($scope.IdVentana);  
		}
		else
		{			
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		}
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	var error = function(respuesta)
	{		
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	

	//Cierra la ventana dado el ID
	$scope.btnCancelar = function()
	{
		bcsComunes.ventanaData($scope.IdVentana);
		bcsComunes.ventanaCerrar($scope.IdVentana);  
	};
	
	
	//Cierra la ventana dado el ID
	$scope.btnGrabar = function()
	{
		
		if ($scope.Comentario === "" || $scope.Comentario === undefined ) {
			bcsComunes.msg("warning","info",$translate.instant('UI_INGRESE_MOTIVO'));		
		} else {

			// Parametros del servicio
			var Param = {};

			// Obligatorios
			Param.p_IdEmpresa	= $scope.SesionIdEmpresa;
			Param.p_IdUsuario   = $scope.SesionIdUsuario;
			Param.p_IdModulo    = 'TES';
			Param.p_IdPais		= $scope.SesionIdPaisSistema;
			Param.p_UsaReglasNegocio = 'S';

			// Claves
			Param.p_NroSerieAnular = $scope.Parametros.IdSerieActual;
			Param.p_IdChequera	   = $scope.Parametros.IdChequera;
			Param.p_Motivo         = $scope.Comentario;

			bcsServiciosTesoreria.AnularCheque(Param,success,error);
			console.log(JSON.stringify(Param));
						
		};
	};

});



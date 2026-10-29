angular.module('ModuloMesa').controller('IngresoClaveCtrl',
		function($scope
				,$state
				,$timeout
				,$modal
				,$translate
				,bcsComunes 
				,bcsServiciosCustodia
				,bcsAlerta 
				) {


	$scope.data = {};
	$scope.grid = {};
	$scope.filtros = {};
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	$scope.objMovimiento = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.Registro = $scope.objMovimiento.Registro;

	$scope.data.frmRut = $scope.Registro[0].DocumentoIdentidad;
	$scope.data.Clave = $scope.Registro[0].Clave;

	// $scope.data.frmCorredor = 'FALTA SQL';	
	// Variables para la ventana
	if ($scope.Registro.length > 1)
		{
		$scope.data.frmFolio = '+ DE UN REGISTRO';
		$scope.data.frmInstrumento = '+ DE UN REGISTRO';
		$scope.data.frmCantidad = '+ DE UN REGISTRO';
		}
	else
		{
		$scope.data.frmFolio = $scope.Registro[0].FolioOperacion;
		$scope.data.frmInstrumento = $scope.Registro[0].Nemo;
		$scope.data.frmCantidad = $scope.Registro[0].Cantidad;
		};

	
	// PROCESOS COMUNES 
	var success = function(respuesta)
	{
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
		{		
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));

			// Cierra ventana
			bcsComunes.ventanaData($scope.IdVentana);
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
	$scope.btnIngresar = function()
	{
		
        // Parametros del servicio
		var param = {};
		var Operaciones = [];		
		var v_FechaSistema = util.dateInicioDia($scope.SesionFechaSistema);

		for (var i in $scope.Registro)
		{
		 
			var CampoOpe = {};
				CampoOpe.IdRegistro = i;
				CampoOpe.IdOperacionPendiente = $scope.Registro[i].IdOperacionPendiente;
				CampoOpe.IdTipoOperacion = $scope.Registro[i].IdTipoOperacion;
				CampoOpe.FolioOperacion = $scope.Registro[i].FolioOperacion;
				//CampoOpe.IdCliente = $scope.Registro[i].IdCliente;
				//CampoOpe.IdCorredor = $scope.Registro[i].Corredor;				
				//CampoOpe.Nemo = $scope.Registro[i].Nemo;
				CampoOpe.Clave = $scope.data.Clave;

				Operaciones.push(CampoOpe);
		};		
		
		
		// Obligatorios
		param.p_IdEmpresa	= $scope.SesionIdEmpresa;
		param.p_IdUsuario   = $scope.SesionIdUsuario;
		param.p_IdModulo    = 'CUS';
		param.p_IdPais		= $scope.SesionIdPaisSistema;

		// Claves
		param.p_TblOperaciones	= Operaciones;
										
       	bcsServiciosCustodia.ClaveOperacionesPendientes(param,success,error);
       	console.log(JSON.stringify(param));
	};

});



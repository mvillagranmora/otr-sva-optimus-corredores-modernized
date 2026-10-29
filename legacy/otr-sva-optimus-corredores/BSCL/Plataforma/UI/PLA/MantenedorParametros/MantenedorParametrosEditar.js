angular.module('ModuloPlataforma').controller('MantenedorParametrosEditarCtrl',
	function($scope,bcsComunes,$translate,bcsServicioPlataforma){


	
	$scope.nuevo = true; //false->EditarRegistro / true->NuevoRegistro
	$scope.datos = {};
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaTamanoAutomatico($scope.IdVentana);
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	console.log($scope.parametros);
	
	$scope.datos.codigoDisable=true;
	$scope.datos.codigoKng=$scope.parametros.oParametro.CodigoParametro;
	$scope.datos.valorDisable=false;
	$scope.datos.valorKng=$scope.parametros.oParametro.Valor;
	
	$scope.LimpiarRegistro = function(){
		$scope.datos.codigoKng 	= $scope.parametros.oParametro.CodigoParametro;
		$scope.datos.alorKng	= $scope.parametros.oParametro.Valor;
	}
	
	$scope.salir = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	};
	
		
	$scope.ActualizaParametros = function(){
		//Bloquea ventana en caso de servicio lento o 2 clics seguidos
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		//GENERACION DE DATOS PARA ENVIO AL SERVICIO
		var datosServicio = {};
		datosServicio.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		datosServicio.p_IdUsuario 		= $scope.SesionIdUsuario;
		datosServicio.p_IdModulo 		= 'PLA';
		datosServicio.p_SimularOperacion = 'N';
		datosServicio.p_Modulo			= $scope.parametros.oParametro.Modulo;	
		datosServicio.p_Codigo			= $scope.datos.codigoKng;	
		datosServicio.p_Valor			= $scope.datos.valorKng;		
		//TODO: agregar otros valores de texto para que puedan ser editables.
		if(datosServicio.p_Valor == null) datosServicio.p_Valor= '';
		console.log(datosServicio);
		

		//LLAMADA AL SERVICIO 
		var post = bcsServicioPlataforma.Servicios().MantenedorParametrosEditar(datosServicio);
		post.$promise.then(function(result){

			var idModal = bcsComunes.ventanaMsg({
				mensaje: (result.p_Respuesta[0])?$translate.instant(result.p_Respuesta[0].P_SALIDA) : $translate.instant("UI_ERROR"),
				conflictos:util.parseServerResponse(result)
			},$scope.IdVentana);

			bcsComunes.ventanaPromesa(idModal).then(function(){
				bcsComunes.ventanaOverlay($scope.IdVentana,false);	
			});
		}).catch(function(err){
			console.log(err);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
		});
		/*
		bcsServicioPlataforma.MantenedorParametrosEditar(datosServicio).then(function(result)
		{
			console.log(JSON.stringify(result));
	        var p = bcsComunes.ventanaMsg({
				mensaje: $translate.instant(result.SALIDA),
				conflictos:util.parseServerResponse(result)
			},$scope.IdVentana);
			bcsComunes.ventanaPromesa(p).then(function(){
        		if (result.P_OUT_ERRORESCONTADOR == "0")
        			bcsComunes.ventanaCerrar($scope.IdVentana);
	        	}
	        );
        },
		function(result){
        	datosDiccionario.message = 'UI_MODIFICAR_INCORRECTO';
		});
		*/
	};
	

});

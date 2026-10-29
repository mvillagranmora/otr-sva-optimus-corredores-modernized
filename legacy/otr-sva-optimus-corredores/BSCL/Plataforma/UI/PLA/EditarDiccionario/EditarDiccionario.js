angular.module('ModuloPlataforma').controller('EditarDiccionarioCtrl',
	function($scope,bcsComunes,$translate,bcsServicioPlataforma){

	console.log("EditarDiccionarioCtrl");
	
	$scope.nuevo = true; //false->EditarRegistro / true->NuevoRegistro
	$scope.datos = {};
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	
	var cargarDatosParaEdicion = function(oDiccionario)
	{	
		$scope.datos.kngCodDiccionario		= oDiccionario.CodDiccionario;
		$scope.datos.kngCodLenguaje			= oDiccionario.CodLenguaje;
		$scope.datos.kngDescripcion			= oDiccionario.Descripcion;
		$scope.datos.kngAbreviado 			= oDiccionario.Abreviado;
	};
	
	if ($scope.parametros.oDiccionario){
		$scope.nuevo = false;
		//SE BLOQUEA EDICION DEL CODIGO DICCIONARIO Y LENGUAJE
		$scope.datos.disableCodDiccionario = true;
		$scope.datos.disableCodLenguaje = true;
		
		$scope.objDiccionario = $.extend({},$scope.parametros.oDiccionario);
		cargarDatosParaEdicion($scope.objDiccionario);
		console.log($scope.objDiccionario);
	}
	else {
		$scope.datos.kngCodLenguaje = $scope.SesionLocale
		//SE BLOQUEA EDICION DEL CODIGO LENGUAJE
		$scope.datos.disableCodLenguaje = true;
	}
	
	$scope.ActualizaDiccionario = function(){
		//Bloquea ventana en caso de servicio lento o 2 clics seguidos
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		//GENERACION DE DATOS PARA ENVIO AL SERVICIO
		var datosDiccionario = {};
		datosDiccionario.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		datosDiccionario.p_IdUsuario 		= $scope.SesionIdUsuario;
		datosDiccionario.p_IdModulo 		= 'PLA';
		datosDiccionario.p_SimularOperacion = 'N';
		
		//NO PUEDEN IR CAMPOS NULOS
		if($scope.datos.kngCodDiccionario 	== null) $scope.datos.kngCodDiccionario = '';
		if($scope.datos.kngCodLenguaje 		== null) $scope.datos.kngCodLenguaje 	= '';
		if($scope.datos.kngDescripcion 		== null) $scope.datos.kngDescripcion 	= '';
		if($scope.datos.kngAbreviado 		== null) $scope.datos.kngAbreviado 		= '';
		
		var dataRows = [];
		dataRows.push({
			CodDiccionario	: $scope.datos.kngCodDiccionario, 
			CodLenguaje		: $scope.datos.kngCodLenguaje, 
			Descripcion		: $scope.datos.kngDescripcion,
			Abreviado		: $scope.datos.kngAbreviado
		});		
		
		datosDiccionario.p_Codigos = dataRows;
		
		if ($scope.nuevo){ //NUEVO INGRESO
			console.log("Ingreso Nuevo Registro");
			console.log(JSON.stringify(datosDiccionario));
			//LLAMADA AL SERVICIO AGREGAR DICCIONARIO
			bcsServicioPlataforma.AgregarDiccionario(datosDiccionario).then(function(result)
			{
				console.log(JSON.stringify(result));
				if (result.P_OUT_ERRORESCONTADOR == "0")
		        {
					console.log(result.p_Respuesta[0].CodDiccionario + ": " + result.p_Respuesta[0].Mensaje);
		        }
				//VENTANA AVISO
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
	        	datosDiccionario.message = 'UI_INGRESO_INCORRECTO_DICCIONARIO';
			});
		}
		else { //EDITA EXISTENTE
			console.log("Edita Registro Existente");
			console.log(JSON.stringify(datosDiccionario));
			//LLAMADA AL SERVICIO MODIFICAR DICCIONARIO
			bcsServicioPlataforma.ModificarDiccionario(datosDiccionario).then(function(result)
			{
				console.log(JSON.stringify(result));
				if (result.P_OUT_ERRORESCONTADOR == "0")
		        {
					console.log(result.p_Respuesta[0].CodDiccionario + ": " + result.p_Respuesta[0].Mensaje);
		        }
				//VENTANA AVISO
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
	        	datosDiccionario.message = 'UI_MODIFICAR_INCORRECTO_DICCIONARIO';
			});
		}
	};
	
	$scope.LimpiarRegistro = function(){
		if ($scope.nuevo){
			$scope.datos.kngCodDiccionario 	= '';
			$scope.datos.kngCodLenguaje 	= $scope.SesionLocale;
			$scope.datos.kngDescripcion 	= '';
			$scope.datos.kngAbreviado 		= '';
		}
		else {
			$scope.datos.kngCodDiccionario 	= $scope.objDiccionario.CodDiccionario;
			$scope.datos.kngCodLenguaje 	= $scope.objDiccionario.CodLenguaje;
			$scope.datos.kngDescripcion 	= $scope.objDiccionario.Descripcion;
			$scope.datos.kngAbreviado 		= $scope.objDiccionario.Abreviado;
		}
	}
	
	$scope.salir = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	};
});

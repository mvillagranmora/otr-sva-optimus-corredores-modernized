angular.module('ModuloPlataforma').controller('AgregarAtributoCtrl',
		function (bcsComunes, $scope, $translate, $resource){
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.atributosMantenedor = parametros.dato1;
	$scope.Mantenedor = parametros.Mantenedor;
	$scope.agregar = [];
	bcsComunes.ventanaTamanoAutomatico(idVentana,20);

	//Actualiza registro de mantenedor
	var urlAgregarAtributo  = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1789";
	var resourceAccionRegistro = $resource("", {}, { 'agregar': { method: "POST", url: urlAgregarAtributo} });
	
	$scope.agregarMantenedor = function()
	{
		var numFila = 1;  // FALTA  revisar
		var p_Dato = [];
		var parametrosSP = {};
		
		
		for (var key in $scope.atributosMantenedor)
		{
			console.log($scope.agregar[$scope.atributosMantenedor[key].NombreCampo] + ' - ' + $scope.atributosMantenedor[key].TipoDatoCampo  + ' - ' +	$scope.agregar[$scope.atributosMantenedor[key].NombreCampo]);
			
			p_Dato.push({ NumFila: numFila, Campo: $scope.atributosMantenedor[key].NombreCampo, Valor: $scope.agregar[$scope.atributosMantenedor[key].NombreCampo] === false || $scope.agregar[$scope.atributosMantenedor[key].NombreCampo] === undefined ? ($scope.atributosMantenedor[key].TipoDatoCampo === 'NVARCHAR' ? '':0) : $scope.agregar[$scope.atributosMantenedor[key].NombreCampo] });
		};
		
	  	parametrosSP.p_IdEmpresa 		= $scope.SesionIdEmpresa;
	  	parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario;
	  	parametrosSP.p_IdModulo 		= 'PLA';
	  	parametrosSP.p_UsaReglasNegocio = 'S';
		parametrosSP.p_Mantenedor 		= $scope.Mantenedor;
		parametrosSP.p_Dato 			= p_Dato;
		
		
		resourceAccionRegistro.agregar(parametrosSP).$promise.then(
			function(result)
			{
				if(result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"), conflictos:util.parseServerResponse(result)}, idVentana);
					bcsComunes.ventanaOverlay(idVentana, false);
					return;
				}				
				bcsComunes.msg("info","info",$translate.instant("DATOS_INGRESO_EXITO"));
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaData(idVentana,"OK");
				bcsComunes.ventanaCerrar(idVentana);
			},
			function(result)
			{	
				///console.log(result);				
				bcsComunes.msg("error","error",$translate.instant("UI_ERROR_INGRESO"));
			}
		);		
	};

	$scope.cerrarMantenedor = function (){
		bcsComunes.ventanaCerrar(idVentana);
	};
	
});


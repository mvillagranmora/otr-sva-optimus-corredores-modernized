angular.module('ModuloPlataforma').controller('ModificarAtributoCtrl',
		function (bcsComunes, $scope, $translate, $resource, bcsAlerta, OPCIONES_BCSALERTA){
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.atributosMantenedor = parametros.dato1;
	$scope.modificar = $.extend({},parametros.dato2);
	$scope.modificarAntes = $.extend({},parametros.dato2);
	$scope.TituloMantenedor = parametros.TituloMantenedor;
	$scope.LlavePrimaria = parametros.LlavePrimaria;
	$scope.CampoPrimario = parametros.CampoPrimario;
	$scope.CampoPK = parametros.CampoPK;   //  [  { nombre: pk1 , valor: valor1 },{ nombre: pk2 , valor: valor2 } ]
	$scope.Mantenedor = parametros.Mantenedor;
		
	//Si campo es BIGINT se cambia a numérico
	$.each($scope.atributosMantenedor,function(key,value){
		if ($scope.atributosMantenedor[key].TipoDatoCampo === 'BIGINT')
		{	
			var nombreCampo = $scope.atributosMantenedor[key].NombreCampo;
			$scope.modificarAntes[nombreCampo] = Number($scope.modificarAntes[nombreCampo]);
			$scope.modificar[nombreCampo] = Number($scope.modificar[nombreCampo]);
		}
		bcsComunes.ventanaTamanoAutomatico(idVentana,20);
	});
	
	//Actualiza registro de mantenedor
	var urlEnvio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=37";
	var resourceAccionRegistro = $resource("", {}, { 'modificar': { method: "POST", url: urlEnvio} });
	
	$scope.modificarMantenedor = function(p_OUT_ID){

		var pregunta = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_MENSAJE_MODIFICAR_PARAMETRO'), idVentana);
		pregunta.then(function(result){
			if(result === OPCIONES_BCSALERTA.ACEPTAR){
				var datos = {};
				datos.p_Mantenedor = "";
				datos.p_Dato = [];
				
				var parametrosSP = {};
				var numFila = 1;
				var p_Dato = [];
				
				/**** Recupera Id desde seleccion de grilla ***/
				p_Dato.push({NumFila: numFila, Campo: $scope.CampoPrimario, Valor: $scope.LlavePrimaria});
				/**** End Recupera Id desde seleccion de grilla ****/
				for (var key in $scope.modificar){			
					p_Dato.push({ NumFila: numFila, Campo: key, Valor: $scope.modificar[key] === false ? 0 : $scope.modificar[key] });
				};
				parametrosSP.p_IdEmpresa 		= $scope.SesionIdEmpresa;
				  parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario;
				  parametrosSP.p_IdModulo 		= 'PLA';
				  parametrosSP.p_UsaReglasNegocio = 'S';
				parametrosSP.p_Mantenedor = $scope.Mantenedor;
				parametrosSP.p_Dato = p_Dato;
				bcsComunes.ventanaOverlay(idVentana, true);
				resourceAccionRegistro.modificar(parametrosSP).$promise.then(function(result){
					var datos = {};
					if(result && result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"), conflictos:util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaOverlay(idVentana, false);
						return;
					}
					bcsComunes.msg("info","Mantenedor General","Registro modificado correctamente");
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaData(idVentana,"OK");
					bcsComunes.ventanaCerrar(idVentana);
				},function(result){
					bcsComunes.msg("error","Mantenedor General","Ocurrio un error al realizar la actualizacion");
					bcsComunes.ventanaOverlay(idVentana, false);
				});
			}
		});

	};
	
	//End Actualiza registro de mantenedor
	
	$scope.cerrarMantenedor = function (){		
		bcsComunes.ventanaCerrar(idVentana);
	};
});

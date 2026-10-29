/*************************************************************
 * Purpose	: Encuesta del Cliente
 * Author	: BCS
 * Date		: 21/12/2023
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteEncuestaCtrl',
	function($scope,$translate,$resource,$stateParams,bcsComunes,$uibModal,bcsServicioPlataforma){
		
	$scope.Respuestas = {};		
	var urlPreguntasEncuesta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100050";
	var r = $resource("", {}, {'preguntasEncuesta': { method: "POST", url: urlPreguntasEncuesta}});
		$scope.test = {};
		
	var paramPreguntasEncuesta = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdModulo : "PLA",
			p_IdUsuario : $scope.SesionIdUsuario					
		};
			
	var post = r.preguntasEncuesta(paramPreguntasEncuesta);
		post.$promise.then(function(result){

		if(result.p_out_Preguntas.length > 0)
		{					
			$scope.test= result;			
		}
		else if(result.P_OUT_MENSAJES[0].tipo =="ERROR")
		{
			var titulo ="UI_DETALLE_TITULO";
			bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
			conflictos:util.parseServerResponse(result)},$scope.ventanaId);
		}
			bcsComunes.setConflictos(util.parseServerResponse(result));
		},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				bcsComunes.setConflictos(util.parseServerResponse(result));
				console.log(JSON.stringify(result));
			});
				
				
	$scope.ResponderEncuesta = function() {		
		var p_Encuesta = [];
		var ventanaId = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaOverlay(ventanaId, true);

		for (var key in $scope.Respuestas)
		{
			p_Encuesta.push({ IdDefinicionPregunta: key, IdDefinicionRespuesta: $scope.Respuestas[key]});
		};		
		
		var respEncuesta = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdModulo : "PLA",
			p_IdUsuario : $scope.SesionIdUsuario,	
			p_IdCliente	: $scope.admSoc.dataSocioNegocio.IdCliente,
			p_IdDefinicionEncuesta : $scope.test.p_out_IdDefinicionEncuesta,
			p_IdCanal : "OPT",
			p_Respuestas : p_Encuesta
		};
	
		var urlRespEncuesta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100056";
		var r = $resource("", {}, {'RespEncuesta': { method: "POST", url: urlRespEncuesta}});
			
		var post = r.RespEncuesta(respEncuesta);
			post.$promise.then(function(result){
				
			bcsComunes.ventanaOverlay(ventanaId,false);
			if(result.P_OUT_ERRORESCONTADOR.length == 0)
			{					
				bcsComunes.ventanaMsg({mensaje:$translate.instant("Carga Exitosa" ),conflictos:util.parseServerResponse(respuesta)},idVentana);			
				}
			else if(result.P_OUT_ERRORESCONTADOR.length != 0)
			{
				var titulo ="UI_DETALLE_TITULO";
				bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
				conflictos:util.parseServerResponse(result)},$scope.ventanaId);
			}
			bcsComunes.setConflictos(util.parseServerResponse(result));
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
				conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				bcsComunes.setConflictos(util.parseServerResponse(result));
				console.log(JSON.stringify(result));
			});		

	};				
	});
}());
//------------------------- FIN CONTROLLER --------------------------
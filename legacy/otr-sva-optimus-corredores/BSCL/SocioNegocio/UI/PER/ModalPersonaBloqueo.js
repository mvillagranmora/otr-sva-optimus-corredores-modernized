/*************************************************************
 * Purpose	: Modal Persona Bloqueo
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalPersonaBloqueoCtrl', 
	function($scope,bcsComunes,$translate,$resource){
		$scope.dataCliente = {};
		var ventanaId = bcsComunes.ventanaWinId($scope);
		parametros = bcsComunes.ventanaParams (ventanaId);
		
		$scope.cerrar = function () {
			bcsComunes.ventanaCerrar(ventanaId); 
		};
		
		var bloqueoPersonaSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=108";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
	    
	    $scope.bloquearPersona = function() {
			var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdPersona =  parametros.IdPersona;
	        parametrosSP.p_IdEstado = 'INA';
	        parametrosSP.p_IdTipoBloqueo =  $scope.dataPersona.IdTipoBloqueo;
	        parametrosSP.p_FechaBloqueo = $scope.dataPersona.FechaBloqueo;
	        parametrosSP.p_Comentario = $scope.dataPersona.ComentarioBloqueo;
	    	    	
	        bloqueoPersonaSP(parametrosSP).then(
				function(result) {

					bcsComunes.ventanaMsg({ mensaje : "Persona Bloqueada",
						conflictos : util.parseServerResponse(result)}, ventanaId);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------
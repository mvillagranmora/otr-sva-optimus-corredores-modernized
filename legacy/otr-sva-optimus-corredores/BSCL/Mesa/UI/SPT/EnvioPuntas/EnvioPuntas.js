angular.module('ModuloMesa').controller('EnvioPuntas',function($scope,bcsComunes,$translate,$resource, bcsServiciosMesa){				
		
		$scope.data	= {}
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);

		$scope.data.IdSolicitudPuntas = parametros.IdSolicitudPuntas;
		$scope.data.UsuarioSolicitud = parametros.UsuarioSolicitud;
		$scope.data.Cliente = parametros.NombreCompletoPortafolio;
		$scope.data.IdMoneda = parametros.IdMoneda;
		$scope.data.TipoOperacion = parametros.TipoOperacion;
		$scope.data.Monto = parametros.Monto;
		$scope.data.TipoCambioCostoSolicitado = parametros.TipoCambioCostoSolicitado;	
		$scope.data.TipoCambioCostoAprobado = parametros.TipoCambioCostoAprobado;	
		$scope.data.ObservacionSolicitud = parametros.ObservacionSolicitud;
		$scope.data.ObservacionAutorizador = parametros.ObservacionAutorizador;
		
		$scope.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		
		$scope.Aceptar = function() {

			var datos = {};	
			bcsComunes.ventanaOverlay(idVentana,true);
			
			datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
			datos.p_IdUsuarioAutorizador	= $scope.SesionIdUsuario;
			datos.p_IdSolicitudPuntas		= $scope.data.IdSolicitudPuntas;						
			datos.p_IdEstado 				= 'APD';
			datos.p_TipoCambioCostoAprobado	= $scope.data.TipoCambioCostoAprobado;	
			datos.p_ObservacionAutorizador 	= $scope.data.ObservacionAutorizador;		
	
			 bcsServiciosMesa.AprobarRechazarSolicitudPuntas(datos,function (result){				
			 	bcsComunes.ventanaMsg({
			 		conflictos:util.parseServerResponse(result)
			    	},idVentana);
				bcsComunes.ventanaData(idVentana,result);
			 	bcsComunes.ventanaCerrar(idVentana);
			 }, function(resError){
			 	bcsComunes.msg("error","UI_ERROR_SOLICITUD_PUNTAS",$translate.instant("UI_REVISE_LOG"));						
			 	bcsComunes.ventanaCerrar(idVentana);
			 });
		};
	
});
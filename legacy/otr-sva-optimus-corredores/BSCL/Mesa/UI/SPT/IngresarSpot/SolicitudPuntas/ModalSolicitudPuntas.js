angular.module('ModuloMesa').controller('ModalSolicitudPuntas',function($scope,bcsComunes,$translate,$resource, bcsServiciosMesa){				
		
		$scope.data	= {}
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);

		$scope.data.Cliente = parametros.Cliente.NombreCompletoPortafolio;
		$scope.data.IdMoneda = parametros.Moneda.IdMonedaPrincipal;
		$scope.data.TipoOperacion = parametros.TipoOperacion;
		$scope.data.Monto = parametros.Monto;
		$scope.data.TipoCambioCierre = parametros.TipoCambioCierre;
		$scope.data.TipoCambioCosto = parametros.TipoCambioCosto;	
		$scope.data.ObservacionSolicitud = "";
		
		
		$scope.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		
		$scope.Aceptar = function() {

			var datos = {};	
			bcsComunes.ventanaOverlay(idVentana,true);
			
			datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
			datos.IdUsuarioSolicitud		= $scope.SesionIdUsuario;
			datos.IdUsuarioAutorizador		= 0;			
			datos.IdOperacion 				= 0;
			datos.IdTipoOperacion 			= $scope.data.TipoOperacion.IdTipoOperacion;
			datos.MontoMonedaPrincipal		= $scope.data.Monto;
			datos.IdMonedaPrincipal			= $scope.data.IdMoneda;
			datos.IdCliente 				= parametros.Cliente.IdCliente;	
			datos.IdPortafolio				= parametros.Cliente.IdPortafolio;
			datos.IdEstado 					= 'PEN';
			datos.TipoCambioCostoSolicitado = $scope.data.TipoCambioCosto;	
			datos.ObservacionSolicitud 		= $scope.data.ObservacionSolicitud;		
	
			 bcsServiciosMesa.CrearSolicitudPuntas(datos,function (result){				
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
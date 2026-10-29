angular.module('ModuloCuentaInversion').controller('ModalCobroComisionOpcionesCobrosCtrl',function(
	$scope,bcsServiciosCuentaInversion,bcsComunes,$translate) {
	
	var modccoc = this;
		
	modccoc.options 	= {};
	modccoc.filtro		= {};
    
    $scope.winID = bcsComunes.ventanaWinId($scope);
    bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	modccoc.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));	
	
	modccoc.optionsFechaVigencia = {};
	modccoc.optionsFechaVigencia.min = new Date(util.dateInicioDia($scope.SesionFechaSistema));
	modccoc.filtro.FechaDesde = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");


	modccoc.cliente = modccoc.datosEntrada.RutCliente + " " + modccoc.datosEntrada.Nombre
	
	
	modccoc.guardar = function(){

		let datos = {}
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo = 'CTA';
		datos.p_UsaReglasNegocio = 'N'
		datos.p_IdPortafolio = modccoc.datosEntrada.IdPortafolio;
		datos.p_MontoComision = modccoc.datosEntrada.MontoComision;
		datos.p_Periodo = modccoc.datosEntrada.Periodo;
		datos.p_EstadoComision = modccoc.datosEntrada.EstadoComision

		bcsServiciosCuentaInversion.EditarCobroComisionRegistro(datos).$promise.then(function(respuesta){
			bcsComunes.ventanaOverlay($scope.winID, false);
			  
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaCerrar( $scope.winID);

			} else{
				if (respuesta.P_OUT_ERRORESCONTADOR > 0){
					
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant(""),
						conflictos:util.parseServerResponse(respuesta)
					}, $scope.winID);
					
					
				} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant(""),
						conflictos:util.parseServerResponse(respuesta)
					}, $scope.winID);
					bcsComunes.ventanaCerrar( $scope.winID);
				}
			}
			
		},function(respuesta){
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.msg("error","Error",'UI_ERROR_EDITAR');
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		});

		
	};
	
	modccoc.cancelar = function() {
		bcsComunes.ventanaData( $scope.winID,false);
		bcsComunes.ventanaCerrar( $scope.winID);
	};
});
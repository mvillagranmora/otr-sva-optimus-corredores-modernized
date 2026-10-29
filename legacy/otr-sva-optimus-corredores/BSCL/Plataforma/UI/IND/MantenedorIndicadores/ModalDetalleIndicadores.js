angular.module('ModuloPlataforma').controller('ModalDetalleIndicadorCtrl',function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){
				
		mta = this;
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		
		$scope.DetalleIndicador = {};
		
		$scope.IdBotonAceptar = 'ModalDetalleAtributo_aceptar' + parametros.AccionBoton;
		
		if (parametros.detalle) //modificacion
		{
			console.log(parametros);
			 $scope.DetalleIndicador.Codigo = parametros.detalle.Codigo;
			 $scope.DetalleIndicador.NombreDetalleIndicador = parametros.detalle.DetalleIndicador;
			 bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_DETALLE_INDICADOR'));
		}
		else //AGREGAR
		{
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_DETALLE_INDICADOR'));
		}
				
		var agregarDetalleTipoAtributoSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=134";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
		
		$scope.Aceptar = function() {
			bcsComunes.ventanaOverlay(idVentana,true);
			//modificacion
			if (parametros.detalle){
	
				var parametrosSP = {};
		        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
		        parametrosSP.p_IdModulo = 'IND';
		        parametrosSP.p_UsaReglasNegocio = "N";
		        parametrosSP.p_IdTipoIndicador = parametros.tipoAtributo.IdTipoIndicador;
		        parametrosSP.p_IdDetalleIndicador = parametros.detalle.IdDetalleIndicador;
		        parametrosSP.p_DetalleIndicador = $scope.DetalleIndicador.NombreDetalleIndicador;
		        parametrosSP.p_Codigo = $scope.DetalleIndicador.Codigo;    	
		        agregarDetalleTipoAtributoSP(parametrosSP).then(
					function(result) {
						
						
						bcsComunes.ventanaMsg({ mensaje : "Detalle Tipo Indicador modificado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(idVentana,false);
					});	
			}
			//creacion
			else {
				
				var parametrosSP = {};
		        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
		        parametrosSP.p_IdModulo = 'IND';
		        parametrosSP.p_UsaReglasNegocio = "N";
		        parametrosSP.p_IdTipoIndicador = parametros.tipoAtributo.IdTipoIndicador;
		        parametrosSP.p_DetalleIndicador = $scope.DetalleIndicador.NombreDetalleIndicador;
		        parametrosSP.p_Codigo = $scope.DetalleIndicador.Codigo;
		        
		            	
		        agregarDetalleTipoAtributoSP(parametrosSP).then(
					function(result) {
						bcsComunes.ventanaMsg({ mensaje : "Los datos fueron modificados",
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(idVentana,false);
					});	
			}
		};
		
		$scope.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		
	
	
});
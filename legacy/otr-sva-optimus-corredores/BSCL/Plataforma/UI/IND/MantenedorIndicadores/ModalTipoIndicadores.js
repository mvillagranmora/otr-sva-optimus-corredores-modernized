angular.module('ModuloPlataforma').controller('ModalTipoIndicadorCtrl',function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){
				
		mta = this;
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		
		$scope.IdTipoEntidad = parametros.IdTipoEntidad;
		
		$scope.TipoAtributo = $.extend({},parametros.TipoAtributo);
		$scope.TipoAtributo.IdTipoPersona = ($scope.TipoAtributo.IdTipoPersona === 'NJ' || $scope.TipoAtributo.IdTipoPersona==="A") ? '0' : $scope.TipoAtributo.IdTipoPersona;
		$scope.TipoAtributo.RequiereAutorizacion = $scope.TipoAtributo.RequiereAutorizacion === 'S' ? true:false;
		$scope.TipoAtributo.RequiereFechaVencimiento = $scope.TipoAtributo.RequiereFechaVencimiento === 'S' ? true:false;
		$scope.TipoAtributoOriginal = $.extend({},parametros.TipoAtributo);
		
		$scope.IdBotonAceptar = 'ModalTipoAtributo_aceptar' + parametros.AccionBoton;
		
		if (parametros.TipoAtributo) //modificacion
		{
			//$scope.titulo = 'UI_MODIFICAR_TIPO_ATRIBUTO';
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_TIPO_INDICADOR'));
			$scope.Habilitado = false;
		}
		else //AGREGAR
		{
			//$scope.titulo = 'UI_AGREGAR_TIPO_ATRIBUTO';
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_TIPO_INDICADOR'));
			$scope.Habilitado = true;
		}
				
		$scope.Aceptar = function() {
			//modificacion
			if ($scope.TipoAtributo.IdTipoIndicador){
		
		
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'ATR',
					p_UsaReglasNegocio :'N',
					p_IdTipoIndicador: $scope.TipoAtributo.IdTipoIndicador,
					p_NombreTipoIndicador : $scope.TipoAtributo.NombreTipoIndicador,
					p_GrupoIndicador : $scope.TipoAtributo.GrupoIndicador,
					p_IdEstado : $scope.TipoAtributo.EstadoIndicadorSeleccionado.IdEstado,
					p_RequiereAutorizacion : $scope.TipoAtributo.RequiereAutorizacion ? 'S' : 'N' ,
					p_IdTipoPersona	: $scope.TipoAtributo.TipoPersonaSeleccionado.IdTipoPersona === 0 ? 'A' : $scope.TipoAtributo.TipoPersonaSeleccionado.IdTipoPersona,
					p_RequiereFechaVencimiento : $scope.TipoAtributo.RequiereFechaVencimiento ? 'S':'N'	,
					p_ValorIndicadorDefecto :  $scope.TipoAtributo.ValorIndicadorDefecto,
					p_CantidadDecimales		: $scope.TipoAtributo.CantidadDecimales,
				};
				bcsComunes.ventanaOverlay(idVentana, true);
				bcsServicioPlataforma.ModificarTipoAtributo(datos).then(
					function(result) {
						var mensajeId = bcsComunes.ventanaMsg({ mensaje : "Tipo Atributo modificado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(mensajeId).then(function(){
							bcsComunes.ventanaOverlay(idVentana, false);
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al modificar Tipo de Atributo");
						bcsComunes.ventanaOverlay(idVentana, false);
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
					});
				
				
				
			}
			//creacion
			else {
				
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'ATR',
					p_UsaReglasNegocio :'N',
					
					p_IdTipoEntidad 		: parametros.IdTipoEntidad,
					p_ValorIndicadorDefecto : $scope.TipoAtributo.ValorIndicadorDefecto,
					p_ClaseDato 			: $scope.TipoAtributo.ClaseDatoSeleccionado.TipoDato,
					p_NombreTipoIndicador 	: $scope.TipoAtributo.NombreTipoIndicador,
					p_CodigoTipoIndicador   : $scope.TipoAtributo.CodigoTipoIndicador,
					p_GrupoIndicador 		: $scope.TipoAtributo.GrupoIndicador,
					p_IdEstado 				: $scope.TipoAtributo.EstadoIndicadorSeleccionado.IdEstado,
					p_CantidadDecimales		: $scope.TipoAtributo.CantidadDecimales || 0,
					p_LongitudMinima		: $scope.TipoAtributo.LongitudMinima || 0,
					p_LongitudMaxima		: $scope.TipoAtributo.LongitudMaxima || 0,
					p_RequiereAutorizacion 	: $scope.TipoAtributo.RequiereAutorizacion ? 'S' : 'N',
					p_IdTipoPersona			: $scope.TipoAtributo.TipoPersonaSeleccionado.IdTipoPersona === 0 ? 'A' : $scope.TipoAtributo.TipoPersonaSeleccionado.IdTipoPersona,
					p_RequiereFechaVencimiento : $scope.TipoAtributo.RequiereFechaVencimiento ? 'S':'N'	,
				};
				bcsComunes.ventanaOverlay(idVentana, true);
				bcsServicioPlataforma.AgregarTipoAtributo(datos).then(
					function(result) {
						console.log(result);
						var mensajeId = bcsComunes.ventanaMsg({ mensaje : "Tipo de Indicador agregado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(mensajeId).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana, false);
						});
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al crear Tipo de Indicador");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana, false);
					});
				
			}
			
			
		};
		
		$scope.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		
	
	
});
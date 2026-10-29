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

	$scope.modccoc.OpcionCobro={
		tipo          : "combobox-opciones-cobro-disponibles" ,
		configuracion : {},
		filtros       : {},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
	};
	
	$scope.modccoc.Mercado={
		tipo          : "combobox-cobro-comision-mercados",
		configuracion : {},
		filtros       : {},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
	};
	
	$scope.modccoc.Estado={
		tipo          : "combobox-estado-instrumento",
		configuracion : {},
		filtros       : {},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
	};
	
	if(modccoc.datosEntrada.IdOpcionCobro!=undefined){
    	bcsComunes.ventanaTitulo($scope.winID,$translate.instant("UI_EDITAR_OPCION_COBRO"));
        $scope.modccoc.OpcionCobro.idPostFiltro = modccoc.datosEntrada.OpcionCobro;
        $scope.modccoc.PortafolioAPV = modccoc.datosEntrada.PortafolioAPV == 'S' ? true : false;
        $scope.modccoc.Mercado.idPostFiltro = modccoc.datosEntrada.IdClasificacionTipoInstrumento;
        $scope.modccoc.PorcentajeCobro = modccoc.datosEntrada.PorcentajeCobro;
        $scope.modccoc.Estado.idPostFiltro = modccoc.datosEntrada.IdEstado;
        $scope.modccoc.filtro.FechaDesde = util.dateSPdate(modccoc.datosEntrada.FechaVigencia);
	}
	else{
    	bcsComunes.ventanaTitulo($scope.winID,$translate.instant("UI_AGREGAR_OPCION_COBRO"));
	}
	
	modccoc.guardar = function(){
		//modificacion
		if (modccoc.datosEntrada.IdOpcionCobro!=undefined){
			
			if(modccoc.PorcentajeCobro < 0 || modccoc.PorcentajeCobro > 1){
				bcsComunes.msg("error","error"," El valor del Porcentaje Cobro debe ser un numero entre 0 y 1");
				return;
			}

			let OpcionCobro = {
				IdOpcionCobro: modccoc.datosEntrada.IdOpcionCobro,
				OpcionCobro: modccoc.OpcionCobro.seleccionado.IdDetalleIndicador,
				PortafolioAPV: modccoc.PortafolioAPV?'S':'N',
				Mercado: modccoc.Mercado.seleccionado.Codigo,
				PorcentajeCobro: modccoc.PorcentajeCobro,
				Estado: modccoc.Estado.seleccionado.IdEstado,
				FechaVigencia: modccoc.filtro.FechaDesde,
			}
			
			var datos = {
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdModulo			: 'CTA',
				p_UsaReglasNegocio	: 'N',
		
				p_Opciones			: [OpcionCobro],
			};
			
			bcsServiciosCuentaInversion.EditarCobroComisionOpcionCobro(datos).$promise.then(
				function(result) {
					console.log(result);
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = "La operación tiene advertencias";
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = "Error al editar opción de cobro";
					}
					else{
						bcsComunes.msg("info","info","Opción modificada");
					}	
					
					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						}, $scope.winID);
					bcsComunes.ventanaData( $scope.winID,true);
					bcsComunes.ventanaCerrar( $scope.winID);
				},
				function(result){
					bcsComunes.msg("error","error","Error al editar opción");
					bcsComunes.ventanaData( $scope.winID,true);
					bcsComunes.ventanaCerrar( $scope.winID);
				});
		}
		//creacion
		else{
			
			if(modccoc.PorcentajeCobro < 0 || modccoc.PorcentajeCobro > 1){
				bcsComunes.msg("error","error"," El valor del Porcentaje Cobro debe ser un numero entre 0 y 1");
				return;
			}
			
			let OpcionCobro = {
				OpcionCobro: modccoc.OpcionCobro.seleccionado.IdDetalleIndicador,
				PortafolioAPV: modccoc.PortafolioAPV?'S':'N',
				Mercado: modccoc.Mercado.seleccionado.Codigo,
				PorcentajeCobro: modccoc.PorcentajeCobro,
				Estado: modccoc.Estado.seleccionado.IdEstado,
				FechaVigencia: modccoc.filtro.FechaDesde,
			}
			
			var datos = {
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdModulo			: 'CTA',
				p_UsaReglasNegocio	: 'N',
		
				p_Opciones			: [OpcionCobro],
			};
			
			bcsServiciosCuentaInversion.CrearCobroComisionOpcionCobro(datos).$promise.then(
				function(result) {
					console.log(result);
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = "La operación tiene advertencias";
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = "Error al ingresar opción de cobro";
					}
					else{
						bcsComunes.msg("info","info","Opción agregada");
					}	
					
					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						}, $scope.winID);
					bcsComunes.ventanaData( $scope.winID,true);
					bcsComunes.ventanaCerrar( $scope.winID);
				},
				function(result){
					console.log(result);
					bcsComunes.msg("error","error","Error al agregar opción");
					bcsComunes.ventanaData( $scope.winID,true);
					bcsComunes.ventanaCerrar( $scope.winID);
				});
		}
	};
	
	modccoc.cancelar = function() {
		bcsComunes.ventanaData( $scope.winID,false);
		bcsComunes.ventanaCerrar( $scope.winID);
	};
});
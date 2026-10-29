(function() {
	angular.module('ModuloMesa').controller('ModalOperadoresMesaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsConfiguracionSociodeNegocio){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mOM 		= this;
		mOM.idVentana	= bcsComunes.ventanaWinId($scope);
		mOM.parametros	= bcsComunes.ventanaParams(mOM.idVentana);
		mOM.domVentana	= $("[ventanaid='"+mOM.idVentana+"']");
		
		var opeMesSel = _.clone(mOM.parametros.OperadorMesaSel);
		
		mOM.win			= {};
		mOM.cmbbx		= {};
		mOM.fn			= {};
		mOM.grilla		= {};
		
		mOM.datosOpeMes	= {};
		mOM.datosOpeMes.IdEstadoDef = 'ACT';

		mOM.datosOpeMes.SuperUsuario = {confSuperUsr: false};
		
		mOM.IdBotonAceptar = 'ModalOperadoresMesa_aceptar' + mOM.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(opeMesSel){
			bcsComunes.ventanaTitulo(mOM.idVentana, $translate.instant('UI_MODIFICAR_AGENTE_OPERADOR_MESA'));
			mOM.datosOpeMes.IdMesa		 = opeMesSel.IdMesa;
			mOM.datosOpeMes.IdMesaDef	 = opeMesSel.IdMesa;
			mOM.datosOpeMes.IdAgenteDef	 = opeMesSel.IdAgenteOperador || 0;
			mOM.datosOpeMes.IdAgenteDef	 = opeMesSel.IdAgenteOperador || 0;
			mOM.datosOpeMes.IdEstado	 = opeMesSel.IdEstado || 'ACT';
			mOM.datosOpeMes.IdEstadoDef	 = opeMesSel.IdEstado || 'ACT';
			mOM.datosOpeMes.SuperUsuario = {confSuperUsr: opeMesSel.ModificaOperacionOtroUsuario == "S" || false};
			
			mOM.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mOM.idVentana, $translate.instant('UI_AGREGAR_AGENTE_OPERADOR_MESA'));
			mOM.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mOM.cmbbx.Mesa = {
			filtros: {
				IdEmpresa	: { field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstadoRegistro",	operator: "eq", value: "ACT" }
			},
			configuracion : {},
		};
		
		
		// Los CodigoTipoCargoEmpleado pueden ser AGENTE, OPERADOR, OPERADOR MESA DE DINERO - TODO revisar si son esos 
		var codigoTipoCargo = [ { field: "CodigoTipoCargoEmpleado", operator: "eq", value: "AGE" },
		                        { field: "CodigoTipoCargoEmpleado", operator: "eq", value: "OPEOTC" },
		                        { field: "CodigoTipoCargoEmpleado", operator: "eq", value: "OPEMES" }
		                        ];
		mOM.cmbbx.Agente={
			filtros : {
				complejo: codigoTipoCargo,
				logic	: "or",
				filters	: codigoTipoCargo
			},
			configuracion : {
				placeholder: $translate.instant('UI_AGENTE_OPERADOR_MESA'),
			},
		};
		
		
		mOM.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MESA" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mOM.fn.cancelar = function () {
			bcsComunes.ventanaData(mOM.idVentana, false);
			bcsComunes.ventanaCerrar(mOM.idVentana);
		};
		
		
		mOM.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(opeMesSel && opeMesSel.CodigoMesa) { // EDITAR
				if(mOM.datosOpeMes.IdMesa	=== opeMesSel.IdMesa && 
				   mOM.datosOpeMes.IdAgente	=== opeMesSel.IdAgenteOperador && 
				   mOM.datosOpeMes.IdEstado	=== opeMesSel.IdEstado &&
				   (mOM.datosOpeMes.SuperUsuario.confSuperUsr ? 'S' : 'N') === opeMesSel.ModificaOperacionOtroUsuario){
					bcsComunes.msg("warning","UI_AGENTE_OPERADOR_MESA",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mOM.idVentana, false);
					bcsComunes.ventanaCerrar(mOM.idVentana);
					return
				}
				
				datosGuardar.p_IdAgenteOperadorMesa	        = opeMesSel.IdAgenteOperadorMesa;
				datosGuardar.p_IdMesa				        = mOM.datosOpeMes.IdMesa;
				datosGuardar.p_IdAgenteOperador		        = mOM.datosOpeMes.IdAgente;
				datosGuardar.p_IdEstado				        = mOM.datosOpeMes.IdEstado;
				datosGuardar.p_ModificaOperacionOtroUsuario = mOM.datosOpeMes.SuperUsuario.confSuperUsr ? "S" : "N";
				
				datosGuardar.p_DetalleLog = [];
				if(mOM.datosOpeMes.IdAgente !== opeMesSel.IdAgenteOperador)
					datosGuardar.p_DetalleLog.push({SubRegistro:opeMesSel.CodigoMesa, CampoRegistro:'IdAgenteOperador', ValorAntiguo:opeMesSel.IdAgenteOperador, ValorNuevo:mOM.datosOpeMes.IdAgente});
				if(mOM.datosOpeMes.IdEstado !== opeMesSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:opeMesSel.CodigoMesa, CampoRegistro:'IdEstado', ValorAntiguo:opeMesSel.IdEstado, ValorNuevo:mOM.datosOpeMes.IdEstado});
				
				bcsComunes.ventanaOverlay(mOM.idVentana,true);
				bcsServiciosMesa.ModificarAgenteOperadorMesa(datosGuardar,function (result){
					mOM.fn.evaluaRespuestaSP(result, 'UI_AGENTE_OPERADOR_MESA_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_AGENTE_OPERADOR_MESA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mOM.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdMesa			            = mOM.datosOpeMes.IdMesa;
				datosGuardar.p_IdAgenteOperador	            = mOM.datosOpeMes.IdAgente;
				datosGuardar.p_IdEstado					    = mOM.datosOpeMes.IdEstado;
				datosGuardar.p_ModificaOperacionOtroUsuario = mOM.datosOpeMes.SuperUsuario.confSuperUsr ? "S" : "N";

				if(datosGuardar.p_IdMesa === false){
					bcsComunes.msg("error","UI_AGENTE_OPERADOR_MESA",$translate.instant("UI_REVISE_LOG"));
					return;
				}	

				bcsComunes.ventanaOverlay(mOM.idVentana,true);
				bcsServiciosMesa.CrearAgenteOperadorMesa(datosGuardar,function (result){
					mOM.fn.evaluaRespuestaSP(result, 'UI_AGENTE_OPERADOR_MESA_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_AGENTE_OPERADOR_MESA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mOM.idVentana,false);
				});
			}
		};
		
		
		mOM.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_AGENTE_OPERADOR_MESA",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_AGENTE_OPERADOR_MESA",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_AGENTE_OPERADOR_MESA",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_AGENTE_OPERADOR_MESA",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mOM.idVentana,false);
			bcsComunes.ventanaData(mOM.idVentana, true);
			bcsComunes.ventanaCerrar(mOM.idVentana);
		};
	});
}());
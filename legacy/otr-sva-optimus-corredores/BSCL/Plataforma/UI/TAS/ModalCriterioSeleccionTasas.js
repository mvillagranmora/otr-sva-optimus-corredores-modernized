(function() {
	angular.module('ModuloPlataforma').controller('ModalCriterioSeleccionTasasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 **********************************************/
	
		var mCST 		= this;
		mCST.idVentana	= bcsComunes.ventanaWinId($scope);
		mCST.parametros	= bcsComunes.ventanaParams(mCST.idVentana);
		mCST.domVentana	= $("[ventanaid='"+mCST.idVentana+"']");
		
		var criterioSel = _.clone(mCST.parametros.CriterioSel);
		
		mCST.win	= {};
		mCST.cmbbx	= {};
		mCST.fn		= {};
		
		mCST.datosCriterio	= {};
		mCST.IdBotonAceptar	= 'ModalCriterioSeleccionTasas_aceptar' + mCST.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(criterioSel){
			bcsComunes.ventanaTitulo(mCST.idVentana, $translate.instant('UI_MODIFICAR_CRITERIO'));
			
			mCST.datosCriterio.Prioridad							= criterioSel.Prioridad || 0;
			mCST.datosCriterio.Valor								= criterioSel.TipoCartera || 0;
			mCST.datosCriterio.ValorDef								= criterioSel.TipoCartera || 0;
			mCST.datosCriterio.IdUso								= criterioSel.IdUso || 0;
			mCST.datosCriterio.IdUsoDef								= criterioSel.IdUso || 0;
			mCST.datosCriterio.IdContraparte						= criterioSel.IdContraparte || 0;
			mCST.datosCriterio.IdContraparteDef						= criterioSel.IdContraparte || 0;
			mCST.datosCriterio.IdClasificacionTipoInstrumento		= criterioSel.IdClasificacionTipoInstrumento || 0;
			mCST.datosCriterio.IdClasificacionTipoInstrumentoDef	= criterioSel.IdClasificacionTipoInstrumento || 0;
			mCST.datosCriterio.IdTipoInstrumento					= criterioSel.IdTipoInstrumento === '0' ? 0 : criterioSel.IdTipoInstrumento;
			mCST.datosCriterio.IdTipoInstrumentoDef					= criterioSel.IdTipoInstrumento === '0' ? 0 : criterioSel.IdTipoInstrumento;
			mCST.datosCriterio.DiasAntiguedad						= criterioSel.DiasAntiguedad || 0;
			mCST.datosCriterio.IdTipoCriterioTasa					= criterioSel.IdTipoCriterioTasa || 0;
			mCST.datosCriterio.IdTipoCriterioTasaDef				= criterioSel.IdTipoCriterioTasa || 0;
			mCST.datosCriterio.Codigo								= criterioSel.CodigoFuente || 0;
			mCST.datosCriterio.CodigoDef							= criterioSel.CodigoFuente || 0;
			mCST.datosCriterio.IdTipoTasa							= criterioSel.IdTipoTasa || 0;
			mCST.datosCriterio.IdTipoTasaDef						= criterioSel.IdTipoTasa || 0;
			mCST.datosCriterio.IdTipoPrecio							= criterioSel.IdTipoPrecio || 0;
			mCST.datosCriterio.IdTipoPrecioDef						= criterioSel.IdTipoPrecio || 0;
			mCST.datosCriterio.Interpolar							= criterioSel.Interpolar === 'S' ? true : false;
			mCST.datosCriterio.NemotecnicoAct                          = criterioSel.Nemotecnico || ' TODOS';
			mCST.datosCriterio.NemotecnicoDef                       = criterioSel.Nemotecnico || ' TODOS';


			mCST.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mCST.idVentana, $translate.instant('UI_AGREGAR_CRITERIO'));
			mCST.datosCriterio.Prioridad			= 0;
			mCST.datosCriterio.ValorDef				= 0;
			mCST.datosCriterio.DiasAntiguedad		= 0;
			mCST.datosCriterio.IdTipoCriterioTasaDef= 0;
			mCST.datosCriterio.IdContraparteDef		= 0;
			mCST.datosCriterio.Nemotecnico          = '';
			mCST.datosCriterio.NemotecnicoDef         = ' TODOS';
			
			mCST.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mCST.cmbbx.TipoCartera={
			filtros: {
				IdEmpresa		: { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa },
				NombreEntidad	: { field: "NombreEntidad",	operator: "eq", value: "TIPOCARTERATASAS" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_TIPO_CARTERA'),
			},
		};
		
		mCST.cmbbx.UsoCriterio={
			filtros: {
				IdEmpresa	: { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstado",	operator: "eq", value: "ACT" }
			}
		};
		

		mCST.cmbbx.EntidadDestinoGarantia={
			filtros : {
				//IdEstado		: { "field" : "IdEstadoEntidadDestinoGarantia", "operator": "eq", "value" : "ACT" }
			}
		};

		mCST.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				IdEstado : {field: "IdEstado",	operator: "eq" , value: "ACT" }
			}
		};
		
		mCST.cmbbx.TipoInstrumento={
			filtros : {
				IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
				IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: '-1' }
			}
		};
		
		mCST.cmbbx.TipoOperacion={
			filtros : {
				IdTipoInstrumento : { field : "IdTipoInstrumento", operator: "eq", value : '-1' }
			}
		};
		
		mCST.cmbbx.TipoCriterioTasa={
			filtros : {
				IdEmpresa	: { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstado",	operator: "eq", value: "ACT" }
			}
		};
		
		mCST.cmbbx.Fuente={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'},
				//NombreEntidad:{field: "NombreEntidad",	operator: "eq", value: 'CRITERIO'}
			}
		};
		
		mCST.cmbbx.TipoTasa={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		
		mCST.cmbbx.TipoPrecio={
			filtros: {
				IdEstado : {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		mCST.cmbbx.InstrumentoIF = {
			filtros : {
				IdTipoInstrumento : { field: "IdTipoInstrumento", operator: "eq", value: 'PAG' }
			}
		};
		
		/*
		mCST.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MANTENEDORCRITERIOS" }
			}
		};
		*/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mCST.fn.cancelar = function () {
			bcsComunes.ventanaData(mCST.idVentana, false);
			bcsComunes.ventanaCerrar(mCST.idVentana);
		};
		
		
		mCST.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(criterioSel && criterioSel.IdCriterioSeleccionTasas) { // EDITAR
				if(mCST.datosCriterio.Prioridad			=== criterioSel.Prioridad && 
				   mCST.datosCriterio.IdTipoInstrumento	=== criterioSel.IdTipoInstrumento && 
				   mCST.datosCriterio.DiasAntiguedad	=== criterioSel.DiasAntiguedad && 
				   mCST.datosCriterio.IdTipoCriterioTasa=== criterioSel.IdTipoCriterioTasa && 
				   mCST.datosCriterio.Codigo			=== criterioSel.CodigoFuente && 
				   mCST.datosCriterio.IdTipoTasa		=== criterioSel.IdTipoTasa && 
				   mCST.datosCriterio.IdTipoPrecio		=== criterioSel.IdTipoPrecio && 
				  (mCST.datosCriterio.Interpolar		=== (criterioSel.Interpolar === 'S' ? true : false))
				){
					bcsComunes.msg("warning","UI_CRITERIO_SELECCION_TASAS",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mCST.idVentana, false);
					bcsComunes.ventanaCerrar(mCST.idVentana);
					return;
				}
				
				datosGuardar.p_IdCriterioSeleccionTasas	= criterioSel.IdCriterioSeleccionTasas;
				datosGuardar.p_Prioridad			= mCST.datosCriterio.Prioridad;
				datosGuardar.p_IdTipoInstrumento	= mCST.datosCriterio.IdTipoInstrumento;
				datosGuardar.p_DiasAntiguedad		= mCST.datosCriterio.DiasAntiguedad;
				datosGuardar.p_IdTipoCriterioTasa	= mCST.datosCriterio.IdTipoCriterioTasa;
				datosGuardar.p_CodigoFuente			= mCST.datosCriterio.Codigo;
				datosGuardar.p_IdTipoTasa			= mCST.datosCriterio.IdTipoTasa;
				datosGuardar.p_IdTipoPrecio			= mCST.datosCriterio.IdTipoPrecio;
				datosGuardar.p_IdContraparte		= mCST.datosCriterio.IdContraparte > 0 ? mCST.datosCriterio.IdContraparte : mCST.datosCriterio.IdContraparteDef;
				datosGuardar.p_Interpolar			= mCST.datosCriterio.Interpolar ? 'S' : 'N';
				datosGuardar.p_Nemotecnico          = mCST.datosCriterio.Nemotecnico;

				datosGuardar.p_DetalleLog = [];
				if(mCST.datosCriterio.Prioridad !== criterioSel.Prioridad)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'Prioridad', ValorAntiguo:criterioSel.Prioridad, ValorNuevo:mCST.datosCriterio.Prioridad});

				if(mCST.datosCriterio.IdContraparte !== criterioSel.IdContraparte)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'IdContraparte', ValorAntiguo:criterioSel.IdContraparte, ValorNuevo:mCST.datosCriterio.IdContraparte});
					
				if(mCST.datosCriterio.IdTipoInstrumento !== criterioSel.IdTipoInstrumento)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'IdTipoInstrumento', ValorAntiguo:criterioSel.IdTipoInstrumento, ValorNuevo:mCST.datosCriterio.IdTipoInstrumento});
				if(mCST.datosCriterio.DiasAntiguedad !== criterioSel.DiasAntiguedad)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'DiasAntiguedad', ValorAntiguo:criterioSel.DiasAntiguedad, ValorNuevo:mCST.datosCriterio.DiasAntiguedad});
				if(mCST.datosCriterio.IdTipoCriterioTasa !== criterioSel.IdTipoCriterioTasa)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'IdTipoCriterioTasa', ValorAntiguo:criterioSel.IdTipoCriterioTasa, ValorNuevo:mCST.datosCriterio.IdTipoCriterioTasa});
				if(mCST.datosCriterio.Codigo !== criterioSel.CodigoFuente)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'CodigoFuente', ValorAntiguo:criterioSel.CodigoFuente, ValorNuevo:mCST.datosCriterio.Codigo});
				if(mCST.datosCriterio.IdTipoTasa !== criterioSel.IdTipoTasa)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'IdTipoTasa', ValorAntiguo:criterioSel.IdTipoTasa, ValorNuevo:mCST.datosCriterio.IdTipoTasa});
				if(mCST.datosCriterio.IdTipoPrecio !== criterioSel.IdTipoPrecio)
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'IdTipoPrecio', ValorAntiguo:criterioSel.IdTipoPrecio, ValorNuevo:mCST.datosCriterio.IdTipoPrecio});
				if(mCST.datosCriterio.Interpolar !== (criterioSel.Interpolar === 'S' ? true : false))
					datosGuardar.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'Interpolar', ValorAntiguo:criterioSel.Interpolar, ValorNuevo:mCST.datosCriterio.Interpolar ? 'S' : 'N'});
				
				bcsComunes.ventanaOverlay(mCST.idVentana,true);
				bcsServiciosTasas.modificarCriterioSeleccionTasas(datosGuardar,function (result){
					mCST.fn.evaluaRespuestaSP(result, 'UI_CRITERIO_SELECCION_TASAS_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mCST.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_Prioridad			= mCST.datosCriterio.Prioridad;
				datosGuardar.p_TipoCartera			= mCST.datosCriterio.Valor;
				datosGuardar.p_IdContraparte		= mCST.datosCriterio.IdContraparte  > 0 ? mCST.datosCriterio.IdContraparte : mCST.datosCriterio.IdContraparteDef;
				datosGuardar.p_IdUso				= mCST.datosCriterio.IdUso;
				datosGuardar.p_IdClasificacionTipoInstrumento	= mCST.datosCriterio.IdClasificacionTipoInstrumento;
				datosGuardar.p_IdTipoInstrumento	= mCST.datosCriterio.IdTipoInstrumento;
				datosGuardar.p_DiasAntiguedad		= mCST.datosCriterio.DiasAntiguedad;
				datosGuardar.p_IdTipoCriterioTasa	= mCST.datosCriterio.IdTipoCriterioTasa;
				datosGuardar.p_CodigoFuente			= mCST.datosCriterio.Codigo;
				datosGuardar.p_IdTipoTasa			= mCST.datosCriterio.IdTipoTasa;
				datosGuardar.p_IdTipoPrecio			= $scope.SesionLocale === 'CL' ? mCST.datosCriterio.IdTipoPrecio : '';
				datosGuardar.p_Interpolar			= mCST.datosCriterio.Interpolar ? 'S' : 'N';
				datosGuardar.p_Nemotecnico          = mCST.datosCriterio.Nemotecnico;
				bcsComunes.ventanaOverlay(mCST.idVentana,true);
				bcsServiciosTasas.crearCriterioSeleccionTasas(datosGuardar,function (result){
					mCST.fn.evaluaRespuestaSP(result, 'UI_CRITERIO_SELECCION_TASAS_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mCST.idVentana,false);
				});
			}
		};
		
		
		mCST.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_CRITERIO_SELECCION_TASAS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_CRITERIO_SELECCION_TASAS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",$translate.instant("UI_REVISE_LOG"));
			
			//console.log(result);
			bcsComunes.ventanaOverlay(mCST.idVentana,false);
			bcsComunes.ventanaData(mCST.idVentana, true);
			bcsComunes.ventanaCerrar(mCST.idVentana);
		};
		
		$scope.$watch('mCST.datosCriterio.ClasificacionTipoInstrumentoObj',function(newval,oldval){
			if(!newval || newval === oldval) return;
			if(mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento !== 'IF'){
				mCST.datosCriterio.Nemotecnico = '';
			}
			if((mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento === 'DV' && mCST.datosCriterio.TipoInstrumentoObj.IdTipoInstrumento === 'FWD') ||
				mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento === 'FW'){
				mCST.datosCriterio.IdTipoPrecio = undefined;
			}
		});
		
		$scope.$watch('mCST.datosCriterio.TipoInstrumentoObj',function(newval,oldval){
			if(!newval || newval === oldval) return;
			if((mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdTipoInstrumento === 'PAC' || mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento !== 'IF') && mCST.datosCriterio.InstrumentoIFObj){
				mCST.datosCriterio.Nemotecnico = '';
			}
			if((mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento === 'DV' && mCST.datosCriterio.TipoInstrumentoObj.IdTipoInstrumento === 'FWD') ||
				mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento === 'FW'){
				mCST.datosCriterio.IdTipoPrecio = undefined;
			}
		});

		$scope.$watch('mCST.datosCriterio.InstrumentoIFObj',function(newval,oldval){
			if(!newval || newval === oldval) return;
			
			if(mCST.datosCriterio.ClasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento !== 'IF'){
				mCST.datosCriterio.Nemotecnico = '';
			}
			if(mCST.datosCriterio.InstrumentoIFObj.Nemotecnico !== ' TODOS')
			{
				mCST.datosCriterio.Nemotecnico = mCST.datosCriterio.InstrumentoIFObj.Nemotecnico;
			} else {
				mCST.datosCriterio.Nemotecnico = '';
			}
		});
	});
}());
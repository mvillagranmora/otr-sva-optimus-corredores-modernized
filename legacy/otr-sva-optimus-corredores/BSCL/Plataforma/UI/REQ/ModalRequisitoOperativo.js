angular.module('ModuloPlataforma').controller('ModalRequisitosOperativosCtrl',function($scope,$translate,bcsComunes,$uibModal,$resource) {
	
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
	
	var mReqOp = this;
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	mReqOp.RequisitoOperativo			= $.extend({},parametros.ReqOpSeleccionado);
	mReqOp.RequisitoOperativoOriginal	= $.extend({},parametros.ReqOpSeleccionado);
	
	mReqOp.IdBotonAceptar = 'ModalRequisitosOperativos_aceptar' + parametros.AccionBoton;
	
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
	
	if(parametros.ReqOpSeleccionado){
		//modificacion
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_REQUISITO_OPERATIVO'));
		
		mReqOp.RequisitoOperativo = {};
		mReqOp.RequisitoOperativo.IdModeloRequisitoOperativo= parametros.ReqOpSeleccionado.IdModeloRequisitoOperativo;
		mReqOp.RequisitoOperativo.IdRequisitoOperativo		= parametros.ReqOpSeleccionado.IdRequisitoOperativo;
		mReqOp.RequisitoOperativo.IdCondicionOperativa		= parametros.ReqOpSeleccionado.IdCondicionOperativa;
		mReqOp.RequisitoOperativo.IdTipoEtapa				= parametros.ReqOpSeleccionado.IdTipoEtapa;
		mReqOp.RequisitoOperativo.IdTipoComportamiento		= parametros.ReqOpSeleccionado.IdTipoComportamiento;
		mReqOp.RequisitoOperativo.IdTipoCanalIngreso		= parametros.ReqOpSeleccionado.IdTipoCanalIngreso;
		mReqOp.RequisitoOperativo.ValorAtributo1			= parametros.ReqOpSeleccionado.ValorAtributo1;
		mReqOp.RequisitoOperativo.ValorAtributo2			= parametros.ReqOpSeleccionado.ValorAtributo2;
		mReqOp.RequisitoOperativo.IdEstado					= parametros.ReqOpSeleccionado.IdEstado;
		
		mReqOp.RequisitoOperativoOriginal	= $.extend({},mReqOp.RequisitoOperativo);
		mReqOp.RequisitoOperativo.modoCrear	= false;
	}
	else{
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_REQUISITO_OPERATIVO'));
		mReqOp.RequisitoOperativo = {};
		
		mReqOp.RequisitoOperativo.modoCrear = true;
	}
	
	mReqOp.aceptar = function(){
		//modificacion
		if(!mReqOp.RequisitoOperativo.modoCrear){
			var datos = {
				p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
				p_IdUsuario			: $scope.$root.SesionIdUsuario,
				p_IdModulo			: 'REQ',
				p_UsaReglasNegocio	: 'N',
				
				p_IdRequisitoOperativo		: mReqOp.RequisitoOperativo.IdRequisitoOperativo,
				p_IdModeloRequisitoOperativo: mReqOp.RequisitoOperativo.IdModeloRequisitoOperativo,
				p_IdCondicionOperativa		: mReqOp.RequisitoOperativo.IdCondicionOperativa,
				p_IdTipoEtapa				: mReqOp.RequisitoOperativo.IdTipoEtapa,
				p_IdTipoComportamiento		: mReqOp.RequisitoOperativo.IdTipoComportamiento,
				p_IdTipoCanalIngreso		: mReqOp.RequisitoOperativo.IdTipoCanalIngreso,
				p_IdMoneda					: mReqOp.RequisitoOperativo.IdMoneda,
				p_ValorAtributo1			: mReqOp.RequisitoOperativo.ValorAtributo1,
				p_ValorAtributo2			: mReqOp.RequisitoOperativo.ValorAtributo2,
				p_IdEstado					: mReqOp.RequisitoOperativo.IdEstado
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			modificarRequisitoOperativo(datos).then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = $translate.instant("UI_OPERACION_TIENE_ADVERTENCIAS");
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = $translate.instant("UI_ERROR_MODIFICAR_REQUISITO_OPERATIVO") + ' : ' + result.P_OUT_ERRORESDETALLE[0].Codigo;
					}
					else{
						bcsComunes.msg("info","info",$translate.instant("UI_REQUISITO_OPERATIVO_MODIFICADO"));
					}
					
					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						},idVentana);
					
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","error",$translate.instant("UI_ERROR_MODIFICAR_REQUISITO_OPERATIVO"));
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
				});
		}
		//creacion
		else{
			var datos = {
				p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
				p_IdUsuario			: $scope.$root.SesionIdUsuario,
				p_IdModulo			: 'REQ',
				p_UsaReglasNegocio	: 'N',
				
				p_IdModeloRequisitoOperativo: mReqOp.RequisitoOperativo.IdModeloRequisitoOperativo,
				p_IdCondicionOperativa		: mReqOp.RequisitoOperativo.IdCondicionOperativa,
				p_IdTipoEtapa				: mReqOp.RequisitoOperativo.IdTipoEtapa,
				p_IdTipoComportamiento		: mReqOp.RequisitoOperativo.IdTipoComportamiento,
				p_IdTipoCanalIngreso		: mReqOp.RequisitoOperativo.IdTipoCanalIngreso,
				p_IdMoneda					: mReqOp.RequisitoOperativo.IdMoneda,
				p_ValorAtributo1			: mReqOp.RequisitoOperativo.ValorAtributo1,
				p_ValorAtributo2			: mReqOp.RequisitoOperativo.ValorAtributo2,
				p_IdEstado					: mReqOp.RequisitoOperativo.IdEstado
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			crearRequisitoOperativo(datos).then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = $translate.instant("UI_OPERACION_TIENE_ADVERTENCIAS");
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = $translate.instant("UI_ERROR_AGREGAR_REQUISITO_OPERATIVO") + ' : ' + result.P_OUT_ERRORESDETALLE[0].Codigo;
					}
					else{
						bcsComunes.msg("info","info",$translate.instant("UI_REQUISITO_OPERATIVO_AGREGADO"));
					}
					
					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						},idVentana);
					
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","error",$translate.instant("UI_ERROR_AGREGAR_REQUISITO_OPERATIVO"));
					bcsComunes.ventanaData(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				});
		}
	};
	
	mReqOp.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
/*********************************************
 * DATASOURCES
 *********************************************/

	
/*********************************************
 * COMBOBOX
 *********************************************/
	
			
/*********************************************
 * GRILLAS
 *********************************************/
	
	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	var crearRequisitoOperativo = function(p_datos){
		var urlCrearRequisitoOperativo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=270";
		var r = $resource("", {}, {'urlCrearRequisitoOperativo': {method:"POST", url:urlCrearRequisitoOperativo}});
		var post = r.urlCrearRequisitoOperativo(p_datos);
		
		return post.$promise;
    };
	
	var modificarRequisitoOperativo = function(p_datos){
		var urlModificarRequisitoOperativo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=271";
		var r = $resource("", {}, {'urlModificarRequisitoOperativo': {method:"POST", url:urlModificarRequisitoOperativo}});
		var post = r.urlModificarRequisitoOperativo(p_datos);
		
		return post.$promise;
    };
    
    
	$scope.$watch('mReqOp.RequisitoOperativo.IdCondicionOperativa',function(newval,oldval) {
		if(mReqOp.RequisitoOperativo.IdCondicionOperativa != undefined && mReqOp.RequisitoOperativo.IdCondicionOperativa != false && mReqOp.CondicionOperativaObj != undefined){
			mReqOp.RequisitoOperativo.IdTipoComportamiento = mReqOp.CondicionOperativaObj.IdTipoComportamientoDefecto;
			
			mReqOp.RequisitoOperativo.CampoRequerido1 = mReqOp.CondicionOperativaObj.RequiereAtributo1 === 'S' ? true : false;
			mReqOp.RequisitoOperativo.CampoRequerido2 = mReqOp.CondicionOperativaObj.RequiereAtributo2 === 'S' ? true : false;
			
			mReqOp.RequisitoOperativo.ValorAtributo1 = mReqOp.CondicionOperativaObj.RequiereAtributo1 === 'S' ? mReqOp.RequisitoOperativo.ValorAtributo1 : undefined;
			mReqOp.RequisitoOperativo.ValorAtributo2 = mReqOp.CondicionOperativaObj.RequiereAtributo2 === 'S' ? mReqOp.RequisitoOperativo.ValorAtributo2 : undefined;
		}
		else{
			mReqOp.RequisitoOperativo.IdTipoComportamiento = undefined;
		}
	});
	
//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
});
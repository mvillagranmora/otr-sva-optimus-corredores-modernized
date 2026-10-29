(function() {
	angular.module('ModuloMesa').controller('ModalItemsManualesPosicionMRFCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mIMPM 		= this;
		mIMPM.idVentana	= bcsComunes.ventanaWinId($scope);
		mIMPM.parametros= bcsComunes.ventanaParams(mIMPM.idVentana);
		mIMPM.domVentana= $("[ventanaid='"+mIMPM.idVentana+"']");

		mIMPM.frmTipoReajuste = '';
		mIMPM.frmCantidadDecimales = 0;
		
		var itemManualSel = _.clone(mIMPM.parametros.ItemManualSel);
		
		mIMPM.win	= {};
		mIMPM.cmbbx	= {};
		mIMPM.fn	= {};
		
		mIMPM.datosItemManual = {};
		mIMPM.datosItemManual.IdEstadoDef = 'VIG';
		mIMPM.datosItemManual.NombreMetodo = $translate.instant('UI_POSICION_INICIAL');		
		mIMPM.IdBotonAceptar = 'ModalItemsManualesPosicionMRF_aceptar' + mIMPM.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(itemManualSel){
			bcsComunes.ventanaTitulo(mIMPM.idVentana, $translate.instant('UI_MODIFICAR_ITEM_MANUAL_POSICION_MRF'));
			mIMPM.datosItemManual.IdMetodo		= itemManualSel.IdMetodo;
			mIMPM.datosItemManual.IdMetodoDef	= itemManualSel.IdMetodo;
			mIMPM.datosItemManual.MontoEvento	= itemManualSel.MontoEvento;
			
			mIMPM.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mIMPM.idVentana, $translate.instant('UI_CREAR_ITEM_MANUAL_POSICION_MRF'));

			if ( mIMPM.parametros.IdMetodo && mIMPM.parametros.IdMetodo > 0 ) {
				mIMPM.datosItemManual.IdMetodo    = mIMPM.parametros.IdMetodo;
				mIMPM.datosItemManual.IdMetodoDef = mIMPM.parametros.IdMetodo;
				mIMPM.frmTipoReajuste = mIMPM.parametros.IdMoneda;
				mIMPM.frmCantidadDecimales = mIMPM.parametros.NroDecimales;
				
			};
			
			
			mIMPM.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mIMPM.cmbbx.PosicionMRF={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			},
			IdMetodo:false,
			configuracion : {
				placeholder: $translate.instant('UI_METODO_POSICION'),
			}
		};
		
		mIMPM.cmbbx.Evento={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'},
				IdTipoEvento : {field: "IdTipoEvento", operator: "eq", value: 'M'}
			},
			configuracion : {
				placeholder: $translate.instant('UI_EVENTO_POSICION'),
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mIMPM.fn.cancelar = function () {
			bcsComunes.ventanaData(mIMPM.idVentana, false);
			bcsComunes.ventanaCerrar(mIMPM.idVentana);
		};
		
		
		mIMPM.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			datosGuardar.p_Fecha        	= moment($scope.SesionFechaSistema).format("YYYY-MM-DDTHH:mm:ss.SSS") + 'Z'; 
			datosGuardar.p_ItemsManuales = [{
				IdPosicion	: 0,			
				IdMetodo	: mIMPM.datosItemManual.IdMetodo,
				IdEvento	: mIMPM.datosItemManual.IdEvento,
				MontoEvento : mIMPM.datosItemManual.MontoEvento
			}];
			
			
			bcsComunes.ventanaOverlay(mIMPM.idVentana,true);
			bcsServiciosMesa.GrabarItemsManualesPosicionMRF(datosGuardar,function (result){
				mIMPM.fn.evaluaRespuestaSP(result, 'UI_ITEM_MANUAL_POSICION_MRF_CREADO');
			}, function(resError){
				bcsComunes.msg("error","UI_ITEMS_MANUALES_POSICION_MRF",$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay(mIMPM.idVentana,false);
			});
		};
		
		
		$scope.$watch('mIMPM.datosItemManual.IdMetodo',function(newval,oldval){
			// mIMPM.cmbbx.PosicionMRF.seleccionado.IdTipoReajuste
			if (newval && mIMPM.datosItemManual.PosicionMRFObj) {
				mIMPM.frmTipoReajuste = mIMPM.datosItemManual.PosicionMRFObj.IdTipoReajuste;
				mIMPM.frmCantidadDecimales = mIMPM.datosItemManual.PosicionMRFObj.CantidadDecimales;
			}
		},true);
		
		mIMPM.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_ITEMS_MANUALES_POSICION_MRF",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_ITEMS_MANUALES_POSICION_MRF",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_ITEMS_MANUALES_POSICION_MRF",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_ITEMS_MANUALES_POSICION_MRF",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mIMPM.idVentana,false);
			bcsComunes.ventanaData(mIMPM.idVentana, true);
			bcsComunes.ventanaCerrar(mIMPM.idVentana);
		};


	});



}());
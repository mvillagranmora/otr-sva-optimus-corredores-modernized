(function() {
	angular.module('ModuloPlataforma').controller('ModalMargenesTasasCtrl',
		function($scope,$translate,bcsComunes,bcsServiciosTasas) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mMT 		= this;
		mMT.idVentana	= bcsComunes.ventanaWinId($scope);
		mMT.parametros	= bcsComunes.ventanaParams(mMT.idVentana);
		mMT.domVentana	= $("[ventanaid='"+mMT.idVentana+"']");
		
		var margenTasaSel= _.clone(mMT.parametros.MargenTasaSel);
		
		mMT.win			= {};
		mMT.cmbbx		= {};
		mMT.fn			= {};
		
		mMT.datosMargen	= {};
		
		mMT.win.TasaDecimal	= (util.bcs_formato(0,'TAS','TASA','CLP','','')).cantidadDecimales;
		mMT.win.MontoDecimal= (util.bcs_formato(0,'TAS','MONTO','CLP','','')).cantidadDecimales;
		
		mMT.IdBotonAceptar = 'ModalMargenesTasas_aceptar' + mMT.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(margenTasaSel){
			bcsComunes.ventanaTitulo(mMT.idVentana, $translate.instant('UI_MODIFICAR_MARGEN_TASA'));
			mMT.datosMargen.IdTipoReajuste		= margenTasaSel.IdMoneda || 0;
			mMT.datosMargen.IdTipoReajusteDef	= margenTasaSel.IdMoneda || 0;
			mMT.datosMargen.MontoDesde			= margenTasaSel.MontoDesde || 0;
			mMT.datosMargen.MontoHasta			= margenTasaSel.MontoHasta || 0;
			mMT.datosMargen.PlazoDesde			= margenTasaSel.PlazoDesde || 0;
			mMT.datosMargen.PlazoHasta			= margenTasaSel.PlazoHasta || 0;
			mMT.datosMargen.Tasa				= margenTasaSel.Tasa || 0;
			mMT.datosMargen.IdEstado			= margenTasaSel.IdEstado || 0;
			mMT.datosMargen.IdEstadoDef			= margenTasaSel.IdEstado || 0;
			mMT.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mMT.idVentana, $translate.instant('UI_AGREGAR_MARGEN_TASA'));
			mMT.datosMargen.MontoDesde	= mMT.datosMargen.MontoHasta = 0;
			mMT.datosMargen.PlazoDesde	= mMT.datosMargen.PlazoHasta = 0;
			mMT.datosMargen.Tasa		= 0;
			mMT.datosMargen.IdEstadoDef	= 'ACT';
			mMT.win.modoEditar = false;
		}
		
		
		mMT.fn.cancelar = function () {
			bcsComunes.ventanaData(mMT.idVentana, false);
			bcsComunes.ventanaCerrar(mMT.idVentana);
		};
		
		
		mMT.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(margenTasaSel && margenTasaSel.IdMargenTasa) { // EDITAR
				if(mMT.datosMargen.Tasa === margenTasaSel.Tasa && mMT.datosMargen.IdEstado === margenTasaSel.IdEstado){
					bcsComunes.msg("warning","UI_MARGEN_TASAS",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mMT.idVentana, false);
					bcsComunes.ventanaCerrar(mMT.idVentana);
					return
				}
				var tasa = typeof mMT.datosMargen.Tasa === 'string' ? Number(mMT.datosMargen.Tasa.replace(',','.')) : mMT.datosMargen.Tasa;
				
				datosGuardar.p_IdMargenTasa = margenTasaSel.IdMargenTasa;
				datosGuardar.p_Tasa			= tasa;
				datosGuardar.p_IdEstado		= mMT.datosMargen.IdEstado;
				
				datosGuardar.p_DetalleLog = [];
				if(tasa !== margenTasaSel.Tasa)
					datosGuardar.p_DetalleLog.push({SubRegistro:'Margen Tasa', CampoRegistro:'Tasa', ValorAntiguo:margenTasaSel.Tasa, ValorNuevo:tasa});
				if(mMT.datosMargen.IdEstado !== margenTasaSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'Margen Tasa', CampoRegistro:'Estado', ValorAntiguo:margenTasaSel.IdEstado, ValorNuevo:mMT.datosMargen.IdEstado});
				
				
				bcsComunes.ventanaOverlay(mMT.idVentana,true);
				bcsServiciosTasas.modificarMargenTasa(datosGuardar,function (result){
					mMT.fn.evaluaRespuestaSP(result, 'UI_MARGEN_TASA_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_MARGEN_TASAS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mMT.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_TipoEntidad	= mMT.parametros.TipoEntidad;
				datosGuardar.p_IdMoneda		= mMT.datosMargen.IdTipoReajuste;
				datosGuardar.p_MontoDesde	= mMT.datosMargen.MontoDesde;
				datosGuardar.p_MontoHasta	= mMT.datosMargen.MontoHasta;
				datosGuardar.p_PlazoDesde	= mMT.datosMargen.PlazoDesde;
				datosGuardar.p_PlazoHasta	= mMT.datosMargen.PlazoHasta;
				datosGuardar.p_Tasa			= mMT.datosMargen.Tasa;
				
				bcsComunes.ventanaOverlay(mMT.idVentana,true);
				bcsServiciosTasas.crearMargenTasa(datosGuardar,function (result){
					mMT.fn.evaluaRespuestaSP(result, 'UI_MARGEN_TASA_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_MARGEN_TASAS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mMT.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mMT.cmbbx.Moneda={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_MONEDA'),
			},
		};
		
		mMT.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MANTENEDORMARGENESTASAS" }
			},
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mMT.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_MARGEN_TASAS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_MARGEN_TASAS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_MARGEN_TASAS",result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
			}
			else bcsComunes.msg("error","UI_MARGEN_TASAS",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			mMT.datosMargen.aceptar = true;
			bcsComunes.ventanaOverlay(mMT.idVentana,false);
			bcsComunes.ventanaData(mMT.idVentana, mMT.datosMargen);
			bcsComunes.ventanaCerrar(mMT.idVentana);
		};
		
	});
}());
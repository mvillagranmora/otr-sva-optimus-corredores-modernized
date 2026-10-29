(function() {
	angular.module('ModuloPlataforma').controller('ModalAgregarValorLimiteDetalleCtrl',
		function ($scope,bcsComunes,servicioPlantillaLimites,$translate) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mavld 			= this;
			mavld.idVentana 	= bcsComunes.ventanaWinId($scope);
			mavld.parametros 	= bcsComunes.ventanaParams(mavld.idVentana);
			mavld.domVentana	= $("[ventanaid='"+mavld.idVentana+"']");
			
			var lineaDetalle	= _.clone(mavld.parametros.lineaDetalle);
			mavld.esEdicion     = lineaDetalle && lineaDetalle.IdLimite ? true : false;
			var IdPlantillaLimite = mavld.parametros.IdPlantillaLimite;
			
			mavld.ds			= {};
			mavld.cmbbx			= {};
			mavld.respuesta 	= {};
			mavld.win			= {};
			
			mavld.win.MontoDecimal	= (util.bcs_formato(0,'ORD','MONTO',$scope.$root.SesionIdMonedaSistema,'','')).cantidadDecimales;
			mavld.win.TasaDecimal	= (util.bcs_formato(0,'ORD','TASA', $scope.$root.SesionIdMonedaSistema,'','')).cantidadDecimales;
			
			
			// logica para los combobox requeridos
			mavld.win.clasifTipoInstr	= true;
			mavld.win.tipoInstrumento	= mavld.parametros.FiltroPlantilla === 'IdClasificacionTipoInstrumento' ? false : true;
			mavld.win.tipoOperacion		= mavld.parametros.FiltroPlantilla === 'IdClasificacionTipoInstrumento' || mavld.parametros.FiltroPlantilla === 'IdTipoInstrumento' ? false : true;
			
			
			/*********************************************
			 * DATASOURCES
			 *********************************************/
			
			
			/*********************************************
			 * COMBOBOX
			 *********************************************/
			
			mavld.cmbbx.ClasificacionTipoInstrumento={
				filtros : {
					IdEstado : {field: "IdEstado",	operator: "eq" , value: "ACT" }
				}
			};
			
			mavld.cmbbx.TipoInstrumento={
				filtros : {
					IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
					IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.SesionBolsaDefectoSistema },
					IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: mavld.win.clasifTipoInstr ? "eq" : "neq", value: '-1' }
				}
			};
			
			mavld.cmbbx.TipoOperacion={
				filtros : {
					IdTipoInstrumento : { field : "IdTipoInstrumento", operator: "eq", value : '-1' }
				}
			};
			
			mavld.cmbbx.TipoCanalIngreso={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
				}
			};
			
			mavld.cmbbx.Estado={
				filtros : {
					NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
				}
			};
			
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			
			mavld.cargaDatosPantalla = function(){
				lineaDetalle = lineaDetalle || {};
				mavld.respuesta.Nombre				= lineaDetalle.Nombre || '';
				mavld.respuesta.IdClasificacionTipoInstrumento	 = lineaDetalle.IdClasificacionTipoInstrumento || 0;
				mavld.respuesta.IdClasificacionTipoInstrumentoDef= lineaDetalle.IdClasificacionTipoInstrumento || 0;
				mavld.respuesta.IdTipoInstrumento	= lineaDetalle.IdTipoInstrumento || 0;
				mavld.respuesta.IdTipoInstrumentoDef= lineaDetalle.IdTipoInstrumento || 0;
				mavld.respuesta.IdTipoOperacion 	= lineaDetalle.IdTipoOperacion || 0;
				mavld.respuesta.IdTipoOperacionDef	= lineaDetalle.IdTipoOperacion || 0;
				mavld.respuesta.IdCanalIngreso		= lineaDetalle.IdCanalIngreso || 0;
				mavld.respuesta.IdCanalIngresoDef 	= lineaDetalle.IdCanalIngreso || 0;
				mavld.respuesta.IdMoneda			= lineaDetalle.IdMoneda || $scope.$root.SesionValoresDefectoSistema.IdMonedaPlantillaLimitesDetalle;
				mavld.respuesta.IdMonedaDef			= lineaDetalle.IdMoneda || $scope.$root.SesionValoresDefectoSistema.IdMonedaPlantillaLimitesDetalle;
				mavld.respuesta.Porcentaje 			= lineaDetalle.Porcentaje || 0;
				mavld.respuesta.MontoTotal 			= lineaDetalle.MontoTotal || 0;
				mavld.respuesta.Estado				= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoPlantillaLimitesDetalle;
				mavld.respuesta.EstadoDef			= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoPlantillaLimitesDetalle;
			};		
	
			mavld.cancelar = function () {
				mavld.respuesta.aceptar = false;
				bcsComunes.ventanaData(mavld.idVentana, mavld.respuesta);
				bcsComunes.ventanaCerrar(mavld.idVentana);
			};

			mavld.aceptar = function () {
				if(lineaDetalle.IdLimite) { // EDITAR
					var datosGuardar = mavld.preparaDatosModificar();
					if(datosGuardar) {
						if(mavld.parametros.modo === 'MP'){  // MP: MantenedorPlantillas
							bcsComunes.ventanaOverlay(mavld.idVentana,true);
							servicioPlantillaLimites.updateDetallePlantillaLimites(datosGuardar,function (result){
								mavld.evaluaRespuestaSP(result, 'UI_PLANTILLA_LIMITES_MODIFICADA');
							}, function(resError){
								bcsComunes.msg("error","LIMITES",$translate.instant("UI_REVISE_LOG"));
								console.log(result);
								bcsComunes.ventanaOverlay(mavld.idVentana,false);
							});
						}
						else if(mavld.parametros.modo === 'PE'){  // PE: PortafolioExtra
							bcsComunes.ventanaOverlay(mavld.idVentana,true);
							servicioPlantillaLimites.modificarLimite(datosGuardar,function (result){
								mavld.evaluaRespuestaSP(result, 'UI_LIMITE_EXTRA_MODIFICADO');
							}, function(resError){
								bcsComunes.msg("error","LIMITES",$translate.instant("UI_REVISE_LOG"));
								console.log(result);
								bcsComunes.ventanaOverlay(mavld.idVentana,false);
							});
						}
						else bcsComunes.msg("warning","LIMITES",$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR"));
					}
					else bcsComunes.msg("warning","LIMITES",$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR"));
				}
				else { // CREAR
					var datosGuardar = mavld.preparaDatosCrear();
					if(datosGuardar) {
						if(mavld.parametros.modo === 'MP'){  // MP: MantenedorPlantillas
							bcsComunes.ventanaOverlay(mavld.idVentana,true);
							servicioPlantillaLimites.saveDetallePlantillaLimites(datosGuardar,function (result){
								mavld.evaluaRespuestaSP(result, 'UI_DETALLE_PLANTILLA_LIMITES_CREADO');
							}, function(resError){
								bcsComunes.msg("error","LIMITES",$translate.instant("UI_REVISE_LOG"));
								console.log(result);
								bcsComunes.ventanaOverlay(mavld.idVentana,false);
							});
						}
						else if(mavld.parametros.modo === 'PE'){  // PE: PortafolioExtra
							bcsComunes.ventanaOverlay(mavld.idVentana,true);
							servicioPlantillaLimites.crearLimite(datosGuardar,function (result){
								mavld.evaluaRespuestaSP(result, 'UI_LIMITE_EXTRA_CREADO');
							}, function(resError){
								bcsComunes.msg("error","LIMITES",$translate.instant("UI_REVISE_LOG"));
								console.log(result);
								bcsComunes.ventanaOverlay(mavld.idVentana,false);
							});
						}
						else bcsComunes.msg("warning","LIMITES",$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR"));
					}
					else bcsComunes.msg("warning","LIMITES",$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR"));
				}
			};
			
			mavld.evaluaRespuestaSP = function(result, msj){
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
					msj = $translate.instant(msj);
				}
				else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
					msj = "LIMITES"+result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion;
				}
				else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
					msj = "LIMITES"+result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion;
				}
				else msj = "LIMITES" + $translate.instant("UI_REVISE_LOG");
				console.log(result);

				var id = bcsComunes.ventanaMsg({
					mensaje: msj,
					conflictos:util.parseServerResponse(result)
				},mavld.idVentana);
				
				var promesa=bcsComunes.ventanaPromesa(id);  
				
				$.when(promesa).then(function(){
					mavld.respuesta.aceptar = true;
					bcsComunes.ventanaOverlay(mavld.idVentana,false);
					bcsComunes.ventanaData(mavld.idVentana, mavld.respuesta);
					bcsComunes.ventanaCerrar(mavld.idVentana);
				});
				
			};
						
			
			mavld.preparaDatosCrear = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 			= "LIM";
				datosGuardar.p_UsaReglasNegocio 	= "N";
				
				if(mavld.parametros.modo === 'PE'){  // PE: PortafolioExtra
					datosGuardar.p_IdTipoEntidad	= 'PORTAFOLIO';
					datosGuardar.p_IdEntidadOrigen	= mavld.parametros.IdPortafolio;
				}
				datosGuardar.p_IdPlantillaLimite	= IdPlantillaLimite;
				datosGuardar.p_IdTipoLimite			= mavld.parametros.modo === 'PE' ? 'LIMPORTPRODUCTO' : '';
				datosGuardar.p_Nombre				= mavld.respuesta.Nombre;
				datosGuardar.p_IdClasificacionTipoInstrumento = mavld.respuesta.IdClasificacionTipoInstrumento;
				datosGuardar.p_IdTipoInstrumento	= mavld.respuesta.IdTipoInstrumento;
				datosGuardar.p_IdTipoOperacion		= mavld.respuesta.IdTipoOperacion;
				datosGuardar.p_IdInstrumento		= 0;
				datosGuardar.p_IdCanalIngreso		= mavld.respuesta.IdCanalIngreso;
				datosGuardar.p_IdMoneda				= mavld.respuesta.IdMoneda;
				datosGuardar.p_Porcentaje			= mavld.respuesta.Porcentaje;
				datosGuardar.p_MontoTotal			= Number(mavld.respuesta.MontoTotal);
				datosGuardar.p_IdEstado				= mavld.respuesta.Estado;
				if(mavld.parametros.modo === 'PE') datosGuardar.p_UI_Limite = 'PLANTILLA';
				
				return datosGuardar;
			};
			
			
			mavld.preparaDatosModificar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 		= mavld.parametros.modo === 'MP' ? 'LIM' : 'POR';
				datosGuardar.p_UsaReglasNegocio	= "N";
				
				if(mavld.parametros.modo === 'PE'){  // PE: PortafolioExtra
					datosGuardar.p_IdTipoEntidad	= 'PORTAFOLIO';
					datosGuardar.p_IdEntidadOrigen	= mavld.parametros.IdPortafolio;
				}
				datosGuardar.p_IdLimite	= lineaDetalle.IdLimite;
				
				datosGuardar.p_RegistroCambio = [];
				if(mavld.respuesta.Nombre !== lineaDetalle.Nombre)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'Nombre',ValorNuevo:mavld.respuesta.Nombre});
				if(mavld.respuesta.IdClasificacionTipoInstrumento !== lineaDetalle.IdClasificacionTipoInstrumento)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdClasificacionTipoInstrumento',ValorNuevo:mavld.respuesta.IdClasificacionTipoInstrumento});
				if(mavld.respuesta.IdTipoInstrumento !== lineaDetalle.IdTipoInstrumento)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoInstrumento',ValorNuevo:mavld.respuesta.IdTipoInstrumento});
				if(mavld.respuesta.IdTipoOperacion !== lineaDetalle.IdTipoOperacion)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoOperacion',ValorNuevo:mavld.respuesta.IdTipoOperacion});
				if(String(mavld.respuesta.IdCanalIngreso) !== lineaDetalle.IdCanalIngreso)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdCanalIngreso',ValorNuevo:mavld.respuesta.IdCanalIngreso});
				if(mavld.respuesta.IdMoneda !== lineaDetalle.IdMoneda)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMoneda',ValorNuevo:mavld.respuesta.IdMoneda});
				if(mavld.respuesta.Porcentaje !== lineaDetalle.Porcentaje)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'Porcentaje',ValorNuevo:mavld.respuesta.Porcentaje});
				if(mavld.respuesta.MontoTotal !== lineaDetalle.MontoTotal)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'MontoTotal',ValorNuevo:mavld.respuesta.MontoTotal});
				if(mavld.respuesta.Estado !== lineaDetalle.IdEstado)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mavld.respuesta.Estado});
				
				if(datosGuardar.p_RegistroCambio.length>0) return datosGuardar;
				else return undefined;
			};
			
			
			$scope.$watch('mavld.respuesta.IdMoneda', function (newValue, oldValue){
				if(newValue !== oldValue)
					mavld.win.MontoDecimal = (util.bcs_formato(0,'ORD','MONTO',mavld.respuesta.IdMoneda,'','')).cantidadDecimales;
			});
			
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			mavld.cargaDatosPantalla();
	});
}());
angular.module('ModuloNormativa').controller('IngresarAnexoCalificacionTributariaCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServiciosNormativa,$timeout){
	var ingresarAnexoCalificacionTributaria = this; 
	ingresarAnexoCalificacionTributaria.winID=bcsComunes.ventanaWinId($scope);
	ingresarAnexoCalificacionTributaria.configuraciones = {};
	ingresarAnexoCalificacionTributaria.configuraciones.date = {
			start: "decade",                          
            depth: "decade",                           
            format: "yyyy"	
	};
	ingresarAnexoCalificacionTributaria.datosAntes = {};
	ingresarAnexoCalificacionTributaria.datosDespues = {};
	
		    
	ingresarAnexoCalificacionTributaria.guardarAnexo = function(){
		
		bcsComunes.ventanaOverlay(ingresarAnexoCalificacionTributaria.winID,true);
		
		
		var datos = {
						p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
						p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						p_IdModulo : 'NOR',
						p_SimularOperacion : 'N',
						p_UsaReglasNegocio : 'S',
						p_Anexo	   :	[{
							IdInterno : 0,
							IdClasificacionTipoInstrumento : ingresarAnexoCalificacionTributaria.datosDespues.IdClasificacionTipoInstrumento,
							IdInstrumento	:	ingresarAnexoCalificacionTributaria.filtros.instrumento.IdInstrumento,
							ValorHistorico  :	ingresarAnexoCalificacionTributaria.datosDespues.ValorHistorico,
							FechaPago		:	util.dateSPdate(ingresarAnexoCalificacionTributaria.datosDespues.FechaPago),
							IdEventoCapital	:	ingresarAnexoCalificacionTributaria.datosDespues.IdEventoCapital,
							Descripcion		:	ingresarAnexoCalificacionTributaria.datosAntes.Descripcion,
							SecuenciaEvento :	ingresarAnexoCalificacionTributaria.datosDespues.SecuenciaEvento,
							AnioOrigen		:	ingresarAnexoCalificacionTributaria.datosDespues.AnioOrigen,
							TasaCredito		:	ingresarAnexoCalificacionTributaria.datosDespues.TasaCredito,
							PorcentajeDividendo : ingresarAnexoCalificacionTributaria.datosDespues.PorcentajeDividendo,
							IdTipoUtilidades :	ingresarAnexoCalificacionTributaria.datosDespues.IdTipoUtilidades
						}]
					
			}
		bcsServiciosNormativa.AgregarAnexoCalificacionTributaria(datos).$promise.then(function(respuesta){
	     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESO_ANEXO_CALIFICACION_TRIBUTARIA"),conflictos:util.parseServerResponse(respuesta)},ingresarAnexoCalificacionTributaria.winID);
	     		bcsConfiguracionMaestroDeclaraciones.ingresarCerrarVentana(id, ingresarAnexoCalificacionTributaria.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_ANEXO_CALIFICACION_TRIBUTARIA"),conflictos:util.parseServerResponse(respuesta)},ingresarAnexoCalificacionTributaria.winID);
	     	 }
  		 },function(respuesta){
  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_ANEXO_CALIFICACION_TRIBUTARIA"),conflictos:util.parseServerResponse(respuesta)},ingresarAnexoCalificacionTributaria.winID);
  		 });

	}
	 
	 bcsComunes.ventanaTamanoAutomatico(ingresarAnexoCalificacionTributaria.winID,10);
});
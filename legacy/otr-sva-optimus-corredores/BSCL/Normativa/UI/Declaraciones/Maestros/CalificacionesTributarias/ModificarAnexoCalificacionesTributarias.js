angular.module('ModuloNormativa').controller('ModificarAnexoCalificacionTributariaCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServiciosNormativa,$timeout){
	var ingresarAnexoCalificacionTributaria = this; 
	ingresarAnexoCalificacionTributaria.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(ingresarAnexoCalificacionTributaria.winID);
	ingresarAnexoCalificacionTributaria.configuraciones = {};
	ingresarAnexoCalificacionTributaria.configuraciones.date = {
			start: "decade",                          
            depth: "decade",                           
            format: "yyyy"	
	};
	ingresarAnexoCalificacionTributaria.datosAntes = {};
	ingresarAnexoCalificacionTributaria.datosDespues = {};
	ingresarAnexoCalificacionTributaria.parametros = $scope.parametros;
	ingresarAnexoCalificacionTributaria.Edicion = ingresarAnexoCalificacionTributaria.parametros.Edicion || false;
	
	var conf = bcsConfiguracionMaestroDeclaraciones.getDataSourceAnexoCalificacionTributaria();
	
	
	
	ingresarAnexoCalificacionTributaria.buscarAnexoCalificacionTributaria = function(){
		bcsComunes.ventanaOverlay(ingresarAnexoCalificacionTributaria.winID,true);
    	var filter = {field: "IdCalificacionTributaria", operator: "eq", value: $scope.parametros.IdCalificacionTributaria};
		
    	conf.query({filter:filter}).then(function(){
			if (conf.data().length<1)
			{
				console.log("Anexo no existe");
				bcsComunes.ventanaOverlay(ingresarAnexoCalificacionTributaria.winID,false);
				bcsComunes.ventanaCerrar(ingresarAnexoCalificacionTributaria.winID);
				return false;
			}
			else
			{
				var Anexo = conf.data()[0];
				ingresarAnexoCalificacionTributaria.datosAntes = conf.data()[0];
				ingresarAnexoCalificacionTributaria.datosDespues = angular.copy(ingresarAnexoCalificacionTributaria.datosAntes);
				ingresarAnexoCalificacionTributaria.datosDespues.FechaPagoUI = util.formatoDate(ingresarAnexoCalificacionTributaria.datosDespues.FechaPago)
				
				console.log("Anexo si existe");
				bcsComunes.ventanaOverlay(ingresarAnexoCalificacionTributaria.winID,false);
				return true;
			}
		});
    }
	
	if($scope.parametros.IdCalificacionTributaria){
		ingresarAnexoCalificacionTributaria.buscarAnexoCalificacionTributaria();
	}else{
		bcsComunes.ventanaCerrar(ingresarAnexoCalificacionTributaria.winID);
		return false;
	}
	
	    
	ingresarAnexoCalificacionTributaria.guardarAnexo = function(){
		bcsComunes.ventanaOverlay(ingresarAnexoCalificacionTributaria.winID,true);
		
		var datos = {}
		
		if($scope.parametros.IdCalificacionTributaria){
			datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo : 'NOR',
					p_SimularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Anexo	   :	[{
						IdInterno : 0,
						IdCalificacionTributaria : $scope.parametros.IdCalificacionTributaria,
						ValorHistorico  :	ingresarAnexoCalificacionTributaria.datosDespues.ValorHistorico,
						Descripcion		:	ingresarAnexoCalificacionTributaria.datosAntes.Descripcion,
						TasaCredito		:	ingresarAnexoCalificacionTributaria.datosDespues.TasaCredito,
						PorcentajeDividendo : ingresarAnexoCalificacionTributaria.datosDespues.PorcentajeDividendo,
						IdTipoUtilidades :	ingresarAnexoCalificacionTributaria.datosDespues.IdTipoUtilidades,
						IdEstado		:	'ACT'
					}]
					
			}
			bcsServiciosNormativa.ModificarAnexoCalificacionTributaria(datos).$promise.then(function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_ANEXO_CALIFICACION_TRIBUTARIA"),conflictos:util.parseServerResponse(respuesta)},ingresarAnexoCalificacionTributaria.winID);
		     		bcsConfiguracionMaestroDeclaraciones.ingresarCerrarVentana(id, ingresarAnexoCalificacionTributaria.winID);
				}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_ANEXO_CALIFICACION_TRIBUTARIA"),conflictos:util.parseServerResponse(respuesta)},ingresarAnexoCalificacionTributaria.winID);
		     	}
	  		 },function(respuesta){
	  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_ANEXO_CALIFICACION_TRIBUTARIA"),conflictos:util.parseServerResponse(respuesta)},ingresarAnexoCalificacionTributaria.winID);
	  		 });
		}
		
	}
    
	 
	ingresarAnexoCalificacionTributaria.editarAnexo = function(){
		 ingresarAnexoCalificacionTributaria.Edicion = false;
		 bcsComunes.ventanaTamanoAutomatico(ingresarAnexoCalificacionTributaria.winID,0);
	 }
	 
	 
	 bcsComunes.ventanaTamanoAutomatico(ingresarAnexoCalificacionTributaria.winID,0);
});
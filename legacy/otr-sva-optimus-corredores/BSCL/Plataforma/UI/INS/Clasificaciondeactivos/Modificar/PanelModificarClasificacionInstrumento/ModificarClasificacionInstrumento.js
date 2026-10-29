angular.module('ModuloPlataforma').controller('ModificarClasificacionInstrumentoCtrl',function($scope,$translate, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA,bcsServicioPlataforma,$resource){
	$scope.filtros = {};
	$scope.data = {};
	$scope.formData = {};
	
	var pLCP 		= this;
	pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
	pLCP.win		= {};
	pLCP.grilla		= {};
	pLCP.cmbbx		= {};
	pLCP.filtro		= {};
	pLCP.fn			= {};
	
	
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-mercados-todoscreados",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.oConfiguracionClasificacionSubMercado={
			tipo : "combobox-submercados-creados",			
			configuracion : {},
			filtros : {
				IdArbolClasificacionActivo 	: { "field" : "Padre", "operator": "eq", "value": -1}			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	
	$scope.data.oConfiguracionClasificacionInstrumento={
			tipo : "combobox-clasificacion-instrumento",			
			configuracion : {},
			filtros : {
				IdArbolClasificacionActivo 	: { "field" : "Padre", "operator": "eq", "value": -1}			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.data.oConfiguracionClasificacionNuevoMercado={
			tipo : "combobox-mercados-todoscreados",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	
	$scope.data.oConfiguracionClasificacionSubMercadoNuevo={
			tipo : "combobox-submercados-creados",			
			configuracion : {},
			filtros : {
				IdArbolClasificacionActivo 	: { "field" : "Padre", "operator": "eq", "value": -1}			
				},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	
	
    $scope.Modificaclasificacion = function(){
		
		var urlConsultarNivel = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601177";
		var r = $resource("", {}, {'ConsultarNivel': { method: "POST", url: urlConsultarNivel}});
		var padreva =0;
		if($scope.filtros.clasificacionSubMercadoNuevo!=false)
			{
			padreva=$scope.filtros.clasificacionSubMercadoNuevo;
			}
		else {
			padreva=$scope.filtros.clasificacionSubMercado;
		}
		
		var datosConsultarNivel = {
				p_Padre             				  :padreva,
				p_IdArbolClasificacionActivo          :$scope.filtros.clasificacionInstrumento,
				p_Descripcion    					  :$scope.formData.NuevoNombreClas
		};
		
		var post = r.ConsultarNivel(datosConsultarNivel);
		post.$promise.then(function(result){
			if(result.P_OUT_MENSAJES.tipo =="OK")
			{
				var titulo ="UI_DETALLE_TITULO";
				bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
			}
			else if(result.P_OUT_MENSAJES[0].tipo =="ADVERTENCIA")
				{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_ASOCIADOS") + ", " + $translate.instant("UI_DESEA_MODIFICARLO") + "" ,pLCP.idVentana);
				t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						 var urlModificar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601176";
							var r = $resource("", {}, {'Modificar': { method: "POST", url: urlModificar}});
							
							var datosModificar = {			
									p_Padre             				  :padreva,
									p_IdArbolClasificacionActivo          :$scope.filtros.clasificacionInstrumento,
									p_Descripcion    					  :$scope.formData.NuevoNombreClas				
							};
							
							// serultado del mod
							var post = r.Modificar(datosModificar);
							post.$promise.then(function(result){
								if(result.P_OUT_MENSAJES[0].tipo =="OK")
								{
									var titulo ="UI_DETALLE_TITULO";
									bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								}
								else
									{
									var titulo ="UI_DETALLE_TITULO";
									bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
									}
								bcsComunes.setConflictos(util.parseServerResponse(result));
							},function(result){
								bcsComunes.ventanaOverlay($scope.ventanaId,false);
								$scope.etapa='';
								bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
									conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								bcsComunes.setConflictos(util.parseServerResponse(result));
								//console.log(JSON.stringify(result));
							});
												
				       };
				});
				}
		});		
		};

	
	
	
});
angular.module('ModuloPlataforma').controller('ModificarMercadoCtrl',function($scope,$translate, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA,bcsServicioPlataforma,$resource){

	$scope.filtros = {};
	$scope.data = {};
	$scope.formData = {};
	
	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/
	var pLCP 		= this;
	pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
	pLCP.win		= {};
	pLCP.grilla		= {};
	pLCP.cmbbx		= {};
	pLCP.filtro		= {};
	pLCP.fn			= {};
	
	
	
	$scope.data.oConfiguracionMercados={
			tipo : "combobox-mercados-todoscreados",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
	
	$scope.ModificaMercado = function(){
				
		var urlConsultarNivel = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601177";
		var r = $resource("", {}, {'ConsultarNivel': { method: "POST", url: urlConsultarNivel}});
		
		var datosConsultarNivel = {			
				p_IdMercado             :$scope.filtros.clasificacionTipoInstrumento,
                p_NombreNuevoMercado    :$scope.formData.NuevoNombreMercado
                
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
									p_IdArbolClasificacionActivo   :$scope.filtros.clasificacionTipoInstrumento,
									p_Descripcion    				:$scope.formData.NuevoNombreMercado				
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
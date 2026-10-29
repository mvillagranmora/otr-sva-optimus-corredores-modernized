angular.module('ModuloCustodia').controller('ConsultarJuntaAccionistasCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsBlotterConsultaJunta, bcsServiciosJuntaAccionista){
	
	var filtros = [];
	$scope.filtros = {};
	$scope.data = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.dataTemplate = {};
	$scope.data.filtrosEmisor = {};

	$scope.data.oConfiguracionTipoJuntaAccionista={
			tipo : "combobox-tipo-junta-accionista",
			configuracion : {},
			filtros : { 	
				NombreEntidad : {"field" : "Entidad", "operator" : "eq", "value" : "TIPOJUNTA"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
	
	$scope.data.oConfiguracionEstadoJunta={
			tipo : "combobox-estado-custodia",
			configuracion : {},
			filtros : { 	
				NombreEntidad : {"field" : "NombreEntidad", "operator" : "eq", "value" : "ESTADO.JUNTAACCIONISTA"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.data.oConfiguracionEstadoRespaldoCustodia={
			tipo : "combobox-estado-custodia",
			configuracion : {},
			filtros : { 	
				NombreEntidad : {"field" : "NombreEntidad", "operator" : "eq", "value" : "RESPALDO.CUSTODIA"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
	
	$scope.eliminarJuntaAccionista = function(juntas){
		if(!juntas || juntas.length == 0) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ELIMINAR_JUNTA_ACCIONISTA'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var datosIngresoEventoJunta = {
						 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
						 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo : 'CUS',
						 p_JuntaAccionista :[]
				}
				$.each(juntas,function(key,junta){
					datosIngresoEventoJunta.p_JuntaAccionista.push({IdInterno : junta.IdEventoJuntaAccionista, IdEventoJuntaAccionista:junta.IdEventoJuntaAccionista});
		  		});
				
				bcsServiciosJuntaAccionista.EliminarEventoJuntaAccionista(datosIngresoEventoJunta).$promise.then(function(respuesta){
			     	var operaciones = [];
					$.each(respuesta.P_OUT_RESULTADO,function(key,value) {
						operaciones.push({id: value.IdEventoJuntaAccionista , resultado: $translate.instant(value.Codigo), mensaje: ''});
					});

		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					});
		     		$scope.buscarJuntaAccionistas();
		     		$scope.juntaAccionistasSeleccionado = undefined;
		     		bcsComunes.ventanaOverlay($scope.winID,false);
				},function(respuesta){
		  			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ELIMINAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		  			
				});
				
			}else{
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
			
		});
	};
	
	$scope.verRespaldoCustodia = function(datos)
	{
		if (datos.length < 1 || datos[0].IdEstadoCustodia != 'PRO') return;
		bcsComunes.ventanaAbrir("ConsultarCustodiaCierreJuntaAccionista", {evento: datos[0]} );
	};
	
	$scope.ingresarJuntaAccionista = function()
	{
		var id = bcsComunes.ventanaAbrir("IngresarJuntaAccionistas", {} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	$scope.buscarJuntaAccionistas();
        	$scope.juntaAccionistasSeleccionado = undefined;
        }); 
	};
	
	$scope.modificarJuntaAccionista = function(datos)
	{      
		var id = bcsComunes.ventanaAbrir("IngresarJuntaAccionistas", {evento: datos[0]} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
        	$scope.buscarJuntaAccionistas();
        	$scope.juntaAccionistasSeleccionado = undefined;
        });
	};
	
	$scope.generarNominaJuntaAccionista = function(datos) {
		if(datos) {
			bcsComunes.ventanaOverlay($scope.winID,true);
			var mensaje = 'UI_GENERAR_NOMINA_CIERRE_JUNTA_ACCIONISTA';
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					var dataGenerarNomina = {
						p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
						p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						p_IdModulo : 'CUS',
						p_JuntaAccionista : [{
							IdInterno : 0,
							IdEventoJuntaAccionista : datos[0].IdEventoJuntaAccionista
						}]
					}
					bcsServiciosJuntaAccionista.CrearClienteNominaJuntaAccionista(dataGenerarNomina).$promise.then(function(respuesta){
						if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaAbrir("MantenedorNominasJuntaAccionistas", {evento: datos[0]} );
						}else{
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_NOMINA_CUSTODIA_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						}
						$scope.buscarJuntaAccionistas();
			        	$scope.juntaAccionistasSeleccionado = undefined;
			     		bcsComunes.ventanaOverlay($scope.winID,false);
					},function(respuesta){
			  			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_NOMINA_CUSTODIA_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  			 bcsComunes.ventanaOverlay($scope.winID,false);
					});
				}else{
					bcsComunes.ventanaOverlay($scope.winID,false);
				}
			})
		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBES_SELECCIONAR_UNA_JUNTA_ACCIONISTA"));
			return;
		}
	};
	
	$scope.generarRespaldoCustodia = function(datos) {
		if(datos) {
			bcsComunes.ventanaOverlay($scope.winID,true);
			var mensaje = 'UI_GENERAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA';
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					var datosProcesarClientesConDerecho = {
							p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
							p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
							p_IdModulo 	: 'CUS',
							p_IdPais    : $scope.SesionIdPaisSistema,
							p_JuntaAccionistaProcesar : [{
								IdEventoJuntaAccionista : datos[0].IdEventoJuntaAccionista
							}]
					}
					bcsServiciosJuntaAccionista.ProcesarClientesConDerechoJuntaAccionista(datosProcesarClientesConDerecho).$promise.then(function(respuesta){
						if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaAbrir("ConsultarCustodiaCierreJuntaAccionista", {evento: datos[0]} );
						}else{
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						}
						$scope.buscarJuntaAccionistas();
			        	$scope.juntaAccionistasSeleccionado = undefined;
			     		bcsComunes.ventanaOverlay($scope.winID,false);
					},function(respuesta){
			  			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  			 bcsComunes.ventanaOverlay($scope.winID,false);
					});
				}else{
					bcsComunes.ventanaOverlay($scope.winID,false);
				}
			})
		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBES_SELECCIONAR_UNA_JUNTA_ACCIONISTA"));
			return;
		}
	}
	
	$scope.limpiarRespaldoCustodia = function(datos) {
		if(datos) {
			bcsComunes.ventanaOverlay($scope.winID,true);
			var mensaje = 'UI_LIMPIAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA';
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					var datosProcesarClientesConDerecho = {
							p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
							p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
							p_IdModulo 	: 'CUS',
							p_JuntaAccionista : [{
								IdInterno : 0,
								IdEventoJuntaAccionista : datos[0].IdEventoJuntaAccionista
							}]
					}
					bcsServiciosJuntaAccionista.EliminarProcesoClientesConDerechoJuntaAccionista(datosProcesarClientesConDerecho).$promise.then(function(respuesta){
						if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_LIMPIAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA_EXITOSO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						}else{
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_LIMPIAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						}
						$scope.buscarJuntaAccionistas();
			        	$scope.juntaAccionistasSeleccionado = undefined;
			     		bcsComunes.ventanaOverlay($scope.winID,false);
					},function(respuesta){
			  			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_LIMPIAR_RESPALDO_CUSTODIA_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  			
					});
				}else{
					bcsComunes.ventanaOverlay($scope.winID,false);
				}
			})
		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBES_SELECCIONAR_UNA_JUNTA_ACCIONISTA"));
			return;
		}
	}
	
	$scope.verNominaJuntaAccionista = function(datos){
		if(datos) {
			bcsComunes.ventanaAbrir("MantenedorNominasJuntaAccionistas", {evento: datos[0]} );
		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBES_SELECCIONAR_UNA_JUNTA_ACCIONISTA"));
			return;
		}
	}
	
	var gridDefault = {
			columns: [
			           bcsBlotterConsultaJunta.getConfiguracionColumna("IdEmisor",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("FechaCierre",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("FechaRealizacion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreEmisor",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("TipoJunta",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Lugar",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("HoraRealizacion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Materias",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreEstadoJunta",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreEstadoCustodia",{ groupable: true})
		    ],
			userSettings:true,
			sortable: true,
      		reorderable: true,
      		filterable: true,
			groupable: true,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: false,
      		scrollable: true,
      		height:400,
	
			dataSource: bcsBlotterConsultaJunta.dataSourceConsultaJunta,
		};
	
	$scope.data.confBasicaJuntaAccionistas = gridDefault;

	$scope.buscarJuntaAccionistas = function(){
		var filtros = [];
		filtros.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa});
		if($scope.filtros.tipoJuntaAccionista){	
			filtros.push({ field: "IdTipoJuntaAccionista", operator: "eq", value: $scope.filtros.tipoJuntaAccionista});
		}
		if($scope.filtros.estadoJunta){
			filtros.push({ field: "IdEstadoJunta", operator: "eq", value: $scope.filtros.estadoJunta});
		}
		if($scope.filtros.estadoRespaldoCustodia){
			filtros.push({ field: "IdEstadoCustodia", operator: "eq", value: $scope.filtros.estadoRespaldoCustodia});
		}
		if($scope.filtros.IdEmisor){
			filtros.push({ field: "IdEmisor", operator: "eq", value: $scope.filtros.IdEmisor});
		}
		if($scope.filtros.FecInicio){
			filtros.push({ field: "FechaRealizacion", operator: "gte", value: $scope.filtros.FecInicio});
		}
		if($scope.filtros.FecTermino){
			filtros.push({ field: "FechaRealizacion", operator: "lte", value: $scope.filtros.FecTermino});
		}
		$scope.apiGridConsultarJuntaAccionistas.dataSource.filters(filtros);
		$scope.apiGridConsultarJuntaAccionistas.refresh();
	}
	
	$scope.limpiarFiltros = function(){
		$scope.filtros.tipoJuntaAccionista = undefined;
		$scope.filtros.estadoJunta = undefined;
		$scope.filtros.estadoRespaldoCustodia = undefined;
		$scope.filtros.IdEmisor = undefined;
		$scope.filtros.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.FecTermino	 = util.dateInicioDia($scope.SesionFechaSistema);
	}
	
	$scope.limpiarFiltros();
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,30);
    }
	
	$scope.cerrarJuntaAccionistas = function(juntas){
		if(!juntas || juntas.length == 0) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_CERRAR_JUNTA_ACCIONISTA'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var datosIngresoEventoJunta = {
						 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
						 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo : 'CUS',
						 p_JuntaAccionista :[]
				}
				$.each(juntas,function(key,junta){
					datosIngresoEventoJunta.p_JuntaAccionista.push({IdInterno : junta.IdEventoJuntaAccionista, IdEventoJuntaAccionista:junta.IdEventoJuntaAccionista});
		  		});
				
				bcsServiciosJuntaAccionista.CerrarEventoJuntaAccionista(datosIngresoEventoJunta).$promise.then(function(respuesta){
			     	var operaciones = [];
					$.each(respuesta.P_OUT_RESULTADO,function(key,value) {
						operaciones.push({id: value.IdEventoJuntaAccionista , resultado: $translate.instant(value.Codigo), mensaje: ''});
					});

		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					});
		     		$scope.juntaAccionistasSeleccionado = undefined;
		     		$scope.buscarJuntaAccionistas();
		     		bcsComunes.ventanaOverlay($scope.winID,false);
				},function(respuesta){
		  			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERRAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		  			
				});
				
			}else{
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
			
		});
	}
	
	$scope.data.confBasicaJuntaAccionistas.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.verInstructivo = function(evento){
		bcsBlotterConsultaJunta.verInstructivo(evento);
	}
});
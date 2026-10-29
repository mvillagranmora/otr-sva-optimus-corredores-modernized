angular.module('ModuloCustodia').controller('MantenedorNominasJuntaAccionistaCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsBlotterConsultaJunta, bcsServiciosJuntaAccionista){
	
	$scope.filtros = {};
	$scope.datos = {}
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	var filtros = [];
	
	$scope.filtros.IdEventoJuntaAccionista = $scope.parametros.evento.IdEventoJuntaAccionista;
	
	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
	var setearFiltros = function(){
		filtros = [];
		filtros.push({ field: "IdEventoJuntaAccionista", operator: "eq", value: $scope.filtros.IdEventoJuntaAccionista});
		if($scope.oSocioNegocio.seleccionado){
			filtros.push({ field: "IdCliente", operator:"eq", value:$scope.oSocioNegocio.seleccionado.IdCliente})
		}
	}
	
	var limpiarFiltros = function(){
		$scope.oSocioNegocio.idActual = false;
	}
	
	setearFiltros();
	
	var ds = bcsBlotterConsultaJunta.dataSourceConsultarNominasJuntaAccionista;
	ds.definition.filter = filtros;
	
	var grid = {
			columns: [
			           bcsBlotterConsultaJunta.getConfiguracionColumna("NombreCliente",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Direccion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Correo",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreEstadoNotificacion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreEstadoRespuesta",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("FechaIngreso",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreUsuario",{ groupable: true})
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
    		autoBind: true,
    		scrollable: true,
    		height:400,
	
			dataSource: ds,
		}
	
	$scope.defaultOptions = grid;
	
	limpiarFiltros();
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,30);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.buscarCustodiaCierreJuntaAccionista = function(){
		setearFiltros();
		$scope.apiMantenedorNominasJuntaAccionista.dataSource.filters(filtros);
		$scope.apiMantenedorNominasJuntaAccionista.refresh();
	}
	
	$scope.notificarCliente = function(clientes) {
		if(clientes) {
			bcsComunes.ventanaOverlay($scope.winID,true);
			var mensaje = 'UI_NOTIFICAR_NOMINA_CLIENTE_JUNTA_ACCIONISTA';
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					var notificacion = {
						p_IdEmpresa   		: $scope.SesionIdEmpresa,
						p_IdUsuario   		: $scope.SesionIdUsuario,
						p_IdModulo 	  		: 'CUS',
						p_TablaNotificacion : [] 
					}
					$.each(clientes,function(key,sol){
						notificacion.p_TablaNotificacion.push({IdClienteNominaJuntaAccionista : sol.IdClienteNominaJuntaAccionista,
			  												   NombreCliente : sol.NombreCliente,
			  												   CiudadCliente : sol.CiudadCliente,
			  												   EnvioCorrespondencia : sol.Direccion,
			  												   FechaRealizacion : util.dateSPdate(sol.FechaRealizacion),
			  												   HoraRealizacion : sol.HoraRealizacion,
			  												   Materias : sol.Materias,
			  												   FechaCierre : util.dateSPdate(sol.FechaCierre),
			  												   IdDireccionElectronica : sol.IdDireccionElectronicaPrincipal,
			  												   Lugar : sol.Lugar,
			  												   IdPersona : sol.IdPersona
			  												  });
			  		});
					bcsServiciosJuntaAccionista.NotificarJuntaAccionistas(notificacion).$promise.then(function(respuesta){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OK_GENERAR_EVENTO_NOTIFICACION_ACCIONISTAS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						bcsComunes.ventanaOverlay($scope.winID,false);
					},function(respuesta){
			  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_EVENTO_NOTIFICACION_ACCIONISTAS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  			bcsComunes.ventanaOverlay($scope.winID,false);
					});
					
				}else{
					bcsComunes.ventanaOverlay($scope.winID,false);
				}
			})
		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBES_SELECCIONAR_UNA_NOMINA_JUNTA_ACCIONISTA"));
			return;
		}
	};
	
	$scope.notificarNomina = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var mensaje = 'UI_NOTIFICAR_NOMINA_JUNTA_ACCIONISTA';
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var notificacion = {
						p_IdEmpresa   			  : $scope.SesionIdEmpresa,
						p_IdUsuario   			  : $scope.SesionIdUsuario,
						p_IdModulo 	  			  : 'CUS',
						p_IdEventoJuntaAccionista : $scope.filtros.IdEventoJuntaAccionista
					}
					bcsServiciosJuntaAccionista.ConsultarNominaAccionistasParaNotificar(notificacion).$promise.then(function(respuesta){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OK_GENERAR_EVENTO_NOTIFICACION_ACCIONISTAS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						bcsComunes.ventanaOverlay($scope.winID,false);
					},function(respuesta){
			  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_EVENTO_NOTIFICACION_ACCIONISTAS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  			bcsComunes.ventanaOverlay($scope.winID,false);
					});
			}else{
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
		})
		
	};

});
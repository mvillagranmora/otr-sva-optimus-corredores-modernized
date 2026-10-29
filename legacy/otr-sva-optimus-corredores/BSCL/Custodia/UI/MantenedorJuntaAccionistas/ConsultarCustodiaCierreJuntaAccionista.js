angular.module('ModuloCustodia').controller('ConsultarCustodiaCierreJuntaAccionistaCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsBlotterConsultaJunta, bcsServiciosJuntaAccionista){
	
	$scope.filtros = {};
	$scope.datos = {}
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	var filtros = [];
	
	$scope.filtros.IdEmisor = $scope.parametros.evento.IdEmisor;
	$scope.filtros.IdEventoJuntaAccionista = $scope.parametros.evento.IdEventoJuntaAccionista;
	$scope.evento = $scope.parametros.evento;
	
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
	$scope.oConfiguracionTipoJuntaAccionista={
			tipo : "combobox-tipo-junta-accionista",
			configuracion : {},
			filtros : { 	
				NombreEntidad : {"field" : "Entidad", "operator" : "eq", "value" : "TIPOINSTRUCCION"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
	
	var setearFiltros = function(){
		filtros = [];
		filtros.push({ field: "IdEmisor", operator: "eq", value: $scope.filtros.IdEmisor});
		filtros.push({ field: "IdEventoJuntaAccionista", operator: "eq", value: $scope.filtros.IdEventoJuntaAccionista});
		filtros.push({ field: "IdEstado", operator: "eq", value: 'ACT'})
		if($scope.filtros.IndicadorMandatoRepresentacion){
			filtros.push({ field: "IndicadorMandatoRepresentacion", operator:"eq", value:$scope.filtros.IndicadorMandatoRepresentacion})
		}
		if($scope.filtros.IdTipoInstruccion){
			filtros.push({ field: "IdTipoInstruccion", operator:"eq", value:$scope.filtros.IdTipoInstruccion})
		}
		if($scope.filtros.instrumento){
			filtros.push({ field: "IdInstrumento", operator:"eq", value:$scope.filtros.instrumento.IdInstrumento})
		}
		if($scope.oSocioNegocio.seleccionado){
			filtros.push({ field: "IdCliente", operator:"eq", value:$scope.oSocioNegocio.seleccionado.IdCliente})
		}
	}
	
	var limpiarFiltros = function(){
		$scope.filtros.IdTipoInstruccion = '0';
		$scope.filtros.IndicadorMandatoRepresentacion = '0';
		$scope.filtros.buscarInstrumento = '-1';
		$scope.oSocioNegocio.idActual = false;
	}
	
	setearFiltros();
	
	var ds = bcsBlotterConsultaJunta.dataSourceConsultarCustodiaCierreJuntaAccionista;
	ds.definition.filter = filtros;
	
	var grid = {
			columns: [
			           bcsBlotterConsultaJunta.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreCliente",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Nemotecnico",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("IndicadorMandatoRepresentacion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("CantidadDerecho",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("CantidadDerechoCalculado",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("TipoInstruccion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Instruccion",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreEstado",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("Origen",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("FechaCalculo",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("NombreUsuario",{ groupable: true})
			          ,bcsBlotterConsultaJunta.getConfiguracionColumna("FechaModificacionRegistro",{ groupable: true})
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
		$scope.apiConsultaCustodiaCierreJuntaAccionista.dataSource.filters(filtros);
		$scope.apiConsultaCustodiaCierreJuntaAccionista.refresh();
	}
	
	$scope.ingresarClienteCustodia = function(){
		var id = bcsComunes.ventanaAbrir("MantenedorClienteCustodiaCierreJuntaAccionista", {evento: $scope.parametros.evento} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	$scope.buscarCustodiaCierreJuntaAccionista();
        	$scope.datos.kNgModel = undefined;
        }); 
	}
	
	$scope.modificarClienteCustodia = function(datos){
		if(datos.lenght < 1) return;
		var id = bcsComunes.ventanaAbrir("MantenedorClienteCustodiaCierreJuntaAccionista", {evento: $scope.parametros.evento, clienteCustodia: datos[0], esEdicion: true} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	$scope.buscarCustodiaCierreJuntaAccionista();
        	$scope.datos.kNgModel = undefined;
        }); 
	}
	
	$scope.eliminarClienteCustodia = function(clientes){
		if(clientes.lenght < 1) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var mensaje = 'UI_ELIMINAR_CLIENTE_CUSTODIA_CIERRE_JUNTA_ACCIONISTA';
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'CUS',
		  			p_UsaReglasNegocio : 'N',
		  			p_ClienteDerecho : [],
	    		};
				
		    	$.each(clientes,function(key,cliente){
		  			datos.p_ClienteDerecho.push({IdInterno: cliente.IdClienteConDerechoJuntaAccionista, IdClienteConDerechoJuntaAccionista : cliente.IdClienteConDerechoJuntaAccionista, IdEventoJuntaAccionista: cliente.IdEventoJuntaAccionista});
		  		});
		    	var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
					tituloVentana : $translate.instant("UI_INFORMACION"), 
					nombreEntidad : $translate.instant("UI_CLIENTE"), 
					operaciones : [{'id':clientes[0].NombreCliente, resultado: $translate.instant('UI_EXITO'), mensaje: ''}]   
				});
		    	
		    	bcsServiciosJuntaAccionista.EliminarClienteConDerechoJuntaAccionista(datos).$promise.then(function(result){
		    		var operaciones = [];
					$.each(result.P_OUT_RESULTADO,function(key,value) {
						operaciones.push({id: value.IdClienteConDerechoJuntaAccionista , resultado: $translate.instant(value.Codigo), mensaje: ''});
					});
		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					});
					
		     		$scope.buscarCustodiaCierreJuntaAccionista();
		        	$scope.datos.kNgModel = undefined;
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    	},function(result){
		    		console.log(result);
		    		$scope.buscarCustodiaCierreJuntaAccionista();
		        	$scope.datos.kNgModel = undefined;
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    	});
			}
			else{
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
		});
	}
	
	$scope.verInstructivo = function(){
		bcsBlotterConsultaJunta.verInstructivo([$scope.parametros.evento]);
	}
	
	
});
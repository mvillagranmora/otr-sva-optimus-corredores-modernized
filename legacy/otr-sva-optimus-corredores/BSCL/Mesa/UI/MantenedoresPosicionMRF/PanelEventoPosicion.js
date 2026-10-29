(function() {
	angular.module('ModuloMesa').controller('PanelEventoPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pEvPos			= this;
		pEvPos.idVentana	= bcsComunes.ventanaWinId($scope);
		pEvPos.win			= {};
		pEvPos.grilla		= {};
		pEvPos.cmbbx		= {};
		pEvPos.filtro		= {};
		pEvPos.fn			= {};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pEvPos.fn.agregar = function() {
			pEvPos.fn.abrirModal(undefined, 'Agregar');
		};
		
		pEvPos.fn.modificar = function(eventoSel) {
			pEvPos.fn.abrirModal(eventoSel, 'Modificar');
		};

		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pEvPos.grilla.EventosPosicion = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=472",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdEvento		: { type: "number"},
								NombreOperacion	: { type: "string"},
								TipoEvento		: { type: "string"},
								IndicadorAccion	: { type: "string"},
								Estado			: { type: "string"},
							}
						}
					}
				}
			}),
			columns: [
			    {field: "IdEvento",			title: $translate.instant("UI_EVENTO"),		width: "70px"	},
			    {field: "NombreOperacion",	title: $translate.instant("UI_OPERACION"),	width: "130px"	},
			    {field: "TipoEvento",		title: $translate.instant("UI_TIPO_EVENTO_POSICION"),width: "100px"	},
				{field: "IndicadorAccion",	title: $translate.instant("UI_INDICADOR_ACCION"),width: "100px"	},
				{field: "Estado",			title: $translate.instant("UI_ESTADO"),		width: "100px"	}
			],
			pageable	: true,
			autoBind	: false,
			height		: 480,
		    selectable 	: "row",
		    userSettings: true,
			navigatable	: true,
			excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pEvPos.cmbbx.TipoEvento={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "TIPOEVENTO" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_TIPO_EVENTO_POSICION'),
			},
		};
		
		pEvPos.cmbbx.IndicadorAccion={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "INDICADORACCION" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_INDICADOR_ACCION'),
			},
		};
		
		pEvPos.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pEvPos.fn.buscar = function(){
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(pEvPos.filtro.IdTipoEvento){
				niv1filters.push({ field: "IdTipoEvento", operator: "eq", value: pEvPos.filtro.IdTipoEvento });
			}
			if(pEvPos.filtro.IndicadorAccion){
				niv1filters.push({ field: "IndicadorAccion", operator: "eq", value: pEvPos.filtro.IndicadorAccion });
			}
			if(pEvPos.filtro.IdEstado){
				niv1filters.push({ field: "IdEstado", operator: "eq", value: pEvPos.filtro.IdEstado });
			}
	    	
	    	if (niv1filters.length>0) pEvPos.grilla.EventosPosicion.dataSource.filters(objfilters);
	    	else pEvPos.grilla.EventosPosicion.dataSource.filters(new Array());
	    	
	    	pEvPos.grilla.eventoPosicionSel = undefined;
		};
		
		
		pEvPos.fn.limpiar = function(){
			pEvPos.filtro.IdTipoEvento		= 0;
			pEvPos.filtro.IndicadorAccion	= 0;
			pEvPos.filtro.IdEstado			= 0;
			pEvPos.grilla.EventosPosicion.dataSource.removeAll();
		};
		
		
		pEvPos.fn.abrirModal = function(eventoSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalEventoPosicion",{ EventoPosicionSel: eventoSel, AccionBoton : accion },false,false,pEvPos.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		pEvPos.grilla.eventoPosicionSel = undefined;
	        		pEvPos.fn.buscar();
				}
	    	}); 
		};
		
	});	
}());
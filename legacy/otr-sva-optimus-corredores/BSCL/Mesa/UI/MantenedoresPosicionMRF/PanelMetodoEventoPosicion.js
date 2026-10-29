(function() {
	angular.module('ModuloMesa').controller('PanelMetodoEventoPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pMEP		= this;
		pMEP.idVentana	= bcsComunes.ventanaWinId($scope);
		pMEP.win		= {};
		pMEP.grilla		= {};
		pMEP.cmbbx		= {};
		pMEP.filtro		= {};
		pMEP.fn			= {};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pMEP.fn.agregar = function() {
			pMEP.fn.abrirModal(undefined, 'Agregar');
		};
		
		pMEP.fn.modificar = function(metEveSel) {
			pMEP.fn.abrirModal(metEveSel, 'Modificar');
		};

		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pMEP.grilla.MetodosEventosPosicion = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=473",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdMatriz	: { type: "number"},
								NombreMetodo: { type: "string"},
								NombreEvento: { type: "string"},
								Agrupacion	: { type: "string"},
								Estado		: { type: "string"},
								OrdenItem	: { type: "number"}
							}
						}
					},
					sort: [
					    //{ field: "IdMatriz",	dir: "asc" },
					    { field: "IdAgrupacion",dir: "asc" },
					    { field: "OrdenItem",	dir: "asc" }
					]
				}
			}),
			columns: [
			    {field: "IdMatriz",		title: $translate.instant("UI_METODO_EVENTO_POSICION"),	width: "80px"	},
			    {field: "NombreMetodo",	title: $translate.instant("UI_NOMBRE_METODO"),	width: "120px"	},
			    {field: "NombreEvento",	title: $translate.instant("UI_NOMBRE_EVENTO"),	width: "120px"	},
				{field: "Agrupacion",	title: $translate.instant("UI_AGRUPACION"),		width: "100px"	},
				{field: "Estado",		title: $translate.instant("UI_ESTADO"),			width: "80px"	},
				{field: "OrdenItem",	title: $translate.instant("UI_ORDEN_ITEM"),		width: "80px"	}
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
		
		pMEP.cmbbx.PosicionMRF={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			}
		};
		
		pMEP.cmbbx.Evento={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			}
		};
		
		pMEP.cmbbx.Agrupacion={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "Grupo_PosMRF" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_AGRUPACION'),
			},
		};
		
		pMEP.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pMEP.fn.buscar = function(){
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(pMEP.filtro.IdMetodo){
				niv1filters.push({ field: "IdMetodo", operator: "eq", value: pMEP.filtro.IdMetodo });
			}
			if(pMEP.filtro.IdEvento){
				niv1filters.push({ field: "IdEvento", operator: "eq", value: pMEP.filtro.IdEvento });
			}
			if(pMEP.filtro.IdAgrupacion){
				niv1filters.push({ field: "IdAgrupacion", operator: "eq", value: pMEP.filtro.IdAgrupacion });
			}
			if(pMEP.filtro.IdEstado){
				niv1filters.push({ field: "IdEstado", operator: "eq", value: pMEP.filtro.IdEstado });
			}
	    	
	    	if (niv1filters.length>0) pMEP.grilla.MetodosEventosPosicion.dataSource.filters(objfilters);
	    	else pMEP.grilla.MetodosEventosPosicion.dataSource.filters(new Array());
	    	
	    	pMEP.grilla.metodoEventoPosicionSel = undefined;
		};
		
		
		pMEP.fn.limpiar = function(){
			pMEP.filtro.IdMetodo	= 0;
			pMEP.filtro.IdEvento	= 0;
			pMEP.filtro.IdAgrupacion= 0;
			pMEP.filtro.IdEstado	= 0;
			pMEP.grilla.MetodosEventosPosicion.dataSource.removeAll();
		};
		
		
		pMEP.fn.abrirModal = function(metEveSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalMetodoEventoPosicion",{ MetodoEventoPosicionSel: metEveSel, AccionBoton : accion },false,false,pMEP.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		pMEP.grilla.metodoEventoPosicionSel = undefined;
	        		pMEP.fn.buscar();
				}
	    	}); 
		};
		
	});	
}());
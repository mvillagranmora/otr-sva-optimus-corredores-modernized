(function() {
	angular.module('ModuloMesa').controller('PanelTipoOperacionPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pTOP		= this;
		pTOP.idVentana	= bcsComunes.ventanaWinId($scope);
		pTOP.win		= {};
		pTOP.grilla		= {};
		pTOP.cmbbx		= {};
		pTOP.filtro		= {};
		pTOP.fn			= {};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pTOP.fn.agregar = function() {
			pTOP.fn.abrirModal(undefined, 'Agregar');
		};
		
		pTOP.fn.modificar = function(tipOpePosSel) {
			pTOP.fn.abrirModal(tipOpePosSel, 'Modificar');
		};

		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pTOP.grilla.TipoOperacionPosicion = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=471",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdOperacion		: { type: "number"},
								TipoProducto	: { type: "string"},
								TipoOperacion	: { type: "string"},
								TipoComprobante	: { type: "string"},
								NombreEvento	: { type: "string"},
								TipoEstado		: { type: "string"},
								Estado			: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "TipoProducto",		title: $translate.instant("UI_TIPO_PRODUCTO"),		width: "130px"	},
			    {field: "TipoOperacion",	title: $translate.instant("UI_TIPO_OPERACION"),		width: "140px"	},
				{field: "TipoComprobante",	title: $translate.instant("UI_TIPO_COMPROBANTE"),	width: "100px"	},
				{field: "NombreEvento",		title: $translate.instant("UI_NOMBRE_EVENTO"),		width: "130px"	},
				{field: "TipoEstado",		title: $translate.instant("UI_TIPO_ESTADO"),		width: "80px"	},
				{field: "Estado",			title: $translate.instant("UI_ESTADO"),				width: "80px"	}
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
		
		pTOP.cmbbx.TipoProducto={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq" , value: "VIG" }
			}
		};
		
		pTOP.cmbbx.TipoOperacion = {
			filtros: {
				IdTipoProducto : { field: "IdTipoProducto",	operator: "eq", value: "-1" }
			}
		};
		
		pTOP.cmbbx.TipoComprobante={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq" , value: "VIG" }
			}
		};
		
		pTOP.cmbbx.TipoEstado={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq" , value: "VIG" }
			}
		};
		
		pTOP.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pTOP.fn.buscar = function(){
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(pTOP.filtro.IdTipoProducto && pTOP.filtro.IdTipoProducto !== '0' ){
	    		niv1filters.push({ field: "IdTipoProducto", operator: "eq", value: pTOP.filtro.IdTipoProducto });
	    	}
			if(pTOP.filtro.IdTipoOperacion){
	    		niv1filters.push({ field: "IdTipoOperacion", operator: "eq", value: pTOP.filtro.IdTipoOperacion });
	    	}
			if(pTOP.filtro.IdTipoComprobante){
	    		niv1filters.push({ field: "IdTipoComprobante", operator: "eq", value: pTOP.filtro.IdTipoComprobante });
	    	}
			if(pTOP.filtro.IdTipoEstado){
	    		niv1filters.push({ field: "IdTipoEstado", operator: "eq", value: pTOP.filtro.IdTipoEstado });
	    	}
			if(pTOP.filtro.IdEstado){
	    		niv1filters.push({ field: "IdEstado", operator: "eq", value: pTOP.filtro.IdEstado });
	    	}
	    	
	    	if (niv1filters.length>0) pTOP.grilla.TipoOperacionPosicion.dataSource.filters(objfilters);
	    	else pTOP.grilla.TipoOperacionPosicion.dataSource.filters(new Array());
	    	
	    	pTOP.grilla.tipoOperacionPosicionSel = undefined;
		};
		
		
		pTOP.fn.limpiar = function(){
			pTOP.filtro.IdTipoProducto		= 0;
			pTOP.filtro.IdTipoOperacion		= 0;
			pTOP.filtro.IdTipoComprobante	= 0;
			pTOP.filtro.IdTipoEstado		= 0;
			pTOP.grilla.TipoOperacionPosicion.dataSource.removeAll();
		};
		
		
		pTOP.fn.abrirModal = function(tipOpePosSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalTipoOperacionPosicion",{ TipoOperacionPosicionSel: tipOpePosSel, AccionBoton : accion },false,false,pTOP.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		pTOP.grilla.tipoOperacionPosicionSel = undefined;
	        		pTOP.fn.buscar();
				}
	    	}); 
		};
		
	});	
}());
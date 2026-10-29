(function() {
	angular.module('ModuloMesa').controller('PanelMetodoPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pMetPos			= this;
		pMetPos.idVentana	= bcsComunes.ventanaWinId($scope);
		pMetPos.win			= {};
		pMetPos.grilla		= {};
		pMetPos.cmbbx		= {};
		pMetPos.filtro		= {};
		pMetPos.fn			= {};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pMetPos.fn.agregar = function() {
			pMetPos.fn.abrirModal(undefined, 'Agregar');
		};
		
		pMetPos.fn.modificar = function(metodoSel) {
			pMetPos.fn.abrirModal(metodoSel, 'Modificar');
		};

		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pMetPos.grilla.MetodosPosicion = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=468",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdMetodo		: { type: "number"},
								NombreMetodo	: { type: "string"},
								Glosa			: { type: "string"},
								IdTipoReajuste	: { type: "string"},
								Mesa			: { type: "string"},
								Estado			: { type: "string"},
								IdTipoPosicion	: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "NombreMetodo",		title: $translate.instant("UI_NOMBRE_METODO"),	width: "120px"	},
			    {field: "Glosa",			title: $translate.instant("UI_GLOSA"),			width: "120px"	},
			    {field: "IdTipoReajuste",	title: $translate.instant("UI_TIPO_REAJUSTE"),	width: "100px"	},
				{field: "Mesa",				title: $translate.instant("UI_MESA"),			width: "100px"	},
				{field: "Estado",			title: $translate.instant("UI_ESTADO"),			width: "80px"	},
				{field: "IdTipoPosicion",	title: $translate.instant("UI_TIPO_POS"),		width: "80px"	}
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
		
		pMetPos.cmbbx.TipoReajuste={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			}
		};
		
		pMetPos.cmbbx.Mesa = {
			filtros: {
				IdEmpresa		: { field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado		: { field: "IdEstadoRegistro",	operator: "eq", value: "ACT" },
				IdTipoProducto	: { field: "IdTipoProducto",	operator: "eq", value: "MERF" }  // TODO revisar si es siempre MERF
			}
		};
		
		pMetPos.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		pMetPos.cmbbx.TipoPos={
			filtros : {
				IdTipoPosicion : { field: "IdTipoPosicion", operator: "eq", value: "DIARIA" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pMetPos.fn.buscar = function(){
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(pMetPos.filtro.IdTipoReajuste){
	    		niv1filters.push({ field: "IdTipoReajuste", operator: "eq", value: pMetPos.filtro.IdTipoReajuste });
	    	}
			if(pMetPos.filtro.IdMesa){
	    		niv1filters.push({ field: "IdMesa", operator: "eq", value: pMetPos.filtro.IdMesa });
	    	}
			if(pMetPos.filtro.IdEstado){
				niv1filters.push({ field: "IdEstado", operator: "eq", value: pMetPos.filtro.IdEstado });
			}
			if(pMetPos.filtro.esPosicionInicial){
				niv1filters.push({ field: "IdTipoPosicion", operator: "eq", value: "INICIAL"});
			}
	    	
	    	if (niv1filters.length>0) pMetPos.grilla.MetodosPosicion.dataSource.filters(objfilters);
	    	else pMetPos.grilla.MetodosPosicion.dataSource.filters(new Array());
	    	
	    	pMetPos.grilla.metodoPosicionSel = undefined;
		};
		
		
		pMetPos.fn.limpiar = function(){
			pMetPos.filtro.IdTipoReajuste	= 0;
			pMetPos.filtro.IdMesa			= 0;
			pMetPos.filtro.IdEstado			= 0;
			pMetPos.filtro.IdTipoPosicion	= 0;
			pMetPos.grilla.MetodosPosicion.dataSource.removeAll();
		};
		
		
		pMetPos.fn.abrirModal = function(metodoSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalMetodoPosicion",{ MetodoPosicionSel: metodoSel, AccionBoton : accion },false,false,pMetPos.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		pMetPos.grilla.metodoPosicionSel = undefined;
	        		pMetPos.fn.buscar();
				}
	    	}); 
		};
		
	});	
}());
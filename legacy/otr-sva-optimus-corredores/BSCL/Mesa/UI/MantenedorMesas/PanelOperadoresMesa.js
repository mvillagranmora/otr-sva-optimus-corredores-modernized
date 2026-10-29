(function() {
	angular.module('ModuloMesa').controller('PanelOperadoresMesaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pOM		= this;
		pOM.idVentana= bcsComunes.ventanaWinId($scope);
		pOM.win		= {};
		pOM.grilla	= {};
		pOM.cmbbx	= {};
		pOM.filtro	= {};
		pOM.fn		= {};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pOM.win.agregar = function() {
			pOM.fn.abrirModal(undefined, 'Agregar');
		};
		
		pOM.win.modificar = function(operadorMesaSel) {
			pOM.fn.abrirModal(operadorMesaSel, 'Modificar');
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pOM.grilla.OperadoresMesa = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=458",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdAgenteOperadorMesa: { type: "number"},
								NombreMesa			: { type: "string"},
								CodigoMesa			: { type: "string"},
								DocumentoIdentidad	: { type: "string"},
								CodigoNombreAgOp	: { type: "string"},
								NombreCargoAgOp		: { type: "string"},
								EstadoRelacion		: { type: "string"},
								ModificaOperacionOtroUsuario : { type: "string"},
							}
						}
					}
				}
			}),
			columns: [
				{field: "NombreMesa",		title: $translate.instant("UI_MESA"),	width: "100px"},
				{field: "CodigoMesa",		title: $translate.instant("UI_CODIGO_MESA"),	width: "100px"},
				{field: "CodigoNombreAgOp",	title: $translate.instant("UI_AGENTE_OPERADOR"),width: "120px"},
				{field: "NombreCargoAgOp",	title: $translate.instant("UI_CARGO_AGENTE_OPERADOR"),width: "120px"},
				{field: "EstadoRelacion",	title: $translate.instant("UI_ESTADO_RELACION"),width: "60px"},
				{field: "ModificaOperacionOtroUsuario",	title: $translate.instant("UI_MODIFICA_OTRAS_OPERACIONES"),width: "60px"}				
			],
			pageable	: true,
			pageSize	: 50,
			autoBind	: false,
			height		: 500,
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
		
		pOM.cmbbx.Mesa = {
			filtros: {
				IdEmpresa	: { field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstadoRegistro",	operator: "eq", value: "ACT" }
			},
			configuracion : {},
		};
		
		pOM.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MESA" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pOM.fn.buscar = function(){
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(pOM.filtro.Codigo){
	    		niv1filters.push({ field: "CodigoMesa", operator: "eq", value: pOM.dataFiltros.MesaObj.Codigo });
	    	}
			if(pOM.filtro.IdEstado){
	    		niv1filters.push({ field: "IdEstado", operator: "eq", value: pOM.filtro.IdEstado });
	    	}
	    	
	    	if (niv1filters.length>0) pOM.grilla.OperadoresMesa.dataSource.filters(objfilters);
	    	else pOM.grilla.OperadoresMesa.dataSource.filters(new Array());
	    	
	    	pOM.grilla.operadorMesaSel = undefined;
		};
		
		pOM.fn.limpiar = function(){
			pOM.filtro.IdMesa	= 0;
			pOM.filtro.IdEstado	= 0;
			pOM.grilla.OperadoresMesa.dataSource.removeAll();
		}
		
		pOM.fn.abrirModal = function(operadorMesaSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalOperadoresMesa",{ OperadorMesaSel: operadorMesaSel, AccionBoton : accion },false,true);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if (respuesta){
	        		pOM.grilla.operadorMesaSel = undefined;
	        		pOM.fn.buscar();
				}
	    	}); 
		};
		
	});
}());

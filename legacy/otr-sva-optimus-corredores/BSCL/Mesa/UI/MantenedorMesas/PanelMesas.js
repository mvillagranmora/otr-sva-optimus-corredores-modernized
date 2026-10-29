(function() {
	angular.module('ModuloMesa').controller('PanelMesasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pMesas		= this;
		pMesas.idVentana= bcsComunes.ventanaWinId($scope);
		pMesas.params	= bcsComunes.ventanaParams(pMesas.idVentana);
		pMesas.win		= {};
		pMesas.grilla	= {};
		pMesas.cmbbx	= {};
		pMesas.filtro	= {};
		pMesas.fn		= {};
		
		
		pMesas.filtro.IdTipoProductoMesaModuloDefecto = '0';
		pMesas.modulo = null;
		if(pMesas.modulo != undefined || pMesas.modulo != ''){
			pMesas.modulo = pMesas.params.modulo;
		}
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pMesas.win.agregar = function() {
			pMesas.fn.abrirModal(undefined, 'Agregar');
		};
		
		pMesas.win.modificar = function(mesaSel) {
			pMesas.fn.abrirModal(mesaSel, 'Modificar');
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pMesas.grilla.Mesas = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=454",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdMesa				: { type: "number"},
								NombreCliente		: { type: "string"},
								CodigoPortafolio	: { type: "string"},
								DocumentoIdentidad	: { type: "string"},
								Nombre				: { type: "string"},
								Codigo				: { type: "string"},
								TipoProducto		: { type: "string"},
								NombreValorRazonable: { type: "string"},
								EstadoRegistro		: { type: "string"},
								PorDefectoUI		: { type: "string"},
								DescripcionIFRS		: { type: "string"},
								CodigoIFRS			: { type: "string"},
								AplicaFusionUI      : { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "Nombre",			title: $translate.instant("UI_NOMBRE_MESA"),width: "90px"	},
			    {field: "Codigo",			title: $translate.instant("UI_CODIGO_MESA"),width: "70px"	},
				{field: "NombreCliente",	title: $translate.instant("UI_CLIENTE"),	width: "130px"	},
				{field: "CodigoPortafolio",	title: $translate.instant("UI_PORTAFOLIO"),	width: "100px"	},
				{field: "DocumentoIdentidad",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),	width: "100px"	},
				{field: "TipoProducto",		title: $translate.instant("UI_TIPO_PRODUCTO"),	width: "80px"	},
				{field: "NombreValorRazonable",	title: $translate.instant("UI_VALOR_RAZONABLE"),width: "100px"	},
				{field: "EstadoRegistro",	title: $translate.instant("UI_ESTADO"),		width: "50px"	},
				{field: "PorDefectoUI",		title: $translate.instant("UI_POR_DEFECTO"),width: "50px",	template : "<span translate>#: PorDefectoUI # </span>"	},
				{field: "DescripcionIFRS",	 title: $translate.instant("UI_CODIGO_IFRS"),	width: "130px"	},
				{field: "AplicaFusionUI",	 title: $translate.instant("UI_APLICA_FUSION_RF_IIF"),	width: "50px",	template : "<span translate>#: AplicaFusionUI #</span>"		}
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
		
		pMesas.cmbbx.TipoProductoMesaModulo={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		
		if(pMesas.modulo != null){
			pMesas.cmbbx.TipoProductoMesaModulo.filtros['IdTipoProductoMesaModulo'] = {field: "IdTipoProductoMesaModulo", operator: "eq", value: pMesas.modulo};
			pMesas.filtro.IdTipoProductoMesaModulo = pMesas.modulo;
			pMesas.filtro.IdTipoProductoMesaModuloDefecto = pMesas.modulo;
		}
		
		pMesas.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MESA" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pMesas.fn.buscar = function(){
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(pMesas.filtro.IdTipoProductoMesaModulo){
	    		niv1filters.push({ field: "IdTipoProductoMesaModulo", operator: "eq", value: pMesas.filtro.IdTipoProductoMesaModulo });
	    	}
			if(pMesas.filtro.IdEstado){
	    		niv1filters.push({ field: "IdEstadoRegistro", operator: "eq", value: pMesas.filtro.IdEstado });
	    	}
	    	
	    	if (niv1filters.length>0) pMesas.grilla.Mesas.dataSource.filters(objfilters);
	    	else pMesas.grilla.Mesas.dataSource.filters(new Array());
	    	
	    	pMesas.grilla.mesaSel = undefined;
		};
		
		pMesas.fn.limpiar = function(){
			pMesas.filtro.IdTipoProductoMesaModulo	= 0;
			pMesas.filtro.IdEstado					= 0;
			pMesas.grilla.Mesas.dataSource.removeAll();
		}
		
		pMesas.fn.abrirModal = function(mesaSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalMantenedorMesas",{ MesaSel: mesaSel, AccionBoton : accion },false,false,pMesas.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		pMesas.grilla.mesaSel = undefined;
	        		pMesas.fn.buscar();
				}
	    	}); 
		};
		
	});
}());
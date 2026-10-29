angular.module('ModuloPlataforma').controller('PanelConveniosCtrl',function ($scope,bcsComunes,$translate) {
	
	const panelConvenios 	= this;
	const idVentana			= bcsComunes.ventanaWinId($scope);
	panelConvenios.filtros 	= {};
	panelConvenios.datos 	= {};
	

	//Grilla Convenios
	panelConvenios.configuracionGrillaConvenios = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700291",
							dataType	: "json"
					    }
	                },
					schema: {
						model: {
							fields: {
								IdContratoComision			: { type: "number"},
								NombreContratoComision		: { type: "string"},
								DescripcionContratoComision	: { type: "string"},
								NombreTipoInstrumento		: { type: "string"},
								DocumentoIdentidad			: { type: "string"},
								NombrePersonaCompleto		: { type: "string"},
								CodigoPortafolio			: { type: "string"},
								NombreEstado				: { type: "string"},
							}
						}
					},
				}
			}),
			columns: [
			    {field: "NombreContratoComision" 	,title: $translate.instant('UI_NOMBRE'),  width: "100px", align: "left", sortable : true, editable: false },
			    {field: "DescripcionContratoComision" 	,title: $translate.instant('UI_DESCRIPCION'), width: "100px", align: "left", sortable : true, editable: false },
			    {field: "DocumentoIdentidad" 	,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "75px", align: "left", sortable : true, editable: false },
			    {field: "NombrePersonaCompleto" 	,title: $translate.instant('UI_CLIENTE'), width: "100px", align: "left", sortable : true, editable: false },
			    {field: "CodigoPortafolio" 	,title: $translate.instant('UI_PORTAFOLIO'), width: "100px", align: "left", sortable : true, editable: false },
			    {field: "NombreTipoInstrumento",	title: $translate.instant('UI_CLASIFICACION'), width: "75px", align: "left", sortable : true, editable: false },
			    {field: "NombreEstado",	title: $translate.instant('UI_ESTADO'), width: "60px", align: "left", sortable : true, editable: false }
			],
			gridCreated : function(grid){
				panelConvenios.ajustaVentana();
			},
			sortable			: false,
			pageable			: true,
			autoBind			: true,
	    	filterable			: true,
			height				: 550,
		    selectable 			: "row",
		    scrollable			: true,
		    userSettings		: true,
			navigatable			: true
	};
	

	panelConvenios.crear = function(){
		const id = bcsComunes.ventanaAbrir("agregarModificarConvenioComision", {}, false, false, idVentana);  
		bcsComunes.ventanaPromesa(id).then(function(){
			panelConvenios.datos.kNgModel = undefined;
			panelConvenios.configuracionGrillaConvenios.dataSource.read();
         }); 
  
	};

  	panelConvenios.editar = function(datos) {
  		
		if(!datos) return;
		const id = bcsComunes.ventanaAbrir("agregarModificarConvenioComision", datos, false, false, idVentana);                              
        bcsComunes.ventanaPromesa(id).then(function(){  
        	panelConvenios.configuracionGrillaConvenios.dataSource.read();
        	panelConvenios.datos.kNgModel = undefined;
        }); 
        
  	};

	panelConvenios.ajustaVentana = function() {
		 bcsComunes.ventanaTamanoAutomatico(idVentana,40);
    }

	panelConvenios.buscar = function() {
		
		let filtroBusqueda = new Array();
		
		if(panelConvenios.filtros.IdPortafolio){
			filtroBusqueda.push({ field: "IdPortafolio", operator: "eq", value: panelConvenios.filtros.IdPortafolio});
		}
		
		panelConvenios.configuracionGrillaConvenios.dataSource.filter(filtroBusqueda);
		
	};

	panelConvenios.limpiarFiltros = function() {
  		panelConvenios.filtros.IdPortafolio = false;
	};
	
	panelConvenios.limpiarFiltros();
	panelConvenios.ajustaVentana();
});

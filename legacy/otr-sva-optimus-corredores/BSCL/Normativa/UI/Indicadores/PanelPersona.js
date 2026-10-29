(function() {
	angular.module('ModuloNormativa').controller('PanelPersonaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pPER 		= this;
		pPER.idVentana	= bcsComunes.ventanaWinId($scope);
		pPER.win		= {};
		pPER.grilla		= {};
		pPER.cmbbx		= {};
		pPER.filtro		= {};
		pPER.fn			= {};
		

		pPER.modulo='NOR';
		pPER.salidaMensaje='';
		$scope.dataTemplate	= {};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pPER.grilla.Persona = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=55676756",  
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								   	NombrePersona			: { type: "string"},
								  	DocumentoIdentidad		: { type: "string"},
								   	IdPersona				: { type: "number"},
									NombreTipoIndicador		: { type: "string"},
									GrupoIndicador			: { type: "string"},
									IdUso					: { type: "string"},
									IdTipoIndicador			: { type: "string"},
									CodigoTipoIndicador		: { type: "string"},
									ClaseDato				: { type: "string"},
									LongitudMinima			: { type: "number"},
									LongitudMaxima			: { type: "number"},
									IdTipoPersona			: { type: "string"},
									ModeloCliente			: {type:  "string"},
									ValorIndicador			: { type: "string"},
									CodigoValorIndicador	: { type: "string"},
									NombreValorIndicador	: { type: "string"},
									UINombreValorIndicador	: { type: "string"},
									IdIndicador				: { type: "number"},
									FechaCreacion 			: { type:   "date"},
							}
						}
					}
				}
			}),
			columns: [
					{field: "IdPersona",				 	title: $translate.instant("UI_PERSONA"),					width: "180px"	},
					{field: "NombrePersona",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
					{field: "DocumentoIdentidad",			title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),	    width: "80px", align: "left"},
					{field: "CodigoTipoIndicador",			title: $translate.instant("UI_CODIGO"),						width: "60px"	},
					{field: "NombreTipoIndicador",			title: $translate.instant("UI_INDICADOR"),					width: "200px"	},
					{field: "UINombreValorIndicador",		title: $translate.instant("UI_VALOR"),						width: "180x"	},
					{field: "FechaCreacion",				title: $translate.instant("UI_FECHA_INGRESO"),				width: "80px"	},
					
			],
			hideDetailButton: function(row) {
				var hide = true;
				return hide;
			},

			pageable: {
                refresh: true,
    
                buttonCount: 5
            },
	
			pageSize	: 50,
			autoBind	: false,
			height		: 540,//580,
			
		    rowColorBackground	: function(row) {		
				switch(row.get("ValorIndicador").trim() + row.get("ClaseDato").trim()) {
		             case 'NCHECKBOX':
		                return "red";
		                break;
		              case 'SCHECKBOX':
		                return "green";
		                break;
		            default:
		            	return "white";
		                break;     
				  }	
				},
				
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
	
		

		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pPER.cmbbx.TipoIndicador={
			filtros: {
				IdTipoEntidad 	: {field: "IdEstado",operator: "eq", value:'ACT' },
				IdGrupoIndicador : { field: "GrupoIndicador", operator: "eq", value: '-1' }
			}
		};
		
		pPER.cmbbx.GrupoIndicador={
			filtros: {
			IdTipoEntidad : {field: "IdTipoEntidad",operator: "eq", value:'Persona' },
					IdUso : {field: "IdUso",operator: "eq", value: 'IND'}
			}
		};
		
		pPER.cmbbx.GrupoIndicador={
			filtros: {
			IdTipoEntidad : {field: "IdTipoEntidad",operator: "eq", value:'CLIENTE' },
					IdUso : {field: "IdUso",operator: "eq", value: 'IND'}
			}
		};

		/*********************************************
		 * FUNCIONES
		 *********************************************/
	
	
		
		
		pPER.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.filters = niv1filters;
	    	
	    	if(pPER.cmbbx.IdGrupoIndicador && ((pPER.cmbbx.IdGrupoIndicador).toUpperCase()).trim() != 'TODOS'){
	    		niv1filters.push({ field: "GrupoIndicador", operator: "eq", value:pPER.cmbbx.IdGrupoIndicador });
	    	};
	    	if(pPER.cmbbx.IdTipoIndicador){
	    		niv1filters.push({ field: "IdTipoIndicador", operator: "eq", value:pPER.cmbbx.IdTipoIndicador });
	    	};
	    		
	    	if(pPER.filtro.IdSocioDeNegocio && pPER.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdPersona", operator: "eq", value: pPER.SocioDeNegocioObj.IdPersona });
			};
			
	    	if (niv1filters.length>0) pPER.grilla.Persona.dataSource.filter(objfilters);
	    	else pPER.grilla.Persona.dataSource.filter(new Array());
	    	
	    	
	    	pPER.grilla.limiteSel = undefined;
		};
	
		pPER.fn.limpiar = function(){
			
			pPER.filtro.IdClasificacionTipoInstrumento	= 0;
			pPER.grilla.Persona.dataSource.removeAll();
		};
		

	});
}());
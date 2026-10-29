(function() {
	angular.module('ModuloNormativa').controller('PanelClienteCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLCP 		= this;
		pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
		pLCP.win		= {};
		pLCP.grilla		= {};
		pLCP.cmbbx		= {};
		pLCP.filtro		= {};
		pLCP.fn			= {};
		

		pLCP.modulo='NOR';
		pLCP.salidaMensaje='';
		$scope.dataTemplate	= {};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLCP.grilla.Cliente = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3069",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								   	IdCliente				: { type: "number"},
								   	IdPersona				: { type: "number"},
								   	IdTipoCliente			: { type: "string"},
								  	NombreCliente			: { type: "string"},
								  	DocumentoIdentidad		: { type: "string"},
									IdTipoEntidad			: { type: "string"},
									NombreTipoIndicador		: { type: "string"},
									GrupoIndicador			: { type: "string"},
									IdUso					: { type: "string"},
									IdTipoIndicador			: { type: "string"},
									CodigoTipoIndicador		: { type: "string"},
									ClaseDato				: { type: "string"},
									IdTipoPersona			: { type: "string"},
									ValorIndicador			: { type: "string"},
									CodigoValorIndicador	: { type: "string"},
									NombreValorIndicador	: { type: "string"},
									UINombreValorIndicador	: { type: "string"},
									IdIndicador				: { type: "number"},
									FechaCreacion 			: { type: "date"},
									FechaModificacion 		: { type: "date"}
							}
						}
					}
				}
			}),
			columns: [
				    {field: "IdCliente",					title: $translate.instant("UI_CLIENTE"),					width: "60px"	},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
					{field: "DocumentoIdentidad",				title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),	width: "80px", align: "left"},
					{field: "CodigoTipoIndicador",			title: $translate.instant("UI_CODIGO"),						width: "60px"	},
					{field: "NombreTipoIndicador",			title: $translate.instant("UI_INDICADOR"),					width: "200px"	},
					{field: "ValorIndicador",		title: $translate.instant("UI_VALOR"),						width: "180x"	},
					{field: "FechaCreacion",				title: $translate.instant("UI_FECHA_INGRESO"),				width: "80px"	},
					{field: "FechaModificacion",			title: $translate.instant("UI_FECHA_MODIFICACION"),			width: "80px"	},
					
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
			
		    rowCellColor : function(row) {
		    	var column = new Array();
		    	if (row.get("ValorIndicador")=='S' && row.get("ClaseDato")=='CHECKBOX'){column.push({column: "UINombreValorIndicador", color: "green"});};
		    	if (row.get("ValorIndicador")=='N' && row.get("ClaseDato")=='CHECKBOX'){column.push({column: "UINombreValorIndicador", color: "red"});};
		    	
		    	return column;

		    },
		    
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
		pLCP.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		
		pLCP.cmbbx.TipoIndicador={
			filtros: {
				IdTipoEntidad 	: {field: "IdEstado",operator: "eq", value:'ACT' },
				IdGrupoIndicador : { field: "GrupoIndicador", operator: "eq", value: '-1' }
			}
		};
		
		pLCP.cmbbx.GrupoIndicador={
			filtros: {
			IdTipoEntidad : {field: "IdTipoEntidad",operator: "eq", value:'CLIENTE' },
					IdUso : {field: "IdUso",operator: "eq", value: 'IND'}
			}
		};

		/*********************************************
		 * FUNCIONES
		 *********************************************/
	
	
		
		
		pLCP.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			//objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			//niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
	    	if(pLCP.cmbbx.IdGrupoIndicador && ((pLCP.cmbbx.IdGrupoIndicador).toUpperCase()).trim() != 'TODOS'){
	    		//pLCP.fn.envioFecha();
	    		niv1filters.push({ field: "GrupoIndicador", operator: "eq", value:pLCP.cmbbx.IdGrupoIndicador });
	    	};
	    	if(pLCP.cmbbx.IdTipoIndicador){
	    		//pLCP.fn.envioFecha();
	    		niv1filters.push({ field: "IdTipoIndicador", operator: "eq", value:pLCP.cmbbx.IdTipoIndicador });
	    	};
			if(pLCP.filtro.IdSocioDeNegocio && pLCP.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pLCP.SocioDeNegocioObj.IdCliente });
			};
	    	
	    	if (niv1filters.length>0) pLCP.grilla.Cliente.dataSource.filter(objfilters);
	    	else pLCP.grilla.Cliente.dataSource.filter(new Array());
	    	
	    	pLCP.grilla.limiteSel = undefined;
		};
	
		pLCP.fn.limpiar = function(){
			
			pLCP.filtro.IdClasificacionTipoInstrumento	= 0;
			pLCP.grilla.Cliente.dataSource.removeAll();
		};
		

	});
}());
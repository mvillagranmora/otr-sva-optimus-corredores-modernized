(function() {
	angular.module('ModuloNormativa').controller('PanelPortafolioCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pPFO 		= this;
		pPFO.idVentana	= bcsComunes.ventanaWinId($scope);
		pPFO.win		= {};
		pPFO.grilla		= {};
		pPFO.cmbbx		= {};
		pPFO.filtro		= {};
		pPFO.fn			= {};
		

		pPFO.modulo='NOR';
		pPFO.salidaMensaje='';
		$scope.dataTemplate	= {};
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pPFO.grilla.Portafolio = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=23983",
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
									IdPortafolio			: { type: "number"},
									CodigoPortafolio		: { type: "string"},
									EstadoPortafolio		: { type: "string"},
			
							}
						}
					}
				}
			}),
			columns: [
				    {field: "IdCliente",					title: $translate.instant("UI_CLIENTE"),					width: "60px"	},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
					{field: "DocumentoIdentidad",			title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),		width: "80px", align: "left"},
					{field: "CodigoPortafolio",				title: $translate.instant("UI_PORTAFOLIO"),			width: "80px", align: "left"},
					{field: "CodigoTipoIndicador",			title: $translate.instant("UI_CODIGO"),						width: "60px"	},
					{field: "NombreTipoIndicador",			title: $translate.instant("UI_INDICADOR"),					width: "200px"	},
					{field: "UINombreValorIndicador",		title: $translate.instant("UI_VALOR"),						width: "180x"	},
					{field: "EstadoPortafolio",				title: $translate.instant("UI_ESTADO_PORTAFOLIO"),			width: "180x"	},
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
			/*
		    rowCellColor : function(row) {
		    	var column = new Array();
		    	if (row.get("ValorIndicador")=='S' && row.get("ClaseDato")=='CHECKBOX'){column.push({column: "UINombreValorIndicador", color: "green"});};
		    	if (row.get("ValorIndicador")=='N' && row.get("ClaseDato")=='CHECKBOX'){column.push({column: "UINombreValorIndicador", color: "red"});};
		    	
		    	return column;

		    },
		    */
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
		pPFO.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		
		pPFO.cmbbx.TipoIndicador={
			filtros: {
				IdTipoEntidad : {field: "IdTipoEntidad",operator: "eq", value:'PORTAFOLIO' },
				IdEstado 	: {field: "IdEstado",operator: "eq", value:'ACT' },
				IdGrupoIndicador : { field: "GrupoIndicador", operator: "eq", value: '-1' }
			}
		};
		
		pPFO.cmbbx.GrupoIndicador={
			filtros: {
			IdTipoEntidad : {field: "IdTipoEntidad",operator: "eq", value:'PORTAFOLIO' },
					//IdUso : {field: "IdUso",operator: "eq", value: 'IND'}
			}
		};

		//Sin Producto
		pPFO.cmbbx.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
	        idPostFiltro  	: false,	
			itemPostFiltro	: false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
		/*********************************************
		 * FUNCIONES
		 *********************************************/

		pPFO.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			//objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			//niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
	    	if(pPFO.cmbbx.IdGrupoIndicador && ((pPFO.cmbbx.IdGrupoIndicador).toUpperCase()).trim() != 'TODOS'){
	    		//pPFO.fn.envioFecha();
	    		niv1filters.push({ field: "GrupoIndicador", operator: "eq", value:pPFO.cmbbx.IdGrupoIndicador });
	    	};
	    	if(pPFO.cmbbx.IdTipoIndicador){
	    		//pPFO.fn.envioFecha();
	    		niv1filters.push({ field: "IdTipoIndicador", operator: "eq", value:pPFO.cmbbx.IdTipoIndicador });
	    	};
			if(pPFO.cmbbx.IdPortafolio){
	    		niv1filters.push({ field: "IdPortafolio", operator: "eq", value: pPFO.cmbbx.IdPortafolio });
			};
	    	
	    	if (niv1filters.length>0) pPFO.grilla.Portafolio.dataSource.filter(objfilters);
	    	else pPFO.grilla.Portafolio.dataSource.filter(new Array());
	    	
	    	pPFO.grilla.limiteSel = undefined;
		};
	
		pPFO.fn.limpiar = function(){
			
			pPFO.filtro.IdClasificacionTipoInstrumento	= 0;
			pPFO.grilla.Portafolio.dataSource.removeAll();
		};
		

	});
}());
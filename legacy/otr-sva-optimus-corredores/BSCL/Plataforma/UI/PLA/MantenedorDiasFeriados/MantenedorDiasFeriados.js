(function() {
	angular.module('ModuloPlataforma').controller('MantenedorDiasFeriadosCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas,bcsAlerta,OPCIONES_BCSALERTA, servicioFeriado){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var manDF 		= this;
		manDF.idVentana	= bcsComunes.ventanaWinId($scope);
		manDF.win		= {};
		manDF.grilla	= {};
		manDF.cmbbx		= {};
		manDF.filtro	= {};
		manDF.fn		= {};
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		manDF.win.agregar = function() {
			manDF.fn.abrirModal(undefined, 'Agregar');
		};
		
		manDF.win.modificar = function(feriadoSel) {
			manDF.fn.abrirModal(feriadoSel, 'Modificar');
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		manDF.cmbbx.TipoDiaFeriado={
			filtros: {
				//IdPais	: { field: "IdPais",	operator: "eq", value: $scope.$root.SesionIdPaisSistema },
				IdEstado: { field: "IdEstado",	operator: "eq", value: "ACT" }
			},
		};
		
		manDF.cmbbx.Dia={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "DIASEMANAESPANOL" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_DIA'),
				dataValueField: "NumeroDiaSemana"
			},
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		manDF.grilla.DiasFeriados = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					filter : {field: "IdEstado", operator: "eq", value: "ACT"},
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=623",
							dataType: "json"
						}
					},					
						
					schema: {
						model: {
							fields: {
								IdFeriado		: { type: "number"},
								TipoDiaFeriado	: { type: "string"},
								Fecha			: { type: "date"},
								FechaUI			: { type: "string"},
								DiaUI			: { type: "string"},
								DescripcionDia	: { type: "string"},
								AgnoCarga		: { type: "number"},
								NombrePais		: { type: "string"},
								IdEstado        : { type: "string"},
							}
						}
					}
				}
			}),
			columns: [
			    {field: "TipoDiaFeriado",	title: $translate.instant('UI_TIPO_DIA_FERIADO'),width: "80px"	},
			    {field: "FechaUI",			title: $translate.instant('UI_FECHA'),			width: "80px"	},
			    {field: "DiaUI",			title: $translate.instant('UI_DIA'),			width: "100px",	template : "<span translate>#: DiaUI # </span>"	},
			    {field: "DescripcionDia",	title: $translate.instant('UI_DESCRIPCION'),	width: "120px"	},
			    {field: "AgnoCarga",		title: $translate.instant('UI_AGNO_CARGA'),		width: "80px"	},
			    {field: "NombrePais",		title: $translate.instant('UI_PAIS'),			width: "100px"	},
			    {field: "IdEstado",		    title: $translate.instant('UI_ESTADO'),			width: "80px"	}
			],
			pageable	: true,
			pageSize	: 100,
			autoBind	: false,
			height		: 500,
		    selectable 	: "row",
		    excel : {
				allPages: true,
			},
		    filterable	: true,
	      	userSettings: true
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		manDF.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = [{ field: "IdEstado", operator: "eq", value: "ACT" }];
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(manDF.filtro.IdTipoDiaFeriado){
				niv1filters.push({ field: "Tipo", operator: "eq", value: manDF.filtro.IdTipoDiaFeriado });
	    	}
			if(manDF.filtro.ValorParametro){				
				niv1filters.push({ field: "Dia", operator: "eq", value: manDF.filtro.ValorParametro-1 });
	    	}
			if(manDF.filtro.AgnoCarga){
				niv1filters.push({ field: "AgnoCarga", operator: "eq", value: manDF.filtro.AgnoCarga });
	    	}
			if(manDF.filtro.IdPais){
				niv1filters.push({ field: "IdPais", operator: "eq", value: manDF.filtro.IdPais });
	    	}
			
			if (niv1filters.length>0) manDF.grilla.DiasFeriados.dataSource.filters(objfilters);
	    	else manDF.grilla.DiasFeriados.dataSource.filters([]);
	    	
			manDF.grilla.diaFeriadoSel = undefined;
		};
		
		
		manDF.fn.limpiar = function(){
			manDF.filtro.IdTipoDiaFeriado	= 0;
			manDF.filtro.ValorParametro		= 0;
			manDF.filtro.AgnoCarga			= '';
			manDF.filtro.IdPais				= 0;
			manDF.grilla.DiasFeriados.dataSource.removeAll();
		};
		
		
		
		
		manDF.fn.abrirModal = function(feriadoSel, accion){
			bcsComunes.ventanaOverlay(manDF.idVentana, true);
			var id1 = bcsComunes.ventanaAbrir("ModalDiasFeriados",{ FeriadoSel: feriadoSel, AccionBoton : accion },false,false);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		manDF.grilla.diaFeriadoSel = undefined;
	        		manDF.fn.buscar();
				}
				bcsComunes.ventanaOverlay(manDF.idVentana, false);
	    	});
		};
		
		
		
		
	});
}());


//---------------------------------------------- FIN CONTROLADOR PRINCIPAL ----------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioFeriado',['$resource',function($resource) {
		var urlCrearDiaFeriado		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=624";
		var urlModificarDiaFeriado	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=625";
		
		return $resource("", {}, {
			'crearDiaFeriado'		: {method: "POST", url: urlCrearDiaFeriado},
			'modificarDiaFeriado'	: {method: "POST", url: urlModificarDiaFeriado},
		});
	}]);
}());
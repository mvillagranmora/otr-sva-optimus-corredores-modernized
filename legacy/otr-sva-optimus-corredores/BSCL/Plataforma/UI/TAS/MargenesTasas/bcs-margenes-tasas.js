angular.module('ModuloPlataforma').directive('bcsMargenesTasas',
function($q,$translate,bcsComunes,$timeout,$state){
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			//idMoneda	: '=?',
			tipoEntidad	: '=?'	// AGENTE, CLIENTE, CUSTODIA
		},
    	templateUrl: '../../Plataforma/UI/TAS/MargenesTasas/bcs-margenes-tasas.html', 
    	link: function (scope, element, attrs, fn) {
    		
    		/*********************************************
			 * CHECK INICIAL DE VARIABLES
			 ********************************************/
    		
    		//if(scope.idMoneda	=== undefined) scope.idMoneda = '';
			if(scope.tipoEntidad=== undefined) scope.tipoEntidad = '';
			
    		/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			
			scope.filtro	= scope.grilla 	|| {};
			scope.datoFiltro= scope.grilla 	|| {};
			scope.grilla	= scope.grilla 	|| {};
			scope.fn		= scope.fn 		|| {};
			scope.win 		= scope.win		|| {};
			
			
			/*********************************************
			 * ACCIONES DE PANTALLA
			 ********************************************/
			
			scope.win.agregar = function() {
				scope.fn.abrirModal(undefined, 'Agregar');
			};
			scope.win.modificar = function(margenTasaSel) {
				scope.fn.abrirModal(margenTasaSel, 'Modificar');
			};
			
			/*********************************************
			 * DATASOURCES
			 *********************************************/
			
		
			/*********************************************
			 * COMBOBOX
			 *********************************************/
		
				
			/*********************************************
			 * GRILLAS
			 *********************************************/
			
			scope.grilla.MargenesTasas = {
				dataSource : new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=618",
								dataType: "json"
							}
						},
						schema: {
							model: {
								fields: {
									IdMargenTasa: { type: "number"},
									IdMoneda	: { type: "string"},
									MontoDesde	: { type: "number"},
									MontoHasta	: { type: "number"},
									PlazoDesde	: { type: "number"},
									PlazoHasta	: { type: "number"},
									Tasa		: { type: "number"},
									Estado		: { type: "string"}
								}
							}
						}
					}
				}),
				columns: [
					{field: "IdMoneda",		title: $translate.instant("UI_MONEDA"),		width: "70px"	},
					{field: "MontoDesde",	title: $translate.instant("UI_MONTO_DESDE"),width: "100px",	},
					{field: "MontoHasta",	title: $translate.instant("UI_MONTO_HASTA"),width: "100px",	},
					{field: "PlazoDesde",	title: $translate.instant("UI_PLAZO_DESDE"),width: "80px",	},
					{field: "PlazoHasta",	title: $translate.instant("UI_PLAZO_HASTA"),width: "80px",	},
					{field: "Tasa",			title: $translate.instant("UI_TASA"),		width: "80px",	},
					{field: "Estado",		title: $translate.instant("UI_ESTADO"),		width: "90px",	},
				],
				pageable	: true,
				pageSize	: 50,
				autoBind	: false,
				height		: 500,
			    selectable 	: "row",
			    excel : {
					allPages: true,
				},
			    filterable	: true
			};
		
		
			/*********************************************
			 * FUNCIONES
			 ********************************************/
			
			scope.fn.buscar = function(){
				var objfilters = { };
				var niv1filters = new Array();
				objfilters.logic = 'and';
				objfilters.filters = niv1filters;
				
				niv1filters.push({ field: "TipoEntidad",operator: "eq", value: scope.tipoEntidad });
				niv1filters.push({ field: "IdEmpresa",	operator: "eq", value: scope.$root.SesionIdEmpresa });
				if(scope.filtro.IdTipoReajuste){
					niv1filters.push({ field: "IdMoneda", operator: "eq", value: scope.filtro.IdTipoReajuste });
		    	}
				
				if (niv1filters.length>0) scope.grilla.MargenesTasas.dataSource.filters(objfilters);
		    	else scope.grilla.MargenesTasas.dataSource.filters(new Array());
		    	
				scope.grilla.margenTasaSel = undefined;
			};
			
			
			scope.fn.abrirModal = function(margenTasaSel, accion){
				var id1 = bcsComunes.ventanaAbrir("ModalMargenesTasas",{ MargenTasaSel: margenTasaSel, TipoEntidad: scope.tipoEntidad, AccionBoton: accion },false,true);
		        var promesa=bcsComunes.ventanaPromesa(id1);
		        
		        promesa.then(function(respuesta){
		        	if(respuesta){
		        		scope.grilla.margenTasaSel = undefined;
		        		scope.fn.buscar();
					}
		    	});
			};
			
			
			/*********************************************
			 * FUNCIONES WATCH
			 *********************************************/
			/*
			scope.$watch('idMoneda',function(newval,oldval){
				if(newval && newval!==oldval){
					scope.fn.buscar();
				}
				else{
					scope.win.directivaCargada = false;
					scope.grilla.MargenesTasas.dataSource.removeAll();
				}
			});
			*/
			
			/*
			scope.$watch('tipoEntidad',function(newval,oldval){
				scope.idMoneda = undefined;
			});
			*/
			
			
			/*********************************************
			 * Al inicio
			 **********************************************/
			/*
			if(scope.idMoneda === '' || scope.tipoEntidad === '') {
				console.log("directiva mal inicializada");
				scope.win.directivaCargada = false;
			}
			else{
				scope.win.directivaCargada = true;
				scope.fn.buscar();
			}
			*/
    	}
	}	
});
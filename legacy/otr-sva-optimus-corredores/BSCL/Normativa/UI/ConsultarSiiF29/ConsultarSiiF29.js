angular.module('ModuloNormativa').controller('ConsultarSiiF29Ctrl',
	function($scope,$translate,$resource,$timeout,bcsComunes)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {};
		
		var cSiiF29 = this;
		
		cSiiF29.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		
		//Configurar al principio

		cSiiF29.data = {};
		cSiiF29.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};


		
		cSiiF29.buscarRegistros = function(nroPag, filtrosAnteriores){
			
			//Filtros Busqueda
			
			var filtroBusqueda = new Array();
			
			if (cSiiF29.kngPeriodo != undefined) {
				filtroBusqueda.push({ field: "Periodo", operator: "eq", value: util.dateSPdate($scope.cSiiF29.kngPeriodo).substring(0,7) });
			}
			cSiiF29.gridMovimientos.dataSource.filter(filtroBusqueda);
		};


		/******************************************************
		 * GRILLAS
		 * ***************************************************/
		cSiiF29.gridMovimientos = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 15,
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6099",
							dataType	: "json"
					    }
	                },
					//filter: { field: "IdPersonaOrigen", operator: "eq", value: Number(ppr.IdPersona) },
					schema: {
						model: {
							fields: {
								DocumentoIdentidad	: { type: "string"},
								Nombre 				: { type: "string"},
								Fondo				: { type: "string"},
								Serie				: { type: "string"},
								MontoRetiro 		: { type: "number"},
								MontoRetenido	    : { type: "number"},
								FechaCertificacion	: { type: "sysdate"},
							}
						}
					}
				}
			}),
			columns: [
			    {field: "DocumentoIdentidad"	,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "Nombre"				,title: $translate.instant('UI_NOMBRE'), width: "250px", align: "left" , sortable : true, editable: false },
			    {field: "Fondo"					,title: $translate.instant('UI_FONDO'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "Serie"					,title: $translate.instant('UI_SERIE'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "MontoRetiro"			,title: $translate.instant('UI_MONTO_RETIRO'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
			    {field: "MontoRetenido"			,title: $translate.instant('UI_MONTO_RETENIDO'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
				{field: "FechaCertificacion"	,title: $translate.instant('UI_FECHA_CERTIFICACION'), width: "80px", align: "left" , sortable : true, editable: false },
			],	
			sortable			: false,
			pageable			: true,
			autoBind			: false,
	    	filterable			: true,
			height				: 550,
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true
		};
			
	});
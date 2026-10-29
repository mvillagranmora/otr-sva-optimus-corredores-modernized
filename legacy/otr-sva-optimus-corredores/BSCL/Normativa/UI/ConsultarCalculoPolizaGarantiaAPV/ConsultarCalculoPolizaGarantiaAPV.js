angular.module('ModuloNormativa').controller('ConsultarCalculoPolizaGarantiaAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {};
		
		var cCalPolAPV = this;
		
		cCalPolAPV.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		
		//Configurar al principio

		cCalPolAPV.data = {};
		cCalPolAPV.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year', min: new Date()};

		

		cCalPolAPV.buscarRegistros = function(){
				
				//Filtros Busqueda

			var filtroBusqueda = new Array();
			cCalPolAPV.gridMovimientos.filter(filtroBusqueda);
			};


		/******************************************************
		 * GRILLAS
		 * ***************************************************/
		cCalPolAPV.gridMovimientos = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6100",
							dataType	: "json"
					    }
	                },
					//filter: { field: "IdPersonaOrigen", operator: "eq", value: Number(ppr.IdPersona) },
					schema: {
						model: {
							fields: {
								PeriodoAPVA			: { type: "string"},
								PeriodoAPVB			: { type: "string"},
								PeriodoDCR			: { type: "string"},
								MontoPromedioAPVA	: { type: "number"},
								MontoPromedioAPVB	: { type: "number"},
								MontoPromedioDCR 	: { type: "number"},
								MontoRetenido	    : { type: "number"},
							}
						}
					}
				}
			}),
			columns: [
			    {field: "PeriodoAPVA"		,title: $translate.instant('UI_PERIODO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "MontoPromedioAPVA"	,title: $translate.instant('UI_MONTO_PROMEDIO_APV-A'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
			    {field: "MontoPromedioAPVB"	,title: $translate.instant('UI_MONTO_PROMEDIO_APV-B'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
			    {field: "MontoPromedioDCR"	,title: $translate.instant('UI_MONTO_PROMEDIO_DC-R'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
			   ],	
			sortable			: false,
			pageable			: true,
			autoBind			: true,
	    	filterable			: true,
			height				: 550,
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true
		};
			
	});
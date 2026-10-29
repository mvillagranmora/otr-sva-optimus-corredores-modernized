angular.module('ModuloPlataforma').controller('ConsultarPreciosInstrumentosCtrl',function($scope,bcsComunes){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);

	$scope.buscar = function() {
		var filters = new Array();
		
		var filters = {
				p_IdTipoInstrumento			:	$scope.idTipoInstrumento,
				p_IdBolsa					:	$scope.idBolsa,
				p_Fecha						:	util.dateSPdate($scope.fecha),
				p_IdCondicionDeLiquidacion	:	$scope.idCondicionDeLiquidacion
		}
		
		
		
		$scope.apiGrid.dataSource.setCallParameters(filters);	
	};
	$scope.RV 	= { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" };
	$scope.DV	= { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" };
	$scope.filtrosTipoInstrumento = {
			complejo : [$scope.RV,$scope.DV],
		  	logic: "and",
		  	filters: [					
				{ "field"   : "IdTipoProducto","operator": "neq", "value"   : "MEFW" },
				{
				    logic: "or",
				    filters: [{ "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "RV" }, { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "DV" }]
				},
				
		  	]}
	/*
	$scope.filtrosTipoInstrumento = {
			logic	: "or",
			RV : {field : "IdClasificacionTipoInstrumento", operator : "eq", value : "RV"},
			DV : {field : "IdClasificacionTipoInstrumento", operator : "eq", value : "DV"},
	}
	*/
	$scope.filtrosCondicionDeLiquidacion = {
			logic : "or",
			T0 : {field : "IdCondicionDeLiquidacion", operator : "eq", value : "T0"},
			T1 : {field : "IdCondicionDeLiquidacion", operator : "eq", value : "T1"},
			T2 : {field : "IdCondicionDeLiquidacion", operator : "eq", value : "T2"},
			T3 : {field : "IdCondicionDeLiquidacion", operator : "eq", value : "T3"}
	}
	
	$scope.fecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	
	$scope.configuracion = {
			allowCopy: true,
			userSettings		: true,
			navigatable			: true,
			gridCreated : function(grid){
				$scope.apiGrid =  grid;
			},
	      	filterable: true,
	      	height:500,
	      	resizable: true,
			scrollable: true,
	      	sortable: true,
	      	pageable			: true,		
			
			columns : [
				{field: "Nemotecnico",					title : "UI_NEMOTECNICO",				width: "100px"},
				{field: "IdMoneda",						title : "UI_MONEDA",					width: "75px"},
			    {field: "PuntaCompra",					title : "UI_PUNTA_COMPRA",				width: "100px",template : " <label> #:  (util.bcs_formato(PuntaCompra,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "PuntaVenta",					title : "UI_PUNTA_VENTA",				width: "100px",template : " <label> #:  (util.bcs_formato(PuntaVenta,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "PrecioMayor",					title : "UI_PRECIO_MAYOR",				width: "100px",template : " <label> #:  (util.bcs_formato(PrecioMayor,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "PrecioMenor",					title : "UI_PRECIO_MENOR",				width: "100px",template : " <label> #:  (util.bcs_formato(PrecioMenor,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "PrecioMedio",					title : "UI_PRECIO_MEDIO",				width: "100px",template : " <label> #:  (util.bcs_formato(PrecioMedio,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "PrecioApertura",				title : "UI_PRECIO_APERTURA",			width: "100px",template : " <label> #:  (util.bcs_formato(PrecioApertura,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "PrecioCierre",					title : "UI_PRECIO_MERCADO",			width: "100px",template : " <label> #:  (util.bcs_formato(PrecioCierre,'GEN','Precio',IdMoneda,'','')).valor #</label>"},
			    {field: "UnidadesTransadas",			title : "UI_VOLUMEN_TRANSADO",			width: "100px",template : " <label> #:  (util.bcs_formato(UnidadesTransadas,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			    {field: "MontoTransado",				title : "UI_MONTO_TRANSADO",			width: "100px",template : " <label> #:  (util.bcs_formato(MontoTransado,'GEN','Monto',IdMoneda,'','')).valor #</label>"},
			    {field: "Variacion",					title : "UI_VARIACION",					width: "75px",template : " <label> #:  (util.bcs_formato(Variacion,'GEN','Tasa',IdMoneda,'','')).valor #</label>"}
			           
			           ],
			dataSource : new bcs.DataSource({
	      		type : "call",
	      		definition : {
	      			
	      			callParameters      : [],
	    			callTransaction 	: "3542",
	    			callResultSet 		: "p_out_Instrumento",
	      			
	      			serverFiltering: false,
					serverPaging: false,
					serverSorting: false,
					schema: {
						model : {
							fields : {
								Nemotecnico 			: { type: "string"},
								PuntaCompra  			: { type: "number"},
								PuntaVenta  			: { type: "number"},
								PrecioMayor  			: { type: "number"},
								PrecioMenor  			: { type: "number"},
								PrecioMedio  			: { type: "number"},
								PrecioApertura  		: { type: "number"},
								PrecioCierre  			: { type: "number"},
								UnidadesTransadas  		: { type: "number"},
								MontoTransado  			: { type: "number"},
								Variacion  				: { type: "number"},
								IdMoneda  				: { type: "string"},
								Fecha  					: { type: "sysdate"},
				           		

							}
						}
					},
				}
	      	}),

	};	
});

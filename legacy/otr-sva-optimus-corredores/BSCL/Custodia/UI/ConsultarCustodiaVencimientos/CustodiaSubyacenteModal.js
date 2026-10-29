angular.module('ModuloCustodia').controller('CustodiaSubyacenteModalCtrl',
		function($scope,$translate,bcsComunes,bcsServicioPlataforma)
{	

	$scope.data = {};
	$scope.grid = {};
	var idVentana  = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	

	$scope.data.Cliente				= parametros.NombreCliente;
	$scope.data.Portafolio			= parametros.Portafolio;
	$scope.data.Ejecutivo			= parametros.NombreCompletoEjecutivo;
	$scope.data.Clasificacion		= parametros.IdClasificacionTipoInstrumento;
	$scope.data.Instrumento			= parametros.Nemotecnico;
	$scope.data.FechaVencimiento	= parametros.FechaVencimientoInstrumento;


	
	$scope.grid.confCustodiaSubyacente = {
		dataSource  : new bcs.DataSource({ 
						type 		: "odata",
						definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.							
							sort 				: [],
							filter				: { field: "FolioSubyacente", operator: "eq", value: Number(parametros.Folio)},
							transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1781",dataType : "json"}},
							schema : {
								model : {
									fields : {
										FolioCarteraRelacionada			: {type : "number"},									
										FolioSubyacente					: {type : "number"},
										IdClasificacionTipoInstrumento	: {type : "string"},
									   	IdTipoInstrumento				: {type : "string"},
									   	CodigoTipoInstrumento			: {type : "string"},
									   	NombreTipoInstrumento			: {type : "string"},
									   	IdInstrumento					: {type : "string"},
									   	Nemotecnico						: {type : "string"},
									   	IdEmisor						: {type : "string"},
									   	IdBoveda						: {type : "string"},									   	
										IdMonedaInstrumento				: {type : "string"},
										IdMonedaMontos					: {type : "string"},
									   	Cantidad						: {type : "number"},
									   	ValorRescate					: {type : "number"},
									   	PrecioSubyacente				: {type : "number"},
									   	TasaSubyacente					: {type : "number"},
									   	MontoATasaSubyacenteActual		: {type : "number"},
									   	PrecioMercadoActual				: {type : "number"},
										TasaMercadoActual				: {type : "number"},
									   	MontoATasaMercadoActual			: {type : "number"},								   
										EsFungible						: {type : "string"},
										INGPrecioSubyacente				: {type : "number"},
										INGTasaSubyacente				: {type : "number"},
										INGMontoATasaSubyacenteActual	: {type : "number"},
										MontoAPlazo						: {type : "number"},
										INGPrecioMercadoActual			: {type : "number"},
										INGTasaMercadoActual			: {type : "number"}
									}
								}
							}
						}
					  }),
		columns 	: [	 {field: "IdClasificacionTipoInstrumento",	title: 'UI_CLASIFICACION',		width: "40px",	groupable: true}
						,{field: "NombreTipoInstrumento",			title: 'UI_TIPO_INSTRUMENTO',	width: "100px",	groupable: true}
						,{field: "Nemotecnico",						title: 'UI_INSTRUMENTO',		width: "100px",	groupable: true}
						,{field: "IdEmisor",						title: 'UI_EMISOR',				width: "100px",	groupable: true}
						,{field: "IdMonedaInstrumento",				title: 'UI_MONEDA',				width: "40px"}
						,{field: "Cantidad",						title: 'UI_CANTIDAD',			width: "100px",	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "ValorRescate",					title: 'UI_VALOR_RESCATE',		width: "100px",	template : " <label> #:  (util.bcs_formato(ValorRescate,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "INGPrecioSubyacente",				title: 'UI_PRECIO_COMPRA',		width: "100px",	template : " <label> #:  (util.bcs_formato(INGPrecioSubyacente,'GEN','Precio',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "INGTasaSubyacente",				title: 'UI_TASA_COMPRA',		width: "100px",	template : " <label> #:  (util.bcs_formato(INGTasaSubyacente,'GEN','Tasa',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "INGMontoATasaSubyacenteActual",	title: 'UI_MONTO_INICIAL',		width: "100px",	template : " <label> #:  (util.bcs_formato(INGMontoATasaSubyacenteActual,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "MontoAPlazo",						title: 'UI_MONTO_PLAZO',	width: "100px",	template : " <label> #:  (util.bcs_formato(MontoAPlazo,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "INGPrecioMercadoActual",			title: 'UI_PRECIO_MERCADO',		width: "100px",	template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'GEN','Precio',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "INGTasaMercadoActual",			title: 'UI_TASA_MERCADO',		width: "100px",	template : " <label> #:  (util.bcs_formato(INGTasaMercadoActual,'GEN','Tasa',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "MontoATasaMercadoActual",			title: 'UI_MONTO_MERCADO',		width: "100px",	template : " <label> #:  (util.bcs_formato(MontoATasaMercadoActual,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
						,{field: "EsFungible",						title: 'UI_FUNGIBLE',			width: "100px"}		
					  ],
		gridCreated : function(g) { 
						$scope.grid.ApiCustodiaVencimientos = g;							
					  },
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		height 		: 340,
		selectable 	: "multiple row",
		autoBind 	: true,
		userSettings: true,
		allowCopy	: { delimeter: "\t" },
		excel 		: { allPages: true }
	};

});
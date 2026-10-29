+(function() {
	angular.module('ModuloAPV').controller('ConsultaDistribucionInversionSAAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes)
	{
		//Obtiene datos desde la ventana principal
		var distrib = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var parametrosDevueltos = {};
		var contadorHabilita = 0;
		var varIdPosicionInicial;
		var ventana = parametrosVentana.modoVentana;
		var row = '';
		var IdTipoInstrumento = parametrosVentana.IdTipoInstrumento;
		
		if(ventana === 'C'){
			var folioSAAPV 	= parametrosVentana.IdContratoSAAPV;
			var montototal 	= parametrosVentana.MontoTotal;
			var ParamDistri = parametrosVentana.paramVentanaDistribuir;
			var folioSelec	= parametrosVentana.FolioContratoSAAPV;
			parametrosDevueltos = parametrosVentana.paramVentanaDistribuir;
		
		}else{
			var folioSAAPV 	= parametrosVentana.IdContratoSAAPV;
			var montototal 	= parametrosVentana.MontoTotal;
			var ParamDistri = parametrosVentana.paramVentanaDistribuir;
			var folioSelec 	= parametrosVentana.FolioContratoSAAPV.FolioContratoSAAPV;
		}
		
		//datasources
		var dsDefDistrib = {
				type : "array",
				definition : {
		            serverFiltering : true, // Filtrado de datos en el servidor.
		            serverPaging    : true, // Paginado de los datos en el servidor.
		            serverSorting   : true, // Ordenamiento de los datos en el servidor.
		            pageSize        : 100,
		            sort 			: false,
		            schema 			: {
		               	model: {
		               		fields: {
		               			FolioContratoSAAPV  : {type: "strng"},
		               			IdInstrumento     	: {type: "number"},
								RazonSocial			: {type: "string"},
								Nemotecnico			: {type: "string"},
								MontoPesos			: {type: "number"},
								Porcentaje			: {type: "float"},
								IdEstado			: {type: "string"}
		               		}
		               	}
		            }
				}
			};
		
		var DistribInversion = new bcs.DataSource(dsDefDistrib);
		$scope.Distribucion = {
			dataSource: DistribInversion,
			columns: [
			     {field: "IdInstrumento"	, title: "UI_ID_INSTRUMENTO", width: "20px", align: "right", hidden: true},
			     {field: "RazonSocial"		, title: "UI_RAZON_SOCIAL", width: "50px", align: "left"},
			     {field: "Nemotecnico"		, title: "UI_SERIE", width: "15px", align: "left"},
			     {field: "MontoPesos"		, title: "UI_MONTO", width: "15px", align: "right", editable: false},
			     {field: "Porcentaje"		, title: "UI_PORCENTAJE", width: "15px", align: "right", editable: false}
			],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 210,
			selectable 	: "row",
			autoBind	: true,
			gridCreated : function(Distribucion){
				$.each(ParamDistri,function(key,value){
					DistribInversion.add({ 
						   FolioContratoSAAPV : value.FolioContratoSAAPV,
						   IdInstrumento 	  : value.IdInstrumento,
						   RazonSocial   	  : value.RazonSocial,
						   Nemotecnico		  : value.Nemotecnico,
						   MontoPesos		  : value.MontoPesos,
						   Porcentaje		  : value.Porcentaje,
						   IdEstado			  : value.IdEstado
						 });
				});
			}
		};
		
		//Al cerrar ventana para no perder datos
		bcsComunes.ventanaData(idVentana,parametrosDevueltos);
		
		if(ventana === 'C')
		{
			//bcsComunes.msg("warning","warning","folio contrato " + folioSelec.FolioContratoSAAPV);
			
			if(folioSelec !== undefined)
			{
				if(ParamDistri === undefined || ParamDistri.length === undefined )
				{
					var dsDefDistrib =
					{
						type : "odata",
						definition : 
						{
							serverFiltering : true, // Filtrado de datos en el servidor.
				            serverPaging    : true, // Paginado de los datos en el servidor.
				            serverSorting   : true, // Ordenamiento de los datos en el servidor.
				            pageSize        : 100,
				            sort 			: false,
				            transport		: 
							{
								read: 
								{
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6010",
									dataType	: "json"
								}
							},
				            schema: 
							{
				               	model: 
								{
				                	fields: 
									{
										FolioContratoSAAPV  : {type: "strng"},
				                		IdInstrumento     	: {type: "number"},
										RazonSocial			: {type: "string"},
										Nemotecnico			: {type: "string"},
										MontoPesos			: {type: "number"},
										Porcentaje			: {type: "float"},
										IdEstado			: {type: "string"}
				                	}
				                }
				            }
						}
					};
					
					var DistribInversion = new bcs.DataSource(dsDefDistrib);
					$scope.Distribucion = 
					{
						dataSource: DistribInversion,
						columns:
						[
						     {field: "IdInstrumento"	, title: "UI_ID_INSTRUMENTO", width: "20px", align: "right", hidden: true},
						     {field: "RazonSocial"		, title: "UI_RAZON_SOCIAL", width: "50px", align: "left"},
						     {field: "Nemotecnico"		, title: "UI_SERIE", width: "15px", align: "left"},
						     {field: "MontoPesos"		, title: "UI_MONTO", width: "15px", align: "right", editable: false},
						     {field: "Porcentaje"		, title: "UI_PORCENTAJE", width: "15px", align: "right", editable: false}
						],
						sortable	: true,
						pageable	: true,
						filterable	: false,
						height		: 210,
						selectable 	: "row",
						autoBind	: false,
						gridCreated : function(grid)
						{
			    			var filter = Array(); 
			    			filter.push({ field: "IdContratoSAAPV", operator: "eq", value: folioSAAPV });
			    			DistribInversion.filter(filter);
					    }
					};
				}
				else
				{
					$.each(ParamDistri,function(key,value)
					{
						DistribInversion.add
						({ 
							FolioContratoSAAPV 	: value.FolioContratoSAAPV,
							IdInstrumento 	  	: value.IdInstrumento,
							RazonSocial   	  	: value.RazonSocial,
							Nemotecnico		  	: value.Nemotecnico,
							MontoPesos		  	: value.MontoPesos,
							Porcentaje		  	: value.Porcentaje,
							IdEstado			: value.IdEstado
						});
					});
				}
			}
		}
	});
}());
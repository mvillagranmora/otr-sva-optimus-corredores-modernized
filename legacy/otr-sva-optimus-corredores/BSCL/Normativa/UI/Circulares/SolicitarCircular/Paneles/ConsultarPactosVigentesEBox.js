angular.module('ModuloNormativa').controller('PactosVigentesEBoxCtrl', function($scope, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsServiciosNormativa,$rootScope) {
	
	$scope.data ={};
	var iOPV = this;
	iOPV.grilla = {};
	iOPV.fn = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.idVentana = idVentana;
	bcsComunes.ventanaAutoHeight($scope.idVentana);
	var fechaSeleccionada = $rootScope.fechaSeleccionadaParaGrilla;

	var dsGrillaPactosVigentesEBox = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true,
			serverPaging     : true,
			serverSorting    : true,
			pageSize         : 100,
			sort 				: { field: "NumeroOperacion", dir: "desc" },
			transport 			: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=2470", 
					dataType : "json",
				}
			},
			
			schema : {
				model: {
					fields: {
						TipoRegistro								: { type: "string"},
						TipoCompromiso			                    : { type: "string"},
						IndicadorCobertura							: { type: "number"},
						FechaOperacion								: { type: "date"},
						FechaOperacionCadena						: { type: "string"},
						NombreCliente								: { type: "string"},
						RutCliente									: { type: "string"},
						Relacionado									: { type: "string"},
						Mercado										: { type: "string"},
						Nemotecnico 								: { type: "string"},
						Emisor										: { type: "string"},
						TipoReajusteInstrumento						: { type: "string"},
						Unidades						            : { type: "number"},
						NumeroOperacion						        : { type: "number"},
						FechaVencimientoPacto						: { type: "string"},
						FechaVencimientoInstrumento					: { type: "string"},									
						MontoPacto									: { type: "number"},
						TipoMonedaPacto 							: { type: "string"},
						MontoOperacion 								: { type: "number"},
						TasaPacto								    : { type: "number"}
					} 
				}
			},
		}
	});
			
   	//GRILLA PRINCIPAL
	iOPV.grilla.confBasicaPactosVigentesEBox = {
		
		dataSource			: dsGrillaPactosVigentesEBox,
		columns				: [
			{field: "TipoRegistro",						title: 'UI_TIPO_REGISTRO', width: "110px", attributes:{style:"text-align:left;"}	},		         
			{field: "TipoCompromiso",					title: 'UI_TIPO_COMPROMISO', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},		         			
			{field: "IndicadorCobertura", 				title: 'UI_INDICADOR_COBERTURA', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "FechaOperacionCadena", 			title: 'UI_FECHA_OPERACION' ,width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "NombreCliente", 					title: 'UI_NOMBRE_CLIENTE',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "RutCliente",				    	title: 'UI_RUT', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Relacionado",				    	title: 'UI_RELACIONADO',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Mercado",	                    	title: 'UI_MERCADO', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Nemotecnico",	                	title: 'UI_NEMOTECNICO',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Emisor",			            	title: 'UI_EMISOR', 	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "TipoReajusteInstrumento",			title: 'UI_TIPO_REAJUSTE_INSTRUMENTO', 	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "Unidades",	 						title: 'UI_PACTOS_UNIDADES',	 width: "75px", attributes: { style: "text-align:center;" } },
			{field: "NumeroOperacion",	 				title: 'UI_NUMERO_OPERACION',	 width: "75px", attributes: { style: "text-align:center;" }, headerAttributes:{style:"text-align: left;"}  },
			{field: "FechaVencimientoPacto",			title: 'UI_FECHA_VENCIMIENTO_PACTO',	 width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
			{field: "FechaVencimientoInstrumento",		title: 'UI_FECHA_VENCIMIENTO_INSTRUMENTO',	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "MontoPacto",						title: 'UI_MONTO_PACTO',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, template: function(dataItem) {return kendo.toString(dataItem.MontoPacto, "n0") } 	},
			{field: "TipoMonedaPacto",					title: 'UI_TIPO_MONEDA_PACTO',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "MontoOperacion",					title: 'UI_MONTO_INICIAL',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, template: function(dataItem) {return kendo.toString(dataItem.MontoOperacion, "n0") } },
			{field: "TasaPacto",						title: 'UI_TASA_CARTERA_PACTO',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, template: function(dataItem) {return kendo.toString(dataItem.TasaPacto, "n4") } },

		],
		sortable			: true,
		filterable 			: true,
		userSettings 		: true,
		excel 				: { allPages : true },
		pageable			: true,
		height				: 450,
		userSettings		: true,
		navigatable			: true,
		selectable 			: "multiple row",

		gridCreated : function(grid) {
			grillaCircularEBox = grid;
			aplicarFiltroInicial(grillaCircularEBox);
		}
	};
	
	function aplicarFiltroInicial(grid) {
		var fechaSeleccionada = $rootScope.fechaSeleccionadaParaGrilla;
		if (fechaSeleccionada) {
			grid.dataSource.filter({
				field: "FechaOperacion",
				operator: "le",
				value: fechaSeleccionada
			});
		}
	}

	$scope.cerrarVentana = function(){
		bcsComunes.ventanaCerrar($scope.idVentana);
	};

});
	
    
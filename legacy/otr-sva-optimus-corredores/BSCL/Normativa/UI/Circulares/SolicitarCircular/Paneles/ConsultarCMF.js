angular.module('ModuloNormativa').controller('CircularCarteraCMFCtrl', function($scope, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsServiciosNormativa,$rootScope) {
	
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.idVentana = idVentana;

	var fechaSeleccionada = $rootScope.fechaSeleccionadaParaGrilla 
	           
    
	var dsGrillaCuadraturaCircularCMF = new bcs.DataSource({
	               type : "odata",
	               definition : {
	                   serverFiltering 	: true,
	                   serverPaging     : true,
	                   serverSorting    : true,
	                   pageSize         : 100,
					  
	                   transport 		: {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010104",
								dataType : "json"
							}
	                   },
	                   schema : {
				    		model: {
				    			fields: {
									Fecha												: { type: "date"},
				    				IdClasificacionTipoInstrumentoModificado			: { type: "number"},
				    				Nemotecnico											: { type: "string"},
									ISIN												: { type: "string"},
									SituacionInstrumento								: { type: "string"},
									RutCliente											: { type: "string"},
									Digito											    : { type: "string"},
									NombrePersona										: { type: "string"},
									CodigoPais 											: { type: "string"},
									IdTipoInstrumento									: { type: "string"},
									FechaVencimiento									: { type: "date"},
									ClasificacionRiesgoInstrumento						: { type: "string"},
									TendenciaClasificacionriesgo						: { type: "string"},
									CantidadUnidades								    : { type: "string"},
									IdMonedaPrincipal									: { type: "string"},									
									Tir											        : { type: "number"},
									CodigoValorizacion 								: { type: "string"},
									BaseTasa 										: { type: "number"},
									ValorizacionCierre								: { type: "string"},
									CodigoPaisEmisionInstrumento					: { type: "string"},
									Relacionado                                     : { type: "string"},
									FuentePrecios 									: { type: "string"},						
									Duration 										: { type: "string"},
									IdEstado 										: { type: "string"}

									
				        		} 
				        	}
					},
								}
			});
			
   	//GRILLA PRINCIPAL
	$scope.confBasicaCuadraturaCircularCMF = {
		
		dataSource			: dsGrillaCuadraturaCircularCMF,
		columns				: [
			{field: "Fecha",					title: 'UI_FECHA',						width: "110px", attributes:{style:"text-align:left;"}	},		         
			{field: "IdClasificacionTipoInstrumentoModificado",						title: 'UI_INSTRUMENTO', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},		         			
			{field: "Nemotecnico", 				title: 'UI_NEMOTECNICO', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "ISIN", 				title: 'UI_ISIN',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "SituacionInstrumento", 				title: 'UI_SITUACION_INSTRUMENTO',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "RutCliente",				title: 'UI_RUT',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Digito",				title: 'UI_DIGITO',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "NombrePersona",	title: 'UI_NOMBRE',	 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "CodigoPais",	title: 'UI_CODIGO_PAIS',	 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "IdTipoInstrumento",			title: 'UI_TIPO_INSTRUMENTO', 	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "FechaVencimiento",						title: 'UI_FECHA_VENCIMIENTO', 	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "ClasificacionRiesgoInstrumento",	 					title: 'UI_CLASIFICACION_RIESGO',	 width: "75px", attributes: { style: "text-align:center;" } },
			{field: "TendenciaClasificacionriesgo",	 					title: 'UI_TENDENCIA_CLASIFICACION_RIESGO',	 width: "75px", attributes: { style: "text-align:center;" }, headerAttributes:{style:"text-align: left;"}  },
			{field: "CantidadUnidades",	 					title: 'UI_CANTIDAD_UNIDADES',	 width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
			{field: "IdMonedaPrincipal",					title: 'UI_MONEDA_PRINCIPAL',	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "Tir",					title: 'UI_TIR',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "CodigoValorizacion",					title: 'UI_C._VALORIZACION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "BaseTasa",					title: 'UI_BASE_TASA',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "ValorizacionCierre",					title: 'UI_VAL._CIERRE',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "CodigoPaisEmisionInstrumento",					title: 'UI_C._PAIS_EMISION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Relacionado",					title: 'UI_RELACIONADO',	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "FuentePrecios",					title: 'UI_FUENTE_PRECIOS',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Duration",					title: 'UI_DURACION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			
					
				
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
        rowColorBackground: function(row) {
            switch(row.get("IdEstado")) {
                case 'DES':
					if(row.get("CantidadUnidades") < 0){
						return "red";
					}else{
						return "yellow";
					}
                    
				case 'NEG':
					if(row.get("CantidadUnidades") < 0){
						return "red";
					}
            }
        },
		gridCreated : function(grid) {
			grillaCircularCMF = grid;
			aplicarFiltroInicial(grillaCircularCMF);
		}
	};
	

	function aplicarFiltroInicial(grid) {
		var fechaSeleccionada = $rootScope.fechaSeleccionadaParaGrilla;
		if (fechaSeleccionada) {
			grid.dataSource.filter({
				field: "Fecha",
				operator: "eq",
				value: fechaSeleccionada
			});
		}
	}

});
	

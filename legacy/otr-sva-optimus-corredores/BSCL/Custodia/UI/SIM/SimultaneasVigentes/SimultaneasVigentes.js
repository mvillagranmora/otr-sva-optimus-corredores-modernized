angular.module('ModuloCustodia').controller('SimultaneasVigentesCtrl',
		function($scope,$state,$translate,$resource,$timeout,bcsComunes,
				 bcsServicioPlataforma,bcsOperacionesComunes)
{	
	$scope.kngFecha 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.FechaConsultaOpt = {/*max:util.dateInicioDia($scope.SesionFechaSistema)*/};
	$scope.HabilitarBotonAnticipar = true;
	$scope.FechaActual = true;

	$scope.oClientePortafolio ={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oConfiguracionTipoOperacion={
		configuracion : {},
		filtros : {
			EstadoTipoOperacionTipoInstrumento : { "field"   : "EstadoTipoOperacionTipoInstrumento","operator": "eq", "value" : "ACT" },
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "SIM"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};	
	

	//-------------------------------------
	// CONFIGURACION DE GILLA
	//-------------------------------------	
	var dsSimultaneasVigentes = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
				pageSize		: 200,
				callTransaction : "55670002",
				callResultSet   : "P_OUT_TABLA",
				schema 			: {
					model : {
						fields : {                                                                                                                                                             
							FolioCartera					: {type: "number"},
							AccionOperacion					: {type: "string"},
							AccionOperacionPlazo			: {type: "string"},
							FechaInversion					: {type: "sysdate"},
							FechaVencimientoOperacion		: {type: "sysdate"},
							MontoCompra 					: {type: "number"},
							MontoAPlazo		 				: {type: "number"},
							EGRFechaOperacion				: {type: "sysdate"},
							INGCantidad						: {type: "number"},
							INGPrecioCompra					: {type: "number"},
							INGTasaCompra 					: {type: "number"},
							INGMontoATasaCompraActual		: {type: "number"},
							INGMontoAPlazo					: {type: "number"},
							INGFechaLiquidacion 			: {type: "sysdate"},
							EGRFechaLiquidacion 			: {type: "sysdate"},
							suby_Nemotecnico				: {type: "string"},
							FolioComprobante				: {type: "number"},
							suby_NombreTipoInstrumento		: {type: "string"},
							Cliente							: {type: "string"},
							Proposito						: {type: "string"},
							CodigoPortafolio 				: {type: "string"},
							FechaLiquidacion				: {type: "sysdate"},
							IdEstado						: {type: "string"},
							PaloNormal						: {type: "string"},
							PaloPlazo						: {type: "string"},
							NombreCorredora					: {type: "string"},
							IdCorredoraContraparte 			: {type: "number"},
							IdBolsa 						: {type: "string"},
							NombreEjecutivo 				: {type: "string"},
							FechaLiquidacionMovtoContado	: {type: "sysdate"},
							PrecioInicial 					: {type: "number"},
							PrecioAPlazo					: {type: "number"},
							suby_INGPrecioMercadoActual 	: {type: "number"},
							PrecioActualCalculado 			: {type: "number"},
							NumeroDias						: {type: "string"},
							INGMontoInteresCompraAnterior							: {type: "number"},
							INGMontoInteresCompraAnteriorMonedaInstrumento			: {type: "number"},
							INGMontoInteresCompraAnteriorMonedaAdicional			: {type: "number"},
							INGMontoInteresCorridoCompraActual						: {type: "number"},
							INGMontoInteresCorridoCompraActualMonedaInstrumento		: {type: "number"},
							INGMontoInteresCorridoCompraActualMonedaAdicional		: {type: "number"},
							INGMontoInteresCompraProximo							: {type: "number"},
							INGMontoInteresCompraProximoMonedaInstrumento			: {type: "number"},
							INGMontoInteresCompraProximoMonedaAdicional				: {type: "number"},
							INGMontoInteresMercadoAnterior							: {type: "number"},
							INGMontoInteresMercadoAnteriorMonedaInstrumento			: {type: "number"},
							INGMontoInteresMercadoAnteriorMonedaAdicional			: {type: "number"},
							INGMontoInteresCorridoMercadoActual						: {type: "number"},
							INGMontoInteresCorridoMercadoActualMonedaInstrumento	: {type: "number"},
							INGMontoInteresCorridoMercadoActualMonedaAdicional		: {type: "number"},
							INGMontoInteresMercadoProximo							: {type: "number"},
							INGMontoInteresMercadoProximoMonedaInstrumento			: {type: "number"},
							INGMontoInteresMercadoProximoMonedaAdicional			: {type: "number"},
							FlagDeterioro											: {type: "string"},
							PorcentajeDeterioro 									: {type: "number"},
							Deterioro												: {type: "number"},
							Fase													: {type: "number"},
							DocumentoIdentidad										: {type: "string"},
							NombreLargoCliente										: {type: "string"}
						}
					}  
				}                           				
		}
	});
	
	
	$scope.columnasSimultaneasVigentes = function() {
		return  [
			{field: "FolioCartera", title: "UI_FOLIO_CARTERA", width: "80px", align: "right", sortable : false, groupable: true }
			,{field: "PaloNormal", title: "UI_TRANSACCION", width: "80px", align: "right", sortable : false, groupable: true }
			,{field: "PaloPlazo", title: "UI_TRANSACCION_A_PLAZO", width: "80px", align: "right", sortable : false, groupable: true }
			//,{field: "AccionOperacion", title: "UI_TIPO_OPERACION", width: "70px", align: "left", sortable : true, groupable: true } 
			,{field: "AccionOperacionPlazo", title: "UI_TIPO_OPERACION", width: "100px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
			,{field: "FechaInversion", title: "UI_FECHA_OPERACION", width: "80px", align: "center", sortable : true, groupable: true }
			,{field: "FechaLiquidacionMovtoContado", title: "UI_FECHA_INICIO", width: "80px", align: "center", sortable : true, groupable: true }
			,{field: "FechaVencimientoOperacion", title: "UI_FECHA_VENCIMIENTO", width: "80px", align: "center", sortable : true, groupable: true }
			,{field: "NumeroDias", title: "UI_NUMERO_DIAS", width: "85px", align: "center", sortable : true, groupable: true }
			,{field: "INGCantidad", title: "UI_CANTIDAD", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "PrecioInicial", title: "UI_PRECIO_INICIAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioInicial,'GEN','Precio','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "PrecioActualCalculado", title: "UI_PRECIO_ACTUAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioActualCalculado,'GEN','Precio','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "PrecioAPlazo", title: "UI_PRECIO_PLAZO", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioAPlazo,'GEN','Precio','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "INGTasaCompra", title: "UI_TASA", width: "60px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGTasaCompra,'GEN','Tasa','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "MontoCompra", title: "UI_MONTO_INICIAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "INGMontoATasaCompraActual", title: "UI_MONTO_ACTUAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "MontoAPlazo", title: "UI_MONTO_PLAZO", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoAPlazo,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "INGMontoInteresCompraProximo", title: "UI_INTERES_DIARIO", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoInteresCompraProximo,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "INGMontoInteresCorridoCompraActual", title: "UI_INTERESES_CORRIDO", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoInteresCorridoCompraActual,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }					
			,{field: "suby_Nemotecnico", title: "UI_NEMOTECNICO", width: "120px", align: "left", sortable : true, groupable: true }
			,{field: "FolioComprobante", title: "UI_FOLIO_COMPROBANTE", width: "80px", align: "right", sortable : false, groupable: true }
			,{field: "suby_NombreTipoInstrumento", title: "UI_TIPO_INSTRUMENTO_ABR", width: "100px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
			,{field: "Cliente", title: "UI_CLIENTE", width: "150px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
			,{field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "55px", align: "left", sortable : true, groupable: true }
			,{field: "NombreEjecutivo", title: "UI_AGENTE", width: "120px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"}  }
			,{field: "NombreCorredora", title: "UI_CORREDOR_CONTRAPARTE", width: "150px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
			,{field: "IdBolsa", title: "UI_BOLSA", width: "120px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"}  }
			,{field: "FlagDeterioro", 					title: 'UI_FLAG_DETERIORO', 				width: "100px", groupable: true, attributes:{style:"white-space:nowrap;"}}
			,{field: "PorcentajeDeterioro", 			title: 'UI_PORCENTAJE_DETERIORO', 			width: "100px", groupable: true, attributes:{style:"white-space:nowrap;"}}
			,{field: "Deterioro", 						title: 'UI_DETERIORO', 						width: "100px", template : " <label> #:  (util.bcs_formato(Deterioro,'GEN', 'Monto', '" +$scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
			,{field: "Fase", 							title: 'UI_FASE',	 						width: "100px", groupable: true, attributes:{style:"white-space:nowrap;"}}
			,{field: "DocumentoIdentidad", 				title: "UI_DOCUMENTO_IDENTIDAD",			width: "80px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
			,{field: "NombreLargoCliente", 				title: "UI_NOMBRE_CLIENTE",					width: "180px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
		];
	};
	
	
	$scope.confBasicaSimultaneasVigentes = {
		dataSource		: dsSimultaneasVigentes,
		columns			: $scope.columnasSimultaneasVigentes(),		
		sortable		: true,
		pageable		: true,
		filterable 		: true,
		selectable 		: "row",
		reorderable		: true,
		userSettings 	: true,	
		groupable		: true,
		height			: 495,
		allowCopy		: { delimeter: "\t" },
		resizable		: true,		
	    autoBind		: false,	    
	    scrollable		: true,
		excel : {
	        allPages: true,
	    }
	};

	//BOTONES
	$scope.buscar = function()
	{		
		var parametrosConsulta = {};

		parametrosConsulta.p_IdEmpresa					= $scope.$root.SesionIdEmpresa;
		parametrosConsulta.p_IdUsuario					= $scope.$root.SesionIdUsuario;
		parametrosConsulta.p_IdModulo					= 'CUS';
		parametrosConsulta.p_FechaConsulta				= util.dateSPdate($scope.kngFecha);
		parametrosConsulta.p_TipoFecha					= 'ACT';
		parametrosConsulta.p_IdCliente					= 0;
		parametrosConsulta.p_IdPortafolio				= 0;
		parametrosConsulta.p_IdTipoOperacion			= "";
		parametrosConsulta.p_IdInstrumentoSubyacente	= 0;
		parametrosConsulta.p_PaloNormal					= "";
		parametrosConsulta.p_PaloPlazo					= "";

		if ($scope.oClientePortafolio.seleccionado.IdPersona){
			parametrosConsulta.p_IdCliente = Number($scope.oClientePortafolio.seleccionado.IdCliente || 0);
		}		
		if ($scope.oClientePortafolio.seleccionado.IdPortafolio){			
			parametrosConsulta.p_IdPortafolio = 	Number($scope.oClientePortafolio.seleccionado.IdPortafolio || 0);
		}			
		if ( $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion && $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion != 0){		
			parametrosConsulta.p_IdTipoOperacion = $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion;
		}
		if ($scope.instrumento !== undefined){
			parametrosConsulta.p_IdInstrumentoSubyacente = Number($scope.instrumento.IdInstrumento || 0);
		}
		if ($scope.Transaccion !== undefined){
			if ($scope.Transaccion > 0){
				parametrosConsulta.p_PaloNormal	= $scope.Transaccion;
				parametrosConsulta.p_PaloPlazo	= $scope.Transaccion;
			}
		}
				
		if(util.dateInicioDia($scope.SesionFechaSistema).toLocaleDateString() == util.dateInicioDia($scope.kngFecha).toLocaleDateString()){
			$scope.HabilitarBotonAnticipar = true;
			parametrosConsulta.p_TipoFecha = 'ACT';
		}else if(util.dateInicioDia($scope.SesionFechaSistema).getTime() < util.dateInicioDia($scope.kngFecha).getTime()){
			$scope.HabilitarBotonAnticipar = false;
			parametrosConsulta.p_TipoFecha = 'ACT';
		}else {
			$scope.HabilitarBotonAnticipar = false;
			parametrosConsulta.p_TipoFecha = 'ANT';
		}			
		dsSimultaneasVigentes.setCallParameters(parametrosConsulta);
		
	};
	$scope.anticipoSimultanea = function()
	{
		if($scope.HabilitarBotonAnticipar != null && $scope.HabilitarBotonAnticipar == true){
			if ($scope.seleccionSimultaneasVigentes != undefined) {
				var aux = $.extend(true, {}, $scope.seleccionSimultaneasVigentes);
				bcsComunes.ventanaAbrir("AnticipoSimultanea", aux, true, false);
			}
			else {
				bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			}
		}
		
	};
	$scope.anulacionSimultanea = function(simultaneaSeleccionada)
	{		
		bcsComunes.msg("warning","info",'Pendiente de desarrollo');
	};
	$scope.limpiar = function()
	{					
		$scope.oConfiguracionTipoOperacion.idActual = 0;
		$scope.buscarinstrumento = "-1";
		$scope.filtros.IdPortafolio = false;
		$scope.TransaccionObj = "";
		$scope.Transaccion = undefined;
		
		dsSimultaneasVigentes.removeAll();
	};
	
});


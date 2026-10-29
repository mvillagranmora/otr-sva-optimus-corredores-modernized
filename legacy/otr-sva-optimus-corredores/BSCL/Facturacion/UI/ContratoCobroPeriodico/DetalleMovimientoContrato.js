angular.module('ModuloFacturacion').controller('DetalleMovimientoComprobanteCtrl',
		function($scope,$uibModal,$translate,bcsComunes){
		
	var bModifMvtoMod = this;	
	
	//Recupera parametros desde la ventana recibida
    var idVentana = bcsComunes.ventanaWinId ($scope);   
    $scope.parametros 	= bcsComunes.ventanaParams(idVentana);
	
	bModifMvtoMod.IdComprobante = $scope.parametros.IdComprobante;
	
	//setTimeout(function(){$scope.focus();},1000);
	
	$scope.getSchemaMovimiento = function(){
		return {
			model: {
				fields : {
					IdContratoCobroPeriodico					: { type: "number"},
					ContratoCobroPeriodico                      : { type: "string"},
					IdCliente									: { type: "number"},
					IdPortafolio								: { type: "number"},
					DocumentoIdentidad							: { type: "string"},
					NombreLargoCliente							: { type: "string"},
					IdTipoContratoCobroPeriodico				: { type: "number"},
					TipoContratoCobroPeriodico					: { type: "string"},
					IdFormatoContratoCobroPeriodico				: { type: "number"},
					FormatoContratoCobroPeriodico				: { type: "string"}
				}
			}
		};
	};
	
	$scope.getSchemaDetalle = function(){
		return {
			model: {
				fields : {
					IdContratoCobroPeriodicoDetalle		: { type: "number"},
					IdContratoCobroPeriodico			: { type: "number"},
					IdClasificacionTipoInstrumento		: { type: "string"},
					ClasificacionTipoInstrumento		: { type: "string"},
					IdTipoInstrumento					: { type: "string"},
					NombreTipoInstrumento				: { type: "string"},
					IdConceptoCobroPeriodico			: { type: "string"},
					NombreCobro							: { type: "string"},
					Concepto							: { type: "string"},
					EsPorRangos							: { type: "string"},
					MontoMinimo							: { type: "number"},
					MontoMaximo							: { type: "number"},
				}
			}
		};
	};
	
	$scope.getSchemaCobros = function(){
		return {
			model: {
				fields : {
					IdCobroPeriodico			: { type: "number"},
					IdCobro						: { type: "string"},
					NombreCobro					: { type: "string"},
					IdContratoCobroPeriodico	: { type: "number"},
					MontoMinimoACobrar			: { type: "number"},
					MontoMaximoACobrar			: { type: "number"},
					TipoCobroPeriodico			: { type: "string"},
					PorRango					: { type: "string"}
				}
			}
		};
	};

	$scope.getSchemaCobrosDetalle = function(){
		return {
			model: {
				fields : {
					IdCobroPeriodicoDetalle		: { type: "number"},
					IdCobroPeriodico			: { type: "number"},
					MontoCobroPeriodico			: { type: "number"},
					PorcentajeCobroPeriodico	: { type: "number"},
					MontoDesde					: { type: "number"},
					MontoHasta					: { type: "number"}
				}
			}
		};
	};

	
	$scope.MvtoContratoDatasource = new bcs.DataSource({
		                   type: "odata",
		                   definition 	: {
							                  serverFiltering 	: true, // Filtrado de datos en el servidor.
							                  serverPaging 		: true, // Paginado de los datos en el servidor.
							                  serverSorting 	: true, // Ordenamiento de los datos en el servidor.
							                  sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
							                  filter            : { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
							                                    
							                  transport 		: { read: {
														                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3042",
														                dataType: "json"
													 	                  }
												                  },
							                  schema            : $scope.getSchemaMovimiento()
						                  }
	}); 
		
		
	$scope.MvtoContratoDatasource.query().then(function(){
		$scope.$apply(function() {
			bModifMvtoMod.oMovimientoComprobante 	= $scope.MvtoContratoDatasource.data();
		});
	});


	$scope.confBasicaMovimientoContrato = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "IdContratoCobroPeriodicoDetalle", dir: "asc" } ],
									transport 		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=136",dataType : "json"}},
									schema 			: $scope.getSchemaDetalle(),
								}
							  }),

		columns				: [
								 {field : "NombreCobro", 			title:"UI_COBRO", 	width: "100px"}								
								 ,{field : "ClasificacionTipoInstrumento", 			title:"UI_CLASIFICACION_TIPO_INSTRUMENTO", 	width: "100px"}
								,{field : "IdConceptoCobroPeriodico", 				title:"UI_CODIGO_CONCEPTO",	 			width: "100px"}
								,{field : "Concepto", 								title:"UI_CONCEPTO", 					width: "100px"}
								,{field : "EsPorRangos", 								title:"UI_POR_RANGOS", 					width: "100px"}
								,{field : "MontoMinimo", 								title:"UI_MONTO_MINIMO", 					width: "100px"}
								,{field : "MontoMaximo", 								title:"UI_MONTO_MAXIMO", 					width: "100px"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter({ field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)}); },  
	};

	$scope.confBasicaMovimientoContratoCobros = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "IdCobroPeriodico", dir: "asc" } ],
									transport 		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=137",dataType : "json"}},
									schema 			: $scope.getSchemaCobros(),
								}
							  }),

		columns				: [
								 {field : "NombreCobro", 		title:"UI_TIPO", 				width: "200px"}
								,{field : "MontoMinimoACobrar", title:"UI_MONTO_MINIMO", 		width: "100px", 	template : " <label> #: util.formateaNumero(MontoMinimoACobrar,0) #</label>"}
								,{field : "MontoMaximoACobrar", title:"UI_MONTO_MAXIMO", 		width: "100px", 	template : " <label> #: util.formateaNumero(MontoMaximoACobrar,0) #</label>"}
								,{field : "TipoCobroPeriodico",	title:"UI_TIPO_COBRO", 			width: "100px"}
								,{field : "PorRango",	 		title:"UI_RANGOS",				width: "80px"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter([{ field: "IdContratoCobroPeriodico", operator: "eq", value: Number(bModifMvtoMod.IdContratoCobroPeriodico)}]); },  
	};

	$scope.confBasicaMovimientoCobrosDetalle = function(dataItem) 
	{
		return {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.						
									sort 				: [ { field: "IdCobroPeriodicoDetalle", dir: "asc" } ],
									transport 			: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=141",dataType : "json"}},
									schema 				: $scope.getSchemaCobrosDetalle(),
									filter				: [{field: "IdCobroPeriodico", operator: "eq", value: Number(dataItem.IdCobroPeriodico)}]
								}
							  }),

		columns				: [
								 {field : "MontoDesde", 				title:"UI_MONTO_DESDE", 		width: "100px", 	template : " <label> #: util.formateaNumero(MontoDesde,0) #</label>"}
								,{field : "MontoHasta", 				title:"UI_MONTO_HASTA", 		width: "100px", 	template : " <label> #: util.formateaNumero(MontoHasta,0) #</label>"}
								,{field : "MontoCobroPeriodico", 		title:"UI_MONTO", 				width: "100px", 	template : " <label> #: util.formateaNumero(MontoCobroPeriodico,2) #</label>"}
								,{field : "PorcentajeCobroPeriodico", 	title:"UI_PORCENTAJE", 			width: "100px", 	template : " <label> #: util.formateaNumero(PorcentajeCobroPeriodico,2) #</label>"}
							  ],		
		sortable			: true,
		pageable			: true,
		autoBind			: true,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true
		}
	};

	let responsiveWindow = $("div#FORM_DetalleMovimientoContrato[ventanaid='" + idVentana + "']");
	responsiveWindow.parent().css("height", "auto");

});

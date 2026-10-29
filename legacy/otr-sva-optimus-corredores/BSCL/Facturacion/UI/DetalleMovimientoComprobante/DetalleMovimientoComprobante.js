angular.module('ModuloFacturacion').controller('DetalleMovimientoComprobanteCtrl',
		function($scope,$uibModal,$translate,bcsComunes,$rootScope){
	
	
	var bModifMvtoMod = this;	
	
	//Recupera parametros desde la ventana recibida
    var idVentana = bcsComunes.ventanaWinId ($scope);   
    $scope.parametros 	= bcsComunes.ventanaParams(idVentana);
	
	bModifMvtoMod.IdMovimientoComprobante = $scope.parametros.IdMovimientoComprobante;
	
	//setTimeout(function(){$scope.focus();},1000);
	
	$scope.getSchemaMovimiento = function(){
		return {
			model: {
				fields : {
					NroLinea					: { type: "number"},
					FechaLiquidacion			: { type: "sysdate"},
					FechaMovimientoComprobante	: { type: "sysdate"},
					NombreTipoInstrumento		: { type: "string"},
					NemotecnicoInstrumento		: { type: "string"},
					Cantidad					: { type: "number"},
					Precio						: { type: "number"},					
					NombreTipoProducto			: { type: "string"},
					NombreTipoOperacion			: { type: "string"},
					IdCobro						: { type: "string"},
					NombreCobro					: { type: "string"},
					FormaCobro					: { type: "string"},
					Porcentaje					: { type: "number"},
					Monto						: { type: "number"},
					MontoLiquidacion			: { type: "number"},
					NombreMedioDePago			: { type: "string"},
					IdMoneda					: { type: "string"},
					NombreTipoCuentaBancaria	: { type: "string"},
					NombreCuentaBancaria		: { type: "string"},
					NumeroCuentaBancaria		: { type: "number"},
				}
			}
		};
	};
	
	$scope.getSchemaDetalle = function(){
		return {
			model: {
				fields : {
					NombreTipoInstrumento				: { type: "string"},
					NombreClasificacionTipoInstrumento	: { type: "string"},
					NemotecnicoInstrumento				: { type: "string"},
					Cantidad							: { type: "number"},
					Precio								: { type: "number"},					
					Monto								: { type: "number"},
				}
			}
		};
	};
	
	$scope.getSchemaCobros = function(){
		return {
			model: {
				fields : {
					IdCobro								: { type: "string"},
					NombreCobro							: { type: "string"},
					NombreFormaCobro					: { type: "string"},
					FormaCobro							: { type: "string"},
					Porcentaje							: { type: "number"},
					IdMonedaMovimientoComprobanteCobro 	: { type: "string"},
					MontoMovimientoComprobanteCobro		: { type: "number"},
					IdMonedaLiquidacionCobro    		: { type: "string"}, 
					MontoLiquidacionCobro 				: { type: "number"},
				}
			}
		};
	};

	$scope.getSchemaFormaPago = function(){
		return {
			model: {
				fields : {
					NombreMedioDePago			: { type: "string"},
					IdMoneda					: { type: "string"},
					MontoLiquidacionFormaDePago	: { type: "number"},
					NombreTipoCuentaBancaria	: { type: "string"},
					NombreCuentaBancaria		: { type: "string"},
					NumeroCuentaBancaria		: { type: "number"},
				}
			}
		};
	};

	//Buscar Información del Comprobante
	var definicionOMovimientoCobros = function(inFiltros) 
	   {
			if ($rootScope.SesionLocale === 'CL') {
				return { 
				      serverFiltering : true,
				      serverSorting   : true, 
				      sort 			  : { field: "IdMovimientoComprobanteCobro", dir: "asc" }, // Criterios de Ordenamiento.
					  pageSize 		  : 10, // Número de registros por página.
					  transport 	  : {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",dataType : "json"}},
					  schema 		  : $scope.getSchemaCobros()
				  };	
			}
			if ($rootScope.SesionLocale === 'PE') {
				return { 
				      serverFiltering : true,
				      serverSorting   : true, 
				      sort 			  : { field: "IdMovimientoComprobanteCobro", dir: "asc" }, // Criterios de Ordenamiento.
					  pageSize 		  : 10, // Número de registros por página.
					  transport 	  : {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20062",dataType : "json"}},
					  schema 		  : $scope.getSchemaCobros()
				  };
			}
			else {
				
			}
		
		  
	   };
	   
	   
	$scope.MvtoComprobanteDatasource = new bcs.DataSource({
		                   type: "odata",
		                   definition 	: {
							                  serverFiltering 	: true, // Filtrado de datos en el servidor.
							                  serverPaging 		: true, // Paginado de los datos en el servidor.
							                  serverSorting 	: true, // Ordenamiento de los datos en el servidor.
							                  sort 				: { field: "IdMovimientoComprobante", dir: "desc" }, // Criterios de Ordenamiento.
							                  filter            : { field: "IdMovimientoComprobante", operator: "eq", value: Number(bModifMvtoMod.IdMovimientoComprobante)},
							                                    
							                  transport 		: { read: {
														                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1235",
														                dataType: "json"
													 	                  }
												                  },
							                  schema            : $scope.getSchemaMovimiento()
						                  }
	}); 
		
		
	$scope.MvtoComprobanteDatasource.query().then(function(){
		$scope.$apply(function() {
			bModifMvtoMod.oMovimientoComprobante 	= $scope.MvtoComprobanteDatasource.data();
		});
	});


	$scope.confBasicaMovimientoComprobante = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "NroLinea", dir: "asc" } ],
									transport 		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1235",dataType : "json"}},
									schema 			: $scope.getSchemaDetalle(),
								}
							  }),
		columns				: [
								 {field : "NombreClasificacionTipoInstrumento", 	title:"UI_CLASIFICACION_TIPO_INSTRUMENTO", 	width: "100px"}								
								,{field : "NombreTipoInstrumento", 					title:"UI_TIPO_INSTRUMENTO", 				width: "100px"}
								,{field : "NemotecnicoInstrumento", 				title:"UI_INSTRUMENTO", 					width: "100px"}
								,{field : "Cantidad", 								title:"UI_CANTIDAD", 						width: "80px",	format : function (value, item) { 
																																													if (item.CodigoTipoProducto === 'MEME'){
																																														return util.bcs_formato(value,'GEN','Precio',item.IdMonedaMovimientoComprobante,item.IdTipoInstrumento,'');
																																													}
																																													return util.bcs_formato(value,'GEN','Cantidad',item.IdMonedaMovimientoComprobante,'','');
																																												 }
    		                                         }
								,{field : "Precio", 								title:"UI_PRECIO", 							width: "80px",	template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
								,{field : "Monto",                					title:'UI_MONTO_OPERADO',					width: "80px", 	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
		height				: "100px",
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter({ field: "IdMovimientoComprobante", operator: "eq", value: Number(bModifMvtoMod.IdMovimientoComprobante)}); },  
	};

	$scope.confBasicaMovimientoComprobanteCobros = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : definicionOMovimientoCobros()
							  }),
		columns				: [
								 {field : "IdCobro", 							title:"UI_CODIGO",					width: "85px"}
								,{field : "NombreCobro", 						title:"UI_NOMBRE", 					width: "150px"}
								,{field : "NombreFormaCobro", 					title:"UI_FORMA", 					width: "100px"}
								,{field: "Porcentaje",			    			title: "UI_PORCENTAJE_COBRO",       width: "80px",  template: "<label>  #: (util.bcs_formato(Porcentaje,'GEN','Tasa',IdMonedaLiquidacionCobro,'','')).valor #</label>"}
								,{field: "IdMonedaMovimientoComprobanteCobro",  title: "UI_MONEDA_LOCAL_COBRO",     width: "80px",  template: "<span class='flags' title='#=NombreMonedaSistemaCobro#'>#=IdMonedaMovimientoComprobanteCobro#</span>"}
								,{field : "MontoMovimientoComprobanteCobro", 	title:"UI_MONTO_LOCAL_COBRO", 		width: "100px", template :"<label> #: (util.bcs_formato(MontoMovimientoComprobanteCobro,'GEN','Monto',IdMonedaMovimientoComprobanteCobro,'','')).valor #</label>"}
								,{field : "IdMonedaLiquidacionCobro", 			title:"UI_MONEDA_LIQUIDACION_COBRO",width: "80px",  template: "<span class='flags' title='#=NombreMonedaLiquidacionCobro#'>#=IdMonedaLiquidacionCobro#</span>" }
								,{field : "MontoLiquidacionCobro", 				title:"UI_MONTO_LIQUIDACION_COBRO", width: "80px",	template :"<label> #: (util.bcs_formato(MontoLiquidacionCobro,'GEN','Monto',IdMonedaLiquidacionCobro,'','')).valor #</label>"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
		height				: "100px",
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter([{ field: "IdMovimientoComprobante", operator: "eq", value: Number(bModifMvtoMod.IdMovimientoComprobante)},
			                                                    {field: "IdEstado", operator: "eq", value: 'VIG'}
		                                                       ]); },  
	};

	$scope.confBasicaMovimientoComprobanteFormaDePago = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
									sort 				: [ { field: "NombreMedioDePago", dir: "asc" } ],
									transport 			: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1237",dataType : "json"}},
									schema 				: $scope.getSchemaFormaPago(),
								}
							  }),
		columns				: [
								 {field : "NombreMedioDePago", 			title:"UI_MEDIO_DE_PAGO",			width: "85px"}
								,{field : "IdMoneda", 					title:"UI_MONEDA", 					width: "200px"}
								,{field : "MontoLiquidacionFormaDePago",title:"UI_MONTO_LIQUIDACION",		width: "100px", 
								//template : " <label> #:  (util.bcs_formato(MontoLiquidacionFormaDePago,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
																format : function (value, item) { 
																								if (item.IdMoneda === 'CLP'){
																									return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');
																								}
																								return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,'','');
																								}
								 }
								,{field : "NombreTipoCuentaBancaria", 	title:"UI_TIPO_CUENTA_BANCARIA",	width: "80px"}
								,{field : "NombreCuentaBancaria", 		title:"UI_CUENTA_BANCARIA", 		width: "100px"}
								,{field : "NumeroCuentaBancaria", 		title:"UI_NUMERO_CUENTA", 			width: "80px", 	template : " <label> #: util.formateaNumero(NumeroCuentaBancaria,0) #</label>"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
		height				: "100px",
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter([{ field: "IdMovimientoComprobante", operator: "eq", value: Number(bModifMvtoMod.IdMovimientoComprobante)},
		           			                                      {field: "IdEstado", operator: "eq", value: 'VIG'}
                                                               ]); }, 
	};
	
	$scope.verMapaRelaciones = function()
	{
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "MOVFACTURAR",IdEntidad :$scope.parametros.IdMovimientoComprobante},false,false );
	};

});

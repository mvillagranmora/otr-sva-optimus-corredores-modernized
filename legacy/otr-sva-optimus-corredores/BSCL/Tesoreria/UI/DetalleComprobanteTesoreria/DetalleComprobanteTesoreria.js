angular.module('ModuloFacturacion').controller('DetalleComprobanteDeTesoreriaCtrl',
		function($scope,$uibModal,$translate,bcsComunes){
	
	
	var bInfoComprobante= this;	
	$scope.oComprobante = {};
	
	//Recupera parametros desde la ventana recibida
    $scope.IdVentana 	= bcsComunes.ventanaWinId ($scope);
    $scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	
	bInfoComprobante.IdComprobante = $scope.parametros.IdComprobante;

	$scope.getSchema = function(){
		return {
			model: {
				fields : {
					IdComprobante					: { type: "number"},
					NombreTipoComprobante			: { type: "string"},
					NombreConcepto					: { type: "string"},
					NombreCaja						: { type: "string"},
					FechaMovimiento					: { type: "sysdate"},
					NombreEstado					: { type: "string"},
					NombrePersona					: { type: "string"},
					DocumentoIdentidad				: { type: "string"},
					NombrePersonaTitularPago		: { type: "string"},
					NombrePortafolioTitularPago		: { type: "string"},
					FechaLiquidacion				: { type: "sysdate"},
					Monto							: { type: "number"},
					NroLinea						: { type: "number"},
					IdTipoComprobanteOrigen			: { type: "string"},
					IdComprobanteOrigen				: { type: "number"},
					FechaComprobanteOrigen			: { type: "sysdate"},
					NombreTipoProducto				: { type: "string"},
					NombreTipoOperacion				: { type: "string"},
					NombreMedioDePago				: { type: "string"},
					NombreModalidadDePago			: { type: "string"},
					IdComprobantePartida			: { type: "number"},

					NombreBancoCorredora				: { type: "string"},
					NombreTipoCuentaBancariaCorredora	: { type: "string"},
					NumeroCuentaBancariaCorredora		: { type: "number"},
					FechaDocumentoPagoCorredora			: { type: "sysdate"},
					NumeroDocumentoCorredora			: { type: "number"},
					NumeroSerieCorredora				: { type: "number"},
					NombreBancoSocio					: { type: "string"},
					NombreTipoCuentaBancariaSocio		: { type: "string"},
					NumeroCuentaBancariaSocio			: { type: "number"},
					FechaDocumentoPagoSocio				: { type: "sysdate"},
					NumeroDocumentoSocio				: { type: "number"},
					NumeroSerieSocio					: { type: "number"},
					NombreVentana						: { type: "string"},
					NombreTipoMovimiento				: { type: "string"},
				}
			}
		};
	};


	$scope.comprobanteDatasource = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, // Filtrado de datos en el servidor.
			transport		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1020",dataType : "json"}},
			filter			: { field: "IdComprobante", operator: "eq", value: bInfoComprobante.IdComprobante},
			schema			: $scope.getSchema()
		}
	});
	$scope.comprobanteDatasource.query().then(function(){
		$scope.$apply(function() {
			bInfoComprobante.oComprobante = $scope.comprobanteDatasource.data();
		});
	});



	$scope.confBasicaMovimientosComprobantes = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
									sort 				: [ { field: "NombreTipoMovimiento", dir: "asc" },
															{ field: "NombreConcepto", dir: "asc" },
														  ],
									filter		: [{ field: "IdComprobante", operator: "eq", value: Number(bInfoComprobante.IdComprobante)},
									      		   { field: "IdEstado", operator: "neq", value: 'ANU'}
												  ],
									transport 	: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1031",dataType : "json"}},
									schema 		: $scope.getSchema(),
									group		: {field: "NombreConcepto", aggregates: [{ field: "Monto", aggregate: "sum" }]}
								}
							  }),
		columns				: [
								 {field : "NombreTipoMovimiento", 		title:"UI_TIPO_COMPROBANTE",		width: "85px"}
								,{field : "NombreConcepto", 			title:"UI_CONCEPTO", 				width: "200px", groupable: true}
								,{field : "Monto", 						title:"UI_MONTO", 					width: "100px",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+ "#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
								,{field : "IdTipoComprobanteOrigen", 	title:"UI_TIPO_COMPROBANTE", 		width: "80px"}
								,{field : "IdComprobanteOrigen", 		title:"UI_SECUENCIA_COMPROBANTE", 	width: "100px", template: '<bcs-link ng-if="dataItem.IdComprobanteOrigen !== 0" class="pull-left" link="dataItem.NombreVentana" params="{ IdComprobante : dataItem.IdComprobanteOrigen }"></bcs-link> <label> #: util.formateaNumero(IdComprobanteOrigen,0) #</label>'}
								,{field : "FechaComprobanteOrigen", 	title:"UI_FECHA_COMPROBANTE", 		width: "80px"}
								,{field : "NombreVentana",              title:'NOMBRE_VENTANA',     		hidden: true  }
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: true, 
		userSettings		: true
	};	  		

	$scope.confBasicaComprobanteFormaDePago = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
									sort 				: [ { field: "NombreMedioDePago", dir: "asc" }],
									filter		: [{ field: "IdComprobante", operator: "eq", value: Number(bInfoComprobante.IdComprobante)},
									      		   { field: "IdEstado", operator: "neq", value: 'ANU'}],
									transport 	: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1021",dataType : "json"}},
									schema 		: $scope.getSchema()
								}
							  }),
		columns				: [	 
								  {field : "NombreMedioDePago",     title:'UI_MEDIO_DE_PAGO', width : "120px"}
								 ,{field : "Monto",                 title:'UI_MONTO',         width : "50px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
								 ,{field : "NombreEstado",          title:'UI_ESTADO',     	  width : "80px"}

								  //Datos de la Corredora
								 ,{title   : "UI_CORREDORA", align: "center",
								   columns : [
												 {field : "NombreBancoCorredora",  	           title:'UI_BANCO',             width : "100px" }												
												,{field : "NumeroCuentaBancariaCorredora",     title:'UI_NUMERO_CUENTA',     width : "60px" }							
												,{field : "FechaDocumentoPagoCorredora",       title:'UI_FECHA_DOCUMENTO',   width : "80px"}
												,{field : "NumeroSerieCorredora",     		   title:'UI_SERIE_DOCUMENTO',   width : "60px"}							
								 ]}
								 //Datos del Socio de Negocio
								 ,{title   : "UI_SOCIO_NEGOCIO", align: "center",
								  columns : [

												 {field : "NombreBancoSocio",     		   title:'UI_BANCO',             width : "100px"}
												,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',     width : "60px"}
												,{field : "FechaDocumentoPagoSocio",       title:'UI_FECHA_DOCUMENTO',   width : "80px"}
												,{field : "NumeroSerieSocio",     		   title:'UI_SERIE_DOCUMENTO',   width : "60px"}

								 ]}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: true,
	};

	var firstHeight = $('#Win_DetalleComprobanteTesoreria');
	firstHeight.css("height", "auto");

});

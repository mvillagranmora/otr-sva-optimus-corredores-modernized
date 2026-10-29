angular.module('ModuloMesa').controller('DetalleOperacionMesaRFCtrl', function ($scope, bcsComunes, bcsConsultasMesa,bcsServiciosGrillasMesaRF, $translate) {
	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.operacion = {};
	$scope.detalleOperacion = [];
	$scope.esAbono = false;
	$scope.formaPagoOperacionEntregamos = [];
	$scope.formaPagoOperacionRecibimos = [];
	$scope.formaPagoOperacionEntregamos2 = [];
	$scope.formaPagoOperacionRecibimos2 = [];
	var parametrosVentana = bcsComunes.ventanaParams($scope.winID);
	var dataSourceDetalleOperacion = bcsServiciosGrillasMesaRF.getDataSourceDetalleOperacionMesaRF();
	var dataSourceDetalleComprobantePagoOperacion = bcsServiciosGrillasMesaRF.getDataSourceComprobantePagoOperacionMesaRF();
	$scope.IdMonedaPrincipal = "CLP";


	
//////////////////////////////////////////////
// INICIO CONFIG KENDO GRID
//////////////////////////////////////////////

	
	$scope.confBasicaFormaPagoOperacionEntregamos = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "NroLinea", dir: "asc" } ],
									//filter			: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
									transport 		: {
										read: {
										url: bcsConsultasMesa('ConsultarComprobantePagoOperacionMesaRF'),
										dataType: "json"
										}
									},
									schema 			: bcsServiciosGrillasMesaRF.getSchemaComprobantePagoOperacionMesaRF()
					   
								}
							  }),
		columns				: [
								 {field : "NombreFormaDePago", 				title:"UI_FORMA_PAGO_INICIAL"		},
								 {field : "NombreBancoCuentaBancaria", 		            title:"UI_NOMBRE_BANCO"},
								 {field : "NumeroCuentaBancaria", 		            title:"UI_CUENTA_BANCARIA"},
								 {field : "IdAbono", 		            title:"UI_SECUENCIA_ABONO"},
								 {field : "IdCargo", 		            title:"UI_SECUENCIA_CARGO"},
								 {field : "IdIngresoEgreso", 		            title:"UI_INGRESO_EGRESO_DE_TESORERIA"},
								 {field : "FechaLiquidacionComprobantePagoOperacion", 		            title:"UI_FECHA_LIQUIDACION"},
								 {field : "Monto", 		            title:"UI_MONTO_LIQUIDACION", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', item.IdMoneda,'',''); }},	 
								
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		// Filtro
		gridCreated			: function(e){
			
			let filters = bcsServiciosGrillasMesaRF.validarFiltros(
																	{
																		"SesionIdEmpresa": $scope.SesionIdEmpresa
																		, "IdOperacion": parametrosVentana.IdOperacion
																		, "IdTipoProducto": parametrosVentana.IdTipoProducto
																		
																	}
																   , 'OPERACION');
																   
			// Agregamos dos filtros esenciales 
			filters.push({ field: "IdFormaDePago", operator: "neq", value: null});
			filters.push({ field: "RecibePaga", operator: "eq", value: 'P'});	
			filters.push({ field: "IdComprobantePagoOperacion", operator: "gt", value: 0});	

			e.dataSource.filter(filters); 
		},  
	};
	
	$scope.confBasicaFormaPagoOperacionRecibimos = {
		
			dataSource			: new bcs.DataSource({
									type : "odata",
									definition : {
										serverFiltering : true, // Filtrado de datos en el servidor.
										serverPaging 	: true, // Paginado de los datos en el servidor.
										serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
										sort 			: [ { field: "NroLinea", dir: "asc" } ],
										//filter			: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
										transport 		: {
											read: {
											url: bcsConsultasMesa('ConsultarComprobantePagoOperacionMesaRF'),
											dataType: "json"
											}
										},
										schema 			: bcsServiciosGrillasMesaRF.getSchemaComprobantePagoOperacionMesaRF()
						   
									}
								  }),
			columns				: [
									 {field : "NombreFormaDePago", 				title:"UI_FORMA_PAGO_FINAL"},
									 {field : "NombreBancoCuentaBancaria", 		            title:"UI_NOMBRE_BANCO"},
									 {field : "NumeroCuentaBancaria", 		            title:"UI_CUENTA_BANCARIA"},
									 {field : "IdAbono", 		            title:"UI_SECUENCIA_ABONO"},
									 {field : "IdCargo", 		            title:"UI_SECUENCIA_CARGO"},
									 {field : "IdIngresoEgreso", 		            title:"UI_INGRESO_EGRESO_DE_TESORERIA"},
									 {field : "FechaLiquidacionComprobantePagoOperacion", 		            title:"UI_FECHA_LIQUIDACION"},
									 {field : "Monto", 		            title:"UI_MONTO_LIQUIDACION", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', item.IdMoneda,'',''); }},	 
					
									
								  ],		
			sortable			: true,
			pageable			: false,
			autoBind			: false,
		    selectable 			: "multiple row",
		    navigatable         : true,
			allowCopy 			: true,
			// Filtro
			gridCreated			: function(e){
				
				let filters = bcsServiciosGrillasMesaRF.validarFiltros(
																		{
																			"SesionIdEmpresa": $scope.SesionIdEmpresa
																			, "IdOperacion": parametrosVentana.IdOperacion
																			, "IdTipoProducto": parametrosVentana.IdTipoProducto
																			
																		}
																	   , 'OPERACION');
																	   
				// Agregamos dos filtros esenciales 
				filters.push({ field: "IdFormaDePago", operator: "neq", value: null});
				filters.push({ field: "RecibePaga", operator: "eq", value: 'R'});	
				filters.push({ field: "IdComprobantePagoOperacion", operator: "gt", value: 0});	
				
				e.dataSource.filter(filters); 
			}

	};


	$scope.confBasicaDetalleOperacion = {
			
			dataSource			: new bcs.DataSource({
									type : "odata",
									definition : {
										serverFiltering : true, // Filtrado de datos en el servidor.
										serverPaging 	: true, // Paginado de los datos en el servidor.
										serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
										sort 			: [ { field: "NroLinea", dir: "asc" } ],
										//filter			: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
										transport 		: {
											read: {
											url: bcsConsultasMesa('ConsultarCarteraOperacionMesaRF'),
											dataType: "json"
											}
										},
										schema 			: bcsServiciosGrillasMesaRF.getSchemaDetalleOperacionMesaRF()
						   
									}
		    }),

			columns				: [
									 {field : "FolioOperacion", 				title:"UI_FOLIO", width: "45px"},
									 {field : "CarteraNominal", 		            title:"UI_ORIGEN", width: "56px"},
									 {field : "IdTipoInstrumentoDetalle", 		            title:"UI_FAMILIA", width: "57px"},
									 {field : "NemotecnicoDetalle", 		            title:"UI_NEMOTECNICO", width: "86px"},
									 {field : "IdClasificacionTipoInstrumentoDetalle", 		            title:"UI_MERCADO", width: "63px"},
									 {field : "IdEmisorDetalle", 		            title:"UI_EMISOR", width: "55px"},
									 {field : "EsFungibleDetalle", 		            title:"UI_FUNGIBLE", width: "65px"},
									 {field : "IdBovedaDetalle", 		            title:"UI_BOVEDA", width: "58px"},
									 {field : "CodigoDescIFRSDetalle", 		            title:"UI_CATEGORIA_IFRS", width: "95px"},
									 {field : "ValorClasificacionRiesgoDetalle", 		            title:"UI_CLASIFICACION_RIESGO", width: "130px"},
									 {field : "CorteMinimoDetalle", 		            title:"UI_CORTE_MINIMO", width: "86px"},
									 {field : "IdTipoReajusteDetalle", 		            title:"UI_TIPO_REAJUSTE", width: "88px"},
									 {field : "FechaVencimientoInstrumentoDetalle", 		            title:"UI_FECHA_VENCIMIENTO", width: "115px"},
									 {field : "ValorRescateDetalle", 		            title:"Cantidad / Valor Rescate", width: "140px", // No encontre como pasar un custom title()
								     	template: function(item) {
								        	return item.IdClasificacionTipoInstrumentoDetalle === 'IF' ? util.bcs_formato(item.ValorRescateDetalle,'GEN','Monto', item.IdTipoReajusteDetalle,'','').valor : util.bcs_formato(item.CantidadReservadaDetalle,'GEN','Cantidad', item.IdMoneda,'','').valor;
					     	 			}
								     },
									 {field : "PrecioCompraDetalle", 		            title:"UI_PRECIO", width: "62px",
								     	template: function(item) {
											
								        	return item.IdClasificacionTipoInstrumentoDetalle === 'RF' ? util.bcs_formato(item.PrecioCompraDetalle,'GEN','Tasa', item.IdMoneda,'','').valor  : util.bcs_formato(item.PrecioCompraDetalle,'GEN','Precio', item.IdMoneda,'','').valor
					     	 			}
								     },
									 {field : "TasaCompraDetalle", 		            title:"UI_TASA", width: "62px",
									 	format: function(value, item){
											return util.bcs_formato(value,'GEN','Tasa', item.IdMoneda,'','') 
										}
									 },
									 {field : "MontoCompraDetalle", 		            title:"UI_MONTO", width: "110px",
									 	format: function(value, item){
											return util.bcs_formato(value,'GEN','Monto', 'CLP','','') 
										}
									 },
									 {field : "MontoLiquidacionOperacionDetalle", 		            title:"UI_MONTO_LIQUIDACION", width: "78px", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', item.IdMoneda,'',''); }},								 
									 {field : "INGMontoAPlazoDetalle", 		            title:"UI_MONTO_PLAZO_DETALLE", width: "78px", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', 'CLP','',''); }},
									 {field : "INGMontoAPlazoMonedaInstrumentoDetalle", 		            title:"UI_MONTO_PLAZO_INSTRUMENTO_DETALLE", width: "181px", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', item.IdMonedaLiquidacionOperacionDetalle || 'CLP','',''); }},
									 {field : "Spread",												title: "UI_SPREAD", width: "110px", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', item.IdMoneda,'',''); }},
									 {field : "ValorPresente",										title: "UI_VALOR_PRESENTE", width: "110px", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto', item.IdMoneda,'',''); }},
									 
									 
								  ],
			sortable			: true,
			pageable			: false,
			autoBind			: false,
		    selectable 			: "multiple row",
		    navigatable         : true,
			allowCopy 			: true,
			// Filtro
			gridCreated			: function(e){
				
				let filters = bcsServiciosGrillasMesaRF.validarFiltros(
																		{
																			"SesionIdEmpresa": $scope.SesionIdEmpresa
																			, "IdOperacion": parametrosVentana.IdOperacion
																			, "IdTipoProducto": parametrosVentana.IdTipoProducto
																			
																		}
																	   , 'OPERACION');
																	   

				e.dataSource.filter(filters); 
			}
	};


//////////////////////////////////////////////
// FIN CONFIG KENDO GRID
//////////////////////////////////////////////


	var cargarOperacion = function () {
		var filtros = [];
		if (parametrosVentana.IdOperacion && parametrosVentana.IdOperacion > 0) {

			console.log(parametrosVentana.IdOperacion);
			bcsComunes.ventanaOverlay($scope.winID, true);

			filtros = bcsServiciosGrillasMesaRF.validarFiltros({
				"SesionIdEmpresa": $scope.SesionIdEmpresa
				, "IdOperacion": parametrosVentana.IdOperacion
				, "IdTipoProducto": parametrosVentana.IdTipoProducto
			}
			, 'OPERACION');

			
			dataSourceDetalleOperacion.query({ filter: filtros }).then(function () {
				var data = dataSourceDetalleOperacion.data();

				if (data.length > 0) {
					//cargo datos de la cabecera
					//datos cliente
					$scope.operacion.IdSocioDeNegocio = Number(data[0].IdSocioDeNegocio);
					$scope.operacion.IdCliente = Number(data[0].IdCliente);
					$scope.operacion.NombreCliente = data[0].DocumentoIdentidad + ' ' + data[0].NombreCompleto;
					$scope.operacion.IdPortafolio = Number(data[0].IdPortafolio);
					$scope.operacion.PortafolioCliente = data[0].CodigoPropositoPortafolio;
					$scope.operacion.FolioCartera = data[0].FolioCartera;
					$scope.operacion.NombreMesaOrigen = data[0].NombreMesaOrigen;
					$scope.operacion.NombreMesaDestino = data[0].NombreMesaDestino;

					$scope.operacion.SecuenciaOperacion = Number(data[0].IdOperacion);
					$scope.operacion.FolioOperacion = Number(data[0].FolioOperacion);
					$scope.operacion.EsIntermesa = data[0].Intermesa === 'S' ? 'UI_SI' : 'UI_NO';
					//tipoOperacion
					$scope.operacion.IdTipoOperacion = data[0].IdTipoOperacion;
					$scope.operacion.TipoOperacion = data[0].NombreTipoOperacion;

					$scope.operacion.FechaOperacion = util.dateSPdate(data[0].FechaOperacion);
					//$scope.data.oConfiguracionEstado.idDefecto = data[0].IdEstado;
					$scope.operacion.IdEstado = data[0].IdEstado;
					$scope.operacion.NombreEstado = data[0].NombreEstado;
					$scope.operacion.IdTipoReajuste = data[0].IdTipoReajuste;
					$scope.operacion.IdTipoReajusteDetalle = data[0].IdTipoReajusteDetalle;
					$scope.operacion.IdMoneda = data[0].IdMoneda;
					$scope.operacion.IdMonedaLiquidacion = data[0].IdMonedaLiquidacion;
					$scope.operacion.IdMonedaLiquidacionDetalle = data[0].IdMonedaLiquidacionInstrumentoDetalle;
					$scope.operacion.Tasa = Number(data[0].Tasa);

					$scope.operacion.Plazo = Number(data[0].PlazoEnDias);
					$scope.operacion.FechaVencimiento = util.dateSPdate(data[0].FechaVencimiento);

					$scope.operacion.MontoOperacion = Number(data[0].MontoOperacion);
					$scope.operacion.MontoOperacionInicial = Number(data[0].MontoOperacionInicial);
					$scope.operacion.MontoOperacionFinal = Number(data[0].MontoOperacionFinal);
					$scope.operacion.MontoOperacionMoneda = Number(data[0].MontoOperacionMoneda);
					$scope.operacion.MontoOperacionInicialMoneda = Number(data[0].MontoOperacionInicialMoneda);
					$scope.operacion.MontoOperacionFinalMoneda = Number(data[0].MontoOperacionFinalMoneda);


					$scope.operacion.CondicionLiquidacion = data[0].IdCondicionLiquidacion;
					$scope.operacion.NombreCondicionLiquidacion = data[0].NombreCondicionLiquidacion;
					$scope.operacion.FechaCondicionLiquidacion = util.dateSPdate(data[0].FechaCondicionLiquidacion);

					$scope.operacion.NombreAgente = data[0].NombreEjecutivo;
					$scope.operacion.TasaAgente = data[0].TasaAgente;
					$scope.operacion.NombreOperador = data[0].CodigoUsuario;

					$scope.operacion.Comentarios = data[0].Glosa;
					$scope.operacion.MontoOperacionAnticipo = data[0].MontoOperacionAnticipo;
					$scope.operacion.MontoOperacionInicialAnticipo = data[0].MontoOperacionInicialAnticipo;
					$scope.operacion.MontoOperacionFinalAnticipo = data[0].MontoOperacionFinalAnticipo;
					$scope.operacion.TasaAnticipo = data[0].TasaAnticipo;
					$scope.operacion.MontoOperacionMonedaAnticipo = data[0].MontoOperacionMonedaAnticipo;
					$scope.operacion.MontoOperacionInicialMonedaAnticipo = data[0].MontoOperacionInicialMonedaAnticipo;
					$scope.operacion.MontoOperacionFinalMonedaAnticipo = data[0].MontoOperacionFinalMonedaAnticipo;
					$scope.operacion.IdMonedaLiquidacionAnticipo = data[0].IdMonedaLiquidacionAnticipo;
					$scope.operacion.CondicionLiquidacionAnticipo = data[0].IdCondicionLiquidacionAnticipo;
					$scope.operacion.NombreCondicionLiquidacionAnticipo = data[0].NombreCondicionLiquidacionAnticipo;
					$scope.operacion.FechaCondicionLiquidacionAnticipo = data[0].FechaLiquidacion;
					$scope.operacion.IdClasificacionTipoInstrumentoDetalle = data[0].IdClasificacionTipoInstrumentoDetalle;
					$scope.operacion.IdTipoInstrumentoDetalle = data[0].IdTipoInstrumentoDetalle;
					$scope.operacion.IngMontoAPlazoDetalle = data[0].INGMontoAPlazoDetalle;
					$scope.operacion.IngMontoAPlazoMonedaInstrumentoDetalle = data[0].INGMontoAPlazoMonedaInstrumentoDetalle;
					$scope.operacion.AfectaCustodia = data[0].AfectaCustodia;
					
					
					//detalle de la operacion
					$.each(data, function (key, value) {
						if (value.IdReservaDetalle > 0)
							$scope.detalleOperacion.push(value);
					});
					if ($scope.operacion.IdTipoOperacion === 'COMPAC' || $scope.operacion.IdTipoOperacion === 'COMTER') {
						$scope.esAbono = true;
					} else {
						$scope.esAbono = false;
					}

					bcsComunes.ventanaOverlay($scope.winID, false);
				}
				else {
					//no existe operacion
					bcsComunes.msg("error", "Error", $translate.instant("UI_OPERACION_INVALIDA"));
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			});
		}
		else {
			console.log("No existe la operacion!");
		}
	};
	cargarOperacion();
});

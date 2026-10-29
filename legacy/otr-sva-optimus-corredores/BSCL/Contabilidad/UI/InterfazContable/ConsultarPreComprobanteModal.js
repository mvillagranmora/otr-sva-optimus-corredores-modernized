angular.module('ModuloContabilidad').controller('ConsultarPreComprobanteModalCtrl',
		function ($rootScope,$scope,$translate,bcsComunes) {

	$scope.data = {};
	$scope.detalle = {};

	var filters = [];
	//var overlay = bcsComunes.overlay;
	var servicio = "";
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.oDatosDetalle = parametros;
	
	if ($scope.SesionLocale === 'PE')
	{
		servicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1807";
	}
	else
	{
		servicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600053";
	}
	
	var dsGrillaDetalle = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			excel 				: { allPages : true },
			sort 				: [ { field: "IdPreComprobante", dir: "asc" } ],
			filter				: {field: "IdPreComprobante", operator: "eq", value: $scope.data.oDatosDetalle.IdPreComprobante},
			transport 			: {read : {url : servicio, dataType : "json"}},
			schema : {
				model : {
					fields : {
						// AMBOS
						IdPreComprobante							: {type  : "number"},
						Linea										: {type  : "number"},
						IdCuentaContable							: {type  : "string"},
						NombreCuenta								: {type  : "string"},
						IdMoneda									: {type  : "string"},
						// CHILE
						IdPreComprobanteAsiento						: {type  : "number"},
						MontoHaberDetalle							: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoDebeDetalle							: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						Evento										: {type  : "string"},
						GlosaDetalle								: {type  : "string"},
						DocumentoIdentidad							: {type  : "string"},
						CodigoPortafolio							: {type  : "string"},
						// PERU
						IdEmpresa                               	: {type  : "number"},
						FechaContable                               : {type  : "string"},
						NombreEstado                                : {type  : "string"},
						MontoDebe									: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoHaber									: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						IdMovimiento								: {type  : "number"},
						FechaProceso								: {type  : "string"},
						DocumentoIdentidad                          : {type  : "string"},
						Portafolio                                  : {type  : "number"},
						NombreCliente                               : {type  : "string"},
						Cuenta                                      : {type  : "number"},
						Ejercicio                                   : {type  : "number"},
						Periodo                                     : {type  : "string"},
						Nombre                                      : {type  : "string"},
						AporteFondoMutuoCuentaPropia                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						AporteFondoMutuoCuentaTerceros              : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						DocumentoMontoEgreso                        : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP'		,'','').excelFormat;}},
						DocumentoMontoIngreso                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP'		,'','').excelFormat;}},
						DocumentoMontoTraspasoEgreso                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP'		,'','').excelFormat;}},
						DocumentoMontoTraspasoIngreso               : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP'		,'','').excelFormat;}},
						InteresAnteriorPactoCompra                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						InteresAnteriorPactoVenta                   : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						InteresProximoPactoCompra                   : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						InteresProximoPactoVenta                    : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MayorValorCompraRF                          : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MayorValorCompraRV                          : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MayorValorMercadoRF                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MayorValorMercadoRV                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MenorValorCompraRF                          : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MenorValorCompraRV                          : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MenorValorMercadoRF                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MenorValorMercadoRV                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaPrincipalEquivalenteSpotCompra   : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaPrincipalEquivalenteSpotVenta    : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaPrincipalSpotCompra              : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaPrincipalSpotVenta               : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaSecundariaEquivalenteSpotCompra  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaSecundariaEquivalenteSpotVenta   : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaSecundariaSpotCompra             : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMonedaSecundariaSpotVenta              : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoCompraActualRF                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoCompraAnteriorRF                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoMercadoAnteriorRF                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoCobro                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalCobros                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoLiquidacionCobros                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoCompra                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoDiferencia                             : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoEgreso                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoFinalComprobante                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoIngreso                                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoLiquidacion                            : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoMayorValorCompraRF                     : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoMayorValorMercadoRF                    : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoMenorValorCompraRF                     : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoMenorValorMercadoRF                    : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoMercado                                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoNeto                                   : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoPacto                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoPapel                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoPresenteEmisionRF                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						Perdida                                     : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ReajusteAnteriorPactoCompra                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ReajusteAnteriorPactoVenta                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ReajusteProximoPactoCompra                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ReajusteProximoPactoVenta                   : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						RescateFondoMutuoCuentaPropia               : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						RescateFondoMutuoCuentaTerceros             : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						SolicitudMontoEgreso                        : {type  : "number"},
						SolicitudMontoIngreso                       : {type  : "number"},
						SolicitudMontoTraspasoEgreso                : {type  : "number"},
						SolicitudMontoTraspasoIngreso               : {type  : "number"},
						Tasa                                        : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TasadeCambio                                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalComisiones                             : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalCompra                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalDerechos                               : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalGastos                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalImpuestos                              : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalVenta                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						Utilidad                                    : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorCompraActualRF                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorCompraActualRV                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMercadoActualRV                        : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorActualPactoCompra                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorActualPactoVenta                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraTasaSubyacenteCompraActual            : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraTasaSubyacenteVentaActual             : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorCompraAnteriorRF                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorCompraAnteriorRV                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMercadoAnteriorRF                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMercadoAnteriorRV                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorAnteriorPactoCompra                    : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorAnteriorPactoVenta                     : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraTasaSubyacenteCompraAnterior          : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraTasaSubyacenteVentaAnterior           : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorCompra                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorMercado                                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorPresenteEmisionRF                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorPromedioCompraRV                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorProximoPactoCompra                     : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValorProximoPactoVenta                      : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraTasaSubyacenteCompraProximo           : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraTasaSubyacenteVentaProximo            : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraRazonableActivo                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraRazonableNeto                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ValoraRazonablePasivo                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ComisionGenerica							: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ComisionCavali								: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ComisionBolsaValores						: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						FondoGarantia								: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ComisionSmv									: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						Igv											: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						ComisionSab									: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoInversion								: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TotalPoliza									: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						MontoBruto									: {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						Agente                                      : {type  : "string"},
						AliasBancoCaja                              : {type  : "string"},
						Bolsa                                       : {type  : "string"},
						Boveda                                      : {type  : "string"},
						Canal                                       : {type  : "string"},
						CentroCosto                                 : {type  : "string"},
						CentroCostoAgente                           : {type  : "string"},
						ClaseEmisor                                 : {type  : "string"},
						ClaseInstrumento                            : {type  : "string"},
						CodigoAgente                                : {type  : "string"},
						CodigoBolsa                                 : {type  : "string"},
						CodigoCaja                                  : {type  : "string"},
						CodigoIFRS                                  : {type  : "string"},
						CodigoProducto                              : {type  : "string"},
						CodigoSucursal                              : {type  : "string"},
						CodigoTipoOperacion                         : {type  : "string"},
						CodSecuencia                                : {type  : "string"},
						CompraVenta                                 : {type  : "string"},
						Concepto                                    : {type  : "string"},
						CondicionLiquidacion                        : {type  : "string"},
						CuentaPropia                                : {type  : "string"},
						DiasLiquidacion                             : {type  : "number"},
						DiasVencido                                 : {type  : "number"},
						DiasVencimiento                             : {type  : "number"},
						DocumentoIdentidad_Adic                     : {type  : "string"},
						Emisor                                      : {type  : "string"},
						EmisorInstrumento                           : {type  : "string"},
						EmitidoA                                    : {type  : "string"},
						EsBilateral                                 : {type  : "string"},
						EsCuentaDepositante                         : {type  : "string"},
						EsCuentaMandante                            : {type  : "string"},
						EsCuentaPropia                              : {type  : "string"},
						EsExento                                    : {type  : "string"},
						EsRelacionado                               : {type  : "string"},
						FamiliaInstrumento                          : {type  : "string"},
						FamiliaTipoInstrumento                      : {type  : "string"},
						Fecha                                       : {type  : "string"},
						FechaActual                                 : {type  : "string"},
						FechaAnterior                               : {type  : "string"},
						FechaDeVencimiento                          : {type  : "string"},
						FechaDocumento                              : {type  : "string"},
						FechaDocumentoPago                          : {type  : "string"},
						FechaEmisionDocumento                       : {type  : "string"},
						FechaLiquidacion                            : {type  : "string"},
						FechaLiquidacionDocumento                   : {type  : "string"},
						FechaProceso_Adic                           : {type  : "string"},
						Folio                                       : {type  : "number"},
						FolioCartera                                : {type  : "number"},
						FolioCmbteCont                              : {type  : "number"},
						FolioComprobanteAdj                         : {type  : "number"},
						FolioOperacion                              : {type  : "number"},
						Glosa_Adic                                  : {type  : "string"},
						IdCartera                                   : {type  : "number"},
						IdEmisor                                    : {type  : "string"},
						IdEmpresa_Adic                              : {type  : "string"},
						IdEstadoSII                                 : {type  : "string"},
						IdGarantia                                  : {type  : "number"},
						IdSubyacente                                : {type  : "number"},
						IdTipoReajuste                              : {type  : "string"},
						IndicadorLiquidacion                        : {type  : "string"},
						IndicadorSimultanea                         : {type  : "string"},
						ISIN                                        : {type  : "string"},
						MedioDePago                                 : {type  : "string"},
						Modalidad                                   : {type  : "string"},
						Moneda                                      : {type  : "string"},
						MonedaComprobante                           : {type  : "string"},
						MonedaLiquidacion                           : {type  : "string"},
						Monto                                       : {type  : "number"},
						Nemo                                        : {type  : "number"},
						Nemotecnico                                 : {type  : "string"},
						NombreAgente                                : {type  : "string"},
						NombreConcepto                              : {type  : "string"},
						NombreCuentaCorriente                       : {type  : "string"},
						NombreSucursalAgente                        : {type  : "string"},
						NumeroComprobante                           : {type  : "number"},
						NumeroCuentaCorriente                       : {type  : "number"},
						NumeroDocumentoPago                         : {type  : "number"},
						Operacion                                   : {type  : "string"},
						PartidaLiquidacion                          : {type  : "string"},
						PrecioCompra                                : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						PrecioMercadoActual                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						PrecioMercadoAnterior                       : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						Proceso                                     : {type  : "string"},
						Producto                                    : {type  : "string"},
						ProductoMovimiento                          : {type  : "string"},
						Relacion                                    : {type  : "string"},
						RelacionadoEmpresa                          : {type  : "string"},
						RelacionCorredora                           : {type  : "number"},
						SerieDocumento                              : {type  : "string"},
						SucursalAgente                              : {type  : "number"},
						TasaCambio                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TasaCompra                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TasaEmision                                 : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TasaMercadoActual                           : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TasaMercadoAnterior                         : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TipoCambio                                  : {type	 : "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}},
						TipoCartera                                 : {type  : "string"},
						TipoCmbteCont                               : {type  : "string"},
						TipoComprobante                             : {type  : "string"},
						TipoComprobanteAdj                          : {type  : "string"},
						TipoDocumento                               : {type  : "string"},
						TipoEmisor                                  : {type  : "string"},
						TipoFamilia                                 : {type  : "string"},
						TipoInstrumento                             : {type  : "string"},
						TipoInstrumentoGarantizado                  : {type  : "string"},
						TipoMovimiento                              : {type  : "string"},
						TipoOperacion                               : {type  : "string"},
						TipoPersona                                 : {type  : "string"},
						TipoReajuste                                : {type  : "string"},
						TituloOCuentaCorriente                      : {type  : "string"},
						Valor                                       : {type  : "string"},
						Valuta                                      : {type  : "string"},
						VentaCorta                                  : {type  : "string"},
						EstadoSUNAT         						: {type  : "string"},
						TipoBonoIRF         						: {type  : "string"},
						OrigenInstrumento        					: {type  : "string"},	
						IndicadorRelacionadoGrupo					: {type  : "string"},
						Plaza         								: {type  : "string"},
						PactoAndino         						: {type  : "string"},
						FormadorMercado         					: {type  : "string"},
						PresenciaBursatil        					: {type  : "string"},
						TipoInstrumentoVenta     					: {type  : "string"},
						TipoInstrumentoCompra    					: {type  : "string"},
						FolioPoliza			     					: {type  : "string"},
						NombreTipoContabilidad    					: {type  : "string"}
					}
				}
			}
		}
	});
	
	
	columnas = function() {
  		return [
  			// CHILE
  			{field: "IdPreComprobante"								,title: 'UI_PRECOMPROBANTE'						,width: "100px"}
  		   ,{field: "IdPreComprobanteAsiento"						,title: 'UI_PRECOMPROBANTE_ASIENTO'				,width: "100px",	hidden: true}
  		   ,{field: "Linea"											,title: 'UI_LINEA'								,width: "60px"}
  		   ,{field: "IdCuentaContable"								,title: 'UI_CUENTA_CONTABLE'					,width: "120px"}
  		   ,{field: "NombreCuenta"									,title: 'UI_NOMBRE_CUENTA'						,width: "120px"}
  		   ,{field: "MontoDebeDetalle"								,title: 'UI_MONTO_DEBE'							,width: "100px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoDebeDetalle,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoHaberDetalle"								,title: 'UI_MONTO_HABER'						,width: "100px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoHaberDetalle,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "IdMoneda"										,title: 'UI_ID_MONEDA'   						,width: "60px"}
  		   ,{field: "Evento"										,title: 'UI_EVENTO'   							,width: "120px",	hidden: true}
  		   ,{field: "GlosaDetalle"									,title: 'UI_GLOSA'   							,width: "160px",	hidden: true}
  		   ,{field: "DocumentoIdentidad"							,title: 'UI_DOCUMENTO_IDENTIDAD'   				,width: "60px",		hidden: true}
  		   ,{field: "CodigoPortafolio"								,title: 'UI_PORTAFOLIO'   						,width: "60px",		hidden: true}
  		   // PERU
  		   ,{field: "MontoDebe"										,title: 'UI_MONTO_DEBE'							,width: "100px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoDebe,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoHaber"									,title: 'UI_MONTO_HABER'						,width: "100px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoHaber,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "Ejercicio"										,title: 'UI_EJERCICIO'							,width: "60px",		hidden: true}
  		   //,{field: "FechaProceso"								,title: 'UI_FECHA'								,width: "60px",		hidden: true}
  		   ,{field: "DocumentoIdentidad"							,title: 'UI_CLIENTE'							,width: "60px",		hidden: true}
  		   ,{field: "Portafolio"									,title: 'UI_PORTAFOLIO'							,width: "60px",		hidden: true}
  		   ,{field: "NombreCliente"									,title: 'UI_NOMBRE'								,width: "200px",	hidden: true}
  		   ,{field: "EsCuentaPropia"								,title: 'UI_ES_CUENTA_PROPIA'					,width: "80px",		hidden: true}
  		   ,{field: "NombreEstado"									,title: 'UI_ESTADO'								,width: "80px",		hidden: true}
  		   //,{field: "IdMovimiento"								,title: 'UI_MOVIMIENTO'							,width: "60px",		hidden: true}
  		   //,{field: "Nombre"										,title: 'UI_TIPO_MOVIMIENTO'    				,width: "150px",	hidden: true}
  		   ,{field: "Cuenta"										,title: 'UI_CUENTA'								,width: "70px",		hidden: true}
  		   ,{field: "MontoCobro"									,title: 'UI_MONTO_COBRO'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoCobro,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "TotalCobros"									,title: 'UI_TOTAL_COBRO'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalCobros,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MontoLiquidacionCobros"						,title: 'UI_TOTAL_LIQUIDO'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoLiquidacionCobros,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoFinalComprobante"							,title: 'UI_FINAL_COMPROBANTE'					,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoFinalComprobante,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoNeto"										,title: 'UI_MONTO_NETO'							,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoNeto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalComisiones"								,title: 'UI_TOTAL_COMISION'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalComisiones,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalCompra"									,title: 'UI_TOTAL_COMPRA'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalDerechos"									,title: 'UI_TOTAL_DERECHOS'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalDerechos,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalGastos"									,title: 'UI_TOTAL_GASTOS'						,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalGastos,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalImpuestos"								,title: 'UI_TOTAL_IMPUESTOS'					,width: "130px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalImpuestos,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "Agente"										,title: 'UI_AGENTE'								,width: "100px",	hidden: true}
  		   ,{field: "Bolsa"											,title: 'UI_BOLSA'								,width: "60px",		hidden: true}
  		   ,{field: "CompraVenta"									,title: 'UI_COMPRA_VENTA'						,width: "70px",		hidden: true}
  		   ,{field: "Concepto"										,title: 'UI_CONCEPTO'							,width: "70px",		hidden: true}
  		   ,{field: "NombreConcepto"								,title: 'UI_NOMBRE_CONCEPTO'					,width: "250px",	hidden: true}
		   ,{field: "CondicionLiquidacion"							,title: 'UI_CONDICION'							,width: "70px",		hidden: true}
  		   ,{field: "EsExento"										,title: 'UI_EXENTO'								,width: "70px",		hidden: true}
  		   ,{field: "EsRelacionado"									,title: 'UI_RELACIONADO'						,width: "70px",		hidden: true}
  		   ,{field: "FechaDocumento"								,title: 'UI_FECHA_DOCUMENTO'					,width: "80px",		hidden: true}
  		   ,{field: "IdEstadoSII"									,title: 'UI_ESTADO_SII'							,width: "80px",		hidden: true}
  		   ,{field: "Operacion"										,title: 'UI_OPERACION'							,width: "80px",		hidden: true}
  		   ,{field: "Producto"										,title: 'UI_PRODUCTO'							,width: "80px",		hidden: true}
  		   ,{field: "AporteFondoMutuoCuentaPropia"              	,title: 'UI_APORTEFMCP'							,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(AporteFondoMutuoCuentaPropia,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "AporteFondoMutuoCuentaTerceros"            	,title: 'UI_APORTEFMCT'							,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(AporteFondoMutuoCuentaTerceros,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "DocumentoMontoEgreso"                      	,title: 'UI_DOC_MONTO_EGRESO'					,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(DocumentoMontoEgreso,'GEN','Monto','CLP','','')).valor #</label>" }		
  		   ,{field: "DocumentoMontoIngreso"                     	,title: 'UI_DOC_MONTO_INGRESO'					,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(DocumentoMontoIngreso,'GEN','Monto','CLP','','')).valor #</label>" }		
  		   ,{field: "DocumentoMontoTraspasoEgreso"              	,title: 'UI_MONTO_TRAS_INGRESO'					,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(DocumentoMontoTraspasoEgreso,'GEN','Monto','CLP','','')).valor #</label>" }		
  		   ,{field: "DocumentoMontoTraspasoIngreso"             	,title: 'UI_MONTO_TRAS_EGRESO'					,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(DocumentoMontoTraspasoIngreso,'GEN','CLP','','')).valor #</label>" }		
  		   ,{field: "InteresAnteriorPactoCompra"                	,title: 'UI_INTERES_ANT_PACTO_COMPRA'			,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(InteresAnteriorPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "InteresAnteriorPactoVenta"                 	,title: 'UI_INTERES_ANT_PACTO_VENTA'			,width: "120px",  	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(InteresAnteriorPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "InteresProximoPactoCompra"                 	,title: 'UI_INTERES_PROX_PACTO_COMPRA'			,width: "120px",    hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(InteresProximoPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "InteresProximoPactoVenta"                  	,title: 'UI_INTERES_PROX_PACTO_VENTA'			,width: "120px",    hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(InteresProximoPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MayorValorCompraRF"                        	,title: 'UI_MAYOR_VALOR_COMPRA_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MayorValorCompraRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MayorValorCompraRV"                        	,title: 'UI_MAYOR_VALOR_COMPRA_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MayorValorCompraRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MayorValorMercadoRF"                       	,title: 'UI_MAYOR_VALOR_MERCADO_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MayorValorMercadoRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MayorValorMercadoRV"                       	,title: 'UI_MAYOR_VALOR_MERCADO_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MayorValorMercadoRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MenorValorCompraRF"                        	,title: 'UI_MENOR_VALOR_COMPRA_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MenorValorCompraRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MenorValorCompraRV"                        	,title: 'UI_MENOR_VALOR_COMPRA_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MenorValorCompraRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MenorValorMercadoRF"                       	,title: 'UI_MENOR_VALOR_MERCADO_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MenorValorMercadoRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MenorValorMercadoRV"                       	,title: 'UI_MENOR_VALOR_MERCADO_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MenorValorMercadoRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaPrincipalEquivalenteSpotCompra"  	,title: 'UI_VALOR_MONEDA_EQUIV_SPOT_COMPRA'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaPrincipalEquivalenteSpotCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaPrincipalEquivalenteSpotVenta"  	,title: 'UI_VALOR_MONEDA_EQUIV_SPOT_VENTA'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaPrincipalEquivalenteSpotVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaPrincipalSpotCompra"            	,title: 'UI_VALOR_MONEDA_PRINC_SPOT_COMPRA'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaPrincipalSpotCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }				
  		   ,{field: "ValorMonedaPrincipalSpotVenta"             	,title: 'UI_VALOR_MONEDA_PRINC_SPOT_VENTA'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaPrincipalSpotVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaSecundariaEquivalenteSpotCompra"  	,title: 'UI_VALOR_MONEDA_SEC_EQUIV_SPOT_COMPRA'	,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaSecundariaEquivalenteSpotCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaSecundariaEquivalenteSpotVenta"  	,title: 'UI_VALOR_MONEDA_SEC_EQUIV_SPOT_VENTA'	,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaSecundariaEquivalenteSpotVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaSecundariaSpotCompra"           	,title: 'UI_VALOR_MONEDA_SEC_SPOT_COMPRA'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaSecundariaSpotCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "ValorMonedaSecundariaSpotVenta"            	,title: 'UI_VALOR_MONEDA_SEC_SPOT_VENTA'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMonedaSecundariaSpotVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MontoCompraActualRF"                       	,title: 'UI_MONTO_COMPRA_ACTUAL_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoCompraActualRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MontoCompraAnteriorRF"                     	,title: 'UI_MONTO_COMPRA_ANT_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoCompraAnteriorRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MontoMercadoAnteriorRF"                    	,title: 'UI_MONTO_MERCADO_ANT_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoMercadoAnteriorRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		
  		   ,{field: "MontoCompra" 									,title: 'UI_MONTO_COMPRA'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		   									                   			,{field: "MontoDiferencia"                         		,title: 'UI_MONTO_DIFERENCIA'					,width: "120px",	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoDiferencia,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoEgreso"                         			,title: 'UI_MONTO_EGRESO'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>" }	
  		   ,{field: "MontoIngreso"                         			,title: 'UI_MONTO_INGRESO'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoLiquidacion"                         		,title: 'UI_MONTO_LIQUIDACION'					,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoLiquidacion,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoMayorValorCompraRF"						,title: 'UI_MONTO_MAYOR_VALOR_COMPRA_RF' 		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoMayorValorCompraRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoMayorValorMercadoRF"						,title: 'UI_MONTO_MAYOR_VALOR_MERCADO_RF'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoMayorValorMercadoRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoMenorValorCompraRF"						,title: 'UI_MONTO_MENOR_VALOR_COMPRA_RF'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoMenorValorCompraRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoMenorValorMercadoRF"						,title: 'UI_MONTO_MENOR_VALOR_MERCADO_RF'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoMenorValorMercadoRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoMercado"                         			,title: 'UI_MONTO_MERCADO'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoMercado,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoPacto"									,title: 'UI_MONTO_PACTO'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoPacto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                              
  		   ,{field: "MontoPapel"									,title: 'UI_MONTO_PAPEL'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoPapel,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoPresenteEmisionRF"						,title: 'UI_MONTO_PTE_EMISOR_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoPresenteEmisionRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "Perdida"										,title: 'UI_PERDIDA'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Perdida,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ReajusteAnteriorPactoCompra"					,title: 'UI_REAJ_ANT_PACTO_COMPRA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ReajusteAnteriorPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ReajusteAnteriorPactoVenta"					,title: 'UI_REAJ_ANT_PACTO_VENTA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ReajusteAnteriorPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ReajusteProximoPactoCompra"					,title: 'UI_REAJ_PROX_PACTO_COMPRA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ReajusteProximoPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ReajusteProximoPactoVenta"						,title: 'UI_REAJ_PROX_PACTO_VENTA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ReajusteProximoPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "RescateFondoMutuoCuentaPropia"					,title: 'UI_REAJ_FM_CP'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(RescateFondoMutuoCuentaPropia,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "RescateFondoMutuoCuentaTerceros"				,title: 'UI_REAJ_FM_CT'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(RescateFondoMutuoCuentaTerceros,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "SolicitudMontoEgreso"							,title: 'UI_SOLICITUD_MONTO_EGRESO'				,width: "120px",	hidden: true}
  		   ,{field: "SolicitudMontoIngreso"							,title: 'UI_SOLICITUD_MONTO_INGRESO'			,width: "120px",	hidden: true}
  		   ,{field: "SolicitudMontoTraspasoEgreso"					,title: 'UI_SOLICITUD_MONTO_TRASP_EGRESO'		,width: "120px",	hidden: true}
  		   ,{field: "SolicitudMontoTraspasoIngreso"					,title: 'UI_SOLICITUD_MONTO_TRASP_INGRESO'		,width: "120px",	hidden: true}
  		   ,{field: "Tasa"											,title: 'UI_TASA'								,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TasadeCambio"									,title: 'UI_TASA_DE_CAMBIO'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TasadeCambio,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalVenta"									,title: 'UI_TOTAL_VENTA'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "Utilidad"										,title: 'UI_UTILIDAD'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Utilidad,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ValorCompraActualRF"							,title: 'UI_VALOR_COMPRA_ACTUAL_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorCompraActualRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                     
  		   ,{field: "ValorCompraActualRV"							,title: 'UI_VALOR_COMPRA_ACTUAL_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorCompraActualRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                     
  		   ,{field: "ValorMercadoActualRV"							,title: 'UI_VALOR_MERCADO_ACTUAL_RV'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMercadoActualRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                    
  		   ,{field: "ValorActualPactoCompra"						,title: 'UI_VALOR_ACTUAL_PACTO_COMPRA'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorActualPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                  
  		   ,{field: "ValorActualPactoVenta"							,title: 'UI_VALOR_ACTUAL_PACTO_VENTA'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorActualPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                   
  		   ,{field: "ValoraTasaSubyacenteCompraActual"				,title: 'UI_VALOR_TASA_SUB_COMPRA_ACTUAL'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraTasaSubyacenteCompraActual,'GEN','Monto',IdMoneda,'','')).valor #</label>" }        
  		   ,{field: "ValoraTasaSubyacenteVentaActual"				,title: 'UI_VALOR_TASA_SUB_VENTA_ACTUAL'		,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraTasaSubyacenteVentaActual,'GEN','Monto',IdMoneda,'','')).valor #</label>" }         
  		   ,{field: "ValorCompraAnteriorRF"							,title: 'UI_VALOR_COMPRA_ANT_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorCompraAnteriorRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                   
  		   ,{field: "ValorCompraAnteriorRV"							,title: 'UI_VALOR_COMPRA_ANT_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorCompraAnteriorRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                   
  		   ,{field: "ValorMercadoAnteriorRF"						,title: 'UI_VALOR_MERCADO_ANT_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMercadoAnteriorRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                  
  		   ,{field: "ValorMercadoAnteriorRV"						,title: 'UI_VALOR_MERCADO_ANT_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMercadoAnteriorRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                  
  		   ,{field: "ValorAnteriorPactoCompra"						,title: 'UI_VALOR_ANT_PACTP_COMPRA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorAnteriorPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                
  		   ,{field: "ValorAnteriorPactoVenta"						,title: 'UI_VALOR_ANT_PACTP_VENTA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorAnteriorPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                 
  		   ,{field: "ValoraTasaSubyacenteCompraAnterior"			,title: 'UI_VALOR_TASA_SUB_COMPRA_ANT'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraTasaSubyacenteCompraAnterior,'GEN','Monto',IdMoneda,'','')).valor #</label>" }      
  		   ,{field: "ValoraTasaSubyacenteVentaAnterior"				,title: 'UI_VALOR_TASA_SUB_VENTA_ANT'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraTasaSubyacenteVentaAnterior,'GEN','Monto',IdMoneda,'','')).valor #</label>" }       
  		   ,{field: "ValorCompra"									,title: 'UI_VALOR_COMPRA'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                             
  		   ,{field: "ValorMercado"									,title: 'UI_VALOR_MERCADO'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorMercado,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                            
  		   ,{field: "ValorPresenteEmisionRF"						,title: 'UI_VALOR_PTE_EMISOR_RF'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorPresenteEmisionRF,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                  
  		   ,{field: "ValorPromedioCompraRV"							,title: 'UI_VALOR_PROM_COMPRA_RV'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorPromedioCompraRV,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                   
  		   ,{field: "ValorProximoPactoCompra"						,title: 'UI_VALOR_PROX_PACTP_COMPRA'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorProximoPactoCompra,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                 
  		   ,{field: "ValorProximoPactoVenta"						,title: 'UI_VALOR_PROX_PACTP_VENTA'				,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValorProximoPactoVenta,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                  
  		   ,{field: "ValoraTasaSubyacenteCompraProximo"				,title: 'UI_VALOR_TASA_SUB_COMPRA_PROX'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraTasaSubyacenteCompraProximo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }       
  		   ,{field: "ValoraTasaSubyacenteVentaProximo"				,title: 'UI_VALOR_TASA_SUB_VENTA_PROX'			,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraTasaSubyacenteVentaProximo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }        
  		   ,{field: "ValoraRazonableActivo"							,title: 'UI_VALOR_RAZON_ACTIVO'					,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraRazonableActivo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                   
  		   ,{field: "ValoraRazonableNeto"							,title: 'UI_VALOR_RAZON_NETO'					,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraRazonableNeto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }                     
  		   ,{field: "ValoraRazonablePasivo"							,title: 'UI_VALOR_RAZON_PASIVO'					,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ValoraRazonablePasivo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ComisionGenerica"								,title: 'COMISION_GENERICA'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ComisionGenerica,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ComisionCavali"								,title: 'COMISION_CAVALI'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ComisionCavali,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ComisionBolsaValores"							,title: 'COMISION_BVL'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ComisionBolsaValores,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "FondoGarantia"									,title: 'FONDO_GARANTIA'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(FondoGarantia,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ComisionSmv"									,title: 'COMISION_SMV'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ComisionSmv,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "Igv"											,title: 'IMPUESTO_IGV'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Igv,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "ComisionSab"									,title: 'COMISION_SAB'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(ComisionSab,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoInversion"								,title: 'MONTO_INVERSION'						,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoInversion,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "TotalPoliza"									,title: 'TOTAL_POLIZA'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(TotalPoliza,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "MontoBruto"									,title: 'MONTO_BRUTO'							,width: "120px",	hidden: true,	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoBruto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
  		   ,{field: "AliasBancoCaja"								,title: 'UI_ALIAS_COD_CAJA'						,width: "120px",	hidden: true}
  		   ,{field: "Canal"											,title: 'UI_CANAL' 								,width: "100",		hidden: true}
  		   ,{field: "CentroCosto"          							,title: 'UI_CENTRO_COSTO' 						,width: "100",		hidden: true}
  		   ,{field: "CentroCostoAgente"    							,title: 'UI_CENTRO_COSTO_AGENTE' 				,width: "100",		hidden: true}
  		   ,{field: "ClaseEmisor"          							,title: 'UI_CLASE_EMISOR' 						,width: "100",		hidden: true}
  		   ,{field: "ClaseInstrumento"     							,title: 'UI_CLASE_INSTRUMENTO' 					,width: "100",		hidden: true}
  		   ,{field: "CodigoAgente"         							,title: 'UI_CODIGO_AGENTE' 						,width: "100",		hidden: true}
  		   ,{field: "CodigoBolsa"          							,title: 'UI_CODIGO_BOLSA' 						,width: "100",		hidden: true}
  		   ,{field: "CodigoCaja"           							,title: 'UI_CODIGO_CAJA' 						,width: "100",		hidden: true}
  		   ,{field: "CodigoIFRS"           							,title: 'UI_CODIGO_IFRS' 						,width: "100",		hidden: true}
  		   ,{field: "CodigoProducto"       							,title: 'UI_CODIGO_PRODUCTO' 					,width: "100",		hidden: true}
  		   ,{field: "CodigoSucursal"       							,title: 'UI_CODIGO_SUCURSAL' 					,width: "100",		hidden: true}
  		   ,{field: "CodigoTipoOperacion"  							,title: 'UI_CODIGO_TIPO_OPERACION' 				,width: "100",		hidden: true}
  		   ,{field: "CodSecuencia"         							,title: 'UI_CODIGO_SECUENCIA' 					,width: "100",		hidden: true}
  		   ,{field: "CuentaPropia"         							,title: 'UI_CUENTA_PROPIA' 						,width: "100",		hidden: true}
  		   ,{field: "DiasLiquidacion"         						,title: 'UI_DIAS_LIQUIDACION' 					,width: "100",		hidden: true}
  		   ,{field: "DiasVencido"         							,title: 'UI_DIAS_VENCIDO' 						,width: "100",		hidden: true}
  		   ,{field: "DiasVencimiento"         						,title: 'UI_DIAS_VENCIMIENTO'					,width: "100",		hidden: true}
  		   ,{field: "Emisor"         								,title: 'UI_EMISOR' 							,width: "100",		hidden: true}
  		   ,{field: "EmisorInstrumento"         					,title: 'UI_EMISOR_INSTRUMENTO'					,width: "100",		hidden: true}
  		   ,{field: "EmitidoA"         								,title: 'UI_EMITIDO_A' 							,width: "100",		hidden: true}
  		   ,{field: "EsBilateral"         							,title: 'UI_BILATERAL' 							,width: "100",		hidden: true}
  		   ,{field: "EsCuentaDepositante"         					,title: 'UI_CUENTA-DEPOSITANTE' 				,width: "100",		hidden: true}
  		   ,{field: "EsCuentaMandante"         						,title: 'UI_CUENTA_MANDANTE' 					,width: "100",		hidden: true}
  		   ,{field: "FamiliaInstrumento"         					,title: 'UI_FAMILIA_INSTRUMENTO' 				,width: "100",		hidden: true}
  		   ,{field: "FamiliaTipoInstrumento"         				,title: 'UI_FAMILIA_TIPO_INSTRUMENTO' 			,width: "100",		hidden: true}
  		   ,{field: "Fecha"         								,title: 'UI_FECHA'			 					,width: "100",		hidden: true}
  		   ,{field: "FechaActual"         							,title: 'UI_FECHA_ACTUAL'	 					,width: "100",		hidden: true}
  		   ,{field: "FechaAnterior"         						,title: 'UI_FECHA_ANTERIOR' 					,width: "100",		hidden: true}
  		   ,{field: "FechaDeVencimiento"         					,title: 'UI_FECHA_DE_VENCIMIENTO'				,width: "100",		hidden: true}
  		   ,{field: "FechaDocumentoPago"         					,title: 'UI_FECHA_DOC_PAGO'						,width: "100",		hidden: true}
  		   ,{field: "FechaEmisionDocumento"         				,title: 'UI_FECHA_EMISION_DOCUMENTO'			,width: "100",		hidden: true}
  		   ,{field: "FechaLiquidacion"         						,title: 'UI_FECHA_LIQUIDACION'					,width: "100",		hidden: true}
  		   ,{field: "FechaLiquidacionDocumento"         			,title: 'UI_FECHA_LIQ_DOCUMENTO'				,width: "100",		hidden: true}
  		   ,{field: "Folio"         								,title: 'UI_FOLIO'								,width: "100",		hidden: true}
  		   ,{field: "FolioCartera"         							,title: 'UI_FOLIO_CARTERA'						,width: "100",		hidden: true}
  		   ,{field: "FolioCmbteCont"         						,title: 'UI_FOLIO_COMPROBANTE_CONT'				,width: "100",		hidden: true}
  		   ,{field: "FolioComprobanteAdj"         					,title: 'UI_FOLIO_COMPROBANTE_ADJ'				,width: "100",		hidden: true}
  		   ,{field: "FolioOperacion"         						,title: 'UI_FOLIO_OPERACION'					,width: "100",		hidden: true}
  		   ,{field: "IdCartera"         							,title: 'UI_CARTERA'							,width: "100",		hidden: true}
  		   ,{field: "IdEmisor"         								,title: 'UI_ID_EMISOR'							,width: "100",		hidden: true}
  		   ,{field: "IdGarantia"         							,title: 'UI_GARANTIA'							,width: "100",		hidden: true}
  		   ,{field: "IdSubyacente"         							,title: 'UI_SUBYACENTE'							,width: "100",		hidden: true}
  		   ,{field: "IdTipoReajuste"         						,title: 'UI_ID_TIPO_RAJUSTE'					,width: "100",		hidden: true}
  		   ,{field: "IndicadorLiquidacion"         					,title: 'UI_INDICADOR_LIQUIDACION'				,width: "100",		hidden: true}
  		   ,{field: "IndicadorSimultanea"         					,title: 'UI_INDICADOR_SIMULTANEA'				,width: "100",		hidden: true}
  		   ,{field: "ISIN"         									,title: 'UI_ISIN'								,width: "100",		hidden: true}
  		   ,{field: "MedioDePago"         							,title: 'UI_MEDIO_PAGO'							,width: "100",		hidden: true}
  		   ,{field: "Modalidad"         							,title: 'UI_MODALIDAD'							,width: "100",		hidden: true}
  		   ,{field: "Moneda"         								,title: 'UI_MONEDA'								,width: "100",		hidden: true}
  		   ,{field: "MonedaComprobante"         					,title: 'UI_MONEDA_COMPROBANTE'					,width: "100",		hidden: true}
  		   ,{field: "MonedaLiquidacion"         					,title: 'UI_MONEDA_LIQUIDACION'					,width: "100",		hidden: true}
  		   ,{field: "Monto"         								,title: 'UI_MONTO'								,width: "100",		hidden: true}
  		   ,{field: "Nemo"         									,title: 'UI_NEMO'								,width: "100",		hidden: true}
  		   ,{field: "Nemotecnico"         							,title: 'UI_NEMOTECNICO'						,width: "100",		hidden: true}
  		   ,{field: "NombreAgente"         							,title: 'UI_NOMBRE_AGENTE'						,width: "300",		hidden: true}
  		   ,{field: "NombreCuentaCorriente"         				,title: 'UI_NOMBRE_CTA_CTE'						,width: "100",		hidden: true}
  		   ,{field: "NombreSucursalAgente"         					,title: 'UI_NOMBRE_SUCURSAL_AGENTE'				,width: "250",		hidden: true}
  		   ,{field: "NumeroComprobante"         					,title: 'UI_NUMERO_COMPROBANTE'					,width: "100",		hidden: true}
  		   ,{field: "NumeroCuentaCorriente"         				,title: 'UI_NUMERO_CTA_CTE'						,width: "100",		hidden: true}
  		   ,{field: "NumeroDocumentoPago"         					,title: 'UI_NUMERO_DOC_PAGO'					,width: "100",		hidden: true}
  		   ,{field: "PartidaLiquidacion"         					,title: 'UI_PARTIDA_LIQ'						,width: "100",		hidden: true}
  		   ,{field: "PrecioCompra"         							,title: 'UI_PRECIO_COMPRA'						,width: "100",		hidden: true}
  		   ,{field: "PrecioMercadoActual"         					,title: 'UI_PRECIO_MERCADO_ACTUAL'				,width: "100",		hidden: true}
  		   ,{field: "PrecioMercadoAnterior"         				,title: 'UI_PRECIO_MERCADO_ANTERIOR'			,width: "100",		hidden: true}
  		   ,{field: "Proceso"         								,title: 'UI_PROCESO'							,width: "100",		hidden: true}
  		   ,{field: "ProductoMovimiento"         					,title: 'UI_PRODUCTO_MOVIMIENTO'				,width: "100",		hidden: true}
  		   ,{field: "Relacion"         								,title: 'UI_RELACION'							,width: "100",		hidden: true}
  		   ,{field: "RelacionadoEmpresa"         					,title: 'UI_RELACION_EMPRESA'					,width: "100",		hidden: true}
  		   ,{field: "RelacionCorredora"         					,title: 'UI_RELACION_CORREDORA'					,width: "100",		hidden: true}
  		   ,{field: "SerieDocumento"         						,title: 'UI_SERIE_DOCUMENTO'					,width: "100",		hidden: true}
  		   ,{field: "SucursalAgente"         						,title: 'UI_SUCURSAL_AGENTE'					,width: "100",		hidden: true}
  		   ,{field: "TasaCambio"         							,title: 'UI_TASA_CAMBIO'						,width: "100",		hidden: true}
  		   ,{field: "TasaCompra"         							,title: 'UI_TASA_COMPRA'						,width: "100",		hidden: true}
  		   ,{field: "TasaEmision"         							,title: 'UI_TASA_EMISION'						,width: "100",		hidden: true}
  		   ,{field: "TasaMercadoActual"         					,title: 'UI_TASA_MERCADO_ACTUAL'				,width: "100",		hidden: true}
  		   ,{field: "TasaMercadoAnterior"         					,title: 'UI_TASA_MERCADO_ANTERIOR'				,width: "100",		hidden: true}
  		   ,{field: "TipoCambio"         							,title: 'UI_TIPO_CAMBIO'						,width: "100",		hidden: true}
  		   ,{field: "TipoCartera"         							,title: 'UI_TIPO_CARTERA'						,width: "100",		hidden: true}
  		   ,{field: "TipoCmbteCont"         						,title: 'UI_TIPO_COMPROBANTE_CONT'				,width: "100",		hidden: true}
  		   ,{field: "TipoComprobante"         						,title: 'UI_TIPO_COMPROBANTE'					,width: "100",		hidden: true}
  		   ,{field: "TipoComprobanteAdj"         					,title: 'UI_TIPO_DOCUMENTO_ADJ'					,width: "100",		hidden: true}
  		   ,{field: "TipoDocumento"         						,title: 'UI_TIPO_DOCUMENTO'						,width: "100",		hidden: true}
  		   ,{field: "TipoEmisor"         							,title: 'UI_TIPO_EMISOR'						,width: "100",		hidden: true}
  		   ,{field: "TipoFamilia"         							,title: 'UI_TIPO_FAMILIA'						,width: "100",		hidden: true}
  		   ,{field: "TipoInstrumento"         						,title: 'UI_TIPO_INSTRUMENTO'					,width: "100",		hidden: true}
  		   ,{field: "TipoInstrumentoGarantizado"         			,title: 'UI_TIPO_INSTRUMENTO_GAR'				,width: "100",		hidden: true}
  		   ,{field: "TipoMovimiento"         						,title: 'UI_TIPO_MOVIMIENTO'					,width: "100",		hidden: true}
  		   ,{field: "TipoOperacion"         						,title: 'UI_TIPO_OPERACION'						,width: "100",		hidden: true}
  		   ,{field: "TipoPersona"         							,title: 'UI_TIPO_PERSONA'						,width: "100",		hidden: true}
  		   ,{field: "TipoReajuste"         							,title: 'UI_TIPO_REAJUSTE'						,width: "100",		hidden: true}
  		   ,{field: "TituloOCuentaCorriente"         				,title: 'UI_TIT_O_CTA_CTE'						,width: "100",		hidden: true}
  		   ,{field: "Valor"         								,title: 'UI_VALOR'								,width: "100",		hidden: true}
  		   ,{field: "Valuta"         								,title: 'UI_VALUTA'								,width: "100",		hidden: true}
  		   ,{field: "VentaCorta"         							,title: 'UI_VENTA_CORTA'						,width: "100",		hidden: true}
  		   ,{field: "EstadoSUNAT"         							,title: 'UI_ESTADO_SUNAT'						,width: "100",		hidden: true}
  		   ,{field: "TipoBonoIRF"         							,title: 'UI_TIPO_BONO'							,width: "100",		hidden: true}
  		   ,{field: "OrigenInstrumento"         					,title: 'UI_ORIGEN_INSTRUMENTO'					,width: "100",		hidden: true}
  		   ,{field: "IndicadorRelacionadoGrupo"         			,title: 'UI_RELACIONADO_GRUPO'					,width: "100",		hidden: true}
  		   ,{field: "Plaza"         								,title: 'UI_PLAZA'								,width: "100",		hidden: true}
  		   ,{field: "PactoAndino"         							,title: 'UI_PACTO_ANDINO'						,width: "100",		hidden: true}
  		   ,{field: "FormadorMercado"         						,title: 'UI_FORMADOR_MERCADO'					,width: "100",		hidden: true}
  		   ,{field: "PresenciaBursatil"         					,title: 'UI_PRESENCIA_BURSATIL'					,width: "100",		hidden: true}
  		   ,{field: "TipoInstrumentoVenta"         					,title: 'UI_TIPO_INSTRUMENTO_VENTA'				,width: "100",		hidden: true}
  		   ,{field: "TipoInstrumentoCompra"         				,title: 'UI_TIPO_INSTRUMENTO_COMPRA'			,width: "100",		hidden: true}
  		   ,{field: "FolioPoliza"         							,title: 'UI_FOLIO_POLIZA'						,width: "100",		hidden: true}
		   ,{field: "NombreTipoContabilidad"         				,title: 'UI_COMPROBANTE'						,width: "100",		hidden: true}
		];
    };
    
    
    $scope.detalle.confBasicaDetalle= {
		dataSource			: dsGrillaDetalle,
		columns				: columnas(),
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		filterable			: true,
		height				: 470,
		selectable 			: "single row",
		autoBind			: $scope.data.oDatosDetalle.Filtrado || true,
		gridCreated 		: function(grid) {
			$scope.grillaAll = grid;
			
			if ($scope.SesionLocale === 'PE')
			{
				grid.showColumn("MontoDebe");
				grid.showColumn("MontoHaber");
				grid.showColumn("Ejercicio");
				grid.showColumn("DocumentoIdentidad");
				grid.showColumn("Portafolio");
				grid.showColumn("NombreCliente");
				grid.showColumn("EsCuentaPropia");
				grid.showColumn("NombreEstado");
				grid.showColumn("Cuenta");
				grid.showColumn("MontoCobro");
				grid.showColumn("TotalCobros");
				grid.showColumn("MontoLiquidacionCobros");
				grid.showColumn("MontoFinalComprobante");
				grid.showColumn("MontoNeto");
				grid.showColumn("TotalComisiones");
				grid.showColumn("TotalCompra");
				grid.showColumn("TotalDerechos");
				grid.showColumn("TotalGastos");
				grid.showColumn("TotalImpuestos");
				grid.showColumn("Agente");
				grid.showColumn("Bolsa");
				grid.showColumn("CompraVenta");
				grid.showColumn("Concepto");
				grid.showColumn("NombreConcepto");
				grid.showColumn("CondicionLiquidacion");
				grid.showColumn("EsExento");
				grid.showColumn("EsRelacionado");
				grid.showColumn("FechaDocumento");
				grid.showColumn("IdEstadoSII");
				grid.showColumn("Operacion");
				grid.showColumn("Producto");
				grid.showColumn("AporteFondoMutuoCuentaPropia");
				grid.showColumn("AporteFondoMutuoCuentaTerceros");
				grid.showColumn("DocumentoMontoEgreso");
				grid.showColumn("DocumentoMontoIngreso");
				grid.showColumn("DocumentoMontoTraspasoEgreso");
				grid.showColumn("DocumentoMontoTraspasoIngreso");
				grid.showColumn("InteresAnteriorPactoCompra");
				grid.showColumn("InteresAnteriorPactoVenta");
				grid.showColumn("InteresProximoPactoCompra");
				grid.showColumn("InteresProximoPactoVenta");
				grid.showColumn("MayorValorCompraRF");
				grid.showColumn("MayorValorCompraRV");
				grid.showColumn("MayorValorMercadoRF");
				grid.showColumn("MayorValorMercadoRV");
				grid.showColumn("MenorValorCompraRF");
				grid.showColumn("MenorValorCompraRV");
				grid.showColumn("MenorValorMercadoRF");
				grid.showColumn("MenorValorMercadoRV");
				grid.showColumn("ValorMonedaPrincipalEquivalenteSpotCompra");
				grid.showColumn("ValorMonedaPrincipalEquivalenteSpotVenta");
				grid.showColumn("ValorMonedaPrincipalSpotCompra");
				grid.showColumn("ValorMonedaPrincipalSpotVenta");
				grid.showColumn("ValorMonedaSecundariaEquivalenteSpotCompra");
				grid.showColumn("ValorMonedaSecundariaEquivalenteSpotVenta");
				grid.showColumn("ValorMonedaSecundariaSpotCompra");
				grid.showColumn("ValorMonedaSecundariaSpotVenta");
				grid.showColumn("MontoCompraActualRF");
				grid.showColumn("MontoCompraAnteriorRF");
				grid.showColumn("MontoMercadoAnteriorRF");
				grid.showColumn("MontoCompra");
				grid.showColumn("MontoEgreso");
				grid.showColumn("MontoIngreso");
				grid.showColumn("MontoLiquidacion");
				grid.showColumn("MontoMayorValorCompraRF");
				grid.showColumn("MontoMayorValorMercadoRF");
				grid.showColumn("MontoMenorValorCompraRF");
				grid.showColumn("MontoMenorValorMercadoRF");
				grid.showColumn("MontoMercado");
				grid.showColumn("MontoPacto");
				grid.showColumn("MontoPapel");
				grid.showColumn("MontoPresenteEmisionRF");
				grid.showColumn("Perdida");
				grid.showColumn("ReajusteAnteriorPactoCompra");
				grid.showColumn("ReajusteAnteriorPactoVenta");
				grid.showColumn("ReajusteProximoPactoCompra");
				grid.showColumn("ReajusteProximoPactoVenta");
				grid.showColumn("RescateFondoMutuoCuentaPropia");
				grid.showColumn("RescateFondoMutuoCuentaTerceros");
				grid.showColumn("SolicitudMontoEgreso");
				grid.showColumn("SolicitudMontoIngreso");
				grid.showColumn("SolicitudMontoTraspasoEgreso");
				grid.showColumn("SolicitudMontoTraspasoIngreso");
				grid.showColumn("Tasa");
				grid.showColumn("TasadeCambio");
				grid.showColumn("TotalVenta");
				grid.showColumn("Utilidad");
				grid.showColumn("ValorCompraActualRF");
				grid.showColumn("ValorCompraActualRV");
				grid.showColumn("ValorMercadoActualRV");
				grid.showColumn("ValorActualPactoCompra");
				grid.showColumn("ValorActualPactoVenta");
				grid.showColumn("ValoraTasaSubyacenteCompraActual");
				grid.showColumn("ValoraTasaSubyacenteVentaActual");
				grid.showColumn("ValorCompraAnteriorRF");
				grid.showColumn("ValorCompraAnteriorRV");
				grid.showColumn("ValorMercadoAnteriorRF");
				grid.showColumn("ValorMercadoAnteriorRV");
				grid.showColumn("ValorAnteriorPactoCompra");
				grid.showColumn("ValorAnteriorPactoVenta");
				grid.showColumn("ValoraTasaSubyacenteCompraAnterior");
				grid.showColumn("ValoraTasaSubyacenteVentaAnterior");
				grid.showColumn("ValorCompra");
				grid.showColumn("ValorMercado");
				grid.showColumn("ValorPresenteEmisionRF");
				grid.showColumn("ValorPromedioCompraRV");
				grid.showColumn("ValorProximoPactoCompra");
				grid.showColumn("ValorProximoPactoVenta");
				grid.showColumn("ValoraTasaSubyacenteCompraProximo");
				grid.showColumn("ValoraTasaSubyacenteVentaProximo");
				grid.showColumn("ValoraRazonableActivo");
				grid.showColumn("ValoraRazonableNeto");
				grid.showColumn("ValoraRazonablePasivo");
				grid.showColumn("ComisionGenerica");
				grid.showColumn("ComisionCavali");
				grid.showColumn("ComisionBolsaValores");
				grid.showColumn("FondoGarantia");
				grid.showColumn("ComisionSmv");
				grid.showColumn("Igv");
				grid.showColumn("ComisionSab");
				grid.showColumn("MontoInversion");
				grid.showColumn("TotalPoliza");
				grid.showColumn("MontoBruto");
				grid.showColumn("AliasBancoCaja");
				grid.showColumn("Canal");
				grid.showColumn("CentroCosto");
				grid.showColumn("CentroCostoAgente");
				grid.showColumn("ClaseEmisor");
				grid.showColumn("ClaseInstrumento");
				grid.showColumn("CodigoAgente");
				grid.showColumn("CodigoBolsa");
				grid.showColumn("CodigoCaja");
				grid.showColumn("CodigoIFRS");
				grid.showColumn("CodigoProducto");
				grid.showColumn("CodigoSucursal");
				grid.showColumn("CodigoTipoOperacion");
				grid.showColumn("CodSecuencia");
				grid.showColumn("CuentaPropia");
				grid.showColumn("DiasLiquidacion");
				grid.showColumn("DiasVencido");
				grid.showColumn("DiasVencimiento");
				grid.showColumn("Emisor");
				grid.showColumn("EmisorInstrumento");
				grid.showColumn("EmitidoA");
				grid.showColumn("EsBilateral");
				grid.showColumn("EsCuentaDepositante");
				grid.showColumn("EsCuentaMandante");
				grid.showColumn("FamiliaInstrumento");
				grid.showColumn("FamiliaTipoInstrumento");
				grid.showColumn("Fecha");
				grid.showColumn("FechaActual");
				grid.showColumn("FechaAnterior");
				grid.showColumn("FechaDeVencimiento");
				grid.showColumn("FechaDocumentoPago");
				grid.showColumn("FechaEmisionDocumento");
				grid.showColumn("FechaLiquidacion");
				grid.showColumn("FechaLiquidacionDocumento");
				grid.showColumn("Folio");
				grid.showColumn("FolioCartera");
				grid.showColumn("FolioCmbteCont");
				grid.showColumn("FolioComprobanteAdj");
				grid.showColumn("FolioOperacion");
				grid.showColumn("IdCartera");
				grid.showColumn("IdEmisor");
				grid.showColumn("IdGarantia");
				grid.showColumn("IdSubyacente");
				grid.showColumn("IdTipoReajuste");
				grid.showColumn("IndicadorLiquidacion");
				grid.showColumn("IndicadorSimultanea");
				grid.showColumn("ISIN");
				grid.showColumn("MedioDePago");
				grid.showColumn("Modalidad");
				grid.showColumn("Moneda");
				grid.showColumn("MonedaComprobante");
				grid.showColumn("MonedaLiquidacion");
				grid.showColumn("Monto");
				grid.showColumn("Nemo");
				grid.showColumn("Nemotecnico");
				grid.showColumn("NombreAgente");
				grid.showColumn("NombreCuentaCorriente");
				grid.showColumn("NombreSucursalAgente");
				grid.showColumn("NumeroComprobante");
				grid.showColumn("NumeroCuentaCorriente");
				grid.showColumn("NumeroDocumentoPago");
				grid.showColumn("PartidaLiquidacion");
				grid.showColumn("PrecioCompra");
				grid.showColumn("PrecioMercadoActual");
				grid.showColumn("PrecioMercadoAnterior");
				grid.showColumn("Proceso");
				grid.showColumn("ProductoMovimiento");
				grid.showColumn("Relacion");
				grid.showColumn("RelacionadoEmpresa");
				grid.showColumn("RelacionCorredora");
				grid.showColumn("SerieDocumento");
				grid.showColumn("SucursalAgente");
				grid.showColumn("TasaCambio");
				grid.showColumn("TasaCompra");
				grid.showColumn("TasaEmision");
				grid.showColumn("TasaMercadoActual");
				grid.showColumn("TasaMercadoAnterior");
				grid.showColumn("TipoCambio");
				grid.showColumn("TipoCartera");
				grid.showColumn("TipoCmbteCont");
				grid.showColumn("TipoComprobante");
				grid.showColumn("TipoComprobanteAdj");
				grid.showColumn("TipoDocumento");
				grid.showColumn("TipoEmisor");
				grid.showColumn("TipoFamilia");
				grid.showColumn("TipoInstrumento");
				grid.showColumn("TipoInstrumentoGarantizado");
				grid.showColumn("TipoMovimiento");
				grid.showColumn("TipoOperacion");
				grid.showColumn("TipoPersona");
				grid.showColumn("TipoReajuste");
				grid.showColumn("TituloOCuentaCorriente");
				grid.showColumn("Valor");
				grid.showColumn("Valuta");
				grid.showColumn("VentaCorta");
				grid.showColumn("EstadoSUNAT");
				grid.showColumn("TipoBonoIRF");
				grid.showColumn("OrigenInstrumento");
				grid.showColumn("IndicadorRelacionadoGrupo");
				grid.showColumn("Plaza");
				grid.showColumn("PactoAndino");
				grid.showColumn("FormadorMercado");
				grid.showColumn("PresenciaBursatil");
				grid.showColumn("TipoInstrumentoVenta");
				grid.showColumn("TipoInstrumentoCompra");
				grid.showColumn("FolioPoliza");
				grid.showColumn("NombreTipoContabilidad");
			}
			else
			{
				grid.showColumn("IdPreComprobanteAsiento");
				grid.showColumn("MontoHaberDetalle");
				grid.showColumn("MontoDebeDetalle");
				grid.showColumn("Evento");
				grid.showColumn("GlosaDetalle");
				grid.showColumn("DocumentoIdentidad");
				grid.showColumn("CodigoPortafolio");
			}
		}		
	};
});
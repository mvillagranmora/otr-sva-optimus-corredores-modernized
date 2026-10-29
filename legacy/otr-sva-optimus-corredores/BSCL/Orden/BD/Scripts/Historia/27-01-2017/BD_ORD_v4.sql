SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_CobrosModificados";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Garantias";

DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle";

DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Ordenes";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Cobros";
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Errores";

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes"(
	"IdInterno"	 						BIGINT,
	"FechaRecepcion"					NVARCHAR(19),
	"FechaVigencia"						NVARCHAR(10),
	"FechaEmision"						NVARCHAR(10),
	"FechaOperacion"					NVARCHAR(19),
	"IdTipoVigencia"					NVARCHAR(2),
	"Glosa"								NVARCHAR(520),
	"Cantidad"							DECIMAL(26,8),
	"MontoNeto"							DECIMAL(26,8),
	"ValorRescate"						DECIMAL(26,8),
	"IdTipoCustodia"					NVARCHAR(3),
	"IdFormaCustodia"					NVARCHAR(3),
	"IdPorcentajeVisibilidad"			BIGINT,
	"PrecioLimite"						DECIMAL(26,8),
	"PrecioMercado"						DECIMAL(26,8),
	"EsFacturaInmediata"				NVARCHAR(1),
	"IdTipoPrecio"						NVARCHAR(1),
	"IdPortafolio"						BIGINT,
	"IdTipoLibroOrden"					NVARCHAR(40),
	"IdTipoCanalIngreso"				NVARCHAR(3),
	"IdTipoReajuste"					NVARCHAR(3),
	"IdEmisor"							NVARCHAR(40),
	"IdBolsa"							NVARCHAR(3),
	"EspecificaBolsa"					NVARCHAR(1),
	"IdMonedaLiquidacionOrden"			NVARCHAR(3),
	"IdMonedaLiquidacionCobros"			NVARCHAR(3),
	"TipoCambio"						DECIMAL(26,8),
	"IdInstrumento"						BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"IdTipoOperacion"					NVARCHAR(6),
	"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
	"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
	"TasaVentaCorta"					DECIMAL(26,8),
	"TasaSimultanea"					DECIMAL(26,8),
	"TasaRFIF"							DECIMAL(26,8),
	"PlazoIF"							BIGINT,
	"PlazoSimultanea"					BIGINT,
	"PlazoVentaCorta"					BIGINT,
	"EsVentaCorta"						NVARCHAR(1),
	"EsOD"								NVARCHAR(1),
	"EsFungible"						NVARCHAR(1),
	"IdCorredorCorresponsal"			BIGINT,
	"MontoPlazo"						DECIMAL(26,8),
	"IdPersonaAutorizadoDarOrdenes"		BIGINT,
	"IdTipoCanalDistribucion"			NVARCHAR(3)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_CobrosModificados"(
	"IdInterno"				BIGINT,
	"IdCobro"				NVARCHAR(6),
	"ValorCobroAplicado"	DECIMAL(26,8)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Garantias"(
	"IdInterno"						BIGINT,
	"IdInstrumento"					BIGINT,		--INSTRUMENTO DE LA GARANTIA
	"IdInstrumentoGarantizado"		BIGINT,		--INSTRUMENTO A GARANTIZAR (SIMULTANEA COMPRA)
	"IdCliente"						BIGINT,
	"IdPortafolio"					BIGINT,
	"IdTipoInstrumento"				NVARCHAR(3), --TIPO DE INSTRUMENTO A GARANTIZAR (SIM)
	"Monto"							DECIMAL(26,8),
	"Cantidad"						DECIMAL(26,8),
	"Precio"						DECIMAL(26,8),
	"IdBolsa"						NVARCHAR(3),
	"EsFungible"					NVARCHAR(1),
	"IdEmisor"						NVARCHAR(40),
	"NemoSVS"						NVARCHAR(40),
	"IdTipoReajuste"				NVARCHAR(3),
	"FechaEmision"					NVARCHAR(10),
	"FechaVencimiento"				NVARCHAR(10)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado"(
	"IdInterno"				BIGINT,
	"IdOrden"				BIGINT,
	"DocumentoIdentidad"	NVARCHAR(40),
	"FechaOperacionOrden"	SECONDDATE,
	"Cantidad"				DECIMAL(26,8),
	"MontoNeto"				DECIMAL(26,8),
	"MontoLiquidacionOperacion"	DECIMAL(26,8),
	"TotalCobros"			DECIMAL(28,8),
	"MontoTotal"			DECIMAL(29,8),
	"PrecioOrden"			DECIMAL(26,8),
	"Instrumento"			NVARCHAR(40),
	"InstrumentoSubyacente"	NVARCHAR(40),
	"NombreTipoOperacion"	NVARCHAR(100),
	"IdTipoOperacion"					NVARCHAR(6),
	"IdMonedaOrden"						NVARCHAR(3),
	"IdMonedaLiquidacionOrden"			NVARCHAR(3),
	"IdMonedaLiquidacionCobros"			NVARCHAR(3),
	"EsVentaCorta"						NVARCHAR(1),
	"AccionOrden"						NVARCHAR(40),
	"IdCondicionDeLiquidacion"			NVARCHAR(2),
	"NombreCondicionDeLiquidacion"		NVARCHAR(150),
	"IdTipoPrecio"						NVARCHAR(1),
	"NombreTipoPrecio"					NVARCHAR(100),
	"IdTipoVigenciaOrden"				NVARCHAR(2),
	"NombreTipoVigenciaOrden"			NVARCHAR(100),
	"FechaVigencia"						DATE,
	"Tasa"								DECIMAL(26,8),
	"Plazo"								DECIMAL(27,8),
	"NombreTipoInstrumento"				NVARCHAR(100),
	"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
	"IdTipoInstrumento" 				NVARCHAR(3),
	"EsOD"				 				NVARCHAR(3),
	"MontoPlazo"						DECIMAL(26,8),
	"NombreCompleto"					NVARCHAR(100),
	"TasaVentaCorta"					DECIMAL(26,8),
	"PlazoVentaCorta"					DECIMAL(27,8),
	"FechaVencimientoVentaCorta"		DATE,
	"NombreEjecutivoPortafolio"			NVARCHAR(100)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros"(
"IdInterno" 						BIGINT,
"IdOrden"	 						BIGINT,
"CodigoCobroClasificacion" 			nvarchar(40),
"IdCobro" 							nvarchar(40),
"IdMonedaCobro" 					nvarchar(3),
"ValorCobroPerfil" 					decimal(26,8),
"FormaCobro" 						NVARCHAR(1),
"MontoMinimo"						DECIMAL(26,8),
"ValorCobroAplicado"				decimal(26,8), 
"MontoCobroCalculado" 				decimal(26,8), 
"MontoCobroCalculadoLocal" 			decimal(26,8), 
"MontoCobroCalculadoLiquidacion" 	decimal(26,8)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias"(
"IdInternoGarantia"		BIGINT,
"IdGarantia"			BIGINT,
"FolioGarantia"			BIGINT,
"IdMonedaInstrumento"	NVARCHAR(3),
"Instrumento"			NVARCHAR(40),
"Cantidad"				DECIMAL(26,8),
"Monto"					DECIMAL(26,8)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle"(
"IdInterno"		BIGINT,
"IdOperacion" 	BIGINT,
"Codigo" 		NVARCHAR(100),
"Descripcion" 	NVARCHAR(5000)
) ON COMMIT DELETE ROWS;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle"(
"IdInterno"				BIGINT,
"IdOperacion" 			BIGINT,
"Codigo" 				NVARCHAR(100),
"RequiereAutorizacion" 	NVARCHAR(100),
"Descripcion" 			NVARCHAR(5000)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle"(
"IdInterno"		BIGINT,
"IdOperacion" 	BIGINT,
"Codigo" 		NVARCHAR(100),
"Descripcion" 	NVARCHAR(5000)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Ordenes" (
	"IdInterno"						BIGINT,
	"IdOrden"						BIGINT,
	"FechaRecepcion"				SECONDDATE,
	"FechaIngresoRegistro"			TIMESTAMP,
	"FechaIngresoRegistroCorta"		DATE,
	"FechaSolicitudAnulacion"		SECONDDATE,
	"FechaAnulacion"				SECONDDATE,
	"FechaOperacion"				SECONDDATE,
	"FechaVigencia"					DATE,
	"FechaVencimientoInstrumento"	DATE,
	"FechaEmision"					DATE,
	"FechaLiquidacion"				DATE,
	"FechaAceptacion"				SECONDDATE,
	"FechaAutorizacion"				DATE,
	"IdTipoVigenciaOrden"			NVARCHAR(2),
	"Glosa"							NVARCHAR(520),
	"IdEstadoOrden"					NVARCHAR(3),
	"Cantidad"						DECIMAL(26,8),
	"IdTipoCustodia"				NVARCHAR(3),
	"ValorRescate"					DECIMAL(26,8),
	"IdTipoReajuste"				NVARCHAR(3),
	"PrecioOrden"					DECIMAL(26,8),
	"PrecioMercado"					DECIMAL(26,8),
	"IndicadorFacturacionInmediata"	NVARCHAR(1),
	"IdCondicionDeLiquidacion"		NVARCHAR(2),
	"IdCondicionDeLiquidacionPlazo" NVARCHAR(2),
	"IdTipoOperacion"				NVARCHAR(6),
	"IdPersonaCliente"				BIGINT,
	"IdCliente"						BIGINT,
	"IdPortafolioCliente"			BIGINT,
	"IdTipoCanalIngreso"			NVARCHAR(3),
	"IdLibroOrdenes"				BIGINT,
	"IdEmisor"						NVARCHAR(40),
	"IdEmpresa"						BIGINT,
	"IdBolsa"						NVARCHAR(3),
	"EspecificaBolsa"				NVARCHAR(1),
	"IdInstrumento"					BIGINT,
	"IdInstrumentoCustodia"			BIGINT,
	"IdTipoInstrumento"				NVARCHAR(3),
	"IdClasificacionTipoInstrumento" NVARCHAR(2),
	"IdProducto"					NVARCHAR(4),
	"IdInstrumentoSubyacente"		BIGINT,
	"Nemotecnico"					NVARCHAR(40),
	"IdGrupoCobro"					BIGINT,
	"TasaRFIF"						DECIMAL(26,8),
	"TasaVentaCorta"				DECIMAL(26,8),
	"TasaSimultanea"				DECIMAL(26,8),
	"PlazoIF"						BIGINT,
	"PlazoVentaCorta"				BIGINT,
	"PlazoSimultanea"				BIGINT,
	"EsVentaCorta"					NVARCHAR(1),
	"IdTipoPrestamo"				NVARCHAR(6),
	"IdMonedaOrden"					NVARCHAR(3),
	"IdMonedaLiquidacionOrden"		NVARCHAR(3),
	"IdMonedaLiquidacionCobros"		NVARCHAR(3),
	"TipoCambio"					DECIMAL(26,8),
	"MontoOrden"					DECIMAL(26,8),
	"MontoLiquidacion"				DECIMAL(26,8),
	"MontoLocal"					DECIMAL(26,8),
	"IdTipoPrecio"					NVARCHAR(1),
	"IdPorcentajeVisibilidad"		BIGINT,
	"IdFormaCustodia"				NVARCHAR(3),
	"OrdenVigente"					NVARCHAR(1),
	"IdEmpleadoEjecutivoPortafolio"	BIGINT, 
	"IdPersonaEjecutivoPortafolio"	BIGINT, 
	"IdEmpleadoDigitador"			BIGINT,
	"IdEstadoEmpleadoDigitador"		NVARCHAR(3),
	"IdMedioDePago"					NVARCHAR(3),
	
	"IdTipoLibroOrden"				NVARCHAR(6),
	"EsFungible"					NVARCHAR(2),
	"FechaOperacionCorta"			NVARCHAR(10),
	"IdEstadoInstrumento"			NVARCHAR(3),
	"IdInstrumentoReal"				BIGINT,
	"NombreTipoInstrumento"			NVARCHAR(100),
	"TamanoCorte"					DECIMAL(26,8),
	"IdMonedaInstrumento"			NVARCHAR(3),
	"NombreClasificacionTipoInstrumento"	NVARCHAR(100),
	"Tamano"						BIGINT,
	"IdTipoOperacionReal"			NVARCHAR(6),
	"NombreTipoOperacion"			NVARCHAR(100),
	"AccionOrden"					NVARCHAR(40),
	"IdBolsaReal"					NVARCHAR(3),
	"EsOD"							NVARCHAR(1),
	"MontoPlazo"					DECIMAL(26,8),
	"IdCondicionDeLiquidacionContadoReal"	NVARCHAR(3),
	"NombreCondicionDeLiquidacion" 			NVARCHAR(100),
	"IdCondicionDeLiquidacionPlazoReal" 	NVARCHAR(3),
	"IdTipoVigenciaReal"					NVARCHAR(2),
	"NombreTipoVigencia"					NVARCHAR(100),
	"IdTipoCustodiaReal"					NVARCHAR(3),
	"IdFormaCustodiaReal"					NVARCHAR(3),
	"IdPorcentajeVisibilidadReal"			BIGINT,
	"IdTipoPrecioReal"						NVARCHAR(1),
	"NombreTipoPrecio"						NVARCHAR(100),
	"IdTipoCanalIngresoReal"				NVARCHAR(3),
	"DiasLiquidacionContado"				BIGINT,
	"IdTipoReajusteReal"					NVARCHAR(3),
	"InstrumentoSubyacente"					NVARCHAR(40),
	"InstrumentoPermiteVentaCorta"			NVARCHAR(1),
	"InstrumentoPermiteSimultanea"			NVARCHAR(1),
	"IdPerfilCobro"						NVARCHAR(40),
	"IdEstadoCliente"					NVARCHAR(3),
	"IdEstadoPortafolio"				NVARCHAR(3),
	"NombreCompleto"					NVARCHAR(100),
	"TipoDocumentoIdentidad"			NVARCHAR(1),
	"DocumentoIdentidad"				NVARCHAR(40),
	"IdDireccionNotificacion"			NVARCHAR(255),
	"IdCorredorCorresponsal"			BIGINT,
	"IdPersonaAutorizadoDarOrdenes"		BIGINT,
	"IdTipoCanalDistribucion"			NVARCHAR(3),
	"IdTipoCanalDistribucionReal"		NVARCHAR(3),
	"TotalCobros"						DECIMAL(26,8),
	"MontoTotal"						DECIMAL(26,8),
	"RuteoGeneraMovimientoFacturacionAutomatico"	NVARCHAR(1),
	"RuteoGeneraComprobanteFacturacionAutomatico"	NVARCHAR(1),
	"PlantillaEmpleadoMaximoDescuentoComision"		DECIMAL(26,8),
	"MontoMaximoDistribucionOrden"		DECIMAL(26,8),
	"CantidadCobros"					BIGINT NOT NULL DEFAULT 0,
	"PortafolioConFlujoCaja"			NVARCHAR(1) NOT NULL DEFAULT 'S',
	
	"ValidaHorario"						NVARCHAR(1),
	"AplazaOrdenPorHorarioNegociacion"	NVARCHAR(1),
	"AplazaOrdenPorModuloCerrado"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	"CumpleHorario"						NVARCHAR(1) NOT NULL DEFAULT 'S',
	"HorarioPreMarketInicio"			TIME,
	"HorarioPreMarketFin"				TIME,
	"HorarioMarketInicio"				TIME,
	"HorarioMarketFin"					TIME,
	
	"IdEstadoModulo"					NVARCHAR(1),
	"NombreModulo"						NVARCHAR(100),
	
	--VALIDACIONES PARAMETRICAS
	"ValidaSaldoCustodia"				NVARCHAR(1),
	"ValidaSaldoCaja"					NVARCHAR(1),
	"OperaLineaSobregiro"				NVARCHAR(1),
	
	--CONTADORES
	"ADVERTENCIA"						BIGINT,
	"AUTORIZACION"						BIGINT,
	"RECHAZO"							BIGINT
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Cobros" (
	"IdInterno"					BIGINT,
	"IdOrden"					BIGINT,
	"IdCobro" 					NVARCHAR(10),
	"IdOrigenCobro" 			NVARCHAR(40),
	"ValorCobroPerfil"			DECIMAL(26,8),
	"MontoMinimo" 				DECIMAL(26,8),
	"IdMoneda" 					NVARCHAR(3),
	"FormaCobro" 				NVARCHAR(1),
	"CodigoCobroClasificacion" 	NVARCHAR(40),
	"Cantidad" 					DECIMAL(26,8),
	"MontoOrdenMonedaCobro"		DECIMAL(26,8),
	"ValorRescate"				DECIMAL(26,8),
	"DiasPlazo"					BIGINT,
	"PermiteCambiarValor"		NVARCHAR(1),
	
	"IdMonedaOrden"				NVARCHAR(3),
	"TipoCambio"				DECIMAL(26,8),
	"MontoOrdenMonedaOrden"		DECIMAL(26,8),
	"ValorCobroAplicado"		DECIMAL(26,8),
	"MontoCobroCalculado"		DECIMAL(26,8),
	"IdMonedaLiquidacionCobros"	NVARCHAR(3),
	"MontoCobroCalculadoLocal"			DECIMAL(26,8),
	"MontoCobroCalculadoLiquidacion"	DECIMAL(26,8),
	"IdGrupoCobro"						BIGINT,
	"IdCobroOrden"						BIGINT,
	"EsComprobante"						NVARCHAR(1),
	"IdPerfilCobro"					NVARCHAR(40)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Errores" LIKE "BSCL.Orden.BD.Tipos::SalidaDetalle"
ON COMMIT DELETE ROWS;

commit;


 
 

 

 

 





 






 

 
 


SET SCHEMA "BSCL"; 
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::MIG.ConsultarCobrosPortafolios_Entrada"(
	"IdInterno"				BIGINT,
	"IdPortafolio" 			BIGINT, 
	"IdTipoProducto" 		NVARCHAR(4), 
	"IdTipoOperacion" 		NVARCHAR(6), 
	"IdCanalIngreso" 		NVARCHAR(4),
	"IdBolsa" 					NVARCHAR(3),
	"IdCorredorCorresponsal" 	BIGINT
) ON COMMIT DELETE ROWS;
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase1_Entrada" (
	"IdInterno"								BIGINT,
	"IdOrden"								BIGINT,
	"Cantidad"								DECIMAL(26,8),
	"MontoNeto"								DECIMAL(26,8),
	"ValorRescate"							DECIMAL(26,8),
	"IdPortafolio"							BIGINT,
	"IdTipoLibroOrden"						NVARCHAR(40),
	"IdInstrumento"							BIGINT,
	"IdInstrumentoReal"						BIGINT,
	"IdEstadoInstrumento"					NVARCHAR(3),
	"IdTipoOperacion"						NVARCHAR(6),
	"IdTipoOperacionReal"					NVARCHAR(6),
	"IdTipoInstrumento"						NVARCHAR(3),
	"IdClasificacionTipoInstrumento"		NVARCHAR(2),
	"IdBolsa"								NVARCHAR(3),
	"IdBolsaReal"							NVARCHAR(3),
	"EspecificaBolsa"						NVARCHAR(1),
	"IdCondicionDeLiquidacionContado"		NVARCHAR(2),
	"IdCondicionDeLiquidacionContadoReal"	NVARCHAR(2),
	"IdCondicionDeLiquidacionPlazo"			NVARCHAR(2),
	"IdCondicionDeLiquidacionPlazoReal"		NVARCHAR(2),
	"InstrumentoPermiteVentaCorta"			VARCHAR(1),
	"IdTipoVigencia"						NVARCHAR(2),
	"IdTipoVigenciaReal"					NVARCHAR(2),
	"IdBoveda"								NVARCHAR(3),
	"IdBovedaReal"							NVARCHAR(3),
	"IdFormaCustodia"						NVARCHAR(3),
	"IdFormaCustodiaReal"					NVARCHAR(3), --CUS, NOM
	"IdPorcentajeVisibilidad"				BIGINT,
	"IdPorcentajeVisibilidadReal"			BIGINT,
	"TasaVentaCorta"						DECIMAL(26,8),
	"TasaSimultanea"						DECIMAL(26,8),
	"PlazoVentaCorta"						BIGINT,
	"PlazoSimultanea"						BIGINT,
	"PrecioLimite"							DECIMAL(26,8),
	"PrecioMercado"							DECIMAL(26,8),
	"IdTipoPrecio"							NVARCHAR(1),
	"IdTipoPrecioReal"						NVARCHAR(1),
	"IdTipoCanalIngreso"					NVARCHAR(3),
	"IdTipoCanalIngresoReal"				NVARCHAR(3),
	"IdTipoCanalDistribucion"				NVARCHAR(3),
	"IdTipoCanalDistribucionReal"			NVARCHAR(3),
	"IdTipoReajuste"						NVARCHAR(3),
	"IdTipoReajusteReal"					NVARCHAR(3),
	"TamanoCorte"							DECIMAL(26,8),
	"EsVentaCorta"							NVARCHAR(1),
	"EsFungible"							NVARCHAR(1),
	"AccionOrden"							NVARCHAR(10),
	"IdEmpleadoDigitador"					BIGINT,
	"IdEstadoEmpleadoDigitador"				NVARCHAR(3),
	"PlantillaEmpleadoProductoHabilitado"	NVARCHAR(1),
	"PlantillaEmpleadoPermiteRuteo"			NVARCHAR(1),
	"CantidadCobros"						BIGINT,
	"IdPortafolioReal"						BIGINT,
	"IdEstadoPortafolio" 					NVARCHAR(3),
	"IdCliente"								BIGINT,
	"IdClienteReal"							BIGINT,
	"IdEstadoCliente" 						NVARCHAR(3),
	"IdLibroOrdenes"						BIGINT,
	"DocumentoIdentidad" 					NVARCHAR(40),
	"IdPerfilCobros"						NVARCHAR(40),
	"IdEmpleadoEjecutivoPortafolio"			BIGINT,
	"CumpleHorario"							NVARCHAR(1),
	"IdEstadoModulo"						NVARCHAR(1),
	"NombreModulo"							NVARCHAR(100),
	"AplazaOrdenPorHorarioNegociacion"		NVARCHAR(1),
	"AplazaOrdenPorModuloCerrado"			NVARCHAR(1),
	"VentaCortaPlazoMinimo"					BIGINT,
	"VentaCortaPlazoMaximo"					BIGINT,
										
	"CO_ContratoSimultanea" 	NVARCHAR(40),
	"CO_ContratoCustodia" 		NVARCHAR(40),
	"CO_PortafolioBloqueado" 	NVARCHAR(40),
	"CO_ContratoFuturos" 		NVARCHAR(40),
	"CO_ContratoVentaCorta" 	NVARCHAR(40),
	"CO_ContratoMercantil" 		NVARCHAR(40),
	"CO_CompraNominalRV" 		NVARCHAR(40),
	"CO_CompraNominalRFIF" 		NVARCHAR(40),
	"CO_VentaNominalRV" 		NVARCHAR(40),
	"CO_VentaNominalRFIF" 		NVARCHAR(40),
	"CO_ConfirmacionOrdenVerbalEscrita" NVARCHAR(40),
	"CO_ContratoCondicionesGenerales" NVARCHAR(40),
	
	"CO_ClienteBloqueado" 				NVARCHAR(40),
	"CO_PersonaBloqueada" 				NVARCHAR(40),
	"CO_EjecutivoSinProductoPlantilla" 	NVARCHAR(40),
	"CO_EjecutivoSinRuteoPlantilla" 	NVARCHAR(40),
	"CO_EjecutivoPortafolioBloqueado" 	NVARCHAR(40),
	"CO_OrdenRequiereAutorizacion" 		NVARCHAR(40),
	"CO_FotocopiaCarnet" 				NVARCHAR(40),
	"CO_AntecedentesLegales" 			NVARCHAR(40),
	"CO_DiferenciaPrecioOrden"			NVARCHAR(40),
	
	
	"ContratoSimultanea" 		NVARCHAR(40),
	"ContratoFuturos" 			NVARCHAR(40),
	"ContratoVentaCorta" 		NVARCHAR(40),
	"ContratoCustodia" 			NVARCHAR(40),
	"ContratoMercantil" 		NVARCHAR(40),
	"ClaseOrden"		 		NVARCHAR(40),
	"ContratoCondicionesGenerales" NVARCHAR(40),
	
	"OperaCuentaInversion" 		NVARCHAR(40),
    "OperaLineaSobregiro" 		NVARCHAR(40),
    "OperaCustodiaRV" 			NVARCHAR(40),
    "OperaCustodiaRFIF" 		NVARCHAR(40),
    "PermiteVenderNominalRV" 	NVARCHAR(40),
    "PermiteVenderNominalRFIF"	NVARCHAR(40),
	
	"IdEstadoPersona"			NVARCHAR(3),
	"IdEstadoEmpleadoEjecutivoPortafolio"	NVARCHAR(3),
	"FotocopiaCarnet"			NVARCHAR(40),
	"AntecedentesLegales"		NVARCHAR(40),
	"PorcentajeDiferenciaPrecio"	DECIMAL(26,8),
	"IdEstadoLibroOrdenes"		NVARCHAR(3)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Ordenes"(
	"IdInterno"									BIGINT,
	"IdOrden"									BIGINT,
	"IdTipoCanalIngreso"						NVARCHAR(3),
	"MontoTotalOrden"							DECIMAL(26,8),
	"MontoOperacionMonedaLocal"					DECIMAL(26,8),
	"MontoMaximoDistribucionOrden"				DECIMAL(26,8),
	"ValidaSaldoCaja"							NVARCHAR(1),
	"PortafolioConFlujoCaja"					NVARCHAR(1),
	"PlantillaEmpleadoMaximoDescuentoComision"	DECIMAL(26,8),
	"ValidaSaldoCustodia"						NVARCHAR(1),
	"PortafolioConSaldoCustodia"				NVARCHAR(1),
	"PorcentajeMaximoComision"					DECIMAL(26,8),
	"PorcentajeMinimoComision"					DECIMAL(26,8),
	"PlantillaEmpleadoMontoMaximoOperacion"		DECIMAL(26,8),
	
--REQUISITOS OPERATIVOS
	"CO_OperacionSinFlujoCustodia"				NVARCHAR(40),
	"CO_DescuentoComisionSuperaPermitidoPlantilla"	NVARCHAR(40),
	"CO_ComisionSuperaMaximoPermitido"				NVARCHAR(40),
	"CO_ComisionBajoMinimoPermitido"				NVARCHAR(40),
	"CO_MontoSuperaMaximoPermitidoCanalDistribucion" NVARCHAR(40),
	"CO_MontoOperacionSuperaPermitidoPlantilla"		NVARCHAR(40),
	
--INFORMATIVOS
	"FlujoCajaPortafolio"						DECIMAL(26,8),
	"IdMonedaLiquidacionOrden"					NVARCHAR(3),
	"FlujoCustodiaPortafolio"					DECIMAL(26,8)
									
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Cobros"(
	"IdInterno"					BIGINT,
	"IdOrden"					BIGINT,
	"IdCobro" 					NVARCHAR(10),
	"FormaCobro"				NVARCHAR(1),
	"CodigoCobroClasificacion" 	NVARCHAR(40),
	"ValorCobroPerfil"			DECIMAL(26,8),
	"ValorCobroAplicado" 		DECIMAL(26,8)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::MIG.RutearOrdenes_Entrada" (
	"IdOrden"					BIGINT,
	"IdBolsaDistribucion" 		NVARCHAR(3) --OPCIONAL
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::MIG.NotificarEstadoOrden_Entrada" (
	"IdOrden"					BIGINT
) ON COMMIT DELETE ROWS;



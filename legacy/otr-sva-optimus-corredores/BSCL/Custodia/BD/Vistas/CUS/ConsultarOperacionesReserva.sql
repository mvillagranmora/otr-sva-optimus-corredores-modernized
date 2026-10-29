
--* ------------------------------------------------------------------------
--* Nombre .....: ConsultarOperacionesMesaRF
--* Modulo .....: Custodia
--* F. Creación : Agosto 2016
--* Proposito ..: Consultar las operaciones de la mesa RF
--* ------------------------------------------------------------------------

SET SCHEMA "BSCL"; 
--* ------------------------------------------------------------------------
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva";
--* ------------------------------------------------------------------------
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" 
AS 
		SELECT  
			"IdReserva"
			,"IdOperacion"
			,"FolioCarteraOrigen"
			,"FolioCarteraCustodia"
			,"FolioOperacion"
			,"IdEstado"
			,"IdUsuarioCreacion"
			,"IdTipoProducto"
			,"IdConcepto"
			,"Tasa"
			,"TasaMercado"
			,"Precio"
			,"PrecioMercado"
			,"MontoValorizado"
			,"MontoLiquidacion"
			,"MontoLiquidacionOperacion"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"IdTipoReajuste"
			,"IdMoneda"
			,"IdMonedaEmisionInstrumento"
			,"IdMonedaLiquidacionInstrumento"
			,"IdMonedaLiquidacionOperacion"
			,"IdEmisor"
			,"CorteMinimo"
			,"FechaVencimiento"
			,"EsFungible"
			,"CantidadInicial"
			,"ValorRescateInicial"
			,"MontoValorizadoReserva"
			,"FechaVencimientoReserva"
			,"FechaVencimientoProducto"
			,"FechaCreacion"
			,"IngIdUsuario"
			,"IngCantidad"
			,"IngValorRescate"
			,"IngMontoValorizado"
			,"IngFecha"
			,"EgrIdUsuario"
			,"EgrCantidad"
			,"EgrValorRescate"
			,"EgrMontoValorizado"
			,"EgrFecha"
			,"IdBolsa"
			,"IdBoveda"
			,"CodigoIFRS"
			,"IdCarteraOrigen"
			,"INGMontoAPlazo"
			,"INGMontoAPlazoMonedaInstrumento"
		FROM	"BSCL.Custodia.BD::CUS.Reserva" OP
			
	WITH 	READ ONLY;	
		
--* ------------------------------------------------------------------------
GO
GRANT 	SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO

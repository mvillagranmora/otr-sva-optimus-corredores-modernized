SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarOperacionesReserva";

CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarOperacionesReserva" 
AS 
		SELECT  
			 "IdReserva"
			,"FechaRespaldo"
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
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"IdTipoReajuste"
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
		FROM	"BSCL.Custodia.BD::HIS.Reserva" OP			
	WITH 	READ ONLY;	


GRANT 	SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarOperacionesReserva"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
	

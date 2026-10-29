SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEventoLiquidarUI";

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEventoLiquidarUI"
AS 
	SELECT 
	 evt."IdInstruccion",
	 evt."DocumentoIdentidad",
	 evt."IdTipoInstrumentoInstruccion",
	 evt."IdEmisor",
	 evt."IdEstadoInstruccion"
	
	FROM "BSCL.Orden.BD::ORD.Instruccion" AS evt;


GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEventoLiquidarUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

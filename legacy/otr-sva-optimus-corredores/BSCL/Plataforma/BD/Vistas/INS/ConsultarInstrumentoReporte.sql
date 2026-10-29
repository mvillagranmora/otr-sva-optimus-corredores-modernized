SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoReporte"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoReporte"  
AS
SELECT * FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 
WHERE "Accion" = 'COMPRA' AND "IdClasificacionTipoInstrumento"='RE';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoReporte" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

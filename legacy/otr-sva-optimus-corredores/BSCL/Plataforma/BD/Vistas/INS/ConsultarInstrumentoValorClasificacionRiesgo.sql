SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoValorClasificacionRiesgo"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoValorClasificacionRiesgo"
AS

SELECT  "IdInstrumento","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo"
FROM   "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoValorClasificacionRiesgo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

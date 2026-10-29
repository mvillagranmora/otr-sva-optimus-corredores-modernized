SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarLogNor";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarLogNor"
AS

SELECT 
"IdLogNormativa", 
"IdTipoLogNormativa",
"IdUsuario",
"FechaLog",
"IdProceso",
"IdSolicitudCircular",
"IdOperacion",
"Registro",
"Observacion"
--,"ValLiq",
--"ValMont" 
FROM "BSCL.Normativa.BD::NOR.LogNormativa" AS NOR 
ORDER BY "IdLogNormativa" DESC
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarLogNor"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;


--select * from "BSCL.Normativa.BD.Vistas::NOR.ConsultarLogNor"
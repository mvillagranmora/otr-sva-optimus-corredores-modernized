SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarMercadoFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarMercadoFondoMutuo"
AS
SELECT 
"IdMercadoFondoMutuo"
,"Nombre"
,"IdEstado"
FROM "BSCL.FondoMutuo.BD::FMM.MercadoFondoMutuo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarMercadoFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

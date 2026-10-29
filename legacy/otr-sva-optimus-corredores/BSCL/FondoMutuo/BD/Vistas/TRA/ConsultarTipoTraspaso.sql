SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTipoTraspaso";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTipoTraspaso"  
AS
SELECT
 "IdTipoTraspaso",
 "Descripcion",
 "IdEstado"
FROM "BSCL.FondoMutuo.BD::TRA.TipoTraspaso";
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTipoTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

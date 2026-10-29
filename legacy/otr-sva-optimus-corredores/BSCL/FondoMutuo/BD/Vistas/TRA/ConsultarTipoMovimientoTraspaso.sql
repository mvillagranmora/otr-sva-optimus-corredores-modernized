SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTipoMovimientoTraspaso";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTipoMovimientoTraspaso"  
AS
SELECT
 "IdTipoMovimientoTraspaso",
 "Descripcion",
 "IdEstado"
FROM "BSCL.FondoMutuo.BD::TRA.TipoMovimientoTraspaso";
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTipoMovimientoTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

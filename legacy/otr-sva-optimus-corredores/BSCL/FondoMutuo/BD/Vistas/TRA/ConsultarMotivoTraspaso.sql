SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarMotivoTraspaso";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarMotivoTraspaso"  
AS
SELECT
 "IdMotivoTraspaso",
 "Descripcion",
 "IdEstado"
FROM "BSCL.FondoMutuo.BD::TRA.MotivoTraspaso";
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarMotivoTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarMotivoAnulacionTraspaso";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarMotivoAnulacionTraspaso"  
AS
SELECT
 "IdMotivoAnulacion",
 "Descripcion",
 "IdEstado"
FROM "BSCL.FondoMutuo.BD::TRA.MotivoAnulacionTraspaso";
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarMotivoAnulacionTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones"; 
 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones"  
AS
SELECT "IdPeriodo" , "Descripcion" , "FechaDesde" , "FechaHasta"  FROM "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones";
     

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobro"; 
 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobro"  
AS
SELECT "OpcionCobro" , "IdEstado" 
FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro"
GROUP BY  "OpcionCobro" , "IdEstado";
     

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMonedaTasa"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMonedaTasa"  
AS
SELECT 
  MON."IdMoneda"  
, MON."Orden" 
, MON."IdEstado"
,  (  ( MON."IdMoneda" || ' - ' )  || MON."Nombre" )  AS "NombreLargo" 
FROM  "BSCL.Plataforma.BD::PLA.Moneda" AS MON
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TMO ON (TMO."IdTipoMoneda" = MON."IdTipoMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = MON."IdEstado")
INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS MONF ON (MON."IdMoneda" = MONF."IdMoneda" 
																AND MONF."IdEstado" = 'ACT' 
																AND "IdUsoMonedaFormato" = 'TAS');
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMonedaTasa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
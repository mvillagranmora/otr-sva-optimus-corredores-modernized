SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"  
AS
SELECT 
  MON."IdMoneda" 
, MON."Codigo" 
, MON."Nombre" 
, MON."Simbolo" 
--, MON."Decimales"  -- no se debe usar, usar formato Moneda 
, MON."IdTipoMoneda"
, TMO."Nombre" AS "NombreTipoMoneda"
, TMO."MultiplicaODivide"
, MON."RangoSuperiorParidad" 
, MON."RangoInferiorParidad" 
, MON."PorcentajeSuperiorParidad" 
, MON."PorcentajeInferiorParidad" 
, MON."RangoSuperiorTipoCambio" 
, MON."RangoInferiorTipoCambio" 
, MON."PorcentajeSuperiorTipoCambio" 
, MON."PorcentajeInferiorTipoCambio" 
, MON."Orden" 
, MON."IdPais" 
, MON."IdEstado" 
, EST."Nombre" as "NombreEstado"
,  (  ( MON."IdMoneda" || ' - ' )  || MON."Nombre" )  AS "NombreLargo" 
, MON."MonedaSMV"
FROM  "BSCL.Plataforma.BD::PLA.Moneda" AS MON
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TMO ON (TMO."IdTipoMoneda" = MON."IdTipoMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = MON."IdEstado");
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
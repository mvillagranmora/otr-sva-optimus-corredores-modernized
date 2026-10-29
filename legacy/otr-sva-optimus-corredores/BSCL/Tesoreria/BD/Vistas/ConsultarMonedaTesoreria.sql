SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" AS

SELECT 
	  MON."IdMoneda"
	 ,MON."Codigo"
	 ,MON."CodigoNumerico"
	 ,MON."Nombre"
	 ,((MON."IdMoneda" || ' - ' ) || MON."Nombre" )  AS "NombreLargo" 
	 ,MON."Simbolo"
	 ,MON."Decimales"
	 ,MON."IdTipoMoneda"
	 ,TMO."Nombre" AS "NombreTipoMoneda"
	 ,TMO."MultiplicaODivide"
	 ,MON."RangoSuperiorParidad"
	 ,MON."RangoInferiorParidad"
	 ,MON."PorcentajeSuperiorParidad"
	 ,MON."PorcentajeInferiorParidad"
	 ,MON."RangoSuperiorTipoCambio"
	 ,MON."RangoInferiorTipoCambio"
	 ,MON."PorcentajeSuperiorTipoCambio"
	 ,MON."PorcentajeInferiorTipoCambio"
	 ,MON."Orden"
	 ,MON."IdPais"
	 ,MON."IdEstado"	 
	 ,MT."IdEstado" AS "IdEstadoMonedaTesoreria"
	 ,E."Nombre" AS "NombreEstadoMonedaTesoreria"
FROM "BSCL.Plataforma.BD::PLA.Moneda" AS MON
INNER JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" AS MT ON (MT."IdMoneda" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TMO ON (TMO."IdTipoMoneda" = MON."IdTipoMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON E."IdEstado" = MT."IdEstado"
WHERE MT."IdEstado" = 'ACT'

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
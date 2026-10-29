SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.Datos" AS
SELECT
     a."ID"
	,a."IdDocumento"
	,a."VboUsuario"
	,a."Observaciones"
	,a."FuenteDatos"
	,a."IdEmpresa"
	,a."IdOperacionSpot"
	,a."EventoReporte"
	,a."CodigoInstitucion"
	,a."NumeroOperacion"
	,a."FechaOperacion"
	,a."TipoDocumento"
	,a."CodigoComercio"
	,COALESCE(g."Descripcion",'') AS "CodigoComercioDescripcion"
	,a."IdPais"
	,COALESCE(f."Descripcion",'') AS "IdPaisDescripcion"
	,a."MonedaOperacion"
	,COALESCE(e."Descripcion",'') AS "MonedaOperacionDescripcion"
	,a."MontoOperacion"
	,a."MontoDolar"
	,a."TipoCambio"
	,a."MontoEquiClp"
	,a."AfectoADerivado"
	,a."DocumentoIdentidad"
	,a."NombreCompleto"
	,a."TipoAutorizacion"
	,COALESCE(b."Descripcion",'') AS "TipoAutorizacionDescripcion"
	,a."NumeroAutorizacion"
	,a."FechaAutorizacion"
	,a."SectorEconomicoBeneficiario"
	,COALESCE(c."Descripcion",'') AS "SectorEconomicoBeneficiarioDescripcion"
	,a."NumeroInscripcion"
	,a."FechaDesembolso"
	,a."SectorEconomicoInversionista"
	,COALESCE(d."Descripcion",'') AS "SectorEconomicoInversionistaDescripcion"
	,a."NombreFinancistaExtranjero"
	,a."FechaVencimientoCapital"
	,a."NroContDerivado" 
	,a."FechaSuscDerivado"
	,a."FechaVencDerivado"
	,CASE WHEN a."Observacion" = '' THEN 'Sin observaciones' ELSE a."Observacion" END AS "Observacion"
	,a."EstadoGeneracion"
	,a."FecUltMod"
	,a."UsrUltMod"
	,a."EsArbitraje"
FROM "BSCL.Normativa.BD::TRI.DatosDJ999" a
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" b
    ON b."Tipo" = 'DJ999_TipoAutorizacion' AND a."TipoAutorizacion" = b."Codigo"
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" c
    ON c."Tipo" = 'DJ999_SectorEconomico' AND CAST(a."SectorEconomicoBeneficiario" as NVARCHAR) = c."Codigo"
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" d
    ON d."Tipo" = 'DJ999_SectorEconomico' AND CAST(a."SectorEconomicoInversionista" as NVARCHAR) = d."Codigo"
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" e
    ON e."Tipo" = 'DJ999_CodigoMoneda' AND CAST(a."MonedaOperacion" as NVARCHAR) = e."Codigo"
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" f
    ON f."Tipo" = 'DJ999_CodigoAduana' AND CAST(a."IdPais" as NVARCHAR) = f."Codigo"
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" g
    ON g."Tipo" = 'DJ999_CodigoComercio' AND CAST(a."CodigoComercio" as NVARCHAR) = g."Codigo"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ999.Datos" TO BCS_DESARROLLADOR WITH GRANT OPTION;

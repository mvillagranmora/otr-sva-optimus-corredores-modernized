SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Datos" (
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    "IdOperacion",
    "Numero",
    "RutEmisor",
    "TaxID",
    "RutCliente",
    "CodigoPaisConstitucion",
    "CodigoPaisConstitucionDescripcion",
    "CodigoPaisResidencia",
    "CodigoPaisResidenciaDescripcion",
    "PorcentajePropiedadDirectaIndirecta",
    "ParticipacionDirectaIndirectaControl",
    "NemotecnicoSVS",
    "FechaOperacion",
    "NumeroCuentaDCV",
    "Moneda",
    "MonedaDescripcion",
    "ValorEmision",
    "ValorColocacion",
    "ValorAdquisicion",
    "ValorAdquisicionEnPesos",
    "ValorEnajenacion",
    "ValorEnajenacionEnPesos",
    "MayorValorEnajenacion",
    "MayorValorEnajenacionEnPesos",
	"IdCliente",
	"IdPortafolio"
) as
select
    a."ID",
	a."IdDocumento",
	a."VboUsuario",
	COALESCE(VAL."CodigoValidacion", a."Observaciones") AS "Observaciones",
	a."FuenteDatos",
	COALESCE(a."IdOperacion", 0) AS "IdOperacion",
	a."Numero",
	a."RutEmisor",
	a."TaxID",
	a."RutCliente",
	a."CodigoPaisConstitucion",
	COALESCE(c."Descripcion",'') AS "CodigoPaisConstitucionDescripcion",
	a."CodigoPaisResidencia",
	COALESCE(b."Descripcion",'') AS "CodigoPaisResidenciaDescripcion",
	a."PorcentajePropiedadDirectaIndirecta",
	a."ParticipacionDirectaIndirectaControl",
	a."NemotecnicoSVS",
	a."FechaOperacion",
	a."NumeroCuentaDCV",
	a."Moneda",
	COALESCE(b."Descripcion",'') AS "MonedaDescripcion",
	a."ValorEmision",
	a."ValorColocacion",
	a."ValorAdquisicion",
	a."ValorAdquisicionEnPesos",
	a."ValorEnajenacion",
	a."ValorEnajenacionEnPesos",
	a."MayorValorEnajenacion",
	a."MayorValorEnajenacionEnPesos",
	a."IdCliente",
	a."IdPortafolio"

from "BSCL.Normativa.BD::TRI.DatosDJ1919" a
LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Diccionario" b ON ( b."Tipo" = 'CodigoMonedaDJ1919' AND a."Moneda" = b."Codigo")
LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Diccionario" c ON ( c."Tipo" = 'CodigoPais' AND a."CodigoPaisConstitucion" = c."Codigo")
LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Diccionario" d ON ( d."Tipo" = 'CodigoPais' AND a."CodigoPaisResidencia" = d."Codigo")
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1919" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Datos" TO bcs_desarrollador WITH GRANT OPTION;
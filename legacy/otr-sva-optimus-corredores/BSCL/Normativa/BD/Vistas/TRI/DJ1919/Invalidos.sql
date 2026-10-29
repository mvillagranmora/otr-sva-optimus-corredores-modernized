set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Invalidos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Invalidos" (
    "CodigoValidacion",
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    "Numero",
    "RutEmisor",
    "TaxID",
    "RutCliente",
    "CodigoPaisConstitucion",
    "CodigoPaisResidencia",
    "PorcentajePropiedadDirectaIndirecta",
    "ParticipacionDirectaIndirectaControl",
    "NemotecnicoSVS",
    "FechaOperacion",
    "NumeroCuentaDCV",
    "Moneda",
    "ValorEmision",
    "ValorColocacion",
    "ValorAdquisicion",
    "ValorAdquisicionEnPesos",
    "ValorEnajenacion",
    "ValorEnajenacionEnPesos",
    "MayorValorEnajenacion",
    "MayorValorEnajenacionEnPesos",
	"IdPortafolio"
) as
SELECT
   	b."CodigoValidacion",
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    a."Observaciones",
    a."FuenteDatos",
    a."Numero",
    a."RutEmisor",
    a."TaxID",
    a."RutCliente",
    a."CodigoPaisConstitucion",
    a."CodigoPaisResidencia",
    a."PorcentajePropiedadDirectaIndirecta",
    a."ParticipacionDirectaIndirectaControl",
    a."NemotecnicoSVS",
    a."FechaOperacion",
    a."NumeroCuentaDCV",
    a."Moneda",
    a."ValorEmision",
    a."ValorColocacion",
    a."ValorAdquisicion",
    a."ValorAdquisicionEnPesos",
    a."ValorEnajenacion",
    a."ValorEnajenacionEnPesos",
    a."MayorValorEnajenacion",
    a."MayorValorEnajenacionEnPesos",
	a."IdPortafolio"     
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Datos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1919.Invalidos" to bcs_desarrollador with grant option;

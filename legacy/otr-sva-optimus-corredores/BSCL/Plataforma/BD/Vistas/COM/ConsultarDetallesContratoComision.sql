SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallesContratoComision"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallesContratoComision"  
AS
SELECT  DCC."IdDetalleContratoComision",
DCC."IdContratoComision",
DCC."IdCobroClasificacion",
CLASIFICACION."CodigoCobroClasificacion",
DCC."IdCobro",
COBRO."NombreCobro",
DCC."EsPorRangos",
DCC."IdConcepto",
CONCEPTO."Nombre" AS "NombreConcepto",
DCC."MontoMinimo",
DCC."MontoMaximo"
FROM "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS DCC
LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CLASIFICACION ON CLASIFICACION."IdCobroClasificacion" = DCC."IdCobroClasificacion"
LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBRO ON COBRO."IdCobro" = DCC."IdCobro"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CONCEPTOFACTURACION ON CONCEPTOFACTURACION."IdConcepto" = DCC."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = CONCEPTOFACTURACION."IdConcepto";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallesContratoComision" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
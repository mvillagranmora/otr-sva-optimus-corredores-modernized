SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoUI";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoUI"
AS
SELECT
"Fondo"."IdFondo"
, "Fondo"."IdSerieFondo"
, "Fondo"."NombreFondo"
, "Fondo"."NombreSerieFondo"
, "Fondo"."NombreFamilia"
, "Fondo"."ValorCuota"
, "Fondo"."FechaValorCuota"
, "Fondo"."IdMoneda"
, "Fondo"."RazonSocialEmisor"
, "Fondo"."IdEmisor"
, "Fondo"."NombreTipoInstrumento"
, "Fondo"."IdEmpresa"
, "Fondo"."NombreEstado"
, "Fondo"."DecimalesValorCuota"
, "Fondo"."ValorDolarAporte"
, "Fondo"."ValorDolarRescate"
, "Fondo"."NombreCorto"
, "Fondo"."EsDiferidoUI"
, "Fondo"."MontoMinimo"
, "Fondo"."MontoMaximo"
, "Fondo"."EsMoneyMarketUI"
, "Fondo"."IdTipoInstrumento"
, "Fondo"."IdEstado"
, "Fondo"."IdFamiliaFondo"
, "Fondo"."FondoExtranjero"
, "Fondo"."AportesAltos"
, "Fondo"."InstrumentoOWP"
, "Fondo"."EsAPV"
, "DocumentoIdentidad"."DocumentoIdentidad" AS "DocumentoIdentidadEmisor"
,"Fondo"."MontoPatrimonioEmisor"
,"Fondo"."RestringeAporte"
,"Fondo"."RestringeRescate"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS "Fondo"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS "Emisor" ON "Fondo"."IdEmisor" = "Emisor"."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona" ON "Persona"."IdPersona" = "Emisor"."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad" ON "DocumentoIdentidad"."IdDocumentoIdentidad" = "Persona"."IdDocumentoIdentidad"  and "DocumentoIdentidad"."Predeterminado" = 'S';


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;   

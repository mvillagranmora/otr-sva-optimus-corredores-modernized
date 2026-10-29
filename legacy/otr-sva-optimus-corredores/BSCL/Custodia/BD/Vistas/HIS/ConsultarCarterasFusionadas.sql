
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadas";

CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadas" AS
SELECT Car."IdCliente" AS "IdCliente",
Fus."IdFusion" AS "IdFusion",
Car."INGFechaValorizacionCartera" AS "INGFechaValorizacionCartera",
Car."FechaLiquidacion" AS "FechaLiquidacion",
Usuario."CodigoUsuario" AS "CodigoUsuario",
Ident."DocumentoIdentidad" AS "DocumentoIdentidad",
Car."IdPortafolio" AS "IdPortafolio",
Port."CodigoPortafolio" AS "CodigoPortafolio",
Car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
Car."IdCartera" AS "IdCartera",
Car."FolioCartera" AS "FolioCartera",
Car."Nemotecnico" AS "Nemotecnico",
Car."NemoSVS" AS "NemoSVS",
Car."IdEmisor" AS "IdEmisor",
Car."IdTipoReajuste" AS "IdTipoReajuste",
Car."IdBoveda" AS "IdBoveda",
Fus."FechaFusion" AS "FechaComprobante",
Car."INGValorRescate"  AS "INGValorRescate",
Car."FechaVencimientoInstrumento" AS "FechaVencimientoInstrumento",
COUNT( Hist."IdCartera" ) AS "CarterasFusionadas",
Car."INGTasaCompra" AS "INGTasaCompra",
CASE WHEN Car."IdClasificacionTipoInstrumento" = 'RF' THEN  Car."Cantidad" ELSE Car."INGCantidad" END  AS "INGCantidad",
Fus."MontoFusion" AS "MontoCompraMonedaInstrumento",
Fus."MontoValorizado" AS "ValorCalculadoFus",
(Fus."MontoValorizado" - Fus."MontoFusion") AS "DiferenciaInversion",
"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" (Car."IdEmisor",
Car."IdClasificacionTipoInstrumento",
Car."IdTipoInstrumento",
Car."Nemotecnico",
Car."IdTipoReajuste") AS "ClasificacionContable",
Car."IdEmpresa" AS "IdEmpresa" ,
Car."IdTipoInstrumento" AS "IdTipoInstrumento",
Car."IdEstado" AS "IdEstado",
case when ifnull(Car."IdTipoReajuste", '') <> '' then Car."IdTipoReajuste"
when ifnull(Car."IdMonedaInstrumento",'') <> '' then Car."IdMonedaInstrumento"
when ifnull(Car."IdMonedaPrincipal",'') <> '' then Car."IdMonedaPrincipal" 
else '' end AS "IdMonedaInstReajuste" 
FROM "BSCL.Custodia.BD::HIS.FusionCartera" AS Fus 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS Car ON Fus."IdCarteraResultante" = Car."IdCartera" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli ON Cli."IdCliente" = Car."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per ON Per."IdPersona" = Cli."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Ident ON Per."IdDocumentoIdentidad" = Ident."IdDocumentoIdentidad" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Port ON Port."IdPortafolio" = Car."IdPortafolio" AND Port."IdCliente" = Car."IdCliente" 
LEFT JOIN "BSCL.Custodia.BD::HIS.FusionCarteraDetalle" AS Hist ON Hist."IdFusion" = Fus."IdFusion" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario ON Usuario."IdUsuario" = Car."IdUsuario"
WHERE Car."IdEstado" = 'VIG'

GROUP BY Car."IdCliente",
 Fus."IdFusion",
 Car."INGFechaValorizacionCartera",
 Car."FechaLiquidacion",
 Usuario."CodigoUsuario",
 Ident."DocumentoIdentidad",
 Car."IdPortafolio",
 Port."CodigoPortafolio",
 Car."IdClasificacionTipoInstrumento",
 Car."IdCartera",
 Car."FolioCartera",
 Car."Nemotecnico",
 Car."NemoSVS",
 Car."IdEmisor",
 Car."IdTipoReajuste",
 Car."IdBoveda",
 Fus."FechaFusion",
 Car."INGValorRescate",
 Car."FechaVencimientoInstrumento",
 Car."INGTasaCompra",
 Car."Cantidad",
 Car."INGCantidad",
 Fus."MontoValorizado",
 Car."MontoCompraMonedaInstrumento",
 Fus."MontoFusion",
 Car."IdEmpresa",
 Car."IdTipoInstrumento",
 Car."IdEstado",
 Car."IdTipoReajuste",
 Car."IdMonedaInstrumento",
 Car."IdMonedaPrincipal";


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadas"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

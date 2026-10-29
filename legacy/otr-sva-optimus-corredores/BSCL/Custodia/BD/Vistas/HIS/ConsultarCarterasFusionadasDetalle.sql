SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadasDetalle"; 


CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadasDetalle" AS
SELECT  FC."FolioCartera" AS "FolioCartera",
FC."INGCantidad" AS "Cantidad",
FC."INGValorRescate" AS "ValorRescate",
coalesce(COMP."Nombre", OP."Nombre") AS "TipoComprobante",
FC."FechaComprobante" AS "FechaCompra",
FC."FechaLiquidacion" AS "FechaLiquidacion",
FC."CodigoIFRS" AS "CategoriaIFRS",
HIS."IdFusion" AS "SecuenciaFusion",
FC."MontoCompra" AS "MontoCompra",
FC."TasaCompra" AS "TasaCompra",
FC."MontoCompraMonedaInstrumento" AS "ValorPresente" 
FROM "BSCL.Custodia.BD::HIS.FusionCarteraDetalle" HIS
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" FC ON HIS."IdCartera" = FC."IdCartera"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoIFRS" IFRS ON FC."CodigoIFRS" = IFRS."Codigo" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OP ON FC."IdTipoOperacion" = OP."IdTipoOperacion" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" COMP ON FC."IdTipoComprobante" = COMP."IdTipoComprobante";


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadasDetalle"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasoCuentaInversion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasoCuentaInversion"  AS
SELECT
    t."IdEmpresa",t."IdTraspaso",t."TipoMovimiento",t."IdContratoSAAPV",t."IdCliente",t."IdPortafolio",t."IdComprobante",
    t."IdRecaudacion",t."IdCargoAbonoMovimiento",t."IdTipoMovimiento",t."IdConcepto",t."FechaCertificacion",
    t."Monto",t."IdEstado",c."IdPersona",p."IdMatrizAgrupadorAPV",m."IdPlanTipoInversion",m."NombrePlan",
    m."Codigo",c."TipoDocumentoIdentidad",c."IdDocumentoIdentidad",c."DocumentoIdentidad",c."NombreCompleto",
    c."IdTipoPersona",c."Sexo",c."FechaNacimiento"
FROM (
    SELECT
        t."IdEmpresa",t."IdTraspaso",t."TipoMovimiento",t."IdContratoSAAPV",t."IdCliente",ci."IdPortafolio",c."IdComprobante",
        0 AS "IdRecaudacion",ca."IdCargoAbonoMovimiento",ca."IdTipoMovimiento",ca."IdConcepto",
        COALESCE(ca."FechaCertificacion",'0001-01-01') AS "FechaCertificacion",ca."Monto",t."IdEstado"
    FROM "BSCL.APV.BD::TRA.Traspaso" AS t
    INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoOrigen" AS c ON (c."IdOperacion" = t."IdTraspaso"
                                                                                      AND c."IdTipoOperacion" = 'EGRTES'
                                                                                      AND c."IdModulo" = 'APV')
    INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ca ON (ca."IdComprobante" = c."IdComprobante"
                                                                      AND ca."IdTipoComprobante" = 'EGR')
    INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ci ON (ci."IdCuentaInversion" = ca."IdCuentaInversion")                                                                       
    WHERE t."TipoMovimiento" = 'EGRESO'
    UNION ALL
    SELECT
         t."IdEmpresa",t."IdTraspaso",t."TipoMovimiento",t."IdContratoSAAPV",t."IdCliente",ci."IdPortafolio",0 AS "IdComprobante",
         r."IdRecaudacion",ca."IdCargoAbonoMovimiento",ca."IdTipoMovimiento",ca."IdConcepto",
         COALESCE(ca."FechaCertificacion",'0001-01-01') AS "FechaCertificacion",ca."Monto",t."IdEstado"
    FROM "BSCL.APV.BD::TRA.Traspaso" AS t
    INNER JOIN "BSCL.APV.BD::REC.Cliente" r ON (r."IdContratoDistribucion" = t."IdContratoSAAPV")
    INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ca ON (ca."IdOperacion" = r."IdRecaudacionCliente"
                                                                      AND ca."IdComprobante" = 0
                                                                      AND ca."IdConcepto" = 'ABOAPV')
    INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ci ON (ci."IdCuentaInversion" = ca."IdCuentaInversion")
    WHERE t."TipoMovimiento" = 'INGRESO'
) AS t
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS c ON (c."IdCliente" = t."IdCliente")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS p ON (p."IdPortafolio" = t."IdPortafolio")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS m ON (m."IdMatrizAgrupadorAPV" = p."IdMatrizAgrupadorAPV")
WITH READ ONLY;

GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasoCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
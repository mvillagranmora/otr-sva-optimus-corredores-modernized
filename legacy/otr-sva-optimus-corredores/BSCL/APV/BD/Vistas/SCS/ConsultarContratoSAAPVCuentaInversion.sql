SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarContratoSAAPVCuentaInversion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarContratoSAAPVCuentaInversion"  AS 

SELECT
    cs."IdEmpresa",cs."IdContratoSAAPV",pcs."IdPortafolioContratoSAAPV",cs."IdEstado",p."IdMatrizAgrupadorAPV",
    m."IdPlanTipoInversion",m."NombrePlan",m."Codigo",cs."IdCliente",c."IdPersona",pcs."IdPortafolio",
    c."TipoDocumentoIdentidad",c."IdDocumentoIdentidad",c."DocumentoIdentidad",c."NombreCompleto",
    c."IdTipoPersona",c."Sexo",c."FechaNacimiento",
    COALESCE(ca."IdCargoAbonoMovimiento",0) AS "IdCargoAbonoMovimiento",ca."IdTipoMovimiento",ca."IdConcepto",
    ca."FechaCertificacion",ca."Monto"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS cs
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS pcs ON (pcs."IdContratoSAAPV" = cs."IdContratoSAAPV")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS c ON (c."IdCliente" = cs."IdCliente")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS p ON (p."IdPortafolio" = pcs."IdPortafolio")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS m ON (m."IdMatrizAgrupadorAPV" = p."IdMatrizAgrupadorAPV")
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS ci ON (ci."IdPortafolio" = pcs."IdPortafolio" AND "IdMoneda" = 'CLP')
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS ca ON (ca."IdCuentaInversion" = ci."IdCuentaInversion")
                                                              
WITH READ ONLY;
GRANT SELECT ON "BSCL.APV.BD.Vistas::SCS.ConsultarContratoSAAPVCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
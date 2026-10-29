SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaCuentasBancarias";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaCuentasBancarias" AS
SELECT
    CTA."IdCuentaBancaria" AS TABLE_KEY,
    DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
    CTA."IdPersona" AS "IDPERSONA",
    CTA."IdCuentaBancaria" AS "IDCUENTABANCARIA",
    CTA."IdBanco" AS "IDBANCO",
    BAN."NombreBanco" AS "NOMBREBANCO",
    CTA."IdTipoCuentaBancaria" AS "IDTIPOCUENTABANCARIA",
    TIP."NombreTipoCuentaBancaria" AS "NOMBRETIPOCUENTABANCARIA",
    CTA."NumeroCuentaBancaria" AS "NUMEROCUENTABANCARIA",
    BAN."IdPaisOperacion" AS "IDPAISOPERACION",
    PAI."NombrePais" AS "NOMBREPAIS",
    "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA', CTA."IdEstado", CTA."IdEstadoAutorizacion" ) AS "IDESTADO",
	ESTCTA."Nombre" AS "NOMBREESTADO"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTA
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TIP ON TIP."IdTipoCuentaBancaria" = CTA."IdTipoCuentaBancaria"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON BAN."IdBanco" = CTA."IdBanco"
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON PAI."IdPais" = BAN."IdPaisOperacion"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS ESTSOCCTA ON ESTSOCCTA."IdEstado" = "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA', CTA."IdEstado", CTA."IdEstadoAutorizacion") AND ESTSOCCTA."NombreEntidad" = 'CUENTABANCARIA'
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCTA ON ESTCTA."IdEstado" = ESTSOCCTA."IdEstado"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CTA."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
WHERE CTA."Borrado" = 'N' AND CTA."EsFatca" = 'S'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaCuentasBancarias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
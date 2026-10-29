
-- Obtiene informacion de cuentas bancarias de la corredora
INSERT INTO "BSCL.Tesoreria.BD::GBL.CuentaBancariaCorredoraGenerarComprobante"
(	
	"IdPersona","NombreCompleto","DocumentoIdentidadDefecto","CodigoTipoPersona","IdCuentaBanco","IdBanco",
	"IdPlaza","PlazoRetencionMismoBanco","PlazoRetencionOtroBanco","NombreCuentaBancaria",
	"NumeroCuentaBancaria",	"IdTipoCuentaBancaria",	"NombreTipoCuentaBancaria"
)
SELECT DISTINCT CB."IdPersona",SNNOMBRE."NombreCompleto",IDENT."DocumentoIdentidad" AS"DocumentoIdentidadDefecto",SN."Tipo" AS "CodigoTipoPersona",
	   CB."IdCuentaBanco",CB."IdBanco",CB."IdPlaza",CB."PlazoRetencionMismoBanco",CB."PlazoRetencionOtroBanco",
	   CB."NombreCuentaBancaria",CB."NumeroCuentaBancaria",CB."IdTipoCuentaBancaria",TCBC."NombreTipoCuentaBancaria"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CB
INNER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CB."IdTipoCuentaBancaria")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" SN ON SN."IdPersona" = CB."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" SNNOMBRE ON SN."IdPersona" = SNNOMBRE."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON IDENT."IdPersona" = SN."IdPersona" and IDENT."Borrado" = 'N'
WHERE CB."IdEmpresa" = :IdEmpresa;

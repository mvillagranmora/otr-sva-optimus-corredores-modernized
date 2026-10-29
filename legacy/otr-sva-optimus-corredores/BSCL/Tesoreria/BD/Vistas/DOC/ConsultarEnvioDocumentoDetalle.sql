SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumentoDetalle";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumentoDetalle" AS

SELECT
	 ED."IdCaja"
	,CAJ."Codigo" AS "CodigoCaja"
	,ED."FechaHoraEnvio"
	,CAST(ED."FechaHoraEnvio" AS DATE) AS "FechaEnvio"
	,ED."IdEmpresa"
	,ED."IdUsuario" AS "IdUsuarioEnvioDocumento"
	,ED."IdEstado" AS "IdEstadoEnvioDocumento"
	,EDD."IdEnvioDocumentoDetalle"
	,EDD."IdEnvioDocumento"
	,EDD."IdComprobanteFormaDePago"
	,EDD."FechaHoraRevision"
	,CAST(EDD."FechaHoraRevision" AS DATE) AS "FechaRevision"
	,EDD."IdUsuarioRevision"
	,USU."CodigoUsuario" AS "CodigoUsuarioRevision"
	,EDD."IdEstado" As "IdEstadoEnvioDocumentoDetalle"
	,EST."Nombre" AS "NombreEstado"
	,CFP."IdComprobante"
	,CFP."IdMoneda" AS "IdMonedaComprobanteFormaDePago"
	,CFP."NombreMedioDePago"
	,CFP."IdBancoSocio"
	,BAN."NombreBanco" AS "NombreBancoSocio"
	,CFP."NumeroCuentaBancariaSocio"
	,CFP."NumeroSerieSocio"
	,CFP."NumeroDocumentoSocio"
	
	,CFP."Monto"
FROM "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle" AS EDD
INNER JOIN "BSCL.Tesoreria.BD::TES.EnvioDocumento" AS ED ON (ED."IdEnvioDocumento" = EDD."IdEnvioDocumento")
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = EDD."IdComprobanteFormaDePago")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = EDD."IdUsuarioRevision")
LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" AS CAJ ON (CAJ."IdCaja" = ED."IdCaja")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" =  EDD."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (BAN."IdBanco" = CFP."IdBancoSocio")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumentoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;



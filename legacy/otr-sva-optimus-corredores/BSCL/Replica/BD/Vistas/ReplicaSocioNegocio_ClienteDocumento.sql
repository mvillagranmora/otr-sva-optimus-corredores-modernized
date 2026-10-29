SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDocumento";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDocumento" AS
SELECT
	CLI."IdCliente" || ':' || TPO."IdTipoDocumento" || ':' || COALESCE(DOC."IdDocumento",0) AS TABLE_KEY,
	DI."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	DOC."IdDocumento" AS "IDDOCUMENTO",
	CLI."IdCliente" AS "IDCLIENTE",
	CLI."IdPersona" AS "IDPERSONA",
	TPO."IdTipoDocumento" AS "IDTIPODOCUMENTO",
	TPO."NombreDocumento" AS "NOMBREDOCUMENTO",
	TPO."IdEmpresa" AS "IDEMPRESA",
	ANX."IdAnexo" AS "IDANEXO",
	TPO."Glosa" AS "GLOSA",
	IFNULL(ANX."IdEstado",'PEN') AS "IDESTADO",
	EST."Nombre" AS "NOMBREESTADO",
	(SELECT  STRING_AGG(CNL."IdTipoCanalIngreso", ',')   
     FROM "BSCL.Plataforma.BD::DOC.CanalesDocumento" AS CNL 
     WHERE CNL."IdTipoDocumento" = TPO."IdTipoDocumento"  GROUP BY CNL."IdTipoDocumento" ) AS "CANALESINGRESO",
     ANX."IdTipoCanalIngreso" AS "IDTIPOCANALINGRESO",
     ANX."FechaIngreso" AS "FECHAINGRESO",
     CASE WHEN IFNULL(ANX."IdEstado",'PEN') <> 'PEN' THEN ADD_DAYS(MAX(LOG."FechaLog"),CONFDV."Valor") ELSE ANX."FechaVigencia" END AS "FECHAVIGENCIA",
     TPO."FechaFirma" AS "FECHAFIRMA",
     ANE."FechaFirma" AS "FECHAFIRMAANEXO",
	ANX."IdUsuarioDescargaDocumento" AS "IDUSUARIOSOLICITANTE",
	CASE PERSOL."Tipo" 
			WHEN 'N' THEN PERSOL."Nombres" ||' '|| PERSOL."ApellidoPaterno" || ' ' ||PERSOL."ApellidoMaterno"
			ELSE PERSOL."RazonSocial"
		END  AS "SOLICITADOPOR",
	USUSOL."CodigoUsuario" AS "USUARIOSOLICITANTE",
	ANE."IdUsuario" AS "IDUSUARIOFIRMANTE",
	CASE PERFIR."Tipo" 
			WHEN 'N' THEN PERFIR."Nombres" ||' '|| PERFIR."ApellidoPaterno" || ' ' ||PERFIR."ApellidoMaterno"
			ELSE PERFIR."RazonSocial"
		END  AS "NOMBREUSUARIOFIRMA",
	USUFIR."CodigoUsuario" AS "USUARIOFIRMA",
	CASE WHEN DOC."DocumentoSubido" IS NULL THEN 'N' ELSE DOC."DocumentoSubido" END AS "DOCUMENTOSUBIDO"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
CROSS JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento" AND DOC."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = IFNULL(ANX."IdEstado",'PEN')
LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON DI."IdPersona" = CLI."IdPersona" AND DI."Borrado" = 'N' AND DI."Predeterminado" = 'S'
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUSOL ON  (ANX."IdUsuarioDescargaDocumento" = USUSOL."IdUsuario")
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOCSOL
ON (SOCSOL."IdEmpleado" = USUSOL."IdEntidad" AND USUSOL."TipoEntidad" = 'EMP')
	OR  (SOCSOL."IdCliente" = USUSOL."IdEntidad" AND USUSOL."TipoEntidad" = 'CLI')
	OR	(SOCSOL."IdOperador" = USUSOL."IdEntidad" AND USUSOL."TipoEntidad" = 'OPE')
	OR	(SOCSOL."IdProveedor" = USUSOL."IdEntidad" AND USUSOL."TipoEntidad" = 'PRO')
	OR	(SOCSOL."IdPersona" = USUSOL."IdEntidad" AND USUSOL."TipoEntidad" = 'PER')
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERSOL ON PERSOL."IdPersona" =  SOCSOL."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUFIR ON  (ANE."IdUsuario" = USUFIR."IdUsuario")
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOCFIR
ON (SOCFIR."IdEmpleado" = USUFIR."IdEntidad" AND USUFIR."TipoEntidad" = 'EMP')
	OR  (SOCFIR."IdCliente" = USUFIR."IdEntidad" AND USUFIR."TipoEntidad" = 'CLI')
	OR	(SOCFIR."IdOperador" = USUFIR."IdEntidad" AND USUFIR."TipoEntidad" = 'OPE')
	OR	(SOCFIR."IdProveedor" = USUFIR."IdEntidad" AND USUFIR."TipoEntidad" = 'PRO')
	OR	(SOCFIR."IdPersona" = USUFIR."IdEntidad" AND USUFIR."TipoEntidad" = 'PER')
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERFIR ON PERFIR."IdPersona" =  SOCFIR."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Log" AS LOG ON LOG."IdCliente" = CLI."IdCliente" OR LOG."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DETALLE_LOG ON DETALLE_LOG."IdLog" = LOG."IdLog"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" AS CAMPO_LOG ON CAMPO_LOG."CodigoCampo" = DETALLE_LOG."CampoRegistro" AND CAMPO_LOG."AfectaFechaModificacion" = 'S'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."Tipo" = CAMPO_LOG."Tipo"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFDV ON CONFDV."IdEmpresa" = TPO."IdEmpresa" AND CONFDV."CodigoParametro" = 'PLA.NumeroDiasFechaVencimiento'
GROUP BY CLI."IdCliente",TPO."IdTipoDocumento",ANX."FechaVigencia",ANX."IdAnexo",ANX."IdUsuarioDescargaDocumento",PERSOL."Tipo",PERSOL."Nombres",PERSOL."ApellidoPaterno",PERSOL."ApellidoMaterno",PERSOL."RazonSocial",
PERFIR."Tipo",PERFIR."Nombres",PERFIR."ApellidoPaterno",PERFIR."ApellidoMaterno",PERFIR."RazonSocial",ANE."IdUsuario",DOC."IdDocumento",DI."DocumentoIdentidad",CLI."IdPersona",TPO."IdTipoDocumento",TPO."NombreDocumento",
TPO."IdEmpresa",TPO."Glosa",ANX."IdEstado",EST."Nombre",ANX."IdTipoCanalIngreso",ANX."FechaIngreso",ANX."FechaVigencia",TPO."FechaFirma",ANE."FechaFirma",DOC."DocumentoSubido",CONFDV."Valor",USUSOL."CodigoUsuario",USUFIR."CodigoUsuario"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
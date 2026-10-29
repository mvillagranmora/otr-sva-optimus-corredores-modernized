SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteLogRegistroCategoriaInversionista";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteLogRegistroCategoriaInversionista" AS
SELECT 
	CRCI."IdRegistroEncuesta" AS TABLE_KEY, 
	CRCI."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
	CRCI."IdPersona" AS "IDPERSONA",
	CRCI."IdCliente" AS "IDCLIENTE",
	CRCI."IdEmpresa" AS "IDEMPRESA",
	CRCI."IdRegistroEncuesta" AS "IDREGISTROENCUESTA",
	CRCI."IdCategoriaInversionista" AS "IDCATEGORIAINVERSIONISTA",
	CRCI."AsignadoPorEncuesta" AS "ASIGNADOPORENCUESTA",
	CRCI."NombreEncuesta" AS "NOMBREENCUESTA",
	CRCI."PuntajeTotal" AS "PUNTAJETOTAL",
	CRCI."FechaIngresoRegistro" AS "FECHAINGRESOREGISTRO",
	CRCI."FechaRegistroCategoria" AS "FECHAREGISTROCATEGORIA",
	CRCI."IdUsuarioResponde" AS "IDUSAURIORESPONDE",
	CRCI."NombreCategoriaInversionista" AS "NOMBRECATEGORIAINVERSIONISTA",
	CRCI."CodigoUsuarioResponde" AS "CODIGOUSUARIORESPONDE",
	CRCI."IdPortafolio" AS "IDPORTAFOLIO",
	CRCI."CodigoPortafolio" AS "CODIGOPORTAFOLIO",
	CRCI."IdPerfilProductos" AS "IDPERFILPRODUCTOS",
	CRCI."IdCanal" AS "IDCANAL",
	CRCI."TipoIngreso" AS "TIPOINGRESO"
FROM (
	SELECT 
		REG."IdEmpresa",
		REG."IdCliente",
		CLI."IdPersona",
		DOC."DocumentoIdentidad",
		REG."IdRegistroEncuesta",
		REG."IdCategoriaInversionista",
		"AsignadoPorEncuesta",
		"NombreEncuesta",
		REG."Puntaje" AS "PuntajeTotal",
		REG."FechaIngresoRegistro",
		"FechaEncuesta" as "FechaRegistroCategoria",
		"IdUsuarioResponde",
		"NombreCategoriaInversionista",
		"CodigoUsuario" as "CodigoUsuarioResponde",
		POR."IdPortafolio",
		COALESCE(POR."CodigoPortafolio",'')  AS "CodigoPortafolio",
		COALESCE(REG."IdPerfilProductos",'')  AS "IdPerfilProductos",
		CASE WHEN REG."IdCanal"= 'AGE' THEN 'AGENTE' ELSE  CASE WHEN REG."IdCanal"='CLI'THEN 'CLIENTE'ELSE CASE WHEN REG."IdCanal"='OPT'THEN 'OPTIMUS' ELSE  CASE WHEN REG."IdCanal"='WS'THEN 'WS' ELSE '' END END END END  AS "IdCanal",
		CASE WHEN REG."AsignadoPorEncuesta"='N' THEN 'Manual' ELSE CASE WHEN REG."AsignadoPorEncuesta"='S' THEN 'Encuesta' ELSE CASE WHEN REG."AsignadoPorEncuesta"='P'THEN 'Planilla Portafolio' ELSE 'Producto Extra' END  END  END AS "TipoIngreso"
	FROM "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" AS REG
	LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS CI on REG."IdCategoriaInversionista" = CI."IdCategoriaInversionista" and REG."IdEmpresa" = CI."IdEmpresa"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU on REG."IdUsuarioResponde" = USU."IdUsuario"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio"= REG."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = REG."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
	UNION
	SELECT DISTINCT 		
		CLI."IdEmpresa",
		CLI."IdCliente",
		CLI."IdPersona",
		DOC."DocumentoIdentidad",
		LOG."IdLog" AS "IdRegistroEncuesta",
		DET."ValorNuevo" AS"IdCategoriaInversionista",
		'N' AS "AsignadoPorEncuesta",
		"NombreProceso" AS "NombreEncuesta",
		0 AS "PuntajeTotal",
		"FechaLog" AS "FechaIngresoRegistro",
		CAST("FechaRegistro" AS SECONDDATE) AS "FechaRegistroCategoria",
		USU."IdUsuario"AS "IdUsuarioResponde",
		DET."ValorNuevo" AS "NombreCategoriaInversionista",
		"CodigoUsuario" as "CodigoUsuarioResponde",
		POR."IdPortafolio",
		POR."CodigoPortafolio" AS "CodigoPortafolio",
		DET."ValorNuevo" AS "IdPerfilProductos",
		CASE WHEN "IdModulo"= 'AGE' THEN 'AGENTE' ELSE  CASE WHEN "IdModulo"='CLI'THEN 'CLIENTE'ELSE CASE WHEN "IdModulo"='OPT'THEN 'OPTIMUS' ELSE "IdModulo" END END  END  AS "IdCanal",
		"NombreProceso" AS "TipoIngreso"
	FROM "BSCL.Plataforma.BD::PLA.Log"             AS LOG
	LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DET  ON (DET."IdLog" = LOG."IdLog")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS CLI  ON (CLI."IdCliente" = LOG."IdCliente")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" AS CLOG ON (CLOG."CodigoCampo" = DET."CampoRegistro")
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario"    AS USU ON (USU."IdUsuario" = LOG."IdUsuario")
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = CLI."IdPortafolioDefecto")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
	INNER JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS CAT ON (CAT."IdCategoriaInversionista" = DET."ValorNuevo")
	WHERE CLOG."AfectaFechaModificacion" = 'N' 
	AND NOT LOG."IdCliente" = 0
	GROUP BY  CLI."IdEmpresa", CLI."IdCliente", CLI."IdPersona", DOC."DocumentoIdentidad", LOG."IdLog", "FechaLog", USU."IdUsuario"
	, "CodigoUsuario" , POR."IdPortafolio", POR."CodigoPortafolio" , DET."ValorNuevo" , "IdModulo", "NombreProceso", "FechaRegistro" 
) AS CRCI
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteLogRegistroCategoriaInversionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
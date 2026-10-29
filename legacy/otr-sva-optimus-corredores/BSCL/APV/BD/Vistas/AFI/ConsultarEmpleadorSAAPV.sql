SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarEmpleadorSAAPV";
 
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarEmpleadorSAAPV"  AS

SELECT
	Persona."IdPersona"							AS "IdPersona"
   ,Persona."IdDocumentoIdentidad"				AS "IdDocumentoIdentidad"
   ,Persona."Tipo"								AS "TipoPersona"
   ,Identidad."DocumentoIdentidad"				AS "DocumentoIdentidadEmpleador"
   ,Persona."RazonSocial"						AS "RazonSocialEmpleador"
   ,Ciudad."NombreCiudad"						AS "NombreCiudad"
   ,DireccionFisicaEmpleador."IdDivisionPais"	AS "IdComunaEmpleador"
   ,DireccionFisicaEmpleador."Calle"			AS "DireccionEmpleador"
   ,DireccionFisicaEmpleador."Numero"			AS "NumeroDireccionEmpleador"
   ,Telefono."Numero"							AS "TelefonoEmpleador"
   ,NombreEncargado."ValorIndicador"			AS "NombreEncargado"
   ,EmailEncargado."ValorIndicador"				AS "EmailEncargado"
   ,TelefonoEncargado."ValorIndicador"			AS "TelefonoEncargado"
FROM
	"BSCL.SocioNegocio.BD::PER.Persona" AS Persona
INNER JOIN
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS Identidad
ON  Persona."IdDocumentoIdentidad" = Identidad."IdDocumentoIdentidad"
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaEmpleador
ON  DireccionFisicaEmpleador."IdPersona" = Persona."IdPersona"
AND DireccionFisicaEmpleador."IdDireccionFisica" = (	SELECT "IdDireccionFisica" FROM (
															SELECT  "IdDireccionFisica",
																	ROW_NUMBER() OVER(ORDER BY "IdDireccionFisica") 	AS "Id"
															FROM (
																	SELECT "IdDireccionFisica"
																	FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" 
																	WHERE "IdPersona" = Persona."IdPersona"
																	AND "IdEstado" = 'ACT'
																	AND "Predeterminado" = 'S'
																	UNION ALL
																	SELECT MAX("IdDireccionFisica") 
																	FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" 
																	WHERE "IdPersona" = Persona."IdPersona"
																	AND "IdEstado" = 'ACT'
																)
														) 
														WHERE "Id" = 1
													)
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad
ON  Ciudad."IdCiudad" = DireccionFisicaEmpleador."IdCiudad"
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS Telefono
ON  Telefono."IdPersona" = Persona."IdPersona"
AND Telefono."Borrado" = 'N'
LEFT OUTER JOIN
(
	SELECT
		Indicador."IdEntidad"																			AS "IdPersona"
	   ,CAST(TipoIndicador."CodigoTipoIndicador" AS NVARCHAR(40))										AS "CodigoTipoIndicador"
	   ,CAST(IFNULL(Indicador."ValorIndicador",TipoIndicador."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador"
	FROM
		"BSCL.Plataforma.BD::IND.Indicador" AS Indicador
	INNER JOIN
		"BSCL.Plataforma.BD::IND.TipoIndicador" AS TipoIndicador
	ON  Indicador."IdTipoEntidad" = TipoIndicador."IdTipoEntidad"
	AND Indicador."IdTipoIndicador" = TipoIndicador."IdTipoIndicador"
	AND TipoIndicador."IdTipoEntidad" = 'PERSONA'
	AND TipoIndicador."GrupoIndicador" = 'DATOS EMPLEADOR'
	AND TipoIndicador."IdEstado" != 'INA'
	ORDER BY
		Indicador."IdEntidad"
)	AS NombreEncargado
ON  NombreEncargado."IdPersona" = Persona."IdPersona"
AND NombreEncargado."CodigoTipoIndicador" = 'NOMBRE_ENCARGADO_RRHH'
AND NombreEncargado."ValorIndicador" IS NOT NULL
AND NombreEncargado."ValorIndicador" != ''
LEFT OUTER JOIN
(
	SELECT
		Indicador."IdEntidad"																			AS "IdPersona"
	   ,CAST(TipoIndicador."CodigoTipoIndicador" AS NVARCHAR(40))										AS "CodigoTipoIndicador"
	   ,CAST(IFNULL(Indicador."ValorIndicador",TipoIndicador."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador"
	FROM
		"BSCL.Plataforma.BD::IND.Indicador" AS Indicador
	INNER JOIN
		"BSCL.Plataforma.BD::IND.TipoIndicador" AS TipoIndicador
	ON  Indicador."IdTipoEntidad" = TipoIndicador."IdTipoEntidad"
	AND Indicador."IdTipoIndicador" = TipoIndicador."IdTipoIndicador"
	AND TipoIndicador."IdTipoEntidad" = 'PERSONA'
	AND TipoIndicador."GrupoIndicador" = 'DATOS EMPLEADOR'
	AND TipoIndicador."IdEstado" != 'INA'
	ORDER BY
		Indicador."IdEntidad"
)	AS EmailEncargado
ON  EmailEncargado."IdPersona" = Persona."IdPersona"
AND EmailEncargado."CodigoTipoIndicador" = 'MAIL_ENCARGADO_RRHH'
AND EmailEncargado."ValorIndicador" IS NOT NULL
AND EmailEncargado."ValorIndicador" != ''
LEFT OUTER JOIN
(
	SELECT
		Indicador."IdEntidad"																			AS "IdPersona"
	   ,CAST(TipoIndicador."CodigoTipoIndicador" AS NVARCHAR(40))										AS "CodigoTipoIndicador"
	   ,CAST(IFNULL(Indicador."ValorIndicador",TipoIndicador."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador"
	FROM
		"BSCL.Plataforma.BD::IND.Indicador" AS Indicador
	INNER JOIN
		"BSCL.Plataforma.BD::IND.TipoIndicador" AS TipoIndicador
	ON  Indicador."IdTipoEntidad" = TipoIndicador."IdTipoEntidad"
	AND Indicador."IdTipoIndicador" = TipoIndicador."IdTipoIndicador"
	AND TipoIndicador."IdTipoEntidad" = 'PERSONA'
	AND TipoIndicador."GrupoIndicador" = 'DATOS EMPLEADOR'
	AND TipoIndicador."IdEstado" != 'INA'
	ORDER BY
		Indicador."IdEntidad"
)	AS TelefonoEncargado
ON  TelefonoEncargado."IdPersona" = Persona."IdPersona"
AND TelefonoEncargado."CodigoTipoIndicador" = 'TELEFONO_ENCARGADO_RRHH'
AND TelefonoEncargado."ValorIndicador" IS NOT NULL
AND TelefonoEncargado."ValorIndicador" != ''
WHERE
	Persona."Borrado"='N'
AND Identidad."Borrado"='N'
AND Persona."Tipo" = 'J';

GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarEmpleadorSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
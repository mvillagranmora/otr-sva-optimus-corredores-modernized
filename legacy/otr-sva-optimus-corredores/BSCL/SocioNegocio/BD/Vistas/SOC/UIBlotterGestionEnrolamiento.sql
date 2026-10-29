SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterGestionEnrolamiento";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterGestionEnrolamiento"  AS

SELECT 	
	NULL																										AS "IdSocioDeNegocio"
   ,ENR."IdEmpresa" 																							AS "IdEmpresa"
   ,NULL																										AS "IdPersona"
   ,NULL																										AS "IdCliente"
   ,NULL																										AS "IdEmpleado"
   ,NULL																										AS "IdProveedor"
   ,NULL																										AS "IdContraparte"
   ,"FechaCreacion"					        																	AS "FechaIngreso"
   ,"FechaCreacion"				        																		AS "FechaCreacion" 
   ,NULL 																										AS "FechaModificacion"
   ,ENR."IdTipoDocumentoIdentidad"																				AS "IdTipoDocumentoIdentidadDefecto"
   ,TIDEN."NombreTipoDocumentoIdentidad"																		AS "NombreTipoDocumentoIdentidadDefecto"
   ,TIDEN."NombreAbrTipoDocumentoIdentidad"																		AS "NombreAbrTipoDocumentoIdentidadDefecto"
   ,TIDENADIC."IdTipoDocumentoIdentidad"																		AS "IdTipoDocumentoIdentidadAdicional"
   ,TIDENADIC."NombreTipoDocumentoIdentidad"																	AS "NombreTipoDocumentoIdentidadAdicional"
   ,TIDENADIC."NombreAbrTipoDocumentoIdentidad"																	AS "NombreAbrTipoDocumentoIdentidadAdicional"
   ,ENR."DocumentoIdentidad"																					AS "DocumentoIdentidadDefecto"
   ,NULL																										AS "DocumentoIdentidadAdicional"
   ,ENR."Tipo"																									AS "CodigoTipoPersona"
   ,TPER."Descripcion"                  																		AS "TipoPersona"
   ,MAP(ENR."Tipo",'N',ENR."Nombres"||' '||ENR."ApellidoPaterno"||' '||ENR."ApellidoMaterno",ENR."RazonSocial")	AS "NombreCompleto"
   ,ENR."RazonSocial" 																							AS "RazonSocial"
   ,''																											AS "Alias"
   ,ENR."Nombres" 																								AS "Nombres"
   ,ENR."ApellidoPaterno"         	                															AS "ApellidoPaterno"
   ,ENR."ApellidoMaterno"																						AS "ApellidoMaterno"
   ,ENR."FechaNacimiento"																						AS "FechaNacimiento"
   ,ENR."IdPais"																								AS "IdPaisResidencia"
   ,ENR."EstadoCivil"																							AS "IdEstadoCivil"
   ,TipoEstadoCivil."Nombre"																					AS "EstadoCivil"
   ,PAIS."CodigoPais" 																							AS "CodigoPaisResidencia"
   ,PAIS."NombrePais"																							AS "NombrePaisResidencia"
   ,'UI_NO'																										AS "EsPersona"
   ,'UI_NO'																										AS "EsCliente"
   ,'UI_NO'																										AS "EsEmpleado"
   ,'UI_NO'																										AS "EsProveedor"
   ,'UI_NO'																										AS "EsContraparte"
	-- ID ESTADOS
   ,NULL																										AS "EstadoPersona"
   ,NULL																										AS "EstadoCliente"
   ,NULL																										AS "EstadoEmpleado"
   ,NULL																										AS "EstadoProveedor"
   ,NULL																										AS "EstadoContraparte"	
	-- NOMBRES ESTADOS
   ,NULL																										AS "NombreEstadoPersona"
   ,NULL																										AS "NombreEstadoCliente"
   ,NULL																										AS "NombreEstadoEmpleado"
   ,NULL																										AS "NombreEstadoProveedor"
   ,NULL																										AS "NombreEstadoContraparte"
   ,NULL																										AS "FechaBloqueo"
   ,'' 																											AS "ComentarioBloqueo"
   ,NULL																										AS "IdPersonaEjecutivo"
   ,NULL																										AS "IdSegmentoCliente"
   ,NULL																										AS "NombreSegmentoCliente"
   ,NULL																										AS "DescripcionSegmento"
   ,NULL																										AS "NombreCompletoEjecutivo"
   ,''																											AS "ListaDocumentosIdentidad"
FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEnrolamiento" ENR
INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS TIDEN
		ON ENR."IdTipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad"
 LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS TIDENADIC
 		ON ENR."IdTipoDocumentoIdentidadAdicional" = TIDENADIC."IdTipoDocumentoIdentidad"
 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS
 		ON PAIS."IdPais" = ENR."IdPais"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TPER
		ON TPER."IdTipoPersona" = ENR."Tipo"
 LEFT JOIN "BSCL.Plataforma.BD::REG.TipoEstadoCivil" AS TipoEstadoCivil
 		ON TipoEstadoCivil."IdTipoEstadoCivil" = ENR."EstadoCivil"
WHERE
	ENR."IdTipoEntidad" = 'PERSONA'
AND	ENR."DocumentoIdentidad" NOT IN (SELECT "DocumentoIdentidad" FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona")
UNION
SELECT
	SocioDeNegocio."IdSocioDeNegocio"													AS "IdSocioDeNegocio"
   ,SocioDeNegocio."IdEmpresa"															AS "IdEmpresa"
   ,Persona."IdPersona"																	AS "IdPersona"
   ,NULL																				AS "IdCliente"
   ,NULL																				AS "IdEmpleado"
   ,NULL																				AS "IdProveedor"
   ,NULL																				AS "IdContraparte"
   ,Persona."FechaIngreso"					        									AS "FechaIngreso"
   ,Persona."FechaCreacion"				        										AS "FechaCreacion"
   ,Persona."FechaModificacion"															AS "FechaModificacion"
   ,TipoDocumentoIdentidad."IdTipoDocumentoIdentidad"									AS "IdTipoDocumentoIdentidadDefecto"
   ,TipoDocumentoIdentidad."NombreTipoDocumentoIdentidad"								AS "NombreTipoDocumentoIdentidadDefecto"
   ,TipoDocumentoIdentidad."NombreAbrTipoDocumentoIdentidad"							AS "NombreAbrTipoDocumentoIdentidadDefecto"
   ,NULL																				AS "IdTipoDocumentoIdentidadAdicional"
   ,NULL																				AS "NombreTipoDocumentoIdentidadAdicional"
   ,NULL																				AS "NombreAbrTipoDocumentoIdentidadAdicional"
   ,DocumentoIdentidad."DocumentoIdentidad"												AS "DocumentoIdentidadDefecto"
   ,NULL																				AS "DocumentoIdentidadAdicional"
   ,Persona."Tipo"																		AS "CodigoTipoPersona"
   ,ConsultarTipoPersona."Descripcion"													AS "TipoPersona"
   ,Persona."Nombres"||' '||Persona."ApellidoPaterno"||' '||Persona."ApellidoMaterno"	AS "NombreCompleto"
   ,Persona."RazonSocial"																AS "RazonSocial"
   ,''																					AS "Alias"
   ,Persona."Nombres"																	AS "Nombres"
   ,Persona."ApellidoPaterno"															AS "ApellidoPaterno"
   ,Persona."ApellidoMaterno"															AS "ApellidoMaterno"
   ,Persona."FechaNacimiento"															AS "FechaNacimiento"
   ,Persona."IdPaisResidencia"															AS "IdPaisResidencia"
   ,Persona."EstadoCivil"																AS "IdEstadoCivil"
   ,TipoEstadoCivil."Nombre"															AS "EstadoCivil"
   ,Pais."CodigoPais"																	AS "CodigoPaisResidencia"
   ,Pais."NombrePais"																	AS "NombrePaisResidencia"
   ,'UI_NO'																				AS "EsPersona"
   ,'UI_NO'																				AS "EsCliente"
   ,'UI_NO'																				AS "EsEmpleado"
   ,'UI_NO'																				AS "EsProveedor"
   ,'UI_NO'																				AS "EsContraparte"
	-- ID ESTADOS
   ,NULL																				AS "EstadoPersona"
   ,NULL																				AS "EstadoCliente"
   ,NULL																				AS "EstadoEmpleado"
   ,NULL																				AS "EstadoProveedor"
   ,NULL																				AS "EstadoContraparte"	
	-- NOMBRES ESTADOS
   ,NULL																				AS "NombreEstadoPersona"
   ,NULL																				AS "NombreEstadoCliente"
   ,NULL																				AS "NombreEstadoEmpleado"
   ,NULL																				AS "NombreEstadoProveedor"
   ,NULL																				AS "NombreEstadoContraparte"
   ,NULL																				AS "FechaBloqueo"
   ,'' 																					AS "ComentarioBloqueo"
   ,NULL																				AS "IdPersonaEjecutivo"
   ,NULL																				AS "IdSegmentoCliente"
   ,NULL																				AS "NombreSegmentoCliente"
   ,NULL																				AS "DescripcionSegmento"
   ,NULL																				AS "NombreCompletoEjecutivo"
   ,''																					AS "ListaDocumentosIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
		ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
	   AND DocumentoIdentidad."IdPersona" = Persona."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS TipoDocumentoIdentidad
		ON TipoDocumentoIdentidad."IdTipoDocumentoIdentidad" = DocumentoIdentidad."IdTipoDocumentoIdentidad"
 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais
 		ON Pais."IdPais" = Persona."IdPaisResidencia"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS ConsultarTipoPersona
		ON ConsultarTipoPersona."IdTipoPersona" = Persona."Tipo"
 LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocioDeNegocio
 		ON SocioDeNegocio."IdPersona" = Persona."IdPersona"
 	   AND SocioDeNegocio."Borrado" = 'N'
 LEFT JOIN "BSCL.Plataforma.BD::REG.TipoEstadoCivil" AS TipoEstadoCivil
 		ON TipoEstadoCivil."IdTipoEstadoCivil" = Persona."EstadoCivil"
WHERE
	Persona."Tipo" = 'N'
AND 
(	Persona."Nombres" = ''
OR 	Persona."ApellidoPaterno" = ''
OR 	Persona."IdPaisNacimiento" IS NULL
OR	Persona."FechaNacimiento" = '0001-01-01'
OR	Persona."FechaNacimiento" IS NULL
OR	Persona."EstadoCivil" IS NULL
OR	Persona."Sexo" = ''
OR	Persona."IdPaisResidencia" IS NULL
OR	Persona."IdDireccionFisicaPrincipal" IS NULL
OR	Persona."IdDireccionElectronicaPrincipal" IS NULL
OR	Persona."IdTelefonoPrincipal" IS NULL);

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterGestionEnrolamiento" TO BCS_DESARROLLADOR WITH GRANT OPTION;
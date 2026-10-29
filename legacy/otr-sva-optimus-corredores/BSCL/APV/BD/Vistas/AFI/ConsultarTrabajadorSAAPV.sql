SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarTrabajadorSAAPV";
 
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarTrabajadorSAAPV"  AS
SELECT DISTINCT
	Persona."IdPersona"																					AS "IdPersona"
   ,Persona."IdDocumentoIdentidad"																		AS "IdDocumentoIdentidad"
   ,Persona."Tipo"																						AS "TipoPersona"
   ,Identidad."DocumentoIdentidad"																		AS "DocumentoIdentidad"
   ,Persona."Nombres"																					AS "Nombres"
   ,Persona."ApellidoPaterno"																			AS "ApellidoPaterno"
   ,Persona."ApellidoMaterno"																			AS "ApellidoMaterno"
   ,Persona."Nombres" ||' '|| Persona."ApellidoPaterno" || ' ' ||Persona."ApellidoMaterno" 				AS "NombreCompleto"
   ,Persona."FechaNacimiento"																			AS "FechaNacimiento"
   ,COALESCE(Persona."IdPaisNacionalidadPrincipal",0)													AS "Nacionalidad"
   ,Persona."EstadoCivil"																				AS "EstadoCivil"
   ,Persona."Sexo"																						AS "Genero"
   ,Persona."IdPaisNacimiento"																			AS "IdPaisNacimiento"
   ,Empleado."IdEmpleado"																										AS "IdEmpleadoEjecutivo"
   ,PersonaEjecutivo."Nombres" || ' ' || PersonaEjecutivo."ApellidoPaterno" || ' ' || PersonaEjecutivo."ApellidoMaterno" 		AS "NombrePersonaEjecutivo"
   ,PaisNacimiento."NombrePais"																			AS "PaisNacimiento"
   ,Persona."IdPaisResidencia"																			AS "IdPaisResidencia"
   ,PaisNacimiento."NombrePais"																			AS "PaisResidencia"
   ,CASE 
		WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."IdCiudad"
		WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."IdCiudad"
		WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."IdCiudad"
		WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."IdCiudad"
		ELSE NULL
	END 																								AS "IdCiudad"
   ,Ciudad."NombreCiudad"																				AS "NombreCiudad"
   ,CASE 
		WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."IdDivisionPais"
		WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."IdDivisionPais"
		WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."IdDivisionPais"
		WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."IdDivisionPais"
		ELSE NULL
	END 																								AS "IdComuna"
   ,Comuna."NombreDivisionPais"																			AS "NombreComuna"
   ,CASE 
		WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."Calle"
		WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."Calle"
		WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."Calle"
		WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."Calle"
		ELSE NULL
	END 																								AS "DireccionCliente"
   ,CASE 
		WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."Numero"
		WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."Numero"
		WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."Numero"
		WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."Numero"
		ELSE NULL
	END 																								AS "NumeroDirecionCliente"
   ,Persona."IdProfesion"																				AS "IdProfesion"
   ,Persona."Cargo"																						AS "Cargo"
   ,Cliente."IdCategoriaInversionista"																	AS "IdCategoriaInversionista"
   ,PerfilComercial."IdPerfilComercialBaseOrigen"														AS "IdPerfilComercial"
   ,CASE
   		WHEN TelefonoFijo."CodigoProposito" = 'PREV' THEN TelefonoFijo."Numero"
		WHEN TelefonoFijo."CodigoProposito" = 'PART' THEN TelefonoFijo."Numero"
		WHEN TelefonoFijo."CodigoProposito" = 'LABO' THEN TelefonoFijo."Numero"
		WHEN TelefonoFijo."CodigoProposito" = 'COME' THEN TelefonoFijo."Numero"
		ELSE NULL
	END 																								AS "TelefonoFijo"
   ,CASE
   		WHEN TelefonoMovil."CodigoProposito" = 'PREV' THEN TelefonoMovil."Numero"
		WHEN TelefonoMovil."CodigoProposito" = 'PART' THEN TelefonoMovil."Numero"
		WHEN TelefonoMovil."CodigoProposito" = 'LABO' THEN TelefonoMovil."Numero"
		WHEN TelefonoMovil."CodigoProposito" = 'COME' THEN TelefonoMovil."Numero"
		ELSE NULL
	END 																								AS "TelefonoMovil"
   ,CASE
		WHEN TelefonoOtro."CodigoProposito" = 'PREV' THEN TelefonoOtro."Numero"
		WHEN TelefonoOtro."CodigoProposito" = 'PART' THEN TelefonoOtro."Numero"
		WHEN TelefonoOtro."CodigoProposito" = 'LABO' THEN TelefonoOtro."Numero"
		WHEN TelefonoOtro."CodigoProposito" = 'COME' THEN TelefonoOtro."Numero"
		ELSE NULL
	END 																								AS "TelefonoLaboral"
   ,DireccionElectronica."Direccion"																	AS "DireccionElectronica"
   ,MatrizAgrupadorAPV."IdPlanTipoInversion"															AS "IdPlanTipoInversion"
FROM
	"BSCL.SocioNegocio.BD::PER.Persona" AS Persona
INNER JOIN
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS Identidad
ON  Persona."IdDocumentoIdentidad" = Identidad."IdDocumentoIdentidad"
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::REG.Pais" AS PaisNacimiento
ON  PaisNacimiento."IdPais" = Persona."IdPaisNacimiento"
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::REG.Pais" AS PaisResidencia
ON  PaisResidencia."IdPais" = Persona."IdPaisResidencia"
LEFT OUTER JOIN 
	"BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaPREV
ON  DireccionFisicaPREV."IdPersona" = Persona."IdPersona"
AND DireccionFisicaPREV."CodigoProposito" = 'PREV'
AND DireccionFisicaPREV."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Persona."IdPersona" AND "CodigoProposito" = 'PREV')
LEFT OUTER JOIN 
	"BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaPART
ON  DireccionFisicaPART."IdPersona" = Persona."IdPersona"
AND DireccionFisicaPART."CodigoProposito" = 'PART'
AND DireccionFisicaPART."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Persona."IdPersona" AND "CodigoProposito" = 'PART')
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaLABO
ON  DireccionFisicaLABO."IdPersona" = Persona."IdPersona"
AND DireccionFisicaLABO."CodigoProposito" = 'LABO'
AND DireccionFisicaLABO."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Persona."IdPersona" AND "CodigoProposito" = 'LABO')
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaCOME
ON  DireccionFisicaCOME."IdPersona" = Persona."IdPersona"
AND DireccionFisicaCOME."CodigoProposito" = 'COME'
AND DireccionFisicaCOME."IdDireccionFisica" = (SELECT MAX("IdDireccionFisica") FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdPersona" = Persona."IdPersona" AND "CodigoProposito" = 'COME')
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad
ON  Ciudad."IdCiudad" = CASE 
							WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."IdCiudad"
							WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."IdCiudad"
							WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."IdCiudad"
							WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."IdCiudad"
							ELSE NULL
						END
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::REG.DivisionPais" AS Comuna
ON  Comuna."IdDivisionPais" =	CASE 
									WHEN DireccionFisicaPREV."CodigoProposito" = 'PREV' THEN DireccionFisicaPREV."IdDivisionPais"
									WHEN DireccionFisicaPART."CodigoProposito" = 'PART' THEN DireccionFisicaPART."IdDivisionPais"
									WHEN DireccionFisicaLABO."CodigoProposito" = 'LABO' THEN DireccionFisicaLABO."IdDivisionPais"
									WHEN DireccionFisicaCOME."CodigoProposito" = 'COME' THEN DireccionFisicaCOME."IdDivisionPais"
									ELSE NULL
								END
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoFijo
ON  TelefonoFijo."IdPersona" = Persona."IdPersona"
AND TelefonoFijo."Borrado" = 'N'
AND TelefonoFijo."IdTipoTelefono" = 'FIJO'
AND TelefonoFijo."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Persona."IdPersona" AND "IdTipoTelefono" = 'FIJO' AND "Borrado" = 'N')
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoMovil
ON  TelefonoMovil."IdPersona" = Persona."IdPersona"
AND TelefonoMovil."Borrado" = 'N'
AND TelefonoMovil."IdTipoTelefono" = 'MOVIL'
AND TelefonoMovil."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Persona."IdPersona" AND "IdTipoTelefono" = 'MOVIL' AND "Borrado" = 'N')
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoOtro
ON  TelefonoOtro."IdPersona" = Persona."IdPersona"
AND TelefonoOtro."Borrado" = 'N'
AND TelefonoOtro."IdTipoTelefono" = 'OTRO'
AND TelefonoOtro."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Persona."IdPersona" AND "IdTipoTelefono" = 'OTRO' AND "Borrado" = 'N')
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
ON  Cliente."IdPersona" = Persona."IdPersona"
AND Cliente."Borrado" = 'N'
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaEjecutivo
ON Cliente."IdPersonaEjecutivo" = PersonaEjecutivo."IdPersona"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS Empleado
ON Empleado."IdPersona" = Cliente."IdPersonaEjecutivo"
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DireccionElectronica
ON  DireccionElectronica."IdDireccionElectronica" = Persona."IdDireccionElectronicaPrincipal"
LEFT OUTER JOIN 
	"BSCL.Portafolio.BD::POR.Portafolio" AS PerfilComercial
ON PerfilComercial."IdCliente" = Cliente."IdCliente"
AND PerfilComercial."IdMatrizAgrupadorAPV" > 0
AND PerfilComercial."IdEstado" <> 'INA'
--AND PerfilComercial."Proposito" <> 'PROPGENERALES'
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupadorAPV
ON MatrizAgrupadorAPV."IdMatrizAgrupadorAPV" = PerfilComercial."IdMatrizAgrupadorAPV"
WHERE
	Persona."Borrado" = 'N'
AND Identidad."Borrado" = 'N'
AND Persona."Tipo" = 'N';

GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarTrabajadorSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
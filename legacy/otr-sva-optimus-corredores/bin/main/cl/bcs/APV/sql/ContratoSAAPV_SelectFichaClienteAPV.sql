SELECT
	Persona."IdPersona"											AS "IdPersona"
   ,DocumentoIdentidad."DocumentoIdentidad"						AS "DocumentoIdentidad"
   ,Persona."Nombres"											AS "Nombres"
   ,Persona."ApellidoPaterno"									AS "ApellidoPaterno"
   ,Persona."ApellidoMaterno"									AS "ApellidoMaterno"
   ,TO_CHAR(Persona."FechaNacimiento",'YYYY-MM-DD')				AS "FechaNacimiento"
   ,DireccionFisica."Calle"										AS "Direccion"
   ,DireccionFisica."Numero"									AS "NumeroDireccion"
   ,TelefonoFijo."Numero"										AS "TelefonoFijo"
   ,TelefonoMovil."Numero"										AS "TelefonoMovil"
   ,TelefonoLaboral."Numero"									AS "TelefonoLaboral"
   ,Pais."IdPais"												AS "IdPais"
   ,Pais."NombrePais"											AS "Pais"
   ,ClasificacionDivisionPais."IdClasificacionDivisionPais"		AS "IdRegion"
   ,ClasificacionDivisionPais."NombreClasificacionDivisionPais"	AS "Region"
   ,DivisionPais."IdDivisionPais"								AS "IdComuna"
   ,DivisionPais."NombreDivisionPais"							AS "Comuna"
   ,Ciudad."IdCiudad"											AS "IdCiudad"
   ,Ciudad."NombreCiudad"										AS "Ciudad"
   ,Persona."EstadoCivil"										AS "EstadoCivil"
   ,Persona."Sexo"												AS "Genero"
   ,DireccionElectronica."Direccion"							AS "DireccionElectronica"
   ,Cliente."IdCategoriaInversionista"							AS "IdCategoriaInversionista"
   ,Portafolio."IdEmpleadoEjecutivo"							AS "IdEjecutivo"
   ,Portafolio."IdPerfilComercialBaseOrigen"					AS "IdPerfilComercial"
   ,Empleador."IdPersona"										AS "IdPersonaEmpleador"
   ,Empleador."RazonSocial"										AS "RazonSocialEmpleador"
   ,DocumentoIdentidadEmpleador."DocumentoIdentidad"			AS "RutEmpleador"
   ,Empleador."IdPaisResidencia"								AS "IdPaisEmpleador"
   ,DireccionComercialEmpleador."Calle"							AS "DireccionEmpleador"
   ,DireccionComercialEmpleador."Numero"						AS "NumeroDireccionEmpleador"
   ,CiudadEmpleador."IdCiudad"									AS "IdCiudadEmpleador"
   ,DivisionPaisEmpleador."IdDivisionPais"						AS "IdComunaEmpleador"
   ,TelefonoEmpleador."Numero"									AS "TelefonoEmpleador"
   --
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Persona."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisica ON DireccionFisica."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoFijo
ON TelefonoFijo."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Persona."IdPersona" AND "IdTipoTelefono" = 'FIJO' AND "Borrado" = 'N')
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoMovil
ON TelefonoMovil."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Persona."IdPersona" AND "IdTipoTelefono" = 'MOVIL' AND "Borrado" = 'N')
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoLaboral
ON TelefonoLaboral."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Persona."IdPersona" AND "CodigoProposito" = 'LABO' AND "Borrado" = 'N')
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DivisionPais ON DivisionPais."IdDivisionPais" = DireccionFisica."IdDivisionPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS ClasificacionDivisionPais ON ClasificacionDivisionPais."IdClasificacionDivisionPais" = DivisionPais."IdClasificacion1"
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad ON Ciudad."IdCiudad" = DireccionFisica."IdCiudad"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais ON Pais."IdPais" = ClasificacionDivisionPais."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DireccionElectronica ON DireccionElectronica."IdPersona" = Persona."IdPersona" AND DireccionElectronica."CodigoProposito" = 'PART'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Empleador ON Empleador."IdPersona" = Persona."IdPersonaEmpleador"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidadEmpleador ON DocumentoIdentidadEmpleador."IdDocumentoIdentidad" = Empleador."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionComercialEmpleador ON DireccionComercialEmpleador."IdPersona" = Empleador."IdPersona" AND DireccionComercialEmpleador."CodigoProposito" = 'COME'
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CiudadEmpleador ON CiudadEmpleador."IdCiudad" = DireccionComercialEmpleador."IdCiudad"
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DivisionPaisEmpleador ON DivisionPaisEmpleador."IdDivisionPais" = DireccionComercialEmpleador."IdDivisionPais"
LEFT OUTER JOIN
	"BSCL.SocioNegocio.BD::PER.Telefono" AS TelefonoEmpleador
ON TelefonoEmpleador."IdTelefono" = (SELECT MAX("IdTelefono") FROM "BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdPersona" = Empleador."IdPersona" AND "Borrado" = 'N')
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdCliente" = Cliente."IdCliente" AND Portafolio."IdPortafolio" = :p_IdPortafolio
WHERE
	Cliente."IdCliente" = :p_IdCliente
AND Portafolio."IdEstado" <> 'INA';
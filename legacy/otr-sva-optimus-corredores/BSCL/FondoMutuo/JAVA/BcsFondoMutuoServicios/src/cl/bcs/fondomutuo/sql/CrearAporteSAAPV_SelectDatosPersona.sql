SELECT
	Persona."IdPersona"											AS "IdPersona"
   ,DocumentoIdentidad."DocumentoIdentidad"						AS "DocumentoIdentidad"
   ,Persona."Nombres"											AS "Nombres"
   ,Persona."ApellidoPaterno"									AS "ApellidoPaterno"
   ,Persona."ApellidoMaterno"									AS "ApellidoMaterno"
   ,TO_CHAR(Persona."FechaNacimiento",'YYYY-MM-DD')				AS "FechaNacimiento"
   ,DireccionFisica."Calle"										AS "Direccion"
   ,DireccionFisica."Numero"									AS "NumeroDireccion"
   ,Telefono."Numero"											AS "Telefono"
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
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Persona."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad" AND DocumentoIdentidad."Predeterminado" ='S'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisica ON DireccionFisica."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS Telefono ON Telefono."IdTelefono" = Persona."IdTelefonoPrincipal" AND Telefono."CodigoProposito" = 'PART'
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DivisionPais ON DivisionPais."IdDivisionPais" = DireccionFisica."IdDivisionPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS ClasificacionDivisionPais ON ClasificacionDivisionPais."IdClasificacionDivisionPais" = DivisionPais."IdClasificacion1"
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad ON Ciudad."IdCiudad" = DireccionFisica."IdCiudad"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais ON Pais."IdPais" = ClasificacionDivisionPais."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DireccionElectronica ON DireccionElectronica."IdPersona" = Persona."IdPersona" AND DireccionElectronica."CodigoProposito" = 'PART'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdCliente" = Cliente."IdCliente" AND Portafolio."IdPortafolio" = :p_IdPortafolio
WHERE
	Cliente."IdCliente" = :p_IdCliente;
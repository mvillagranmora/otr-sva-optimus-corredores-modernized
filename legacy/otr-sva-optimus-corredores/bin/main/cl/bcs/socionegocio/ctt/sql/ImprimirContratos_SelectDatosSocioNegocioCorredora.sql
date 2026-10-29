SELECT Persona."IdPersona"
     , Persona."DocumentoIdentidad"
     , Persona."RazonSocial"
     , COALESCE(Giro."Nombre",'') 							AS "GiroComercial"
     , COALESCE(DireccionElectronica."Direccion",'') 		AS "DireccionElectronicaPrincipal"
     , COALESCE(DireccionFisica."Calle",'')					AS "DireccionFisicaPrincipal"
     , COALESCE(DireccionFisica."Numero",'')				AS "NumeroDireccion"
     , COALESCE(DireccionFisica."NombreCiudad",'')			AS "NombreCiudad"
     , COALESCE(DireccionFisica."NombreDivisionPais",'')	AS "NombreComuna"
     , COALESCE(DireccionFisica."NombrePais",'') 			AS "NombrePais"
     , COALESCE(Telefono."Numero",'') 						AS "NumeroTelefono"
     , COALESCE(ContratoDatosGenerales."Fax",'') 			AS "Fax"
     , COALESCE(Relacion."DocumentoIdentidadDestino",'')	AS "DocumentoIdentidadRepresentante"
     , COALESCE(Relacion."NombreCompletoDestino",'')		AS "NombreRepresentante"
     , COALESCE(DireccionFisicaRepresentante."Calle",'') 	AS "DireccionFisicaRepresentante"
     , COALESCE(DireccionFisicaRepresentante."Numero",'')	AS "NumeroDireccionRepresentante"     
     , COALESCE(RelacionApo."NombreCompletoDestino",'')		AS "NombreApoderado"
     , COALESCE(RelacionApo."DocumentoIdentidadDestino",'') AS "DocumentoIdentidadApoderado"
     , COALESCE(CodigoCorredora."CodigoInscripcionRegulador",'') 	AS "CodigoCorredora"
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa
INNER JOIN "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales" AS ContratoDatosGenerales
		ON ContratoDatosGenerales."IdEmpresa" = Empresa."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocioNegocio
		ON SocioNegocio."IdSocioDeNegocio" = Empresa."IdSocioDeNegocio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Persona
		ON Persona."IdPersona" = SocioNegocio."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::REG.Giro" AS Giro 
       ON Giro."IdGiro" = Persona."IdGiroPrincipal" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DireccionFisica 
	   ON DireccionFisica."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DireccionElectronica 
	   ON DireccionElectronica."IdDireccionElectronica" = Persona."IdDireccionElectronicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS Telefono 
       ON Telefono."IdTelefono" = Persona."IdTelefonoPrincipal"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais 
	   ON Pais."IdPais" = Persona."IdPaisNacionalidadPrincipal" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS Relacion
	   ON Relacion."IdPersonaDestino" = Persona."IdPersonaRepresentante"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaRepresentante
	   ON PersonaRepresentante."IdPersona" = Relacion."IdPersonaDestino"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisicaRepresentante 
	   ON DireccionFisicaRepresentante."IdDireccionFisica" = PersonaRepresentante."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS RelacionApo
	  ON RelacionApo."DocumentoIdentidad" = Persona."DocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" AS CodigoCorredora
	  ON CodigoCorredora."IdPersona" = Persona."IdPersona"
WHERE Empresa."IdEmpresa" = :IdEmpresa AND  RelacionApo."IdTipoRelacion" = 'APO' AND RelacionApo."IdEstado" = 'ACT' ;
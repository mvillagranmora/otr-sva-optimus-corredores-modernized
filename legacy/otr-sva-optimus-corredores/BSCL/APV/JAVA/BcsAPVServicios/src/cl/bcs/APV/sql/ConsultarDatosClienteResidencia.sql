SELECT "IdCiudad","IdPais","IdDivisionPais","NombreCiudad","NombrePais","PaisNombreDivisionPais" FROM "BSCL.SocioNegocio.BD::PER.Persona" AS Persona 
INNER JOIN
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS Identidad
ON  Persona."IdPersona" = Identidad."IdPersona"
AND Identidad."Predeterminado" = 'S'
INNER JOIN
	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DatosResidencia
ON Persona."IdDireccionFisicaPrincipal" = DatosResidencia."IdDireccionFisica"
WHERE "DocumentoIdentidad" = :DocumentoIdentidad
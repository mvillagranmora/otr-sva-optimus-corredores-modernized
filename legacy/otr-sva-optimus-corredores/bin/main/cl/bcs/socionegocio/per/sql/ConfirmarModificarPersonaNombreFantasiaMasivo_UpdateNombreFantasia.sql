UPDATE NOMF
SET 
	  NOMF."DescripcionNombreFantasia" = COALESCE(:DescripcionNombreFantasia, NOMF."DescripcionNombreFantasia")
	, NOMF."IdEstado" = COALESCE(:IdEstado, NOMF."IdEstado")
	, NOMF."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" NOMF
WHERE NOMF."IdNombreFantasia" = :IdNombreFantasia;
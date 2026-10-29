SELECT 
	"IdPortafolio"
	,"CodigoPortafolio"
	,"DocumentoIdentidad"
	,"NombreCompleto"
FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluyeLimpiezaLimiteLineaOperacional"
WHERE "IdPortafolio" IN (:idsPortafolio);
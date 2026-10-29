SELECT 
	"DocumentoIdentidad",
	"IdPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "DocumentoIdentidad"= :ExistePersona;
--AND "IdEmpresa" = p_IdEmpresa;
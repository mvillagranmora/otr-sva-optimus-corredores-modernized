SELECT Count("DocumentoIdentidad")
	FROM  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "DocumentoIdentidad"= :ExistePersona;
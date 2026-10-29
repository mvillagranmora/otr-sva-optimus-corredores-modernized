SELECT 
	CAE."IdAutorizadoEtapa",
	CAE."IdEtapa",
	COALESCE(CAE."IdUsuario", 0) AS "IdUsuario",
	COALESCE(CAE."IdRol", 0) AS "IdRol",
	CAE."IdUsuarioReemplaza",
	CAE."ReemplazaDesde",
	CAE."ReemplazaHasta",
	CAE."IdEstado"
FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa" AS CAE
WHERE CAE."IdEtapa" IN (:ListaIdEtapa)
AND  CAE."IdEstado" = 'VIG';
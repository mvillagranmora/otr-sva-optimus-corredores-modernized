SELECT
	 SPT."IdUnicoDatatec"
	,SPT."NumeroRegistro"
	,SPT."NumeroIntentos"
	--,SPT."IdCliente"
	,COALESCE(CLI."IdCliente",0) AS "IdClienteValidacion"
	,CASE WHEN CLI."IdEstado" = 'ACT' THEN CASE CLI."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END
		  ELSE 'BLO' END AS "IdEstadoCliente"
	--,SPT."IdPortafolio"
	,COALESCE(POR."IdPortafolio",0) AS "IdPortafolioValidacion"
	,CASE WHEN POR."IdEstado" IN ('ACT','VIG') THEN CASE WHEN POR."IdEstadoAutorizacion" IN ('PEN','') THEN 'PEN' ELSE POR."IdEstado" END
		  ELSE 'BLO' END AS "IdEstadoPortafolio"
	,SPT."FechaOperacion"
	,TO_CHAR(SPT."FechaOperacion",'HH24:MI:SS') AS "HoraOperacion"
	,SPT."FechaReproceso"
	,COALESCE(SPT."IdEstadoRegistro",'') AS "IdEstadoRegistro"
	--,COALESCE(SPT."GlosaProceso",'') AS "GlosaProceso"
	--,COALESCE(SPT."InstitucionValidacion",'') AS "InstitucionValidacion"
	--,COALESCE(SPT."UsuarioValidacion",'') AS "UsuarioValidacion"
	,SPT."IdUsuarioCreador"
	,COALESCE(USU."IdUsuario",0) AS "IdUsuarioValidacion"
	,SPT."ClaseMensaje"
FROM "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" SPT
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (CLI."IdCliente" = SPT."IdCliente" AND
		CLI."Borrado" = 'N')
--LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
	ON (POR."IdPortafolio" = SPT."IdPortafolio")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
	ON (SPT."IdUsuarioCreador" = USU."IdUsuario")
WHERE SPT."IdUnicoDatatec" IN (:IdUnicoDatatec)
  --AND SPT."NumeroRegistro" IN (:NumeroRegistro)
  AND SPT."IdEmpresa" = :IdEmpresa
  AND SPT."ClaseMensaje" = '5';

SELECT 
	pernom."NombreCompleto",
	port."IdPortafolio",
	port."CodigoPortafolio",
	cli."IdCliente",
	cliestado."IdEstado" AS "IdEstadoCliente",
	port."IdEstado" AS "IdEstadoPortafolio",
	ident."DocumentoIdentidad",
	EMP."IdEmpleado" AS "IdEmpleadoEjecutivo",
	ident."IdTipoDocumentoIdentidad" as "TipoDocumentoIdentidad",
	port."Proposito",
	cli."IdPersona",
	CASE WHEN CURRENT_UTCTIMESTAMP <= ADD_SECONDS(cli."FechaCreacion", COALESCE(TO_BIGINT(OPECIN."Valor"),0)*3600) AND cliestado."IdEstado" != 'ACT' THEN 'S' ELSE 'N' END AS "EsClienteNuevoProspecto",
	COALESCE(INDCUMP."ValorIndicador",'N') AS "BloqueadoPorCumplimiento"
	
FROM "BSCL.Portafolio.BD::POR.Portafolio" port
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on port."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado" cliestado on cliestado."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON PER."IdPersona" = cli."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON CLI."IdPersonaEjecutivo" = EMP."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND IDENT."Predeterminado" ='S'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" pernom on pernom."IdPersona" = per."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS OPECIN ON OPECIN."CodigoParametro" = 'PLA.HorasOperaCINClienteNuevoBloqueado'
LEFT JOIN 
  ( 
  	SELECT IND."IdEntidad", IND."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON  (IND."IdTipoIndicador" = TIND."IdTipoIndicador" AND IND."IdTipoEntidad" = TIND."IdTipoEntidad")
	WHERE TIND."IdTipoEntidad" 		= 'CLIENTE' 
	AND TIND."CodigoTipoIndicador" 	= 'BCCUM' -- Bloqueo por cumplimiento
	AND TIND."IdEstado" 			= 'ACT'
  ) AS INDCUMP ON (INDCUMP."IdEntidad" = cli."IdCliente")
  
WHERE port."IdPortafolio" IN (:IdPortafolio);

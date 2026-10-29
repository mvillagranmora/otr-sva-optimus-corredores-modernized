SELECT datos."IdEmpresa",
	datos."DocumentoIdentidad",
	datos."Nombre",
	datos."Entidad",
	datos."DescripcionEntidad",
	datos."Notificacion",
	datos."EstadoNotificacion",
	datos."FechaActivacion"
FROM
	(SELECT 
	    CC."IdEmpresa",
	    CC."DocumentoIdentidad",
	    CC."NombreCompleto" AS "Nombre",
	    'CLIENTE' AS "Entidad",
	    'FICHA SOCIO NEGOCIO' AS "DescripcionEntidad",
	    CE."Descripcion" AS "Notificacion",
	    CASE WHEN COALESCE(PE."IdEstado",'') = '' THEN 'ACTIVO' ELSE E."Nombre" END AS "EstadoNotificacion",
	    COALESCE(PE."FechaCreacion", '0001-01-01') AS "FechaActivacion",
	    CC."IdCliente",
	    CASE WHEN COALESCE(PE."IdEstado",'') = '' THEN 'ACT' ELSE PE."IdEstado" END AS "IdEstadoNotificacion"
	FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE
		INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" CTEAC ON CE."IdConfiguracionEvento" = CTEAC."IdConfiguracionEvento"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" IN (SELECT "IdCliente" FROM "BSCL.SocioNegocio.BD::CLI.Cliente")
		LEFT JOIN "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" PE ON PE."IdConfiguracionEvento" = CE."IdConfiguracionEvento" AND PE."TipoEntidad" = 'CLI' AND PE."IdEntidad" = CC."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" E ON E."IdEstado" = PE."IdEstado"
	WHERE CTEAC."DirEvt" = 'EMPCLI'
		AND CC."IdEmpresa" = :p_IdEmpresa
	UNION ALL
	SELECT
	    CC."IdEmpresa",
	    CC."DocumentoIdentidad",
	    CC."NombreCompleto" AS "Nombre",
	    'PORTAFOLIO' AS "Entidad",
	    CP."CodigoPropositoPortafolio" AS "DescripcionEntidad",
	    CE."Descripcion" AS "Notificacion",
	    CASE WHEN COALESCE(PE."IdEstado",'') = '' THEN 'ACTIVO' ELSE E."Nombre" END AS "EstadoNotificacion",
	    COALESCE(PE."FechaCreacion", '0001-01-01') AS "FechaActivacion",
	    CC."IdCliente",
	    CASE WHEN COALESCE(PE."IdEstado",'') = '' THEN 'ACT' ELSE PE."IdEstado" END AS "IdEstadoNotificacion"
	FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE
		INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" CTEAC ON CE."IdConfiguracionEvento" = CTEAC."IdConfiguracionEvento"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" IN (SELECT "IdCliente" FROM "BSCL.SocioNegocio.BD::CLI.Cliente")
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP ON CP."IdCliente" = CC."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" PE ON PE."IdConfiguracionEvento" = CE."IdConfiguracionEvento" AND PE."TipoEntidad" = 'POR' AND PE."IdEntidad" = CP."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" E ON E."IdEstado" = PE."IdEstado"
	WHERE CTEAC."DirEvt" = 'EMPPOR'
		AND CC."IdEmpresa" = :p_IdEmpresa) datos
WHERE datos."IdCliente" IN(:p_listaClientes)
	AND (datos."IdEstadoNotificacion" = :p_IdEstadoNotificacion OR 1 = :p_ValorPaso)


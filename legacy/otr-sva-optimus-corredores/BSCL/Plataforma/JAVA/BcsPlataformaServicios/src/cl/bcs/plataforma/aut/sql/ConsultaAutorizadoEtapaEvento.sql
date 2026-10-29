SELECT  
	ATE."IdAutorizadoEtapaEvento",
	EV."IdEtapa",
	ATE."IdEtapaEvento",
	ATE."IdUsuario",
	ATE."IdRol",
	ATE."IdUsuarioReemplaza",
	ATE."ReemplazaDesde",
	ATE."ReemplazaHasta",
	ATE."Observaciones",
	ATE."FechaHoraCambioEstado",
	ATE."IdEstado"
FROM "BSCL.Plataforma.BD::AUT.EtapaEvento" as EV 
INNER JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" as ATE 
	ON ATE."IdEtapaEvento" =  EV."IdEtapaEvento" 
	AND  EV."IdEvento" IN (:ListaIdEventos);

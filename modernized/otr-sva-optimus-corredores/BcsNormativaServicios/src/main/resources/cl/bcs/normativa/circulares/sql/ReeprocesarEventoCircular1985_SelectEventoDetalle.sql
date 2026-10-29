SELECT EVT."IdEvento",EVT."Procesado",COALESCE(EVTDIR."IdDireccionElectronica",0) AS "IdDireccionElectronica",CLI."IdCliente"
FROM "BSCL.Plataforma.BD::EVT.Evento" AS EVT
LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" AS EVTDIR  ON EVTDIR."IdEvento" = EVT."IdEvento"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = EVT."IdPersonaDestinatario"
WHERE EVT."IdEvento" in (:p_IdEvento);
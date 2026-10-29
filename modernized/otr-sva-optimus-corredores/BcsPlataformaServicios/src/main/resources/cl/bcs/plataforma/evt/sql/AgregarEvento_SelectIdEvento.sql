
-- Obtiene los Id de los eventos creados
SELECT
	EVE."CodigoTipoEvento",
	EVE."IdEvento",
	TMP."IdSecuencia"
FROM "BSCL.Plataforma.BD::GBL.AgregarEvento_Evento" AS TMP
INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" AS EVE ON (EVE."IdEvento" = TMP."IdEvento");

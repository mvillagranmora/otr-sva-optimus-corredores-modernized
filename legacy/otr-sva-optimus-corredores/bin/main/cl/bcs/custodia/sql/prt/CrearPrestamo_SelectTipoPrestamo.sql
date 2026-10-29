SELECT 
	COALESCE(tprt."CobraPrimaAlInicio", 'S') AS "CobraPrimaAlInicio", 
	tprt."IdTipoPrestamo", 
	tprt."IdBovedaDefecto",
	ins."IdEmisor",
	tprt."TipoPrestamoSistemaExterno",
	tprt."IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD::PRT.TipoPrestamo" tprt
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON ins."IdInstrumento" = tprt."IdInstrumentoPrestamo" 
WHERE tprt."IdInstrumentoPrestamo" = :IdInstrumentoPrestamo;
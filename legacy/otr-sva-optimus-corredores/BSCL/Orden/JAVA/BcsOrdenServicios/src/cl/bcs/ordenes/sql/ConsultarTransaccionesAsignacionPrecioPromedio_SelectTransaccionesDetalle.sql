SELECT 
	  "IdTransaccion"
	, "FolioTransaccion"
	, "FechaOperacionTransaccion"
	, "IdClasificacionTipoInstrumento"
	, "AccionTipoOperacion"
	, "InstrumentoUI"
	, "IdInstrumentoUI"
	, "CantidadTransaccion"
	, "PrecioTransaccion"
	, "CantidadDisponibleTransaccion"
	, "MontoDisponibleTransaccion"
	, "NombreCondicionDeLiquidacion"
	, "MarcaNegociacion"
	, "NombreBolsa"
	, "IdMonedaInstrumento"
	, "IdTipoInstrumento"
	, "IdLibroOrdenes"
	, "IdTipoOperacion"
	, "IdMonedaTransaccion"
	, "IdBoveda"
	, "IdModeloCustodia"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"
WHERE "CantidadDisponibleTransaccion" > 0
  AND "IdEstadoTransaccion" = 'ING' AND "IdTipoInstrumento" <> 'SIM' AND "EsVentaCorta" = 'N' 
  -- OBLIGATORIOS
  AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
  AND "IdInstrumentoUI"               IN (:IdInstrumentos)
  AND "AccionTipoOperacion" = :TipoOperacion
  --PARAMETRO
  <<FILTRO_MARCAS_RESERVADO>>
  -- OPCIONALES
  <<FILTRO_MARCAS_NEGOCIACION>>
  AND ( :IdCondicionDeLiquidacion IS NULL OR "IdCondicionDeLiquidacion" = :IdCondicionDeLiquidacion )
  AND ( :FechaDesde IS NULL OR :HoraDesde IS NULL OR UTCTOLOCAL("FechaOperacionTransaccion", 'America/Santiago','platform') >= TO_TIMESTAMP(CONCAT(:FechaDesde,' '|| :HoraDesde), 'YYYY-MM-DD HH24:MI:SS') )
  AND ( :FechaHasta IS NULL OR :HoraHasta IS NULL OR UTCTOLOCAL("FechaOperacionTransaccion", 'America/Santiago','platform') <= TO_TIMESTAMP(CONCAT(:FechaHasta,' '|| :HoraHasta), 'YYYY-MM-DD HH24:MI:SS') )
  AND ( :PrecioDesde IS NULL OR "PrecioTransaccion" >= :PrecioDesde )
  AND ( :PrecioHasta IS NULL OR :PrecioHasta = 0 OR "PrecioTransaccion" <=  :PrecioHasta )
  AND ( IFNULL(:IdBolsa, '') = '' OR "IdBolsa" = :IdBolsa)
  AND ( :IdLibroOrdenes = 0 OR "IdLibroOrdenes" = :IdLibroOrdenes)
  AND (IFNULL(:IdBoveda, '') = '' OR "IdBoveda" = :IdBoveda)
  <<FILTRO_FOLIO_TRANSACCIONES>>;
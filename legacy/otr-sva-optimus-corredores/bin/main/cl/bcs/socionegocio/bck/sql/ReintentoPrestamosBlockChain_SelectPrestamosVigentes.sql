SELECT PVIG."FolioPrestamo"
		, CAST(PVIG."FechaPrestamo" AS VARCHAR(10)) AS "FechaPrestamo"
		, PVIG."NemoSubyacente"
		, PVIG."IdClasificacionTipoInstrumento"
		, TP."TipoPrestamoSistemaExterno"
		, PVIG."CodigoCondicionLiquidacion"
		, PVIG."Cantidad" 
		, PVIG."TasaPrestamo" 
		, PVIG."FechaVencimientoPrestamo"
		, PVIG."DocumentoIdentidad"
        , PVIG."FolioSistemaExterno"
        , PVIG."IdEstado"
        , PVIG."IdCliente"
        , PVIG."IdTipoPrestamo"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" PVIG
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" TP ON PVIG."IdTipoPrestamo" = TP."IdTipoPrestamo"
WHERE ( PVIG."IdEmpresa" = :p_IdEmpresa AND PVIG."FechaPrestamo" <= :p_FechaOperacion AND PVIG."FechaVencimientoPrestamo" >= :p_FechaOperacion AND PVIG."FolioSistemaExterno" = '' AND 
( ( PVIG."EGRCantidadPrestamo" = 0 AND PVIG."IdEstado" = 'VIG' AND PVIG."INGFechaOperacion" <= :p_FechaOperacion ) 
OR ( PVIG."INGFechaOperacion" >= :p_FechaOperacion AND PVIG."EGRCantidadPrestamo" <> 0 AND PVIG."EGRFechaOperacion" >= :p_FechaOperacion ) OR PVIG."IdEstado" = 'ANT' )
AND PVIG."IdClasificacionTipoInstrumentoSubyacente" <> '' )


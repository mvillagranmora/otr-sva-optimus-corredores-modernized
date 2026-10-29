SELECT 
	CTF."IdTransaccion",
    CTF."FolioTransaccion",
    CTF."FechaTransaccion",
    CTF."FechaHoraTransaccion",
    CTF."IdClasificacionTipoInstrumento",
    CTF."IdTipoInstrumento",
    CTF."NombreBolsa",
    CTF."TipoOperacion",
    CTF."IdCondicionLiquidacion",
    CTF."IdMoneda",
    CTF."NombreTipoInstrumento",
    CTF."NemotecnicoInstrumento",
    TRANS."Cantidad",
    TRANS."Precio",
    TRANS."Monto",
    CTF."CantidadAsignada",
    CTF."CantidadPorAsignar",
    CTF."CantidadEnviada",
    CTF."CantidadPorEnviar",
    CTF."CantidadFacturada",
    CTF."CantidadPorFacturar",
    CTF."MontoFacturado",
    CTF."MontoPorFacturar",
    TRANS."TipoRegistro",
    TRANS."IdEstadoRegistro"
FROM "BSCL.Facturacion.BD.Vistas::CuadraturaTransaccionFacturacion" CTF
	INNER JOIN "BSCL.Orden.BD::HIS.Transaccion" AS TRANS 
		ON TRANS."IdTransaccion" = CTF."IdTransaccion" 
WHERE CTF."FechaTransaccion" = :FechaInicio; 

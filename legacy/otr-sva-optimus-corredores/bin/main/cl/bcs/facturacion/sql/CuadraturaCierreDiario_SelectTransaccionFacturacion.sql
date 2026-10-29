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
    CTF."Cantidad",
    CTF."Precio",
    CTF."Monto",
    CTF."CantidadAsignada",
    CTF."CantidadPorAsignar",
    CTF."CantidadEnviada",
    CTF."CantidadPorEnviar",
    CTF."CantidadFacturada",
    CTF."CantidadPorFacturar",
    CTF."MontoFacturado",
    CTF."MontoPorFacturar"
FROM "BSCL.Facturacion.BD.Vistas::CuadraturaTransaccionFacturacion" CTF
	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion"	AS Trans 
		ON Trans."IdTransaccion" = CTF."IdTransaccion" 
WHERE CTF."FechaTransaccion" = :FechaInicio 
  AND Trans."IdTipoOperacion" = :IdTipoOperacion; 

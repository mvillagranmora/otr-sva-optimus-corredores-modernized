WITH
     OperacionesAPlazoPrestamosEmprestitos AS
     (
      SELECT
      CAR."IdCliente"
      ,CAR."IdPortafolio"
      ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
      --,MAP(CAR."IdTipoOperacion", 'EMPRES',-1*CAR."INGMontoATasaCompraActual", CAR."INGMontoATasaCompraActual" ) AS "Monto"
      ,CASE
      -- EMPRESTITO (Prestamista) --> Monto Premio
      WHEN (CAR."IdTipoOperacion" = 'EMPRES') THEN CAR."INGPrecioAPlazo" 
      -- PRESTAMO (Financiado) --> -1 x (PxQ + Prima)
      WHEN (CAR."IdTipoOperacion" = 'PRESTA') THEN    -1 * (CAR."INGMontoATasaCompraActual" + CAR."INGPrecioAPlazo")                              
      END  AS "Monto"
      ,:p_FechaDesde AS "FechaSaldo"       
      FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" AS CI 
      INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
      ON CI."IdPortafolio"    = CAR."IdPortafolio"
      AND CI."IdMoneda"        = CAR."IdMonedaInstrumento"
      WHERE
      "IdTipoOperacion"           IN ('PRESTA','EMPRES')
      AND CI."IdTrabajo" = :IdTrabajo
      AND CAR."INGFechaLiquidacion"   >= :p_FechaDesde
      AND CAR."IdEmpresa"             = :p_IdEmpresa
      AND CAR."IdEstado"              = 'VIG'                     
      ----AND CAR."IdTipoPrestamo"    = 'PRTVTC'   -- Prestamos Venta Corta
      )
SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
FROM OperacionesAPlazoPrestamosEmprestitos
GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";

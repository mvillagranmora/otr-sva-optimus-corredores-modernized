
-------------------------------------------------------------------------------------
-- Obtiene los datos de las formas de pago de los comprobantes a liquidar
-------------------------------------------------------------------------------------

SELECT
   	PRO."IdInterno",
   	PRO."IdComprobanteInformado",
   	PRO."IdComprobanteFormaDePagoInformado",
   	PRO."IdComprobanteFormaDePago",
   	
   
   	COALESCE(PRO."IdCuentaBancariaCorredora",0) AS "IdCuentaBancariaCorredora",
   	
   	
	PRO."IdEstadoFormaDePago",
	PRO."IdEstadoComprobante",
	PRO."TipoMedioDePago",
	PRO."LiquidacionParcialEnviaACTA",
	PRO."EstadosPosibleTipoMovimientoMedioDePago",
	PRO."AfectaSaldoEnEstado",
	PRO."IdMoneda",
	PRO."Monto",
	PRO."IdDeposito",
	PRO."IdEstadoDepositoDetalle",
	PRO."FechaLiquidacion",
	PRO."DiasRetencion",
	PRO."IdMedioDePago",
	PRO."IdCaja"
	
	,(CASE
		WHEN (CONFIG."Valor" = 'S' AND COALESCE(CAJREC."IdCaja",-1) = PRO."IdCaja") THEN 'N' -- Recaudacion APV no valida certificacion ya que es PAC
		ELSE 'S'
	END)	AS "ValidarFormaDePagoSinCertificar"

	,(CASE
		WHEN (CONFIG."Valor" = 'S' AND COALESCE(CAJREC."IdCaja",-1) = PRO."IdCaja") THEN 'N' -- Recaudacion APV no valida depositos para cheques ya que es PAC
		ELSE 'S'
	END) AS "ValidarFormaDePagoSinDepositar"
	 

	,CFP."NumeroCuentaBancariaCorredora"
	,CFP."IdTipoMovimiento"
	,CFP."IdModalidadDePago"	
	,COALESCE(CFP."IdCuentaBancariaSocio",0) AS "IdCuentaBancariaSocio"
	,CFP."NumeroCuentaBancariaSocio"
	

   	,COALESCE(CFP."IdBancoSocio",0) AS "IdBancoSocio"
   	
   	,COM."TipoComprobante"    -- Indica si es un comprobante normal o una partida de ingreso, egreso
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago" AS PRO

LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = PRO."IdComprobanteFormaDePago")
LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = CFP."IdComprobante")

LEFT JOIN (    -- Obtiene Caja destinada para recaudación APV
			 SELECT "IdCaja" 
			 FROM "BSCL.Tesoreria.BD::TES.Caja" 
			 WHERE "Codigo"  	= 'PAC' 
			 AND   "IdEstado" 	= 'ABT' 
			 AND   "IdEmpresa" 	= :IdEmpresa
  		  )	 AS CAJREC ON (1=1)
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIG ON (CONFIG."CodigoParametro" = 'APV.Habilitado');





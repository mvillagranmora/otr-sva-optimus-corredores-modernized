SELECT
	 CAST(:v_CodigoGrupoCamara AS BIGINT) AS "CodigoGrupoCamara"
	,:v_NombreGrupoCamara AS "NombreGrupoCamara"						
	,'RV' AS "IdClasificacionTipoInstrumento" -- dividendos deben quedar con camara y mercado RV
	,"IdEventoCapitalCliente"
	,"IdEventoCapital"
	,"IdTipoEventoCapital"
	,"TipoEvento"
	,"NumeroDividendo"
	,"Secuencia"
	,"IdTipoInstrumento"   
	,"Nemo"
	,"IdMoneda"
	,"IdMoneda" AS "IdMonedaTransaccion"
	,"NombreCliente" AS "RazonSocialContraparte"
	,"CantidadDerecho"
	,"Total"
	,(CASE WHEN "TipoOperacion" = 'V' THEN ABS("Total") ELSE 0 END) AS "TotalVenta"    -- lo deja positivo ya que en tesorería siempre es positivo 
	,(CASE WHEN "TipoOperacion" = 'C' THEN ABS("Total") ELSE 0 END) AS "TotalCompra"
	,"IdBoveda"							
	,"IdComprobanteTesoreria"
	,"IdBolsa"
	,"DocumentoIdentidad" AS "RutContraparte"
	,"IdCorredorContraparte"
	,"TipoOperacion"   -- C/V
	,"MontoRetencion"
FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" 
WHERE  "AfectoaClearing"   = 'S'
AND    "ProcesadoClearing" = 'N'
AND    "FechaPago"         = :p_FechaLiquidacion;
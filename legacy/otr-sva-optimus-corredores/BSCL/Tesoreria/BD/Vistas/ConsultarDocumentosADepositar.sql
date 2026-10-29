SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarDocumentosADepositar";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarDocumentosADepositar" AS
SELECT  CFP.*
	   ,TM."Nombre" AS "NombreTipoMovimiento"
	   ,C."FechaLiquidacion" AS "FechaLiquidacionComprobante"
	   ,CAST(ADD_DAYS (TO_DATE (C."FechaLiquidacion", 'YYYY-MM-DD'), CFP."DiasRetencion") AS DATE) AS "FechaLiquidacion"	 
	   ,ADD_DAYS (TO_DATE (C."FechaLiquidacion", 'YYYY-MM-DD'), CFP."DiasRetencion")  AS "FechaHoraLiquidacion"	 
	   ,BC."IdBanco"     AS "IdBancoCorredora"
	   ,BC."NombreBanco" AS "NombreBancoCorredora"
	   ,C."NombrePersona"
	   ,C."NombrePersonaTitularPago"
	   ,BS."NombreBanco" AS "NombreBancoSocio"
	   ,CASE WHEN instr(TMMP."EstadosPosibleTipoMovimientoMedioDePago",'NDP')> 0 THEN 'S' ELSE 'N' END AS "DocumentoEnviaADeposito"
  FROM (
  		SELECT  "IdComprobanteFormaDePago"
				,"IdComprobante"
				,"IdTipoMovimiento"
				,"IdMedioDePago"
				,"NombreMedioDePago"	 
				,"IdMoneda"
				,"Monto"
				,"DiasRetencion"
				,"IdCuentaBancariaCorredora"
				,"IdTipoCuentaBancariaCorredora"	 
			 	,"NombreTipoCuentaBancariaCorredora"
			 	,"NombreCuentaBancariaCorredora"
			 	,"NumeroCuentaBancariaCorredora"
			 	,"IdCuentaBancariaSocio"
				,"IdTipoCuentaBancariaSocio"
				,"NombreTipoCuentaBancariaSocio"	 
				,"IdBancoSocio"
				,"NombreCuentaBancariaSocio"
				,"NumeroCuentaBancariaSocio"
				,"IdEstado"
				,"Depositado"
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
		 WHERE "IdEstado" = 'CRT'
		 UNION ALL
		 SELECT  CFP."IdComprobanteFormaDePago"
				,CFP."IdComprobante"
				,CFP."IdTipoMovimiento"
				,CFP."IdMedioDePago"
				,CFP."NombreMedioDePago"	 
				,CFP."IdMoneda"
				,CFP."Monto"
				,CFP."DiasRetencion"
				,CFP."IdCuentaBancariaCorredora"
				,CFP."IdTipoCuentaBancariaCorredora"	 
			 	,CFP."NombreTipoCuentaBancariaCorredora"
			 	,CFP."NombreCuentaBancariaCorredora"
			 	,CFP."NumeroCuentaBancariaCorredora"
			 	,CFP."IdCuentaBancariaSocio"
				,CFP."IdTipoCuentaBancariaSocio"
				,CFP."NombreTipoCuentaBancariaSocio"	 
				,CFP."IdBancoSocio"
				,CFP."NombreCuentaBancariaSocio"
				,CFP."NumeroCuentaBancariaSocio"
				,CFP."IdEstado"
				,CFP."Depositado"
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
		 WHERE CFP."IdTipoMovimiento" = 'MOVING' AND CFP."IdMedioDePago" = 'EFE' AND CFP."IdEstado" = 'LIQ' AND COALESCE(CFP."IdDeposito",0) = 0 AND COALESCE("Depositado",'N') = 'N'
  	   ) AS CFP
 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS C ON (C."IdComprobante" = CFP."IdComprobante")
 INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TM ON (TM."IdModulo" = 'TES' and  TM."IdTipoMovimiento" = CFP."IdTipoMovimiento") 
 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP ON (TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento" AND TMMP."IdMedioDePago" = CFP."IdMedioDePago" )
  LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBC ON (CBC."IdCuentaBanco" = CFP."IdCuentaBancariaCorredora")
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BC ON (BC."IdBanco" = CBC."IdBanco")
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BS ON (BS."IdBanco" = CFP."IdBancoSocio")   
	
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarDocumentosADepositar" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

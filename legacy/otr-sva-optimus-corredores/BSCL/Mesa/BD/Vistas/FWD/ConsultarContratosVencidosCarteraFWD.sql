SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarContratosVencidosCarteraFWD";
GO

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarContratosVencidosCarteraFWD" AS 

SELECT CMVF."IdOperacion" 			
	 , CMVF."TipoOperacion" 		
	 , CMVF."FechaOperacion"		
	 , CMVF."FechaVencimiento" 		
	 , CMVF."FechaLiquidacion" 		
	 , CMVF."MontoMonedaPrincipal" 	
	 , CMVF."IdMonedaPrincipal" 	
	 , CMVF."TipoCambioCierre" 		
	 , CART."IdPortafolio"			
	 , COALESCE(PTCDO."TipoCambioMercado",0) AS  "DolarObs"
	 , CASE WHEN CMVF."TipoOperacion" = 'COMPRA FORWARD' THEN (PTCDO."TipoCambioMercado" * CMVF."MontoMonedaPrincipal") - (CMVF."MontoMonedaPrincipal" * CMVF."TipoCambioCierre") 
		    ELSE (CMVF."MontoMonedaPrincipal" * CMVF."TipoCambioCierre") - (CMVF."MontoMonedaPrincipal" * PTCDO."TipoCambioMercado") END AS "ResultadoRealizado"
	 FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosVencidosFWD" AS CMVF  
	 LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PTCDO 
    		ON PTCDO."Origen" =  'OBSERVADO' 
   			AND PTCDO."Fecha"  = CMVF."FechaOperacion" 
   			AND PTCDO."IdMoneda"= 'DO'
   	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera"  AS CART
   			ON CART."IdCartera" = CMVF."IdCartera"
GO
		
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarContratosVencidosCarteraFWD" TO BCS_DESARROLLADOR WITH GRANT OPTION;   			
SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarContratosVigentesFWD";
GO

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarContratosVigentesFWD" AS

SELECT 
       CAR."IdPortafolio",
       CAR."FolioCartera",       
       CASE 
         WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN 'C' 
       ELSE 'V' 
       END AS "TipoOperacion",       
       CAR."FechaOperacion",
       CAR."FechaVencimientoOperacion",       
       CASE
		 WHEN CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaPrincipal" 
	   ELSE CAR."INGMontoMonedaPrincipal" 
	   END AS "MontoMonedaPrincipal",
	   CAR."IdMonedaPrincipal" || '/' || CAR."IdMonedaSecundaria" AS "Moneda",
       CASE 
         WHEN CAR."Nemotecnico" = 'ARBITR' THEN CAR."ValorParidadCierre" 
       ELSE CAR."ValorTipoCambioCierre"  
       END AS "TipoCambioFWD",
       COALESCE(PTCDO."TipoCambioMercado",0) AS "DO",
              
       CASE 
         WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN (((COALESCE(PTCDO."TipoCambioMercado", 0) * CAR."EGRMontoMonedaPrincipal")) - (CAR."EGRMontoMonedaPrincipal" * (CASE
                                                                                                                                                                     WHEN CAR."Nemotecnico" = 'ARBITR' THEN CAR."ValorParidadCierre"
                                                                                                                                                                   ELSE CAR."ValorTipoCambioCierre"
                                                                                                                                                                   END)))                                                                                    
       ELSE ((CAR."INGMontoMonedaPrincipal" * (CASE
                                                WHEN CAR."Nemotecnico" = 'ARBITR' THEN CAR."ValorParidadCierre"
                                              ELSE CAR."ValorTipoCambioCierre"
                                              END)) - (CAR."INGMontoMonedaPrincipal" * (COALESCE(PTCDO."TipoCambioMercado", 0)))) 
       END AS "ResultadoDev"        
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
  LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO 
    ON PTCDO."Origen" =  'OBSERVADO' 
   AND PTCDO."Fecha"  = CAR."FechaOperacion" 
   AND PTCDO."IdMoneda"= 'DO'
WHERE CAR."IdTipoProducto" = 'MEFW';
GO

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarContratosVigentesFWD" TO BCS_DESARROLLADOR WITH GRANT OPTION;    
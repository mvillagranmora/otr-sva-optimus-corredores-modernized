
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso"
(
	 "NumeroReferencia" 		--	BIGINT
	,"IdTipoMovimiento" 		-- 	NVARCHAR(6)    -- MOVING, MOVEGR
	,"IdPersona"        		-- 	BIGINT
	,"IdMoneda"         		-- 	NVARCHAR(3)                                                                      
   	,"IdConcepto"       		--  NVARCHAR(6)
   	,"IdPersonaTitularPago" 	--  BIGINT
   	,"IdCaja" 		 			--	BIGINT
	,"IdCondicionLiquidacion" 	--  NVARCHAR(2)
	,"IdTipoProducto" 			--  NVARCHAR(4)
	,"IdTipoOperacion" 			--  NVARCHAR(6)
	,"SecuenciaAgrupacion"  	--  NVARCHAR(40)
	,"Monto"					--  DECIMAL(26,8)
	,"IdTipoCanalIngreso"       --  NVARCHAR(20)
	,"IdPortafolio"				--	BIGINT
	,"IdPortafolioTitularPago"	--	BIGINT
)
VALUES 
(
	 :NumeroReferencia
	,:IdTipoMovimiento
	,:IdPersona
	,:IdMoneda                        
   	,:IdConcepto
   	,:IdPersonaTitularPago
   	,:IdCaja
	,:IdCondicionLiquidacion
	,:IdTipoProducto
	,:IdTipoOperacion
	,:SecuenciaAgrupacion
	,:Monto
	,:IdTipoCanalIngreso
	,:IdPortafolio
	,:IdPortafolioTitularPago
);

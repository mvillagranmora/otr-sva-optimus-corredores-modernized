
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso"
(	
	 "NumeroReferencia"					--  BIGINT
	,"NumeroReferenciaFormaDePago"		--  BIGINT
	,"IdMedioDePago"          			--  NVARCHAR(3)
	,"IdModalidadDePago"      			--  NVARCHAR(6)
	,"IdMoneda"							--  NVARCHAR(3)
	,"Monto"							--	DECIMAL(26,8)
	,"IdCuentaBancariaCorredora" 		--  BIGINT
	,"IdCuentaBancariaSocio"			-- 	BIGINT
	,"IdTarjetaBancariaCorredora"   	-- 	BIGINT
	,"IdTarjetaBancariaSocio"   	  	--	BIGINT
)
VALUES 
(
	 :NumeroReferencia
	,:NumeroReferenciaFormaDePago
	,:IdMedioDePago
	,:IdModalidadDePago
	,:IdMoneda
	,:Monto
	,:IdCuentaBancariaCorredora
	,:IdCuentaBancariaSocio
	,:IdTarjetaBancariaCorredora
	,:IdTarjetaBancariaSocio
);

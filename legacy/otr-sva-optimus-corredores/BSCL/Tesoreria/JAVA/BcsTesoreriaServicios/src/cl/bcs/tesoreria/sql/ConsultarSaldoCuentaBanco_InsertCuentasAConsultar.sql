
INSERT INTO "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_CuentasAConsultar"
(		
	 "IdCuentaBanco" 		--	BIGINT
	,"IdBanco"        		-- 	BIGINT
	,"IdMoneda"         	-- 	NVARCHAR(3)
)
VALUES 
(
	 :IdCuentaBanco
	,:IdBanco
	,:IdMoneda
);

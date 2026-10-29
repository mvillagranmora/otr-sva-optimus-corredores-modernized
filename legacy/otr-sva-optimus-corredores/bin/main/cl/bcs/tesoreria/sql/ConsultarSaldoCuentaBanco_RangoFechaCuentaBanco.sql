INSERT INTO "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco" 
(
	"IdCuentaBanco",		-- BIGINT
	"FechaSaldo", 	    	-- NVARCHAR(10)
	"Orden"					-- INTEGER
)
VALUES
(
	:IdCuentaBanco,
	:FechaSaldo,
	:Orden
);
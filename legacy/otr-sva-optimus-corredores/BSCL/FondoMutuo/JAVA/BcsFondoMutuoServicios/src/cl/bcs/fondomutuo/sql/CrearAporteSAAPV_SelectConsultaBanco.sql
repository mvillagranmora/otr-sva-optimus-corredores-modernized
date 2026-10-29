SELECT 
	"IdBanco",
	"NumeroCuentaBancaria" 
from "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias"
WHERE "IdCuentaBanco" = :IdCuentaBanco;
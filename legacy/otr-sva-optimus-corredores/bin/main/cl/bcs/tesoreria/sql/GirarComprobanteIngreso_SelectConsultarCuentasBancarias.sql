SELECT 
	CCS."IdTipoCuentaBancaria", 
	CCS."NombreTipoCuentaBancaria", 
	CCS."NombreCuentaBancaria", 
	CCS."NumeroCuentaBancaria"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CCS 
WHERE CCS."IdCuentaBanco" = :p_IdCuentaBancariaCorredora;
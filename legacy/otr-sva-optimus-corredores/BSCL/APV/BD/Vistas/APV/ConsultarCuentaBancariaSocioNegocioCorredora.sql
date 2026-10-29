SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarCuentaBancariaSocioNegocioCorredora"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarCuentaBancariaSocioNegocioCorredora" AS 

SELECT 
	 PCB."IdCuentaBancaria"
	,PCB."NombreCuentaBancariaFull"
	,PCB."NombreBanco"
	,PCB."NumeroCuentaBancaria"
	,EMP."IdEmpresa"
	,PCB."IdEstado"
	,PCB."IdMoneda"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
ON CLI."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" PCB
ON CLI."IdPersona" = PCB."IdPersona";

GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarCuentaBancariaSocioNegocioCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

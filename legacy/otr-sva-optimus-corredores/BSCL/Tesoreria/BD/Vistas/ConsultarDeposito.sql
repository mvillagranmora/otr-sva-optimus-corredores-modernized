SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarDeposito";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarDeposito" AS
SELECT   D."IdDeposito"
		,D."IdEmpresa"
		,D."FechaMovimiento"
		,D."FechaIngresoRegistro"
		,D."IdTipoDeposito"
		,TD."Nombre" AS "NombreTipoDeposito"
		,D."IdTipoCuentaBanco"
		,D."IdCuentaBanco"
		,CCB."NombreCuentaBancariaFull"
		,CCB."IdBanco"
		,CCB."NombreBanco"		
		,D."IdMoneda"
		,M."Nombre" AS "NombreMoneda"
		,D."Monto"
		,D."FechaHoraLiquidacion"
		,D."IdUsuarioLiquidador"
		,D."IdUsuario"
		,D."IdEstado"
		,ET."NombreEstado"
		,D."IdConcepto" AS "IdConceptoDeposito"
		,CON."Nombre" AS "NombreConceptoDeposito"
		,D."Descripcion"
		,DD."Count" AS "Cantidad"
 FROM "BSCL.Tesoreria.BD::TES.Deposito" as D
INNER JOIN (SELECT "IdDeposito", COUNT("IdDeposito") as "Count" 
			  FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" 
			 GROUP BY "IdDeposito") as DD ON DD."IdDeposito" = D."IdDeposito"
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CCB on CCB."IdCuentaBanco" = D."IdCuentaBanco"
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" as ET ON ET."IdEstado" = D."IdEstado"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as M ON M."IdMoneda" = D."IdMoneda"
INNER JOIN "BSCL.Tesoreria.BD::TES.TipoDeposito" as TD ON TD."IdTipoDeposito" = D."IdTipoDeposito"
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as CON ON CON."IdConcepto" = D."IdConcepto" 
WHERE ET."NombreEntidad" = 'TES.Deposito'
GROUP BY D."IdDeposito",D."IdEmpresa",D."FechaMovimiento",D."FechaIngresoRegistro",D."IdTipoDeposito"
		,TD."Nombre",D."IdTipoCuentaBanco",D."IdCuentaBanco",CCB."NombreCuentaBancariaFull"
		,CCB."IdBanco",CCB."NombreBanco",D."IdMoneda",M."Nombre",D."Monto",D."FechaHoraLiquidacion"
		,D."IdUsuarioLiquidador",D."IdUsuario",D."IdEstado",ET."NombreEstado",DD."Count"
		,D."IdConcepto",CON."Nombre",D."Descripcion"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarDeposito" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

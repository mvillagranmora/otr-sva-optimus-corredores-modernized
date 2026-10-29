INSERT INTO "BSCL.FondoMutuo.BD::TRA.Recaudacion"
(
"IdRecaudacion"
, "IdEmpresa"
, "IdUsuario"
, "IdTraspaso"
, "FechaDeposito"
, "IdMedioDePago"
, "IdBanco"
, "NumeroCuentaBancaria"
, "IdCuentaBanco"
, "Sucursal"
, "MontoDeposito"
, "Observacion"
, "IdEstado"
, "FechaIngreso"
, "FechaIngresoRegistro"
)
VALUES(
	 :IdRecaudacion
	,:IdEmpresa
	,:IdUsuario
	,:IdTraspaso
	,:FechaDeposito
	,:IdMedioDePago
	,:IdBanco
	,:NumeroCuentaBancaria
	,:IdCuentaBanco
	,:Sucursal
	,:MontoDeposito
	,:Observacion
	,:IdEstado
	,:FechaIngreso
	,CURRENT_UTCTIMESTAMP

);

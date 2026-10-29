SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversion" AS 	 
SELECT	
		 cargoabonomov."IdCargoAbonoMovimiento",
		 cargoabonomov."IdTipoMovimiento",
		 cuentainversion."IdCliente",
		 cuentainversion."IdPortafolio",
		 cuentainversion."IdMoneda",
		 cargoabonomov."FechaSistema"  AS "FechaMovimiento",		 
		 cargoabonomov."FechaEfectivaMovimiento",
		 COALESCE(cargoabonomov."FechaCertificacion",'0001-01-01') AS "FechaCertificacion",
		 cargoabonomov."Monto",
		 cargoabonomov."Saldo" AS "SaldoPorFechaEfectiva",
		 cargoabonomov."SaldoPorFechaMovimiento",
		 cargoabonomov."SaldoLibroInventario",
		 cargoabonomov."FechaIngresoRegistro",
		 cargoabonomov."IdEmpresa",
		 cargoabonomov."IdModulo",
		 cargoabonomov."IdEstado"
   FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
   INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
WITH READ ONLY;	 	


GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
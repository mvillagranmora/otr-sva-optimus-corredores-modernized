SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.CuentaInversion.BD.Utilitarios::ObtenerFechaDeuda";

--  Proposito: Obtiene la fecha desde que tiene la deuda
--  Autor: - Gat-Blac 
--  Fecha Creacion: 12/05/2022
--  select "BSCL.CuentaInversion.BD.Utilitarios::ObtenerFechaDeuda" (69113,'2020-06-04') FROM DUMMY;
CREATE FUNCTION "BSCL.CuentaInversion.BD.Utilitarios::ObtenerFechaDeuda"(
	p_IdCuentaInversion			DECIMAL(26,8),
	p_Fecha						NVARCHAR(10)
) RETURNS p_out_FechaDeuda		NVARCHAR(10)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
	DECLARE v_ultimo_positivo BIGINT;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		p_out_FechaDeuda := p_Fecha;
	END;
		
	 SELECT TOP 1 "IdCargoAbonoMovimiento" INTO v_ultimo_positivo
	 FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
	 WHERE "IdCuentaInversion" = :p_IdCuentaInversion--69113 
	 AND "SaldoPorFechaMovimiento" > 0
	 AND "FechaEfectivaMovimiento" <= :p_Fecha--'2020-06-04'
	 ORDER BY "IdCargoAbonoMovimiento" DESC;
	 
	 
	 SELECT TOP 1 "FechaEfectivaMovimiento" INTO p_out_FechaDeuda
	 FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
	 WHERE "IdCuentaInversion" = :p_IdCuentaInversion --69113 
	 AND "SaldoPorFechaMovimiento" < 0
	 AND "IdCargoAbonoMovimiento" > :v_ultimo_positivo
	 AND "FechaEfectivaMovimiento" <= :p_Fecha--'2020-06-04'
	 ORDER BY "IdCargoAbonoMovimiento" ASC;
	 
	
END;

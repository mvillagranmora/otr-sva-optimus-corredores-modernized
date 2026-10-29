SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarCuadraturaSaldos";

CREATE PROCEDURE 
-- 
--  Proposito: Realizar consulta para determinar si existe diferencia entre los saldos registrados para cada cuenta inversión del cliente.
--  Autor: NRM - BCS
--  Fecha Creacion: 12/01/2016
--  
--  Tabla entrada
--  BSCL.CuentaInversion.BD::CTA.ConsultarCuadraturaSaldos_IN_CuentaInversionAConsultar
--
"BSCL.CuentaInversion.BD.Servicios::ConsultarCuadraturaSaldos" ( 
	-- Parametros entrada comunes en todos los servicios publicos
    IN p_IdEmpresa                     	BIGINT,
    IN p_IdUsuario                     	BIGINT, 
    IN p_IdModulo                      	NVARCHAR(3),        
	IN p_UsaReglasNegocio	 		   	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados. 
	IN p_FechaDesde  					NVARCHAR(10),
	IN p_FechaHasta  					NVARCHAR(10),
	IN p_ConsultaSaldoEfectivo			NVARCHAR(1)		DEFAULT 'S' --S: incluye saldo efectivo	N: incluye saldo movimiento
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN			
		v_CuentaInversion = 
		SELECT CTAINV."IdCuentaInversion",CTAINV."IdCliente",CTAINV."IdPortafolio", CTAINV."IdMoneda"	
		  FROM "BSCL.CuentaInversion.BD::CTA.ConsultarCuadraturaSaldos_IN_CuentaInversionAConsultar" AS CINV
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINV 
		 		 ON (CINV."IdPortafolio" IS NULL OR CTAINV."IdPortafolio" = CINV."IdPortafolio")
		 		AND (CINV."IdMoneda" IS NULL OR CTAINV."IdMoneda" = CINV."IdMoneda");
	
		
		v_Movmientos = 
		SELECT CAM."IdCuentaInversion",
			   CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN CAM."Saldo" ELSE CAM."SaldoPorFechaMovimiento" END "Saldo",
			   CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN CAM."FechaEfectivaMovimiento" ELSE CAM."FechaSistema" END "FechaSaldo",
			   ROW_NUMBER() OVER (PARTITION BY CAM."IdCuentaInversion" 
						 	          ORDER BY CAM."FechaEfectivaMovimiento" desc, CAM."FechaCertificacion" desc, CAM."IdCargoAbonoMovimiento" desc) as "RowSaldoEfectivo",
			   ROW_NUMBER() OVER (PARTITION BY CAM."IdCuentaInversion" 
						 	          ORDER BY CAM."FechaSistema" desc, CAM."FechaCertificacion" desc, CAM."IdCargoAbonoMovimiento" desc) as "RowSaldoMovimiento"
	      FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM
	     INNER JOIN :v_CuentaInversion AS CI ON CI."IdCuentaInversion" = CAM."IdCuentaInversion"
	     WHERE CAM."IdEstado" = 'CRT'
	       AND (CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN CAM."FechaEfectivaMovimiento" ELSE CAM."FechaSistema" END) BETWEEN :p_FechaDesde AND :p_FechaHasta;
		
		
		
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_IN_CuentaInversionAConsultar" SELECT "IdPortafolio", "IdMoneda" FROM :v_CuentaInversion;		
		CALL "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversion" (
		    p_IdEmpresa					=> :p_IdEmpresa,												 
			p_FechaDesde				=> :p_FechaDesde,
		    p_FechaHasta				=> :p_FechaHasta,
		    p_SoloSaldoNegativo			=> 'N'
		);

		
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConsultarCuadraturaSaldos_OUT_SaldoInicialFinal"
		SELECT REG."IdCuentaInversion", CI."IdCliente",CI."IdPortafolio", CI."IdMoneda",
			   REG."SaldoInicial", REG."SaldoFinalReal", REG."SaldoFinalCalculado", (REG."SaldoFinalReal" - REG."SaldoFinalCalculado") "Diferencia"
		  FROM (
				SELECT   SIF."IdCuentaInversion"
						,CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN SIF."SaldoEfectivoInicial" ELSE SIF."SaldoMovimientoInicial" END "SaldoInicial"
						,COALESCE(SF."Saldo", CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN SIF."SaldoEfectivoInicial" ELSE SIF."SaldoMovimientoInicial" END) "SaldoFinalReal"
						,CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN SIF."SaldoEfectivoFinal" ELSE SIF."SaldoMovimientoFinal" END "SaldoFinalCalculado"									  
				  FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoInicialFinal" as SIF
				  LEFT JOIN (SELECT "IdCuentaInversion","Saldo","FechaSaldo"
							   FROM :v_Movmientos
							  WHERE (CASE WHEN :p_ConsultaSaldoEfectivo = 'S' THEN "RowSaldoEfectivo" ELSE "RowSaldoMovimiento" END) = 1) as SF
						 ON SF."IdCuentaInversion" = SIF."IdCuentaInversion"
			   ) as REG
		 INNER JOIN :v_CuentaInversion as CI ON CI."IdCuentaInversion" = REG."IdCuentaInversion";
	END;
END;

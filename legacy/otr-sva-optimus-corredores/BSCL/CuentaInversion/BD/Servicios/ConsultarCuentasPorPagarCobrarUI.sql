SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarCuentasPorPagarCobrarUI";
GO

CREATE PROCEDURE   
-- 
--  Proposito: Listar las cuentas pendientes de pago o cobro filtrado por ejecutivo
--  Autor: NRM - BCS
--  Fecha Creacion: 28/02/2017
--

"BSCL.CuentaInversion.BD.Servicios::ConsultarCuentasPorPagarCobrarUI" (	
    IN 	p_IdEmpresa						BIGINT,
    IN 	p_IdUsuario						BIGINT,
    IN 	p_IdModulo						NVARCHAR(3),    
    IN 	p_IdEmpleado					BIGINT,
    IN	p_SaldoLibroInventario			DECIMAL(26,8),
    IN	p_IdEstado						NVARCHAR(3),
    IN	p_FechaEfectivaMovimiento		NVARCHAR(10),
    IN	p_IdCliente						BIGINT,
    IN	p_IdPortafolio					BIGINT,
    IN	p_IdTipoMovimiento				NVARCHAR(6)   DEFAULT '',   -- Si es vacio, consulta MOVCAR y MOVABN 
    
    OUT	p_OUT_CuentasPorPagarCobrar		TABLE(
		    								 "IdCargoAbonoMovimiento"		BIGINT
											,"IdTipoMovimiento"				NVARCHAR(6)
											,"IdConcepto"					NVARCHAR(6)
											,"NombreConcepto"				NVARCHAR(300)
											,"IdEjecutivo"					BIGINT
											,"NombreEjecutivo"				NVARCHAR(300)
											,"IdSucursal"					BIGINT
											,"IdCuentaInversion"			BIGINT
											,"TipoCliente"					NVARCHAR(25)
											,"IdCliente"					BIGINT
											,"DocumentoIdentidadCliente"	NVARCHAR(40)
											,"NombreCliente"				NVARCHAR(300)
											,"RazonSocial"					NVARCHAR(100)
											,"IdPortafolio"					BIGINT
											,"CodigoPortafolio"				NVARCHAR(100)
											,"Proposito"					NVARCHAR(100)
											,"CodigoProposito"				NVARCHAR(100)
											,"IdMoneda"						NVARCHAR(3)
											,"CodigoMoneda"					NVARCHAR(3)
											,"NombreMoneda"					NVARCHAR(300)
											,"CodigoNombreMoneda"			NVARCHAR(25)
											,"IdCondicionDeLiquidacion"		NVARCHAR(2)
											,"FechaEfectivaMovimiento"		NVARCHAR(10)
											,"Monto"						DECIMAL(26,8)
											
											,"MontoDeuda"					DECIMAL(26,8)
											
											,"SaldoLibroInventario"			DECIMAL(26,8)
											,"AntiguedadDeuda"				BIGINT
										    ,"IdEstado"						NVARCHAR(3)
										    ,"FechaDeuda"					NVARCHAR(10)
		    							) DEFAULT EMPTY
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	
	----------------------------------------------------------------
	-- Recupera los portafoios visibles al empleado	
	----------------------------------------------------------------
	CALL "BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioUI" (
		p_IdEmpresa			=> :p_IdEmpresa,
		p_IdUsuario			=> :p_IdUsuario,
		p_IdModulo			=> :p_IdModulo,
		p_IdEmpleado		=> :p_IdEmpleado,
		p_out_Portafolios	=> v_Portafolios
	);
	
	--SELECT 'v_Portafolios', * FROM :v_Portafolios;    -- Para Revisar
	
	----------------------------------------------------------------
	-- OBTIENE MOVIMIENTOS DE CUENTA INVERSION
	----------------------------------------------------------------
	v_CuentasPorPagarCobrar = 
	SELECT   CPC."IdCargoAbonoMovimiento"
			,CPC."IdTipoMovimiento"
			,CPC."IdConcepto"			
			,CPC."NombreConcepto"
			,EPOR."IdEmpleado" as "IdEjecutivo"
			,EPOR."NombreCompleto" as "NombreEjecutivo"
			,EPOR."IdSucursal"
			,CPC."IdCuentaInversion"
			,CPC."TipoCliente"
			,CPC."IdCliente"
			,CPC."DocumentoIdentidadCliente"
			,CPC."NombreCliente"
			,CPC."RazonSocial"
			,CPC."IdPortafolio"
			,CPC."CodigoPortafolio"
			,CPC."Proposito"
			,CPC."CodigoProposito"
			,CPC."IdMoneda"
			,CPC."CodigoMoneda"
			,CPC."NombreMoneda"
			,CPC."CodigoNombreMoneda"
			,CPC."IdCondicionDeLiquidacion"
			,CPC."FechaEfectivaMovimiento"
			,CPC."Monto"			
			,CPC."SaldoLibroInventario"
			,CPC."AntiguedadDeuda"
		    ,CPC."IdEstado"		    
		    ,CPC."SaldoPorFechaMovimiento"
	    	,CPC."FechaSistema"	    

	 FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" as CPC
	 INNER JOIN :v_Portafolios as PORV   ON PORV."IdPortafolio" = CPC."IdPortafolio"
	 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EPOR  ON EPOR."IdEmpleado" = PORV."IdEmpleadoEjecutivo"
	 
	 WHERE CPC."IdEstado" 			= p_IdEstado 
	 AND ( CPC."EsCargoAbonoPorTraspaso" ='N' OR CPC."IdModulo"!='CTA' )
	 AND CPC."FechaSistema"         = :p_FechaEfectivaMovimiento	 
	 AND (:p_IdCliente 		IS NULL 	OR CPC."IdCliente"    = :p_IdCliente)
	 AND (:p_IdPortafolio 	IS NULL 	OR CPC."IdPortafolio" = :p_IdPortafolio);	
	 
	 
	 
	 
	 --SELECT 'v_CuentasPorPagarCobrar', * FROM :v_CuentasPorPagarCobrar;    -- Para Revisar
	
	 
	----------------------------------------------------------------
	-- OBTIENE SALDO FINAL
	----------------------------------------------------------------
	v_SaldoFinalPorDia = 
	SELECT "IdCuentaInversion", "FechaSistema", "SaldoFinal"	
	FROM 
	(  
		SELECT 
			RANK() OVER ( PARTITION BY "IdCuentaInversion", "FechaSistema" ORDER BY "FechaSistema" DESC, "IdCargoAbonoMovimiento" DESC ) AS "Rank",
			"IdCuentaInversion", "FechaSistema", "SaldoPorFechaMovimiento" AS "SaldoFinal"			
		FROM :v_CuentasPorPagarCobrar	
	)
	WHERE "Rank" = 1;    	
	    	
	--SELECT 'v_SaldoFinalPorDia', * FROM :v_SaldoFinalPorDia;    -- Para Revisar
	 
	
	----------------------------------------------------------------
	-- PREPARA LA SALIDA
	----------------------------------------------------------------	
	p_OUT_CuentasPorPagarCobrar = 	
	SELECT   CPC."IdCargoAbonoMovimiento"
			,CPC."IdTipoMovimiento"
			,CPC."IdConcepto"
			,CPC."NombreConcepto"
			,CPC."IdEjecutivo"
			,CPC."NombreEjecutivo"
			,CPC."IdSucursal"
			,CPC."IdCuentaInversion"
			,CPC."TipoCliente"
			,CPC."IdCliente"
			,CPC."DocumentoIdentidadCliente"
			,CPC."NombreCliente"
			,CPC."RazonSocial"
			,CPC."IdPortafolio"
			,CPC."CodigoPortafolio"
			,CPC."Proposito"
			,CPC."CodigoProposito"
			,CPC."IdMoneda"
			,CPC."CodigoMoneda"
			,CPC."NombreMoneda"
			,CPC."CodigoNombreMoneda"
			,CPC."IdCondicionDeLiquidacion"
			,CPC."FechaEfectivaMovimiento"
			,CPC."Monto"				
			,SFIN."SaldoFinal" AS "MontoDeuda"			
			,CPC."SaldoLibroInventario"
			,CPC."AntiguedadDeuda"
		    ,CPC."IdEstado"
		    ,"BSCL.CuentaInversion.BD.Utilitarios::ObtenerFechaDeuda" (CPC."IdCuentaInversion",CPC."FechaSistema") AS "FechaDeuda"
	FROM :v_CuentasPorPagarCobrar AS CPC
	INNER JOIN :v_SaldoFinalPorDia AS SFIN ON (SFIN."IdCuentaInversion" = CPC."IdCuentaInversion" AND SFIN."FechaSistema" = CPC."FechaSistema")
	WHERE SFIN."SaldoFinal" <> 0  -- Si está neteado en cero no hay que pagar ni cobrar	
	AND
	 (
	   	     CPC."IdTipoMovimiento" = (CASE WHEN :p_IdTipoMovimiento <> '' THEN :p_IdTipoMovimiento ELSE 'MOVCAR' END)
	   	  OR CPC."IdTipoMovimiento" = (CASE WHEN :p_IdTipoMovimiento <> '' THEN :p_IdTipoMovimiento ELSE 'MOVABN' END)
	 )	 
	 AND
	 (
	   	     (CPC."IdTipoMovimiento" = 'MOVCAR' AND SFIN."SaldoFinal" < 0)    -- Cuentas por Cobrar, saldo debe ser negativo
	   	  OR (CPC."IdTipoMovimiento" = 'MOVABN' AND SFIN."SaldoFinal" > 0)    -- Cuentas por Pagar,  saldo debe ser positivo
	 );


	 
 	 
END;

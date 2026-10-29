SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBTEBACC"; 
--select * from bscl."BSCL.Replica.BD.Vistas::Sebra.TBTEBACC"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBTEBACC" AS

	WITH v_ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco AS 
	(
		SELECT  CB."IdCuentaBanco"
				,(SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema') AS "FechaSaldo"
				,CB."IdMoneda" AS "Moneda"
				,BAN."IdBanco"
				,BAN."NombreBanco"
				,CB."NombreCuentaBancaria" AS "NombreCuenta"
				,CB."NumeroCuentaBancaria" AS "NumeroCuenta"
		FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" as CB 	 		 	 	 		 
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = CB."IdMoneda")
		LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco"  AS BAN ON (BAN."IdBanco"  = CB."IdBanco") 
	),
	v_SaldoDisponibleDetallado AS
	(
		SELECT	COUNT(RF."FechaSaldo") over (partition by RF."FechaSaldo" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo",VCFP."FechaHoraLiquidacion",VCFP."IdComprobanteFormaDePago"  asc rows between unbounded preceding and current row) as "Id"
				,VCFP."IdComprobanteFormaDePago"
				,RF."IdCuentaBanco"
				,RF."FechaSaldo"
				,VCFP."FechaAfectaSaldo"
				,VCFP."FechaHoraAfectaSaldo"
				,COALESCE(SUM(VCFP."MontoEgreso") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponibleEgreso"
				,COALESCE(SUM(VCFP."MontoIngreso") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponibleIngreso"
				,RF."Moneda"
				,RF."IdBanco"
				,RF."NombreBanco"
				,RF."NombreCuenta"
				,RF."NumeroCuenta"
		FROM v_ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco as RF
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
			ON VCFP."IdCuentaBancariaCorredora" = RF."IdCuentaBanco" AND VCFP."FechaAfectaSaldo" = RF."FechaSaldo"	AND VCFP."AfectoSaldo" = 'S'
	),
	v_SaldoDisponibleFinalBanco AS
	(
		SELECT  SC."IdCuentaBanco"
				,SC."FechaSaldo"
				,SC."Moneda"
				,SC."IdBanco"
				,SC."NombreBanco"
				,SC."NombreCuenta"
				,SC."NumeroCuenta"
				,SC."SaldoDisponibleEgreso"
				,SC."SaldoDisponibleIngreso"
		FROM v_SaldoDisponibleDetallado AS SC
		INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM v_SaldoDisponibleDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
	   		ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id"	
	)
	SELECT
		CB."IdCuentaBanco"		 AS "IdCuentaBanco" 
		,RPAD(EMP."DocumentoIdentidad",11) AS RUT_EMPRESA
		, 'B' AS TIPO_REG
		, RPAD(
			CASE  
				WHEN CB."IdMoneda" = 'USD' THEN 'US' 
				WHEN CB."IdMoneda" = 'EUR' THEN 'EU'
				WHEN CB."IdMoneda" = 'BRL' THEN 'BR'
				WHEN CB."IdMoneda" = 'CD'  THEN 'DC'
				WHEN CB."IdMoneda" = 'CDN' THEN 'DA'
				WHEN CB."IdMoneda" = 'CHF' THEN 'CF'
				WHEN CB."IdMoneda" = 'CN'  THEN 'CN'
				WHEN CB."IdMoneda" = 'COP' THEN 'CO'
				WHEN CB."IdMoneda" = 'CS'  THEN 'CS'
				WHEN CB."IdMoneda" = 'DHK' THEN 'DH'
				WHEN CB."IdMoneda" = 'DHR' THEN 'DR'
				WHEN CB."IdMoneda" IN ('GBP','GBD') THEN 'LB'
				WHEN CB."IdMoneda" IN ('JPY','JY')  THEN 'JY'
				WHEN CB."IdMoneda" = 'PEN' THEN 'PE'
				WHEN CB."IdMoneda" = 'UF'  THEN 'UF' 
				ELSE '$' END,2) AS TIPO_MON
		, RPAD(LEFT(CB."NombreCuentaBancaria",10),10) AS ALIAS_BANCO
		, RPAD(B."CodigoBanco",4) AS COD_BANCO
		, RPAD(CB."NumeroCuentaBancaria",20) AS CTA_CTE
		, 0 AS COLUM_CAMPO_001
		, 0 AS COLUM_CAMPO_002
		, 0 AS COLUM_CAMPO_003
		, 0 AS COLUM_CAMPO_004
		, 0 AS COLUM_CAMPO_005
		, 0 AS COLUM_CAMPO_006
		, 0 AS COLUM_CAMPO_007
		, 0 AS COLUM_CAMPO_008
		, 0 AS COLUM_CAMPO_009
		, 0 AS COLUM_CAMPO_010
		, '' AS CTA_CONTAB
		, 0 AS FILA_CAMPO_001
		, 0 AS FILA_CAMPO_002
		, 0 AS FILA_CAMPO_003
		, 0 AS FILA_CAMPO_004
		, 0 AS FILA_CAMPO_005
		, 0 AS FILA_CAMPO_006
		, 0 AS FILA_CAMPO_007
		, 0 AS FILA_CAMPO_008
		, 0 AS FILA_CAMPO_009
		, 0 AS FILA_CAMPO_010
		, COALESCE(SB."SaldoDisponibleIngreso",0) AS MONTO_ACUM_ABONO
		, COALESCE(SB."SaldoDisponibleEgreso",0) AS MONTO_ACUM_CARGO
		, CAST(CB."SaldoInicial" AS DECIMAL(17,2)) AS SALDO_INI
		, '' AS TIPO_CUENTA
		, '' AS USO_CUENTA
		, 0 AS PAIS_CUENTA
		, CB."Sucursal" AS SUCURSAL
		, CB."Direccion" AS DIRECCION
		, '0' AS CIUDAD
		, '' AS GENERA_INTERESES
		, '' AS AFECTO_GMF
		, 0.0 AS CREDITO_INTRADIA
		, 0.0 AS CREDITO_CANJE
		, 0.0 AS CREDITO_TRANSFENCIA
		, 0 AS PRODUCTO
		, '' AS CTA_CENTRALIZADORA
		, '' AS CTA_CONTAB_RENDIMIE
		, 0 AS REGION
		, '' AS CODIGO_FDO
		, '' AS CODIGO_SER
		, 0.0 AS MONTO_RETENIDO
		, '' AS CALIFICACION
		, '' AS COD_SWIFT
		, '' AS COD_ABA
		, CB."CodigoIban" AS COD_IBAN
		, '' AS CTA_PEN_TRA
		, '' AS CTA_PEN_REC
		, 0.0 AS MON_PEN_REC
		, 0.0 AS MON_PEN_TRAN
		, '' AS NIT
		, CP."DocumentoIdentidad" AS RUT_BANCO
		, '' AS AFECTA_POSICION
	FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" CB
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP on CB."IdEmpresa" = EMP."IdEmpresa"
	INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" B ON B."IdBanco" = CB."IdBanco"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP ON B."IdPersona" = CP."IdPersona"
	LEFT JOIN v_SaldoDisponibleFinalBanco AS SB ON SB."IdBanco" = CB."IdBanco" AND SB."NumeroCuenta" = CB."NumeroCuentaBancaria" AND SB."Moneda" = CB."IdMoneda" AND CB."NombreCuentaBancaria" = SB."NombreCuenta"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBTEBACC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

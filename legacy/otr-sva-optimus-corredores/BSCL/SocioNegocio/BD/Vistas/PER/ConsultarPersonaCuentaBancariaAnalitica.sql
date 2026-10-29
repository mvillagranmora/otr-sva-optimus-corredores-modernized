SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentaBancariaAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentaBancariaAnalitica" 
AS 
	SELECT
		CUENTA."IdCuentaBancaria"
		,CASE WHEN CUENTA."IdBanco" = 0 THEN NULL ELSE CUENTA."IdBanco" END AS "IdBanco"
		,CASE WHEN CUENTA."IdTipoCuentaBancaria" = '' THEN NULL ELSE CUENTA."IdTipoCuentaBancaria" END AS "IdTipoCuentaBancaria"
		,CASE WHEN CUENTA."IdMoneda" = '' THEN NULL ELSE CUENTA."IdMoneda" END AS "IdMoneda"
		,CAST(CUENTA."NumeroCuentaBancaria" AS NVARCHAR(100)) AS "NumeroCuentaBancaria"
		,CASE WHEN CUENTA."IdPersona" = 0 THEN NULL ELSE CUENTA."IdPersona" END AS "IdPersona"  
		,CASE WHEN IFNULL(CUENTA."FechaIngreso",'')= '' THEN '0001-01-01 00:00:00' 
 	    		ELSE  
 	    			CASE WHEN  CUENTA."FechaIngreso" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
			 	    ELSE LEFT(TO_CHAR(ADD_SECONDS (CUENTA."FechaIngreso", CONFIGURACION."Valor")),19)
		 	    END 
 	    END AS "FechaIngreso"
		,CASE WHEN IFNULL(CUENTA."FechaCreacion",'')= '' THEN '0001-01-01 00:00:00' 
 	    	ELSE  
				CASE WHEN  CUENTA."FechaCreacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
				ELSE LEFT(TO_CHAR(ADD_SECONDS (CUENTA."FechaCreacion", CONFIGURACION."Valor")),19)
 	            END 
		END AS "FechaCreacion"
		,CASE WHEN CUENTA."IdEstado" = '' THEN NULL ELSE CUENTA."IdEstado" END AS "IdEstado"
		,CAST(TCUENTA."NombreTipoCuentaBancaria" AS NVARCHAR(100)) AS "NombreTipoCuentaBancaria"
		,CASE WHEN BANCO."IdPaisOperacion" = 0 THEN NULL ELSE BANCO."IdPaisOperacion" END as "IdPais"
		,CAST(BANCO."NombreBanco" AS NVARCHAR(100)) AS "NombreBanco"
		,CAST(BANCO."CodigoBanco" AS NVARCHAR(40)) AS "CodigoBanco"
		,CAST(PAIS."NombrePais" AS NVARCHAR(100)) AS "NombrePais"
		,CAST(CUENTA."CodigoAba" AS VARCHAR(50)) AS "CodigoAba"
		,CAST(CUENTA."OtroCodigoBancoExtranjero" AS VARCHAR(50)) AS "CodigoBancoExtranjero"
		,CAST(CUENTA."OtroCodigoBancoExtranjero" AS VARCHAR(50)) AS "CodigoBancoExt"
		,CAST(CUENTA."CodigoIban" AS VARCHAR(50)) AS "CodigoIban"
		,CAST(CUENTA."OtroCodigoCuentaExtranjero" AS VARCHAR(50)) AS "CodigoCuentaExtranjero"
		,CAST(CUENTA."OtroCodigoCuentaExtranjero" AS VARCHAR(50)) AS "CodigoCuentaExt"
		,CAST(PER."NombreCompleto" AS NVARCHAR(302)) AS "NombreCompleto"
		,CUENTA."Predeterminado" AS "Predeterminado"
		from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CUENTA
		inner join "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TCUENTA on TCUENTA."IdTipoCuentaBancaria" = CUENTA."IdTipoCuentaBancaria"
		inner join "BSCL.Plataforma.BD::BAN.Banco" as BANCO on BANCO."IdBanco" = CUENTA."IdBanco"
		inner join "BSCL.Plataforma.BD::REG.Pais" as PAIS on PAIS."IdPais" = BANCO."IdPaisOperacion"
		inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on PER."IdPersona" = CUENTA."IdPersona"
		left join "BSCL.Plataforma.BD::PLA.Configuracion" CONFIGURACION on "CodigoParametro" = 'PLA.SegundosZonaHoraria'
		where CUENTA."Borrado" = 'N'
		WITH READ ONLY; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentaBancariaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


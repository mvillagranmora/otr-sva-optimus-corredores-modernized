SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.Origen";
		  
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.Origen"(
	"ID",
	"IdEmpresa",
	"IdDocumento",
	"FuenteDatos",
	"Estado",
    "EstadoSICAM",
    "SICAM",
--camos layout   
 	"TipoRegistro",
	"RUTEntidadMCF",
	"Fecha",
	"SaldoCuentaCambioConversion",
	"SaldoMonedaOriginal",
	"MonedaSaldo",
	"Paridad",
--campos de control interno especifico
	"IdCliente",
	"IdCargoAbonoMovimiento",
	"IdTipoMovimiento",
	"FechaEfectivaMovimiento",
	"Observaciones",
	"IdCuentaInversion"
	
) AS

 		 	SELECT 	
 	 		 	0 								AS 	"ID",
 	 		 	DAT."IdEmpresa" 				AS 	"IdEmpresa",
 	 		 	0 								AS 	"IdDocumento",
 	 		 	'' 								AS 	"FuenteDatos",
			    'ACT'							AS	"Estado",
			    ''								AS	"EstadoSICAM",
			    ''								AS	"SICAM",
			    '01'							AS	"TipoRegistro",
			    CLI."DocumentoIdentidad" 		AS "RUTEntidadMCF",
				DAT."Fecha" 					AS	"Fecha",
				CASE WHEN DAT."MonedaSaldo"='USD'  THEN DAT."SaldoMonedaOriginal"
				 	 ELSE DAT."SaldoMonedaOriginal" * DAT."Paridad"  END AS	"SaldoCuentaCambioConversion",
				DAT."SaldoMonedaOriginal" 		AS	"SaldoMonedaOriginal",
				DAT."MonedaSaldo"				AS	"MonedaSaldo",
				DAT."Paridad" 					AS "Paridad",
			--campos de control interno especific
				DAT."IdCliente" 				AS	"IdCliente",
				DAT."IdCargoAbonoMovimiento"	AS	"IdCargoAbonoMovimiento",
				DAT."IdTipoMovimiento"			AS	"IdTipoMovimiento",
				DAT."FechaEfectivaMovimiento"	AS	"FechaEfectivaMovimiento",
				''								AS	"Observaciones",
				0								AS "IdCuentaInversion" -- Se deja DEFAULT 0
			FROM     
			(
			SELECT
		        sal."IdEmpresa",
		        sal."Fecha",
		        sal."MonedaSaldo",
		        sal."SaldoMonedaOriginal",
		        sal."Paridad",
		        u."IdCliente",
		        u."IdCargoAbonoMovimiento",
		        u."IdTipoMovimiento",
		        u."FechaEfectivaMovimiento"
		    		FROM (
		        -- SUMATORIA OPERACIONES DEL MES
		        SELECT
		            PRE."IdEmpresa",
		            PRE."Fecha",
		            PRE."MonedaSaldo",
		            SUM(PRE."SaldoMonedaOriginal") AS "SaldoMonedaOriginal",
		            AVG(PRE."Paridad")             AS "Paridad"
		        FROM "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.PreOrigen" PRE
		        GROUP BY
		            PRE."IdEmpresa",
		            PRE."MonedaSaldo",
		            PRE."Fecha"
		   		 )  sal
		    	INNER JOIN (
		        -- ULTIMA OPERACION REPRESENTATIVA
		        	SELECT
		            PRE."IdEmpresa",
		            PRE."Fecha",
		            PRE."MonedaSaldo",
		            PRE."IdCliente",
		            PRE."IdCargoAbonoMovimiento",
		            PRE."IdTipoMovimiento",
		            PRE."FechaEfectivaMovimiento",
		            ROW_NUMBER() OVER (
		                PARTITION BY
		                    PRE."IdEmpresa",
		                    PRE."MonedaSaldo",
		                    PRE."Fecha"
		                ORDER BY
		                    PRE."FechaEfectivaMovimiento" DESC,
		                    PRE."IdCargoAbonoMovimiento" DESC
		            ) AS rn
		        FROM "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.PreOrigen" PRE
		    ) u
		        ON u."IdEmpresa"   = sal."IdEmpresa"
		       AND u."MonedaSaldo" = sal."MonedaSaldo"
		       AND u."Fecha"       = sal."Fecha"
		       AND u.rn = 1
			) AS DAT
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
			    ON EMP."IdEmpresa" = DAT."IdEmpresa"
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
			    ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			    ON CLI."IdCliente" = SOC."IdCliente"
																								
														  					     
			    
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.Origen" TO bcs_desarrollador WITH GRANT OPTION;

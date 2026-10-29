SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Origen" (								
	"IdEmpresa"
	,"ID"
	,"Numero"
	--,"IdDocumento"
	,"Mes"									
	,"RutDepositanteInversionistaPrestamista"
	,"IdCliente"
	,"TaxID"									
	,"Pais"									
	,"DepositosInversionesPrestamos"			
	,"RescatesDevoluciones"					
	,"InteresesPercibidas"					
	,"DividendosPercibidas"					
	,"ContratosPorDiferenciaPercibidas"		
	,"OtrasRentasPercibidas"					
	,"InteresesDevengadas"					
	,"OtrasRentasDevengadas"
	,"CodPaisRenta"
	,"FechaOperacion"
	,"TipoOperacion"
) AS

SELECT 	"IdEmpresa",
		0 AS "ID",
		0 AS "Numero",
		--0 AS "IdDocumento",
		"Mes",
		"RutCliente" AS "RutDepositanteInversionistaPrestamista",
		"IdCliente",
		"TaxId" AS "TaxID",
		"CodPais" AS "Pais"	,
		ROUND(SUM("DepositosInversionesPrestamos"),0) AS "DepositosInversionesPrestamos",
		ROUND(SUM("RescatesDevoluciones"),0) AS "RescatesDevoluciones",
		0 AS "InteresesPercibidas", 
		0 AS  "DividendosPercibidas",
   		0 AS  "ContratosPorDiferenciaPercibidas",
   		ROUND(SUM("OtrasRentasPercibidas"),2) AS  "OtrasRentasPercibidas",
   		0 AS  "InteresesDevengadas",
   		0 AS  "OtrasRentasDevengadas",
   		'CL'  AS  "CodPaisRenta",
   		"FechaOperacion",
		'FFMM' AS "TipoOperacion"
FROM
 	 (
     SELECT	
		TO_BIGINT(sol."IdEmpresa") AS "IdEmpresa",
     	MONTH(sol."FechaValorCuota") AS "Mes",
   		TRIM(cli."DocumentoIdentidad")  AS "RutCliente",
   		TO_BIGINT(cli."IdCliente") AS "IdCliente",
   		LPAD(cli."DocumentoIdentidad",11,' ')  AS "TaxId",
   		--pais."CodigoPais" AS "CodPais",
   		'US' AS "CodPais",
   		sol."FechaValorCuota" AS "FechaOperacion",
   		0 AS "DepositosInversionesPrestamos",
    	CAR."EGRMonto" AS "RescatesDevoluciones",
    	 coalesce (ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0) ,0) as "OtrasRentasPercibidas"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car 
					ON (sol."IdSolicitudAporteRescate"= car."EGRIdOperacion"
					--AND car."IdModulo" = 'FMM' 
					AND car."IdClasificacionTipoInstrumento" = 'FM'
					--AND car."EGRIdConcepto" = 'RESCFM'
					AND car."IdEstado" IN ('LIQ','MOD','RPA')
					)
					
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol_org   ON (car."FolioComprobante" = sol_org."IdSolicitudAporteRescate")	 --Origen de cartera
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo ON (ser_fdo."IdSerieFondo" = sol."IdSerieFondo" )
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo ON (fdo."IdFondo" = ser_fdo."IdFondo" )
		LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS pais ON (fdo."IdPaisOrigen" = pais."IdPais" )
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo_des ON (ser_fdo_des."IdSerieFondo" = sol."IdSerieFondoDestino" )  --Destino
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo_des ON (fdo_des."IdFondo" = ser_fdo_des."IdFondo" )							--Destino

		--LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi  ON (emi."IdEmisor" = fdo."IdEmisor")								--Emisor 
		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= sol."FechaValorCuota" )  AND uf_mov."IdParametroFinanciero" = 'UF'
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli 
		ON cli."IdCliente" = sol."IdCliente" 
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por
		ON por."IdPortafolio"= sol."IdPortafolio" 
		WHERE  	sol."IdTipoMovimientoFondoMutuo" IN  ('RESNORM','RESREINV','RESCDF')
		AND 	sol."IdRegimenTributario" IN ('18Q','18T','ART107','ART108') AND por."Proposito" NOT IN ('REGAPV')
		AND 	sol."Estado" = 'CRT'
		AND 	fdo."FondoExtranjero" ='S'
		AND 	fdo."IdMoneda" !='CLP'
		
		/* AND 	(Coalesce(fdo_des."IdMoneda",'') !='USD' AND   	sol."IdTipoMovimientoFondoMutuo" IN   ('RESNORM','RESREINV','RESCDF'))
		AND  NOT EXISTS (
			SELECT 1  FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" so
			LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo_des ON (ser_fdo_des."IdSerieFondo" = so."IdSerieFondoDestino" )  --Destino
			LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo_des ON (fdo_des."IdFondo" = ser_fdo_des."IdFondo" )						   --Destino
			WHERE so."IdSolicitudAporteRescate"=sol."IdSolicitudAporteRescate"--11558115
			AND Coalesce(fdo_des."IdMoneda",'') ='USD'
	    ) */
	)
		
GROUP BY "IdEmpresa","Mes","RutCliente","IdCliente","TaxId","CodPais","FechaOperacion"

UNION ALL

-- CC               
SELECT 
	CAR."IdEmpresa",
	0 AS "ID",
	CAR."IdOperacion" AS "Numero",
	MONTH(CAR."FechaIngresoRegistro") AS "Mes",
	TRIM(ECC."DocumentoIdentidad")  AS "RutCliente",
	TO_BIGINT(CCP."IdCliente") AS "IdCliente",
	LPAD(ECC."DocumentoIdentidad",11,' ')  AS "TaxId",
	'CL' AS "CodPais",
	0 AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	ECC."MontoInteres" AS "InteresesPercibidas",
	0 AS  "DividendosPercibidas",
   	0 AS  "ContratosPorDiferenciaPercibidas",
   	0 AS  "OtrasRentasPercibidas",
   	0 AS  "InteresesDevengadas",
   	0 AS  "OtrasRentasDevengadas",
   	'CL'  AS  "CodPaisRenta",
   	ECC."FechaCierre" as "FechaOperacion",
	'Corte Cupon' AS "TipoOperacion"

FROM    "BSCL.Custodia.BD::CUS.EventoCapital"         AS ECA 
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"            AS ECC ON (ECA."IdEventoCapital" = ECC."IdEventoCapital")
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"    AS FPA ON (ECC."IdEventoCapitalCliente" = FPA."IdEventoCapitalCliente")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"        AS CAR ON (FPA."IdEventoCapitalCliente" = CAR."IdOperacion")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"     AS CCP ON (ECC."IdPortafolio" = CCP."IdPortafolio")-- Para obtener el IdCliente

WHERE ECA."IdTipoEventoCapital" = 'CC' AND ECA."IdEstado" = 'CER' AND ECC."TipoSocioNegocio" = 'CLI' AND CAR."IdConcepto"='CICCMI' 
AND CCP."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND CCP."Proposito" NOT IN ('REGAPV')

UNION ALL 

-- Sorteos de letras
SELECT 
    CAR."IdEmpresa",
	0 AS "ID",
	CAR."IdOperacion" AS "Numero",
	MONTH(CAR."FechaIngresoRegistro") AS "Mes",
	TRIM(ECC."DocumentoIdentidad")  AS "RutCliente",
	TO_BIGINT(CCP."IdCliente") AS "IdCliente",
	LPAD(ECC."DocumentoIdentidad",11,' ')  AS "TaxId",
	'CL' AS "CodPais",
	0 AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	0 AS  "DividendosPercibidas",
   	0 AS  "ContratosPorDiferenciaPercibidas",
   	ECC."MontoInteres" AS  "OtrasRentasPercibidas",
   	0 AS  "InteresesDevengadas",
   	0 AS  "OtrasRentasDevengadas",
   	'CL'  AS  "CodPaisRenta",
   	"FechaOperacion",
	'Sorteo Letras' AS "TipoOperacion"
   	
FROM    "BSCL.Custodia.BD::CUS.EventoCapital"         AS ECA 
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"            AS ECC ON (ECA."IdEventoCapital" = ECC."IdEventoCapital")
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"    AS FPA ON (ECC."IdEventoCapitalCliente" = FPA."IdEventoCapitalCliente")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"        AS CAR ON (ECC."IdEventoCapitalCliente" = CAR."IdOperacion")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"     AS CCP ON (ECC."IdPortafolio" = CCP."IdPortafolio")-- Para obtener el IdCliente

WHERE ECA."IdTipoEventoCapital" = 'SL' AND ECA."IdEstado" = 'CER' AND ECC."TipoSocioNegocio" = 'CLI' AND CAR."IdConcepto"='CIECSL' 
AND CCP."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND CCP."Proposito" NOT IN ('REGAPV')


UNION ALL 

--Depositos a plazo	Vencidos Liquidados o RPA

SELECT
	HCAR."IdEmpresa",
	0 AS "ID",
	HCAR."FolioCartera" AS "Numero",
	MONTH(HCAR."EGRFechaOperacion") AS "Mes",
	TRIM(CCP."DocumentoIdentidad")  AS "RutCliente",
	TO_BIGINT(HCAR."IdCliente") AS "IdCliente",
	LPAD(CCP ."DocumentoIdentidad",11,' ')  AS "TaxId",
	'CL' AS "CodPais",
	"MontoCompra" AS "DepositosInversionesPrestamos",
	CASE WHEN HCAR."IdEstado" IN ('VCD','LIQ','ANT','RPA') THEN HCAR."EGRValorRescate"*HCAR."ValorFactorMonedaInstrumento" ELSE 0
	END AS "RescatesDevoluciones",
	ROUND (
	CASE WHEN HCAR."IdEstado" IN ('VCD','LIQ','ANT','RPA') THEN 
		(HCAR."EGRValorRescate" / HCAR."ValorRescate") * ((HCAR."ValorRescate"*HCAR."ValorFactorMonedaInstrumento" - HCAR."MontoCompra")
		/
		CASE WHEN HCAR."PlazoEnDiasOperacion" = 0 THEN 1 ELSE HCAR."PlazoEnDiasOperacion" END )
		*CASE WHEN DAYS_BETWEEN( HCAR."FechaOperacion", HCAR."EGRFechaOperacion" )=0 THEN 
		CASE WHEN HCAR."PlazoEnDiasOperacion" =0 THEN 1 ELSE HCAR."PlazoEnDiasOperacion" END  
		ELSE DAYS_BETWEEN( HCAR."FechaOperacion", HCAR."EGRFechaOperacion" ) END -- Calculo para interes hasta el dia de un RPA
		ELSE 0
	END ,  0) AS "InteresesPercibidas",
	0 AS  "DividendosPercibidas",
    0 AS  "ContratosPorDiferenciaPercibidas",
    0 AS  "OtrasRentasPercibidas",
    0 AS "InteresesDevengadas",
    0 AS  "OtrasRentasDevengadas",
    'CL'  AS  "CodPaisRenta",
    HCAR."EGRFechaOperacion" AS "FechaOperacion",
    'DAP' AS "TipoOperacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" HCAR
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"     AS CCP ON (HCAR."IdPortafolio" = CCP."IdPortafolio")
WHERE HCAR."IdClasificacionTipoInstrumento" = 'IF' 
AND HCAR."IdTipoOperacion"  IN ('COMIIF','COMTER')
AND CCP."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') 
AND CCP."Proposito" NOT IN ('REGAPV')
and HCAR."IdEstado" NOT IN ('ANU','VIG')
AND HCAR."EGRIdTipoOperacion" IN ('VCDTIT','VENTER','VENIIF') 
AND HCAR."INGIdTipoOperacion" NOT IN ('VCDPAC') 


UNION ALL 


--Depositos a plazo	Vigentes

SELECT
	HCAR."IdEmpresa",
	0 AS "ID",
	HCAR."FolioCartera" AS "Numero",
	MONTH(
	  CASE WHEN HCAR."IdEstado" = 'VIG' THEN
	      CASE WHEN YEAR(HCAR."FechaIngresoRegistro") < YEAR(HCAR."FechaRespaldo")
	        	THEN TO_DATE(YEAR(HCAR."FechaRespaldo") || '-01-01','YYYY-MM-DD')
	        	ELSE HCAR."FechaIngresoRegistro"
	    END
 	END ) AS "Mes",
	TRIM(CCP."DocumentoIdentidad")  AS "RutCliente",
	TO_BIGINT(HCAR."IdCliente") AS "IdCliente",
	LPAD(CCP ."DocumentoIdentidad",11,' ')  AS "TaxId",
	'CL' AS "CodPais",
	HCAR."MontoCompra" AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	0 AS  "DividendosPercibidas",
   	0 AS  "ContratosPorDiferenciaPercibidas",
   	0 AS  "OtrasRentasPercibidas",
   CASE WHEN HCAR."IdEstado" = 'VIG' THEN
	(((HCAR."ValorRescate"*HCAR."ValorFactorMonedaInstrumento" - HCAR."MontoCompra")*HCAR."INGValorRescate"/HCAR."ValorRescate")
	   /
	   CASE  WHEN HCAR."PlazoEnDiasOperacion" > 0 THEN HCAR."PlazoEnDiasOperacion"
	   ELSE 1 END)
	*
	CASE 
	   WHEN YEAR(HCAR."FechaIngresoRegistro") = YEAR(HCAR."FechaRespaldo") THEN
	        DAYS_BETWEEN(
	            HCAR."FechaIngresoRegistro",
	            TO_DATE(YEAR(HCAR."FechaRespaldo") || '-12-31','YYYY-MM-DD')
	        )
	
	   WHEN YEAR(HCAR."FechaIngresoRegistro") < YEAR(HCAR."FechaRespaldo") THEN
	        DAYS_BETWEEN(
	            TO_DATE(YEAR(HCAR."FechaRespaldo") || '-01-01','YYYY-MM-DD'),
	            TO_DATE(YEAR(HCAR."FechaRespaldo") || '-12-31','YYYY-MM-DD')
	        )
	
	   ELSE 0
	END
	ELSE 0 END AS "InteresesDevengadas",
   	0 AS  "OtrasRentasDevengadas",
   	'CL'  AS  "CodPaisRenta",
   	CASE 
	  WHEN HCAR."IdEstado" = 'VIG' THEN
	       CASE WHEN YEAR(HCAR."FechaIngresoRegistro") < YEAR(HCAR."FechaRespaldo")
	         THEN TO_DATE(YEAR(HCAR."FechaRespaldo") || '-01-01','YYYY-MM-DD')
	         ELSE HCAR."FechaIngresoRegistro"
			END
	END AS "FechaOperacion",
	'DAP' AS "TipoOperacion"
FROM "BSCL.Custodia.BD::HIS.Cartera" HCAR
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"     AS CCP ON (HCAR."IdPortafolio" = CCP."IdPortafolio")

INNER JOIN
(SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" FROM  "BSCL.Custodia.BD::HIS.Cartera"
WHERE "IdClasificacionTipoInstrumento" = 'IF' AND "IdTipoOperacion"  IN ('COMIIF','COMTER') AND MONTH("FechaRespaldo")='12'
AND "IdEstado" = 'VIG'
GROUP BY YEAR("FechaRespaldo"))  FechaR ON HCAR."FechaRespaldo" = FechaR."FechaRespaldoFinal"
	
WHERE HCAR."IdClasificacionTipoInstrumento" = 'IF' AND HCAR."IdTipoOperacion"  IN ('COMIIF','COMTER') AND HCAR."FechaRespaldo"=FechaR."FechaRespaldoFinal" 
AND CCP."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND CCP."Proposito" NOT IN ('REGAPV') AND HCAR."IdEstado" IN ('VIG') 
and HCAR."FechaVencimientoOperacion" > HCAR."FechaRespaldo" -- CASOS VIGENTES CON PLAZO EN 0

UNION ALL

--Venta Corta

SELECT
	CEV."IdEmpresa",
	0 AS "ID",
	CEV."FolioSubyacente" AS "Numero",
	MONTH(CEV."FechaIngresoRegistro") AS "Mes",
	TRIM(CEV."DocumentoIdentidad")  AS "RutCliente",
	TO_BIGINT(CEV."IdCliente") AS "IdCliente",
	LPAD(CEV."DocumentoIdentidad",11,' ')  AS "TaxId",
	'CL' AS "CodPais",
	CEV."INGMontoPremio" AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	0 AS  "DividendosPercibidas",
   	0 AS  "ContratosPorDiferenciaPercibidas",
   	0 AS  "OtrasRentasPercibidas",
   	0 AS  "InteresesDevengadas",
   	0 AS  "OtrasRentasDevengadas",
   	'CL'  AS  "CodPaisRenta",
   	CEV."INGFechaOperacion" AS "FechaOperacion",
	'Venta Corta' AS "TipoOperacion"
   	--"IdCartera", 
   	--"INGMontoPremio", 
   	--"IdMonedaSubyacente"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS CEV
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"     AS CCP ON (CEV."IdPortafolio" = CCP."IdPortafolio")
WHERE
	CEV."IdEstado" = 'VIG' 
	AND CCP."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND CCP."Proposito" NOT IN ('REGAPV')
	
UNION ALL 

SELECT
	CPV."IdEmpresa",
	0 AS "ID",
	CPV."FolioSubyacente" AS "Numero",
	MONTH(CPV."FechaIngresoRegistro") AS "Mes",
	TRIM(CPV."DocumentoIdentidad")  AS "RutCliente",
	TO_BIGINT(CPV."IdCliente") AS "IdCliente",
	LPAD(CPV."DocumentoIdentidad",11,' ')  AS "TaxId",
	'CL' AS "CodPais",
	CPV."INGMontoPrima" AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	0 AS  "DividendosPercibidas",
   	0 AS  "ContratosPorDiferenciaPercibidas",
   	0 AS  "OtrasRentasPercibidas",
   	0 AS  "InteresesDevengadas",
   	0 AS  "OtrasRentasDevengadas",
   	'CL'  AS  "CodPaisRenta",
   	CPV."INGFechaOperacion" AS "FechaOperacion",
	'Prestamo Vig' AS "TipoOperacion"
	 --,"IdMonedaSubyacente"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" AS CPV
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"     AS CCP ON (CPV."IdPortafolio" = CCP."IdPortafolio")
WHERE
	CPV."IdEstado" = 'VIG' AND CPV."DocumentoIdentidad" != 'NULL'
	AND CCP."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND CCP."Proposito" NOT IN ('REGAPV')
	
UNION ALL

-- ETF internacionales

SELECT
	CAR."IdEmpresa", 
	0 AS "ID",
	ECC."Secuencia" AS "Numero",
	MONTH(CAR."FechaIngresoRegistro") AS "Mes",
	TRIM(ECC."DocumentoIdentidad") AS "RutCliente",
	TO_BIGINT(CCP."IdCliente") AS "IdCliente",
	LPAD(ECC."DocumentoIdentidad",11,' ') AS "TaxId",
	'CL' AS "CodPais",
	0 AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	CASE 
		WHEN ECC."IdMoneda" ='CLP' THEN ECC."Total" 
		ELSE ROUND(ECC."Total" * MON."Valor", 0)
	END AS "DividendosPercibidas",
	0 AS "ContratosPorDiferenciaPercibidas",
	0 AS  "OtrasRentasPercibidas",
	0 AS "InteresesDevengadas",
	0 AS "OtrasRentasDevengadas",
	'CL' AS "CodPaisRenta",
	CAR."FechaOperacion",
	'ETX' AS "TipoOperacion"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS ECA 
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS ECC ON (ECA."IdEventoCapital" = ECC."IdEventoCapital")
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago" AS FPA ON (ECC."IdEventoCapitalCliente" = FPA."IdEventoCapitalCliente")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAR ON (FPA."IdEventoCapitalCliente" = CAR."IdOperacion")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CCP ON (ECC."IdPortafolio" = CCP."IdPortafolio")-- Para obtener el IdCliente
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS MON
  ON MON."Fecha" = ECC."FechaPago"
  AND (
    (ECC."IdMoneda" = 'USD' AND MON."IdParametroFinanciero" = 'DO')
    OR
    (ECC."IdMoneda" =  MON."IdParametroFinanciero")
  )
WHERE ECA."IdTipoEventoCapital" = 'DV' 
	AND ECA."IdTipoInstrumento" = 'ETX'
	AND ECA."IdEstado" = 'CER' 
	AND ECC."TipoSocioNegocio" = 'CLI'
	AND ECC."Total" > CAST(0 AS DECIMAL(26,8))

UNION ALL

-- ACN internacionales

SELECT
	CAR."IdEmpresa",
	0 AS "ID",
	ECC."Secuencia" AS "Numero",
	MONTH(CAR."FechaIngresoRegistro") AS "Mes",
	TRIM(ECC."DocumentoIdentidad") AS "RutCliente",
	TO_BIGINT(CCP."IdCliente") AS "IdCliente",
	LPAD(ECC."DocumentoIdentidad",11,' ') AS "TaxId",
	'CL' AS "CodPais",
	0 AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	CASE 
		WHEN ECC."IdMoneda" ='CLP' THEN ECC."Total" 
		ELSE ROUND(ECC."Total" * MON."Valor", 0)
	END AS "DividendosPercibidas",
	0 AS "ContratosPorDiferenciaPercibidas",
	0 AS  "OtrasRentasPercibidas",
	0 AS "InteresesDevengadas",
	0 AS "OtrasRentasDevengadas",
	'CL' AS "CodPaisRenta",
	CAR."FechaOperacion",
	'ACX' AS "TipoOperacion"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS ECA 
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS ECC ON (ECA."IdEventoCapital" = ECC."IdEventoCapital")
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago" AS FPA ON (ECC."IdEventoCapitalCliente" = FPA."IdEventoCapitalCliente")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAR ON (FPA."IdEventoCapitalCliente" = CAR."IdOperacion")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CCP ON (ECC."IdPortafolio" = CCP."IdPortafolio")-- Para obtener el IdCliente
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS MON
  ON MON."Fecha" = ECC."FechaPago"
  AND (
    (ECC."IdMoneda" = 'USD' AND MON."IdParametroFinanciero" = 'DO')
    OR
    (ECC."IdMoneda" =  MON."IdParametroFinanciero")
  )
WHERE ECA."IdTipoEventoCapital" = 'DV' 
	AND ECA."IdTipoInstrumento" = 'ACX'
	AND ECA."IdEstado" = 'CER' 
	AND ECC."TipoSocioNegocio" = 'CLI'
	AND ECC."Total" > CAST(0 AS DECIMAL(26,8))


UNION ALL

-- CFI internacionales

SELECT
	CAR."IdEmpresa",
	0 AS "ID",
	ECC."Secuencia" AS "Numero",
	MONTH(CAR."FechaIngresoRegistro") AS "Mes",
	TRIM(ECC."DocumentoIdentidad") AS "RutCliente",
	TO_BIGINT(CCP."IdCliente") AS "IdCliente",
	LPAD(ECC."DocumentoIdentidad",11,' ') AS "TaxId",
	'CL' AS "CodPais",
	0 AS "DepositosInversionesPrestamos",
	0 AS "RescatesDevoluciones",
	0 AS "InteresesPercibidas",
	CASE 
		WHEN ECC."IdMoneda" ='CLP' THEN ECC."Total" 
		ELSE ROUND(ECC."Total" * MON."Valor", 0)
	END AS "DividendosPercibidas",
	0 AS "ContratosPorDiferenciaPercibidas",
	0 AS  "OtrasRentasPercibidas",
	0 AS "InteresesDevengadas",
	0 AS "OtrasRentasDevengadas",
	'CL' AS "CodPaisRenta",
	CAR."FechaOperacion",
	'CFX' AS "TipoOperacion"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS ECA 
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS ECC ON (ECA."IdEventoCapital" = ECC."IdEventoCapital")
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago" AS FPA ON (ECC."IdEventoCapitalCliente" = FPA."IdEventoCapitalCliente")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAR ON (FPA."IdEventoCapitalCliente" = CAR."IdOperacion")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CCP ON (ECC."IdPortafolio" = CCP."IdPortafolio")-- Para obtener el IdCliente
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS MON
  ON MON."Fecha" = ECC."FechaPago"
  AND (
    (ECC."IdMoneda" = 'USD' AND MON."IdParametroFinanciero" = 'DO')
    OR
    (ECC."IdMoneda" =  MON."IdParametroFinanciero")
  )
WHERE ECA."IdTipoEventoCapital" = 'DV' 
	AND ECA."IdTipoInstrumento" = 'CFX'
	AND ECA."IdEstado" = 'CER' 
	AND ECC."TipoSocioNegocio" = 'CLI'
	AND ECC."Total" > CAST(0 AS DECIMAL(26,8))
	

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Origen" TO bcs_desarrollador WITH GRANT OPTION;
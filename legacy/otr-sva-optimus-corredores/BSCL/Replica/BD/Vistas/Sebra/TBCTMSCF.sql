SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" AS

WITH
	cobros as (
		SELECT 
			"IdComprobante",
			SUM("COFI"+"COSI"+"COVA"+"COAI"+"COIF"+"CCOR") AS "Comision",
			SUM("DESI"+"DEFI"+"DEVA") AS "Derechos",
			SUM("COEX"+"DEEX"+"GAEX") AS "Exentos",
			SUM("GFOD"+"GFFA") AS "Gastos",
			SUM("IVA") AS "IVA"
		FROM (
			SELECT  "IdComprobante",
				--COMISIONES
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COFI' THEN "MontoCobro" END),0) AS "COFI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COSI' THEN "MontoCobro" END),0) AS "COSI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COVA' THEN "MontoCobro" END),0) AS "COVA",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COAI' THEN "MontoCobro" END),0) AS "COAI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COIF' THEN "MontoCobro" END),0) AS "COIF",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'CCOR' THEN "MontoCobro" END),0) AS "CCOR",
				--EXENTOS
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COEX' THEN "MontoCobro" END),0) AS "COEX",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DEEX' THEN "MontoCobro" END),0) AS "DEEX",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'GAEX' THEN "MontoCobro" END),0) AS "GAEX",
				--DERECHOS
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DESI' THEN "MontoCobro" END),0) AS "DESI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DEFI' THEN "MontoCobro" END),0) AS "DEFI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DEVA' THEN "MontoCobro" END),0) AS "DEVA",
				--GASTOS
				COALESCE(MAX(CASE WHEN "IdCobro" = 'GFOD' THEN "MontoCobro" END),0) AS "GFOD",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'GFFA' THEN "MontoCobro" END),0) AS "GFFA",
				--IVA
				COALESCE(MAX(CASE WHEN "IdCobro" = 'IVA' THEN "MontoCobro" END),0) AS "IVA"
			FROM (
					SELECT "IdComprobante", "IdCobro", "MontoCobro"
					FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
			     )
			GROUP BY "IdComprobante"
			)
		GROUP BY "IdComprobante"
	)
SELECT 	 
	COM."IdComprobante",
	"BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"(COM."IdTipoComprobante") AS TIPO_COMP_FAC		--SE DEBE HOMOLOGAR COM."IdTipoComprobante" --char
	,COM."FolioComprobante" AS FOLIO_COMP_FAC										--decimal
	,CAST(COM."FechaComprobante" AS VARCHAR(10)) AS FEC_ING_COMP					--char
	,coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( COM."DocumentoIdentidad" AS VARCHAR(11))), '')  AS RUT_CLI						--char
	,COALESCE(POR."CodigoPortafolio" ,'-1') AS SEC_RUT_CLI						--decimal
	,COM."IdSucursalEmpleadoEjecutivoPortafolio" AS COD_SUC							--char
	,CAST(coalesce(COD_EMP_TRUN4."CodigoEmpleado",'') AS VARCHAR) AS COD_AGENTE	--SE DEBE HOMOLOGAR COM."IdEmpleadoEjecutivoPortafolio" --char
	, 0 AS COD_CORR_CONTRA		--decimal
	,'' AS COD_GASTO_FACT		--char
	,LEFT(COM."IdEstadoComprobante",1) AS EST_COMP 				--char
	,CAST(TO_VARCHAR (COM."FechaComprobante",'YYYY-MM-DD') AS VARCHAR(10)) AS FEC_EMI_COMP		--"FechaEmision" siempre en '0001-01-01'	--char to_varchar
	,CAST(TO_VARCHAR (COM."FechaLiquidacion",'YYYY-MM-DD') AS VARCHAR(10)) AS FEC_PAGO						--char
	,IFNULL(COM."FolioComprobanteReferencia",0) AS FOLIO_COMP_ASOC							--decimal
	,IFNULL(COMREF."FolioComprobante",0) AS FOLIO_COMP_CONT		--decimal
	,coalesce (COM."IdBolsa", 'BCS') AS IND_BOLSA			--char 
	,'' AS IND_CHEQUE			--char
	,'N' AS IND_CTA_PRO			--char
	,'' AS IND_DEST				--char
	,'' AS IND_FACTURA			--char
	,CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END  AS IND_LIQUID			--chars 
	,'' AS IND_PAGO_COBRO		--char
	/*CASE WHEN COM."IdTipoProducto" = 'INSI' THEN 'TS'
		  ELSE 'NOR'
	  END AS IND_SIMUL*/
	,CAST(CASE WHEN locate(UPPER(TOPE."Nombre"), 'SIMUL') > 0 then 'TS' when locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN 'VC'
			ELSE '' END as Varchar) AS    IND_SIMUL	--SE DEBE HOMOLOGAR		--char
	
	
	/*,LEFT(COALESCE( hpr."ValorOrigen", 
				case when COM."IdTipoProducto"  = 'INSI' then 'AC'
						when COM."IdTipoProducto"  = 'INFU' then 'FU'
						when COM."IdTipoProducto"  = 'INEN' then 'EN'
						when COM."IdTipoProducto"  = 'INEX' then 'ET'
						when COM."IdTipoProducto"  = 'INRF' then 'RF'
						when COM."IdTipoProducto"  = 'MEME' then 'MO'
					 else  COM."IdTipoProducto"  END)
				
				, 2) AS "MERCADO"*/ -- TIPO :char
				
	
	,COALESCE(LEFT(CASE WHEN COM."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN COM."IdClasificacionTipoInstrumento"
            			WHEN COM."IdTipoInstrumento" IN ('OSA','SIM') THEN 'AC'
            			WHEN COM."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
            			WHEN COM."IdTipoInstrumento" = 'FWD' THEN 'FU'
            			WHEN COM."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			ELSE COALESCE( hpr."ValorOrigen", COM."IdTipoInstrumento")  END ,2),'')	 AS "MERCADO" -- CHAR(2) -- homologar
	
	
	,COALESCE(CASE 
	WHEN COM."IdClasificacionTipoInstrumento" = 'SP' AND COM."IdTipoOperacion" = 'COMSPT' THEN 0
	WHEN COM."IdClasificacionTipoInstrumento" = 'SP' AND COM."IdTipoOperacion" = 'VENSPT' THEN (ABS(COM."MontoComprobanteFinal"))
	ELSE
    	CASE 
    	WHEN TOPE."Accion" = 'COMPRA' THEN (ABS(COM."MontoComprobante"))
    	WHEN TOPE."Accion" = 'VENTA'  THEN 0
    	END 
	END,0) AS MONTO_COM						--decimal

	,COALESCE ( COB."Comision",0) AS MONTO_COMIS_CALC					--decimal

	,COALESCE ( COB."Comision",0) AS MONTO_COMISION		--decimal
	,COALESCE(COB."Derechos",0) AS MONTO_DERECHOS		--decimal
	,COALESCE(COB."Exentos",0) AS MONTO_EXENTO			--decimal
	,COALESCE(COB."Gastos",0) AS MONTO_GASTO			--decimal
	,COALESCE(COB."IVA",0) AS MONTO_IVA					--decimal
	,0 AS MONTO_PAGADO									--decimal
	,COALESCE(CASE 
    WHEN COM."IdClasificacionTipoInstrumento" = 'SP' AND COM."IdTipoOperacion" = 'COMSPT' AND COM."MontoComprobanteFinal" < 0 THEN COM."MontoComprobanteFinal" * -1
    WHEN COM."IdClasificacionTipoInstrumento" = 'SP' AND COM."IdTipoOperacion" = 'VENSPT' AND COM."MontoComprobanteFinal" > 0 THEN COM."MontoComprobanteFinal" * -1
	ELSE 
    COM."MontoComprobanteFinal"
		END,0) AS MONTO_TOTAL											--decimal
	,COALESCE(
	 CASE  
		WHEN COM."IdClasificacionTipoInstrumento" = 'SP' AND COM."IdTipoOperacion" = 'COMSPT' THEN ABS(COM."MontoComprobanteFinal")
		WHEN COM."IdClasificacionTipoInstrumento" = 'SP' AND COM."IdTipoOperacion" = 'VENSPT' THEN 0
		ELSE
		CASE 
    	WHEN TOPE."Accion" = 'COMPRA' THEN 0
    	WHEN TOPE."Accion" = 'VENTA'  THEN (ABS(COM."MontoComprobante"))
    	END 
	  END,0) AS MONTO_VEN			--decimal
	,CASE
		WHEN COM."IdTipoProducto" = 'MEME' THEN 'MDOL' 
		WHEN COM."IdTipoProducto" = 'MEFW' THEN '    ' 
		WHEN COM."IdTipoProducto" = 'INCI' THEN 'INCF'
		WHEN COM."IdTipoProducto" = 'INSI' THEN 'INTS'
		WHEN COM."IdTipoProducto" = 'CUST' AND COM."IdTipoInstrumento" = 'ACN' THEN 'INAC' 
		WHEN COM."IdTipoProducto" = 'CUST' AND COM."IdTipoInstrumento" = 'CFI' THEN 'INCF' 
		WHEN COM."IdTipoProducto" = 'EVCA' THEN '    ' 
		WHEN COM."IdTipoOperacion" = 'ANSIMV' THEN 'INTS'
		WHEN COM."IdTipoOperacion" = 'ANSIMC' THEN 'INTS'
		WHEN COM."IdTipoOperacion" = 'PRES' THEN ''
		ELSE COALESCE(COM."IdTipoProducto",'')
	END AS "PRODUCTO" -- CHAR(4)
	,'' AS IND_CONV				--char
	, 0 AS MONTO_DERECHOS_CONV	--decimal
	, 0 AS MONTO_COMISION_CONV	--decimal
	, 0 AS MONTO_GASTOS_CONV	--decimal
	, 0 AS SEC_DIST				--decimal
	,'' AS IND_PROC_LIQUID		--char
	,'' AS IND_PRECIO_PRIMA		--char
	, 0 AS PLAZO_PRIMA			--numeric
	, 0 AS PARIDAD_MORIGEN		--decimal
	, 0 AS CAPTACION_MORIGEN	--decimal
	,CAST(COM."IdMonedaLiquidacion" AS VARCHAR(3)) AS MON_MORIGEN --char
	,CASE COALESCE(COM."FolioComprobanteReferencia",0) WHEN 0 THEN '' ELSE CAST(COM."FolioComprobanteReferencia" AS CHAR(18)) END AS REFERENCIA	--CHAR(18)
	,COM."MayorMenorValor" AS MONTO_MMVALOR --decimal
	,COM."MontoRetencionGananciaCap" AS MONTO_RETENCIONGC  --decimal
	,COM."CargoGananciaCap" AS CARGO_RETENCION_IGK --decimal
	,'' as NUM_CONTRATO
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM  
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON TOPE."IdTipoOperacion" = COM."IdTipoOperacion"
LEFT JOIN cobros AS COB ON COB."IdComprobante" = COM."IdComprobante"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = COM."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON (POR."IdEmpleadoEjecutivo" = EMP."IdEmpleado")
LEFT JOIN (
	SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
		SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
	) WHERE RANK < 10
) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
												  	ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
												  	AND TIPOCOMPROBANTE."ValorOrigen" = TRIM(COM."IdTipoComprobante")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = COM."IdCondicionLiquidacion") 
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = COM."IdTipoInstrumento" AND    hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET')  )
                                                              
                                                              
LEFT JOIN ( --Se incluye casuistica de los Anticipos de Sim dejando solo la Ultima NC asociada a la factura como FOLIO_COMP_CONT como en Sebra
	SELECT "IdComprobanteReferencia", "FolioComprobante" FROM (
		SELECT RANK() OVER (PARTITION BY "IdComprobanteReferencia" ORDER BY "IdComprobante" DESC)  as RANK,"FolioComprobanteReferencia", "IdComprobanteReferencia", "IdEstadoComprobante", "FolioComprobante"
		FROM "BSCL.Facturacion.BD::FAC.Comprobante" WHERE "IdComprobanteReferencia" <> 0 AND "IdEstadoComprobante" != 'ANU'
	) WHERE RANK = 1
) AS COMREF ON COM."IdComprobante" = COMREF."IdComprobanteReferencia"
WHERE COM."FolioComprobante" != 0 AND "IdEstadoComprobante" != 'ANU'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;



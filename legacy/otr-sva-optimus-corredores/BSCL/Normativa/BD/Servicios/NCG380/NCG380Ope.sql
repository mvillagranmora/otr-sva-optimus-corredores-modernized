SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ope";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ope"
( 
    -- Parametros de Entrada
    IN p_IdSolicitudCircular BIGINT DEFAULT 0, 
    -- SALIDA
	OUT p_out_TablaNCG380Ope 			"BSCL.Normativa.BD::NOR.SALIDA_NCG380OPE"
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	AS 
BEGIN

	DECLARE v_IdNormativa NVARCHAR(15):='NCG380Operc';
	DECLARE v_Count BIGINT:=0;

    DECLARE v_FechaInicio DATE DEFAULT '0001-01-01';
    DECLARE v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_IdEmpresa INT := 0;
    DECLARE v_NroProductos INT := 0;


   	DECLARE v_IdPersonaCtaPropia						 BIGINT:= 0;
	DECLARE	v_IdClienteCtaPropia 						 BIGINT:=0;
 	DECLARE v_CodigoCtaPropia 							 NVARCHAR(3):= '';
 	DECLARE v_DocumentoIdentidadCtaPropia  				 NVARCHAR(40) DEFAULT '';
 	DECLARE v_NombreCtaPropia  						 	 NVARCHAR(200) DEFAULT '';
 	DECLARE v_Clains									 NVARCHAR(6) DEFAULT '';
 	DECLARE v_ParametroSecuencia 						 NVARCHAR(3) DEFAULT 'N';
 	DECLARE v_IdClienteCorredora						 BIGINT := 0;
 		

	--* ------------------------------------------------------------------------------	
    --* PARAMETROS DESDE LA UI EN VARIABLES
	--* ------------------------------------------------------------------------------
    SELECT
        CAST(COALESCE(MAX("IdEmpresa"),0) AS INT),
        CAST(COALESCE(MAX("FechaInicio"),'0001-01-01') AS DATE),
        CAST(MAX("FechaTermino") AS DATE)
        INTO v_IdEmpresa, v_FechaInicio, v_FechaTermino
    FROM
    (
        SELECT
            CASE WHEN "TipoParametro"='EMP' THEN "Valor" ELSE NULL END AS "IdEmpresa",
            CASE WHEN "TipoParametro"='FINI' THEN "Valor" ELSE NULL END AS "FechaInicio",
            CASE WHEN "TipoParametro"='FTER' THEN "Valor" ELSE NULL END AS "FechaTermino"
        FROM (SELECT
                "TipoParametro","Valor"
              FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
              WHERE "IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" IN ('EMP','FINI','FTER'))
    );

	v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaInicio AS "Valor" FROM DUMMY
				UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaTermino AS "Valor" FROM DUMMY;
				
	select COUNT(1) INTO v_Count from "BSCL.Normativa.BD::NOR.Configuracion" where "CodigoParametro" = 'Secuencia_Movimiento_NCG380OPE' AND "IdEmpresa" = :v_IdEmpresa;
	IF v_Count > 0 THEN
    		select "Valor" into v_ParametroSecuencia from "BSCL.Normativa.BD::NOR.Configuracion" where "CodigoParametro" = 'Secuencia_Movimiento_NCG380OPE' AND "IdEmpresa" = :v_IdEmpresa;
    		ELSE
    		v_ParametroSecuencia='N';
    END IF;
    
    --IdClienteCorredora
	SELECT SOC."IdCliente" INTO v_IdClienteCorredora FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio";
	
    
	--* ------------------------------------------------------------------------------	
	--* MODULOS A CONSULTAR
	--* ------------------------------------------------------------------------------	
	v_TablaParametroModulos =  
		SELECT	'CUS' AS "IdModulo" FROM DUMMY UNION ALL 	
		SELECT 	"Valor" AS "IdModulo" 
		FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
		WHERE	"TipoParametro" = 'CLAINS' AND "IdSolicitudCircular" = p_IdSolicitudCircular;

	
	SELECT COUNT(1) INTO v_NroProductos FROM :v_TablaParametroModulos;

	IF v_NroProductos > 1 THEN
		SELECT "Valor" INTO v_Clains FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "TipoParametro" = 'CLAINS' AND "IdSolicitudCircular" = p_IdSolicitudCircular;
	END IF;
	
	v_TablaModulo = 
		SELECT	'CUS' AS "IdModulo" FROM DUMMY UNION ALL 	
		SELECT	'MRF' AS "IdModulo" FROM :v_TablaParametroModulos WHERE "IdModulo" IN ('IF','RF') OR v_NroProductos = 1 UNION ALL 
		SELECT	'MSP' AS "IdModulo" FROM :v_TablaParametroModulos WHERE "IdModulo" = 'SP' OR v_NroProductos = 1 UNION ALL  
		SELECT	'MFW' AS "IdModulo" FROM :v_TablaParametroModulos WHERE "IdModulo" = 'DV' OR v_NroProductos = 1 UNION ALL  
		SELECT	'FMM' AS "IdModulo" FROM :v_TablaParametroModulos WHERE "IdModulo" = 'FM' OR v_NroProductos = 1 UNION ALL
		SELECT	top 1 'FAC' AS "IdModulo" FROM :v_TablaParametroModulos WHERE "IdModulo" NOT IN ('SP', 'DV') OR v_NroProductos = 1 UNION ALL
	    SELECT	'PRE' AS "IdModulo" FROM :v_TablaParametroModulos WHERE "IdModulo" = 'DV' OR v_NroProductos = 1;
	
	CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
	(
		p_IdEmpresa			=> v_IdEmpresa,
		p_IdNormativa		=> v_IdNormativa,
		p_tablaModulo		=> :v_TablaModulo,
		p_registroParametro => :v_registroParametro,
		p_nominal => 'S',
		p_out_TablaSalida	=> v_SalidaConsultarOperaciones
	);


	SELECT COUNT(1) INTO v_Count FROM :v_SalidaConsultarOperaciones;	

	IF v_Count > 0 THEN
	
	 	
 				---Valores de la cuenta propia	
			SELECT TOP 1 CLI."IdCliente", CLI."DocumentoIdentidad",COR."CodigoCorredora",  COD."NombreCorto" INTO v_IdClienteCtaPropia,v_DocumentoIdentidadCtaPropia,v_CodigoCtaPropia, v_NombreCtaPropia
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COD ON COD."IdPersona" =SOC."IdPersona"
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" COR ON COD."IdCorredora" =COR."IdCorredora"
			WHERE EMP."IdEmpresa" = v_IdEmpresa
			AND CLI."IdEstado"='ACT';
			
	
		v_HomCodigoTipoOperacion = 
			SELECT 	"Tipo", "ValorOrigen", "ValorDestino"
			FROM 	"BSCL.Normativa.BD::DOC.Homologacion" 
			WHERE	"Esquema" = 'NCG380Ope' AND "Tipo" = 'CodigoTipoOperacion';

		--* Hay una lista de Rut???
		v_TablaClientes = 
			SELECT 	"Valor", "IdSistema" 
			FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
			WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'RUT';

		SELECT COUNT(1) INTO v_Count FROM :v_TablaClientes;
		IF v_Count = 0 THEN
			v_TablaClientes = 
				SELECT	NULL AS "Valor", NULL AS "IdSistema"
				FROM	DUMMY;
		END IF;
		
    -- Tabla para filtrar por NEMO
		v_TablaNemos = 
			SELECT 	"Valor", "IdSistema" 
			FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
			WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'NEMO';

    SELECT COUNT(1) INTO v_Count FROM :v_TablaNemos;
		IF v_Count = 0 THEN
			v_TablaNemos = 
				SELECT	NULL AS "Valor", NULL AS "IdSistema"
				FROM	DUMMY;
		END IF;
	
		p_out_TablaNCG380Ope =

	
			SELECT DISTINCT
			CLI."NombreCompleto" AS "NombreCliente",
			CLI."DocumentoIdentidad" AS "RutCliente",
			POR."CodigoPortafolio" AS "CodigoPortafolio",
			CASE 
					WHEN OPE."DescripcionOperacion" IN ('COMPRA FORWARD', 'VENTA FORWARD', 'VENTA A TERMINO', 'COMPRA A TERMINO') THEN 'FRUE'
					WHEN OPE."IdTipoInstrumento" = 'SPT' THEN 'FRUE'
					ELSE 
						CASE 
							WHEN OPE."IdBolsa" <> '' THEN COALESCE(OPE."IdBolsa", 'FRUE')
							ELSE 'FRUE'
						END
			END AS "CodigoBolsa",
			CASE
				WHEN v_ParametroSecuencia = 'S' AND OPE."DescripcionOperacion" IN ('COMPRA FORWARD', 'VENTA FORWARD') THEN COALESCE(CAST(OPE."FolioComprobanteAsociado" AS BIGINT), 0)
				WHEN v_ParametroSecuencia = 'S' AND OPE."IdTipoOperacion" IN ('VESIMV','VESIMC') THEN OPE."FolioComprobanteAsociado"
				WHEN v_ParametroSecuencia = 'S' AND (TOPE."EsTransaccion" = 'S' OR OPE."IdTipoOperacion" = 'PRESTA') AND OPE."FolioTransaccion" <> '0' AND OPE."FolioTransaccion" <> '' AND OPE."FolioTransaccion" IS NOT NULL THEN COALESCE(CAST(OPE."FolioTransaccion" AS BIGINT), 0)
				WHEN OPE."FolioComprobante" = 0 THEN COALESCE(CAST(OPE."IdComprobante" AS BIGINT), 0)
				ELSE OPE."FolioComprobante" END AS "Folio",
			CASE
				WHEN OPE."DescripcionOperacion" IN ('VENTA A TERMINO','COMPRA A TERMINO', 'ANTICIPO PACTOS') THEN 
				CASE 
				    WHEN CARR."NemoSVS" IS NOT NULL THEN CARR."NemoSVS"
				    WHEN INS."Nemotecnico" IN ('PAGARE R','PAGARE NR') AND CAR."NemoSVS" IS NOT NULL THEN CAR."NemoSVS"
				    WHEN INS."Nemotecnico" IS NOT NULL THEN INS."Nemotecnico"
				    ELSE CAR."NemoSVS"
				END
				WHEN OPE."DescripcionOperacion" IN ('COMPRA INTERMEDIACION FINANCIERA', 'VENTA INTERMEDIACION FINANCIERA', 'ANULACION COMPRA INTERMEDIACION FINANCIERA') THEN ORD."NemotecnicoSVS"
				WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO') THEN CARR."NemoSVS"				
				WHEN OPE."DescripcionOperacion" IN ('COMPRA FORWARD', 'VENTA FORWARD') THEN 'FORWARD'
				ELSE 
					CASE 					
						WHEN OPE."DescripcionOperacion" LIKE '%SPOT%' THEN 'DOLAR'
						ELSE OPE."Nemotecnico" 
					END
			END AS "Instrumento",
			CASE 
				WHEN (OPE."EsVencimientoPacto" = 'S' OR OPE."DescripcionOperacion" = 'ANTICIPO PACTOS') AND OPE."AccionOperacion" = 'VENTA' THEN 'CPS'
				WHEN (OPE."EsVencimientoPacto" = 'S' OR OPE."DescripcionOperacion" = 'ANTICIPO PACTOS') AND OPE."AccionOperacion" = 'COMPRA' THEN 'VPS'
				WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN 'CCS'
				WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN 'VCS'
				WHEN OPE."IdTipoOperacion" = 'COMSIM' AND OPE."IdCondicionLiquidacion" = 'T0'  THEN 'CCS'
				WHEN OPE."IdTipoOperacion" = 'VENSIM' AND OPE."IdCondicionLiquidacion" = 'T0'  THEN 'VCS'
				WHEN OPE."IdTipoOperacion" = 'COMSIM' AND OPE."IdCondicionLiquidacion" = 'TP'  THEN 'CPS'
				WHEN OPE."IdTipoOperacion" = 'VENSIM' AND OPE."IdCondicionLiquidacion" = 'TP'  THEN 'VPS'
				WHEN OPE."IdTipoOperacion" = 'ANTPAC' AND OPE."AccionOperacion" = 'VENTA' THEN 'VPS'
				WHEN OPE."IdTipoOperacion" = 'ANTPAC' AND OPE."AccionOperacion" = 'COMPRA' THEN 'CPS'
				WHEN OPE."IdTipoOperacion" = 'VESIMC' THEN 'VE'
				WHEN OPE."IdTipoOperacion" = 'VESIMV' THEN 'CO'
				WHEN OPE."IdTipoOperacion" = 'ANSIMV' THEN 'CO'
				WHEN OPE."IdTipoOperacion" = 'ANSIMC' THEN 'VE'
				WHEN OPE."IdEstadoFactura" = 'RGN' THEN
					CASE 
						WHEN OPE."IdTipoOperacion" = 'COMTER' THEN 'RC' ELSE 'RV' 
					END
				ELSE HCTO."ValorDestino" 
			END AS "NombreTipoOperacion",
			CASE WHEN v_ParametroSecuencia = 'S' THEN 
					CASE
						WHEN OPE."DescripcionOperacion" IN ('COMPRA FORWARD','VENTA FORWARD') THEN COALESCE(CAST(OPE."FolioTransaccion" AS BIGINT), 0)
						WHEN OPE."IdTipoOperacion" IN ('VENPAC','COMPAC') AND v_IdClienteCorredora = OPE."IdCliente"	AND MES."IdEstado" <> 'LIQ'	THEN RES."SecMovCorIngreso"
						WHEN OPE."IdTipoOperacion" IN ('VENPAC','COMPAC') AND v_IdClienteCorredora <> OPE."IdCliente"	AND MES."IdEstado" <> 'LIQ'	THEN RES."SecMovCliIngreso"
						WHEN OPE."IdTipoOperacion" IN ('VENPAC','COMPAC') AND v_IdClienteCorredora = OPE."IdCliente"	AND MES."IdEstado" = 'LIQ'	THEN RES."SecMovCorLiquidacion"
						WHEN OPE."IdTipoOperacion" IN ('VENPAC','COMPAC') AND v_IdClienteCorredora <> OPE."IdCliente"	AND MES."IdEstado" = 'LIQ'	THEN RES."SecMovCliLiquidacion"
						WHEN OPE."IdTipoOperacion" IN ('ANTPAC')		  AND v_IdClienteCorredora = OPE."IdCliente"	AND MES."IdEstado" = 'ANT'	THEN RES."SecMovCorLiquidacion"
						WHEN OPE."IdTipoOperacion" IN ('ANTPAC')		  AND v_IdClienteCorredora <> OPE."IdCliente"	AND MES."IdEstado" = 'ANT'	THEN RES."SecMovCliLiquidacion"
						WHEN OPE."IdTipoOperacion" IN ('COMTER','VENTER') THEN RES."SecMovCliIngreso"
						WHEN OPE."IdTipoOperacion" IN ('PRESTA')		  THEN MOVTRAN."SecuenciaMovimiento"
					ELSE OPE."SecuenciaMovimiento" END 
				WHEN OPE."IdBolsa" = '' THEN '0'				
				ELSE OPE."IdOperacion"
			END AS "IdOperacion",
			CASE 
				WHEN v_ParametroSecuencia = 'S' AND OPE."FolioComprobante" IS NOT NULL AND OPE."FolioComprobante" <> 0 THEN CAST(OPE."FolioComprobante" AS NVARCHAR)
				WHEN OPE."DescripcionOperacion" IN ('COMPRA FORWARD', 'VENTA FORWARD', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS') THEN '0'
				WHEN OPE."IdBolsa" = '' THEN '0'
				ELSE OPE."FolioTransaccion"
			END AS "IdBolsa",
			TO_CHAR(OPE."FechaMovimiento", 'DD/MM/YYYY') AS "Fecha",
			CAST(UTCTOLOCAL(OPE."FechaMovimiento", 'America/Santiago','platform') AS Time) AS "Hora",
			CASE 
				WHEN OPE."IdTipoComprobante" IN ('NCR','NCE') AND OPE."CodigoOperadorCorredor" = '' THEN COALESCE(TRANS."CodigoOperadorCorredor", '')
				ELSE OPE."CodigoOperadorCorredor"
			END AS "NumeroOperador",
			CASE 
				WHEN OPE."IdTipoInstrumento" = 'FFM' AND PAI."CodigoPais" = 'CL' THEN ''
				ELSE INS."ISIN"
			END AS "ISIN",
			CASE
				WHEN OPE."DescripcionOperacion" IN ('VENTA A TERMINO','COMPRA A TERMINO', 'ANTICIPO PACTOS') THEN 
				CASE 
				    WHEN CARR."NemoSVS" IS NOT NULL THEN CARR."NemoSVS"
				    WHEN INS."Nemotecnico" IN ('PAGARE R','PAGARE NR') AND CAR."NemoSVS" IS NOT NULL THEN CAR."NemoSVS"
				    WHEN INS."Nemotecnico" IS NOT NULL THEN INS."Nemotecnico"
				    ELSE CAR."NemoSVS"
				END
				WHEN OPE."DescripcionOperacion" IN ('COMPRA INTERMEDIACION FINANCIERA', 'VENTA INTERMEDIACION FINANCIERA', 'ANULACION COMPRA INTERMEDIACION FINANCIERA') THEN ORD."NemotecnicoSVS"
				WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO') THEN CARR."NemoSVS"
				WHEN OPE."DescripcionOperacion" IN ('COMPRA FORWARD', 'VENTA FORWARD') THEN 'FORWARD'
				ELSE
					CASE 
						WHEN OPE."DescripcionOperacion" LIKE '%SPOT%' THEN 'DOLAR'
						ELSE INS."Nemotecnico"
					END
			END AS "Nemotecnico",			
			CASE 
				WHEN OPE."IdClasificacionTipoInstrumento" = 'RV' THEN INS."Nemotecnico"
				ELSE SDF."NemoCMF"
			END AS "NemoCMF",
			CASE  
					WHEN OPE."IdTipoOperacion" IN ('ANSIMV','ANSIMC') AND OPE."HIdTipoProducto" IN ('INAC','INSI') THEN 'ACCN'
					WHEN OPE."IdTipoOperacion" IN ('ANSIMV','ANSIMC') THEN OPE."HIdTipoProducto"
					WHEN OPE."IdTipoOperacion" IN ('VESIMV','VESIMC') THEN 
						CASE WHEN OPE."IdTipoInstrumento" ='ACN' THEN 'ACCN'
							 WHEN OPE."IdTipoInstrumento" ='CFI' THEN 'CFNA'
							 WHEN OPE."IdTipoInstrumento" ='ETN' THEN 'ETNA'
							 WHEN OPE."IdTipoInstrumento" ='CFX' THEN 'CFEX'
							 WHEN OPE."IdTipoInstrumento" ='ACX' THEN 'ACCE' 
							 WHEN OPE."IdTipoInstrumento" ='SIM' THEN 'OTRD'
						END
					WHEN OPE."IdTipoProducto" = 'INFM' AND PAI."CodigoPais" = 'CL' THEN 'CFNA'
					WHEN OPE."IdTipoProducto" = 'INFM' AND PAI."CodigoPais" != 'CL' THEN 'CFEX'
					WHEN OPE."IdTipoProducto" IN ('INAX','INMI') THEN 'ACCE'
					WHEN OPE."IdTipoProducto" IN ('INAC','INSI') THEN 'ACCN'
					WHEN OPE."IdTipoProducto" IN ('INCI','INCM','INEN') THEN 'CFNA'
					WHEN OPE."IdTipoProducto" IN ('INCX','INEX') THEN 'CFEX'
					WHEN OPE."IdTipoProducto" IN ('INFU','MEFW') THEN 'CFUT'
					WHEN RES."IdClasificacionTipoInstrumento" = 'IF' OR OPE."IdTipoProducto" = 'INIF' THEN 'IIFN'
					WHEN OPE."IdTipoProducto" IN ('INRF','MERF') THEN 'IRFN'
					WHEN OPE."IdTipoProducto" = 'MEME' THEN 'MONE'
					WHEN OPE."IdTipoProducto" = 'INMO' THEN 'PROD'
					WHEN OPE."IdTipoProducto" = 'INOS' THEN 'DPSS'
					WHEN OPE."IdTipoProducto" = 'PRES' AND OPE."IdClasificacionTipoInstrumento" = 'RV' THEN 'ACCN'
					WHEN OPE."IdTipoProducto" = 'PRES' AND OPE."IdClasificacionTipoInstrumento" = 'RF' THEN 'IRFN'
					WHEN OPE."IdTipoProducto" = 'FACT' THEN 'FACT'
					WHEN OPE."IdTipoProducto" = 'NEST' THEN 'OTRD'
					ELSE 'OTRT' 
			END AS "TipoInstrumento",
			OPE."DescripcionOperacion" AS "TipoOperacionCod",
			CASE  
					WHEN OPE."IdTipoOperacion" IN ('VENSIM','COMSIM','VESIMV','VESIMC') AND OPE."IdCondicionLiquidacion" = 'TP' THEN '0'
					WHEN OPE."DescripcionOperacion" LIKE '%APORTE%' THEN SDF."DiasRetencionesAporte"
					WHEN OPE."DescripcionOperacion" LIKE '%RESCATE%' THEN SDF."DiasRetencionesRescate"
					WHEN OPE."DescripcionOperacion" LIKE '%REINVER%' THEN SDF."DiasRetencionesRescate"
					WHEN OPE."IdTipoInstrumento" 	= 	 'SPT' 		 THEN REPLACE(MVTOCMBTE."IdCondicionLiquidacion",'T','')		
					WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO') THEN REPLACE(MES."IdCondicionLiquidacion",'T','') 
					WHEN OPE."IdCondicionLiquidacion" = '' THEN '0' 
					WHEN 
 	                	OPE."IdCondicionLiquidacion" = 'OT'
 						AND (CAST(COALESCE(MVTOCMBTEOT."FechaHoraMovimientoComprobante",'0001-01-01') AS DATE)) <> '0001-01-01'
 						AND (CAST(COALESCE(MVTOCMBTEOT."FechaLiquidacion",'0001-01-01') AS DATE)) <> '0001-01-01'
 						AND COALESCE(DAYS_BETWEEN(CAST(MVTOCMBTEOT."FechaHoraMovimientoComprobante" AS DATE),CAST(MVTOCMBTEOT."FechaLiquidacion" AS DATE)), '0') > 0
 						THEN DAYS_BETWEEN(CAST(MVTOCMBTEOT."FechaHoraMovimientoComprobante" AS DATE),CAST(MVTOCMBTEOT."FechaLiquidacion" AS DATE))
					WHEN OPE."IdCondicionLiquidacion" = 'OT' THEN '0'
					ELSE REPLACE(OPE."IdCondicionLiquidacion",'T','') 
			END AS "CondicionLiquidacion",
			--* OPE."Cantidad",
			CASE 
				WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS') AND RES."IdClasificacionTipoInstrumento" = 'IF' THEN res."IngValorRescate"
				WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS') AND RES."IdClasificacionTipoInstrumento" <> 'IF' THEN res."IngCantidad"			
				WHEN OPE."IdTipoInstrumento" = 'IF' AND OPE."EsFungible" = 'S' THEN COALESCE(OPE."Cantidad" * OPE."ValorRescateInicial",0)				
				ELSE COALESCE(OPE."Cantidad",0) 
			END AS "Cantidad",
			
			CASE
				WHEN OPE."EsVencimientoPacto" = 'S' THEN RES."INGMontoAPlazo"
				WHEN OPE."DescripcionOperacion" = 'ANTICIPO PACTOS' THEN CEIL((RES."MontoValorizado" / MES."MontoOperacionAnticipo") * MES."MontoOperacionFinalAnticipo")
				WHEN OPE."DescripcionOperacion" LIKE '%PACTO%' or OPE."DescripcionOperacion" LIKE  '%TERMINO%'
	 						THEN RES."MontoLiquidacionOperacion"
	 			WHEN OPE."IdTipoOperacion" LIKE '%SIM%' 
	 						THEN  ROUND (OPE."Cantidad" * OPE."Precio",0)
				WHEN OPE."IdTipoComprobante" = 'FOE' AND OPE."IdTipoInstrumento" = 'FFM' 
								THEN ROUND((OPE."Monto" + OPE."Impuesto"), 0)
				WHEN OPE."MontoCompra" = 0 
								THEN OPE."MontoVenta"
				ELSE OPE."MontoCompra" 
			END AS "MontoTotal",
			CASE
				WHEN OPE."EsVencimientoPacto" = 'S' OR OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO') THEN 
					CASE 
						WHEN RES."IdTipoReajuste" = 'CLP' THEN '$$'
						WHEN RES."IdTipoReajuste" = 'AUD' THEN 'DA'
					ELSE RES."IdTipoReajuste" END
                WHEN SUBSTRING(OPE."IdMoneda", 1, 3) = 'CLP' THEN '$$' || COALESCE(SUBSTRING(OPE."IdMoneda", 4), '')
                WHEN SUBSTRING(OPE."IdMoneda", 1, 3) = 'AUD' THEN 'DA' || COALESCE(SUBSTRING(OPE."IdMoneda", 4), '')
                ELSE COALESCE(OPE."IdMoneda",'')
            END AS "Moneda",

			CASE
				WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS') THEN res."Tasa"
				WHEN OPE."IdTipoComprobante" = 'FAE' AND OPE."IdClasificacionTipoInstrumento" <> 'RV' THEN COALESCE(OPE."Tasa",0)
				WHEN OPE."DescripcionOperacion" IN ('COMPRA FORWARD', 'VENTA FORWARD') 
				AND OPE."Nemotecnico" IN ('ARBITR') THEN CAR."INGTasaPuntosMonedaSecundaria"
				WHEN OPE."IdTipoInstrumento" = 'IF' THEN COALESCE(OPE."Tasa",0)				
				ELSE OPE."Precio"
			END AS "Precio",
			CASE 
					WHEN OPE."DescripcionOperacion" LIKE '%APORTE%' THEN '06'
					WHEN OPE."DescripcionOperacion" LIKE '%RESCATE%' THEN '06'
					WHEN OPE."DescripcionOperacion" LIKE '%REINVER%' THEN '06'
					WHEN OPE."DescripcionOperacion" LIKE '%SPOT%' THEN '05'
					WHEN OPE."DescripcionOperacion" LIKE '%FORWARD%' THEN '05'
					WHEN OPE."DescripcionOperacion" LIKE '%TERMINO%' THEN '05'					
					WHEN OPE."IdBolsa" = ''    THEN '05'
					WHEN OPE."IdBolsa" = 'BCS' THEN '01' --> BOLSA DE SANTIAGO
					WHEN OPE."IdBolsa" = 'BCV' THEN '02'
					WHEN OPE."IdBolsa" = 'BEC' THEN '03'
					WHEN OPE."IdBolsa" = 'FRD' THEN '05'
					WHEN OPE."DescripcionOperacion" LIKE '%PACTO%' THEN '05'
					WHEN OPE."DescripcionOperacion" LIKE '%CON PACTO%' THEN '05'
					WHEN OPE."DescripcionOperacion" LIKE '%COMPRA%' THEN '01'
					WHEN OPE."DescripcionOperacion" LIKE '%VENTA%' THEN '01'
					ELSE '00'
			END AS "Mercado",
			CASE
					WHEN OPE."DescripcionOperacion" LIKE '%SPOT%' AND OPE."Canal" = 'DAT' THEN '02'
					WHEN OPE."IdSistemaBursatil" = 'BCSTELEPREGON' THEN '02'
					WHEN OPE."IdSistemaBursatil" = 'BCSREMATESERIALIZADO ' THEN '04'
					WHEN OPE."IdSistemaBursatil" = 'BCSTELERENTA' THEN '02'
					WHEN OPE."IdSistemaBursatil" = 'BCSREMATEELETRONICO' THEN '04'
					WHEN OPE."IdSistemaBursatil" = 'BCSREMATEHOLANDES' THEN '04'
					ELSE '00' 
			END AS "SistemaTransaccion",
			CASE
					WHEN OPE."IdTipoComprobante" IN ('NCR','NCE') THEN 'OOI'
					WHEN OPE."IdTipoInstrumento" = 'SPT' THEN 'OSD'
					WHEN OPE."IdTipoInstrumento" = 'FFM' THEN 'OOE'
					WHEN OPE."IdClasificacionTipoInstrumento" = 'RV' THEN 'OCD'
					WHEN OPE."DescripcionOperacion" LIKE '%PACTO%' THEN 'OOE'
					WHEN OPE."DescripcionOperacion" LIKE '%TERMINO%' THEN 'OOE'
					WHEN OPE."DescripcionOperacion" LIKE '%COMPRA%' THEN 'OOI'
					WHEN OPE."DescripcionOperacion" LIKE '%VENTA%' THEN 'OOI'
					WHEN OPE."ModalidadEjecucion"='' THEN 'OSD' 
					ELSE OPE."ModalidadEjecucion" 
			END AS "ModalidadEjecucion",
			OPE."Canal" AS "Canal",
			CASE
				WHEN OPE."IdTipoInstrumento" IN ('SPT', 'FWD') OR OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS') THEN SUBSTRING(v_DocumentoIdentidadCtaPropia, 1, LENGTH(v_DocumentoIdentidadCtaPropia) - 2)
				ELSE SUBSTRING( COALESCE(nullif(CNC."DocumentoIdentidad",''),   nullif(CNCSIM."DocumentoIdentidad",''),ASIMCNC."DocumentoIdentidad"     ), 1, LENGTH( COALESCE(nullif(CNC."DocumentoIdentidad",''),   nullif(CNCSIM."DocumentoIdentidad",''),ASIMCNC."DocumentoIdentidad"     )) - 2)
			END AS "RutContraparte",
			CASE
				WHEN OPE."IdTipoInstrumento" IN ('SPT', 'FWD') OR OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS')  THEN SUBSTRING(v_DocumentoIdentidadCtaPropia, LOCATE(v_DocumentoIdentidadCtaPropia, '-') + 1, 1)
				ELSE SUBSTRING( COALESCE( nullif(CNC."DocumentoIdentidad",''),  nullif(CNCSIM."DocumentoIdentidad",''),ASIMCNC."DocumentoIdentidad"    ), LOCATE(COALESCE(nullif(CNC."DocumentoIdentidad",''),  nullif(CNCSIM."DocumentoIdentidad",''),ASIMCNC."DocumentoIdentidad"    ), '-') + 1, 1)
			END AS "DigitoContraparte",			
			'' AS "Pasaporte",
			PER."GlobalLegalEntity" as "GLEI",
			CASE WHEN OPE."IdEstadoFactura" = 'RGN' THEN CONCAT('Operacion de reversa de Operacion ', OPE."IdOperacionOrigenReasignacion")
				 WHEN OPE."IdTipoOperacion" IN ('ANSIMV', 'ANSIMC') AND OPE."INGCantidad" = OPE."Cantidad"  THEN CONCAT('Anticipo Total Simultanea Venta Plazo, Comprobante: ', OPE."IdComprobante") 
				 WHEN OPE."IdTipoOperacion" IN ('ANSIMV', 'ANSIMC') AND OPE."INGCantidad" < OPE."Cantidad"  THEN CONCAT('Anticipo Parcial Simultanea Compra Plazo, Comprobante: ', OPE."IdComprobante")
				 WHEN OPE."IdTipoOperacion" = 'VESIMC' THEN 'Vencimiento Simultanea De Compra'
				 WHEN OPE."IdTipoOperacion" = 'VESIMV' THEN 'Vencimiento Simultanea De Venta'
				 WHEN OPE."EsVencimientoPacto" = 'S' THEN CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN 'Vencimiento Venta con Pacto' WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN 'Vencimiento Compra con Pacto' END
				 ELSE '' 
			END AS "Observaciones",
			CASE
				WHEN v_ParametroSecuencia = 'S' AND COALESCE(OPE."FolioComprobante", 0) <> 0 THEN OPE."FolioComprobante"
				WHEN v_ParametroSecuencia = 'S' AND COALESCE(OPE."FolioComprobante", 0) = 0 AND COALESCE(OPE."IdComprobante", '') <> '0' AND COALESCE(OPE."IdComprobante", '') <> '' THEN COALESCE(CAST(OPE."IdComprobante" AS BIGINT), 0)
				WHEN OPE."FolioComprobante" = 0 THEN OPE."IdComprobante" ELSE OPE."FolioComprobante" 
			END AS "FolioComprobante",
			OPE."IdTipoComprobante" AS "IdTipoComprobante",
			CASE 
				WHEN OPE."DescripcionOperacion" IN ('VENTA CON PACTO') THEN 'VP'
				WHEN OPE."DescripcionOperacion" IN ('COMPRA CON PACTO') THEN 'CP'
				WHEN OPE."DescripcionOperacion" IN ('VENTA A TERMINO') THEN 'VT'
				WHEN OPE."DescripcionOperacion" IN ('COMPRA A TERMINO') THEN 'CT'				
				ELSE OPE."TipoComprobante"
			END AS "TipoComprobante",
			COALESCE(RES."FolioCarteraCustodia", 0) AS "FolioCarteraCustodia" ,
			OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
			CASE
			  WHEN TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD HH24:MI:SS') = '0001-01-01 00:00:00' THEN ''
			  ELSE TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD HH24:MI:SS')
			END AS "FechaVencimiento",		
			CASE
			  WHEN TO_VARCHAR(OPE."FechaLiquidacion", 'YYYY-MM-DD') = '0001-01-01' THEN ''
			  ELSE TO_VARCHAR(OPE."FechaLiquidacion", 'YYYY-MM-DD')
			END AS "FechaLiquidacion"
					
			FROM :v_SalidaConsultarOperaciones OPE
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OPE."IdCliente" = CLI."IdCliente" --AND CLI."TipoDocumentoIdentidad" = 'R'
				INNER JOIN :v_TablaClientes AS PrmCli ON (PrmCli."Valor" IS NULL OR PrmCli."IdSistema" = CLI."IdCliente")
					---CONTRAPARTE CORREDOR
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" CNC  ON (CNC."IdCorredora"=OPE."IdContraparte")
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON OPE."IdPortafolio" =  POR."IdPortafolio"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON CLI."IdPersona" = PER."IdPersona"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON EMI."IdEmisor" = OPE."IdEmisor"
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" MES ON MES."IdOperacion" = OPE."IdOperacion" AND OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO', 'VENTA A TERMINO', 'COMPRA A TERMINO', 'ANTICIPO PACTOS')
				LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON ((CAR."FolioCartera" = OPE."IdOperacion") OR (CAR."IdCartera" = OPE."IdOperacion" AND OPE."IdTipoOperacion" = 'PRESTA'))
				LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON MES."IdOperacion" = RES."IdOperacion"
				LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARR ON CARR."FolioCartera" = RES."FolioCarteraCustodia"
				LEFT JOIN (
					SELECT 	I."IdInstrumento",
							I."ISIN",
							I."Nemotecnico", 
							TI."IdClasificacionTipoInstrumento"
					FROM "BSCL.Plataforma.BD::INS.Instrumento" I
						INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TI ON I."IdTipoInstrumento" = TI."IdTipoInstrumento"
				) AS INS ON OPE."IdClasificacionTipoInstrumento" = INS."IdClasificacionTipoInstrumento" AND ((OPE."Nemotecnico" = INS."Nemotecnico" AND OPE."IdTipoOperacion" NOT IN ('VENPAC','COMPAC')) OR (CARR."NemoSVS" = INS."Nemotecnico" AND OPE."IdTipoOperacion" IN ('VENPAC','COMPAC')))
				INNER JOIN :v_TablaNemos AS PrmNemo ON (PrmNemo."Valor" IS NULL OR PrmNemo."IdSistema" = INS."IdInstrumento")
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SDF ON INS."IdInstrumento" = SDF."IdInstrumento"
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FND ON SDF."IdFondo" = FND."IdFondo"
				LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAI ON PAI."IdPais" = FND."IdPaisOrigen"
				LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ORD."IdOrden" = OPE."IdOperacion" AND OPE."DescripcionOperacion" IN ('COMPRA INTERMEDIACION FINANCIERA', 'VENTA INTERMEDIACION FINANCIERA', 'ANULACION COMPRA INTERMEDIACION FINANCIERA')				
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMIS ON EMIS."IdEmisor" = RES."IdEmisor"
				LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTE ON CAST(MVTOCMBTE."IdComprobante" AS NVARCHAR(40)) = OPE."IdComprobante" AND OPE."IdTipoInstrumento" = 'SPT'
				LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTEORI ON MVTOCMBTEORI."IdComprobante" = OPE."IdComprobanteReferencia" AND OPE."IdTipoComprobante" IN ('NCR','NCE') AND OPE."IdTransaccion" = 0 AND (COALESCE(OPE."CodigoOperadorCorredor", '') = '' OR COALESCE(OPE."IdContraparte", 0) = 0)
				LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRANS ON TRANS."IdTransaccion" = MVTOCMBTEORI."IdTransaccion"
				LEFT JOIN :v_HomCodigoTipoOperacion HCTO ON OPE."IdTipoOperacion" = HCTO."ValorOrigen"
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON OPE."IdTipoOperacion" = TOPE."IdTipoOperacion" AND TOPE."EsTransaccion" = 'S'
				LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" TRAN ON (TRAN."FolioTransaccion" = OPE."FolioTransaccion" AND TRAN."IdEmisor" = OPE."Nemotecnico"  AND  OPE."IdTipoOperacion" LIKE '%SIM%')
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" CNCSIM  ON (CNCSIM."IdCorredora"=TRAN."IdCorredorContraparte")
				LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" ASIMTRA ON ASIMTRA."IdTransaccion" = MVTOCMBTEORI."IdTransaccion"
				LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTEOT ON CAST(MVTOCMBTEOT."IdComprobante" AS NVARCHAR(40)) = OPE."IdComprobante" AND OPE."IdCondicionLiquidacion" = 'OT'
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" ASIMCNC ON (ASIMCNC."IdCorredora" = ASIMTRA."IdCorredorContraparte" AND OPE."IdTipoOperacion" NOT IN ('VESIMV','VESIMC')) OR (ASIMCNC."IdCorredora" = OPE."IdContraparte" AND OPE."IdTipoOperacion" IN ('VESIMV','VESIMC'))
				LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" AS ultSub ON (ultSub."FolioCarteraRelacionada" = CAR."FolioCartera" AND OPE."IdTipoOperacion" = 'PRESTA' AND OPE."IdEstadoFactura" <> 'VIG')
				LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON (SUB."IdSubyacente" = ultSub."IdSubyacente" AND OPE."IdTipoOperacion" = 'PRESTA')
				LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOVTRAN ON ((MOVTRAN."IdMovimientoCarteraEnTransito" = SUB."IdMovimientoCarteraEnTransito" AND OPE."IdEstadoFactura" <> 'VIG') OR (MOVTRAN."IdMovimientoCarteraEnTransito" = CAR."IdMovimientoCarteraEnTransito" AND OPE."IdEstadoFactura" = 'VIG')) AND OPE."IdTipoOperacion" = 'PRESTA'
--				
			WHERE 	TO_CHAR(OPE."FechaMovimiento", 'YYYY-MM-DD') BETWEEN :v_FechaInicio AND :v_FechaTermino
					AND (
					    	(OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO') AND OPE."IdEstadoFactura" IN ('VIG'))
					    OR 
					    	(OPE."DescripcionOperacion" NOT IN ('VENTA CON PACTO', 'COMPRA CON PACTO') AND OPE."IdEstadoFactura" IN ('VIG', 'LIQ', 'ENV', 'FAC', 'RGN', 'EMI','ING','CRT','PEN', 'VCD','ANT'))
					    OR
							(OPE."IdTipoOperacion" IN ('VESIMV','VESIMC'))
					)
					AND NOT (OPE."IdTipoInstrumento" = 'SPT' AND OPE."IdModulo" = 'FAC') AND NOT (OPE."IdTipoInstrumento" = 'FFM' AND INS."Nemotecnico" IS NULL)
					AND (OPE."IdClasificacionTipoInstrumento" = v_Clains OR v_Clains = '')
					AND (
							(OPE."DescripcionOperacion" IN ('VENTA CON PACTO', 'COMPRA CON PACTO') AND OPE."IdEmisor" <> '')
						OR 
							OPE."DescripcionOperacion" NOT IN ('VENTA CON PACTO', 'COMPRA CON PACTO')
					)
			AND OPE."IdTipoOperacion" NOT IN ('VCDPAC')		
			-- AND OPE."DescripcionOperacion" NOT LIKE '%ANULACION%'		
			ORDER BY "Fecha" ASC, "Hora" ASC;
			
	ELSE
	
		SELECT
			'' AS "NombreCliente",
			'' AS "RutCliente",
			'' AS "CodigoPortafolio",
			'' AS "CodigoBolsa",
			'' AS "Folio",
			'' AS "Instrumento",
			'' AS "NombreTipoOperacion",
			0 AS "IdOperacion",
			'' AS "IdBolsa",
			'' AS "Fecha",
			'' AS "Hora",
			0 AS "NumeroOperador",
			'' AS "ISIN",
			'' AS "Nemotecnico",
			'' AS "NemoCMF",
			'' AS "TipoInstrumento",
			'' AS "TipoOperacionCod",
			'' AS "CondicionLiquidacion",
			0 AS "Cantidad",
			0 AS "MontoTotal",
			'' AS "Moneda",
			0 AS "Precio",
			'' AS "Mercado",
			'' AS "SistemaTransaccion",
			'' AS "ModalidadEjecucion",
			'' AS "Canal",
			'' AS "RutContraparte",
			'' AS "DigitoContraparte",
			'' AS "Pasaporte",
			'' AS "GLEI",
			'' AS "Observaciones",
			'' AS "FolioComprobante",
			'' AS "IdTipoComprobante",
			'' AS "TipoComprobante",
			0 AS "FolioCarteraCustodia" ,
			'' AS "InstrumentoNegociacion",
			'' AS "FechaVencimiento",
			'' AS "FechaLiquidacion"
		FROM DUMMY
		WHERE 1=2;
		
	END IF;
END;
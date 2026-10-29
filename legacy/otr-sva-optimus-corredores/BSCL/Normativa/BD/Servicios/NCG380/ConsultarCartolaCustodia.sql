SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::ConsultarCartolaCustodia";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::ConsultarCartolaCustodia"
( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,
    IN p_FechaInicio		DATE,
	IN p_FechaTermino		DATE,
	IN p_FolioCartera		BIGINT DEFAULT 0,
	OUT p_out_TablaSalida	TABLE   
	(
	"FolioCartera"						BIGINT
	,"IdCartera" 						BIGINT 
	,"NumeroCliente"				    BIGINT 
	,"NumeroCuenta"				    	NVARCHAR(50)
	,"Fecha" 							DATE 
	,"Hora" 							NVARCHAR(10) 
	,"NumeroComprobante"				BIGINT	
	,"Emisor" 							NVARCHAR(50)
	,"TipoMovimiento" 					NVARCHAR(10)
	,"Movimiento" 						BIGINT
	,"Isin"								NVARCHAR(50)
	,"Nemotecnico" 						NVARCHAR(40)
	,"Cantidad" 						DECIMAL(26,8)
	,"Monto"	 						DECIMAL(26,8)
	,"Moneda"        					NVARCHAR(30) 
	,"Saldo" 							DECIMAL(26,8)
	,"SubCustodio" 						NVARCHAR(3)
	,"Estado"							NVARCHAR(40)
	,"DocumentoIdentidad"               NVARCHAR(40)
	,"IdTipoOperacion" 					NVARCHAR(6)
	,"IdClasificacionTipoInstrumento"   NVARCHAR(6)
	,"EGRFechaLiquidacion" 				DATE
	,"INGFechaLiquidacion" 				DATE
	,"NombreConcepto"					NVARCHAR(50)
	)
)


LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN

	DECLARE v_Count BIGINT DEFAULT 0;

	-- Debe estar en 'N', para que el retiro de Garantia vuelva a la misma cartera.
	-- SELECT "GeneraCarteraNueva", * FROM "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" WHERE "IdTipoOperacion" = 'RETGTA'

	--* --------------------------------------------------------------------
    --* 1.1 - CARTERAS VIGENTE HASTA LA FECHA DESDE DONDE SE PIDE EL INFORME
	--* --------------------------------------------------------------------

	--* Carteras que entraron antes de la fecha de inicio y con fecha de egreso > a la fecha de inicio de la consulta
	v_CustodiaTmp = 
		SELECT	DISTINCT "FolioCartera", 'CUS' AS "Tipo",
				0 AS "IdCartera"
		FROM	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
		WHERE	CAR."IdEmpresa" = p_IdEmpresa 
				AND TO_VARCHAR(CAR."INGFechaLiquidacion", 'YYYY-MM-DD')  <= p_FechaInicio -- Fecha Inicio	
				AND TO_VARCHAR(CAR."EGRFechaLiquidacion", 'YYYY-MM-DD')  > p_FechaInicio -- Fecha Inicio -1 dia
				AND CAR."IdEstado" NOT IN ('ANU', 'LIQ');

	IF p_FolioCartera > 0 THEN 
		SELECT 'CUSTODIA 0' AS "TABLA", * FROM 	:v_CustodiaTmp WHERE "FolioCartera" = p_FolioCartera;
	END IF;

	--* Ahora selecciono la ultima antes del periodo de consulta
	v_CustodiaTmp = 
		SELECT	CAR."FolioCartera", 'CUS' AS "Tipo",
				MAX(CAR."IdCartera") AS "IdCartera"
		FROM	:v_CustodiaTmp CUS
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR 
				ON 	CUS."FolioCartera" = CAR."FolioCartera" 
					AND TO_VARCHAR(CAR."EGRFechaLiquidacion", 'YYYY-MM-DD')  <= p_FechaInicio
					AND TO_VARCHAR(CAR."EGRFechaLiquidacion", 'YYYY-MM-DD')  >= '1900-01-01' 
		GROUP BY  CAR."FolioCartera";

	IF p_FolioCartera > 0 THEN 
		SELECT 'CUSTODIA 1' AS "TABLA", * FROM 	:v_CustodiaTmp WHERE "FolioCartera" = p_FolioCartera;
	END IF;


	--* MAS las que estan vigentes y no tienen movimientos posterior a la fecha de inicio de la consulta
	v_CustodiaTmp = 
		SELECT	*
		FROM	:v_CustodiaTmp
		--* -----------------------------------
		UNION ALL
		--* -----------------------------------		
		SELECT	CAR."FolioCartera", 'CUS' AS "Tipo",
				CAR."IdCartera" AS "IdCartera"
		FROM	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR 
		WHERE 	"IdEstado" = 'VIG'				
				AND TO_VARCHAR(CAR."INGFechaLiquidacion", 'YYYY-MM-DD')  <= p_FechaInicio 
				--* Solo por siaca...
				AND CAR."FolioCartera" NOT IN ( SELECT "FolioCartera" FROM :v_CustodiaTmp );

	IF p_FolioCartera > 0 THEN 
		SELECT 'CUSTODIA VIG' AS "TABLA", * FROM 	:v_CustodiaTmp WHERE "FolioCartera" = p_FolioCartera;
	END IF;


	--* --------------------------------------------------------------------
    --* 1.2.- INGRESOS DE CARTERA EN EL PERIODO DE LA CONSULTA
	--* --------------------------------------------------------------------
	v_CustodiaTmp = 
		SELECT	*
		FROM	:v_CustodiaTmp 
		--* -----------------------------------
		UNION ALL
		--* -----------------------------------
		SELECT	"FolioCartera", 'ING' AS "Tipo",
				MIN("IdCartera") AS "IdCartera"
		FROM	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
		WHERE	CAR."IdEmpresa" = p_IdEmpresa 
--				AND CAR."IdTipoOperacion" NOT IN ( 'INGGTA', 'EGRGTA' )
				AND TO_VARCHAR(CAR."INGFechaLiquidacion", 'YYYY-MM-DD') BETWEEN  p_FechaInicio AND p_FechaTermino 
				AND CAR."IdEstado" <> 'ANU'
				--* Saco los ya estan en la custodia antes de la fecha de consulta
				--* Estos casos son los que quedan vigentes con un retiro dentro del periodo de consulta
				AND "FolioCartera" NOT IN ( SELECT "FolioCartera" FROM :v_CustodiaTmp )
		GROUP BY  "FolioCartera";
	
	v_CustodiaNegativaTemp = 
		SELECT DISTINCT 
			CARNEG."IdCarteraNegativa" AS "FolioCartera", 'RET' AS "Tipo",
			MIN(CARNEG."IdCarteraNegativa") AS "IdCartera"
		FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" AS CARNEG
		WHERE CARNEG."IdEmpresa" = p_IdEmpresa AND CARNEG."IdEstado" = 'VIG'
		AND TO_VARCHAR(CARNEG."FechaLiquidacion", 'YYYY-MM-DD') BETWEEN  p_FechaInicio AND p_FechaTermino 
		GROUP BY  "IdCarteraNegativa";

	IF p_FolioCartera > 0 THEN 
		SELECT 'CUSTODIA 2' AS "TABLA", * FROM 	:v_CustodiaTmp WHERE "FolioCartera" = p_FolioCartera;
	END IF;


	--* --------------------------------------------------------------------
    --* 1.3.- HASTA ACA ESTA TODO A NIVEL DE CARTERAS => DEBO AGRUPAR
	--* 	  POR CLIENTE Y NEMO 
	--* --------------------------------------------------------------------
	v_Subyacente = 
		SELECT 	"FolioCarteraRelacionada", 
				"Nemotecnico", 
				RANK() OVER ( PARTITION BY "FolioCarteraRelacionada" ORDER BY "IdSubyacente" ) AS "Registro"
		FROM	"BSCL.Custodia.BD::CUS.Subyacente" 
		WHERE	"FolioCarteraRelacionada" IN ( SELECT DISTINCT "FolioCartera" FROM :v_CustodiaTmp );


	v_CustodiaCartera = 
		SELECT	CAR."IdCartera" AS "IdCartera"
				, CAR."IdCliente" AS "IdCliente"
				, CAR."IdPortafolio" AS "IdPortafolio" 	
				, CAR."CodigoPortafolio" AS "CodigoPortafolio"
				, CASE 
				  WHEN CAR."IdTipoInstrumento" = 'SIM' THEN COALESCE( SUBY."Nemotecnico" , 'SUBY-??' )
				  ELSE CAR."Nemotecnico"
				  END AS "Nemotecnico"
				, CAR."NemoSVS" AS "NemoSVS" 	
				, CASE
				  WHEN CUS."Tipo" = 'CUS' THEN 
				  	CASE WHEN CAR."IdClasificacionTipoInstrumento" <> 'IF' THEN CAR."Cantidad"
				  	ELSE CAR."ValorRescate" END
				  -- - CAR."EGRCantidad"
				  ELSE 
				  	CASE WHEN CAR."IdClasificacionTipoInstrumento" <> 'IF' THEN CAR."INGCantidad"
				  	ELSE CAR."INGValorRescate" END
				  END AS "Cantidad" 	
				, CAR."INGMontoATasaCompraActual" AS "Monto"
				, 'NORM' AS "TipoCartera"
		FROM 	:v_CustodiaTmp AS CUS
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR 
				ON 	CAR."IdCartera" = CUS."IdCartera"
					AND CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM')
				LEFT JOIN :v_Subyacente AS SUBY ON (CAR."FolioCartera" = SUBY."FolioCarteraRelacionada" AND SUBY."Registro" = 1);
			
	-- OBTENGO CARTERAS NEGATIVAS
	v_CustodiaCarteraNegativa = 
		SELECT 
			CARNEG."IdCarteraNegativa" AS "IdCartera"
			, CARNEG."IdCliente" AS "IdCliente"
			, CARNEG."IdPortafolio" AS "IdPortafolio"
			, PORT."CodigoPortafolio" AS "CodigoPortafolio"
			, CARNEG."Nemotecnico" AS "Nemotecnico"
			, CARNEG."NemoSVS" AS "NemoSVS"
			, CASE WHEN TIPOINS."IdClasificacionTipoInstrumento" = 'IF' THEN CARNEG."ValorRescate" ELSE CARNEG."Cantidad" END AS "Cantidad"
			, CARNEG."MontoOperacionMonedaInstrumento" AS "Monto"
			, 'NEG' AS "TipoCartera"
		FROM :v_CustodiaNegativaTemp AS CUS 
		INNER JOIN "BSCL.Custodia.BD::CUS.CarteraNegativa" AS  CARNEG ON CARNEG."IdCarteraNegativa" = CUS."IdCartera"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = CARNEG."IdInstrumento" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPOINS ON TIPOINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON port."IdPortafolio" = CARNEG."IdPortafolio";

	
		
	--* --------------------------------------------------------------------
    --* 1.4.- NORMALIZO DATOS DE CUSTODIA VIGENTE 
	--* --------------------------------------------------------------------
	v_Custodia = 
		SELECT	CAR."IdCartera" AS "IdCartera"
				, CAR."FolioCartera" 
				, CAR."FechaIngresoRegistro" AS "FechaRegistro"
				, CAR."FechaOperacion" AS "FechaOperacion" 					
				, 'ING' AS "TipoOperacion" 
				, 0 AS "OrdenCUS"
				, 0 AS "OrdenGTA"
				, CAR."IdTipoOperacion" AS "IdTipoOperacion"		
				, CUS."Cantidad" 	
				, CUS."Monto"
				, 'LG' AS "Estado"				  
				, LEFT(TO_TIME( CAR."FechaIngresoRegistro"), 8) AS "Hora"			
				, CAR."IdModulo"
		FROM 	:v_CustodiaCartera AS CUS
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR 
				ON CAR."IdCartera" = CUS."IdCartera" AND CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM')  AND CUS."TipoCartera" = 'NORM'
		UNION ALL
		SELECT 
			CARNEG."IdCarteraNegativa" AS "IdCartera"
			, CARNEG."IdCarteraNegativa" AS "FolioCartera"
			, CARNEG."FechaIngresoRegistro" AS "FechaRegistro"
			, CARNEG."FechaOperacion" AS "FechaOperacion"
			, 'RETNEG' AS "TipoOperacion" 
			, 0 AS "OrdenCUS"
			, 0 AS "OrdenGTA" 
			, CARNEG."IdTipoOperacion" AS "IdTipoOperacion"
			, CUS."Cantidad" AS "Cantidad"
			, CUS."Monto" AS "Monto"
			, 'LG' AS "Estado"
			, LEFT(TO_TIME( CARNEG."FechaIngresoRegistro"), 8) AS "Hora"
			, 'CUS' AS "IdModulo" -- MODULO
		FROM :v_CustodiaCarteraNegativa AS CUS
			INNER JOIN "BSCL.Custodia.BD::CUS.CarteraNegativa" AS CARNEG 
			ON CUS."IdCartera" = CARNEG."IdCarteraNegativa" AND CUS."TipoCartera" = 'NEG';
	SELECT * FROM :v_Custodia;
	IF p_FolioCartera > 0 THEN 
		SELECT 'CUSTODIA 3' AS "TABLA", * FROM 	:v_Custodia WHERE "FolioCartera" = p_FolioCartera;
	END IF;


	--* --------------------------------------------------------------------
    --* 2.1 - MOVIMIENTOS DE LA CARTERA EN EL PERIODO DE LA CONSULTA 
	--*       INCLUYE INGRESOS DE GARANTIAS
	--* --------------------------------------------------------------------
	v_Movtos = 
		SELECT	CAR."IdCartera" AS "IdCartera"
				, CAR."FolioCartera" 
				, CAR."FechaModificacionRegistro" AS "FechaRegistro"
				, CAR."EGRFechaOperacion" AS "FechaOperacion" 					
				, CASE 
				  WHEN CAR."EGRIdTipoOperacion" = 'INGGTA' THEN 'INGG' ELSE 'RET' 
				  END AS "TipoOperacion"
				, CAR."IdCartera" AS "OrdenCUS"
				, 0 AS "OrdenGTA"
				, CAR."EGRIdTipoOperacion" AS "IdTipoOperacion"		
				,CASE WHEN CAR."IdClasificacionTipoInstrumento" <> 'IF' THEN CAR."EGRCantidad"
				ELSE CAR."EGRValorRescate" END AS "Cantidad"			
				, CAR."EGRMonto" AS "Monto"	 														
				, CASE 
				  WHEN CAR."EGRIdTipoOperacion" = 'INGGTA' THEN 'GT' ELSE 'LG' 
				  END AS "Estado"				
				, LEFT(TO_TIME( CAR."FechaModificacionRegistro"), 8) AS "Hora"				
				, CAR."IdModulo"				
		FROM 	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR 
  				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS FAC ON ( fac."IdComprobante" = CAR."FolioComprobante" AND CAR."IdModulo" = 'FAC') 				
		WHERE	CAR."IdEmpresa" = p_IdEmpresa 
				AND CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM')
				AND CAR."EGRIdTipoOperacion" NOT IN ( 'RETGTA' )
				AND CAR."EGRCantidad" <> 0
				AND CAR."IdEstado" <> 'ANU'
				AND CAR."EGRFechaOperacion" BETWEEN p_FechaInicio AND p_FechaTermino;


	IF p_FolioCartera > 0 THEN 
		SELECT 'RETIRO 1' AS "TABLA", * FROM 	:v_Movtos WHERE "FolioCartera" = p_FolioCartera;
	END IF;

	--* --------------------------------------------------------------------
    --* 2.2 - RETIROS DE GARANTIAS
	--* --------------------------------------------------------------------
	v_Movtos = 
		SELECT	*
		FROM	:v_Movtos
		UNION ALL
		SELECT	GTA."IdCarteraOrigen" AS "IdCartera"
				, GTA."FolioCarteraOrigen"  AS "FolioCartera" 
				, GTA."FechaModificacionRegistro" AS "FechaRegistro"
				, GTA."EGRFechaOperacion" AS "FechaOperacion" 					
				, 'RETG' AS "TipoOperacion" 
				, GTA."IdCarteraOrigen" AS "OrdenCUS"
				, GTA."IdGarantia" AS "OrdenGTA"
				, GTA."EGRIdTipoOperacion" AS "IdTipoOperacion"		
				, GTA."EGRCantidad" AS "Cantidad" 		
				, GTA."EGRMonto"as "Monto"	 														
				, 'GT' AS "Estado"				
				, LEFT(TO_TIME( GTA."FechaModificacionRegistro"), 8) AS "Hora"		
				, 'GTA' AS "IdModulo"		
		FROM 	"BSCL.Custodia.BD::GTA.Garantia" AS GTA 
				INNER JOIN (	SELECT CUS."FolioCartera" FROM :v_Custodia CUS
								UNION 
								SELECT DISTINCT MOV."FolioCartera" FROM :v_Movtos MOV ) AS DET
							ON DET."FolioCartera" = GTA."FolioCarteraOrigen"
		WHERE	GTA."IdEmpresa" = p_IdEmpresa 
				AND GTA."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM')
				AND GTA."EGRCantidad" <> 0
				AND GTA."IdEstado" <> 'ANU'
				AND GTA."EGRFechaOperacion" BETWEEN p_FechaInicio AND p_FechaTermino;

	IF p_FolioCartera > 0 THEN 
		SELECT 'RETIRO 2' AS "TABLA", * FROM 	:v_Movtos WHERE "FolioCartera" = p_FolioCartera;
	END IF;

	--* -------------------------------
	v_TodoTmp = 
		SELECT 	* 	FROM 	:v_Custodia
		UNION ALL
		SELECT 	* 	FROM 	:v_Movtos;
	--* -------------------------------		

	IF p_FolioCartera > 0 THEN 
		SELECT 'TODO 0' AS "TABLA", * FROM 	:v_TodoTmp WHERE "FolioCartera" = p_FolioCartera;		
	END IF;

	--* Existe pato que Subyacente "a veces" tiene 2 nemos para misma cartera => se toma el 1ro
	v_Subyacente = 
		SELECT 	"FolioCarteraRelacionada", 
				"Nemotecnico", 
				RANK() OVER ( PARTITION BY "FolioCarteraRelacionada" ORDER BY "IdSubyacente" ) AS "Registro"
		FROM	"BSCL.Custodia.BD::CUS.Subyacente" 
		WHERE	"FolioCarteraRelacionada" IN ( SELECT DISTINCT "FolioCartera" FROM :v_TodoTmp );


	v_Todo = 
		SELECT 	MOV."FolioCartera" AS "FolioCartera"						
				, MOV."IdCartera" AS "IdCartera"
				, CAR."IdCliente"AS "IdCliente"
				, CAR."IdPortafolio" AS "IdPortafolio"
				, CAR."CodigoPortafolio" AS "CodigoPortafolio"
				, MOV."FechaOperacion" AS "FechaOperacion"
				, MOV."Hora" AS "Hora"
				, MOV."Estado" AS "Estado"
				--, COALESCE(CAR."EGRFolioComprobante", FAC."FolioComprobante", 0) AS "FolioComprobante"									
				, CASE WHEN "EGRIdTipoOperacion"='RETGTA' THEN CAR."FolioComprobante" ELSE CAR."EGRFolioComprobante" END   AS "FolioComprobante"
				, CAR."IdEmisor" AS "IdEmisor" 										
				, MOV."TipoOperacion" AS "TipoOperacion" 
				, MOV."IdModulo" AS "IdModulo"
				, CAR."ISIN" AS "ISIN"					
				, CAR."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" 					
				, CAR."IdInstrumento" AS "IdInstrumento" 					
				, CASE 
				  WHEN CAR."IdTipoInstrumento" = 'SIM' THEN COALESCE( SUBY."Nemotecnico" , 'SUBY-??' )
				  ELSE CAR."Nemotecnico"
				  END AS "Nemotecnico"
				, CAR."NemoSVS" AS "NemoSVS" 	
				, MOV."Cantidad" AS "Cantidad"			
				, MOV."Monto" AS "Monto"	
				, CAR."IdMonedaInstrumento" AS "IdMonedaInstr" 									
				, CAR."IdBoveda" AS "IdBoveda" 	
				, MOV."IdTipoOperacion" AS "IdTipoOperacion"
				, CAR."DocumentoIdentidad" AS "DocumentoIdentidad" 	
				, MOV."OrdenCUS"
				, MOV."OrdenGTA"
				,CAR."IdTipoInstrumento"
				,CAR."EGRFechaLiquidacion"
				,CAR."INGFechaLiquidacion"
				,CAR."NombreConcepto"
				,TO_TIMESTAMP(CONCAT(CONCAT(TO_NVARCHAR(MOV."FechaOperacion",'YYYY-MM-DD'),' '), MOV."Hora")) AS "FechaHora"
				
		FROM 	:v_TodoTmp AS MOV
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR  ON ( CAR."IdCartera" = MOV."IdCartera" )
				LEFT JOIN :v_Subyacente AS SUBY ON (MOV."FolioCartera" = SUBY."FolioCarteraRelacionada" AND SUBY."Registro" = 1)
  				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS FAC ON ( fac."IdComprobante" = CAR."FolioComprobante" AND CAR."IdModulo" = 'FAC');							
  			
  	v_todoNeg = 
  		SELECT 
  	    	CUS."FolioCartera" AS "FolioCartera"
  	    	, CUS."IdCartera" AS "IdCartera"
  	    	, CARNEG."IdCliente" AS "IdCliente"
  	    	, CARNEG."IdPortafolio" AS "IdPortafolio"
  	    	, POR."CodigoPortafolio" AS "CodigoPortafolio"
  	    	, CARNEG."FechaOperacion" AS "FechaOperacion"
  	    	, CUS."Hora" AS "Hora"
  	    	, CUS."Estado" AS "Estado"
  	    	, CARNEG."FolioComprobante" AS "FolioComprobante"
  	    	, CARNEG."IdEmisor" AS "IdEmisor"
  	    	, CUS."TipoOperacion" AS "TipoOperacion"
  	    	, CUS."IdModulo" AS "IdModulo"
  	    	, CINS."ISIN"  AS "ISIN"
  	    	, CINS."IdClasificacionTipoInstrumento"  AS "IdClasificacionTipoInstrumento"
  	    	, CINS."IdInstrumento" AS "IdInstrumento" 
  	    	, CARNEG."Nemotecnico" AS "Nemotecnico"
  	    	, CARNEG."NemoSVS" AS "NemoSVS" 
  	    	, -1 * CUS."Cantidad" AS "Cantidad"
  	    	, -1 * CUS."Monto" AS "Monto"
  	    	, CARNEG."IdMonedaInstrumento" AS "IdMonedaInstr" 
  	    	, CARNEG."IdBoveda" AS "IdBoveda"	
  	    	, CUS."IdTipoOperacion" AS "IdTipoOperacion"
  	    	, CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
			, CUS."OrdenCUS"
			, CUS."OrdenGTA"
			, CINS."IdTipoInstrumento"  AS "IdTipoInstrumento"
			, CARNEG."FechaLiquidacion" AS "EGRFechaLiquidacion" 
			, CARNEG."FechaLiquidacion" AS "INGFechaLiquidacion"
			, CONCEPTO."Nombre" AS "NombreConcepto"
			,TO_TIMESTAMP(CONCAT(CONCAT(TO_NVARCHAR(CARNEG."FechaOperacion",'YYYY-MM-DD'),' '), CUS."Hora")) AS "FechaHora"
  	    FROM :v_TodoTmp AS CUS
  	    INNER JOIN "BSCL.Custodia.BD::CUS.CarteraNegativa" AS CARNEG ON CARNEG."IdCarteraNegativa" = CUS."IdCartera"
  	    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CARNEG."IdPortafolio" 
  	    LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto"  AS CONCEPTO ON CONCEPTO."IdConcepto" = CARNEG."IdConcepto"
  	    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = CARNEG."IdCliente"
  	    LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS CINS ON CINS."IdInstrumento" = CARNEG."IdInstrumento"
  	    LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS FAC ON ( fac."IdComprobante" = CARNEG."FolioComprobante");	
  
  	v_Todo = 
  		SELECT * FROM :v_Todo
  		UNION ALL
  		SELECT * FROM :v_todoNeg;
  	    

	SELECT 	COUNT(1)
	INTO	v_Count
	FROM	:v_Todo;
	
	IF v_Count > 0 THEN
		p_out_TablaSalida = 
			SELECT	"FolioCartera" 
					,"IdCartera" 
					,"NumeroCliente"
					,"NumeroCuenta"	
					,"Fecha" 
					,"Hora" 
					,"NumeroComprobante"
					,"Emisor" 
					,"TipoMovimiento" 	
					--* 1: Movimiento es producto de la ejecución de una operación 0: Movimiento no es producto de la ejecución de una operación
					,"Movimiento" 
					,"Isin"		
					,"Nemotecnico" 	
					,"Cantidad"
					,"Monto"
					,CASE WHEN (rn=1 AND max_rn=1) THEN CONCAT("Moneda", ' Saldo Inicial/Final')
						WHEN rn=1 THEN CONCAT("Moneda", ' Saldo Inicial')
						WHEN rn= max_rn THEN CONCAT("Moneda", ' Saldo Final')
						ELSE "Moneda" END AS"Moneda"
					--* ------------------------------------------------------------------------------------
					,"Saldo"
											
					--* ------------------------------------------------------------------------------------											
			    	,"SubCustodio" 
			    	,"Estado"
			    	,"DocumentoIdentidad" 
			    	,"IdTipoOperacion"
			    	,"IdClasificacionTipoInstrumento"
			    	,"EGRFechaLiquidacion"
			    	,"INGFechaLiquidacion"
                    ,"NombreConcepto"
               FROM(                   
			SELECT	 "FolioCartera" 		AS 	"FolioCartera" 
					,"IdCartera" 			AS 	"IdCartera" 
					,"IdCliente" 			AS	"NumeroCliente"
					,"CodigoPortafolio"		AS	"NumeroCuenta"	
					,"FechaOperacion"		AS	"Fecha" 
					,"Hora" 				AS	"Hora" 
					,CASE
					 WHEN "IdTipoInstrumento" = 'FWD' THEN "FolioCartera"
					  ELSE CASE WHEN "FolioComprobante" = 0 THEN "FolioCartera" ELSE "FolioComprobante" END END
					 						AS	"NumeroComprobante"
					,"IdEmisor" 			AS	"Emisor" 
					,"TipoOperacion"		AS	"TipoMovimiento" 	
					--* 1: Movimiento es producto de la ejecución de una operación 0: Movimiento no es producto de la ejecución de una operación
					,CASE 
					 WHEN "IdModulo" = 'CUS' OR "IdModulo" = 'GTA' THEN 0 
					 ELSE 1 END 			AS 	"Movimiento" 
					,"ISIN" 				AS	"Isin"		
					,CASE 
					 WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" 
					 ELSE "Nemotecnico" END AS 	"Nemotecnico" 	
					,"Cantidad" 			AS	"Cantidad"
					,"Monto"				AS	"Monto"
					,"IdMonedaInstr" 		AS	"Moneda"
					--* ------------------------------------------------------------------------------------
					,SUM( "Cantidad" * ( 
									CASE WHEN "TipoOperacion"  IN ('ING', 'RETG', 'RETNEG') THEN 1 ELSE -1 END ) 
									) OVER (	
									PARTITION BY "IdCliente", "IdPortafolio", "Nemotecnico" ORDER BY "Nemotecnico","FechaHora","FolioCartera","IdCartera","FolioComprobante" 
									) 
											AS 	"Saldo"
											
					--* ------------------------------------------------------------------------------------											
			    	,"IdBoveda" 			AS	"SubCustodio" 
			    	,CASE 
			    	 WHEN "TipoOperacion" = 'INGG' OR "TipoOperacion" = 'RETG' THEN 'GT' 
			    	 ELSE "Estado" END 		AS 	"Estado"
			    	,"DocumentoIdentidad"	AS	"DocumentoIdentidad" 
			    	,"IdTipoOperacion"		AS	"IdTipoOperacion"
			    	,"IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			    	,"EGRFechaLiquidacion"
			    	,"INGFechaLiquidacion"
                    ,"NombreConcepto"
                    ,ROW_NUMBER() OVER (PARTITION BY "IdCliente", "IdPortafolio", CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END ORDER BY "Nemotecnico","FechaHora","OrdenCUS","FolioComprobante") AS rn
    				,COUNT(*) OVER (PARTITION BY "IdCliente", "IdPortafolio", CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END ORDER BY "Nemotecnico" ) AS max_rn
			FROM 	:v_Todo FIN2 ) fin
			ORDER BY "NumeroCuenta", "Nemotecnico", rn ASC, "Fecha", "Hora";
			

	IF p_FolioCartera > 0 THEN 
		select 	*
		FROM 	:v_Todo FIN
		WHERE 	"IdPortafolio" = p_FolioCartera;
	END IF;

			
	ELSE 
	
		p_out_TablaSalida=
			SELECT 
					0	AS "FolioCartera"
					,0	AS "IdCartera"
					,0 	AS "NumeroCliente"
					,0 	AS "NumeroCuenta"	
					,'01-01-1900' AS "Fecha" 
					,'00:00' AS "Hora" 
					,0 	AS "NumeroComprobante"
					,'' AS "Emisor" 
					,'' AS "TipoMovimiento" 
					,1 	AS "Movimiento" 
					,'' AS "Isin"
					,'' AS "Nemotecnico" 	
					,0 	AS "Cantidad"
					,0	AS "Monto"
					,'' AS "Moneda"
					,0 	AS "Saldo"
			    	,'' AS "SubCustodio" 
			    	,'' AS "Estado"
			    	,'' AS "DocumentoIdentidad" 
			    	,'' AS "IdTipoOperacion"
			    	,''  AS "IdClasificacionTipoInstrumento"
			    	,''  AS "EGRFechaLiquidacion"
			    	,''  AS "INGFechaLiquidacion"
                    ,'' AS "NombreConcepto"
                    
    		FROM 	DUMMY 
    		WHERE 	1=2;
	
	END IF;
	
END;
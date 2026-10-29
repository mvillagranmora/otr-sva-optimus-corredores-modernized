SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Cus";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Cus"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0,
    --VARIABLES DE SALIDA 
	OUT p_out_TablaSalida	 		  "BSCL.Normativa.BD::NOR.SALIDA_NCG380CUS" --Se debe llamar siemte p_out_TablaSalida
   
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL" 
AS 
  BEGIN
  

	
	DECLARE v_Count BIGINT:=0;

    DECLARE v_FechaInicio DATE DEFAULT '0001-01-01';
    DECLARE v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_IdEmpresa INT := 0;
	
	DECLARE v_SalidaSPCus	TABLE   
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
		,"IdTipoOperacion"                  NVARCHAR(10)
		,"IdClasificacionTipoInstrumento"   NVARCHAR(8)
		,"EGRFechaLiquidacion" 				DATE
		,"INGFechaLiquidacion" 				DATE
		--,"IdTipoMovimiento"					NVARCHAR(3)
		,"NombreConcepto"					NVARCHAR(80)
		);


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


	--* ------------------------------------------------------------------------------	
	--* DATOS SIN FILTRO DE LA CUSTODIA
	--* ------------------------------------------------------------------------------	
	CALL "BSCL.Normativa.BD.Servicios.NCG380::ConsultarCartolaCustodia"(
		p_IdEmpresa => v_IdEmpresa,
		p_FechaInicio => v_FechaInicio,
		p_FechaTermino=> v_FechaTermino,
		p_OUT_TablaSalida => v_SalidaSPCus 
	);


    -- Select * from :v_SalidaSPCus where "DocumentoIdentidad" = '91904000-9';

	SELECT COUNT(1) INTO v_Count FROM :v_SalidaSPCus;
	
	IF v_Count > 0 THEN

		--* FILTRO POR CLIENTES (Asume que la info viene como 338-4)
		--* ------------------------------------------------------------------------------	
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
	
		--* FILTRO POR MERCADO
		--* ------------------------------------------------------------------------------	
		v_TablaMercado = 
			SELECT 	"Valor", "IdSistema" 
			FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
			WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'CLAINS';
	
		SELECT COUNT(1) INTO v_Count FROM :v_TablaMercado;
		IF v_Count = 0 THEN
			v_TablaMercado = 
				SELECT	NULL AS "Valor", NULL AS "IdSistema"
				FROM	DUMMY;
		END IF;
	
	
		--* FILTRO POR NEMO
		--* ------------------------------------------------------------------------------	
		v_TablaNemo = 
			SELECT 	"Valor", "IdSistema" 
			FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
			WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'NEMO' AND "Valor" IS NOT NULL ;
	
		SELECT COUNT(1) INTO v_Count FROM :v_TablaNemo;
		IF v_Count = 0 THEN
			v_TablaNemo = 
				SELECT	NULL AS "Valor", NULL AS "IdSistema"
				FROM	DUMMY;
		END IF;
	
		--* HOMOLOGACION TIPO DE MOVIMIENTO
		--* ------------------------------------------------------------------------------	
		v_TablaTipoMovimiento = 
			SELECT 	"ValorOrigen", "IdEstado", "ValorDestino"
			FROM 	"BSCL.Normativa.BD::DOC.Homologacion"
			WHERE 	"Esquema" = 'NCG380Cus';
			
	
		--* TABLA FINAL
		--* ------------------------------------------------------------------------------	
		p_OUT_TablaSalida=
			SELECT 	SC."DocumentoIdentidad" AS "NumeroCliente"
					,SC."NumeroCuenta" AS "NumeroCuenta"
					,SC."Fecha" AS "Fecha"
					,SC."Hora" AS "Hora"
					,CASE WHEN SC."EGRFechaLiquidacion" < v_FechaInicio THEN SC."NumeroComprobante" ELSE  SC."NumeroComprobante" END  AS "NumeroComprobante"
					,SC."Emisor"  AS "Emisor"
					,IFNULL(TMOV."ValorDestino" ,CASE SC."TipoMovimiento" WHEN 'ING' THEN 'ICO' WHEN 'RET' THEN 'ECO' ELSE  SC."IdTipoOperacion" || '-' || SC."TipoMovimiento" END ) AS "TipoMovimiento"
					,SC."Movimiento" AS "Movimiento"
					,SC."Isin"	 AS "ISIN"
					,SC."Nemotecnico" AS "Nemotecnico"
					,SC."Cantidad" AS "Cantidad" 
					,SC."Monto" AS "Monto"
					,CASE
						WHEN SUBSTRING(SC."Moneda", 1, 3) = 'CLP' THEN '$$' || COALESCE(SUBSTRING(SC."Moneda", 4), '') 
						WHEN SUBSTRING(SC."Moneda", 1, 3) = 'AUD' THEN 'DA' || COALESCE(SUBSTRING(SC."Moneda", 4), '') 
						ELSE COALESCE(SC."Moneda",'')
					END AS "Moneda"
					,SC."Saldo" AS "Saldo"
					,SC."SubCustodio" AS "SubCustodio"
					,SC."Estado" AS "Estado"
					,SC."NombreConcepto" AS "NombreConcepto"
			FROM	:v_SalidaSPCus AS SC
					INNER JOIN :v_TablaNemo AS PrmNemo ON (PrmNemo."Valor" IS NULL OR PrmNemo."Valor" = SC."Nemotecnico")
					INNER JOIN :v_TablaClientes AS PrmCli ON (PrmCli."Valor" IS NULL OR PrmCli."Valor" = SC."DocumentoIdentidad")								
					INNER JOIN :v_TablaMercado AS PrmMcdo ON (PrmMcdo."Valor" IS NULL OR PrmMcdo."Valor" = SC."IdClasificacionTipoInstrumento")
					LEFT JOIN :v_TablaTipoMovimiento TMOV ON (TMOV."ValorOrigen" = SC."IdTipoOperacion" and TMOV."IdEstado" = SC."TipoMovimiento")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" AS CLIENTE ON CLIENTE."DocumentoIdentidad" = SC."DocumentoIdentidad"
			WHERE	SC."IdClasificacionTipoInstrumento" NOT IN ('FM')
			AND CLIENTE."TipoEnrolamiento" = 'FULL'
			ORDER BY TO_BIGINT(REPLACE(REPLACE(SC."DocumentoIdentidad",'-',''),'K','0')), SC."NumeroCuenta", SC."Nemotecnico" ASC, SC."Fecha", SC."Hora", SC."NumeroComprobante" ASC;
					

	ELSE -- IF v_Count > 0 THEN

		p_OUT_TablaSalida = 
			SELECT	0 AS "NumeroCliente"				     
			    	,0 AS "NumeroCuenta"				    	 
			    	,TO_DATE('01-01-1900','DD-MM-YYYY') as "Fecha" 						 
			    	,'' AS "Hora" 							
			    	,0 AS "NumeroComprobante"				
			    	,'' AS "Emisor" 							
			    	,'' AS "TipoMovimiento" 					
			    	,0 AS "Movimiento" 						
					,'' AS "ISIN"								
					,'' AS "Nemotecnico" 						
					,0 AS "Cantidad" 						
					,0 AS "Monto"	 					
					,'' AS "Moneda"        					
					,0 AS "Saldo" 							
					,'' AS "SubCustodio" 						
					,'' AS "Estado"	
					,'' AS "NombreConcepto"						
			FROM 	DUMMY 
			WHERE	1=2;

	END IF;

END;

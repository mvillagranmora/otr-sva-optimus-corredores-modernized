SET SCHEMA "BSCL";	
	
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ArchivoPactosVigentesEbox";

CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Fecha Creacion: 08/05/2024
"BSCL.Normativa.BD.Servicios.NOR::ArchivoPactosVigentesEbox" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
    IN p_IdTipoDocumento   		 	  NVARCHAR(10) DEFAULT 'PACVIGEBOX',
 	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida    TABLE(
        "TipoRegistro"					NVARCHAR(1),
        "FechaEnc"						NVARCHAR(8),
        "CorredorEnc"					NVARCHAR(3),
        "DescripcionEnc"				NVARCHAR(50),
        "FillerEnc"						NVARCHAR(130),
        "TipoCompromiso" 				NVARCHAR(10),
        "IndicadorCobertura" 			NVARCHAR(1),
        "FechaOperacion" 				NVARCHAR(10),
        "NombreCliente" 				NVARCHAR(50),
        "RutCliente" 					NVARCHAR(11),
        "Relacionado" 					NVARCHAR(1), 
        "Mercado" 						NVARCHAR(2), 
        "Nemotecnico" 					NVARCHAR(10), 
    	"Emisor"  						NVARCHAR(10),
        "TipoReajusteInstrumento" 		NVARCHAR(2), 
        "Unidades" 						NVARCHAR(15),
        "NumeroOperacion" 				NVARCHAR(8), 
        "FechaVencimientoPacto" 		NVARCHAR(10),
        "FechaVencimientoInstrumento"	NVARCHAR(10),
        "MontoPacto" 					NVARCHAR(17),
        "TipoMonedaPacto" 				NVARCHAR(2),
        "MontoOperacion" 				NVARCHAR(17), 
        "TasaPacto" 					NVARCHAR(7)
    )
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,v_out_IdInfoTran INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
	
	DECLARE v_IdTipoDocumento				 NVARCHAR(10):= 'PACVIGEBOX';
	DECLARE v_TODOS_CLI,v_TODOS_NEMO		 NVARCHAR(2):='N';
	DECLARE v_NEMO 							 NVARCHAR(40):='';
	DECLARE v_IDCLI				 			 NVARCHAR(40):='0';
	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	DECLARE v_ParametrosSolicitudNemo    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	DECLARE v_TotalRegistros INT := 0;
	
	IF p_IdSolicitudCircular>0 THEN
		
		SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
		FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
		
		SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdDocumento" = v_IdDocumento;
	
		 				
		
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"
			FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
		 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		 	
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" PAR 
						WHERE PAR."IdSolicitudCircular" = p_IdSolicitudCircular;	
			
	END IF;
	
	SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
	if v_Count>0 THEN
		SELECT TOP 1  "Valor" INTO v_IdEmpresa
			FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
			
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"			
			FROM :p_TablaParametros SOL;
			
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM :p_TablaParametros PAR UNION ALL
						 SELECT 'IdTipoDocumento' AS "TipoParametro" , v_IdTipoDocumento AS "Valor"
						 FROM DUMMY;		
			
	END IF;


	v_DatosAgrupado =SELECT  
					  MAX(TRIM(SOL."FINI")) 	AS "FINI"
					, MAX(TRIM(SOL."FTER"))		AS "FTER"
					, MAX(TRIM(SOL."IDCLI"))	AS "IDCLI"
					, MAX(TRIM(SOL."NEMO"))		AS "NEMO"
					FROM :v_Datos SOL;
					

	SELECT TOP 1 "FINI","FTER", "IDCLI","NEMO" INTO v_FechaOperacionDesde,v_FechaOperacionHasta, v_IDCLI, v_NEMO FROM :v_DatosAgrupado;
	
	v_ParametrosSolicitudRut 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	v_ParametrosSolicitudNemo 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	
	
	
		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		v_FechaVersion :=TO_VARCHAR (v_FechaOperacionDesde, 'DD-MM-YYYY');
		
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
  		
	
	SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros".NEXTVAL INTO v_IdTrabajoParametros FROM DUMMY;
			
	INSERT INTO "BSCL.Normativa.BD::GBL.Parametros" ("IdInterno","Tipo","NombreParametro","Valor")
	SELECT :v_IdTrabajoParametros AS "IdInterno",'SOLICITUD', PAR."TipoParametro", PAR."Valor"
		FROM :v_DatosParametros PAR;
			
	v_IdCliente = CAST (TRIM(v_IDCLI) AS BIGINT); 	
	

	p_out_TablaSalida = 
	(SELECT
    	'1' AS "TipoRegistro" 
    	,REPLACE(:v_FechaOperacionDesde, '-', '') AS "FechaEnc"
    	,CCE."CodigoCorredoraFix" AS "CorredorEnc"
    	,'Compromisos de compras y ventas de instrumentos   ' AS "DescripcionEnc"
    	,RPAD('', 130, ' ') AS "FillerEnc"
		,'' AS "TipoCompromiso"
		,'' AS "IndicadorCobertura"	
		,'' AS "FechaOperacion"
		,'' AS "NombreCliente"
		,'' AS "RutCliente"
		,'' AS "Relacionado"
		,'' AS "Mercado"	
		,'' AS "Nemotecnico"
		,'' AS "Emisor"
		,'' AS "TipoReajusteInstrumento"	
		,'' AS "Unidades"
		,'' AS "NumeroOperacion"
		,'' AS "FechaVencimientoPacto"
		,'' AS "FechaVencimientoInstrumento"
		,'' AS "MontoPacto"
		,'' AS "TipoMonedaPacto"	
		,'' AS "MontoOperacion"
		,'' AS "TasaPacto"
    FROM "BSCL.Plataforma.BD::PLA.Empresa" as EMP
	left join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC
	    on "EMP"."IdSocioDeNegocio" ="SOC"."IdSocioDeNegocio"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS CCE
		ON "CCE"."IdSocioDeNegocio" = "SOC"."IdSocioDeNegocio" 
		AND "CCE"."IdBolsa" = 'BCS' -- Esto garantiza que solo se agrega una fila
	UNION
	SELECT 
		PAC."TipoRegistro" 
		,'' AS "FechaEnc"
    	,'' AS "CorredorEnc"
    	,'' AS "DescripcionEnc"
    	,'' AS "FillerEnc"
		,RPAD(LTRIM(PAC."TipoCompromiso"), 10, ' ') AS "TipoCompromiso"
		,TO_VARCHAR(PAC."IndicadorCobertura") AS "IndicadorCobertura"
		,TO_VARCHAR(PAC."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion"
		,RPAD(LTRIM(PAC."NombreCliente"), 50, ' ') AS "NombreCliente"
		,RPAD(LTRIM(PAC."RutCliente"), 11, ' ') AS "RutCliente"
		,PAC."Relacionado"
		,PAC."Mercado"	
		,RPAD(LTRIM(PAC."Nemotecnico"), 10, ' ') AS "Nemotecnico"
		,RPAD(LTRIM(PAC."Emisor"), 10, ' ') AS "Emisor"
		,RPAD(LTRIM(CASE WHEN PAC."TipoReajusteInstrumento" = 'CLP' THEN '$'
				WHEN PAC."TipoReajusteInstrumento" = 'UF' THEN 'UF'
				WHEN PAC."TipoReajusteInstrumento" = 'USD' THEN 'US' 
				WHEN PAC."TipoReajusteInstrumento" = 'EUR' THEN 'EU' ELSE '' END), 2, ' ') AS "TipoReajusteInstrumento"	
		,LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(PAC."Unidades", 15, 4))),'.'), 11, '0') || 
			LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(PAC."Unidades", 15, 4))),'.'), 4, '0' ) AS "Unidades"
		,LPAD(TO_CHAR(ABS(PAC."NumeroOperacion")), 8, '0') AS "NumeroOperacion"
		,PAC."FechaVencimientoPacto" AS "FechaVencimientoPacto"
		,TO_VARCHAR(TO_DATE(PAC."FechaVencimientoInstrumento", 'DD-MM-YYYY'), 'YYYY-MM-DD') AS "FechaVencimientoInstrumento"
		,LPAD(
			SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(PAC."MontoPacto", 15, 2))),'.'), 15, '0') || 
			LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(PAC."MontoPacto", 15, 2))),'.'), 2, '0' ) AS "MontoPacto"
		,RPAD(LTRIM(CASE WHEN PAC."TipoMonedaPacto" = 'CLP' THEN '$'
				WHEN PAC."TipoMonedaPacto" = 'UF' THEN 'UF'
				WHEN PAC."TipoMonedaPacto" = 'USD' THEN 'US' 
				WHEN PAC."TipoMonedaPacto" = 'EUR' THEN 'EU' ELSE '' END), 2, ' ') AS "TipoMonedaPacto"	
		,LPAD(
			SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(PAC."MontoOperacion", 11, 2))),'.'), 15, '0') || 
			LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(PAC."MontoOperacion", 11, 2))),'.'), 2, '0' ) AS "MontoOperacion"
		,LPAD(
			SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(PAC."TasaPacto", 3, 3))),'.'), 3, '0') || 
			LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(PAC."TasaPacto", 5, 4))),'.'), 4, '0' ) AS "TasaPacto"
			
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperacionesPactosVigentes" PAC
	WHERE PAC."FechaOperacion" <= :v_FechaOperacionDesde
	);

	SELECT * FROM :p_out_TablaSalida;
END
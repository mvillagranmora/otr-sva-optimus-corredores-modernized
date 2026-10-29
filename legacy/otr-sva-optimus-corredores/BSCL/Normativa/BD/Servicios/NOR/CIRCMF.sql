SET SCHEMA "BSCL";	
	
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::CIRCMF";

CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: MROJAS - BCS
--  Fecha Creacion: 19/12/2023
"BSCL.Normativa.BD.Servicios.NOR::CIRCMF" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
    IN p_IdTipoDocumento   		 	  NVARCHAR(10) DEFAULT 'CIRCMF',
 	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida    TABLE(
        "Fecha"NVARCHAR(10),
        "Disponible1" NVARCHAR(328),
         "Disponible2" NVARCHAR(1),
         "Disponible3" NVARCHAR(1),
         "Disponible4" NVARCHAR(1),
         "Disponible5"NVARCHAR(108),
        "IdClasificacionTipoInstrumentoModificado" NVARCHAR(6), 
        "Nemotecnico" NVARCHAR(50), 
        "ISIN" NVARCHAR(12), 
        "IdCliente"  DECIMAL(26, 8),
        "CodigoPais" NVARCHAR(2), 
        "SituacionInstrumento" NVARCHAR(2),
        "IdTipoInstrumento" NVARCHAR(10), 
        "FechaVencimiento" NVARCHAR(10), 
        "CantidadUnidades" NVARCHAR(19),
        "ClasificacionRiesgoInstrumento" NVARCHAR(3),
        "TendenciaClasificacionriesgo" NVARCHAR(50),
        "IdMonedaPrincipal" NVARCHAR(3), 
        "Tir" NVARCHAR(20), 
        "CodigoValorizacion" NVARCHAR(2), 
        "BaseTasa"NVARCHAR(3),
        "ValorizacionCierre"  NVARCHAR(20), 
        "CodigoPaisEmisionInstrumento" NVARCHAR(3),
        "FuentePrecios" NVARCHAR(100),
        "Duracion" NVARCHAR(20),
        "Relacionado" NVARCHAR(2),
        "RutCliente" NVARCHAR(9),
        "Digito" NVARCHAR(1),
        "NombrePersona" NVARCHAR(100),
        "TotalRegistros" NVARCHAR(10)
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
	
	DECLARE v_IdTipoDocumento				 NVARCHAR(10):= 'CIRCMF';
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
	


SELECT COUNT(*) + 2 INTO v_TotalRegistros
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCircularCMF" CIR
WHERE CIR."Fecha" = :v_FechaOperacionDesde
AND (CIR."IdEstado" = 'ACT' OR (CIR."IdEstado" = 'NEG' AND CIR."CantidadUnidades" > 0));

	p_out_TablaSalida = 
	(SELECT
		TO_NVARCHAR(:v_FechaOperacionDesde, 'YYYYMMDD') AS "Fecha",
        LPAD(' ', 328, ' ')  AS "Disponible1",
          '1'  AS "Disponible2",
         ''  AS "Disponible3",
         ''  AS "Disponible4",
         '' AS "Disponible5",
         '' AS "IdClasificacionTipoInstrumentoModificado" , 
        '' AS "Nemotecnico", 
        ''AS"ISIN" , 
         0 AS"IdCliente" ,
        ''AS"CodigoPais" , 
        ''AS"SituacionInstrumento" ,
        ''AS"IdTipoInstrumento" , 
        ''AS"FechaVencimiento" , 
         '' AS"CantidadUnidades" ,
        ''AS"ClasificacionRiesgoInstrumento" ,
        ''AS"TendenciaClasificacionriesgo",
        ''AS"IdMonedaPrincipal", 
        '' AS"Tir", 
        ''AS"CodigoValorizacion" , 
         '' AS"BaseTasa" ,
        '' AS"ValorizacionCierre" , 
        ''AS"CodigoPaisEmisionInstrumento" ,
         '' AS "FuentePrecios" ,
         '' AS"Duracion",
        ''AS"Relacionado",
        LPAD(SUBSTRING(DOC."DocumentoIdentidad", 0, 8), 9, 0) AS "RutCliente",
        RIGHT(DOC."DocumentoIdentidad",1) AS "Digito",
	RPAD(LTRIM(PER."RazonSocial"), 100, ' ') AS "NombrePersona",
    '' AS "TotalRegistros"
    FROM "BSCL.Plataforma.BD::PLA.Empresa" as EMP
left join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC
    on "EMP"."IdSocioDeNegocio" ="SOC"."IdSocioDeNegocio"
left join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER
    on "PER"."IdPersona"="SOC"."IdPersona"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON PER."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
UNION
SELECT 
	'' AS "Fecha",
	'' AS "Disponible1",
	'' AS "Disponible2",
	'2' AS "Disponible3",
	'' AS "Disponible4",
	'' AS "Disponible5",
	LPAD(CIR."IdClasificacionTipoInstrumentoModificado", 6, '0') AS "IdClasificacionTipoInstrumentoModificado",
	RPAD(LTRIM(CIR."Nemotecnico"), 50, ' ') AS "Nemotecnico", 
	RPAD(LTRIM(CIR."ISIN"), 12, ' ') AS "ISIN",
	CIR."IdCliente",
	RPAD(LTRIM(COALESCE(CIR."CodigoPais", ' ')), 2, ' ') AS "CodigoPais",
	RPAD(CIR."SituacionInstrumento", 2, ' ') AS "SituacionInstrumento",
	RPAD(LTRIM(COALESCE(CIR."IdTipoInstrumento", ' ')), 10, ' ') AS "IdTipoInstrumento",
	CASE 
		WHEN TO_NVARCHAR(CIR."FechaVencimiento") = '0001-01-01' THEN RPAD('', 10, ' ')
		ELSE LPAD(TO_NVARCHAR(TO_CHAR(CIR."FechaVencimiento", 'DD-MM-YYYY')), 10, ' ')
	END AS "FechaVencimiento",
	LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(CIR."CantidadUnidades", 19, 4))), '.'), 15, '0') || LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(CIR."CantidadUnidades", 19, 4))), '.'), 4, '0') 
	AS "CantidadUnidades",
	RPAD(LTRIM(CIR."ClasificacionRiesgoInstrumento"), 3, ' ') AS "ClasificacionRiesgoInstrumento",
	RPAD(LTRIM(COALESCE(CIR."TendenciaClasificacionriesgo", ' ')), 50, ' ') AS "TendenciaClasificacionriesgo",
	RPAD(LTRIM(CIR."IdMonedaPrincipal"), 3, ' ') AS "IdMonedaPrincipal",
	LPAD(CASE 
	    	WHEN CIR."Tir" < 0 THEN CONCAT('-', LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(CIR."Tir", 16, 4))), '.'), 15, '0'))
	    	ELSE CONCAT('+', LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(CIR."Tir", 16, 4))), '.'), 15, '0'))
		END, 16, '0') || LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(CIR."Tir", 16, 4))), '.'), 4, '0') 
	AS "Tir",
	RPAD(LTRIM(COALESCE(CIR."CodigoValorizacion", ' ')), 2, ' ') AS "CodigoValorizacion", LPAD(CASE WHEN CIR."BaseTasa" = 30 THEN '030' WHEN CIR."BaseTasa" = 0 THEN '000' ELSE TO_NVARCHAR(CIR."BaseTasa") END, 3, '0') AS "BaseTasa",
	LPAD(CASE 
	 		WHEN CIR."ValorizacionCierre" < 0 THEN CONCAT('-', LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(CIR."ValorizacionCierre", 16, 4))), '.'), 15, '0'))
	 		ELSE CONCAT('+', LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(CIR."ValorizacionCierre", 16, 4))), '.'), 15, '0'))
	 	END, 16, '0') || LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(CIR."ValorizacionCierre", 16, 4))), '.'), 4, '0')
	AS "ValorizacionCierre",
    RPAD(LTRIM(COALESCE(CIR."CodigoPaisEmisionInstrumento", ' ')), 2, ' ') AS "CodigoPaisEmisionInstrumento",
	RPAD(LTRIM(COALESCE(CIR."FuentePrecios", ' ')), 100, ' ') AS "FuentePrecios",
	LPAD(CASE 
	    	WHEN COALESCE(CIR."Duration", 0) < 0 THEN CONCAT('-', LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(COALESCE(CIR."Duration", 0), 20, 4))), '.'), 15, '0'))
	    	ELSE CONCAT('+', LPAD(SUBSTR_BEFORE(TO_CHAR(ABS(TO_DECIMAL(COALESCE(CIR."Duration", 0), 20, 4))), '.'), 15, '0'))
		END, 16, '0') || LPAD(SUBSTR_AFTER(TO_CHAR(ABS(TO_DECIMAL(COALESCE(CIR."Duration", 0), 20, 4))), '.'), 4, '0') 
	AS "Duracion",
	RPAD(LTRIM(CIR."Relacionado"), 2, ' ') AS "Relacionado",
	LPAD(CIR."RutCliente",9, '0') AS "RutCliente",
	LPAD(CIR."Digito",1,'0') AS "Digito",
	RPAD(LTRIM(CIR."NombrePersona"), 100, ' ') AS "NombrePersona",
	'' AS "TotalRegistros"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCircularCMF" CIR
WHERE CIR."Fecha" = :v_FechaOperacionDesde
AND (CIR."IdEstado" = 'ACT' OR (CIR."IdEstado" = 'NEG' AND CIR."CantidadUnidades" > 0))

UNION
SELECT
    '' AS "Fecha",
  LPAD(' ', 328, ' ') AS "Disponible1",
    ''  AS "Disponible2",
      ''  AS "Disponible3",
        '3'  AS "Disponible4",
       LPAD(' ', 108, ' ')  AS "Disponible5",
    '' AS "IdClasificacionTipoInstrumentoModificado",
    '' AS "Nemotecnico", 
    '' AS "ISIN",
    0 AS "IdCliente",
    '' AS "CodigoPais",
    '' AS "SituacionInstrumento",
    '' AS "IdTipoInstrumento", 
    '' AS "FechaVencimiento",
    '' AS "CantidadUnidades",
    '' AS "ClasificacionRiesgoInstrumento",
    '' AS "TendenciaClasificacionriesgo",
    '' AS "IdMonedaPrincipal",
    '' AS "Tir",
    '' AS "CodigoValorizacion",
    '' AS "BaseTasa",
    '' AS "ValorizacionCierre",
    '' AS "CodigoPaisEmisionInstrumento",
    '' AS "FuentePrecios",
    '' AS "Duracion",
    '' AS "Relacionado",
    '' AS "RutCliente",
    '' AS "Digito",
    '' AS "NombrePersona",
       LPAD(CAST(v_TotalRegistros AS NVARCHAR(10)) ,10,'0') AS "TotalRegistros"
    FROM DUMMY -- Esto garantiza que solo se agrega una fila
);

	SELECT * FROM :p_out_TablaSalida;
END
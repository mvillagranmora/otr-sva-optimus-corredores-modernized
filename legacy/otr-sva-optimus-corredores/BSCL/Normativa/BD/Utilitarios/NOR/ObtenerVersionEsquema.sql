SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema";

CREATE FUNCTION
-- 
--  Proposito: Obtener la version correspondiente al esquema del tipo de documento
--  Autor: CFLORES
--  Fecha Creacion: 22/08/2016
--  
"BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema" (
	p_IdTipoDocumento   NVARCHAR(10),
	p_FechaDesde		NVARCHAR(10) --'DD-MM-YYYY'
)
	RETURNS TABLE (p_FechaDesde NVARCHAR(10),p_out_Esquema NVARCHAR(10),p_out_Version NVARCHAR(10),p_out_FechaDatosDesde DATE,p_out_FechaDatosHasta DATE)
--	RETURNS p_out_Esquema NVARCHAR(10)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	DECLARE v_IdNormativa 												NVARCHAR(100) DEFAULT '';
	DECLARE v_count,v_countVigenciaDefinida,v_countVigenciaInfinita		INT:=0;
	DECLARE v_Version, v_Esquema  										NVARCHAR(10):='';	
	
	v_Version:=''; 	 
		--------------------
	--LOGICA 
	--------------------
	v_VigenciaDefinida=SELECT '' AS "Esquema",'' AS "Version",'0001-01-01' AS "FechaDatosDesde",'0001-01-01' AS "FechaDatosHasta" FROM DUMMY;
	v_VigenciaInfinita=SELECT * FROM :v_VigenciaDefinida;
	IF  p_FechaDesde='' OR p_FechaDesde='0001-01-01' THEN
		--SIN VERSION
	ELSE
	  	/* Obtener esquema*/
	  	
	  	IF p_IdTipoDocumento<>'' THEN
	  	  	SELECT TOP 1 "Esquema" INTO v_Esquema
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;
		END IF;
		
	  	/* Seleccion de la version segun vigencia del esquema*/
	  	--vigencia definida
		SELECT COUNT(1) INTO v_countVigenciaDefinida FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE ("Esquema"=v_Esquema OR v_Esquema='')
		AND TO_DATE (p_FechaDesde, 'DD-MM-YYYY') BETWEEN "FechaDatosDesde" and "FechaDatosHasta";
		
		--vigencia infinita
		SELECT COUNT(1) INTO v_countVigenciaInfinita  FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE ("Esquema"=v_Esquema  OR v_Esquema='')
		AND TO_DATE (p_FechaDesde, 'DD-MM-YYYY') > "FechaDatosDesde" and ("FechaDatosHasta"='0001-01-01'  OR "FechaDatosHasta" IS NULL);
	
		IF 	v_countVigenciaDefinida > 0 THEN 								--vigencia definida
			
		--INSERT INTO :v_VigenciaDefinida ("Esquema","Version")
		v_VigenciaDefinida=
			SELECT "Esquema",MIN("Version") AS "Version","FechaDatosDesde","FechaDatosHasta"  FROM "BSCL.Normativa.BD::DOC.Esquema"
			WHERE  ("Esquema"=v_Esquema  OR v_Esquema='')
			AND TO_DATE (p_FechaDesde, 'DD-MM-YYYY') BETWEEN "FechaDatosDesde" and "FechaDatosHasta"
			GROUP BY "Esquema","FechaDatosDesde","FechaDatosHasta";
		
		END IF;
		IF 	v_countVigenciaInfinita > 0 AND v_countVigenciaDefinida = 0 THEN --vigencia infinita
			v_VigenciaInfinita=
			SELECT "Esquema",MIN("Version") AS "Version","FechaDatosDesde","FechaDatosHasta" FROM "BSCL.Normativa.BD::DOC.Esquema"
			WHERE  ("Esquema"=v_Esquema  OR v_Esquema='')
			AND TO_DATE (p_FechaDesde, 'DD-MM-YYYY') > "FechaDatosDesde" and ("FechaDatosHasta"='0001-01-01' OR "FechaDatosHasta" IS NULL)
			GROUP BY "Esquema","FechaDatosDesde","FechaDatosHasta";
		END IF;
			
	END IF;
 	
 	
	RETURN SELECT :p_FechaDesde AS p_FechaDesde,SALIDA."Esquema" AS p_out_Esquema , SALIDA."Version" AS p_out_Version,SALIDA."FechaDatosDesde" AS p_out_FechaDatosDesde, SALIDA."FechaDatosHasta" AS p_out_FechaDatosHasta
	FROM
		(
			SELECT "Esquema" , "Version","FechaDatosDesde","FechaDatosHasta"
			 FROM :v_VigenciaDefinida
		  UNION ALL
		   SELECT I."Esquema", I."Version",I."FechaDatosDesde",I."FechaDatosHasta"
		    FROM :v_VigenciaInfinita AS I
			   LEFT OUTER JOIN :v_VigenciaDefinida AS D ON (I."Esquema"=D."Esquema")
			    WHERE D."Esquema" IS NULL
		) AS SALIDA WHERE SALIDA."Esquema" <>'';
 	
 	 
END;
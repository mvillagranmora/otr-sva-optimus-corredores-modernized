SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Custodia.BD.Reglas::ServicioCumpleRequisitosVencimientoSimultanea";

CREATE PROCEDURE
-- 
--  Proposito: Validar 
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015  
--     
"BSCL.Custodia.BD.Reglas::ServicioCumpleRequisitosVencimientoSimultanea"(
	-- Parametros entrada
	IN 	p_FechaSistema				NVARCHAR(40),

	-- Salidas para mensajes de errores
	OUT p_out_SimultaneasRech     	TABLE ("FolioCartera" BIGINT),
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
	OUT p_out_resultadoRegla        NVARCHAR(100)   --OK=Cumple todos los requisitos,			 								  	   
                                                   --RECH=Alguna de las reglas detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100);
	DECLARE v_ErroresContador 		INT; 
	DECLARE v_i                     INT;
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);	

		IF v_i = v_ErroresContador THEN
			p_out_resultadoRegla := 'RECH';
		ELSE
			p_out_resultadoRegla := 'ADVE';
		END IF;		 					 	
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
								SELECT * from :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_SimultaneasRech	= SELECT 0 as "FolioCartera" FROM dummy where 1=2;
	p_out_ErroresDetalle	= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy where 1=2;
	
	BEGIN	
		DECLARE v_TableSimultaneas		TABLE ("FolioCartera" BIGINT);
		DECLARE v_ARR_MOV               BIGINT ARRAY;
		DECLARE v_FolioCartera			BIGINT;
		
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'CARTERA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
			
		v_TableSimultaneas =				
		SELECT "FolioCartera"
		  FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes"
		 WHERE "IdEstado" = 'VIG'
		   AND "FechaVencimientoOperacion" = :p_FechaSistema;

		
		v_ARR_MOV := ARRAY_AGG(:v_TableSimultaneas."FolioCartera" ORDER BY "FolioCartera" ASC); 
		
		FOR v_i IN 1 .. CARDINALITY(:v_ARR_MOV) DO 
			v_FolioCartera := :v_ARR_MOV[:v_i];		
			v_hashmsg = SELECT 'FolioCartera' as "Codigo", CAST(v_FolioCartera as NVARCHAR) as "Valor" FROM DUMMY;
			

            --
			--1- Validar que el cliente posea "Ficha cliente"
            
            CONTINUE;
            
		END FOR;
		
		SELECT COUNT(1) INTO v_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :v_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
    END;
	
END;

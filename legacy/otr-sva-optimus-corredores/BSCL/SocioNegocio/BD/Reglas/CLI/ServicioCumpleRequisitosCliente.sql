SET SCHEMA "BSCL";
   
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Reglas::CLI.ServicioCumpleRequisitosCliente";

CREATE PROCEDURE
-- 
--  Proposito: Validar datos del cliente.
--  Autor: BCS
--  Fecha Creacion: 01/07/2015  
--   
"BSCL.SocioNegocio.BD.Reglas::CLI.ServicioCumpleRequisitosCliente" (
	IN  p_IdCliente           BIGINT,
    
	-- Parametros utilizados por el motor de reglas
	IN  p_IdEjecucion		  BIGINT,  --id entregado por el motor de reglas,0 = omite
    OUT p_out_ErroresDetalle  TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_resultadoRegla  NVARCHAR(40)   --OK=Cumple todos los requisitos,			 								  	   
                                             --RECH=Alguna de las reglas detienen la operacion
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
READS SQL DATA AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE v_ErroresContador INT; 
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--segun sea el caso se puede enviar otro codigo de salida
		p_out_resultadoRegla := 'RECH';			 	
	END;
		
	p_out_resultadoRegla  := 'OK';
	p_out_ErroresDetalle  = select '' as "Codigo",'' as "Descripcion" from dummy where (1=2);
	
	--------------------
	--LOGICA 
	--------------------

  -- cliente existe
  -- portafolio existe
  -- portafolio asignado a cliente
  -- cliente bloqueado
  -- portafolio bloqueado
	
	BEGIN
		DECLARE v_PorcentajeDivergencia DECIMAL(26,8);
		DECLARE v_Quantity            BIGINT;
		DECLARE v_indice              BIGINT;
		DECLARE v_ARR_CodigoError     NVARCHAR(40) ARRAY;
		DECLARE v_ARR_ValorParametro  NVARCHAR(40) ARRAY;
		DECLARE v_ARR_NombreParametro NVARCHAR(40) ARRAY;
		DECLARE v_CodigoError         NVARCHAR(40);
		DECLARE v_ValorParametro      NVARCHAR(5000);
		DECLARE v_NombreParametro     NVARCHAR(40);
		DECLARE v_MensajeRegla        NVARCHAR(520);
		DECLARE v_TablaRequisitos     TABLE ("Entidad" NVARCHAR(40), "Prioridad" INT, 
											 "Cumple" BIGINT, "Codigo" NVARCHAR(40), 
											 "ValorParametro" NVARCHAR(5000), "NombreParametro" NVARCHAR(40));
		DECLARE v_RequisitoInvalidos  TABLE ("Entidad" NVARCHAR(40), "Prioridad" INT);
		DECLARE v_TablaCodigoError    TABLE ("Codigo" NVARCHAR(40), "ValorParametro" NVARCHAR(5000), "NombreParametro" NVARCHAR(40));
		
		
		v_TablaRequisitos    = SELECT '' as "Entidad", 0 as "Prioridad", 0 as "Cumple",'' as "Codigo", 
									  '' as "ValorParametro", '' as "NombreParametro" FROM dummy WHERE (1=2);
		v_RequisitoInvalidos = SELECT '' as "Entidad", 0 as "Prioridad" FROM dummy WHERE (1=2);
		v_TablaCodigoError   = SELECT '' as "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM dummy; 	

		
		-- recupero los datos de la bd 
		consulta_cli = SELECT CLI."IdEstado",
					 		  CLI."EsEnrolado"
					   FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI 
					   WHERE CLI."IdCliente" = COALESCE(:p_IdCliente,-1);
			  
		v_TablaRequisitos = 
		SELECT 'CLIENTE' as "Entidad", 
		       1 as "Prioridad", 
		       CASE WHEN (COUNT("IdEstado") < 1 ) THEN 0 ELSE 1 END as "Cumple",
		       'CLIENTE_NO_EXISTE' as "Codigo",
		       TO_VARCHAR(:p_IdCliente) as "ValorParametro",
		       'IdCliente' as "NombreParametro"   
	    FROM :consulta_cli
	    UNION ALL SELECT "Entidad","Prioridad","Cumple","Codigo","ValorParametro","NombreParametro" FROM :v_TablaRequisitos;
	     	
		v_TablaRequisitos = 
		SELECT 'CLIENTE' as "Entidad", 
		       2 as "Prioridad", 
		       CASE WHEN ("IdEstado" <> 'ACT') THEN 0 ELSE 1 END as "Cumple",
		       'CLIENTE_BLOQUEADO' as "Codigo",
		       '' as "ValorParametro",
		       '' as "NombreParametro"   
	    FROM :consulta_cli
	    UNION ALL SELECT "Entidad","Prioridad","Cumple","Codigo","ValorParametro","NombreParametro" FROM :v_TablaRequisitos;

	    v_TablaRequisitos = 
		SELECT 'CLIENTE' as "Entidad", 
		       3 as "Prioridad", 
		       CASE WHEN ("EsEnrolado" <> 'S') THEN 0 ELSE 1 END as "Cumple",
		       'CLIENTE_AUTORIZACION_PENDIENTE' as "Codigo",
		       '' as "ValorParametro",
		       '' as "NombreParametro"   
	    FROM :consulta_cli
	    UNION ALL SELECT "Entidad","Prioridad","Cumple","Codigo","ValorParametro","NombreParametro" FROM :v_TablaRequisitos;
	    
	    
		v_RequisitoInvalidos = 
		SELECT "Entidad", MIN("Prioridad") as "Prioridad" FROM :v_TablaRequisitos
		WHERE "Cumple" = 0 
	    GROUP BY "Entidad";   
	     
	     
	    SELECT COUNT("Entidad") INTO v_Quantity FROM :v_RequisitoInvalidos;  
	    IF v_Quantity > 0 THEN
			v_TablaCodigoError =
			SELECT "Codigo", "ValorParametro", "NombreParametro"
			  FROM :v_TablaRequisitos as tr
			 INNER JOIN :v_RequisitoInvalidos as ri ON tr."Entidad" = ri."Entidad"
			 WHERE tr."Prioridad" = ri."Prioridad";
			
			v_ARR_CodigoError      := ARRAY_AGG(:v_TablaCodigoError."Codigo" ORDER BY "Codigo" DESC);
			v_ARR_ValorParametro   := ARRAY_AGG(:v_TablaCodigoError."ValorParametro" ORDER BY "Codigo" DESC); 
			v_ARR_NombreParametro  := ARRAY_AGG(:v_TablaCodigoError."NombreParametro" ORDER BY "Codigo" DESC); 
			FOR v_indice IN 1 .. CARDINALITY(:v_ARR_CodigoError) DO 
				v_CodigoError     := :v_ARR_CodigoError[:v_indice];
				v_ValorParametro  := :v_ARR_ValorParametro[:v_indice];
				v_NombreParametro := :v_ARR_NombreParametro[:v_indice];

				v_hashmsg = SELECT v_NombreParametro as "Codigo", COALESCE(v_ValorParametro,'') as "Valor" FROM dummy; 
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_CodigoError,:v_hashmsg, :v_MensajeRegla);		
 				p_out_ErroresDetalle =  select :v_CodigoError as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;

			END FOR;
			
	    	SIGNAL SALIDA_CONTROLADA;
	    END IF; 					

	END;
	
END;

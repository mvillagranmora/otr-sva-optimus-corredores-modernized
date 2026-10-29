SET SCHEMA "BSCL";
   
DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosAnularOperacionRF";

--* ------------------------------------------------------------------------
--* Nombre .....: ServicioCumpleRequisitosCrearOperacionRF.sql
--* Modulo .....: MesaRF
--* F. Creación : Agosto 2016
--* Proposito ..: Reglas de validación para anulacion de una operación de la 
--*               Mesa RF.
--* ------------------------------------------------------------------------
CREATE PROCEDURE "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosAnularOperacionRF"(

--- 										"IdRegistroOpe"		BIGINT
	 IN p_IdEmpresa				BIGINT,
	 IN p_DatosOperacion     	TABLE (  "IdRegistroOpe"	BIGINT
			                            ,"Intermesa"		NVARCHAR(1)
										,"IdMesaOrigen"		BIGINT
										,"IdMesaDestino"	BIGINT
										,"IdUsuario"		BIGINT
										,"FechaOperacion" 	NVARCHAR(10)
										,"IdEstado"			NVARCHAR(3)),
     
	OUT p_out_SalidaDetalle     TABLE (	
										"IdRegistroOpe" 		BIGINT
										,"Resultado" 		NVARCHAR(40)
										,"Codigo" 			NVARCHAR(40)
										,"ValorParametro" 	NVARCHAR(100)
										,"NombreParametro" 	NVARCHAR(40)
										),
	OUT p_out_Salida        	TABLE (	
										"IdRegistroOpe" 	BIGINT 
										,"Resultado" 	NVARCHAR(40)
										) 	-- OK 	= Cumple todos los requisitos
											-- ADV	= Advertencia
											-- AUT	= Autorizacion							  	   
                                            -- ERR	= Error
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

	DECLARE v_FechaSistema NVARCHAR(10);
	
	SELECT 	COALESCE("Valor",'') 
	INTO 	v_FechaSistema 
	FROM 	"BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE 	"IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
       
	v_matriz_validaciones =	SELECT ope."IdRegistroOpe"
								 --* PARA TODAS LAS OPERACIONES
								 , 1 AS "USUARIO_NO_AUTORIZADO_A_ANULAR_OPERACION"
								 , 1 AS "MODULO_CERRADO"
								 , 0/*CASE ope."Intermesa"
								 		WHEN 'S' THEN 1 
								 		ELSE 0
								 	END*/ AS "OPERACION_INTERMESA_EXISTENTE"--si hay una operacion relacionada por intermesa
								 ,1 AS "MESA_ORIGEN_CERRADA"
								 ,1 AS "OPERACION_INVALIDA"
                                 
 							  FROM :p_DatosOperacion ope;
 		
 							

	v_TablaRequisitos =	
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."OPERACION_INTERMESA_EXISTENTE" = 0 THEN 1 
					WHEN "IdMesaOrigen" = "IdMesaDestino" THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'OPERACION_INTERMESA_EXISTENTE' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM 	:p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."MODULO_CERRADO" = 0 THEN 1 
					WHEN "TipoEstadoModulo" != 'C' THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'UI_MODULO_CERRADO' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM :p_DatosOperacion ope INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" mes ON (ope."IdMesaOrigen" = mes."IdMesa")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."MESA_ORIGEN_CERRADA" = 0 THEN 1 
					WHEN "IdEstadoMesa" != 'C' THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'MESA_ORIGEN_CERRADA' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM :p_DatosOperacion ope INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" mes ON (ope."IdMesaOrigen" = mes."IdMesa")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		SELECT ope."IdRegistroOpe"
			,CASE 
				WHEN ope."IdUsuario" = cOpe."IdUsuarioCreacion" OR ope."IdUsuario" = usuSup."IdUsuario" THEN 1
				ELSE 4 END 								AS "IdResultado"
			,'USUARIO_NO_AUTORIZADO_A_ANULAR_OPERACION' 					AS "Codigo"
  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) AS "ValorParametro"
  			,'IdOperacion' 								AS "NombreParametro"
		FROM :p_DatosOperacion ope 
			INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" cOpe ON (ope."IdRegistroOpe" = cOpe."IdOperacion")
			INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu ON (cOpe."IdUsuarioCreacion" = usu."IdUsuario" AND usu."TipoEntidad" = 'EMP')
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp ON (usu."IdEntidad" = emp."IdEmpleado")
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuSup ON (usuSup."IdEntidad" = emp."IdEmpleadoSupervisor")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."OPERACION_INVALIDA" = 0 THEN 1 
					WHEN ope."IdEstado"='VIG' and ope."FechaOperacion" = :v_FechaSistema THEN 1
					WHEN ope."IdEstado"='PEN' THEN 1 
				 	ELSE 4 END 									AS "IdResultado"
	  			,CASE 
					WHEN ope."IdEstado"='VIG' and ope."FechaOperacion" != :v_FechaSistema THEN 'FECHA_OPERACION_ANULACION_INVALIDA'
					WHEN ope."IdEstado"!='PEN' THEN 'ESTADO_OPERACION_ANULACION_INVALIDA' 
				 	ELSE '' END 			 						AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 									AS "NombreParametro"
		FROM :p_DatosOperacion ope , :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		;


	p_out_SalidaDetalle = 
		SELECT 	tb."IdRegistroOpe"
	    		,CASE 	"IdResultado"
						WHEN 1 THEN 'OK'
						WHEN 2 THEN 'ADV'
						WHEN 3 THEN 'AUT'
						WHEN 4 THEN 'ERR'
				END AS "Resultado"
				,tb."Codigo" 
				,tb."ValorParametro" 
				,tb."NombreParametro" 
		FROM 	:v_TablaRequisitos tb
		WHERE 	"IdResultado" <> 1
		ORDER BY "IdRegistroOpe";
						
	p_out_Salida = 	
		SELECT 	"IdRegistroOpe"
				,CASE 	MAX("IdResultado")
						WHEN 1 THEN 'OK'
						WHEN 2 THEN 'ADV'
						WHEN 3 THEN 'AUT'
						WHEN 4 THEN 'ERR'
				END AS "Resultado"
		FROM 	:v_TablaRequisitos 
		GROUP BY "IdRegistroOpe"
		ORDER BY "IdRegistroOpe";	
END;

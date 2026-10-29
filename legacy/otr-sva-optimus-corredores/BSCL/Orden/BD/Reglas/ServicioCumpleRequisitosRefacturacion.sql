SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosRefacturacion";
CREATE PROCEDURE
-- 
--  Proposito: VALIDACIONES DEL SERVICIO REFACTURACION
--  Autor: PROJO - BCS
--  Fecha Creacion: 15/01/2016
--     

"BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosRefacturacion"(
	-- Parametros entrada
	IN p_IdEmpresa                        	BIGINT,
	
	IN p_Asignaciones	TABLE(
						"IdTransaccion" 	BIGINT,
						"CantidadAsignada"		DECIMAL(26,8),
						"ValorRescateAsignado"	DECIMAL(26,8)
						),
	IN p_Transacciones	TABLE(
							"IdTransaccion" 			BIGINT,
							"CantidadRefacturar"		DECIMAL(26,8),
							"ValorRescateRefacturar"	DECIMAL(26,8)
						),
	-- Salidas para mensajes de errores
	OUT p_out_SalidaDetalle     TABLE ("IdTransaccion" BIGINT,"Resultado" NVARCHAR(40), "Codigo" nvarchar(40),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)),
	OUT p_out_Salida        	TABLE ("IdTransaccion" BIGINT,"Resultado" NVARCHAR(40),"OK" BIGINT,"ADVERTENCIA" BIGINT,"AUTORIZACION" BIGINT,"RECHAZO" BIGINT)  



) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA
	AS
BEGIN		
	--VALIDACIONES EN POSITIVO --PALABRAS CLAVE: COINCIDE, EXISTE, MAYOR_CERO, CORRECTO, DISPONIBLE
	
	--********************** VALIDACIONES *********************************** 	
	v_asignacionesTot = select 
							 sum("CantidadAsignada") as  "TotalAsignado"
							,sum("ValorRescateAsignado") as "TotalRescateAsignado"
							,"IdTransaccion"
						from :p_asignaciones
						group by "IdTransaccion";
						
	v_matriz_validaciones =	SELECT 	asig."IdTransaccion" ,
									1 	as "CANTIDAD_TOTAL_ASIGNADA_NO_VALIDA"
									
							FROM :v_asignacionesTot asig
							INNER JOIN :p_Transacciones tran on asig."IdTransaccion" = tran."IdTransaccion";
	
	
	

	v_TablaRequisitos =
	SELECT tt."IdTransaccion",
		CASE WHEN val."CANTIDAD_TOTAL_ASIGNADA_NO_VALIDA" = 0 OR asig."TotalAsignado" = tt."CantidadRefacturar" AND asig."TotalRescateAsignado" = tt."ValorRescateRefacturar" THEN 'OK' ELSE 'RECHAZO' END as "IdResultado", 
		'CANTIDAD_TOTAL_ASIGNADA_NO_VALIDA' as "Codigo", CAST(tt."IdTransaccion" AS NVARCHAR(100)) as "ValorParametro", 'IdTransaccion' as "NombreParametro"
	FROM :v_asignacionesTot asig
		INNER JOIN :p_Transacciones tt on asig."IdTransaccion" = tt."IdTransaccion"
		, :v_matriz_validaciones val
	WHERE asig."IdTransaccion" = val."IdTransaccion";

	
	p_out_SalidaDetalle =	SELECT 
								tb."IdTransaccion",
								"IdResultado" AS "Resultado",
								tb."Codigo" ,
								tb."ValorParametro" ,
								tb."NombreParametro" 
							FROM :v_TablaRequisitos tb
							WHERE "IdResultado" != 'OK'
							ORDER BY "IdTransaccion";
	
	v_count2 = SELECT "IdTransaccion",
		count(CASE WHEN "IdResultado" = 'OK' THEN 1 ELSE NULL END) as "OK",
		count(CASE WHEN "IdResultado" = 'ADVERTENCIA' THEN 1 ELSE NULL END) as "ADVERTENCIA",
		count(CASE WHEN "IdResultado" = 'AUTORIZACION' THEN 1 ELSE NULL END) as "AUTORIZACION",
		count(CASE WHEN "IdResultado" = 'RECHAZO' THEN 1 ELSE NULL END) as "RECHAZO"
		FROM :v_TablaRequisitos 
		group by "IdTransaccion";
	
	p_out_Salida = 	SELECT "IdTransaccion",
						CASE
							WHEN "RECHAZO" > 0 THEN 'RECHAZO'
							WHEN "AUTORIZACION" > 0 THEN 'AUTORIZACION'
							WHEN "ADVERTENCIA" > 0 THEN 'ADVERTENCIA'
							WHEN "OK" > 0 THEN 'OK'
							ELSE 'OK'
						END AS "Resultado","OK","ADVERTENCIA","AUTORIZACION","RECHAZO"
						FROM :v_count2
						ORDER BY "IdTransaccion";

END;

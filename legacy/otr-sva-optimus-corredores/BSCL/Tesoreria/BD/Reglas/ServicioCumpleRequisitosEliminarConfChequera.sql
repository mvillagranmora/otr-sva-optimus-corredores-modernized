SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarConfChequera";

CREATE PROCEDURE
-- 
--  Proposito: Validar si movimientos cumplen con los requisitos solicitados
--  Autor: CES - BCS
--  Fecha Creacion: 22/02/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarConfChequera"(
	-- Parametros entrada
	IN p_IdConfiguracionChequera		BIGINT,
	
	IN  p_IdEjecucion		  			BIGINT,  --id entregado por el motor de reglas,0 = omite

	OUT p_out_ErroresDetalle    TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
	OUT p_out_ResultadoRegla  NVARCHAR(40), --'OK' si se cumple la validacion , RECH: Rechazado
    OUT p_out_MensajeRegla    NVARCHAR(520) --Solo de uso interno
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE LOGICA
	--------------------
 	DECLARE v_Count BIGINT;
    DECLARE v_Codigo NVARCHAR(40);
    
    
	--------------------
	--DECLARE SALIDA
	--------------------	
	DECLARE v_codigoMensaje NVARCHAR(40) :='REGLA_OK';
	DECLARE v_tablaContador TABLE ("Contador" BIGINT, "Tabla" NVARCHAR(520));
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		DECLARE v_hashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
		if v_codigoMensaje = 'REGLA_OK'	then
			p_out_ResultadoRegla := 'OK';	
			p_out_MensajeRegla   := 'OK++';		
		else
			--Arma el mensaje de salida de acuerdo a codigo de mensaje y los valores pasados
			p_out_ResultadoRegla := 'RECH';
			v_hashmsg = select 'IdCaja' as "Codigo",p_IdConfiguracionChequera as "Valor"   from dummy;		

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_codigoMensaje,:v_hashmsg, :p_out_MensajeRegla);
			p_out_ErroresDetalle =  SELECT :v_codigoMensaje  AS "Codigo",:p_out_MensajeRegla AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		end if;					 	
	END;	
		
	--------------------
	--LOGICA 
	--------------------
	
	v_tablaContador = SELECT 0 AS "Contador", '' AS "Tabla" FROM DUMMY WHERE 1=2;
	v_Count := 0;	   	
	
	--TABLA CHEQUERA
	v_tablaContador = 	SELECT COUNT("IdConfiguracionChequera") AS "Contador", 'BSCL.Tesoreria.BD::TES.Chequera' AS "Tabla"
   					 	FROM "BSCL.Tesoreria.BD::TES.Chequera"
    					WHERE "IdConfiguracionChequera" = p_IdConfiguracionChequera;
    
    SELECT COUNT("Contador") INTO v_Count FROM :v_tablaContador WHERE "Contador" <> 0;

	IF (:v_Count <> 0) THEN
 		v_codigoMensaje := 'CONFIGURACION_CHEQUERA_UTILIZADA';
    	SIGNAL SALIDA_CONTROLADA;				     
 	END IF;
 	 	
	--------------------
	--TERMINO OK  
	--------------------
	v_codigoMensaje := 'REGLA_OK';
	SIGNAL SALIDA_CONTROLADA;
END;

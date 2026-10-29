SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario";

CREATE PROCEDURE
-- 
--  Proposito: Traduce desde un codigo a un mensaje de acuerdo al idioma del sistema
--  Autor: - BCS
--  Fecha Creacion: 26/10/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (
	IN P_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520)),
    OUT p_out_TablaMensajes		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN
		
	DECLARE v_CodLenguaje 			NVARCHAR(40) := 'CL'; --TODO: leer desde tabla configuracion
	DECLARE v_SinTraduccion 		NVARCHAR(520):= 'Sin Traducción';
	DECLARE v_TablaMensajes			TABLE ("IdRegistro" BIGINT, "CodigoMensaje" NVARCHAR(100), "Mensaje" NVARCHAR(520));
	DECLARE v_TablaDatos 			TABLE ("IdRegistro" BIGINT, "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520));
	DECLARE v_Registro				INTEGER;
	DECLARE v_RegistroDatos			INTEGER;
	DECLARE v_TolalRegistros		INTEGER;
	DECLARE v_TolalRegistrosDatos	INTEGER;
	DECLARE v_Mensaje				NVARCHAR(520);
	DECLARE v_CodigoMensaje			NVARCHAR(100);
	DECLARE v_CodigoDato			NVARCHAR(100);
	DECLARE v_ValorDato				NVARCHAR(520);
	DECLARE v_Quantity				BIGINT;
	
	
	v_Quantity := 0;
	
	SELECT COUNT("Descripcion") INTO v_Quantity
	FROM "BSCL.Plataforma.BD::PLA.Diccionario" 
	WHERE 	"CodDiccionario" = 'SIN_TRADUCCION' AND
			"CodLenguaje" = v_CodLenguaje;
	
	IF (:v_Quantity > 0) THEN
		SELECT "Descripcion" INTO v_SinTraduccion
		FROM "BSCL.Plataforma.BD::PLA.Diccionario" 
		WHERE 	"CodDiccionario" = 'SIN_TRADUCCION' AND
				"CodLenguaje" = v_CodLenguaje;
	END IF;
	
	v_TablaMensajes = 	SELECT DISTINCT
						ROW_NUMBER() over (order by "CodigoMensaje") 	AS "IdRegistro",
						"CodigoMensaje" 								AS "CodigoMensaje",
						IFNULL("Descripcion", "CodigoMensaje"||' '||v_SinTraduccion ) AS "Mensaje"
						FROM 
						(SELECT DISTINCT "CodigoMensaje", "Descripcion" FROM :p_TablaCodigosMensajes
						Left OUTER Join "BSCL.Plataforma.BD::PLA.Diccionario" 
						On "CodDiccionario" = :p_TablaCodigosMensajes."CodigoMensaje");
							
	SELECT COUNT(1) INTO v_TolalRegistros FROM :v_TablaMensajes;

	v_Registro := 1;
	
	WHILE (:v_Registro <= :v_TolalRegistros) DO --Ciclo de proceso
				
		SELECT "CodigoMensaje","Mensaje"  INTO v_CodigoMensaje,v_Mensaje 
		from :v_TablaMensajes 
		WHERE "IdRegistro" = v_Registro;
		
		v_TablaDatos = 	SELECT 
						ROW_NUMBER() over (order by "CodigoDato") as "IdRegistro",
						"CodigoDato" AS "CodigoDato", 
						"ValorDato" AS "ValorDato" 
						FROM :P_TablaCodigosMensajes WHERE "CodigoMensaje" = v_CodigoMensaje;

		SELECT COUNT(1) INTO v_TolalRegistrosDatos FROM :v_TablaDatos;

		v_RegistroDatos := 1;

		WHILE (:v_RegistroDatos <= :v_TolalRegistrosDatos) DO
		
			SELECT "CodigoDato","ValorDato" INTO v_CodigoDato,v_ValorDato 
			FROM :v_TablaDatos 
			WHERE "IdRegistro" = v_RegistroDatos;
			
			v_Mensaje := REPLACE(v_Mensaje, '::'||v_CodigoDato||'::' , v_ValorDato);
					
			v_RegistroDatos := v_RegistroDatos + 1;
		
		END WHILE; 
	
		v_Registro := v_Registro + 1;
		
		p_out_TablaMensajes = 	SELECT "Codigo","Descripcion" FROM :p_out_TablaMensajes UNION ALL
								SELECT v_CodigoMensaje AS "CodigoMensaje", v_Mensaje AS "Mensaje" FROM DUMMY;
				
		v_TablaDatos =  SELECT 
						0  as "IdRegistro",
						'' AS "CodigoDato", 
						'' AS "ValorDato" 
						FROM Dummy Where 2=1;
	
	END WHILE; 

END;

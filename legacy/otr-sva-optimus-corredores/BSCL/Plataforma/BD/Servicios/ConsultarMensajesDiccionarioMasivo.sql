SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Traduce desde un codigo a un mensaje de acuerdo al idioma del sistema
--  Autor: - BCS
--  Fecha Creacion: 26/10/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (
	IN P_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520)),
    OUT p_out_TablaMensajes		TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN
		
	DECLARE v_CodLenguaje 			NVARCHAR(40) := 'CL'; --TODO: leer desde tabla configuracion
	DECLARE v_SinTraduccion 		NVARCHAR(520):= 'Sin Traducción';
	DECLARE v_Quantity				BIGINT;
	
	
	/*v_Quantity := 0;
	
	SELECT COUNT("Descripcion") INTO v_Quantity
	FROM "BSCL.Plataforma.BD::PLA.Diccionario" 
	WHERE 	"CodDiccionario" = 'SIN_TRADUCCION' AND
			"CodLenguaje" = v_CodLenguaje;
	
	IF (:v_Quantity > 0) THEN
		SELECT "Descripcion" INTO v_SinTraduccion
		FROM "BSCL.Plataforma.BD::PLA.Diccionario" 
		WHERE 	"CodDiccionario" = 'SIN_TRADUCCION' AND
				"CodLenguaje" = v_CodLenguaje;
	END IF;*/
						
	p_out_TablaMensajes = SELECT "IdInterno",
								 "Codigo",
								 COALESCE(  REPLACE(
											REPLACE(
												"Descripcion",
												'::' || COALESCE("CodigoDato1",'#$213?') || '::',
												COALESCE("ValorDato1",'')
											), 
											'::' || COALESCE("CodigoDato2",'#$213?') || '::',
											COALESCE("ValorDato2",'')), 
										"Codigo" || ' ' || v_SinTraduccion) AS "Descripcion"
	FROM :P_TablaCodigosMensajes men
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" dic on dic."CodDiccionario" = men."Codigo" and "CodLenguaje" = v_CodLenguaje;
			
END;

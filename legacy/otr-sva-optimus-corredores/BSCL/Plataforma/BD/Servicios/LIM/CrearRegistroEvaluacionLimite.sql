SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.CrearRegistroEvaluacionLimite";

CREATE PROCEDURE  
-- 
--  Proposito: Servicio Interno que crea el registro de evaluaciones según una estructura de valores 
--  enviados desde las entidades del Negocio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/05/2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.CrearRegistroEvaluacionLimite" (
	-- Parametros entrada comunes en todos los servicios publicos

	
	
	IN p_RegistroEvaluacionLimite  TABLE(  "IdEvaluacionLimite"          BIGINT,
										   "IdLimite"                    BIGINT,
										   "IdTipoOperacion" 			 NVARCHAR(6),
										   "IdMoneda" 					 NVARCHAR(3),
										   "ConceptoExceso" 			 NVARCHAR(520),
										   "MontoExcedido"               DECIMAL(26,8),
										   "MontoUtilizado"              DECIMAL(26,8),
										   "FechaIngreso"				 SECONDDATE, 
										   "IdEstado"					 NVARCHAR(3),
										   "IdEstadoAutorizacion" 		 NVARCHAR(3),
										   "IdUsuarioCreador"   		 BIGINT,
										   "IdComportamiento" 		     NVARCHAR(3)),
	-- Parametros salida del servicio publico
	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
		


	BEGIN	 

		v_TablaCambios= SELECT	
				("BSCL.Plataforma.BD.Secuencias::LIM.IdSecuenciaEvaluacion".NEXTVAL) as "IdSecuenciaEvaluacion",
				 "IdEvaluacionLimite",
				 "IdLimite",
				 "IdTipoOperacion",
				 "IdMoneda",
				 "ConceptoExceso",
				 "MontoExcedido",
				 "MontoUtilizado",
				 "FechaIngreso",
				 "IdEstado",
				 "IdEstadoAutorizacion",
				 "IdUsuarioCreador",
				 "IdComportamiento"
		FROM :p_RegistroEvaluacionLimite;


		SELECT 	"IdEvaluacionLimite",
				"IdSecuenciaEvaluacion",
				"IdLimite",
				"IdTipoOperacion",
				 0,
				"IdMoneda",
				"ConceptoExceso",
				"MontoExcedido",
				"MontoUtilizado",
				"FechaIngreso",
				CURRENT_UTCTIMESTAMP,
				"IdEstado",
				'0001-01-01 00:00:00',
				( CASE WHEN "IdComportamiento"='AUT' THEN 'PEN' ELSE 'ING' END) AS "IdEstadoAutorizacion",
				"IdUsuarioCreador"
 		FROM :v_TablaCambios		
		INTO "BSCL.Plataforma.BD::LIM.EvaluacionLimites"(
			   "IdEvaluacionLimite",
			   "IdSecuenciaEvaluacion",
			   "IdLimite",
			   "IdTipoOperacion",
			   "IdUsuarioAutoriza",
			   "IdMoneda",
			   "ConceptoExceso",
			   "MontoExcedido",
			   "MontoUtilizado",
			   "FechaIngreso",
			   "FechaCreacion",
			   "IdEstado",
			   "FechaAutorizacion",
			   "IdEstadoAutorizacion",
			   "IdUsuarioCreador");
		
		
		 p_out_IdSecuencias =   SELECT "IdSecuenciaEvaluacion" as "IdSecuencia" FROM :v_TablaCambios;
 
 
 
	END;	
END;

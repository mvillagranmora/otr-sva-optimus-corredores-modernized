SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.ProcesarTarea";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para procesar tareas Agendadas o inmediatas deesde el JOB Manager, segun solictud o tipo documento .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/01/2017

"BSCL.Normativa.BD.Servicios::NOR.ProcesarTarea" ( 
    --VARIABLES DEL PROCESO PRINCIPAL
	IN p_IdEmpresa					  BIGINT,
 	IN p_IdTipoTarea   		 		  NVARCHAR(40),  --Tarea de inscripcion en BAT
   	IN p_IdSolicitudCircular		  BIGINT DEFAULT 0,
   	IN p_IdProceso		  BIGINT DEFAULT 0
    --VARIABLES DE SALIDA 
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
	DECLARE v_Count,v_countSal1,v_countSal2 INT:=0;
	DECLARE v_Esquema NVARCHAR(10):='';
	DECLARE v_sp NVARCHAR(500):='';
	DECLARE v_IdTipoDocumento NVARCHAR(10) :='';
	

---------------------------------------------------------------
---- ORIGEN SOLICITUD NORMATIVA
---------------------------------------------------------------				
	IF  p_IdSolicitudCircular > 0 THEN ---Se busca la solcitud
		
			

		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular"=p_IdSolicitudCircular;
		
		IF v_Count>0 THEN 
/*
				v_Datos = SELECT 
						  CASE WHEN SOL."NombreParametro"='idTipoDocumento'		THEN "Valor" ELSE NULL END AS "idTipoDocumento"
						, CASE WHEN SOL."NombreParametro"='IdSolicitudCircular'	THEN "Valor" ELSE NULL END AS "IdSolicitudCircular"
						 FROM "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas" SOL
						WHERE SOL."IdPrograma"=p_IdProceso AND SOL."IdTipoPrograma"=p_IdTipoTarea;
	
						v_DatosAgrupado =SELECT 
												  MAX(SOL."idTipoDocumento") 		AS "idTipoDocumento"
												, MAX(SOL."IdSolicitudCircular")	AS "IdSolicitudCircular"
												FROM :v_Datos SOL;
												*/
											
				SELECT TOP 1 "IdTipoDocumento"  INTO v_IdTipoDocumento FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
				WHERE "IdSolicitudCircular"=p_IdSolicitudCircular;
				
				
				
				SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE  "IdTipoDocumento" =v_IdTipoDocumento;
						
				IF v_Count>0 THEN 
				
					SELECT RTRIM("Esquema") INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE  "IdTipoDocumento" =v_IdTipoDocumento;
					---------------------------------------------------------------
					---- PROCESOS
					---------------------------------------------------------------													
							
					IF 	v_Esquema='CIRCNomina' THEN 
						CALL "BSCL.Normativa.BD.Servicios::CIR.ProcesarCircularizacion" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaPSalida1);
					END IF;
					
				END IF;
		END IF;		
	ELSE 
---------------------------------------------------------------
---- ORIGEN AGENDA
---------------------------------------------------------------	
		IF  p_IdTipoTarea <> '' THEN
			---------------------------------------------------------------
			---- PROCESOS
			---------------------------------------------------------------	
			IF 	p_IdTipoTarea='PROCESOCUADRATURACUSTODIA_NOR' THEN 
				--Proceso que obtiene la Cuadratura desde Custodia y la carga de forma Transpuesta a la interfaz Normativa
				CALL "BSCL.Normativa.BD.Servicios::PRO.ProcesarCuadraturaCustodia" (p_IdEmpresa=>p_IdEmpresa,p_IdProceso=>p_IdProceso,p_FechaOperacion=>'');
			END IF;
			
			---PROCESOS NORMATIVOS
 			
				--Proceso de Notificacion Circular 1985 (MENSUAL) --DIARIA se ejecuta por Pauta
			IF 	p_IdTipoTarea='NOTIFICACIONEJECUCIONORDENES_MENSUAL' THEN 
				CALL "BSCL.Normativa.BD.Servicios::EJORD.NotificarNomina"(p_IdEmpresa=>p_IdEmpresa,p_IdUsuario=>0,p_IdModulo=>'NOR',p_Frecuencia=>'MENSUAL',p_out_IdEvento=>v_out_IdEvento,p_out_AdvertenciasContador=>v_countSal1,p_out_AdvertenciasDetalle=>v_out_AdvertenciasDetalle,p_out_ErroresContador=>v_countSal2,p_out_ErroresDetalle=>v_out_ErroresDetalle);
			END IF;		
				--Proceso de Notificacion Circular 1985 (SEMANAL)
			IF 	p_IdTipoTarea='NOTIFICACIONEJECUCIONORDENES_SEMANAL' THEN 
				CALL "BSCL.Normativa.BD.Servicios::EJORD.NotificarNomina"(p_IdEmpresa=>p_IdEmpresa,p_IdUsuario=>0,p_IdModulo=>'NOR',p_Frecuencia=>'SEMANAL',p_out_IdEvento=>v_out_IdEvento,p_out_AdvertenciasContador=>v_countSal1,p_out_AdvertenciasDetalle=>v_out_AdvertenciasDetalle,p_out_ErroresContador=>v_countSal2,p_out_ErroresDetalle=>v_out_ErroresDetalle);
			END IF;	
			
	
			
		END IF;		
	
	END IF;	

	
	
END; 

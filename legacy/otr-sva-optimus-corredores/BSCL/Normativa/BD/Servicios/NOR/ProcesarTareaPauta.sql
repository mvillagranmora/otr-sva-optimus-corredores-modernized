SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.ProcesarTareaPauta";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para procesar tareas agregadas en la Pauta.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 22/02/2017

"BSCL.Normativa.BD.Servicios::NOR.ProcesarTareaPauta" ( 
    --VARIABLES DEL PROCESO PRINCIPAL
	IN p_IdEmpresa					  BIGINT,
 	IN p_IdTipoTarea   		 		  NVARCHAR(40),  --Tarea de inscripcion en BAT
   	IN p_IdProceso		  			  BIGINT DEFAULT 0
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
	
	
------------------------------------------------------
---- ORIGEN PAUTA
---------------------------------------------------------------	
		IF  p_IdTipoTarea <> '' THEN
			---------------------------------------------------------------
			---- PROCESOS
			---------------------------------------------------------------	
			---PROCESOS INTERMEDIOS DE NEGOCIO

			IF 	p_IdTipoTarea='ESTADISTICAOPERACIONCLIENTE' THEN 
				--INSERT INTO "BSCL.Normativa.BD::CIR.Paso" ("Valor")VALUES (p_IdProceso);
				--Proceso que genera una estadistica de las operaciones del cliente con el fin de realizar una evaluacion (SOI) de la misma
				CALL "BSCL.Normativa.BD.Servicios::PRO.ProcesarEstadisticaOperacionCliente" (p_IdEmpresa=>p_IdEmpresa,p_IdSolicitudCircular=>0,p_IdProceso=>p_IdProceso);
			END IF;

			---PROCESOS NORMATIVOS
	
			IF 	p_IdTipoTarea='PROCESOEVALUACIONMONTOSOI' THEN 
				--Proceso que realiza la evealuacion de las operaciones para determinar si estas pueden ser inusuales y notificar al oficial de cumplimiento
				CALL "BSCL.Normativa.BD.Servicios::UAF.ProcesarEvaluacionMontoSOI" (p_IdEmpresa=>p_IdEmpresa,p_IdSolicitudCircular=>0,p_IdProceso=>p_IdProceso,p_FechaOperacion=>'');
			END IF;
			
			IF 	p_IdTipoTarea='PROCESOLECTURAEJECUCIONORDENES' THEN 
				--Proceso que Lee las Ordenes Ejecutadas y las compara con la mejor oferta del mercado para notificar al cliente
				CALL  "BSCL.Normativa.BD.Servicios::EJORD.ProcesarEjecucionOrdenes" (p_IdEmpresa=>p_IdEmpresa,p_IdSolicitudCircular=>0,p_IdProceso=>p_IdProceso,p_FechaOperacion=>'');
			END IF;
			
				--Proceso de Notificacion Circular 1985 (DIARIA) --> MENSUAL y SEMANAL son por Agenda 
			IF 	p_IdTipoTarea='NOTIFICACIONEJECUCIONORDENES_DIARIA' THEN 
				CALL "BSCL.Normativa.BD.Servicios::EJORD.NotificarNomina"(p_IdEmpresa=>p_IdEmpresa,p_IdUsuario=>0,p_IdModulo=>'NOR',p_Frecuencia=>'DIARIA',p_out_IdEvento=>v_out_IdEvento,p_out_AdvertenciasContador=>v_countSal1,p_out_AdvertenciasDetalle=>v_out_AdvertenciasDetalle,p_out_ErroresContador=>v_countSal2,p_out_ErroresDetalle=>v_out_ErroresDetalle);
			END IF;
			
			
			--PROCESOS QUE CREAN UNA SOLICITUD
			IF 	p_IdTipoTarea='GENERAR1064PAUTA_R' OR p_IdTipoTarea='GENERAR1064PAUTA_FR' OR p_IdTipoTarea='GENERAR1720PAUTA' OR p_IdTipoTarea='GENERARNCG365FFMMPAUTA' OR p_IdTipoTarea='GENERARINFODERECHOS_PAUTA' THEN 
				IF 	 p_IdTipoTarea='GENERAR1064PAUTA_FR' THEN
					p_IdTipoTarea = 'CIR1064FR';
				END IF;
				IF 	p_IdTipoTarea='GENERAR1064PAUTA_R' THEN
					p_IdTipoTarea = 'CIR1064R';
				END IF; 
				IF 	p_IdTipoTarea='GENERARINFODERECHOS_PAUTA' THEN
					p_IdTipoTarea = 'INFDERECH';
				END IF; 
				CALL "BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudDesdePauta"(p_IdEmpresa=>p_IdEmpresa,p_IdTipoTarea=>p_IdTipoTarea,p_IdProceso=>p_IdProceso);
			END IF;
			
			--Proceso que registra nueva información de clientes (DIARIA)

			IF p_IdTipoTarea='CONSOLIDACLIENTEHISTORICO' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NCG380::ConsolidaClientes"();
			END IF;

			
		END IF;		
	
---END IF;	--p_IdProceso > 0	
--	BEGIN
--	INSERT INTO "BSCL.Normativa.BD::CIR.Paso" ("Valor")VALUES (3);
--	END;
	
	
END; 

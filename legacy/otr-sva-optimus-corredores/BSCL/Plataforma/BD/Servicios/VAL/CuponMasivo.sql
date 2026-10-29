SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo";
GO
CREATE PROCEDURE
--ALTER PROCEDURE
-- 
--  Proposito: Obtener cupon masivos invocando WebService
--  Autor: JG - BCS
--  Fecha Creacion: 27/10/2016
--  
"BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),   
	-- OPCIONES DE VALORIZACION
	IN p_RutinaValorizador  NVARCHAR(04) default 'BCS',   	-- Rutina de valorización desde parametro de sistema
	-- in p_in_ListaInstrumentos -> "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos"				 									 									 
	-- out p_out_ListaCuponeXs -> "BSCL.Plataforma.BD::VAL.CuponMasivo_Salida_Cupones"										
	OUT p_out_Mensaje 		nvarchar(520) --CONTROL DE SALIDA
)
    
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	--declara errores  (debe estar registrado en tabla CodigoError y Diccionario) 
	DECLARE ERROR_TIMEOUT     CONDITION FOR SQL_ERROR_CODE 10012;
	DECLARE ERROR_OBTENERCUPON CONDITION FOR SQL_ERROR_CODE 10013;
	BEGIN		
		--Control de Errores
		declare p_out_AdvertenciasContador int;
		declare p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		declare p_out_ErroresContador int;
		declare p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));		
		--Logicas
		declare V_TIMEOUT 			DATETIME;
		declare V_TIME_TO_TOUT 		DATETIME;
		--declare p_out_IdPrograma  	table("IdPrograma" BIGINT);
		declare p_TablaParametros 	table("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
		declare v_grupo 			bigint;
		--declare v_IdPrograma 		bigint;
		declare v_recs 				int;
		declare v_estado 			nvarchar(3);
		declare v_run 				int;
		declare v_sizerun 			int;
		declare v_inicio 			int;
		declare v_fin				int;
		declare v_njobs				int;
		declare v_TimeOutSegundos	int;
		declare v_Paralelismo    	int;	
		--variables para agregar programa
		declare v_IdPrograma bigint;
		declare v_Nodo NVARCHAR(1);
		declare v_fechaSistema DATE;
		declare p_out_IdPrograma table("IdPrograma" BIGINT) default select null as "IdPrograma" from dummy where 1=2;
		DECLARE v_grupo_param BIGINT;		
		--Tablas de paso
		
		DELETE FROM "BSCL.Plataforma.BD::VAL.CuponMasivo_Salida_Cupones";
		
		BEGIN
		    DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 259    BEGIN END;                
		END;  

		--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','S-VAl1','INICIO');
		
		-----------------------------------------------------
		--- BCS - PREPARA DATOS PARA OBTENER CUPONES
		-----------------------------------------------------		
		IF p_RutinaValorizador = 'BCS' THEN
	  		v_data = select ROW_NUMBER() over (Order by "IdTarea") as TAREA,tp.* from "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos" as tp;
	  		select count(1) into v_recs from :v_data;
	  		
	  		--call "BSCL.Plataforma.BD.Servicios::Depuracion"( p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','VAL-WS','v_recs: ' || CAST(v_recs as NVARCHAR)); -- BORRAR

			if v_recs>0 then
				select "BSCL.Plataforma.BD.Secuencias::INS.TareaValorizacion".NEXTVAL into v_grupo from dummy; -- for all records
				p_TablaParametros = select 'IdGrupo' as "NombreParametro",cast(:v_grupo as nvarchar(100)) as "Valor" from dummy; 
				
				--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','v_grupo para tablaParametros',cast(:v_grupo as nvarchar(100)));
				
				BEGIN AUTONOMOUS TRANSACTION
					INSERT INTO "BSCL.Plataforma.BD::INS.TareaValorizacion"  (
						"IdTarea", "GrupoValorizacion", "FechaSolicitud", "IdEstado", "FechaVal",
						"RutinaValorizacion","Nemo","FechaIni","FechaFin","CodTipoMod","Plazo","TipoTarea"
					 )select 
						a."IdTarea",v_grupo,CURRENT_UTCTIMESTAMP,'PEN',a."FechaIni", 									
						p_RutinaValorizador,a."Nemo",a."FechaIni",a."FechaFin",a."CodTipoMod",a."Plazo",'CUP'				 							 								  
					 from :v_data a
					 LEFT JOIN "BSCL.Plataforma.BD::INS.TareaValorizacion" b ON a."IdTarea" = b."IdTarea"
					 WHERE b."IdTarea" IS NULL;
					 
					 commit;
				END;	
				
				--no hacer el call, procesamiento directo.
				SELECT "BSCL.Plataforma.BD.Secuencias::Programa".NEXTVAL into v_IdPrograma from dummy;
				SELECT "Nodo" INTO v_Nodo from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = 'VALORIZADOR'; 
				SELECT TO_DATE("Valor") INTO v_fechaSistema FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa and "CodigoParametro" = 'PLA.FechaSistema';
				select "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma".nextval into v_grupo_param from dummy;
				BEGIN AUTONOMOUS TRANSACTION
					insert into "BSCL.Plataforma.BD::BAT.ParametrosPrograma"("IdSecuencia","IdGrupoParametro","IdPrograma","Orden","NombreParametro","Valor")
					select "BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia".nextval,:v_grupo_param,:v_IdPrograma,1,"NombreParametro","Valor"
					from :p_TablaParametros;
					INSERT INTO "BSCL.Plataforma.BD::BAT.Programa"("IdPrograma","IdAgenda","IdTipoPrograma","Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaSistema","Nodo","IdGrupoParametro")
					VALUES(:v_IdPrograma,0,'VALORIZADOR',0,:p_IdUsuario,'PEN','Esperando ejecución',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP, :v_fechaSistema,:v_Nodo,:v_grupo_param);
					commit;
				END;
				p_out_IdPrograma = select :v_IdPrograma as "IdPrograma" from dummy;
				p_out_ErroresContador := 0;
				--no hacer el call, procesamiento directo.
				
				select max("IdPrograma") into v_IdPrograma from :p_out_IdPrograma;					
				--Falló generación de Programa Batch
				if p_out_ErroresContador>0 then
					--p_out_Mensaje := 'No se pudo agregar programa batch';
					--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','ERROR',p_out_Mensaje);
					DELETE FROM "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos";
					SIGNAL ERROR_OBTENERCUPON;
				end if;
			end if;
			
			if v_recs = 0 then
				--p_out_Mensaje := 'NO RECORDS';
				--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','ERROR',p_out_Mensaje);
				DELETE FROM "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos";
				SIGNAL ERROR_OBTENERCUPON;		
			end if;		
			-----------------------------------------------------
			--- ESPERA RESPUESTA SERVIDOR
			-----------------------------------------------------
			v_TimeOutSegundos:= 120;--30+(v_recs*250/1000);
			--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','TimeOutSegundos',v_TimeOutSegundos);
		    V_TIMEOUT      := CURRENT_UTCTIMESTAMP;
			V_TIME_TO_TOUT := ADD_SECONDS (CURRENT_UTCTIMESTAMP, v_TimeOutSegundos);
			v_estado       := 'OUT';
			WHILE V_TIMEOUT < V_TIME_TO_TOUT DO 
			    --consulta si existe respuesta del servidor
				select count(1) into v_recs
					from "BSCL.Plataforma.BD::BAT.Programa" as cp
					where cp."idEstado" in ('TER','ERR') and cp."IdPrograma" = :v_IdPrograma;				
				if v_recs>0 then
					select count(1) into v_recs
					  from "BSCL.Plataforma.BD::BAT.Programa" as cp 
					 where  cp."idEstado" <> 'TER'  and cp."IdPrograma" = :v_IdPrograma;			
					 if v_recs> 0 then
			         	v_estado:='ERR';
			         else
			         	v_estado:='OK';			 	
					 end if;
					V_TIME_TO_TOUT := ADD_SECONDS (CURRENT_UTCTIMESTAMP, -100); 
				end if;
				V_TIMEOUT := CURRENT_UTCTIMESTAMP;
			END WHILE;
			-----------------------------------------------------
			--- Control de errores
			-----------------------------------------------------	
			if v_estado = 'OUT' then
				--p_out_Mensaje := 'TimeOut esperando valorizador';
				--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','ERROR',p_out_Mensaje);
				DELETE FROM "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos";
				SIGNAL ERROR_TIMEOUT;		
			end if;
			if v_estado = 'ERR' then
				--p_out_Mensaje := 'Error de ejecución programa batch';
				--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','ERROR',p_out_Mensaje);
				--SIGNAL ERROR_OBTENERCUPON;		
			end if;
			
			select count(1) into v_recs from "BSCL.Plataforma.BD::INS.TareaValorizacion" as tv
			where  tv."IdEstado" <> 'TER' and  tv."IdTarea" in (select distinct "IdTarea" from :v_data);
			
			if v_recs > 0 then
				--p_out_Mensaje := 'advertencia de obtener cupon, no todos los instrumentos fueron procesados';
				--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','ERROR',p_out_Mensaje);
				--SIGNAL ERROR_OBTENERCUPON;		
			end if;						
			-----------------------------------------------------
			--- SALIDA
			-----------------------------------------------------	
			p_out_Mensaje := 'OK';
			
			insert into "BSCL.Plataforma.BD::VAL.CuponMasivo_Salida_Cupones"
			(	"IdTarea",
				"GrupoValorizacion",
				"FechaSolicitud",
			 	"IdEstado",
				 -- DATOS ENTRADA|SALIDA
				"Nemo",
				"FechaIni",
				"FechaFin",
				"CodTipoMod",
				"Plazo",
				"Glosa",
				"CodigoEmisor",
				"FechaEmision",
				"FechaVencimiento",
				"TipoMod",
				"TipoUnidad",
				"ValorReferencial",
				"CuponAmortizacion",
				"CuponFactorAjuste",
				"CuponFechaEfectiva",
				"CuponFechaHabil",
				"CuponFlujo",
				"CuponIntereses",
				"CuponSecuencia",
				-- salida invocacion webservice desde el job
				"WsCallerCodSalida",
				"WsCallerDescSalida"
			) select 
				-- DATOS TAREA
				"IdTarea",
				"GrupoValorizacion",
				"FechaSolicitud",
			 	"IdEstado",
				 -- DATOS ENTRADA|SALIDA
				"Nemo",
				"FechaIni",
				"FechaFin",
				"CodTipoMod",
				"Plazo",
				"Glosa",
				"CodigoEmisor",
				"FechaEmision",
				"FechaVencimiento",
				"TipoMod",
				"TipoUnidad",
				"ValorReferencial",
				"CuponAmortizacion",
				"CuponFactorAjuste",
				"CuponFechaEfectiva",
				"CuponFechaHabil",
				"CuponFlujo",
				"CuponIntereses",
				"CuponSecuencia",
				-- salida invocacion webservice desde el job
				"WsCallerCodSalida",
				"WsCallerDescSalida"
             from "BSCL.Plataforma.BD::INS.TareaValorizacion" tv
			 where tv."IdTarea" in (select distinct "IdTarea" from :v_data);
			 
		END  IF; --fin tipificador BCS
		
		DELETE FROM "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos";
		
		-----------------------------------------------------
		--- XXX - PREPARA DATOS PARA tipificar
		-----------------------------------------------------		
		IF p_RutinaValorizador = 'XXX' THEN
			p_out_Mensaje := 'ERROR: ';
		END IF; --fin valorizador XXX
		--			
		--call "BSCL.Plataforma.BD.Servicios::Depuracion"(p_IdEmpresa,CURRENT_UTCTIMESTAMP,p_IdUsuario,'BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo','E-VAl1','TERMINO');
	END;
END;
GO
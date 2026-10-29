SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda";
GO
CREATE PROCEDURE
-- 
--  Proposito: Agrega Agenda  
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                 
	                                
									IN	p_IdTipoTarea		NVARCHAR(40),
									IN	p_EsDemonio			NVARCHAR(1),   
									IN	p_DiaDeMes			NVARCHAR(2),
									IN	p_DiaDeSemana		NVARCHAR(1),
									IN	p_Mes				NVARCHAR(2),
									IN	p_Hora				NVARCHAR(2),
									IN  p_Minuto			NVARCHAR(2),
									IN	p_SoloDiasHabiales	NVARCHAR(1),
									IN  p_SoloPrincipioMes	NVARCHAR(1),
									IN	p_SoloFinDeMes		NVARCHAR(1),
									IN  p_habilitado		NVARCHAR(1), 
									IN  p_TablaParametros	table("NombreParametro" NVARCHAR(100), "Valor" NVARCHAR(5000)),
									
 									out p_IdAgenda			BIGINT,
									OUT p_IdPrograma		BIGINT,
									OUT p_out_AdvertenciasContador int,
									OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
									OUT p_out_ErroresContador int,
									OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 


	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;


		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
BEGIN
 
 		 
		declare v_ExisteAgenda INT;
		declare v_ExisteTipoTarea INT;
		declare	v_DiaDeMes				INT; 
		declare	v_DiaDeSemana			INT;
		declare	v_Mes					INT;
		declare	v_Hora					INT;
		declare v_Minuto				INT;
		declare v_contador 				INT;
		declare v_contadorArmarFecha	INT;
		declare v_IdAgenda				BIGINT;
		declare v_IdPrograma			BIGINT;
		declare v_MensajeSalida		NVARCHAR(540);
		declare p_FechaSalida   NVARCHAR(45);
		declare v_IdProgramTable   TABLE("IdPrograma" BIGINT);
		DECLARE v_seconds BIGINT;
		DECLARE v_out_msg NVARCHAR(540);


		 
		


		v_ExisteTipoTarea := 0;	
	   select count(1) into v_ExisteTipoTarea from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = p_IdTipoTarea;
	   SELECT "BSCL.Plataforma.BD.Secuencias::Agenda".nextval INTO v_IdAgenda from dummy;
	   
	   
	   v_contadorArmarFecha :=0;
	   v_contador :=0;
	   v_contador := (v_ExisteTipoTarea + v_contador);
	   IF :p_DiaDeMes = '**' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	    v_DiaDeMes := to_number(p_DiaDeMes);
	       
	        IF :v_DiaDeMes > 0 AND :v_DiaDeMes < 32 THEN
	          v_contador := (v_contador +1);
 	        ELSE
	         p_out_ErroresDetalle = select 'DMNV01' as "Codigo" , 'Dia de mes no valido' as "Descripcion" from dummy;
	        END IF;
	   END IF;
	   
	   IF :p_DiaDeSemana = '*' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	    v_DiaDeSemana := to_number(p_DiaDeSemana);
	       
	        IF :v_DiaDeSemana > 0 AND :v_DiaDeSemana < 8 THEN
	          v_contador := (v_contador +1);
	        ELSE
	          p_out_ErroresDetalle = select 'DSNV01' as "Codigo" , 'Dia de semana no valido' as "Descripcion" from dummy;
 	        END IF;
	   END IF;
	   
	   IF :p_Mes = '**' THEN 
	   	  v_contador := (v_contador +1);
	   	  
 	   ELSE
	    v_Mes := to_number(p_Mes);
	       
	        IF :v_Mes > 0 AND :p_Mes < 13 THEN
	          v_contador := (v_contador +1);
 	        ELSE
 	          p_out_ErroresDetalle = select 'MSNV01' as "Codigo" , 'Mes no valido' as "Descripcion" from dummy;
 	        END IF;
	   END IF;
	   
	   IF :p_Hora = '**' THEN 
	   	  v_contador := (v_contador +1);
	   	  
 	   ELSE
	    v_Hora := to_number(p_Hora);
	       
	        IF :v_Hora >= 0 AND :v_Hora < 24 THEN
	          v_contador := (v_contador +1);
 	        ELSE
 	         p_out_ErroresDetalle = select 'HRNV01' as "Codigo" , 'Hora no valido' as "Descripcion" from dummy;
	        END IF;
	   END IF;
	   
	   IF :p_Minuto = '**' THEN 
	   	  v_contador := (v_contador +1);
	   	   
 	   ELSE
	    v_Minuto := to_number(p_Minuto);
	       
	        IF :v_Minuto >= 0 AND :v_Minuto < 60 THEN
	         v_contador := (v_contador +1);
	          
	        ELSE
	           p_out_ErroresDetalle = select 'MINNV01' as "Codigo" , 'Minuto no valido' as "Descripcion" from dummy;
 	        END IF;
	   END IF;
	   
	   IF :p_SoloDiasHabiales = 'N' or :p_SoloDiasHabiales = 'S' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	   		p_out_ErroresDetalle = select 'DHNV01' as "Codigo" , 'Solo Dias Habiles no valido' as "Descripcion" from dummy;
 	   END IF;
	    IF :p_SoloPrincipioMes = 'N' or :p_SoloPrincipioMes = 'S' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	   		p_out_ErroresDetalle = select 'SPMNV01' as "Codigo" , 'Solo principio mes no valido' as "Descripcion" from dummy;

 	   END IF;
	    IF :p_SoloFinDeMes = 'N' or :p_SoloFinDeMes = 'S' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	         
	   		p_out_ErroresDetalle = select 'SFMNV01' as "Codigo" , 'Solo fin mes no valido' as "Descripcion" from dummy;
	         
 	   END IF;
	    IF :p_habilitado = 'N' or :p_habilitado = 'S' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	   		p_out_ErroresDetalle = select 'HNV01' as "Codigo" , 'Habilitado no valido' as "Descripcion" from dummy;
 	   END IF;
	   IF :p_EsDemonio = 'N' or :p_EsDemonio = 'S' THEN 
	   	  v_contador := (v_contador +1);
	   ELSE
	   		p_out_ErroresDetalle = select 'DNV01' as "Codigo" , 'El demonio no valido' as "Descripcion" from dummy;
 	   END IF;
	   
	   
	   
	   
	   
	   
	   
	   
	   
 	   
       IF :v_contador = 11 THEN
       
       	IF :v_ExisteTipoTarea = 0 THEN
	   		p_out_ErroresDetalle = select 'TPNE01' as "Codigo" , 'Tipo Programa no configurado' as "Descripcion" from dummy;
        
        ELSE
        	
	       	INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda"(
	       	"IdAgenda", "IdTipoPrograma","EsDemonio","DiaDeMes", "DiaDeSemana", 
	       	"Mes", "Hora", "Minuto", "SoloDiasHabiles", "SoloPrincipioMes","SoloFinDeMes", "habilitado")
	       	
	       	values( 
	       	v_IdAgenda,p_IdTipoTarea,p_EsDemonio,p_DiaDeMes,p_DiaDeSemana,
	       	p_Mes,p_Hora,p_Minuto, p_SoloDiasHabiales,p_SoloPrincipioMes,p_SoloFinDeMes,p_habilitado
	       	
	       	);
	       	
	        INSERT INTO  "BSCL.Plataforma.BD::BAT.ParametrosAgenda"
	       	SELECT
	       	v_IdAgenda,
	       	0,
	       	"NombreParametro",    
	       	'String',
	       	"Valor"
	       	from :p_TablaParametros;
	     	
        
 		     	
      	 	IF :p_habilitado = 'S' THEN
       	
		       	CALL "BSCL.Plataforma.BD.Servicios::BAT.ObtenerSiguienteFechaAgendada"(p_IdEmpresa,p_IdUsuario,'PLA',v_IdAgenda,v_FechaSalidaSegundos,v_out_msg);
		      	  
		        select "FechaAgendar" into v_seconds from :v_FechaSalidaSegundos;
		        
	      	 	select add_seconds(CURRENT_UTCTIMESTAMP,v_seconds ) into p_FechaSalida from dummy;
	  		
	  		   
	  			update "BSCL.Plataforma.BD::BAT.Agenda" set "SiguienteFechaDeEjecucion" = p_FechaSalida	where "IdAgenda" = v_IdAgenda;  
	       	
		       	call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
				p_IdEmpresa
				,p_IdUsuario
				,p_IdModulo 
				,v_IdAgenda
				,p_IdTipoTarea
				, v_seconds
				,:p_TablaParametros
				,'S'
				,v_IdProgramTable
				,p_out_AdvertenciasContador
				,p_out_AdvertenciasDetalle
				,p_out_ErroresContador
				,p_out_ErroresDetalle
				);
			
	
				select "IdPrograma" into p_IdPrograma from :v_IdProgramTable;
				
			END IF;
       	
       	END IF;
       	
       	
       p_IdAgenda := v_IdAgenda;
       
       end if;
   
	
	 
   END;
END;

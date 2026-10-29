SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona" ;
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de una relación entre entidades.
--  Autor: CFlores - BCS
--  Fecha Creacion: 03/02/2016
-- 
--  Proposito: Se incorporan definiciones Optimus Perú
--  Autor: JGARCES - BCS
--  Fecha Modificacion: 14/03/2017
--
"BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						bigint,			-- ID  Empresa destino
	IN  p_IdUsuario						bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo						nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 			nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN  p_IdTipoRelacion				NVARCHAR(6),
	IN  p_IdPersonaOrigen 				BIGINT,
	IN  p_IdPersonaDestino 				BIGINT,
	IN  p_IdEstado 						NVARCHAR(3),
	IN  p_FechaInicioRelacion 			SECONDDATE,
	IN  p_FechaFinRelacion 				SECONDDATE,
	IN 	p_PorcentajeParticipacion		DECIMAL(26,8),
	IN  p_RetiraDocumentos 				NVARCHAR(1) DEFAULT 'N',
	IN  p_FinalidadRelacion				NVARCHAR(255) DEFAULT '',
	IN  p_CondicionesRelacion			NVARCHAR(255) DEFAULT '',
	IN  p_IdGradoVinculacionParental	BIGINT DEFAULT 0,
	IN	p_Ciudad						NVARCHAR(40) DEFAULT '',
	IN	p_Calle							NVARCHAR(100) DEFAULT '',
	IN	p_Numero						NVARCHAR(40) DEFAULT '',
	IN	p_Telefono						NVARCHAR(20) DEFAULT '',
	IN	p_Email							NVARCHAR(50) DEFAULT '',
	IN	p_Complemento					NVARCHAR(50) DEFAULT '',
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
		        	
	
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
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	v_out_AdvertenciasDetalle = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	v_out_ErroresDetalle = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdRelacionPersona			 		bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NUEREL';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
       
		DECLARE v_countPersona			 		bigint:=0;
		DECLARE v_countRelacion			 		bigint:=0;
		DECLARE v_countArbitrador			 	bigint:=0;
		DECLARE v_countEmpleado			 	    bigint:=0;
		DECLARE v_countPorcentaje               bigint:=0;
	
		DECLARE v_cantRep			 	    bigint:=0;
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		--- Existencia 		
		BEGIN
			
			SELECT COUNT("IdPersona") INTO v_countPersona 
			FROM "BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdPersona"=p_IdPersonaDestino;
			IF(v_countPersona=0) THEN
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('PERSONA_RELACIONADA_NO_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'PERSONA_RELACIONADA_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
										
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;

			SELECT COUNT("IdRelacionPersona") INTO v_countRelacion 
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" 
			WHERE "IdPersonaOrigen"=p_IdPersonaOrigen AND "IdPersonaDestino"=p_IdPersonaDestino AND "IdTipoRelacion"=p_IdTipoRelacion;
			IF(v_countRelacion>0) THEN
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RELACION_YA_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'RELACION_YA_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			

			IF(p_IdPersonaOrigen=p_IdPersonaDestino) THEN
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RELACION_INVALIDA',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'RELACION_INVALIDA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			SELECT COUNT("IdRelacionPersona") INTO v_countArbitrador 
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" 
			WHERE "IdPersonaOrigen"=p_IdPersonaOrigen AND "IdEstado" <> 'BLO' AND "IdPersonaDestino" = p_IdPersonaDestino AND "IdTipoRelacion"=p_IdTipoRelacion;
			IF(v_countArbitrador>0) THEN
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RELACION_YA_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'RELACION_YA_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			SELECT COUNT("IdPersona") INTO v_countEmpleado 
			FROM "BSCL.SocioNegocio.BD::EMP.Empleado" WHERE "IdPersona" = p_IdPersonaDestino;
			
			IF(v_countEmpleado=0 AND (p_IdTipoRelacion='AR1' OR p_IdTipoRelacion='AR2'))  THEN
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('PERSONA_RELACIONADA_EMPLEADO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'PERSONA_RELACIONADA_EMPLEADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
										
				SIGNAL SALIDA_CONTROLADA; 		
			END IF;                
                SELECT SUM("PorcentajeParticipacion") INTO v_countPorcentaje FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion"
                WHERE "IdPersonaOrigen"=p_IdPersonaOrigen AND "IdTipoRelacion"='BENFIN';
                
                IF ((v_countPorcentaje+p_PorcentajeParticipacion)>100 AND p_IdTipoRelacion='BENFIN') THEN
                v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('PORCENTAJE_BENEFICIARIO_INVALIDO',:v_hash,:v_erroroutmsg);
                p_out_ErroresDetalle =  select 'PORCENTAJE_BENEFICIARIO_INVALIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
                select * from :p_out_ErroresDetalle;
                SIGNAL SALIDA_CONTROLADA;
			END IF;
			
		END;	
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
	
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    SELECT "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona".NEXTVAL INTO v_IdRelacionPersona FROM DUMMY;
	
			p_out_RefId:=v_IdRelacionPersona;
			
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------	
			--si relacion es de RepLegal, actualiza el tabla Persona el Rep principal cuando no tiene uno principal
				SELECT count("IdPersonaOrigen")
				INTO v_cantRep
				FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"
				WHERE "IdPersonaOrigen"= :p_IdPersonaOrigen AND "IdTipoRelacion" = 'REP';
			
				UPDATE "BSCL.SocioNegocio.BD::PER.Persona" 
				SET "IdPersonaRepresentante" = :p_IdPersonaDestino
				WHERE :p_IdTipoRelacion = 'REP' AND :v_cantRep = 0 AND "IdPersona"= :p_IdPersonaOrigen;
				
					insert into "BSCL.SocioNegocio.BD::REL.RelacionPersona"
					(					
						"IdRelacionPersona",
						"IdTipoRelacion",
						"IdPersonaOrigen",
						"IdPersonaDestino",
						"IdEstado",
						"FechaInicioRelacion",
						"FechaFinRelacion",
						"FechaIngreso",
						"FechaCreacion",
						"FechaModificacion",
						"IdUsuarioCreador",
						"IdEstadoAutorizacion",
						"PorcentajeParticipacion",
						"RetiraDocumentos",
						"FinalidadRelacion",
						"CondicionesRelacion",
						"IdGradoVinculacionParental",
						"Ciudad",
						"Calle",
						"Numero",
						"Email",
						"Telefono",
						"Complemento"
						
					) 
					VALUES (
								v_IdRelacionPersona,
								p_IdTipoRelacion,
								p_IdPersonaOrigen,
								p_IdPersonaDestino,
								p_IdEstado,
								p_FechaInicioRelacion,
								p_FechaFinRelacion,
							  	v_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7') 					
								CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
								CURRENT_UTCTIMESTAMP,
							  	p_IdUsuario, --IdUsuarioCreador
								v_IdEstadoAutorizacion,
								p_PorcentajeParticipacion,
								p_RetiraDocumentos,
								p_FinalidadRelacion,
								p_CondicionesRelacion,
								p_IdGradoVinculacionParental,
								p_Ciudad,
								p_Calle,
								p_Numero,
								p_Email,
								p_Telefono,
								p_Complemento
							);			
										------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			V_RegistroSolicitudCambio =SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
			:p_IdEmpresa,
			:p_IdUsuario,
			:p_IdModulo,
			:V_RegistroSolicitudCambio,
			:v_IdConcepto,
			:v_RequiereAutorizacion
			);
					
			if v_RequiereAutorizacion='S' then
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------

				DECLARE	v_Glosa				NVARCHAR(520);
				DECLARE v_out_idAuth 		bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;		
				
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto"  into  v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = p_IdPersonaDestino;		
					
						CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
							p_IdEmpresa 				=> p_IdEmpresa,
							p_IdUsuario 				=> p_IdUsuario,
							p_IdModulo 					=> p_IdModulo,
							p_UsaReglasNegocio 			=> 'N',								
							p_IdTipoEvento 				=> v_IdConcepto,--'NUEDOC',
							p_IdModuloSolicita 			=> p_IdModulo,
							p_IdUsuarioSolicita 		=> p_IdUsuario,
							p_Fecha 					=> TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'), 
							p_IdConcepto 				=> v_IdConcepto,
							p_NumeroOperacionConcepto 	=> CAST (v_IdRelacionPersona AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
							p_FechaOperacionConcepto 	=> TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'),
							p_IdCliente 				=> p_IdPersonaOrigen, --v_IdCliente,
							p_Glosa 					=> v_Glosa,
							--salida
							p_out_IdEvento 				=> v_out_idAuth,
							p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
							p_out_AdvertenciasDetalle 	=> v_out_AuthAdvertenciasDetalle,
							p_out_ErroresContador 		=> v_out_AuthErroresContador,
							p_out_ErroresDetalle 		=> v_out_AuthErroresDetalle);		
					
			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
					  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
			                             From :v_out_AuthAdvertenciasDetalle Union All 
			                             Select "Codigo","RequiereAutorizacion", "Descripcion"
			                             From :p_out_AdvertenciasDetalle;  
					                                          
					                                          
			 p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
					                                         
			 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
			                        From :v_out_AuthErroresDetalle Union All 
			                        Select "Codigo", "Descripcion" 
			                        From :p_out_ErroresDetalle; 
				                                     
					                                     
			else	--Confirma la creacion y cambia el estado del documento
			
			--Insert en tabla log para re-generacion automatica de documentos en caso de ser necesario....
				INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
				("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
				SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'RELACIONES_', 'Creado', :p_IdEmpresa, 0, :p_IdPersonaOrigen, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Persona - CrearPersonaTelefono.sql'
				FROM dummy;

				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.SocioNegocio.BD.Servicios::REL.ConfirmarCrearRelacionPersona"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					v_IdRelacionPersona,
					v_IdEstadoAutorizacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle);
						
			end if;
			
		
		SIGNAL SALIDA_CONTROLADA;		
		END;		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
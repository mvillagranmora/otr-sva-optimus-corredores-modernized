SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::CrearAutorizadoOrden" ;
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de personas autorizadas a dar ordenes.
--  Autor: CFlores - BCS
--  Fecha Creacion: 02/02/2016
-- 
-- 
--  Modificacion: Se a�ade creaci�n de log
--  Autor: shechenleitner - BCS
--  Fecha Modificacion: 13/12/2018
-- 

"BSCL.Portafolio.BD.Servicios::CrearAutorizadoOrden" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
   	IN  p_IdPortafolio					BIGINT,
   	IN  p_IdPersona 					BIGINT,
   	IN  p_IdEstado						NVARCHAR(3),
   	IN  p_IdTipoAutorizado				NVARCHAR(40),
   	IN  p_FirmaOptimusCliente 			NVARCHAR(1),
   	
   	--PERU
	IN p_NumeroRegistroPublico			NVARCHAR(40) DEFAULT '',
	IN p_Asiento						NVARCHAR(40) DEFAULT '',
	IN p_FichaPartida					NVARCHAR(40) DEFAULT '',
	IN p_TipoFirma						NVARCHAR(40) DEFAULT 'INDIVIDUAL',	--INDIVIDUAL,CONJUNTA
	

	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT SELECT '' AS "Codigo",'' as "Descripcion" from dummy where 1=2;
    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		v_TablaCodigosMensajes = 	SELECT 0 AS "IdInterno",0 as "IdOperacion","Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
									FROM :v_out_ErroresDetalle det;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT det."Codigo",
										msj."Descripcion"
								FROM :v_out_ErroresDetalle det
								LEFT JOIN :v_out_ErroresDetalleMensajes msj on det."Codigo" = msj."Codigo";
		
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;
								
	END;
	
	BEGIN
	
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NAUTOR';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    
		DECLARE v_countAutorizado			 		bigint:=0;
		DECLARE v_countPersona				 		bigint:=0;
		
		--PRONETIT
		DECLARE v_countIdPortafolio 	INT:=0;
		DECLARE v_IdEntidad				BIGINT;
		DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'AUTORIZACION';
		DECLARE v_TipoOperacion			NVARCHAR(1):= 'I';
		
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
			
		p_out_ErroresContador := 0;
		p_out_AdvertenciasContador :=0;
		
		SELECT COUNT("IdPersona") INTO v_countPersona FROM "BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdPersona"=p_IdPersona;
		IF(v_countPersona=0) THEN
			v_out_ErroresDetalle =  select 'PERSONA_RELACIONADA_NO_EXISTE' as "Codigo",'' as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		SELECT COUNT("IdAutorizado") INTO v_countAutorizado FROM "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" WHERE "IdPortafolio"=p_IdPortafolio
														AND "IdPersona"=p_IdPersona AND "IdTipoAutorizado"=p_IdTipoAutorizado;
		IF(v_countAutorizado>0) THEN
			v_out_ErroresDetalle =  select 'AUTORIZADO_YA_EXISTE' as "Codigo",'' as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
		
		-- recuperamos numero de secuencia que usaremos para el id 
	    SELECT "BSCL.Portafolio.BD.Secuencias::Autorizado".NEXTVAL INTO p_out_RefId FROM DUMMY;
	    
		insert into "BSCL.Portafolio.BD::POR.Autorizado"
		(
				"IdAutorizado", 
			   	"IdPortafolio",
			   	"IdPersona",
			   	"IdTipoAutorizado",
			   	"IdEstado",
			   	"IdEstadoAutorizacion",
			   	"FechaIngreso" ,
			   	"FechaCreacion",
				"FechaModificacion",
				"IdUsuarioCreador",
				"NumeroRegistroPublico",
				"Asiento",
				"FichaPartida",
				"TipoFirma",
				"FirmaOptimusCliente" 
				) VALUES (
				p_out_RefId,
				p_IdPortafolio,
				p_IdPersona,
				p_IdTipoAutorizado,--'ORDEN-CREAR',
				p_IdEstado,
				v_IdEstadoAutorizacion,							
				v_FechaIngreso,
				CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
				CURRENT_UTCTIMESTAMP,
				p_IdUsuario,
				p_NumeroRegistroPublico,
				p_Asiento,
				p_FichaPartida,
				p_TipoFirma,
				p_FirmaOptimusCliente); --IdUsuarioCreador
		
		-- SE A�ADE LOG INFORMATIVO
		v_DetalleLog =  
						SELECT 
							'AUTORIZADO' AS "SubRegistro", 
			        		'IdTipoAutorizado' AS "CampoRegistro", 
			        		cast('' AS NVARCHAR(5000)) AS "ValorAntiguo", 
			        		cast("IdTipoAutorizado" AS NVARCHAR(5000)) AS "ValorNuevo"
			        		FROM "BSCL.Portafolio.BD::POR.Autorizado" WHERE 
							"IdPortafolio" = p_IdPortafolio AND 
							"IdPersona" = p_IdPersona AND 
							"IdTipoAutorizado" = p_IdTipoAutorizado AND 
							"FechaIngreso" = v_FechaIngreso
						UNION ALL
						SELECT
			        		'AUTORIZADO' AS "SubRegistro", 
			        		'IdPortafolio' AS "CampoRegistro", 
			        		cast('' AS NVARCHAR(5000)) AS "ValorAntiguo", 
			        		cast("IdPortafolio" AS NVARCHAR(5000)) AS "ValorNuevo"
			        		FROM "BSCL.Portafolio.BD::POR.Autorizado" WHERE 
							"IdPortafolio" = p_IdPortafolio AND 
							"IdPersona" = p_IdPersona AND 
							"IdTipoAutorizado" = p_IdTipoAutorizado AND 
							"FechaIngreso" = v_FechaIngreso
						UNION ALL
						SELECT 
							'AUTORIZADO' AS "SubRegistro", 
			        		'NombreAutoriza' AS "CampoRegistro", 
			        		cast('' AS NVARCHAR(5000)) AS "ValorAntiguo", 
			        		cast(CONCAT(CONCAT(CONCAT(CONCAT("Nombres", ' '), "ApellidoPaterno"), ' '), "ApellidoMaterno")AS NVARCHAR(5000)) AS "ValorNuevo"
			        		FROM "BSCL.SocioNegocio.BD::PER.Persona" WHERE 
			        		"IdPersona" IN (
			        			select "IdPersona" from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCliente" IN (
									select "IdCliente" from "BSCL.Portafolio.BD::POR.Portafolio" where "IdPortafolio" = p_IdPortafolio
								)
							)
						UNION ALL
						/*
						SELECT 
							'AUTORIZADO' AS "SubRegistro", 
			        		'IdPersonaAutorizado' AS "CampoRegistro", 
			        		cast('' AS NVARCHAR(5000)) AS "ValorAntiguo", 
			        		cast("IdPersona" AS NVARCHAR(5000)) AS "ValorNuevo"
			        		FROM "BSCL.Portafolio.BD::POR.Autorizado" WHERE 
							"IdPortafolio" = p_IdPortafolio AND 
							"IdPersona" = p_IdPersona AND 
							"IdTipoAutorizado" = p_IdTipoAutorizado AND 
							"FechaIngreso" = v_FechaIngreso
						UNION ALL
						*/
						SELECT 
							'AUTORIZADO' AS "SubRegistro", 
			        		'NombreAutorizado' AS "CampoRegistro", 
			        		cast('' AS NVARCHAR(5000)) AS "ValorAntiguo", 
			        		cast(CONCAT(CONCAT(CONCAT(CONCAT("Nombres", ' '), "ApellidoPaterno"), ' '), "ApellidoMaterno")AS NVARCHAR(5000)) AS "ValorNuevo"
			        		FROM "BSCL.SocioNegocio.BD::PER.Persona" WHERE 
							"IdPersona" = p_IdPersona;
							
							
		        		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
														p_IdTipoLog 		=> 'INGRESO',
														p_IdModulo 			=> :p_IdModulo,
														p_IdUsuario 		=> :p_IdUsuario,
														p_IdEmpresa 		=> :p_IdEmpresa,
														p_FechaSistemaLog 	=> TO_VARCHAR(:v_FechaIngreso, 'YYYY-MM-DD'),
														p_IdUsuarioAtendido => :p_IdUsuario,
														p_IdTipoRegistro	=> 'INGRESOAUTORIZADO',
														p_NombreProceso 	=> 'BSCL.Portafolio.BD.Servicios::CrearAutorizadoOrden',
														p_DetalleLog 		=> :v_DetalleLog
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
		
			DECLARE	 v_Glosa						NVARCHAR(255);
			DECLARE  v_CodPortafolio 				NVARCHAR(255);
			DECLARE v_out_idAuth 					bigint;
			DECLARE v_out_AuthAdvertenciasContador 	int;
			DECLARE v_out_AuthErroresContador 		int;
			
			SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto"  into  v_Glosa 
  			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona"=p_IdPersona;
  			
  			SELECT 'CodPort: ' || "CodigoPortafolio" into v_CodPortafolio
  			FROM "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" where "IdPortafolio" = p_IdPortafolio;
  			
  			
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa => p_IdEmpresa,
				p_IdUsuario => p_IdUsuario,
				p_IdModulo => p_IdModulo,
				p_UsaReglasNegocio => 'N',								
				p_IdTipoEvento => v_IdConcepto,--'NUEDOC',
				p_IdModuloSolicita => p_IdModulo,
				p_IdUsuarioSolicita => p_IdUsuario,
				p_Fecha => TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'),  
				p_IdConcepto => v_IdConcepto,
				p_NumeroOperacionConcepto =>  CAST (p_out_RefId AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
				p_FechaOperacionConcepto => TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'), 
				p_IdCliente => p_IdPersona, --v_IdCliente,
				p_Glosa => v_Glosa || ' / ' || v_CodPortafolio,
				--salida
				p_out_IdEvento => v_out_idAuth,
				p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
				p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
				p_out_ErroresContador =>  v_out_AuthErroresContador,
				p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);		
				
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

			v_IdEstadoAutorizacion:='ING';
		
			CALL "BSCL.Portafolio.BD.Servicios::ConfirmarCrearAutorizadoOrden"(
				p_IdEmpresa 				=> p_IdEmpresa,
				p_IdUsuario 				=> p_IdUsuario,
				p_IdModulo 					=> p_IdModulo,
				p_UsaReglasNegocio 			=> p_UsaReglasNegocio,
				p_IdAutorizado 				=> p_out_RefId,
				p_IdEstadoAutorizacion 		=> v_IdEstadoAutorizacion,
				p_out_AdvertenciasContador 	=> v_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle 	=> v_out_AdvertenciasDetalle,
				p_out_ErroresContador 		=> v_out_ErroresContador,
				p_out_ErroresDetalle 		=> v_out_ErroresDetalle);

			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
				  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
			                             From :v_out_AdvertenciasDetalle Union All 
			                             Select "Codigo","RequiereAutorizacion", "Descripcion"
			                             From :p_out_AdvertenciasDetalle;  
			                                         
				                                          
			 p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
				                                         
			 p_out_ErroresDetalle = Select "Codigo", "Descripcion"
			                             From :v_out_ErroresDetalle Union All 
			                             Select "Codigo", "Descripcion" 
			                             From :p_out_ErroresDetalle; 
			                             
			 	--PRONETIT
	
			v_IdEntidad	:= p_out_RefId;

			SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
			FROM "BSCL.Portafolio.BD::POR.Portafolio"
			WHERE "IdPortafolio" = :p_IdPortafolio
			AND "Proposito" = 'PROPOSITOAPV';
			
			IF(v_countIdPortafolio > 0)THEN
				CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:p_IdPortafolio, 
					v_IdEntidad,
					v_TipoEntidadReplica, 
					v_TipoOperacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle
				);
			
				
	  			p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
	  			 
				p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
	                                          From :v_out_AdvertenciasDetalle Union All 
	                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
	                                          From :p_out_AdvertenciasDetalle;  
	                                          
	            p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
	                                         
	            p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
	                                     From :v_out_ErroresDetalle Union All 
	                                     Select "Codigo", "Descripcion" 
	                                     From :p_out_ErroresDetalle; 
	                
	            IF	p_out_ErroresContador > 0 then
					SIGNAL SALIDA_CONTROLADA;
			  	END if;
            END IF;   
						
						
		end if;
	END;
END;

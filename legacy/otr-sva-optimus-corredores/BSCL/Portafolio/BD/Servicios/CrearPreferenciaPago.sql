SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::CrearPreferenciaPago" ;
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de la preferencia de pago.
--  Autor: CFlores - BCS
--  Fecha Creacion: 31/03/2016
-- 

"BSCL.Portafolio.BD.Servicios::CrearPreferenciaPago" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN  p_IdPortafolio 					BIGINT,
	IN  p_IdPreferenciaPago				NVARCHAR(40), 
	IN  p_IdCuentaBancaria				BIGINT,
	IN  p_IdTarjetaBancaria				BIGINT,
	IN  p_IdEstado						NVARCHAR(3),
	IN 	p_PorDefecto					NVARCHAR(1) DEFAULT 'N',

--	IN  p_IdPersonaRelacionada			BIGINT,   -- cuando PreferenciaPago.UsaPersonaRelacionada = 'S'

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
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdPreferenciaPagoPortafolio	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NCPPAG';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(100),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		
		DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'PREFERENCIASDEPAGO';
		DECLARE v_TipoOperacion			NVARCHAR(1):= 'I';
		DECLARE v_countIdPortafolio INT:=0;
		
		-----------------------------------------------------
		-- VALIDACIONES
		-----------------------------------------------------
		BEGIN
			-- Validaciones datos de entrada			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				 SIGNAL SALIDA_CONTROLADA;
			END IF;

			IF p_IdPreferenciaPago = '' THEN
				p_out_ErroresDetalle = SELECT 'PREFERENCIA_PAGO_NO_EXISTE' AS "Codigo", 'No existe Preferencia de Pago' AS "Descripcion" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;	    
			
			CALL "BSCL.Portafolio.BD.Reglas::ServicioCumpleRequisitosGestionPreferenciaPago"(:p_IdEmpresa, :p_IdPortafolio, 0, :p_IdPreferenciaPago, :p_IdCuentaBancaria,:v_out_ErroresDetalle, :v_out_AdvertenciasDetalle, :v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN				
				p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			ELSEIF :v_resultadoRegla = 'ADVE' THEN
				--LLamada al servicio de autorizacion si se requiere
				p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_out_AdvertenciasDetalle;
				SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		
			END IF;
	
		END;
	

		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    SELECT "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL INTO v_IdPreferenciaPagoPortafolio FROM DUMMY;
	
			p_out_RefId:=v_IdPreferenciaPagoPortafolio;
			
		 	------------------------------------------
			--Crear
			------------------------------------------


					insert into "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
					(					
						"IdPreferenciaPagoPortafolio",
						"IdPortafolio",
						"IdPreferenciaPago",
						--"IdPersonaRelacionada",
						"IdCuentaBancaria",
						"IdTarjetaBancaria",
					   	"FechaCreacion",
						"FechaModificacion",
						"IdUsuarioCreador",
						"IdEstado",
						"IdEmpresa",			
						"FechaIngreso",					 
						"IdEstadoAutorizacion",
						"PorDefecto"
						) VALUES (
						v_IdPreferenciaPagoPortafolio,
						p_IdPortafolio,
						p_IdPreferenciaPago,
						p_IdCuentaBancaria,
						p_IdTarjetaBancaria,
						CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
						CURRENT_UTCTIMESTAMP,
						p_IdUsuario,
						p_IdEstado,
						p_IdEmpresa,
						v_FechaIngreso,
						v_IdEstadoAutorizacion,
						p_PorDefecto);
									
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

				DECLARE	 v_Glosa		NVARCHAR(255);
				DECLARE v_out_idAuth 		bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
--				SELECT CAST("NombreCompleto" AS NVARCHAR(255))  into  v_Glosa from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona"=p_IdPersona;		
						v_Glosa:= p_IdPreferenciaPago;
						CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
							p_IdEmpresa => p_IdEmpresa,
							p_IdUsuario => p_IdUsuario,
							p_IdModulo => p_IdModulo,
							p_UsaReglasNegocio => 'N',								
							p_IdTipoEvento => v_IdConcepto,--'NUEDOC',
							p_IdModuloSolicita => p_IdModulo,
							p_IdUsuarioSolicita => p_IdUsuario,
							p_Fecha => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'), 
							p_IdConcepto => v_IdConcepto,
							p_NumeroOperacionConcepto =>  CAST (v_IdPreferenciaPagoPortafolio AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
							p_FechaOperacionConcepto => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
							p_IdCliente =>0, --v_IdCliente,
							p_Glosa => v_Glosa,
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
			 if 	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
						
					else
						--Mensaje de Salida
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
						v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
												     select * from :p_out_AdvertenciasDetalle;
						SIGNAL SALIDA_CONTROLADA;
			  end if;								
			              	                                     
					                                     
			else	--Confirma la creacion y cambia el estado del documento

				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.Portafolio.BD.Servicios::ConfirmarCrearPreferenciaPago"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					v_IdPreferenciaPagoPortafolio,
					v_IdEstadoAutorizacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle);

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
						v_IdPreferenciaPagoPortafolio,
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
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;


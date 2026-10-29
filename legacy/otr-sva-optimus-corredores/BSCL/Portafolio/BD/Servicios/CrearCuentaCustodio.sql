SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::CrearCuentaCustodio";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de cuenta custodio
--  Autor: JG - BCS
--  Fecha Creacion: 05/07/2016
-- 

"BSCL.Portafolio.BD.Servicios::CrearCuentaCustodio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			-- ID  Empresa destino
	IN  p_IdUsuario					BIGINT,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdPortafolio 				BIGINT,
	IN  p_IdBoveda					NVARCHAR(3), 
	IN  p_Codigo					NVARCHAR(100),
	IN  p_IdEstado					NVARCHAR(3),
	
	IN p_IdTipoCuentaCustodia		NVARCHAR(40) DEFAULT '',
	IN p_IdTipoCustodia				NVARCHAR(40) DEFAULT '',
	IN p_IdModeloCustodia			NVARCHAR(40) DEFAULT '',
	IN p_IdPersonaBancoCustodio		BIGINT DEFAULT 0,
	IN p_IdPersonaBancoLiquidador	BIGINT DEFAULT 0,
	
	IN p_LiquidaDividendos			NVARCHAR(1) DEFAULT 'N',
	IN p_LiquidaCupones				NVARCHAR(1) DEFAULT 'N',
	IN p_LiquidaVencimentos			NVARCHAR(1) DEFAULT 'N',
	IN p_LiquidaPrepagos			NVARCHAR(1) DEFAULT 'N',
	
	
	
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))  DEFAULT EMPTY
	
	,OUT p_out_idAuth int
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
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdCuentaCustodioPortafolio	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NPCCUS';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
		
       	DECLARE v_countIdPortafolio 	INT:=0;
		DECLARE v_IdEntidad				BIGINT;
		DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'CUENTACUSTODIO';
		DECLARE v_TipoOperacion			NVARCHAR(1):= 'I';
				
				
		BEGIN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			------------------------------------------
            --Pregunta Si requiere autorizacion
            ------------------------------------------
            V_RegistroSolicitudCambio = SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
            
            call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
                :p_IdEmpresa,
                :p_IdUsuario,
                :p_IdModulo,
                :V_RegistroSolicitudCambio,
                :v_IdConcepto,
                :v_RequiereAutorizacion
            );  

			IF v_RequiereAutorizacion='N' THEN
				v_IdEstadoAutorizacion := 'AUT';
			END IF;
		 	------------------------------------------
			--Crear
			------------------------------------------
			-- recuperamos numero de secuencia que usaremos para el id 
            SELECT "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio".NEXTVAL INTO v_IdCuentaCustodioPortafolio FROM DUMMY;
            p_out_RefId:=v_IdCuentaCustodioPortafolio;
            
			INSERT INTO "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
			(
				"IdCuentaCustodioPortafolio",
			  	"IdPortafolio",
			   	"IdBoveda",
			   	"Codigo",
			   	"FechaIngreso",
			   	"FechaCreacion",
				"FechaModificacion",
				"IdUsuarioCreador",
				"IdEstado",
				"IdEstadoAutorizacion",
				"IdTipoCuentaCustodia",
				"IdTipoCustodia",
				"IdModeloCustodia",
				"IdPersonaBancoCustodio",
				"IdPersonaBancoLiquidador",
				"LiquidaDividendos",
				"LiquidaCupones",
				"LiquidaVencimientos",
				"LiquidaPrepagos"
			) VALUES 
			(
				v_IdCuentaCustodioPortafolio,
				p_IdPortafolio,
				p_IdBoveda,
				p_Codigo,
				v_FechaIngreso,
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				p_IdUsuario,
				p_IdEstado,
				v_IdEstadoAutorizacion,
				p_IdTipoCuentaCustodia,
				p_IdTipoCustodia,
				p_IdModeloCustodia,
				p_IdPersonaBancoCustodio,
				p_IdPersonaBancoLiquidador,
				p_LiquidaDividendos,
				p_LiquidaCupones,
				p_LiquidaVencimentos,
				p_LiquidaPrepagos
			);
	
			IF v_RequiereAutorizacion='S' THEN
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------
				DECLARE	v_Glosa				NVARCHAR(255);
				DECLARE v_out_idAuth 		BIGINT;
				DECLARE v_out_AuthAdvertenciasContador 	INT;
				DECLARE v_out_AuthErroresContador 		INT;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
				SELECT CLI."DocumentoIdentidad" || ' / '|| CLI."NombreCompleto"  || ' / CodPort: ' || POR."CodigoPortafolio" ||  ' / Cuenta Custodio: ' || p_Codigo  
					INTO  v_Glosa 
				FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR 
					INNER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
						ON (POR."IdCliente" = CLI."IdCliente")
				WHERE POR."IdPortafolio" = p_IdPortafolio;		

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
					p_NumeroOperacionConcepto =>  CAST (v_IdCuentaCustodioPortafolio AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
					p_FechaOperacionConcepto => TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'),
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
				 
				 p_out_idAuth := v_out_idAuth; 
						                                         
				 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
				                        From :v_out_AuthErroresDetalle Union All 
				                        Select "Codigo", "Descripcion" 
				                        From :p_out_ErroresDetalle; 
			 	if p_out_ErroresContador > 0 then
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
			else	
			
				v_IdEstadoAutorizacion:='AUT';
				p_CuentaCustodioPortafolio = SELECT :v_IdCuentaCustodioPortafolio as "IdCuentaCustodioPortafolio" FROM DUMMY;
				CALL "BSCL.Portafolio.BD.Servicios::ConfirmarCrearCuentaCustodioMasivo"(
					p_IdEmpresa 				=> :p_IdEmpresa,
					p_IdUsuario 				=> :p_IdUsuario,
					p_IdModulo 					=> :p_IdModulo,
					p_UsaReglasNegocio 			=> :p_UsaReglasNegocio,
					p_CuentaCustodioPortafolio	=> :p_CuentaCustodioPortafolio,
					p_IdEstadoAutorizacion 		=> v_IdEstadoAutorizacion,
					p_out_AdvertenciasContador	=> v_out_AdvertenciasContador,
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
						
				v_IdEntidad := :p_out_RefId;
						
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
			END IF;
			
		END;		 
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

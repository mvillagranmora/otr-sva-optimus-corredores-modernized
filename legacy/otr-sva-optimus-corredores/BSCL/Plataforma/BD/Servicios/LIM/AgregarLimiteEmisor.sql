SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.AgregarLimiteEmisor" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para agregar Emisores a un Limite.
--  Autor: CFlores - BCS
--  Fecha Creacion: 13/05/2016
-- 

"BSCL.Plataforma.BD.Servicios::LIM.AgregarLimiteEmisor" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

   IN  p_IdEmisor			       NVARCHAR(40),
   IN  p_IdLimite			       BIGINT,
   IN  p_IdEstado			       NVARCHAR(3),
  
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
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
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
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
		DECLARE v_IdLimiteEmisor			 	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NELCTP';--Nuevo Emisor Limite Cta Propia
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
    	DECLARE v_IdEntidadBase					BIGINT:=0;
       	DECLARE v_IdCliente			 			bigint:=0;
		DECLARE v_NombreEmisor					NVARCHAR(100):='';
		DECLARE v_Count			 				bigint:=0;

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor"
		WHERE "IdEmisor" = p_IdEmisor
		AND "IdLimite" = p_IdLimite;
	
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'EMISOR_LIMITE_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"
		WHERE "IdEmisor" = p_IdEmisor
		AND "IdEstado" = 'ACT';
	
		IF(v_Count = 0) THEN
			p_out_ErroresDetalle = select 'EMISOR_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		
		SELECT COUNT(1) into v_Count
	    FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	   	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
		WHERE EMP."IdEmpresa" = p_IdEmpresa
		AND CLI."IdEstado" = 'ACT';
			    		
		IF v_Count = 0 THEN
			p_out_ErroresDetalle = SELECT 'UI_ENTIDAD_NO_ES_CUENTA_PROPIA' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			SELECT CLI."IdPersona" INTO v_IdEntidadBase
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
			WHERE EMP."IdEmpresa" = p_IdEmpresa
			AND CLI."IdEstado" = 'ACT';
		END IF;
		
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    SELECT "BSCL.Plataforma.BD.Secuencias::LIM.IdLimiteEmisor".NEXTVAL INTO v_IdLimiteEmisor FROM DUMMY;
	
			p_out_RefId:=v_IdLimiteEmisor;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			insert into "BSCL.Plataforma.BD::LIM.LimiteEmisor"(
				"IdLimiteEmisor",
				"IdEmisor",
				"IdLimite",
				"IdEstado",
				"FechaIngreso",
				"FechaCreacion",
				"FechaModificacion",
				"IdEstadoAutorizacion",
				"IdUsuarioCreador"
			) VALUES (
				v_IdLimiteEmisor,
				p_IdEmisor,
				p_IdLimite,
				p_IdEstado,
				v_FechaIngreso,						
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				v_IdEstadoAutorizacion,
				p_IdUsuario
			);
	
   			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			V_RegistroSolicitudCambio = SELECT * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
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

				DECLARE	v_Glosa							NVARCHAR(520);
				DECLARE v_out_idAuth					bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
				SELECT /*"Nombre"*/ "IdEmisor" INTO v_NombreEmisor	FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"
				WHERE "IdEmisor"=p_IdEmisor
				AND "IdEstado"= 'ACT';
	
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / Limite: ' || v_NombreEmisor into v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = v_IdEntidadBase;
				
				v_tablaEventos = SELECT
									v_IdConcepto AS "IdTipoEvento",              		 
								    p_IdModulo 	 AS "IdModuloSolicita",
								    p_IdUsuario	 AS "IdUsuarioSolicita",
								    TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD') AS "Fecha",  
								    '' AS "IdTipoOperacion",
								    v_IdConcepto AS	"IdConcepto",
								    CAST (v_IdLimiteEmisor AS NVARCHAR(40)) AS "NumeroOperacionConcepto",
								    TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD') AS "FechaOperacionConcepto",
								    0  AS "IdCliente",
								    0  AS "IdFuncionario",
								    0  AS "IdProveedor",
								    '' AS "IdTipoProducto",
									'' AS "IdClasificacionTipoInstrumento",
									'' AS "IdTipoInstrumento",
									0  AS "IdInstrumento",
									'' AS "Nemo",
								    0  AS "Cantidad",
								    0  AS "Monto",
								    0  AS "Precio",
								    0  AS "Tasa",
								    '' AS "IdMoneda",
								    v_Glosa AS "Glosa",
								    0 as "IdUnion" 
								FROM DUMMY;
				----------------------------------------------------------------------------------------------------------------							
		        v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
		   									from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
		   									where "IdEventoDetalle" = 0;
		   		-----------------------------------------------------------------------------------------------------------------
				CALL"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo"(
						p_IdEmpresa			=> p_IdEmpresa,
						p_IdUsuario			=> p_IdUsuario,
						p_IdModulo			=> p_IdModulo,
						p_UsaReglasNegocio	=> :p_UsaReglasNegocio,
					    p_simularOperacion	=> 'N',
						p_tablaEventos		=> :v_tablaEventos,	
						p_tablaDetalle		=> :v_tablaEventosDetalle,
						--salida
						p_out_IdEvento				=> v_out_idAuthtable,
						p_out_AdvertenciasContador	=> v_out_AuthAdvertenciasContador,
						p_out_AdvertenciasDetalle	=> v_out_AuthAdvertenciasDetalle,
						p_out_ErroresContador		=> v_out_AuthErroresContador,
						p_out_ErroresDetalle		=> v_out_AuthErroresDetalle);
				
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
				--Confirma la creacion y cambia el estado del documento
				v_IdEstadoAutorizacion:='ING';

				v_tablaIdLimite = SELECT v_IdLimiteEmisor AS "IdLimiteEmisor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::LIM.ConfirmarAgregarLimiteEmisor"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					:v_tablaIdLimite,
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
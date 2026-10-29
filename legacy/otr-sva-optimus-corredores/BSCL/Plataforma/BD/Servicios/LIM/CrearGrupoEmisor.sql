SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.CrearGrupoEmisor" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de Grupo Emisor
--  Autor: Mmachmar - BCS
--  Fecha Creacion: 24/06/2016
-- 

"BSCL.Plataforma.BD.Servicios::LIM.CrearGrupoEmisor" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa			bigint,			-- ID  Empresa destino
	IN  p_IdUsuario			bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo			nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN  p_IdGrupoEmisor		NVARCHAR(40),
	IN  p_Descripcion		NVARCHAR(100),
	IN  p_IdEstado			NVARCHAR(3),
	
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
		DECLARE	v_IdGrupoEmisor				NVARCHAR(40):= UPPER(p_IdGrupoEmisor);
		
		DECLARE v_FechaIngreso				SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       		NVARCHAR(6):='NGELCP';
		DECLARE v_RequiereAutorizacion		NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion		NVARCHAR(3):='PEN';
		DECLARE v_count						INT:=0;
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN 
			SELECT COUNT(1) INTO v_count FROM "BSCL.Plataforma.BD::LIM.GrupoEmisor" WHERE "IdGrupoEmisor"=v_IdGrupoEmisor;
			
			IF(v_count > 0) THEN
				p_out_ErroresDetalle =  select 'GRUPO_EMISOR_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
					
		END;
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			p_out_RefId:=1;--v_IdGrupoEmisor;
			
		 	------------------------------------------
			--Crear Grupo Emisor
			------------------------------------------

			insert into "BSCL.Plataforma.BD::LIM.GrupoEmisor"(
				"IdGrupoEmisor",
				"IdEmpresa",
				"Descripcion",
				"IdEstado",
				"FechaIngreso",
				"FechaCreacion",
				"FechaModificacion",
				"IdEstadoAutorizacion",
				"Borrado",
				"IdUsuarioCreador"						
				)
			VALUES (
				v_IdGrupoEmisor,
				p_IdEmpresa,
				p_Descripcion,
				p_IdEstado,
				v_FechaIngreso,
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				v_IdEstadoAutorizacion,
				'N',
				p_IdUsuario
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
	
				DECLARE	v_Glosa			NVARCHAR(255);
				DECLARE v_out_idAuth	bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
				v_Glosa:=p_Descripcion;
				
				v_tablaEventos = SELECT
									v_IdConcepto AS "IdTipoEvento",              		 
								    p_IdModulo 	 AS "IdModuloSolicita",
								    p_IdUsuario	 AS "IdUsuarioSolicita",
								    TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD') AS "Fecha",
								    '' AS "IdTipoOperacion",
								    v_IdConcepto AS	"IdConcepto",
								    v_IdGrupoEmisor AS "NumeroOperacionConcepto",
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
						p_IdEmpresa => p_IdEmpresa,
						p_IdUsuario => p_IdUsuario,
						p_IdModulo => p_IdModulo,
						p_UsaReglasNegocio=> :p_UsaReglasNegocio,
					    p_simularOperacion=> 'N',
						p_tablaEventos => :v_tablaEventos,
						p_tablaDetalle => :v_tablaEventosDetalle,
						--salida
						p_out_IdEvento => v_out_idAuthtable,
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
				v_IdEvento = SELECT v_IdGrupoEmisor as "IdGrupoEmisor" FROM DUMMY;				
				CALL "BSCL.Plataforma.BD.Servicios::LIM.ConfirmarCrearGrupoEmisor"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					:v_IdEvento,
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
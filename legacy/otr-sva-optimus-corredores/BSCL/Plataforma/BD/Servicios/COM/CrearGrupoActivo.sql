SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearGrupoActivo" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion del Grupo Activo.
--  Autor: CFlores - BCS
--  Fecha Creacion: 30/03/2016
-- 

"BSCL.Plataforma.BD.Servicios::COM.CrearGrupoActivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN p_IdGrupoActivos						NVARCHAR(40),	
	IN p_DescripcionGrupoActivos			NVARCHAR(100),		
	IN p_IdTipoGrupoActivos					NVARCHAR(40),			
	IN p_IdEstado							NVARCHAR(3),	
	IN p_PorDefecto							NVARCHAR(1),
	
	
	IN p_DetallePerfil TABLE(
		"p_IdInstrumento"					BIGINT	  	
		,"p_IdEstado"						NVARCHAR(3)		
	),
		
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
		--DECLARE v_IdPerfilCobros			 	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NCGACT';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    --SELECT "BSCL.SocioNegocio.BD.Secuencias::Telefono".NEXTVAL INTO v_IdPerfilCobros FROM DUMMY;
			--v_IdPerfilCobros:=p_IdGrupoActivos;
	
			p_out_RefId:=p_IdGrupoActivos;
			
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------


					insert into "BSCL.Plataforma.BD::COM.GrupoActivos"
					(
						"IdGrupoActivos",
						"DescripcionGrupoActivos",
						"IdTipoGrupoActivos",	
						"FechaCreacion",
						"FechaModificacion",
						"IdEstado",
						"FechaIngreso",
						"IdEstadoAutorizacion",
						"IdUsuarioCreador",	
						"PorDefecto",
						"IdEmpresa"	
						) VALUES (
						  p_IdGrupoActivos,
						  p_DescripcionGrupoActivos,
						  p_IdTipoGrupoActivos,
						  CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
						  CURRENT_UTCTIMESTAMP,	
						  p_IdEstado,
						  v_FechaIngreso,
						  v_IdEstadoAutorizacion,
						  p_IdUsuario,
						  p_PorDefecto,
						  p_IdEmpresa);

				
		  INSERT INTO "BSCL.Plataforma.BD::COM.DetalleGrupoActivos" (
				"IdDetalleGrupoActivos", 
				"IdGrupoActivos",
				"IdInstrumento",
				"FechaCreacion",
			 	"FechaIngreso",
				"FechaModificacion",
				"IdEstadoAutorizacion",
				"IdUsuarioCreador",	
				"IdEstado",
				"IdEmpresa"
			)
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::DetalleGrupoActivos".NEXTVAL as "IdDetalleGrupoActivos",
				:p_IdGrupoActivos,
				"p_IdInstrumento",
				CURRENT_UTCTIMESTAMP,
				v_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7') 						
				CURRENT_UTCTIMESTAMP,
				v_IdEstadoAutorizacion,
				p_IdUsuario,
				"p_IdEstado",
				p_IdEmpresa
			FROM
				:p_DetallePerfil;
	
			
						
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
				
				--SELECT CAST("NombreCompleto" AS NVARCHAR(255))  into  v_Glosa from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona"=p_IdPersona;		

						v_Glosa:=p_DescripcionGrupoActivos;
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
							p_NumeroOperacionConcepto =>  p_IdGrupoActivos,--v_secuencia_IdSolicitudCambio,
							p_FechaOperacionConcepto => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
							p_IdCliente => 0, --v_IdCliente,
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
				                                     
					                                     
			else	--Confirma la creacion y cambia el estado del documento

				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.Plataforma.BD.Servicios::COM.ConfirmarCrearGrupoActivo"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					p_IdGrupoActivos,
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
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::CrearAnotacionPortafolio" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de anotaciones de portafolio.
--  Autor: CAPONTE - BCS
--  Fecha Creacion: 07/02/2016
-- 

"BSCL.Portafolio.BD.Servicios::CrearAnotacionPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
   	IN  p_IdPortafolio					BIGINT,
   	IN  p_Titulo						NVARCHAR(100),
   	IN  p_Detalle						NVARCHAR(500),
   	IN  p_IdMotivo						NVARCHAR(6),
   	IN  p_IdAmbito						NVARCHAR(6),
   	IN  p_IdRelevancia					NVARCHAR(6),
   	IN  p_FechaDesde					NVARCHAR(10),
   	IN  p_FechaHasta					NVARCHAR(10),


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
		
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		DECLARE v_countIdPortafolio 			INT:=0;
		DECLARE v_IdEntidad						BIGINT;
		DECLARE v_TipoEntidad					NVARCHAR(40):= 'ANOTACIONPORTAFOLIO';
		DECLARE v_TipoEntidadReplica			NVARCHAR(50):= 'ANOTACION';
		DECLARE v_TipoOperacion					NVARCHAR(1):= 'I';
	
		--Variables Logs
		DECLARE v_IdCliente						BIGINT;
		DECLARE v_EstadoCliente					NVARCHAR(3);
		DECLARE	v_NombreCliente					NVARCHAR(100);
		DECLARE	v_IdTipoDocumentoIdentidad		NVARCHAR(40);
		DECLARE	v_IdDocumentoIdentidad			NVARCHAR(40);
		DECLARE v_fechaHoraSistema 				NVARCHAR(40);
		DECLARE v_NombreProceso					NVARCHAR(100):='Ingreso Anotacion Portafolio';
		DECLARE v_subregistro 					NVARCHAR(40):= '';
		DECLARE v_motivo						NVARCHAR(100):='';
		DECLARE v_titulo						NVARCHAR(100):='';
		DECLARE v_detalle						NVARCHAR(100):='';
		DECLARE v_fechaDesde					NVARCHAR(40):='';
		DECLARE v_fechaHasta					NVARCHAR(40):='';
		DECLARE v_ambito						NVARCHAR(100):='';
		DECLARE v_relevancia					NVARCHAR(100):='';
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		---------------------------------------------------

					
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			SELECT "BSCL.Portafolio.BD.Secuencias::Anotacion".NEXTVAL INTO p_out_RefId from dummy;

		 	------------------------------------------
			--Crear Anotacion Portafolio
			------------------------------------------
					insert into "BSCL"."BSCL.Portafolio.BD::POR.Anotacion" 
					(
						"IdAnotacion",
						"IdPortafolio",
						"FechaAnotacion",
						"IdMotivoAnotacion",
						"Titulo",
						"Detalle",
						"FechaIngreso",
						"FechaCreacion",
						"FechaModificacion",
						"IdUsuarioCreador",
						"IdRelevanciaAnotacion",
						"IdAmbitoAnotacion",
						"ValidoDesde",
						"ValidoHasta"					
					)
					values(	p_out_RefId,
							p_IdPortafolio,
							v_FechaIngreso,
							p_IdMotivo,
							p_Titulo,
							p_Detalle,
							v_FechaIngreso,
							CURRENT_UTCTIMESTAMP,
							CURRENT_UTCTIMESTAMP,
							p_IdUsuario,
							p_IdRelevancia,
							p_IdAmbito,
							p_FechaDesde,
							p_FechaHasta);

			--PRONETIT
			
			v_IdEntidad	:= :p_out_RefId;

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
					p_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle,
					p_out_ErroresContador,
					p_out_ErroresDetalle
				);
	                
	            IF	p_out_ErroresContador > 0 then
					SIGNAL SALIDA_CONTROLADA;
			  	END if;
            END IF;  
           
           --LOG DE AUDITORIA (inicio)
			BEGIN 
				
				--DATOS CABECERA LOG
				SELECT  CLI."IdCliente"
					   ,CLI."NombreCompleto"
					   ,CLI."TipoDocumentoIdentidad"
					   ,CLI."DocumentoIdentidad"
					   ,CLI."IdEstado"
				INTO v_IdCliente,
				     v_NombreCliente,
					 v_IdTipoDocumentoIdentidad,
					 v_IdDocumentoIdentidad,
					 v_EstadoCliente					   
				 FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
				 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
				 WHERE POR."IdPortafolio" = :p_IdPortafolio;
					
				--DATOS DETALLE LOG
				SELECT 
					COALESCE("ANOTA"."NombreMotivo", '') 											AS "Motivo",
					COALESCE("ANOTA"."Titulo",'')													AS "Titulo",
					COALESCE("ANOTA"."Detalle",'')													AS "Detalle", 
					TO_NVARCHAR(COALESCE("ANOTA"."ValidoDesde", '0001-01-01'), 'YYYY-MM-DD')		AS "FechaDesde", 
					TO_NVARCHAR(COALESCE("ANOTA"."ValidoHasta", '0001-01-01'),'YYYY-MM-DD')			AS "FechaHasta", 
					COALESCE("ANOTA"."NombreAmbito", '')											AS "Ambito",
					COALESCE("ANOTA"."NombreRelevancia" , '')										AS "Relevancia"  
				INTO 
					v_motivo,
					v_titulo,
					v_detalle,
					v_fechaDesde,
					v_fechaHasta,
					v_ambito,
					v_relevancia
					FROM "BSCL.Portafolio.BD.Vistas::ConsultarAnotaciones" "ANOTA"  
					WHERE "ANOTA"."IdAnotacion" = :p_out_RefId;

				v_subregistro:= v_TipoEntidad;
			
				IF(v_fechaDesde = '0001-01-01')THEN
					
					v_fechaDesde:='';
				
				END IF;
			
				IF(v_fechaHasta = '0001-01-01')THEN
					
					v_fechaHasta:='';
				
				END IF;
			
				p_DetalleLog = (SELECT v_subregistro AS "SubRegistro", 'Motivo' AS "CampoRegistro", '' AS "ValorAntiguo", v_motivo AS "ValorNuevo"
									FROM dummy
								UNION ALL 
								SELECT v_subregistro AS "SubRegistro", 'Titulo' AS "CampoRegistro", '' AS "ValorAntiguo", v_titulo AS "ValorNuevo"
									FROM dummy
								UNION ALL
								SELECT v_subregistro AS "SubRegistro", 'Detalle' AS "CampoRegistro", '' AS "ValorAntiguo", v_detalle AS "ValorNuevo"
									FROM dummy
								UNION ALL
								SELECT v_subregistro AS "SubRegistro", 'ValidoDesde' AS "CampoRegistro", '' AS "ValorAntiguo", v_fechaDesde AS "ValorNuevo"
									FROM dummy
								UNION ALL 
								SELECT v_subregistro AS "SubRegistro", 'ValidoHasta' AS "CampoRegistro", '' AS "ValorAntiguo", v_fechaHasta AS "ValorNuevo"
									FROM dummy
								UNION ALL 
								SELECT v_subregistro AS "SubRegistro", 'Relevancia' AS "CampoRegistro", '' AS "ValorAntiguo", v_relevancia AS "ValorNuevo"
									FROM dummy
								UNION ALL 
								SELECT v_subregistro AS "SubRegistro", 'Ambito' AS "CampoRegistro", '' AS "ValorAntiguo", v_ambito AS "ValorNuevo"
									FROM dummy);

				--GRABAR LOG
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'INGRESO' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => :v_TipoEntidad--'ANOTACIONPORTAFOLIO'
					,p_DetalleLog => :p_DetalleLog	
					,p_NombreProceso => :v_NombreProceso
					,p_IdRegistro => :p_out_RefId
					,p_EstadoProceso => 'ING'
					,p_EstadoRegistro=> 'ING'
					,p_EstadoCliente=> :v_EstadoCliente 
					,p_IdCliente => :v_IdCliente
					,p_NombreCliente=> :v_NombreCliente
					,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad						
				);
				
			END;
			--LOG DE AUDITORIA (Fin)				
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

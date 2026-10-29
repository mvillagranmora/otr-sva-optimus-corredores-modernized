SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::UAF.NotificarEvaluacionSOI";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para generar notificacion al oficial de cumplimiento desde la evaluacion SOI.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/01/2016

"BSCL.Normativa.BD.Servicios::UAF.NotificarEvaluacionSOI" ( 

    --VARIABLES DEL PROCESO
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN p_TablaNotificacion	  	TABLE("IdEvaluacion"							BIGINT),
    --VARIABLES DE SALIDA OBLIGATORIAS

	-- Parametros salida del servicio publico
	OUT p_out_IdEvento table ("IdEvento" BIGINT),
--	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER 			FOR SQLEXCEPTION
	
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

	BEGIN
		--salida/error/advertencia controlado por este servicio
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_out_Agregar_Evento TABLE("IdEvento" BIGINT,"IdSecuencia" BIGINT);
		DECLARE v_GEN_IDPERSONA_CUMPLIMIENTO1	 	NVARCHAR(520):='';
		DECLARE	v_Count,v_IdPersona,v_IdPersonaCumplimiento,v_IdDireccionDestinatario		BIGINT:=0;
		DECLARE v_NombreDestinatario		 	NVARCHAR(40):='';
		
		
		
		--------------------
		--Lógica Negocio
		--------------------
		
		--Inicializacion de variables:
	
		BEGIN -- NOTIFICACIONES 
		
		SELECT COUNT(1) INTO v_count FROM :p_TablaNotificacion;
		
		IF v_count > 0 THEN 	
		
		
			----Parametro de Oficial de Cumplimiento
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='GEN_IDPERSONA_CUMPLIMIENTO1' AND "IdNormativa"='';
			IF v_Count>0 THEN
				SELECT "Valor" INTO v_GEN_IDPERSONA_CUMPLIMIENTO1
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='GEN_IDPERSONA_CUMPLIMIENTO1' AND "IdNormativa"='';
				ELSE
					v_GEN_IDPERSONA_CUMPLIMIENTO1:='0';
			END IF;
			
			v_IdPersonaCumplimiento=CAST(TRIM(v_GEN_IDPERSONA_CUMPLIMIENTO1) AS BIGINT);
			IF v_Count>0 THEN
								
						---Datos Oficial de Cumplimientos
			
							SELECT PER."IdDireccionElectronicaPrincipal",PER."NombreCompleto"
							INTO v_IdDireccionDestinatario,v_NombreDestinatario
							FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
								WHERE PER."IdPersona"=v_IdPersonaCumplimiento;
								
								
						----NOTIFICACION A CUMPLIMIENTO
						v_GrupoEventoNotificacion = SELECT 
								ROW_NUMBER()over ( order by EVA."IdCliente",EVA."IdPortafolio") AS "IdInterno",
								EVA.* 
								,CLI."NombreCompleto"
								,CLI."DocumentoIdentidad"
								,CCDG."RazonSocial" AS "Corredora"
								,CCDG."Telefono" AS "AtencionCliente"
								,v_NombreDestinatario AS "NombreDestinatario"
								,EJE."NombreCompleto" AS "Agente",
								LC."RutaPublica" AS "UrlLogoCorredor"
								FROM :p_TablaNotificacion TNO 
								INNER JOIN "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" EVA
								ON (TNO."IdEvaluacion"=EVA."IdEvaluacion")	
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
								ON (CLI."IdCliente"=EVA."IdCliente")
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" EJE
								ON CLI."IdPersonaEjecutivo" = EJE."IdPersona"
								INNER JOIN   "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS CCDG ON (CCDG."IdEmpresa"=:p_IdEmpresa)
								INNER JOIN   "BSCL.Plataforma.BD::PLA.Imagenes" AS LC  ON  "NombreImagen"='LOGO_CORREDOR' AND (LC."IdEmpresa"=:p_IdEmpresa);
			
			

							
								
						v_Eventos = 
							 SELECT 
							CAST('CUMPLIMIENTO_SOI' AS NVARCHAR(40))	AS "CodigoTipoEvento", 
							NOF."IdInterno" 					AS "IdSecuencia", 
							CURRENT_UTCTIMESTAMP 				AS "FechaCreacionSistema", 
							0 									AS "IdPersonaRemitente", 
							v_IdPersonaCumplimiento 			AS "IdPersonaDestinatario", 
							0 									AS "IdPortafolio" 
							FROM  :v_GrupoEventoNotificacion NOF;
											
							v_IdDirecciones =
							SELECT NOF."IdInterno"					  AS "IdSecuencia",
									v_IdDireccionDestinatario 		  AS "IdDireccion",
								'TO' 								  AS "TipoDestinatario"
							FROM  :v_GrupoEventoNotificacion NOF;
							
							v_ArchivosAdjuntos =
							SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
							
							--Datos del Cliente en mail
														
						v_EventoDetalle =   						--- DATOS CLIENTE
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'NombreCompleto' AS "Llave",  
							NOF."NombreCompleto" AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'DocumentoIdentidad' AS "Llave",  
							NOF."DocumentoIdentidad" AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'IdCliente' AS "Llave",  
							CAST (NOF."IdCliente" AS  NVARCHAR(100)) AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION 
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'MontoUtilizado' AS "Llave",  
							CAST (NOF."MontoUtilizado" AS  NVARCHAR(100)) AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'Moneda' AS "Llave",  
							CAST (NOF."IdMoneda" AS  NVARCHAR(100)) AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION  
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'IdOperacion' AS "Llave",  
							CAST (NOF."IdOperacion" AS  NVARCHAR(100)) AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION 
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'TipoOperacion' AS "Llave",  
							COALESCE(OPE."Nombre",'') AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE
							ON (NOF."IdTipoOperacion"=OPE."IdTipoOperacion")
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'TipoProducto' AS "Llave",  
							COALESCE(PRO."Nombre",'') AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRO
							ON (PRO."IdTipoProducto"=NOF."IdTipoProducto")
							UNION    											      
							SELECT NOF."IdInterno" AS "IdSecuencia",  -----DATOS DESTINATARIO
							'HoraRegistro' AS "Llave",  
							TO_NVARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY HH:MI') AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'CORREDORA' AS "Llave",  
							TRIM(NOF."Corredora") AS "Valor"  
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'ATENCIONCLIENTE' AS "Llave", 
							TRIM(NOF."AtencionCliente") AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'NombreDestinatario' AS "Llave", 
							TRIM(NOF."NombreDestinatario") AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'AGENTE' AS "Llave", 
							TRIM(NOF."Agente") AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF
							UNION
							SELECT NOF."IdInterno" AS "IdSecuencia", 
							'LOGO_CORREDORA' AS "Llave", 
							TRIM(NOF."UrlLogoCorredor") AS "Valor" 
							FROM :v_GrupoEventoNotificacion NOF;
							
							
						
						
						
							v_out_Agregar_Evento = SELECT 0 AS "IdEvento",0 as "IdSecuencia" FROM DUMMY WHERE 1=2;	
								
				 				CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
				 					p_IdEmpresa => p_IdEmpresa,
				 					p_IdUsuario => p_IdUsuario,
				 					p_IdModulo => p_IdModulo,
				 					p_Eventos => :v_Eventos,
				 					p_Valores => :v_EventoDetalle,
				 					p_ArchivosAdjuntos => :v_ArchivosAdjuntos,
				 					p_IdDirecciones => :v_IdDirecciones,
				 					p_AplicaOrigenEvento  =>'S',
				 					p_out_IdEvento => :v_out_Agregar_Evento
				 					);
				 		p_out_IdEvento=SELECT "IdEvento"  FROM :v_out_Agregar_Evento;	
			END IF;	 		
	 		
		END IF;
			
		END;
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

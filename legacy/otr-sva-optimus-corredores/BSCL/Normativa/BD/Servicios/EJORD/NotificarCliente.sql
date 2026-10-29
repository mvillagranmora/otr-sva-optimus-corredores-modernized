SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::EJORD.NotificarCliente";
GO

CREATE PROCEDURE 
--
--  Proposito: Servicio para generar notificacion a la nomina con la Ejecucion de Ordenes del periodo.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 16/02/2017

"BSCL.Normativa.BD.Servicios::EJORD.NotificarCliente" ( 

    --VARIABLES DEL PROCESO
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN p_TablaNotificacion	  	TABLE("IdEjecucion"							BIGINT),
--	IN p_Periodo        				 	  NVARCHAR(6),
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
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
    
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

	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_out_Agregar_Evento TABLE("IdEvento" BIGINT,"IdSecuencia" BIGINT);
		DECLARE v_count	BIGINT:=0;
		DECLARE v_IdPersonaRemitente        BIGINT;
        DECLARE v_IdDireccionPersonaRemitente BIGINT;
		--DECLARE v_FechaMinima, v_FechaMaxima NVARCHAR(10):='';
		--------------------
		--Lógica Negocio
		--------------------
		
		--Inicializacion de variables:
	
		BEGIN -- NOTIFICACIONES
		
    		SELECT CE."IdPersona", CE."IdDireccionElectronica"  INTO v_IdPersonaRemitente, v_IdDireccionPersonaRemitente
            FROM "BSCL.Plataforma.BD::ACC.Usuario" U
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"
            WHERE "IdUsuario" = :p_IdUsuario;
		
		SELECT COUNT(1) INTO v_count FROM :p_TablaNotificacion;
		
		IF v_count > 0 THEN 	
            
		--Generar grupo evento  de Notificar
			v_datos_basicos = 
				SELECT 
					ROW_NUMBER()over ( order by G."IdCliente",G."IdPortafolio") AS "IdInterno"
					,G."IdPersona"
					,G."IdPortafolio"
					,G."IdDireccionElectronicaPrincipal"
					,G."NombreCliente"
					,"BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha"(G."FechaMinima",'d') AS "FechaMinima"
					,"BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha"(G."FechaMaxima",'d') AS "FechaMaxima"
					, v_IdPersonaRemitente AS "IdPersonaRemitente"
					, v_IdDireccionPersonaRemitente AS "IdDireccionPersonaRemitente"
				FROM
					(	
						SELECT 
							 N."IdCliente"
 						 	,N."NombreCliente"
							,N."IdPersona"
							,N."IdPortafolio"
							,N."IdDireccionElectronicaPrincipal"
							,MAX(N."IdEjecucion") AS "IdEjecucion"
							,TO_VARCHAR (MIN(TO_DATE("FechaEjecucion")), 'YYYY-MM-DD')  as "FechaMinima"
							,TO_VARCHAR (MAX(TO_DATE("FechaEjecucion")), 'YYYY-MM-DD')  as "FechaMaxima"
						FROM :p_TablaNotificacion NOR  
						INNER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina" N ON (N."IdEjecucion"=NOR."IdEjecucion")
						
						GROUP BY  N."IdCliente",N."IdPortafolio",N."IdPersona",N."IdDireccionElectronicaPrincipal",N."NombreCliente" 
					 ) AS G;
					 
			v_GrupoEventoNotificacion = 
			    SELECT 
			        GNI.*
			        ,CC."IdPersona" AS "IdPersonaDestinatario"
                    ,CC."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronicaDestinatario"
                    ,CC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD"
                    ,CC."TipoDocumentoIdentidad" AS "TIPODOCUMENTOIDENTIDAD"
                    ,CEMP."NombreCompleto" AS "AGENTE"
                    ,CPT."Numero" AS "TELEFONO"
                    ,CPDE."Direccion" AS "CORREO" 
                    ,CCDG."RazonSocial" AS "CORREDORA"
                    ,CCDG."Telefono" AS "ATENCIONCLIENTE"
                    ,LC."RutaPublica" AS "LOGOCORREDORA"
                FROM :v_datos_basicos GNI    
                INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdPersona" = GNI."IdPersona"
                    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP ON CC."IdPersona" = CP."IdPersona"
                    LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CC."IdTelefonoPrincipal" = CPT."IdTelefono"
                    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CC."IdDireccionElectronicaPrincipal"
                    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CC."IdEmpleadoEjecutivo" = CEMP."IdEmpleado"
                    LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa
                    INNER JOIN   "BSCL.Plataforma.BD::PLA.Imagenes" AS LC  ON  "NombreImagen"='LOGO_CORREDOR' AND (LC."IdEmpresa"=:p_IdEmpresa);

			v_Eventos = 
				 SELECT 
				'PROCESOEJECUCIONORDENES_CLIENTE' 	AS "CodigoTipoEvento", 
				NOF."IdInterno" 					AS "IdSecuencia", 
				CURRENT_UTCTIMESTAMP 				AS "FechaCreacionSistema", 
				v_IdPersonaRemitente				AS "IdPersonaRemitente", 
				NOF."IdPersona" 					AS "IdPersonaDestinatario", 
				NOF."IdPortafolio" 					AS "IdPortafolio" 
				FROM  :v_GrupoEventoNotificacion NOF;
								
				v_IdDirecciones =
				SELECT NOF."IdInterno"					  AS "IdSecuencia",
					NOF."IdDireccionElectronicaPrincipal" AS "IdDireccion",
					'TO' 								  AS "TipoDestinatario"
				FROM  :v_GrupoEventoNotificacion NOF
				UNION ALL
				SELECT 
                    ROW_NUMBER() OVER (ORDER BY "IdInterno") AS "IdSecuencia",
                    v_IdDireccionPersonaRemitente AS "IdDireccion",
                    'CC' AS "TipoDestinatario"
                FROM  :v_GrupoEventoNotificacion NOF;
				
				v_ArchivosAdjuntos =
				SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
				
				--Datos del Cliente en mail
											
			v_EventoDetalle = 
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'CLIENTE' AS "Llave",  
				NOF."NombreCliente" AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'HORA' AS "Llave",  
				TO_NVARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY HH:MI') AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'DETALLEPERIODO' AS "Llave",  
				(CASE WHEN NOF."FechaMinima" = NOF."FechaMaxima" THEN  ' dia :' || NOF."FechaMinima"   ELSE
																	   ' periodo entre el  :' || NOF."FechaMinima" || ' y el  :' || NOF."FechaMaxima"  END) AS "Valor"
				FROM :v_GrupoEventoNotificacion NOF
				UNION
				SELECT NOF."IdInterno" AS "IdSecuencia", 
                'AGENTE' AS "Llave",  
                NOF."AGENTE" AS "Valor" 
                FROM :v_GrupoEventoNotificacion NOF
                UNION
                SELECT NOF."IdInterno" AS "IdSecuencia", 
                'DOCUMENTOIDENTIDAD' AS "Llave",  
                NOF."DOCUMENTOIDENTIDAD" AS "Valor" 
                FROM :v_GrupoEventoNotificacion NOF
				UNION
                SELECT NOF."IdInterno" AS "IdSecuencia", 
                'TIPODOCUMENTOIDENTIDAD' AS "Llave",  
                NOF."TIPODOCUMENTOIDENTIDAD" AS "Valor" 
                FROM :v_GrupoEventoNotificacion NOF
                 UNION
                SELECT NOF."IdInterno" AS "IdSecuencia", 
                'CORREDORA' AS "Llave",  
                NOF."CORREDORA" AS "Valor" 
                FROM :v_GrupoEventoNotificacion NOF
                UNION
                SELECT NOF."IdInterno" AS "IdSecuencia", 
                'ATENCIONCLIENTE' AS "Llave",  
                NOF."ATENCIONCLIENTE" AS "Valor" 
                FROM :v_GrupoEventoNotificacion NOF
                UNION
                SELECT NOF."IdInterno" AS "IdSecuencia", 
				'LOGOCORREDORA' AS "Llave", 
				TRIM(NOF."LOGOCORREDORA") AS "Valor" 
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
	 					p_out_IdEvento => :v_out_Agregar_Evento
	 					);
	 		p_out_IdEvento=SELECT "IdEvento"  FROM :v_out_Agregar_Evento;		
	 		

	 		UPDATE N
			SET  N."IdEstadoNotificacion"='ENV',
            	 N."FechaModificacion"=CURRENT_UTCTIMESTAMP
            FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" N	  
            INNER JOIN :p_TablaNotificacion P ON (N."IdEjecucion"=P."IdEjecucion");
	 		
	 		--SELECT * FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes"
	 		--SELECT * FROM "BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina"
	 					
		END IF;
			
		END;
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
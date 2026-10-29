SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR.NotificarNominaCircularizacion";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para generar notificacion a la nomina de Circularizaci�n
--  agrupando de acuerdo al cliente, asi se enviar� un solo correo por cada uno.
--  Autor: JVILLARROEL - GATBLAC
--  Fecha Creacion: 25/06/2018

"BSCL.Normativa.BD.Servicios::CIR.NotificarNominaCircularizacion"(
	--VARIABLES DEL PROCESO
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN p_Periodo        				 	   	    NVARCHAR(6),
    --VARIABLES DE SALIDA OBLIGATORIAS

	-- Parametros salida del servicio publico
	OUT p_out_IdEvento table ("IdEvento" BIGINT),
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
		DECLARE v_out_Evento TABLE("IdEvento" BIGINT);
		DECLARE v_count	BIGINT:=0;
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_FechaSistema                NVARCHAR(10);
	    
	    --------------------
		--Lógica Negocio
		--------------------
	
		BEGIN -- NOTIFICACIONES 
		
			CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
			
			SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"
			WHERE "Periodo"= p_Periodo AND "Notificar"='SI' AND "IdEstadoNotificacion"='PEN';
			
			IF v_count > 0 THEN 	
				--Generar grupo evento  de Notificar
			
				v_tablaNomina = SELECT "IdNomina", "IdCliente" FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"
							WHERE "Periodo"= p_Periodo AND "Notificar"='SI' AND "IdEstadoNotificacion"='PEN'
							AND "FormaEnvio"='INTERNET';
				
				--obtengo una nomina por cliente para generar un evento, y por ende un correo
				v_tablaNotificacion = SELECT "IdNomina" 
										from (select max("IdNomina") as "IdNomina", "IdCliente"
											from "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"
											where "Periodo"= p_Periodo AND "Notificar"='SI' 
											AND "IdEstadoNotificacion"='PEN' AND "FormaEnvio"='INTERNET'
											group by "IdCliente");
			
				v_out_Evento = SELECT 0 AS "IdEvento" FROM DUMMY WHERE 1=2;
				
				--Generar grupo evento  de Notificar
				v_GrupoEventoNotificacion = SELECT 
						NOR."IdNomina" AS "IdInterno",
						N.* ,
						PER."IdDireccionElectronicaPrincipal",
						PER."NombreCompleto",
						DIRE_CIRC_CONS."IdDireccionElectronica",
						EMP."NombreCompleto" AS "Agente",
						PER."DocumentoIdentidad",
						PER."TipoDocumentoIdentidad",
						CCDG."RazonSocial" AS "Corredora",
						CCDG."Telefono" AS "AtencionCliente"
				FROM :v_tablaNotificacion NOR  
				INNER JOIN "BSCL.Normativa.BD::CIR.Nomina" N ON (N."IdNomina"=NOR."IdNomina")
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS PER ON (PER."IdPersona"=N."IdPersona")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP ON (PER."IdEmpleadoEjecutivo"=EMP."IdEmpleado")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DIRE_CIRC_CONS 
				    ON (PER."IdPersona"=DIRE_CIRC_CONS."IdPersona" AND DIRE_CIRC_CONS."CodigoProposito"='CIRC')
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = p_IdEmpresa;

				v_Eventos = 
					 SELECT 
					'CIRCULARIZACION_CLIENTE' 			AS "CodigoTipoEvento", 
					NOF."IdInterno" 					AS "IdSecuencia", 
					CURRENT_UTCTIMESTAMP 				AS "FechaCreacionSistema", 
					0 									AS "IdPersonaRemitente", 
					NOF."IdPersona" 					AS "IdPersonaDestinatario", 
					NOF."IdPortafolio" 					AS "IdPortafolio" 
					FROM  :v_GrupoEventoNotificacion NOF;
									
					v_IdDirecciones =
					SELECT NOF."IdInterno"					  AS "IdSecuencia",
						CASE WHEN (SELECT "Valor" FROM "BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro" = 'ConfiguracionCircularizacionCustodia') = 'CP' 
								and NOF."IdDireccionElectronica" is not null 
						THEN NOF."IdDireccionElectronica"
					    ELSE NOF."IdDireccionElectronicaPrincipal" END  AS "IdDireccion",
						'TO' 								  AS "TipoDestinatario"
					FROM  :v_GrupoEventoNotificacion NOF;
					
					v_ArchivosAdjuntos =
					SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
					
					--Datos del Cliente en mail
												
				v_EventoDetalle = 
					SELECT NOF."IdInterno" AS "IdSecuencia", 
					'CLIENTE' AS "Llave",  
					NOF."NombreCompleto" AS "Valor" 
					FROM :v_GrupoEventoNotificacion NOF
					UNION
					SELECT NOF."IdInterno" AS "IdSecuencia", 
                    'AGENTE' AS "Llave",  
                    NOF."Agente" AS "Valor" 
                    FROM :v_GrupoEventoNotificacion NOF
                    UNION
                    SELECT NOF."IdInterno" AS "IdSecuencia", 
                    'DOCUMENTOIDENTIDAD' AS "Llave",  
                    NOF."DocumentoIdentidad" AS "Valor" 
                    FROM :v_GrupoEventoNotificacion NOF
                    UNION
                    SELECT NOF."IdInterno" AS "IdSecuencia", 
                    'TIPODOCUMENTOIDENTIDAD' AS "Llave",  
                    NOF."TipoDocumentoIdentidad" AS "Valor" 
                    FROM :v_GrupoEventoNotificacion NOF
                    UNION
                    SELECT NOF."IdInterno" AS "IdSecuencia", 
                    'CORREDORA' AS "Llave",  
                    NOF."Corredora" AS "Valor" 
                    FROM :v_GrupoEventoNotificacion NOF
                    UNION
                    SELECT NOF."IdInterno" AS "IdSecuencia", 
                    'ATENCIONCLIENTE' AS "Llave",  
                    NOF."AtencionCliente" AS "Valor" 
                    FROM :v_GrupoEventoNotificacion NOF
                    UNION
					SELECT NOF."IdInterno" AS "IdSecuencia", 
					'FECHA' AS "Llave",  
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha"(v_FechaSistema) AS "Valor" 
					FROM :v_GrupoEventoNotificacion NOF;
				
				SELECT 'v_Eventos',* FROM :v_Eventos;
				SELECT 'v_EventoDetalle',* FROM :v_EventoDetalle;
				
				v_out_Agregar_Evento = SELECT 0 AS "IdEvento", 0 as "IdSecuencia" FROM DUMMY WHERE 1=2;	
					
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
	 			SELECT 'v_out_Agregar_Evento',* FROM :v_out_Agregar_Evento;
	 			
	 			UPDATE N 
	 			SET N."IdEvento" = AUX."IdEvento",
	 			N."IdEstadoNotificacion"='ENV',
	           	N."FechaModificacion"=CURRENT_UTCTIMESTAMP
	 			FROM "BSCL.Normativa.BD::CIR.Nomina" N
				INNER JOIN (
					SELECT * FROM :v_tablaNomina TN
					INNER JOIN :v_out_Agregar_Evento E on (TN."IdNomina" = E."IdSecuencia")
					) AUX 
				ON N."IdCliente" = AUX."IdCliente";							
			END IF;
		END;
    END;
END;
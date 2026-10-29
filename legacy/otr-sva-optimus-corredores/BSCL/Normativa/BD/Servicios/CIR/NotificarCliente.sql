SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR.NotificarCliente";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para generar notificacion a la nomina de Circularizacion del periodo.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 21/10/2016

"BSCL.Normativa.BD.Servicios::CIR.NotificarCliente" ( 

    --VARIABLES DEL PROCESO
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN p_TablaNotificacion	  	TABLE("IdNomina"							BIGINT),
	--IN p_Periodo        				 	  NVARCHAR(6),
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
		DECLARE v_FechaSistema                NVARCHAR(10);
		DECLARE v_count	BIGINT:=0;
		--DECLARE v_id_cliente TABLE("IdCliente" BIGINT);
		--DECLARE v_contadorFilas BIGINT:=1;
		--DECLARE v_contadorEventos BIGINT:=0;
		--DECLARE v_auxIdEvento BIGINT:=0;
		--DECLARE v_auxIdSecuencia BIGINT:=0;
		--------------------
		--Lógica Negocio
		--------------------
		
		--Inicializacion de variables:
	
		BEGIN -- NOTIFICACIONES 
		
		SELECT COUNT(1) INTO v_count FROM :p_TablaNotificacion;
		
		IF v_count > 0 THEN 	
		
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
                FROM :p_TablaNotificacion NOR  
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
	 		
	 		/*v_id_cliente = SELECT N."IdCliente"
					 		FROM "BSCL.Normativa.BD::CIR.Nomina" N	  
				            INNER JOIN :v_GrupoEventoNotificacion P ON (N."IdNomina"=P."IdNomina");*/
            
            UPDATE N
			SET "IdEvento" = noti."IdEvento",
				N."IdEstadoNotificacion"='ENV',
            	 N."FechaModificacion"=CURRENT_UTCTIMESTAMP
            FROM "BSCL.Normativa.BD::CIR.Nomina" N
            INNER JOIN :v_out_Agregar_Evento noti on N."IdNomina" = noti."IdSecuencia"
            INNER JOIN :v_GrupoEventoNotificacion P ON (N."IdNomina"=P."IdNomina");
	 		
	 		/*UPDATE N 
            SET "IdEvento" = noti."IdEvento", 
            N."IdEstadoNotificacion"='ENV', 
            N."FechaModificacion"=CURRENT_UTCTIMESTAMP
			FROM "BSCL.Normativa.BD::CIR.Nomina" N	  
			LEFT JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" CN ON (CN."IdNomina"=N."IdNomina")
			INNER JOIN :v_id_cliente C ON (N."IdCliente" = C."IdCliente")
			WHERE CN."Periodo" = p_Periodo;*/
			
			--SELECT COUNT(1) INTO v_contadorEventos FROM :v_out_Agregar_Evento;
			
			--por cada idsecuencia(idnomina) actualizamos el con el idevento respectivo
			/*WHILE v_contadorFilas <= v_contadorEventos DO
				SELECT "IdEvento", "IdSecuencia" INTO v_auxIdEvento, v_auxIdSecuencia FROM
				(
				    SELECT Row_Number() Over (ORDER BY "IdSecuencia") AS RowNum, *
				    FROM :v_out_Agregar_Evento
				) t2
				WHERE RowNum = v_contadorFilas;
				
				UPDATE N
				SET N."IdEvento" = v_auxIdEvento
				FROM "BSCL.Normativa.BD::CIR.Nomina" N
				LEFT JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" CN ON (CN."IdNomina"=N."IdNomina")
				WHERE CN."Periodo"= p_Periodo AND CN."Notificar"='SI' AND CN."FormaEnvio"='INTERNET'
				AND N."IdCliente" = (
					SELECT "IdCliente" 
					FROM "BSCL.Normativa.BD::CIR.Nomina"
					WHERE "IdNomina" = v_auxIdSecuencia
					);
				v_contadorFilas:=v_contadorFilas+1;
			END WHILE;*/
	 		
	 		/*UPDATE N
			SET  N."IdEstadoNotificacion"='ENV',
            	 N."FechaModificacion"=CURRENT_UTCTIMESTAMP
            FROM "BSCL.Normativa.BD::CIR.Nomina" N	  
            INNER JOIN :v_GrupoEventoNotificacion P ON (N."IdNomina"=P."IdNomina");*/

	 		--Actualizar estado de Notificacion en Nomina
			/*
			SELECT * FROM 
			"BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" EVE
			INNER JOIN :v_out_Agregar_Evento PRO
			ON (EVE."IdEvento"=PRO."IdEvento")
			
	 		*/
	 		/*SELECT COUNT(1) INTO v_count  FROM 	:v_out_Agregar_Evento;
	 		IF v_count > 0 THEN
	 			p_out_RefId=1;
	 		ELSE
	 			p_out_RefId=0;	 		
	 		END IF; 	
	 			*/		
	 					
		END IF;
			
		END;
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

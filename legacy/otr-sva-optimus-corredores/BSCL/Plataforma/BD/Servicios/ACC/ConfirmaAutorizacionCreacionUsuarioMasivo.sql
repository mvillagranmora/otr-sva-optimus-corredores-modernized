SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConfirmaAutorizacionCreacionUsuarioMasivo";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar Creacion de usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 18/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConfirmaAutorizacionCreacionUsuarioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_Usuario						TABLE ("IdUsuario"	BIGINT)			--ID usuario que se esta autorizando
) 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	declare v_IdPersona bigint;
	DECLARE ERROR_CLIENTE_INVALIDO CONDITION FOR SQL_ERROR_CODE 10012;	
	
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL ERROR_CLIENTE_INVALIDO;
		END;

		
		v_EVENTOS = SELECT 'SOLICITUD_CAMBIO_CLAVE' AS "CodigoTipoEvento",
			 		  USU."IdUsuario" AS "IdSecuencia",
					 "FechaIngreso" AS "FechaCreacionSistema",
					  0 AS "IdPersonaRemitente",
					  SOC."IdPersona"  "IdPersonaDestinatario",
					  1 AS "IdPortafolio"
		FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
		JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC 
		ON SOC."IdCliente" = USU."IdEntidad" and USU."TipoEntidad" = 'CLI'
		JOIN :p_Usuario U ON U."IdUsuario" = USU."IdUsuario"
		WHERE USU."EsAutorizado" = 'P';	

		
		v_VALORES = SELECT USU."IdUsuario" AS "IdSecuencia",
					  'Nombre' AS "Llave",
					  USU."NombreCompleto" AS "Valor"
					  FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" USU
					  JOIN :p_Usuario U ON U."IdUsuario" = USU."IdUsuario"
					  WHERE USU."EsAutorizado" = 'P'

				      UNION ALL
				      
				       SELECT USU."IdUsuario" AS "IdSecuencia",
					  'Corredora' AS "Llave",
					   "RazonSocial" AS "Valor"
					   FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
					   JOIN :p_Usuario U ON U."IdUsuario" = USU."IdUsuario"
					   JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" RSOC ON RSOC."IdEmpresa" = USU."IdEmpresa" 
					   WHERE USU."EsAutorizado" = 'P'
--					   AND RSOC."IdEmpresa" = p_IdEmpresa

				      UNION ALL
				       SELECT USU."IdUsuario" AS "IdSecuencia",
					  'ClaveProvisoria' AS "Llave",
					   USU."ClaveProvisoria" AS "Valor"
					 FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
					 JOIN :p_Usuario U ON U."IdUsuario" = USU."IdUsuario"
					 WHERE USU."EsAutorizado" = 'P';	
				  
		v_ARCHIVOSADJUNTOS = SELECT 0 AS "IdSecuencia",
			 		  '' AS "ArchivoAdjunto",
					  '' AS "Nombre",
					  '' AS "Contrasenia"
					   FROM DUMMY WHERE 1=2;
				  
		v_IDDIRECCIONES = SELECT USU."IdUsuario" AS "IdSecuencia",
			 		  PER."IdDireccionElectronicaPrincipal" AS "IdDireccion",
					  'TO' AS "TipoDestinatario"
					  FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" USU
					 JOIN :p_Usuario U ON U."IdUsuario" = USU."IdUsuario"
					 JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = USU."IdPersona"
					 WHERE USU."EsAutorizado" = 'P';
				  
		--Cambia Estado de Usuario solo si esta pendiente
		UPDATE USU
		   SET "EsAutorizado" = 'S'
		FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
		JOIN :p_Usuario U ON U."IdUsuario" = USU."IdUsuario"
		WHERE USU."EsAutorizado" = 'P';	

		call "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento" (
				p_IdEmpresa => 1,
				p_IdUsuario => 4,
				p_IdModulo => 'PLA',
				p_Eventos => :v_EVENTOS,
				p_Valores => :v_VALORES,
				p_ArchivosAdjuntos => :v_ARCHIVOSADJUNTOS,
				p_IdDirecciones => :v_IDDIRECCIONES,
			    p_out_IdEvento => :v_OUT_IDEVENTO);

		   
	          
	END;				    
END;




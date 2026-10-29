SET SCHEMA "BSCL";
GO
DROP PROCEDURE   "BSCL.SocioNegocio.BD.Servicios::EJE.NotificarCambioInformacionFichaCliente";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para notificar a los clientes cambios en su ficha de informacion.
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 26/01/2017
--  
"BSCL.SocioNegocio.BD.Servicios::EJE.NotificarCambioInformacionFichaCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca

	-- Parametros entrada del servicio publico
	IN  p_clientes                  TABLE ("IdCliente" BIGINT, "IdSolicitud" BIGINT),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
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
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        DECLARE v_countEmpleadoAnt BIGINT := 0;
        DECLARE v_countEmpleadoNue BIGINT := 0;
        DECLARE v_IdPersonaEjecutivoNuevo BIGINT := 0;
		--------------------
		--Validaciones
		--------------------
		BEGIN

		END;		
		--------------------
		--Lógica de Negocio 
		--------------------	
		BEGIN
			DECLARE v_IdPersonaUsuario BIGINT;
			DECLARE v_IdDireccionElectronicaUsuario BIGINT;
			
			p_notificar = 
			select E.* 
			from "BSCL.SocioNegocio.BD::EJE.NotificarCambioInformacionFichaCliente_Entrada" E
			INNER JOIN :p_clientes C ON E."IdSolicitud" = C."IdSolicitud";
		
			
			SELECT TOP 1 CE."IdPersona", CE."IdDireccionElectronicaPrincipal" INTO v_IdPersonaUsuario, v_IdDireccionElectronicaUsuario
			FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN
			INNER JOIN"BSCL.Plataforma.BD::ACC.Usuario" U ON CSN."IdEmpleado" = U."IdEntidad"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CSN."IdPersona" = CE."IdPersona"
			WHERE U."IdUsuario" = p_IdUsuario;
			
			v_cliente =
            SELECT
                "BSCL.Plataforma.BD.Secuencias::EventoNotificacion".nextval AS "IdSecuencia",
                CC."IdCliente",
                CC."NombreCompleto" AS "CLIENTE", 
                CC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
                CC."TipoDocumentoIdentidad" AS "TIPODOCUMENTOIDENTIDAD", 
                CPT."Numero" AS "TELEFONO", 
                CPDE."Direccion" AS "CORREO", 
                CEMP."NombreCompleto" AS "AGENTE",
                CCDG."RazonSocial" AS "CORREDORA",
                CCDG."Telefono" AS "ATENCIONCLIENTE",
                CC."IdPersona" AS "IdPersonaDestinatario",
                CC."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronicaDestinatario",
                v_IdPersonaUsuario AS "IdPersonaRemitente",
                v_IdDireccionElectronicaUsuario AS "IdDireccionElectronicaRemitente"
            FROM (SELECT "IdCliente" FROM :p_notificar GROUP BY "IdCliente") N
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = N."IdCliente"
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP ON CC."IdPersona" = CP."IdPersona"
            LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CC."IdTelefonoPrincipal" = CPT."IdTelefono"
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CC."IdDireccionElectronicaPrincipal"
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CC."IdEmpleadoEjecutivo" = CEMP."IdEmpleado"
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa;
	       				
			p_Eventos = SELECT 
			'CAMBIO_INFORMACION_CLIENTE' AS "CodigoTipoEvento", 
			c."IdSecuencia", 
			CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
			c."IdPersonaRemitente", 
			c."IdPersonaDestinatario",
			0 AS "IdPortafolio"
			FROM :v_cliente c;
					
			p_IdDirecciones = 
            SELECT 
                E."IdSecuencia",
                C."IdDireccionElectronicaDestinatario" AS "IdDireccion",
                'TO' AS "TipoDestinatario"
            FROM :p_Eventos E
            INNER JOIN :v_cliente C ON E."IdSecuencia" = C."IdSecuencia"
            UNION ALL
            SELECT 
                E."IdSecuencia",
                C."IdDireccionElectronicaRemitente" AS "IdDireccion",
                'CC' AS "TipoDestinatario"
            FROM :p_Eventos E
            INNER JOIN :v_cliente C ON E."IdSecuencia" = C."IdSecuencia";
			
			p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;

			--TODO falta agregar la data para llenar la notificacion
			p_EventoDetalle = 
    			SELECT "IdSecuencia", "Llave", "Valor"
                    FROM (
                        SELECT * ,
                                
                        COALESCE(
                            "1" || "IdSecuencia", 
                            "2" || "CLIENTE",
                            "3" || "DOCUMENTOIDENTIDAD",
                            "4" || "TIPODOCUMENTOIDENTIDAD",
                            "5" || "TELEFONO",
                            "6" || "CORREO",
                            "7" || "AGENTE",
                            "8" || "CORREDORA",
                            "9" || "ATENCIONCLIENTE"
                        ) AS "Valor",
                                
                        COALESCE(
                            "1" || 'IdSecuencia', 
                            "2" || 'CLIENTE',
                            "3" || 'DOCUMENTOIDENTIDAD',
                            "4" || 'TIPODOCUMENTOIDENTIDAD',
                            "5" || 'TELEFONO',
                            "6" || 'CORREO',
                            "7" || 'AGENTE',
                            "8" || 'CORREDORA',
                            "9" || 'ATENCIONCLIENTE'
                        ) AS "Llave" 
                        FROM :v_cliente T, "BSCL.Plataforma.BD::PLA.Identidad"
                    )
                WHERE "Valor" IS NOT NULL
    			UNION ALL
        			SELECT 
        			C."IdSecuencia", 
        			'MODIFICACION' AS "Llave", 
        			STRING_AGG('<tr><td>' || N."CampoModificado" || '</td><td>' || N."ValorAntiguo" || '</td><td>' || N."ValorNuevo" || '</td><tr>','') AS "Valor"
    			FROM :v_cliente C
    			INNER JOIN :p_Notificar N ON N."IdCliente" = C."IdCliente"
    			GROUP BY "IdSecuencia";			
			
			--Guardar eventos
			
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
	 			p_IdEmpresa => p_IdEmpresa,
	 			p_IdUsuario => p_IdUsuario,
	 			p_IdModulo => p_IdModulo,
	 			p_Eventos => :p_Eventos,
	 			p_Valores => :p_EventoDetalle,
	 			p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
	 			p_IdDirecciones => :p_IdDirecciones,
	 			p_out_IdEvento => p_out_IdEvento
	 		);
	 		
			END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::DOC.VencerDocumentos";

CREATE PROCEDURE  "BSCL.Plataforma.BD.Servicios::DOC.VencerDocumentos" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  		--ID del Modulo que lo invoca	
		  
	-- Parametros entrada del servicio publico
	    	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
 
	DECLARE v_errorhashmsgtable TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 		NVARCHAR(520);
	--DECLARE v_SecuenciaEvento	BIGINT;	  
	DECLARE v_out_Resultado 	NVARCHAR(1);
    DECLARE v_out_Mensaje 		NVARCHAR(520);
	
	DECLARE	v_HorasVigencia				DECIMAL(5,2);
	DECLARE	v_EtapasSecuenciales		NVARCHAR(1);	
	DECLARE	V_IdUsuarioSuperAutorizador BIGINT;
	DECLARE	V_IdRolSuperAutorizador    	BIGINT; 
	
	DECLARE v_CodigoError 	INT;
    DECLARE v_MensajeError 	NVARCHAR(1000);	
    DECLARE v_FechaValida	CHAR(1);

	DECLARE v_FechaHoraVigencia 	DATETIME;
	DECLARE v_FechaHoraResgistro	DATETIME;
	DECLARE v_FechaSistema			NVARCHAR(40);
	
	DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));

	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			--Error INTerno no manejado  (El log ser� captado por la capa de XS control)		
			--RESIGNAL; v_MensajeError
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_Cantidad 				INT;
		DECLARE v_NombreTipoEvento 		NVARCHAR(100);
		DECLARE v_NombreTipoOperacion 	NVARCHAR(100);
		DECLARE v_NombreConcepto		NVARCHAR(100);
		
		-------------------------
		--Validaciones b�sicas 
		-------------------------
	
		--Validacion la existencia del Id del tipo de evento
				

		--Si existen errores en las validaciones debe salir
		---------------------------------------------------
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
			/* CONTINUA SOLO CON LOS EVENTOS SIN PROBLEMA */
			--p_tablaEventos=SELECT EV.* FROM :p_tablaEventos EV WHERE NOT EXISTS (SELECT 1 FROM  :p_out_ErroresDetalle DET WHERE DET."Codigo"= EV."IdTipoEvento");
		END IF;		
		
		
		
		------------------------
		--Validaciones via Motor (solo complejas)
		------------------------		
		--
	
		--------------------
		--Reglas Negocio 
		--------------------		
		--
					
		-------------------- 
		--Negocio Aqui
		--------------------
		BEGIN
		
			declare v_FechaHoraRegistroVarchar NVARCHAR(40);
			declare v_count BIGINT := 0;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa, :v_FechaHoraRegistroVarchar);
			v_FechaHoraResgistro := TO_SECONDDATE(v_FechaHoraRegistroVarchar, 'YYYY-MM-DD HH24:MI:SS');
			
			UPDATE ANX
			SET "IdEstado" = 'VCD'
			FROM "BSCL.Plataforma.BD::DOC.Anexo" ANX
			JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON DOC."IdDocumento" = ANX."IdDocumento"
			WHERE DOC."IdEmpresa" = p_IdEmpresa AND ANX."FechaVigencia" < v_FechaHoraResgistro;
			
			
			--Vencer documentos de identidad
			UPDATE DI
			SET "IdEstado" = 'VCD'
			FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
			WHERE DI."FechaVencimiento" > TO_DATE('01-01-0001', 'DD-MM-YYYY') 
			AND DI."FechaVencimiento" < v_FechaHoraResgistro
			AND DI."IdEstado" = 'ACT';
			
				--TODO log auditoria
		END;
			
	
	 	
				   
		--------------------
		--FIN SP 
		--------------------
	END;	
END;


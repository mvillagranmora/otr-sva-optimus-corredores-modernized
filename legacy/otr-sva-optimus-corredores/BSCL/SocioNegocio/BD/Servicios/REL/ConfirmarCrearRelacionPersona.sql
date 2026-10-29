SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::REL.ConfirmarCrearRelacionPersona";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la creacion de una relacion entre entidades.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 03-02-2016
--   
"BSCL.SocioNegocio.BD.Servicios::REL.ConfirmarCrearRelacionPersona" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_IdRelacionPersona							BIGINT,	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
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
	DECLARE v_out_AdvertenciasContador	BIGINT;
	DECLARE v_out_ErroresContador		BIGINT;
	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
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
		DECLARE v_IdEntidadOrigen	NVARCHAR(40);
		DECLARE v_TipoEntidad		NVARCHAR(40):= 'RELACION-PERSONA';
		
		DECLARE v_IdPersonaOrigen	BIGINT;
		DECLARE v_IdPersonaDestino	BIGINT;
		DECLARE v_IdIndicador		BIGINT;
		DECLARE v_ValorIndicadorPEP	NVARCHAR(1);
		DECLARE v_IdTipoRelacion	NVARCHAR(40);
		DECLARE v_CantRep			BIGINT;
		--------------------
		--Validaciones
		--------------------
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			
			v_IdEntidadOrigen:=CAST (p_IdRelacionPersona AS NVARCHAR(40));
			
			
			UPDATE "BSCL.SocioNegocio.BD::REL.RelacionPersona"
			SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			WHERE "IdRelacionPersona"=p_IdRelacionPersona;
	
			
			--Se termina el flujo con la actualizacion del registro de solicitud.
/*
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_IdEntidadOrigen		=>:v_IdEntidadOrigen,
				p_IdTipoEntidad			=>:v_TipoEntidad,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle	
			);
*/
			
			------------------------------------------------------
			--PERSONA ORIGEN SE MARCA PEP SI PERSONA DESTINO LO ES
			------------------------------------------------------
			--Obtiene IdPersona del Origen y Destino
			SELECT REL."IdPersonaOrigen", REL."IdPersonaDestino", REL."IdTipoRelacion" INTO v_IdPersonaOrigen, v_IdPersonaDestino, v_IdTipoRelacion
			FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
			WHERE REL."IdRelacionPersona" = p_IdRelacionPersona;
		
			--si relacion es de RepLegal, actualiza el tabla Persona el Rep principal
			SELECT count("IdPersonaOrigen")
			INTO v_cantRep
			FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"
			WHERE "IdPersonaOrigen"= :v_IdPersonaOrigen;
		
			UPDATE "BSCL.SocioNegocio.BD::PER.Persona" 
			SET "IdPersonaRepresentante" = :v_IdPersonaDestino
			WHERE :v_IdTipoRelacion = 'REP' AND :v_cantRep = 1 AND "IdPersona"= :v_IdPersonaOrigen;
			
			--Consulta si persona relacion destino es PEP
			SELECT IND."ValorIndicador" INTO v_ValorIndicadorPEP
			FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
			WHERE IND."CodigoTipoIndicador" = 'PEP'
			  AND IND."IdPersona" = :v_IdPersonaDestino;
			
			IF (:v_ValorIndicadorPEP IS NOT NULL AND :v_ValorIndicadorPEP = 'S') THEN
				--Obtiene IdIndicador de PEP de la persona Origen. Si no existe valor para el indicador, envia valor 0
				SELECT IND."IdIndicador" INTO v_IdIndicador
				FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
				WHERE IND."CodigoTipoIndicador" = 'PEP'
				  AND IND."IdPersona" = :v_IdPersonaOrigen;
				
				v_RegistroAtributo =
					SELECT TOP 1
						:v_IdIndicador AS "IdIndicador",
						IND."IdTipoIndicador",
						'S' AS "ValorIndicador",
						'0001-01-01 00:00:00' AS "FechaVencimiento"
					FROM "BSCL.Plataforma.BD::IND.TipoIndicador" IND
					WHERE IND."CodigoTipoIndicador" = 'PEP'
					  AND IND."IdEmpresa" = :p_IdEmpresa;
				
				CALL "BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador"(
					p_IdEmpresa	=> :p_IdEmpresa,
					p_IdUsuario	=> :p_IdUsuario,
					p_IdModulo	=> :p_IdModulo,
					p_UsaReglasNegocio	=> :p_UsaReglasNegocio,
					p_IdTipoEntidad	=> 'PERSONA',
					p_IdEntidad	=> :v_IdPersonaOrigen,
					p_RegistroAtributo	=> :v_RegistroAtributo,
					p_ModeloCliente	=> '',
					p_out_IdSecuencias	=> :v_out_IdSecuencias,
					p_out_AdvertenciasContador	=> :v_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle	=> :v_out_AdvertenciasDetalle,	
					p_out_ErroresContador	=> :v_out_ErroresContador,
					p_out_ErroresDetalle	=> :v_out_ErroresDetalle
				);
			END IF;
			
		END;

		--------------------
		--FIN SP 
		--------------------
	END;	
END;
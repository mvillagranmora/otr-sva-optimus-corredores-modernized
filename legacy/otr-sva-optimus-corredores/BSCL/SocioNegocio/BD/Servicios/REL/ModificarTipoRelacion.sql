SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::REL.ModificarTipoRelacion";
CREATE PROCEDURE
--  
--  Proposito: Servicio que obtiene y evalua informacion de los tipos de relaciones a modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 03-02-2016
--  
--  Proposito: Se incorporan definiciones Optimus Perú
--  Autor: JGARCES - BCS
--  Fecha Modificacion: 14/03/2017
--
"BSCL.SocioNegocio.BD.Servicios::REL.ModificarTipoRelacion" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						bigint,			--ID Empresa destino
	IN  p_IdUsuario						bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						nvarchar(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 			nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	-- Parametros entrada del servicio publico
	IN  p_IdTipoRelacion 				nvarchar(6),
	IN  p_EsPersonaNatural 				nvarchar(1),
	IN  p_IdTipoEntidadOrigen 			nvarchar(100)	DEFAULT '',
	IN  p_NombreTipoRelacion 			nvarchar(100),
	IN  p_IdTipoRelacionDestino 		nvarchar(6)		DEFAULT '',
	IN  p_EsIndefinida 					nvarchar(1),
	IN  p_IdEstado 						nvarchar(3),
	IN  p_UsaPorcentajeParticipacion 	nvarchar(1),
	IN  p_RetiraDocumentos			 	nvarchar(1),
	IN  p_UsaFinalidadRelacion			nvarchar(1),
	IN  p_UsaCondicionesRelacion		nvarchar(1),
	IN  p_UsaGradoVinculacionParental	nvarchar(1),
	IN  p_UsaFirmaDocumento				nvarchar(1),
	IN 	p_AccesoOptimusCliente			nvarchar(1),
	-- Parametros salida del servicio publico
	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
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
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE	
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
		DECLARE v_countTipoRelacion	 		   bigint:=0;
		--------------------
		--Validaciones
		--------------------
		BEGIN
			---Verifica si el Atributo no este registrado
			SELECT COUNT(1) INTO v_countTipoRelacion
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"
			WHERE "IdTipoRelacion" = :p_IdTipoRelacion; 
			IF(v_countTipoRelacion=0) THEN
				p_out_ErroresDetalle =  select 'TIPO_RELACION_NO_EXISTE' as "Codigo",'DESC_TIPO_RELACION_NO_EXISTE' as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;							
		END;		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------		
		BEGIN
			UPDATE  REL SET
				REL."EsPersonaNatural"	   			= p_EsPersonaNatural,
				REL."IdTipoEntidadOrigen"			= p_IdTipoEntidadOrigen,
				REL."NombreTipoRelacion"			= p_NombreTipoRelacion,
				REL."IdTipoRelacionDestino"			= p_IdTipoRelacionDestino,
   			    REL."EsIndefinida"					= p_EsIndefinida,
				REL."IdEstado"						= p_IdEstado,
				REL."UsaPorcentajeParticipacion"	= p_UsaPorcentajeParticipacion,
				REL."RetiraDocumentos"				= p_RetiraDocumentos,
				REL."UsaFinalidadRelacion"			= p_UsaFinalidadRelacion,
				REL."UsaCondicionesRelacion"		= p_UsaCondicionesRelacion,
				REL."UsaGradoVinculacionParental"	= p_UsaGradoVinculacionParental,
				REL."UsaFirmaDocumento"				= p_UsaFirmaDocumento,
				REL."AccesoOptimusCliente"				= p_AccesoOptimusCliente	
			FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" REL
			WHERE REL."IdTipoRelacion" = p_IdTipoRelacion;
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

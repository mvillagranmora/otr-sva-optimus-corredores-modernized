SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConfirmarCrearInstrumentoFondoMutuo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la creacion de un fondo mutuo.
--  Autor: JVILLARROEL - GAT-BLAC
--  Fecha Creacion: 07-06-2018
--  
"BSCL.FondoMutuo.BD.Servicios::ConfirmarCrearInstrumentoFondoMutuo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico	
	IN 	p_IdInstrumento								TABLE ("IdInstrumento" BIGINT),
	IN 	p_IdEstadoAutorizacion						NVARCHAR(3),	
	
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
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
	DECLARE v_IdCliente				BIGINT;
	DECLARE v_IdMatrizAgrupadorAPV	BIGINT;
	DECLARE v_CountIdMatrizAgrupadorAPV	BIGINT;
	DECLARE v_ProductoAgrupador		BIGINT;
	DECLARE v_IdEntidadOrigen		NVARCHAR(40);
	
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
		DECLARE v_countIdInstrumento	BIGINT;
		
		v_operaciones = SELECT inst."IdInstrumento", inst."IdEstado" as "EstadoInstrumento", 
			sf."IdEstado" as "EstadoSerieFondo"
			FROM "BSCL.Plataforma.BD::INS.Instrumento" inst
			INNER JOIN :p_IdInstrumento pInst ON inst."IdInstrumento" = pInst."IdInstrumento"
			INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" sf
			ON (sf."IdInstrumento" = inst."IdInstrumento")
			WHERE (inst."IdEstado" = 'PDA' or inst."IdEstado" = 'INA') and sf."IdEstado" = 'PDA';
						
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN
			SELECT COUNT("IdInstrumento") INTO v_countIdInstrumento
			FROM :v_operaciones;
			
			IF(v_countIdInstrumento=0) THEN
				p_out_ErroresDetalle =  select 'NO_EXISTE_INSTRUMENTO_FFMM' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
		END;
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN	
			UPDATE inst
			SET inst."IdEstado" = 'ACT'
			FROM "BSCL.Plataforma.BD::INS.Instrumento" inst
			INNER JOIN :p_IdInstrumento pInst ON inst."IdInstrumento" = pInst."IdInstrumento"
			INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" sf
			ON sf."IdInstrumento" = inst."IdInstrumento"
			WHERE (inst."IdEstado" = 'PDA' or inst."IdEstado" = 'INA') and sf."IdEstado" = 'PDA';
			
			UPDATE sf
			SET sf."IdEstado" = 'ACT'
			FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" sf
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst
			ON sf."IdInstrumento" = inst."IdInstrumento"
			INNER JOIN :p_IdInstrumento pInst 
			ON inst."IdInstrumento" = pInst."IdInstrumento"
			WHERE sf."IdEstado" = 'PDA';
		END;
		--------------------
		--FIN SP 
		--------------------
		
		--LOG DE AUDITORIA (inicio)
		BEGIN 
			DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Crear Instrumento Fondo Mutuo';
			
			--DATOS CABECERA LOG
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			--DATOS DETALLE LOG
			p_DetalleLog = 
				Select 'INGFFM'	 as "SubRegistro",
						ope."IdInstrumento"	as "CampoRegistro",
						ope."EstadoInstrumento"	as "ValorAntiguo",
						'ACT' as "ValorNuevo" 
				FROM :v_operaciones ope;
			
			--GRABAR LOG
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'INFORMATIVO' 
				,p_IdModulo => p_IdModulo 
				,p_IdUsuario => p_IdUsuario 
				,p_IdEmpresa => p_IdEmpresa 
				,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
				,p_IdUsuarioAtendido => p_IdUsuario
				,p_IdTipoRegistro => 'INGFFM'--'CLIENTE'
				,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
				,p_NombreProceso => :v_IdEntidadOrigen-- hacia abajo opcionales
				,p_IdRegistro => :v_IdEntidadOrigen
				,p_EstadoProceso => :p_IdEstadoAutorizacion
			);

		END;
		--LOG DE AUDITORIA (Fin)
	END;	
END;
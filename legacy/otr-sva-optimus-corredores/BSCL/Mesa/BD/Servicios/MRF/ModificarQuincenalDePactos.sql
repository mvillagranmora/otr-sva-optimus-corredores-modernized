SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ModificarQuincenalDePactos";
GO
-- 
--  Proposito: Modificar Quincenal de Pactos
--  Autor: JCASTRO - BCS
--  Fecha Creacion: 24/01/2017
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ModificarQuincenalDePactos" (
	-- Parametros entrada
    IN p_Fecha			DATE,
    IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo       NVARCHAR(3),
    IN p_DatosQuincenalDePactos	TABLE 	(	"IdQuincenalPactos"				BIGINT
	    									,"FechaProceso"					NVARCHAR(10)
	    									,"TipoCartera"					NVARCHAR(4)
											,"Emisor"						NVARCHAR(10)
											,"TipoReajuste"					NVARCHAR(4)
											,"FechaInicioCompromiso"			NVARCHAR(10)
											,"FechaVencimientoCompromiso" 	NVARCHAR(10)
											,"FechaVencimientoInstrumento"	NVARCHAR(10)
											,"MontoNominal"					DECIMAL(26,8)
											,"IdTipoOperacion"				NVARCHAR(6)
											,"Nemo"							NVARCHAR(40)
											,"IdMoneda"						NVARCHAR(3)	
											,"FolioOperacion"				BIGINT
											,"Secuencia_Pareo"				BIGINT									
											,"CantidadResto" 				DECIMAL(26,8)
											,"IndicadorCobertura"			NVARCHAR(1)
											,"CantidadComprometida"			DECIMAL(26,8)
											,"IdRelacionCorredor"			NVARCHAR(1)
	    									,"Lin_Comprom"					BIGINT
	    									,"ClasificacionTipoInstrumento"	NVARCHAR(3)
											,"ValorPresente"				DECIMAL(26,8)
											,"ValorComprometido"			DECIMAL(26,8)
											,"NumeroCupones" 				BIGINT
											,"PlazoResidual"				BIGINT
											,"RutCliente"					NVARCHAR(11)
											,"SecuenciaRut"					BIGINT
											,"RutCorredor"					NVARCHAR(11)
											,"Sec_Comprom"					BIGINT
											,"TasaInstrumento"				DECIMAL(26,8)									
											,"TasaPacto" 					DECIMAL(26,8)
											,"TipoFamilia"					NVARCHAR(3)
											,"TipoInstrumento"				NVARCHAR(3)
											,"TipoReajustePacto"			NVARCHAR(4)
											,"TipoClasificacionSvsCliente"	NVARCHAR(2)
											,"TipoAmortizacion"				NVARCHAR(10)
											,"CategoriaRiesgo"				NVARCHAR(10)
										),
	
     -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE valida_existencia INT;
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 AS "IdInterno",'' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno",'' AS "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes =
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato" FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato" FROM :v_out_AdvertenciasDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle =	SELECT errores."IdInterno", errores."Codigo", msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle =	SELECT adv."IdInterno", adv."Codigo", adv."RequiereAutorizacion", msj."Descripcion"
									FROM :v_out_AdvertenciasDetalle adv
									INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle 	= SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle 		= SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
    BEGIN
  		
  		-- Registros modificados
  		UPDATE QDP
		SET	
	    	QDP."FechaProceso"					= DQDP."FechaProceso"
	    	,QDP."TipoCartera"					= DQDP."TipoCartera"
			,QDP."Emisor"						= DQDP."Emisor"
			,QDP."TipoReajuste"					= DQDP."TipoReajuste"
			,QDP."FechaInicioCompromiso"		= DQDP."FechaInicioCompromiso"
			,QDP."FechaVencimientoCompromiso" 	= DQDP."FechaVencimientoCompromiso"
			,QDP."FechaVencimientoInstrumento"	= DQDP."FechaVencimientoInstrumento"
			,QDP."MontoNominal"					= DQDP."MontoNominal"
			,QDP."IdTipoOperacion"				= DQDP."IdTipoOperacion"
			,QDP."Nemo"							= DQDP."Nemo"
			,QDP."IdMoneda"						= DQDP."IdMoneda"
			,QDP."FolioOperacion"				= DQDP."FolioOperacion"
			,QDP."Secuencia_Pareo"				= DQDP."Secuencia_Pareo"
			,QDP."CantidadResto" 				= DQDP."CantidadResto"
			,QDP."IndicadorCobertura"			= DQDP."IndicadorCobertura"
			,QDP."CantidadComprometida"			= DQDP."CantidadComprometida"
			,QDP."IdRelacionCorredor"			= DQDP."IdRelacionCorredor"
	    	,QDP."Lin_Comprom"					= DQDP."Lin_Comprom"
	    	,QDP."ClasificacionTipoInstrumento"	= DQDP."ClasificacionTipoInstrumento"
			,QDP."ValorPresente"				= DQDP."ValorPresente"
			,QDP."ValorComprometido"			= DQDP."ValorComprometido"
			,QDP."NumeroCupones" 				= DQDP."NumeroCupones"
			,QDP."PlazoResidual"				= DQDP."PlazoResidual"
			,QDP."RutCliente"					= DQDP."RutCliente"
			,QDP."SecuenciaRut"					= DQDP."SecuenciaRut"
			,QDP."RutCorredor"					= DQDP."RutCorredor"
			,QDP."Sec_Comprom"					= DQDP."Sec_Comprom"
			,QDP."TasaInstrumento"				= DQDP."TasaInstrumento"
			,QDP."TasaPacto" 					= DQDP."TasaPacto"
			,QDP."TipoFamilia"					= DQDP."TipoFamilia"
			,QDP."TipoInstrumento"				= DQDP."TipoInstrumento"
			,QDP."TipoReajustePacto"			= DQDP."TipoReajustePacto"
			,QDP."TipoClasificacionSvsCliente"	= DQDP."TipoClasificacionSvsCliente"
			,QDP."TipoAmortizacion"				= DQDP."TipoAmortizacion"
			,QDP."CategoriaRiesgo"				= DQDP."CategoriaRiesgo"
		FROM "BSCL.Mesa.BD::MES.QuincenalPactos" QDP
		INNER JOIN :p_DatosQuincenalDePactos DQDP
			ON (QDP."IdQuincenalPactos" = DQDP."IdQuincenalPactos")
		WHERE DQDP."IdQuincenalPactos" != 0;
		
		-- Nuevos registros
		INSERT INTO "BSCL.Mesa.BD::MES.QuincenalPactos"(
			 "IdQuincenalPactos"
	    	,"FechaProceso"					
	    	,"TipoCartera"					
			,"Emisor"						
			,"TipoReajuste"
			,"FechaInicioCompromiso"					
			,"FechaVencimientoCompromiso" 	
			,"FechaVencimientoInstrumento"	
			,"MontoNominal"					
			,"IdTipoOperacion"				
			,"Nemo"	
			,"IdMoneda"						 
			,"FolioOperacion"				
			,"Secuencia_Pareo"												
			,"CantidadResto" 				
			,"IndicadorCobertura"			
			,"CantidadComprometida"			
			,"IdRelacionCorredor"			
	    	,"Lin_Comprom"					
	    	,"ClasificacionTipoInstrumento"	
			,"ValorPresente"				
			,"ValorComprometido"			
			,"NumeroCupones" 				
			,"PlazoResidual"				
			,"RutCliente"					
			,"SecuenciaRut"					
			,"RutCorredor"					
			,"Sec_Comprom"					
			,"TasaInstrumento"													
			,"TasaPacto" 					
			,"TipoFamilia"					
			,"TipoInstrumento"				
			,"TipoReajustePacto"			
			,"TipoClasificacionSvsCliente"	
			,"TipoAmortizacion"							
			,"CategoriaRiesgo"				
		) 
		SELECT
			 "BSCL.Mesa.BD.Secuencias::MRF.IdQuincenalPactos".NEXTVAL AS "IdQuincenalPactos"
			,"FechaProceso"					
	    	,"TipoCartera"					
			,"Emisor"						
			,"TipoReajuste"		
			,"FechaInicioCompromiso"			
			,"FechaVencimientoCompromiso" 	
			,"FechaVencimientoInstrumento"	
			,"MontoNominal"					
			,"IdTipoOperacion"				
			,"Nemo"
			,"IdMoneda"							 
			,"FolioOperacion"				
			,"Secuencia_Pareo"												
			,"CantidadResto" 				
			,"IndicadorCobertura"			
			,"CantidadComprometida"			
			,"IdRelacionCorredor"			
	    	,"Lin_Comprom"					
	    	,"ClasificacionTipoInstrumento"	
			,"ValorPresente"				
			,"ValorComprometido"			
			,"NumeroCupones" 				
			,"PlazoResidual"				
			,"RutCliente"					
			,"SecuenciaRut"					
			,"RutCorredor"					
			,"Sec_Comprom"					
			,"TasaInstrumento"													
			,"TasaPacto" 					
			,"TipoFamilia"					
			,"TipoInstrumento"				
			,"TipoReajustePacto"			
			,"TipoClasificacionSvsCliente"	
			,"TipoAmortizacion"							
			,"CategoriaRiesgo"
		FROM :p_DatosQuincenalDePactos
		WHERE "IdQuincenalPactos" = 0;
  			    	
    END;
    
    --LOG DE AUDITORIA (inicio)
	BEGIN 
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		DECLARE v_NombreProceso	NVARCHAR(100):='Modificación de datos de Quincenal de Pactos';
		
		--DATOS CABECERA LOG
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => v_fechaHoraSistema);
				
		--DATOS DETALLE LOG
		p_DetalleLog =
			SELECT
				'QUINCENALDEPACTOS' as "SubRegistro",
				'FilaCompleta' as "CampoRegistro",
				'' as "ValorAntiguo",
				CONCAT('Se modifican los datos de Quincenal de Pactos con el ID: ', "IdQuincenalPactos") as "ValorNuevo"
			FROM :p_DatosQuincenalDePactos;
		
		--GRABAR LOG
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog			=> 'MODIFICADOR' 
			,p_IdModulo			=> p_IdModulo 
			,p_IdUsuario		=> p_IdUsuario 
			,p_IdEmpresa		=> p_IdEmpresa 
			,p_FechaSistemaLog	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido=> p_IdUsuario
			,p_IdTipoRegistro	=> 'QUINCENALDEPACTOS'
			,p_DetalleLog		=> :p_DetalleLog
			,p_NombreProceso	=> :v_NombreProceso
		);
	
	END;
	--LOG DE AUDITORIA (Fin)	

END;

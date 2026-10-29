SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Orden.BD.Servicios::ModificarCorredorCorresponsal";
GO
CREATE PROCEDURE  
-- 
--  Proposito: Crear Configuracion del Operador
--  Autor: TEAM ORDENES! - BCS
--  Fecha Creacion: 09/02/2016
--  
"BSCL.Orden.BD.Servicios::ModificarCorredorCorresponsal" (
	
	IN  p_IdEmpresa						BIGINT,
	IN  p_IdUsuario						BIGINT,
	IN  p_IdModulo						NVARCHAR(3),
	IN  p_simularOperacion				NVARCHAR(1) DEFAULT 'N',
	IN  p_UsaReglasNegocio	 			NVARCHAR(1) DEFAULT 'N',	
	
	IN	p_IdCorredorCorresponsal		BIGINT,
	IN	p_IdPais						BIGINT 			DEFAULT NULL,
	IN	p_IdContraparte					BIGINT 			DEFAULT NULL,
	IN	p_IdSocioDeNegocio				BIGINT			DEFAULT NULL,
	IN	p_IdPersona						BIGINT 			DEFAULT NULL,
	IN	p_IdPerfilCobros				NVARCHAR(40) 	DEFAULT NULL,
	IN  p_IdTipoProducto				NVARCHAR(4)		DEFAULT NULL,
	IN	p_IdBoveda						NVARCHAR(3)		DEFAULT NULL,
	IN	p_IdEstado						NVARCHAR(3) 	DEFAULT NULL,
	IN	p_DefaultPais					NVARCHAR(1) 	DEFAULT 'N',
	
	OUT p_out_InformacionContador		INT,
	OUT p_out_InformacionDetalle		TABLE ("IdInterno"	BIGINT,"IdOperacion"	BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"IdOperacion"	BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"IdOperacion"	BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_SalidaDetalle "BSCL.Orden.BD.Tipos::SalidaDetalle";
	DECLARE v_RegistroNuevo INT;
	DECLARE v_LocalizacionSistema NVARCHAR(4);
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_SalidaDetalle,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		

		v_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_SalidaDetalle);
		
	END;

	BEGIN
		
		IF p_simularOperacion = 'S' THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CREATE LOCAL TEMPORARY TABLE #CORR (
			"IdCorredorCorresponsal"	BIGINT,
			"IdCorredorCorresponsalReal"	BIGINT,
			"CodigoCorredor"			NVARCHAR(40),
			"IdPais"					BIGINT,
			"IdContraparte"				BIGINT,
			"IdSocioDeNegocio"			BIGINT,
			"IdPersona"					BIGINT,
			"IdEmpresa"					BIGINT,
			"IdTipoProducto"			NVARCHAR(4),
			"IdPerfilCobros"			NVARCHAR(40),
			"IdBoveda"					NVARCHAR(3),
			"IdEstado"					NVARCHAR(3),
			"DefaultPais"				NVARCHAR(1),
			"Cumple"					NVARCHAR(1)
		);
		
		INSERT INTO #CORR ("IdCorredorCorresponsal","IdCorredorCorresponsalReal","CodigoCorredor","IdPais","IdContraparte","IdTipoProducto","IdPerfilCobros","IdBoveda","IdEstado","DefaultPais","IdEmpresa","IdPersona","IdSocioDeNegocio","Cumple")
		SELECT :p_IdCorredorCorresponsal AS "IdCorredorCorresponsal",null AS "IdCorredorCorresponsalReal",null AS "CodigoCorredor",
		:p_IdPais as "IdPais",p_IdContraparte as "IdContraparte",p_IdTipoProducto as "IdTipoProducto",p_IdPerfilCobros as "IdPerfilCobros",p_IdBoveda as "IdBoveda",p_IdEstado as "IdEstado",:p_DefaultPais as "DefaultPais",
		p_IdEmpresa as "IdEmpresa", p_IdPersona as "IdPersona",p_IdSocioDeNegocio as "IdSocioDeNegocio",'S' as "Cumple"
		FROM DUMMY;
		
		UPDATE CORR
		SET 
			"IdCorredorCorresponsalReal" = c2."IdCorredorCorresponsal",
			"CodigoCorredor" = c2."CodigoCorredor"
		FROM #CORR CORR
		INNER JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" c2 on CORR."IdCorredorCorresponsal" = c2."IdCorredorCorresponsal";
		
		--VALIDACION ID NO EXISTE
		v_SalidaDetalleIdCorredorNoExiste = SELECT c1."IdCorredorCorresponsal" as "IdInterno",c1."IdCorredorCorresponsal" as "IdOperacion",'RECHAZO' as "Resultado",'CODIGO_CORREDOR_NO_EXISTE' as "Codigo",'IdCorredorCorresponsal' as "CodigoDato1",c1."IdCorredorCorresponsal" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
												FROM #CORR c1
												WHERE c1."IdCorredorCorresponsalReal" IS NULL;
					
		UPDATE c1
		SET "Cumple" = 'N'
		FROM #CORR c1
		INNER JOIN :v_SalidaDetalleIdCorredorNoExiste det on c1."IdCorredorCorresponsal" = det."IdOperacion";
		
		v_SalidaDetalle = SELECT * from :v_SalidaDetalleIdCorredorNoExiste;
		--FIN VALIDACION CODIGO EXISTE	
		
		UPDATE "BSCL.Orden.BD::ORD.CorredorCorresponsal"
		SET "DefaultPais" = 'N'
		WHERE :p_DefaultPais = 'S' AND p_IdPais = :p_IdPais;
		
		UPDATE C1 
		SET "IdPais" 			= COALESCE(CORR."IdPais",C1."IdPais"),
			"IdContraparte" 	= COALESCE(CORR."IdContraparte",C1."IdContraparte"),
			"IdSocioDeNegocio" 	= COALESCE(CORR."IdSocioDeNegocio",C1."IdSocioDeNegocio"),
			"IdPersona" 		= COALESCE(CORR."IdPersona",C1."IdPersona"),
			"IdPerfilCobros"	= COALESCE(CORR."IdPerfilCobros",C1."IdPerfilCobros"),
			"IdEstado" 			= COALESCE(CORR."IdEstado",C1."IdEstado"),
			"DefaultPais" 		= COALESCE(CORR."DefaultPais",C1."DefaultPais")
		FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" C1
		INNER JOIN #CORR CORR on C1."IdCorredorCorresponsal" = CORR."IdCorredorCorresponsal" AND "Cumple" = 'S';
		
		SELECT "Valor" INTO v_LocalizacionSistema
		FROM BSCL."BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "CodigoParametro" = 'PLA.LocalizacionSistema';
		
		IF v_LocalizacionSistema = 'PE' THEN
			SELECT COUNT("IdCorredorCorresponsal") INTO v_RegistroNuevo 
			FROM "BSCL.Orden.BD::ORD.CorredorCorresponsalBoveda" 
			WHERE "IdCorredorCorresponsal" = :p_IdCorredorCorresponsal
			AND "IdTipoProducto" = :p_IdTipoProducto; 
			
			IF v_RegistroNuevo <> 0 THEN
			
			   	UPDATE C1
				SET "IdBoveda"			= COALESCE(CORR."IdBoveda",C1."IdBoveda")
				FROM "BSCL.Orden.BD::ORD.CorredorCorresponsalBoveda" C1
				INNER JOIN #CORR CORR on C1."IdCorredorCorresponsal" = CORR."IdCorredorCorresponsal" AND "Cumple" = 'S'
				WHERE C1."IdCorredorCorresponsal" = :p_IdCorredorCorresponsal
				AND C1."IdTipoProducto" = :p_IdTipoProducto;
				
			ELSE
			
				INSERT INTO "BSCL.Orden.BD::ORD.CorredorCorresponsalBoveda"
				("IdCorredorCorresponsal","IdTipoProducto","IdBoveda")
				SELECT "IdCorredorCorresponsal","IdTipoProducto","IdBoveda"
				FROM #CORR; 
			
			END IF;
		END IF;
		
		--INGRESAMOS OPERACIONES EXITOSAS A SALIDA
		v_SalidaDetalle = SELECT "IdCorredorCorresponsal" as "IdInterno","IdCorredorCorresponsal" as "IdOperacion",'INFORMACION' as "Resultado",'CORREDOR_CORRESPONSAL_MODIFICADO' as "Codigo",'CodigoCorresponsal' as "CodigoDato1","CodigoCorredor" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
							from #CORR c1 WHERE "Cumple" = 'S'
							UNION ALL SELECT * FROM :v_SalidaDetalle;
		
		--DROP TABLAS TEMPORALES Y DESPUES SIGNAL
		DROP TABLE #CORR;
		SIGNAL SALIDA_CONTROLADA;
		
		
	END;	
END;
GO

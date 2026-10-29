SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Orden.BD.Servicios::CrearCorredorCorresponsal";
GO
CREATE PROCEDURE  
-- 
--  Proposito: Crear Configuracion del Operador
--  Autor: TEAM ORDENES! - BCS
--  Fecha Creacion: 09/02/2016
--  
"BSCL.Orden.BD.Servicios::CrearCorredorCorresponsal" (
	
	IN  p_IdEmpresa						BIGINT,
	IN  p_IdUsuario						BIGINT,
	IN  p_IdModulo						NVARCHAR(3),
	IN  p_simularOperacion				NVARCHAR(1) DEFAULT 'N',
	IN  p_UsaReglasNegocio	 			NVARCHAR(1) DEFAULT 'N',	
	
	IN	p_IdPais						BIGINT,
	IN	p_CodigoCorredor				NVARCHAR(100),
	IN	p_IdContraparte					BIGINT,
	IN	p_IdPersona						BIGINT,
	IN	p_IdSocioDeNegocio				BIGINT,
	IN	p_IdPerfilCobros				NVARCHAR(40),
	IN	p_IdEstado						NVARCHAR(3),
	IN	p_DefaultPais					NVARCHAR(1),
	
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
			"IdPais"					BIGINT,
			"CodigoCorredor"			NVARCHAR(40),
			"IdContraparte"				BIGINT,
			"IdPersona"					BIGINT,
			"IdSocioDeNegocio"			BIGINT,
			"IdEmpresa"					BIGINT,
			"IdPerfilCobros"			NVARCHAR(40),
			"IdEstado"					NVARCHAR(3),
			"DefaultPais"				NVARCHAR(1),
			"Cumple"					NVARCHAR(1)
		);
		
		INSERT INTO #CORR ("IdCorredorCorresponsal","IdPais","CodigoCorredor","IdContraparte","IdEstado","IdPerfilCobros","DefaultPais","IdEmpresa","IdPersona","IdSocioDeNegocio","Cumple")
		SELECT "BSCL.Orden.BD.Secuencias::CorredorCorresponsal".NEXTVAL AS "IdCorredorCorresponsal",
		:p_IdPais as "IdPais",:p_CodigoCorredor as "CodigoCorredor",p_IdContraparte as "IdContraparte",p_IdEstado as "IdEstado",p_IdPerfilCobros as "IdPerfilCobros",:p_DefaultPais as "DefaultPais",
		p_IdEmpresa AS "IdEmpresa",p_IdPersona as "IdPersona",p_IdSocioDeNegocio AS "IdSocioDeNegocio",'S' as "Cumple"
		FROM DUMMY;
		
		--VALIDACION CODIGO EXISTE
		v_SalidaDetalleCodigoExiste = SELECT c1."IdCorredorCorresponsal" as "IdInterno",c1."IdCorredorCorresponsal" as "IdOperacion",'RECHAZO' as "Resultado",'CODIGO_CORREDOR_YA_EXISTE' as "Codigo",'CodigoCorresponsal' as "CodigoDato1",c1."CodigoCorredor" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
					FROM #CORR c1
					INNER JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" c2 ON c1."CodigoCorredor" = c2."CodigoCorredor";
					
		UPDATE c1
		SET "Cumple" = 'N'
		FROM #CORR c1
		INNER JOIN :v_SalidaDetalleCodigoExiste det on c1."IdCorredorCorresponsal" = det."IdOperacion";
		
		v_SalidaDetalle = SELECT * from :v_SalidaDetalleCodigoExiste;
		--FIN VALIDACION CODIGO EXISTE	
		
		UPDATE "BSCL.Orden.BD::ORD.CorredorCorresponsal"
		SET "DefaultPais" = 'N'
		WHERE :p_DefaultPais = 'S' AND p_IdPais = :p_IdPais;
		
		INSERT INTO "BSCL.Orden.BD::ORD.CorredorCorresponsal" 
		("IdCorredorCorresponsal","IdPais","CodigoCorredor","IdContraparte","IdEstado","IdPerfilCobros","DefaultPais","IdPersona","IdEmpresa","IdSocioDeNegocio")
		SELECT "IdCorredorCorresponsal","IdPais","CodigoCorredor","IdContraparte","IdEstado","IdPerfilCobros","DefaultPais","IdPersona","IdEmpresa","IdSocioDeNegocio"
		FROM #CORR WHERE "Cumple" = 'S';
		
		SELECT "Valor" INTO v_LocalizacionSistema
		FROM BSCL."BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "CodigoParametro" = 'PLA.LocalizacionSistema';
		
		IF v_LocalizacionSistema = 'PE' THEN
			INSERT INTO "BSCL.Orden.BD::ORD.CorredorCorresponsalBoveda"
			("IdCorredorCorresponsal","IdTipoProducto")
			SELECT Corresponsal."IdCorredorCorresponsal",DetallePerfilCobros."IdTipoProducto"
			FROM #CORR AS Corresponsal
			INNER JOIN "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDetallePerfilCobros" AS DetallePerfilCobros			 
					ON DetallePerfilCobros."IdPerfilCobros" = Corresponsal."IdPerfilCobros"
			WHERE Corresponsal."Cumple" = 'S';
		END IF;
		
		--INGRESAMOS OPERACIONES EXITOSAS A SALIDA
		v_SalidaDetalle = SELECT "IdCorredorCorresponsal" as "IdInterno","IdCorredorCorresponsal" as "IdOperacion",'INFORMACION' as "Resultado",'CORREDOR_CORRESPONSAL_INGRESADO' as "Codigo",'CodigoCorresponsal' as "CodigoDato1","CodigoCorredor" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
							from #CORR c1 WHERE "Cumple" = 'S'
							UNION ALL SELECT * FROM :v_SalidaDetalle;
		
		--DROP TABLAS TEMPORALES Y DESPUES SIGNAL
		DROP TABLE #CORR;
		SIGNAL SALIDA_CONTROLADA;
		
		
	END;	
END;

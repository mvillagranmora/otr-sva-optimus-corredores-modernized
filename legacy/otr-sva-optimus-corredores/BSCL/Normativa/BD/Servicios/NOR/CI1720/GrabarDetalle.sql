SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CI1720.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CI1720.GrabarDetalle"
(
	--PARAMETROS DE ENTRADA COMUNES
	IN p_IdEmpresa 			BIGINT,
	IN p_IdUsuario 			BIGINT,
	IN p_IdModulo 			NVARCHAR(3),
	
	-- PARAMETROS DE ENTRADA PROPIOS
	IN p_ID							BIGINT,
	IN p_IdDocumento				BIGINT,
	--IN p_IdSolicitudCircular 		BIGINT default 0,
	IN p_VboUsuario 				CHAR(1),
	IN p_Observaciones				NVARCHAR(250),
	IN p_FuenteDatos				NVARCHAR(40),
	
	--IN p_IdPortafolio				BIGINT DEFAULT 0,
	--IN p_RutCliente					NVARCHAR(40),
	--IN p_TipoRegistro				SMALLINt default 2, -- siempre 2
	IN p_Nemotecnico				NVARCHAR(20),
	IN p_NombreEmisor				NVARCHAR(30),
	IN p_TipoInstrumento			NVARCHAR(10),
	IN p_CantidadUnidadesNominal 	DECIMAL(15,4),
	IN p_TipoUnidades				NVARCHAR(15),
	IN p_Valorizacion				BIGINT,
	IN p_CarteraPropia				DECIMAL(15,4),
	IN p_CustodiaTerceros			DECIMAL(15,4),
	IN p_CustodiaPropiaDCV			DECIMAL(15,4),
	IN p_CustodiaTercerosDCV		DECIMAL(15,4),
	IN p_CustodiaSociedadEmisora	DECIMAL(15,4),
	IN p_CustodiaIntermediario		DECIMAL(15,4),
	IN p_CustodiaBolsas				DECIMAL(15,4),
	IN p_CustodiaOtros				DECIMAL(15,4),
	IN p_FechaRegistro				DATE,
	--IN p_IdCliente					BIGINT,
	IN p_Usuario					NVARCHAR(30),
	
	--Parametro sin uso
	IN p_IdBaseDocumento 			BIGINT default null,
	
	
	--PARAMETROS DE SALIDAS PROPIOS
	OUT p_out_id 			BIGINT,
	
	-- SALIDAS DE ADVERTENCIAS
	OUT p_out_AdvertenciasContador		INT,
	OUT p_out_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)),
	
	--SALIDAS DE ERRORES
	OUT p_out_ErroresContador			INT,
	OUT p_out_ErroresDetalle			TABLE ("Codigo" nvarchar(40), "Descripcion" nvarchar(5000))
)
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::sql_error_code > 10000 THEN
			
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
									SELECT v_errorExt, ::sql_error_message FROM DUMMY;
			
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		ELSE
			RESIGNAL;
		END IF;
	END;
	
	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador := 0;
	p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
	p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
	
	--LOGICA DE ACTUALIZACION

	IF p_ID > 0 THEN
		p_out_id := p_ID;
		
		UPDATE "BSCL.Normativa.BD::NOR.DatosCI1720" SET
			--"IdSolicitudCircular" = 1,
			"VboUsuario" = p_VboUsuario,
			"Observaciones" = p_Observaciones,
			"FuenteDatos" = p_FuenteDatos,
			--"IdPortafolio" = p_IdPortafolio,
			--"RutCliente" = p_RutCliente,
			--"TipoRegistro" = 2,
			"Nemotecnico" = p_Nemotecnico,
			"NombreEmisor" = p_NombreEmisor,
			"TipoInstrumento" = p_TipoInstrumento,
			"CantidadUnidadesNominal" = p_CantidadUnidadesNominal,
			"TipoUnidades" = p_TipoUnidades,
			"Valorizacion" = p_Valorizacion,
			"CarteraPropia" = p_CarteraPropia,
			"CustodiaTerceros" = p_CustodiaTerceros,
			"CustodiaPropiaDCV" = p_CustodiaPropiaDCV,
			"CustodiaTercerosDCV" = p_CustodiaTercerosDCV,
			"CustodiaSociedadEmisora" = p_CustodiaSociedadEmisora,
			"CustodiaIntermediario" = p_CustodiaIntermediario,
			"CustodiaBolsas" = p_CustodiaBolsas,
			"CustodiaOtros" = p_CustodiaOtros,
			"FechaRegistro"	= p_FechaRegistro,
			--"IdCliente" =  p_IdCliente
			"Usuario" = p_IdUsuario
			
		WHERE "ID" = p_ID
		AND "FechaRegistro" = p_FechaRegistro;
			
	ELSE
		SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::NOR.DatosCI1720";
		
		INSERT INTO "BSCL.Normativa.BD::NOR.DatosCI1720"(
			"ID",
            "IdDocumento",
			"VboUsuario",
			"Observaciones",
			"FuenteDatos",
			--"IdPortafolio",
			--"RutCliente",
			--"TipoRegistro",
			"Nemotecnico",
			"NombreEmisor",
			"TipoInstrumento",
			"CantidadUnidadesNominal",
			"TipoUnidades",
			"Valorizacion",
			"CarteraPropia",
			"CustodiaTerceros",
			"CustodiaPropiaDCV",
			"CustodiaTercerosDCV",
			"CustodiaSociedadEmisora",
			"CustodiaIntermediario",
			"CustodiaBolsas",
			"CustodiaOtros",
			"FechaRegistro",
			--"IdCliente"
			"Usuario"
		) VALUES (
			p_out_id,
			p_IdDocumento,
			'N',
			'',
			'MANTENEDOR',
			--p_IdPortafolio,
			--p_RutCliente,
			--2,
			p_Nemotecnico,
			p_NombreEmisor,
			p_TipoInstrumento,
			p_CantidadUnidadesNominal,
			p_TipoUnidades,
			p_Valorizacion,
			p_CarteraPropia,
			p_CustodiaTerceros,
			p_CustodiaPropiaDCV,
			p_CustodiaTercerosDCV,
			p_CustodiaSociedadEmisora,
			p_CustodiaIntermediario,
			p_CustodiaBolsas,
			p_CustodiaOtros,
			p_FechaRegistro,
			--p_IdCliente
			p_Usuario
		);
		
	END IF;

END;
SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ConsultarPrecioTasa";

-- 
--  Proposito: Consulta Precio Tasa
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 10/10/2017
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ConsultarPrecioTasa" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,
	IN p_IdUsuario			BIGINT,
	IN p_FechaSolicitud		NVARCHAR(10),
	IN p_CodigoFuente		NVARCHAR(40),
	IN p_IdClasificacionTipoInstrumento	NVARCHAR(3),
	IN p_IdMoneda			NVARCHAR(3),
	IN p_TipoRegistro		NVARCHAR(10),
	IN p_IdEstado			NVARCHAR(3),
	IN p_UltimaCurvaPublicada	NVARCHAR(1),
	
	-- Parametro salida
	OUT p_out_PrecioTasa	TABLE (	"IdPrecioTasa"			BIGINT,
									"IdFuente"				BIGINT,
									"CodigoFuente"			NVARCHAR(40),
									"CodDescFuente"			NVARCHAR(300),
									"Clasificacion"			NVARCHAR(150),
									"Nemotecnico"			NVARCHAR(40),
									"IdMoneda"				NVARCHAR(3),
									"MonedaFormato"			NVARCHAR(3),
                                    "IdTipoReajuste"        NVARCHAR(3),
									"TipoRegistro"			NVARCHAR(10),
									"TipoTasa"				NVARCHAR(250),
									"Tasa"					DECIMAL(26,8),
									"TasaEstimada"			DECIMAL(26,8),
									"TipoPrecio"			NVARCHAR(250),
									"Precio"				DECIMAL(26,8),
									"Plazo"					BIGINT,
									"Estado" 				NVARCHAR(520),
									"IdClasificacionTipoInstrumento" NVARCHAR(5),
									"FechaSolicitud"		SECONDDATE,
									"FechaOficializacion"	SECONDDATE,
									"FechaPublicacion"		SECONDDATE,
									"IdUsuarioCreador"		BIGINT,
									"IdUsuarioOficializador" BIGINT,
									"CodigoUsuarioCreador"	NVARCHAR(100),
									"CodigoUsuarioOficializador"	NVARCHAR(100)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE (	"IdInterno"	BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40),
											"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador	INT,
	OUT p_out_ErroresDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_resultadoRegla	NVARCHAR(40);
	
  	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;         	
	DECLARE v_out_AdvertenciasDetalle	TABLE (	"IdInterno" BIGINT, "Codigo" nvarchar(100), "RequiereAutorizacion" NVARCHAR(1),
												"ValorParametro" nvarchar(100), "NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "RequiereAutorizacion",
													  '' AS "ValorParametro", '' AS "NombreParametro"
												FROM DUMMY WHERE 1=2;
												
	DECLARE v_out_ErroresDetalle	TABLE (	"IdInterno" BIGINT, "Codigo" nvarchar(100), "ValorParametro" nvarchar(100),
											"NombreParametro" NVARCHAR(40))
									DEFAULT SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", 
												  '' AS "NombreParametro"
											FROM DUMMY WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes =
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato"
			FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato"
			FROM :v_out_AdvertenciasDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle =
			SELECT errores."IdInterno", errores."Codigo", msj."Descripcion"
			FROM :v_out_ErroresDetalle errores
			INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle =
			SELECT adv."IdInterno", adv."Codigo", adv."RequiereAutorizacion", msj."Descripcion"
			FROM :v_out_AdvertenciasDetalle adv
			INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	v_out_AdvertenciasDetalle =
		SELECT 0 as "IdInterno", '' as "Codigo", 'N' as "RequiereAutorizacion", '' as "ValorParametro", '' as "NombreParametro"
		from dummy where 1=2;
	
	v_out_ErroresDetalle =
		SELECT 0 as "IdInterno", '' as "Codigo", '' as "ValorParametro", '' as "NombreParametro"
		from dummy where 1=2;
		
   	 	
   	--------------------
	--VALIDACIONES
	--------------------
	
	CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(
		:p_IdEmpresa,
		:p_IdUsuario,
		'PLA',
		:v_out_ErroresDetalle_Basicos,
		:v_resultadoRegla
	);
	
	if :v_resultadoRegla = 'RECH' then
		v_out_ErroresDetalle =
			SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro"
			FROM :v_out_ErroresDetalle_Basicos;
		
		SIGNAL SALIDA_CONTROLADA; 	
	end if;
	
	
	--------------------
	--LOGICA
	--------------------
	CREATE LOCAL TEMPORARY TABLE #TempMonedaReajustablePag (
		Letra CHAR(1),
		TipoMonedaReajuste CHAR(3),
        TipoMoneda CHAR(3)
	);

    INSERT INTO #TempMonedaReajustablePag (Letra, TipoMonedaReajuste,TipoMoneda) VALUES ('N','CLP', 'CLP');
	INSERT INTO #TempMonedaReajustablePag (Letra, TipoMonedaReajuste,TipoMoneda) VALUES ('U','UF', 'CLP');
    INSERT INTO #TempMonedaReajustablePag (Letra, TipoMonedaReajuste,TipoMoneda) VALUES ('D', 'USD', 'CLP');
    INSERT INTO #TempMonedaReajustablePag (Letra, TipoMonedaReajuste,TipoMoneda) VALUES ('*', 'USD', 'USD');
    INSERT INTO #TempMonedaReajustablePag (Letra, TipoMonedaReajuste,TipoMoneda) VALUES ('E', 'EUR', 'CLP');
    INSERT INTO #TempMonedaReajustablePag (Letra, TipoMonedaReajuste,TipoMoneda) VALUES ('R', 'EUR','EUR');   

	IF :p_UltimaCurvaPublicada = 'S' THEN
		v_PrecioTasa =
			SELECT
				 PTA."IdPrecioTasa"
				,PTA."IdFuente"
				,COALESCE(FU."Codigo",' -') AS "CodigoFuente"
				,COALESCE(FU."Descripcion",' -') AS "DescripcionFuente"
				,COALESCE(FU."Codigo",'') || ' - ' || COALESCE(FU."Descripcion",'') AS "CodDescFuente"
				,PTA."IdTipoInstrumento"
				,PTA."IdInstrumento"
				,PTA."Nemotecnico"
				,PTA."IdClasificacionTipoInstrumento"
				,COALESCE(CLA."Codigo",'') || ' - ' || COALESCE(CLA."Nombre",'') AS "Clasificacion"
				,CASE WHEN  PTA."IdTipoInstrumento" = 'PAG' THEN (SELECT "TIPOMONEDA" FROM #TempMonedaReajustablePag WHERE "LETRA" = SUBSTRING(PTA."Nemotecnico", 2,1)) ELSE  PTA."IdMoneda" END as "IdMoneda"
				,PTA."IdMoneda" AS "MonedaFormato"
				--,COALESCE(EMI."DocumentoIdentidad",'') || ' - ' || COALESCE(EMI."IdEmisor",'') AS "Emisor"
                ,CASE WHEN  PTA."IdTipoInstrumento" = 'PAG' THEN (SELECT "TIPOMONEDAREAJUSTE" FROM #TempMonedaReajustablePag WHERE "LETRA" = SUBSTRING(PTA."Nemotecnico", 2,1)) ELSE  INS."IdTipoReajuste" END  AS "IdTipoReajuste"
				,PTA."IdTipoTasa"
				,TTA."Descripcion" AS "TipoTasa"
				,PTA."Tasa"
				,PTA."TasaEstimada"
				,PTA."IdTipoPrecio"
				,TPR."Descripcion" AS "TipoPrecio"
				,PTA."Precio"
				,PTA."Plazo"
				,PTA."IdEstado"
				,EST."Descripcion" AS "Estado"
				,PTA."IdEmpresa"
				,PTA."IdUsuarioCreador"
				,PTA."IdUsuarioModificacion"
				,PTA."FechaSolicitud"
				,PTA."FechaIngreso"
				,PTA."FechaCreacion"
				,PTA."FechaModificacion"
				,PTA."FechaPublicacion"
				,PTA."RutaArchivo"
				,PTA."NombreArchivo"
				,PTA."Borrado"
				,PTA."TipoRegistro"
				,PTA."FechaOficializacion"
				,PTA."IdUsuarioOficializador"
				,COALESCE(USUARIO_CREADOR."CodigoUsuario", '') AS "CodigoUsuarioCreador"
				,COALESCE(USUARIO_OFICIALIZADOR."CodigoUsuario", '') AS "CodigoUsuarioOficializador"
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" PTA
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUARIO_CREADOR ON USUARIO_CREADOR."IdUsuario" = PTA."IdUsuarioCreador"
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUARIO_OFICIALIZADOR ON USUARIO_OFICIALIZADOR."IdUsuario" = PTA."IdUsuarioOficializador"
			INNER JOIN (
					SELECT PTA."IdEmpresa", PTA."IdFuente", PTA."IdClasificacionTipoInstrumento", PTA."Nemotecnico", PTA."IdMoneda",
						   PTA."TipoRegistro", COALESCE(PTA."IdTipoTasa",'') AS "IdTipoTasa",
						   COALESCE(PTA."IdTipoPrecio",'') AS "IdTipoPrecio", PTA."Plazo", MAX(PTA."FechaSolicitud") AS "FechaSolicitud"
					FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" PTA
					INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
						ON (PTA."IdFuente" = FU."IdFuente" AND 
							PTA."IdEmpresa" = FU."IdEmpresa" AND
							(FU."Codigo" = :p_CodigoFuente OR :p_CodigoFuente = ''))
					WHERE PTA."Borrado" = 'N'
					  AND PTA."IdEmpresa" = :p_IdEmpresa
					  AND (PTA."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')
					  AND (PTA."IdMoneda" = :p_IdMoneda OR :p_IdMoneda = '')
					  AND (PTA."TipoRegistro" = :p_TipoRegistro OR :p_TipoRegistro = '')
					  AND PTA."IdEstado" = 'PUB'
					GROUP BY PTA."IdEmpresa", PTA."IdFuente", PTA."IdClasificacionTipoInstrumento", PTA."Nemotecnico", PTA."IdMoneda",
							 PTA."TipoRegistro", PTA."IdTipoTasa", PTA."IdTipoPrecio", PTA."Plazo"
					) PTAPUB
				ON (PTA."IdEmpresa"		= PTAPUB."IdEmpresa" AND
					PTA."IdFuente"		= PTAPUB."IdFuente" AND
					PTA."IdClasificacionTipoInstrumento" = PTAPUB."IdClasificacionTipoInstrumento" AND
					PTA."Nemotecnico"	= PTAPUB."Nemotecnico" AND
					PTA."IdMoneda"		= PTAPUB."IdMoneda" AND
					PTA."TipoRegistro"	= PTAPUB."TipoRegistro" AND
					COALESCE(PTA."IdTipoTasa",'')	= PTAPUB."IdTipoTasa" AND
					COALESCE(PTA."IdTipoPrecio",'')	= PTAPUB."IdTipoPrecio" AND
					PTA."Plazo"			= PTAPUB."Plazo" AND
					PTA."FechaSolicitud"= PTAPUB."FechaSolicitud")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
				ON (PTA."IdFuente" = FU."IdFuente" AND 
					PTA."IdEmpresa" = FU."IdEmpresa" AND
					(FU."Codigo" = :p_CodigoFuente OR :p_CodigoFuente = ''))
			/*LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
				ON (PTA."IdInstrumento" = INS."IdInstrumento" AND INS."IdEstado" = 'ACT')*/
			INNER JOIN (SELECT DISTINCT("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento", "Codigo", "Nombre", "IdEstado"
					   FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento") CLA
				ON (PTA."IdClasificacionTipoInstrumento" = CLA."IdClasificacionTipoInstrumento" AND 
					CLA."IdEstado" = 'ACT')
			/*LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
				ON (INS."IdEmisor" = EMI."IdEmisor")*/
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa" TTA
				ON (PTA."IdTipoTasa" = TTA."IdTipoTasa" AND 
					PTA."IdEmpresa" = TTA."IdEmpresa" AND 
					TTA."IdEstado" = 'ACT')
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio" TPR
				ON (PTA."IdTipoPrecio" = TPR."IdTipoPrecio" AND 
					PTA."IdEmpresa" = TPR."IdEmpresa" AND 
					TPR."IdEstado" = 'ACT')
			INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" EST
				ON (PTA."IdEstado" = EST."IdEstado")
			INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
				ON (PTA."IdMoneda" = MON."IdMoneda" AND 
					MON."IdEstado" = 'ACT' AND
					(PTA."IdMoneda" = :p_IdMoneda OR :p_IdMoneda = ''))
            LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."Nemotecnico"  = PTA."Nemotecnico" 
			WHERE PTA."Borrado" = 'N'
			  AND PTA."IdEmpresa" = :p_IdEmpresa
			  AND (PTA."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')
			  AND (PTA."TipoRegistro" = :p_TipoRegistro OR :p_TipoRegistro = '')
			  AND PTA."IdEstado" = 'PUB'
			  AND (FU."Codigo" = :p_CodigoFuente OR :p_CodigoFuente = '')
			;
	ELSE
		v_PrecioTasa =
			SELECT
				 PTA."IdPrecioTasa"
				,PTA."IdFuente"
				,COALESCE(FU."Codigo",' -') AS "CodigoFuente"
				,COALESCE(FU."Descripcion",' -') AS "DescripcionFuente"
				,COALESCE(FU."Codigo",'') || ' - ' || COALESCE(FU."Descripcion",'') AS "CodDescFuente"
				,PTA."IdTipoInstrumento"
				,PTA."IdInstrumento"
				,PTA."Nemotecnico"
				,PTA."IdClasificacionTipoInstrumento"
				,COALESCE(CLA."Codigo",'') || ' - ' || COALESCE(CLA."Nombre",'') AS "Clasificacion"
				,CASE WHEN  PTA."IdTipoInstrumento" = 'PAG' THEN (SELECT "TIPOMONEDA" FROM #TempMonedaReajustablePag WHERE "LETRA" = SUBSTRING(PTA."Nemotecnico", 2,1)) ELSE  PTA."IdMoneda" END as "IdMoneda"
				,PTA."IdMoneda" AS "MonedaFormato"
				--,COALESCE(EMI."DocumentoIdentidad",'') || ' - ' || COALESCE(EMI."IdEmisor",'') AS "Emisor"
                ,CASE WHEN  PTA."IdTipoInstrumento" = 'PAG' THEN (SELECT "TIPOMONEDAREAJUSTE" FROM #TempMonedaReajustablePag WHERE "LETRA" = SUBSTRING(PTA."Nemotecnico", 2,1)) ELSE  INS."IdTipoReajuste" END  AS "IdTipoReajuste"
				,PTA."IdTipoTasa"
				,TTA."Descripcion" AS "TipoTasa"
				,PTA."Tasa"
				,PTA."TasaEstimada"
				,PTA."IdTipoPrecio"
				,TPR."Descripcion" AS "TipoPrecio"
				,PTA."Precio"
				,PTA."Plazo"
				,PTA."IdEstado"
				,EST."Descripcion" AS "Estado"
				,PTA."IdEmpresa"
				,PTA."IdUsuarioCreador"
				,PTA."IdUsuarioModificacion"
				,PTA."FechaSolicitud"
				,PTA."FechaIngreso"
				,PTA."FechaCreacion"
				,PTA."FechaModificacion"
				,PTA."FechaPublicacion"
				,PTA."RutaArchivo"
				,PTA."NombreArchivo"
				,PTA."Borrado"
				,PTA."TipoRegistro"
				,PTA."FechaOficializacion"
				,PTA."IdUsuarioOficializador"
				,COALESCE(USUARIO_CREADOR."CodigoUsuario", '') AS "CodigoUsuarioCreador"
				,COALESCE(USUARIO_OFICIALIZADOR."CodigoUsuario", '') AS "CodigoUsuarioOficializador"
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" PTA
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUARIO_CREADOR ON USUARIO_CREADOR."IdUsuario" = PTA."IdUsuarioCreador"
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUARIO_OFICIALIZADOR ON USUARIO_OFICIALIZADOR."IdUsuario" = PTA."IdUsuarioOficializador"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
				ON (PTA."IdFuente" = FU."IdFuente" AND 
					PTA."IdEmpresa" = FU."IdEmpresa" AND
					(FU."Codigo" = :p_CodigoFuente OR :p_CodigoFuente = ''))
			/*LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
				ON (PTA."IdInstrumento" = INS."IdInstrumento" AND INS."IdEstado" = 'ACT')*/
			LEFT JOIN (SELECT DISTINCT("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento", "Codigo", "Nombre", "IdEstado"
					   FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento") CLA
				ON (PTA."IdClasificacionTipoInstrumento" = CLA."IdClasificacionTipoInstrumento" AND 
					CLA."IdEstado" = 'ACT')
			/*LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
				ON (INS."IdEmisor" = EMI."IdEmisor")*/
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa" TTA
				ON (PTA."IdTipoTasa" = TTA."IdTipoTasa" AND 
					PTA."IdEmpresa" = TTA."IdEmpresa" AND 
					TTA."IdEstado" = 'ACT')
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio" TPR
				ON (PTA."IdTipoPrecio" = TPR."IdTipoPrecio" AND 
					PTA."IdEmpresa" = TPR."IdEmpresa" AND 
					TPR."IdEstado" = 'ACT')
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" EST
				ON (PTA."IdEstado" = EST."IdEstado")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
				ON (PTA."IdMoneda" = MON."IdMoneda" AND 
					MON."IdEstado" = 'ACT' AND
					(PTA."IdMoneda" = :p_IdMoneda OR :p_IdMoneda = ''))
            LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."Nemotecnico"  = PTA."Nemotecnico" 
			WHERE PTA."Borrado" = 'N'
			  AND PTA."IdEmpresa" = :p_IdEmpresa
			  AND (TO_VARCHAR(PTA."FechaSolicitud",'YYYY-MM-DD') = :p_FechaSolicitud OR :p_FechaSolicitud = '0001-01-01')
			  AND (PTA."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')
			  --AND (PTA."IdMoneda" = :p_IdMoneda OR :p_IdMoneda = '')
			  AND (PTA."TipoRegistro" = :p_TipoRegistro OR :p_TipoRegistro = '')
			  AND (PTA."IdEstado" = :p_IdEstado OR :p_IdEstado = '')
			  AND (FU."Codigo" = :p_CodigoFuente OR :p_CodigoFuente = '')
			;
	END IF;
	
	
	p_out_PrecioTasa =
		SELECT
			 PTA."IdPrecioTasa"
			,PTA."IdFuente"
			,PTA."CodigoFuente"
			,PTA."CodDescFuente"
			,PTA."Clasificacion"
			,PTA."Nemotecnico"
			,PTA."IdMoneda"
			,PTA."MonedaFormato"
			,PTA."IdTipoReajuste"
			,PTA."TipoRegistro"
			,PTA."TipoTasa"
			,PTA."Tasa"
			,PTA."TasaEstimada"
			,PTA."TipoPrecio"
			,PTA."Precio"
			,PTA."Plazo"
			,PTA."Estado"
			,PTA."IdClasificacionTipoInstrumento"			
			,PTA."FechaSolicitud"
			,PTA."FechaOficializacion"
			,PTA."FechaPublicacion"
			,PTA."IdUsuarioCreador"
			,PTA."IdUsuarioOficializador"
			,PTA."CodigoUsuarioCreador"
			,PTA."CodigoUsuarioOficializador"
		FROM :v_PrecioTasa PTA
		ORDER BY
			 PTA."IdFuente" ASC
			,PTA."IdClasificacionTipoInstrumento" ASC
			,PTA."Nemotecnico" ASC
			,PTA."IdMoneda" ASC
			,PTA."TipoRegistro" ASC
			,PTA."IdTipoTasa" ASC
			,PTA."IdTipoPrecio" ASC
			,PTA."Plazo" ASC;


   DROP TABLE #TempMonedaReajustablePag;
END;


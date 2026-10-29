SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.ModificarInstrumentos";
GO
CREATE PROCEDURE
--  Proposito: crear instrumento
--  Autor: - TEAM ORDENES 
--  Fecha Creacion: 20/07/2015 
 
"BSCL.Plataforma.BD.Servicios::INS.ModificarInstrumentos" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.
	IN p_Autorizar							NVARCHAR(1), -- S/N se usa en caso de modificacion	

	IN p_Instrumentos			TABLE(
		"IdInstrumento"			BIGINT,
		"ISIN"					NVARCHAR(40),
		"CorteMinimo"			DECIMAL(26,8),
		"EstaInscrito"			NVARCHAR(1),
		"RazonSocial"			NVARCHAR(100),
		"FechaEmision"			NVARCHAR(10),
		"FechaVencimiento"		NVARCHAR(10),
		"IdEmisor"				NVARCHAR(40),
		"IdTipoReajuste"		NVARCHAR(3),
		"IdMoneda"				NVARCHAR(3),
		"IdEstado"				NVARCHAR(3),
		"Tamano"				BIGINT,
		"PrecioBase"			DECIMAL(26,8),
		"PrecioReferencia"		DECIMAL(26,8),
		"FechaReferencia"		NVARCHAR(10),
		"FrecuenciaCupon"		DECIMAL(26,8),
		"NumeroCupones"			INT,
		"InstrumentoAcreditado"	NVARCHAR(1),
		"TipoBonoIRF"			NVARCHAR(3),
		"OrigenInstrumento"		NVARCHAR(1),
		"TipoRenta"				NVARCHAR(1),
		"RelacionadoGrupo"		NVARCHAR(1),
		"FormadorMercado"		NVARCHAR(1),
		"PactoAndino"			NVARCHAR(1),
		"FactorExposicion"		DECIMAL(26,8),
		"LargoPlazo"			NVARCHAR(1),
		"PorcentajeCastigo"     DECIMAL(26,8),
		"EsPutEsCall"       	NVARCHAR(5),
		"PrecioStrike"			DECIMAL(26,8),
		"IdTipoInstrumento"		NVARCHAR(3),
		"Tasa"					DECIMAL(26,8),
		"AfectoArt107"			NVARCHAR(1),
		"AfectoArt104"			NVARCHAR(1),
		"DividendoOtraMoneda"	NVARCHAR(1),
		"EsAutorizado" 			NVARCHAR(1),
		"CobroComisionesCustodia" NVARCHAR(1),
		"IdInstrumentoRaiz"		BIGINT,
		"IdCondicionLiquidacion"NVARCHAR(2),	
		"EsInstrumentoCalificado" NVARCHAR(1)
	),
	
	IN p_ValoresRiesgo TABLE(
		"IdInstrumento"						BIGINT,
		"IdEntidadClasificadoraRiesgo"		NVARCHAR(10),
		"ValorClasificacionRiesgo"			NVARCHAR(20)
	),
	
	IN p_Bolsas TABLE(
		"IdInstrumento"						BIGINT,
		"IdBolsa"							NVARCHAR(3),
		"IdInstrumentoSubyacente"			BIGINT,
		"IdTipoInstrumento"					NVARCHAR(3),
		"IdTipoInstrumentoSubyacente"		NVARCHAR(3),
		"EsGarantizable"					NVARCHAR(1)
	) DEFAULT EMPTY,
    
    OUT p_out_Resultado		TABLE(
    	"IdInstrumento"		BIGINT
    ),						
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInstrumento" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInstrumento" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE maxIdInstrumento	BIGINT;
	
	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador = 0;
	p_out_AdvertenciasDetalle = select 0 as "IdInstrumento",'' as "Codigo", '' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle = select 0 as "IdInstrumento",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	--validacion nemo sin emisor
	nemo_sin_emisor = select "IdInstrumento" 
						from :p_instrumentos p_ins 
						where COALESCE("IdEmisor",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInstrumento", 'INSTRUMENTO_SIN_EMISOR' AS "Codigo", '' AS "Valor"
							from :nemo_sin_emisor;
	---------------------------------------------------------------------------------------	
	--validacion nemo sin emisor
	nemo_sin_moneda = select "IdInstrumento" 
						from :p_instrumentos p_ins 
						where COALESCE("IdMoneda",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInstrumento", 'INSTRUMENTO_SIN_MONEDA' AS "Codigo", '' AS "Valor"
							from :nemo_sin_moneda
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
						----"CodigoDato1", "ValorDato1"
	---------------------------------------------------------------------------------------	
	--validacion isin con cartera asociada
	isin_con_cartera = select distinct Ins."IdInstrumento" AS "IdInstrumento", Ins."ISIN" 
	                  from "BSCL.Plataforma.BD::INS.Instrumento" AS Ins
	                    inner join :p_instrumentos as p_ins 
	                       on p_ins."IdInstrumento" = Ins."IdInstrumento"
	                    inner join "BSCL.Custodia.BD::CUS.Cartera" AS Car
	                       on  Car."ISIN" = Ins."ISIN"
	                       and Car."ISIN" <> ''
                      where p_ins."ISIN" <> Ins."ISIN"
					  and Car."IdClasificacionTipoInstrumento" = 'FM';
                       

    v_out_ErroresDetalle = select "IdInstrumento", 'ISIN_CARTERA' AS "Codigo", "ISIN" AS "Valor"
							from :isin_con_cartera
							UNION ALL SELECT * FROM :v_out_ErroresDetalle; 		
	---------------------------------------------------------------------------------------
	--validacion 1 nemo por mercado (IdClasificacionTipoInstrumento)
        nemos_con_mercado_repetido = SELECT p_ins."IdInstrumento"
                                                                        FROM :p_Instrumentos p_ins
                                                                        INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on p_ins."IdInstrumento" = inst."IdInstrumento"
                                                                        INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" nemoInst ON inst."Nemotecnico" = nemoInst."Nemotecnico"
                                                                        INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoInstNemo ON nemoInst."IdTipoInstrumento" = tipoInstNemo."IdTipoInstrumento"
                                                                        INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoInstEntrada ON p_ins."IdTipoInstrumento" = tipoInstEntrada."IdTipoInstrumento"
                                                                        WHERE p_ins."IdInstrumento" <> nemoInst."IdInstrumento"
                                                                                 AND tipoInstEntrada."IdClasificacionTipoInstrumento" = tipoInstNemo."IdClasificacionTipoInstrumento";
                      
    v_out_ErroresDetalle = select "IdInstrumento", 'NEMOTECNICO_CON_MERCADO_REPETIDO' AS "Codigo", '' AS "Valor"
                                                        from :nemos_con_mercado_repetido
                                                        UNION ALL SELECT * FROM :v_out_ErroresDetalle;
    ---------------------------------------------------------------------------------------
   	--validacion instrumento en custodia
	v_en_custodia_ina =	SELECT DISTINCT I."IdInstrumento"
					  	FROM :p_Instrumentos I
					  	JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" C
					    ON C."IdInstrumento" = I."IdInstrumento"
					   	AND C."IdEmpresa"     = :p_IdEmpresa
					  	WHERE I."IdEstado" = 'INA';
						
	IF (SELECT COUNT(1) FROM :v_en_custodia_ina) > 0 THEN
  		v_out_ErroresDetalle =	SELECT I."IdInstrumento",
					           	'INSTRUMENTO_EN_CUSTODIA' AS "Codigo",
					           	'' AS "Valor"
						    	FROM :v_en_custodia_ina I
						    	UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	END IF;
    ---------------------------------------------------------------------------------------
    nemos_ok =	select "IdInstrumento" 
				from :p_instrumentos p_ins 
				except
				select "IdInstrumento" from :nemo_sin_emisor
				except
				select "IdInstrumento" from :nemo_sin_moneda
				except 
			    select "IdInstrumento" from :isin_con_cartera
			   	except
			   	select "IdInstrumento" FROM :nemos_con_mercado_repetido
			   	except
			    select "IdInstrumento" from :v_en_custodia_ina;
	---------------------------------------------------------------------------------------
    --guarda los valores antiguos para registrarlo en el log
    v_ValoresAntiguos =   SELECT   Ins."IdInstrumento"
                                ,Ins."EsAutorizado"                                 AS "EsAutorizado"
                                ,Ins."IdTipoInstrumento"                            AS "IdTipoInstrumento"
                                ,Ins."ISIN"                                         AS "ISIN"
                                ,Ins."IdEmisor"                                     AS "IdEmisor"
                                ,Ins."EstaInscrito"                                 AS "EstaInscrito" 
                                ,Ins."AfectoArt107"                                 AS "AfectoArt107" 
                                ,Ins."AfectoArt104"                                 AS "AfectoArt104" 
                                ,Ins."CobroComisionesCustodia"                      AS "CobroComisionesCustodia" 
                                ,TO_NVARCHAR(Ins."FechaEmision", 'YYYY-MM-DD')      AS "FechaEmision"
                                ,TO_NVARCHAR(Ins."FechaVencimiento", 'YYYY-MM-DD')  AS "FechaVencimiento" 
                                ,Ins."IdTipoReajuste"                               AS "IdTipoReajuste" 
                                ,Ins."CorteMinimo"                                  AS "CorteMinimo" 
                                ,Ins."Precio"                                       AS "PrecioBase"
                                ,Ins."Tasa"                                         AS "Tasa" 
                                ,Ins."Tamano"                                     AS "Tamano"
                                ,Ins."EsInstrumentoCalificado"                      AS "EsInstrumentoCalificado" 
                                ,Ins."IdMoneda"                                     AS "IdMoneda"
                                ,Ins."DividendoOtraMoneda" 							AS "DividendoOtraMoneda"
								,Ins."RazonSocial"									AS "RazonSocial"
								,Ins."IdEstado"										AS "IdEstado"
								,Ins."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"
								,Cla."Nombre"										AS "Mercado"
                        FROM "BSCL.Plataforma.BD::INS.Instrumento" Ins
                        INNER JOIN :p_instrumentos AS p_ins ON p_ins."IdInstrumento" = Ins."IdInstrumento"
                        LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" Ti ON Ti."IdTipoInstrumento" = Ins."IdTipoInstrumento" 
						LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" Cla ON Cla."IdClasificacionTipoInstrumento"  = Ti."IdClasificacionTipoInstrumento";
	
	v_NuevoMercado =		SELECT 	 p_ins."IdInstrumento"
								,Cla."Nombre"											AS "Mercado"
						FROM :p_instrumentos p_ins 
						LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" Ti ON Ti."IdTipoInstrumento" = p_ins."IdTipoInstrumento"
						LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" Cla ON Cla."IdClasificacionTipoInstrumento"  = Ti."IdClasificacionTipoInstrumento";
    ---------------------------------------------------------------------------------------
				
	v_instrumentos = SELECT p_ins."IdInstrumento",
							COALESCE(p_ins."ISIN",ins."ISIN",'') 									AS "ISIN",
							COALESCE(p_ins."CorteMinimo",ins."CorteMinimo",0) 						AS "CorteMinimo",
							COALESCE(p_ins."EstaInscrito",ins."EstaInscrito",'N') 					AS "EstaInscrito",
							COALESCE(p_ins."RazonSocial",ins."RazonSocial",'') 						AS "RazonSocial",
							COALESCE(p_ins."FechaEmision",ins."FechaEmision",'0001-01-01') 			AS "FechaEmision",
							COALESCE(p_ins."FechaVencimiento",ins."FechaVencimiento",'0001-01-01') 	AS "FechaVencimiento",
							COALESCE(p_ins."IdEmisor",ins."IdEmisor") 								AS "IdEmisor",
							p_ins."IdTipoReajuste",
							COALESCE(p_ins."IdMoneda",ins."IdMoneda") 								AS "IdMoneda",
							COALESCE(p_ins."IdEstado",ins."IdEstado",'INA') 						AS "IdEstado",
							COALESCE(p_ins."Tamano",ins."Tamano",0) 								AS "Tamano",
							COALESCE(p_ins."PrecioBase",null)                                       AS "Precio",
							COALESCE(p_ins."PrecioReferencia",ins."PrecioReferencia",0) 			AS "PrecioReferencia",
							COALESCE(p_ins."FechaReferencia",ins."FechaReferencia",'0001-01-01') 	AS "FechaReferencia",
							COALESCE(p_ins."FrecuenciaCupon",ins."FrecuenciaCupon",0) 				AS "FrecuenciaCupon",
							COALESCE(p_ins."NumeroCupones",ins."NumeroCupones",0) 					AS "NumeroCupones",
							COALESCE(p_ins."InstrumentoAcreditado",'N') 							AS "InstrumentoAcreditado",
							COALESCE(p_ins."TipoBonoIRF",'') 										AS "TipoBonoIRF",
							COALESCE(p_ins."OrigenInstrumento",'') 									AS "OrigenInstrumento",
							COALESCE(p_ins."TipoRenta",'') 											AS "TipoRenta",
							CASE COALESCE(p_ins."RelacionadoGrupo",'N')
								WHEN 'O' THEN ''
								ELSE COALESCE(p_ins."RelacionadoGrupo",'N')
							END 																	AS "RelacionadoGrupo",
							CASE COALESCE(p_ins."FormadorMercado",'N')
								WHEN 'O' THEN ''
								ELSE COALESCE(p_ins."FormadorMercado",'N')
							END 																	AS "FormadorMercado",
							CASE COALESCE(p_ins."PactoAndino",'N')
								WHEN 'O' THEN ''
								ELSE COALESCE(p_ins."PactoAndino",'N')
							END 																	AS "PactoAndino", 
							COALESCE(p_ins."FactorExposicion",0) 									AS "FactorExposicion",
							COALESCE(p_ins."LargoPlazo",'N') 										AS "LargoPlazo",
							COALESCE(p_ins."PorcentajeCastigo",0)                                   AS "PorcentajeCastigo",
							COALESCE(p_ins."EsPutEsCall",'') 										AS "EsPutEsCall",
							COALESCE(p_ins."PrecioStrike",0)                                   		AS "PrecioStrike",
							COALESCE(p_ins."IdTipoInstrumento",'')                                  AS "IdTipoInstrumento",
							COALESCE(p_ins."Tasa",0)                                   				AS "Tasa",
							COALESCE(p_ins."AfectoArt107",ins."AfectoArt107",'N')					AS "AfectoArt107",
							COALESCE(p_ins."AfectoArt104",ins."AfectoArt104",'N')					AS "AfectoArt104",
							COALESCE(p_ins."DividendoOtraMoneda",ins."DividendoOtraMoneda",'N')		AS "DividendoOtraMoneda",
							COALESCE(p_ins."EsAutorizado",ins."EsAutorizado",'N')					AS "EsAutorizado",
							COALESCE(p_ins."CobroComisionesCustodia", 'S') 							AS "CobroComisionesCustodia",
							COALESCE(p_ins."IdInstrumentoRaiz",ins."IdInstrumentoRaiz",0)				AS "IdInstrumentoRaiz",
							COALESCE(p_ins."IdCondicionLiquidacion",ins."IdCondicionLiquidacion",'')	AS "IdCondicionLiquidacion",
							COALESCE(p_ins."EsInstrumentoCalificado", 'N') 							AS "EsInstrumentoCalificado"
							FROM :p_instrumentos p_ins
							LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on p_ins."IdInstrumento" = ins."IdInstrumento"
							INNER JOIN :nemos_ok nemo on p_ins."IdInstrumento" = nemo."IdInstrumento";
	---------------------------------------------------------------------------------------
	UPDATE INST
	SET 
		"ISIN" = p_ins."ISIN",
		"CorteMinimo" 			= p_ins."CorteMinimo",
		"EstaInscrito" 			= p_ins."EstaInscrito",
		"RazonSocial" 			= p_ins."RazonSocial",
		"FechaEmision" 			= p_ins."FechaEmision",
		"FechaVencimiento" 		= p_ins."FechaVencimiento",
		"IdEmisor" 				= p_ins."IdEmisor",
		"IdTipoReajuste" 		= COALESCE(p_ins."IdTipoReajuste",''),
		"IdMoneda" 				= p_ins."IdMoneda",
		"IdEstado" 				= p_ins."IdEstado",
		"Tamano" 				= p_ins."Tamano",
		"Precio" 				= p_ins."Precio",
		"PrecioReferencia" 		= p_ins."PrecioReferencia",
		"FechaReferencia" 		= p_ins."FechaReferencia",
		"FrecuenciaCupon" 		= p_ins."FrecuenciaCupon",
		"NumeroCupones" 		= p_ins."NumeroCupones",
		"InstrumentoAcreditado"	= p_ins."InstrumentoAcreditado",
		"TipoBonoIRF"			= p_ins."TipoBonoIRF",
		"OrigenInstrumento"		= p_ins."OrigenInstrumento",
		"TipoRenta"				= p_ins."TipoRenta",
		"RelacionadoGrupo"		= p_ins."RelacionadoGrupo",
		"FormadorMercado"		= p_ins."FormadorMercado",
		"PactoAndino"			= p_ins."PactoAndino",
		"FactorExposicion"		= p_ins."FactorExposicion",
		"LargoPlazo"			= p_ins."LargoPlazo",
		"PorcentajeCastigo"     = p_ins."PorcentajeCastigo",
		"EsPutEsCall"			= p_ins."EsPutEsCall",
		"PrecioStrike"     		= p_ins."PrecioStrike",
		"IdTipoInstrumento"     = p_ins."IdTipoInstrumento",
		"Tasa"					= p_ins."Tasa",
		"AfectoArt107"			= p_ins."AfectoArt107",
		"AfectoArt104"			= p_ins."AfectoArt104",
		"DividendoOtraMoneda"	= p_ins."DividendoOtraMoneda",
		"EsAutorizado"			= p_ins."EsAutorizado",
		"CobroComisionesCustodia" = p_ins."CobroComisionesCustodia",
		"IdInstrumentoRaiz"		= p_ins."IdInstrumentoRaiz",
		"IdCondicionLiquidacion"= p_ins."IdCondicionLiquidacion",
		"EsInstrumentoCalificado" = p_ins."EsInstrumentoCalificado"
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INST
	INNER JOIN :v_instrumentos p_ins on INST."IdInstrumento" = p_ins."IdInstrumento";
	
	UPDATE SERIE
	SET "ISIN" = p_ins."ISIN"
	FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE
	INNER JOIN :v_instrumentos p_ins on SERIE."IdInstrumento" = p_ins."IdInstrumento";
	
	UPDATE SERIE
	SET "IdEstado" = p_ins."IdEstado"
	FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE
	INNER JOIN :v_instrumentos p_ins on SERIE."IdInstrumento" = p_ins."IdInstrumento";

	UPDATE CART
	SET CART."ISIN" = p_ins."ISIN"
	FROM "BSCL.Custodia.BD::CUS.Cartera" CART
	INNER JOIN :v_instrumentos p_ins on CART."IdInstrumento" = p_ins."IdInstrumento";

	UPDATE GARA
	SET GARA."ISIN" = p_ins."ISIN"
	FROM "BSCL.Custodia.BD::GTA.Garantia" GARA
	INNER JOIN :v_instrumentos p_ins on GARA."IdInstrumento" = p_ins."IdInstrumento";

	UPDATE SUBY
	SET SUBY."ISIN" = p_ins."ISIN"
	FROM "BSCL.Custodia.BD::CUS.Subyacente" SUBY
	INNER JOIN :v_instrumentos p_ins on SUBY."IdInstrumento" = p_ins."IdInstrumento";

	UPDATE MCET
	SET MCET."ISIN" = p_ins."ISIN"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MCET
	INNER JOIN :v_instrumentos p_ins on MCET."IdInstrumento" = p_ins."IdInstrumento";
	---------------------------------------------------------------------------------------
	
	DELETE FROM "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" WHERE "IdInstrumento" IN (SELECT "IdInstrumento" FROM :v_instrumentos) AND :p_Autorizar = 'S';
	
	INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"("IdInstrumento","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo")
	SELECT val."IdInstrumento",val."IdEntidadClasificadoraRiesgo",val."ValorClasificacionRiesgo" 
	FROM :v_instrumentos ins 
	INNER JOIN :p_ValoresRiesgo val on ins."IdInstrumento" = val."IdInstrumento" AND :p_Autorizar = 'S';	
	
	DELETE FROM "BSCL.Plataforma.BD::INS.BolsaInstrumento" WHERE "IdInstrumento" IN (SELECT "IdInstrumento" FROM :v_instrumentos) AND :p_Autorizar = 'S';

	v_Bolsas = SELECT "IdInstrumento", "IdBolsa" FROM :p_Bolsas GROUP BY "IdInstrumento", "IdBolsa";

	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento"("IdBolsaInstrumento", "IdBolsa","IdInstrumento")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL AS "IdBolsaInstrumento",bol."IdBolsa",ins."IdInstrumento"
	FROM :v_instrumentos ins
	INNER JOIN :v_Bolsas bol ON ins."IdInstrumento" = bol."IdInstrumento" AND :p_Autorizar = 'S';

	DELETE FROM "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" WHERE "IdInstrumento" IN (SELECT "IdInstrumento" FROM :v_instrumentos) AND :p_Autorizar = 'S';

	v_Operaciones = SELECT "IdTipoInstrumento", "IdBolsa", "IdTipoInstrumentoSubyacente", "IdInstrumento", "EsGarantizable" FROM :p_Bolsas WHERE "IdTipoInstrumento" <> '';

	INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdInstrumento","EsGarantizable")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.InstrumentoSubyacente".NEXTVAL AS "IdInstrumentoSubyacente", ope."IdTipoInstrumento", ope."IdBolsa", ope."IdTipoInstrumentoSubyacente", ope."IdInstrumento", ope."EsGarantizable"
	FROM :v_Operaciones ope
	WHERE :p_Autorizar = 'S';
	
	v_tablaMensajes = select "IdInstrumento" as "IdInterno", "Codigo", 'IdInstrumento' AS "CodigoDato1", "Valor" AS "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from :v_out_ErroresDetalle;
	
	CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_tablaMensajes,v_out_TablaMensajes);
	
	p_out_ErroresDetalle = select det."IdInstrumento",det."Codigo",outt."Descripcion" from :v_out_ErroresDetalle det INNER JOIN :v_out_TablaMensajes outt on outt."IdInterno" = det."IdInstrumento" and outt."Codigo" = det."Codigo";
	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
	
	p_out_Resultado = select "IdInstrumento" from :v_instrumentos;
		
	--LOG DE AUDITORIA
	BEGIN
		DECLARE p_Log TABLE(
			"IdCabeceraLog"    BIGINT,
			"IdTipoLog"     NVARCHAR(40),
			"IdModulo"     NVARCHAR(3),
			"IdUsuario"     BIGINT,
			"IdEmpresa"     BIGINT,
			"FechaSistemaLog"   NVARCHAR(10),
			"IdUsuarioAtendido"   BIGINT,
			"IdPersona"     BIGINT,
			"IdCliente"     BIGINT,
			"EstadoCliente"    NVARCHAR(3),
			"NombreCliente"    NVARCHAR(100),
			"IdTipoDocumentoIdentidad" NVARCHAR(40),
			"IdDocumentoIdentidad"  NVARCHAR(40),
			"IdPortafolio"    BIGINT,
			"EstadoPortafolio"   NVARCHAR(3),
			"IdTipoRegistro"   NVARCHAR(40),
			"IdRegistro"    NVARCHAR(40),
			"EstadoRegistro"   NVARCHAR(3),
			"IdTipoOperacion"   NVARCHAR(40),
			"Folio"      BIGINT,
			"Cantidad"     DECIMAL(26,8),
			"PrecioTasa"    DECIMAL(26,8),
			"Monto"      DECIMAL(26,8),
			"IdMonedaOrigen"   NVARCHAR(3),
			"IdMonedaLiquidacion"  NVARCHAR(3),
			"TipoCambio"    NVARCHAR(40),
			"Nemo"      NVARCHAR(260),
			"IdProceso"     BIGINT,
			"EstadoProceso"    NVARCHAR(3),
			"NombreProceso"    NVARCHAR(100)
		);
		DECLARE p_IdLog BIGINT;
		DECLARE p_Nemo NVARCHAR(260);
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		DECLARE v_DetalleLog INT;
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa => p_IdEmpresa,
			p_out_FechaHoraSistema => v_fechaHoraSistema
		);
		SELECT "Nemotecnico" INTO p_Nemo FROM "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" WHERE "IdInstrumento" IN (SELECT "IdInstrumento" FROM :p_Instrumentos) LIMIT 1;
		SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;
		p_Log =
			Select
			p_IdLog as "IdCabeceraLog"
			,'MODIFICACION' as "IdTipoLog"
			,p_IdModulo as "IdModulo"
			,p_IdUsuario as "IdUsuario"
			,p_IdEmpresa as "IdEmpresa"
			,TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') as "FechaSistemaLog"
			,p_IdUsuario as "IdUsuarioAtendido"
			,0 as "IdPersona"
		    ,0 as "IdCliente"
			,'' as "EstadoCliente"
			,COALESCE(entrada."RazonSocial", '') as "NombreCliente"
			,'' as "IdTipoDocumentoIdentidad"
			,'' as "IdDocumentoIdentidad"
			,0 as "IdPortafolio"
			,'' as "EstadoPortafolio"
			,'EDITAR_INSTRUMENTO' as "IdTipoRegistro"
			,'' as "IdRegistro"
			,COALESCE(entrada."IdEstado", '') as "EstadoRegistro"
			,'' as "IdTipoOperacion"
			,0 as "Folio"
			,0 as "Cantidad"
			,0 as "PrecioTasa" 
			,0 as "Monto"
			,COALESCE(entrada."IdMoneda", '') as "IdMonedaOrigen"
			,'' as "IdMonedaLiquidacion"
			,'' as "TipoCambio"
			,COALESCE (p_Nemo, '') as "Nemo"
			,0 as "IdProceso"
			,'' as "EstadoProceso"
			,'Modificar Instrumento Plataforma' as "NombreProceso"
			FROM :p_Instrumentos as entrada;
		p_LogDetalle =
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'Autorizado' as "CampoRegistro"
	            ,VA."EsAutorizado" as "ValorAntiguo"
	            ,COALESCE(detlog1."EsAutorizado", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog1
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog1."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."EsAutorizado" <> COALESCE(detlog1."EsAutorizado", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'Mercado' as "CampoRegistro"
	            ,VA."Mercado" as "ValorAntiguo"
	            ,NM."Mercado" as "ValorNuevo"
	        FROM :p_Instrumentos as detlog2
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog2."IdInstrumento" = VA."IdInstrumento"
	        INNER JOIN :v_NuevoMercado AS NM ON NM."IdInstrumento" = detlog2."IdInstrumento"
	        WHERE VA."Mercado" <> NM."Mercado"
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'TipoInstrumento' as "CampoRegistro"
	            ,VA."IdTipoInstrumento" as "ValorAntiguo"
	            ,COALESCE(detlog3."IdTipoInstrumento", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog3
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog3."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."IdTipoInstrumento" <> COALESCE(detlog3."IdTipoInstrumento", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'ISIN' as "CampoRegistro"
	            ,VA."ISIN" as "ValorAntiguo"
	            ,COALESCE(detlog4."ISIN", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog4
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog4."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."ISIN" <> COALESCE(detlog4."ISIN", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'Emisor' as "CampoRegistro"
	            ,VA."IdEmisor" as "ValorAntiguo"
	            ,COALESCE(detlog5."IdEmisor", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog5
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog5."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."IdEmisor" <> COALESCE(detlog5."IdEmisor", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'Inscrito' as "CampoRegistro"
	            ,VA."EstaInscrito" as "ValorAntiguo"
	            ,COALESCE(detlog6."EstaInscrito", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog6
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog6."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."EstaInscrito" <> COALESCE(detlog6."EstaInscrito", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'Acog.Art.107' as "CampoRegistro"
	            ,VA."AfectoArt107" as "ValorAntiguo"
	            ,COALESCE(detlog7."AfectoArt107", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog7
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog7."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."AfectoArt107" <> COALESCE(detlog7."AfectoArt107", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'Acog.Art.104' as "CampoRegistro"
	            ,VA."AfectoArt104" as "ValorAntiguo"
	            ,COALESCE(detlog8."AfectoArt104", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog8
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog8."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."AfectoArt104" <> COALESCE(detlog8."AfectoArt104", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'CobroComisionesCustodia' as "CampoRegistro"
	            ,VA."CobroComisionesCustodia" as "ValorAntiguo"
	            ,COALESCE(detlog9."CobroComisionesCustodia", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog9
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog9."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."CobroComisionesCustodia" <> COALESCE(detlog9."CobroComisionesCustodia", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'FechaEmision' as "CampoRegistro"
	            ,VA."FechaEmision" as "ValorAntiguo"
	            ,COALESCE(detlog10."FechaEmision", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog10
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog10."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."FechaEmision" <> COALESCE(detlog10."FechaEmision", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'FechaVencimiento' as "CampoRegistro"
	            ,VA."FechaVencimiento" as "ValorAntiguo"
	            ,COALESCE(detlog11."FechaVencimiento", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog11
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog11."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."FechaVencimiento" <> COALESCE(detlog11."FechaVencimiento", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'TipoReajuste' as "CampoRegistro"
	            ,VA."IdTipoReajuste" as "ValorAntiguo"
	            ,COALESCE(detlog12."IdTipoReajuste", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog12
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog12."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."IdTipoReajuste" <> COALESCE(detlog12."IdTipoReajuste", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'CorteMinimo' as "CampoRegistro"
	            ,TO_VARCHAR(VA."CorteMinimo") as "ValorAntiguo"
	            ,TO_VARCHAR(COALESCE(detlog13."CorteMinimo", VA."CorteMinimo", 0)) as "ValorNuevo"
	        FROM :p_Instrumentos as detlog13
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog13."IdInstrumento" = VA."IdInstrumento"
	        WHERE COALESCE(VA."CorteMinimo", 0) <> COALESCE(detlog13."CorteMinimo", VA."CorteMinimo", 0)
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'PrecioBase' as "CampoRegistro"
	            ,TO_VARCHAR(VA."PrecioBase") as "ValorAntiguo"
	            ,COALESCE(TO_VARCHAR(detlog14."PrecioBase"), '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog14
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog14."IdInstrumento" = VA."IdInstrumento"
	        WHERE TO_VARCHAR(VA."PrecioBase") <> COALESCE(TO_VARCHAR(detlog14."PrecioBase"), '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'TasaTir' as "CampoRegistro"
	            ,TO_VARCHAR(VA."Tasa") as "ValorAntiguo"
	            ,TO_VARCHAR(COALESCE(detlog15."Tasa", 0)) as "ValorNuevo"
	        FROM :p_Instrumentos as detlog15
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog15."IdInstrumento" = VA."IdInstrumento"
	        WHERE COALESCE(VA."Tasa", 0) <> COALESCE(detlog15."Tasa", 0)
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'TamContrato' as "CampoRegistro"
	            ,TO_VARCHAR(VA."Tamano") as "ValorAntiguo"
	            ,TO_VARCHAR(COALESCE(detlog16."Tamano", VA."Tamano", 0)) as "ValorNuevo"
	        FROM :p_Instrumentos as detlog16
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog16."IdInstrumento" = VA."IdInstrumento"
	        WHERE COALESCE(VA."Tamano", 0) <> COALESCE(detlog16."Tamano", VA."Tamano", 0)
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'EsInstrumentoCalificado' as "CampoRegistro"
	            ,TO_VARCHAR(VA."EsInstrumentoCalificado") as "ValorAntiguo"
	            ,COALESCE(TO_VARCHAR(detlog17."EsInstrumentoCalificado"), '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog17
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog17."IdInstrumento" = VA."IdInstrumento"
	        WHERE TO_VARCHAR(VA."EsInstrumentoCalificado") <> COALESCE(TO_VARCHAR(detlog17."EsInstrumentoCalificado"), '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'IdMoneda' as "CampoRegistro"
	            ,COALESCE(TO_VARCHAR(VA."IdMoneda"), '') as "ValorAntiguo"
	            ,COALESCE(TO_VARCHAR(detlog18."IdMoneda"), '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog18
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog18."IdInstrumento" = VA."IdInstrumento"
	        WHERE COALESCE(TO_VARCHAR(VA."IdMoneda"), '') <> COALESCE(TO_VARCHAR(detlog18."IdMoneda"), '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'DividendoOtraMoneda' as "CampoRegistro"
	            ,VA."DividendoOtraMoneda" as "ValorAntiguo"
	            ,COALESCE(detlog19."DividendoOtraMoneda", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog19
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog19."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."DividendoOtraMoneda" <> COALESCE(detlog19."DividendoOtraMoneda", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'RazonSocial' as "CampoRegistro"
	            ,VA."RazonSocial" as "ValorAntiguo"
	            ,COALESCE(detlog20."RazonSocial", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog20
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog20."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."RazonSocial" <> COALESCE(detlog20."RazonSocial", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'IdEstado' as "CampoRegistro"
	            ,VA."IdEstado" as "ValorAntiguo"
	            ,COALESCE(detlog21."IdEstado", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog21
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog21."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."IdEstado" <> COALESCE(detlog21."IdEstado", '')
	        UNION ALL
	        SELECT  
	            p_IdLog as "IdCabeceraLog"
	            ,'MODIFICACION' as "SubRegistro"
	            ,'IdCondicionLiquidacion' as "CampoRegistro"
	            ,VA."IdCondicionLiquidacion" as "ValorAntiguo"
	            ,COALESCE(detlog22."IdCondicionLiquidacion", '') as "ValorNuevo"
	        FROM :p_Instrumentos as detlog22
	        INNER JOIN :v_ValoresAntiguos AS VA ON detlog22."IdInstrumento" = VA."IdInstrumento"
	        WHERE VA."IdCondicionLiquidacion" <> COALESCE(detlog22."IdCondicionLiquidacion", '');
	       
	    
	    SELECT COUNT(1) INTO v_DetalleLog FROM :p_LogDetalle;
	
		IF v_DetalleLog > 0 THEN	
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (
				p_Log   => :p_Log,
				p_LogDetalle => :p_LogDetalle
			);	
		END IF;
	END;
END;
GO

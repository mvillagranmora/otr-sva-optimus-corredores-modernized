SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.CrearInstrumentos";
GO
CREATE PROCEDURE
--  Proposito: crear instrumento
--  Autor: - TEAM ORDENES
--  Fecha Creacion: 20/07/2015 
 
"BSCL.Plataforma.BD.Servicios::INS.CrearInstrumentos" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.

	IN p_Instrumentos TABLE(
		"IdInterno"						BIGINT,
		"Nemotecnico"					NVARCHAR(40),
		"ISIN"							NVARCHAR(40),
		"CorteMinimo"					DECIMAL(26,8),
		"EstaInscrito"					NVARCHAR(1),
		"RazonSocial"					NVARCHAR(100),
		"FechaEmision"					NVARCHAR(10),
		"FechaVencimiento"				NVARCHAR(10),
		"IdEmisor"						NVARCHAR(40),
		"IdTipoReajuste"				NVARCHAR(3),
		"IdMoneda"						NVARCHAR(3),
		"IdTipoInstrumento"				NVARCHAR(3),
		"IdEstado"						NVARCHAR(3),
		"Tamano"						BIGINT,
		"IdEntidadClasificadoraRiesgo"	NVARCHAR(10),
		"ValorClasificacionRiesgo"		NVARCHAR(20),
		"PrecioBase"						DECIMAL(26,8),
		"PrecioReferencia"				DECIMAL(26,8),
		"FechaReferencia"				NVARCHAR(10),
		"FrecuenciaCupon"				DECIMAL(26,8),
		"NumeroCupones"					INT,
		"InstrumentoAcreditado"			NVARCHAR(1),
		"IdInstrumentoRelacionado"		BIGINT,
		"EsPutEsCall"       			NVARCHAR(5),
		"PrecioStrike"					DECIMAL(26,8),
		"TipoBonoIRF"					NVARCHAR(3),
		"OrigenInstrumento"				NVARCHAR(1),
		"TipoRenta"						NVARCHAR(1),
		"RelacionadoGrupo"				NVARCHAR(1),
		"FormadorMercado"				NVARCHAR(1),
		"PactoAndino"					NVARCHAR(1),
		"FactorExposicion"				DECIMAL(26,8),
		"LargoPlazo"					NVARCHAR(1),
		"Tasa"							DECIMAL(26,8),
		"AfectoArt107"					NVARCHAR(1),
		"AfectoArt104"					NVARCHAR(1),
		"DividendoOtraMoneda"			NVARCHAR(1),
		"EsAutorizado"					NVARCHAR(1),
		"CobroComisionesCustodia"		NVARCHAR(1),
		"IdInstrumentoRaiz"				BIGINT,
		"IdCondicionLiquidacion"		NVARCHAR(2),
		"EsInstrumentoCalificado"		NVARCHAR(1)
		
	),
    
    IN p_ValoresRiesgo TABLE(
		"IdInterno"						BIGINT,
		"IdEntidadClasificadoraRiesgo"		NVARCHAR(10),
		"ValorClasificacionRiesgo"			NVARCHAR(20)
	),
	
	IN p_Bolsas TABLE(
		"IdInterno"							BIGINT,
		"IdBolsa"							NVARCHAR(3)
	) DEFAULT EMPTY,
	
    OUT p_out_Resultado		TABLE(
    	"IdInterno"			BIGINT,
    	"IdInstrumento"		BIGINT,
    	"Nemotecnico"		NVARCHAR(40) 	
    ),						
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE v_idInstrumentoSubyacente bigint;
	DECLARE v_Locale	NVARCHAR(40);
	
	SELECT "Valor" INTO v_Locale
	FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.LocalizacionSistema';
	
	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador = 0;
	p_out_AdvertenciasDetalle = select 0 as "IdInterno",'' as "Codigo", '' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle = select 0 as "IdInterno",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	---------------------------------------------------------------------------------------
	--validacion nemo en blanco
	sin_nemo = select "IdInterno"
						from :p_instrumentos p_ins 
						where COALESCE("Nemotecnico",'') = '';
	
	v_out_ErroresDetalle = select "IdInterno", 'NEMOTECNICO_VACIO' AS "Codigo"
							from :sin_nemo;
	---------------------------------------------------------------------------------------
	--validacion nemo repetido para mercado rv y fm (estos mercados pueden compartir nemo)
	nemos_repetidos_RV_FM = select "IdInterno", 'NEMOTECNICO_CON_MERCADO_REPETIDO' AS "Codigo" 
						     from :p_instrumentos p_ins
						     INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on p_ins."Nemotecnico" = inst."Nemotecnico"
						     LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" instTipoInst ON inst."IdTipoInstrumento" = instTipoInst."IdTipoInstrumento"
						     LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" p_insTipoInst ON p_ins."IdTipoInstrumento" = p_insTipoInst."IdTipoInstrumento"
						     WHERE instTipoInst."IdClasificacionTipoInstrumento" IN ('FM', 'RV')
						     	   AND p_insTipoInst."IdClasificacionTipoInstrumento" IN ('FM', 'RV')
	     	   					   AND instTipoInst."IdClasificacionTipoInstrumento" = p_insTipoInst."IdClasificacionTipoInstrumento"
	     	   				UNION 
	     	   				
	     	   				select "IdInterno", 'NEMOTECNICO_YA_EXISTE' AS "Codigo"
						     from :p_instrumentos p_ins
						     INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on p_ins."Nemotecnico" = inst."Nemotecnico"
						     LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" instTipoInst ON inst."IdTipoInstrumento" = instTipoInst."IdTipoInstrumento"
						     WHERE instTipoInst."IdClasificacionTipoInstrumento" NOT IN ('FM', 'RV');
	
	v_out_ErroresDetalle = select DISTINCT "IdInterno", "Codigo"
							from :nemos_repetidos_RV_FM
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------
	--validacion nemo repetido
	nemos_repetidos = select "IdInterno" 
						from :p_instrumentos p_ins 
						INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on p_ins."Nemotecnico" =  inst."Nemotecnico"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoInst ON tipoInst."IdTipoInstrumento" = inst."IdTipoInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" p_insTipoInst ON p_ins."IdTipoInstrumento" = p_insTipoInst."IdTipoInstrumento"
						WHERE p_insTipoInst."IdClasificacionTipoInstrumento" NOT IN ('FM', 'RV');
	
	
	v_out_ErroresDetalle = select "IdInterno", 'NEMOTECNICO_YA_EXISTE' AS "Codigo"
							from :nemos_repetidos
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------
	--validacion nemo sin tipo instrumento
	nemo_sin_tipo_instrumento = select "IdInterno" 
						from :p_instrumentos p_ins 
						where COALESCE("IdTipoInstrumento",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'INSTRUMENTO_SIN_TIPO' AS "Codigo"
							from :nemo_sin_tipo_instrumento
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------	
	--validacion nemo sin emisor
	nemo_sin_emisor = select "IdInterno" 
						from :p_instrumentos p_ins 
						where COALESCE("IdEmisor",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'INSTRUMENTO_SIN_EMISOR' AS "Codigo"
							from :nemo_sin_emisor
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------	
	--validacion nemo sin emisor
	nemo_sin_moneda = select "IdInterno" 
						from :p_instrumentos p_ins 
						where COALESCE("IdMoneda",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'INSTRUMENTO_SIN_MONEDA' AS "Codigo"
							from :nemo_sin_moneda
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------					
	nemos_ok =	select "IdInterno" 
				from :p_instrumentos p_ins
				except 
				select "IdInterno" from :nemos_repetidos_RV_FM
				except
				select "IdInterno" from :nemos_repetidos
				except
				select "IdInterno" from :nemo_sin_tipo_instrumento
				except
				select "IdInterno" from :nemo_sin_emisor
				except
				select "IdInterno" from :nemo_sin_moneda;
				
	v_instrumentos = SELECT p_ins."IdInterno","BSCL.Plataforma.BD.Secuencias::INSInstrumento".nextval as "IdInstrumento",
							p_ins."Nemotecnico",
							COALESCE("ISIN",'') 							AS "ISIN",
							COALESCE("CorteMinimo",0) 						AS "CorteMinimo",
							COALESCE("EstaInscrito",'N') 					AS "EstaInscrito",
							COALESCE("RazonSocial",'') 						AS "RazonSocial",
							COALESCE("FechaEmision",'0001-01-01') 			AS "FechaEmision",
							COALESCE("FechaVencimiento",'0001-01-01') 		AS "FechaVencimiento",
							"IdEmisor",
							"IdTipoReajuste",
							"IdMoneda",
							"IdTipoInstrumento",
							COALESCE("IdEstado",'INA') 						AS "IdEstado",
							COALESCE("Tamano",0) 							AS "Tamano",
							COALESCE("IdEntidadClasificadoraRiesgo",'') 	AS "IdEntidadClasificadoraRiesgo",
							COALESCE("ValorClasificacionRiesgo",'') 		AS "ValorClasificacionRiesgo",
							COALESCE("PrecioBase",0) 						AS "Precio",
							COALESCE("PrecioReferencia",0) 					AS "PrecioReferencia",
							COALESCE("FechaReferencia",'0001-01-01') 		AS "FechaReferencia",
							COALESCE("FrecuenciaCupon",0) 					AS "FrecuenciaCupon",
							COALESCE("NumeroCupones",0) 					AS "NumeroCupones",
							COALESCE("InstrumentoAcreditado",'N') 			AS "InstrumentoAcreditado",
							COALESCE("IdInstrumentoRelacionado",0) 			AS "IdInstrumentoRelacionado",
							COALESCE("EsPutEsCall",'') 						AS "EsPutEsCall",
							COALESCE("PrecioStrike",0) 						AS "PrecioStrike",
							COALESCE(p_ins."TipoBonoIRF",'') 				AS "TipoBonoIRF",
							COALESCE(p_ins."OrigenInstrumento",'') 			AS "OrigenInstrumento",
							COALESCE(p_ins."TipoRenta",'') 					AS "TipoRenta",
							COALESCE(p_ins."RelacionadoGrupo",'N') 			AS "RelacionadoGrupo",
							COALESCE(p_ins."FormadorMercado",'N') 			AS "FormadorMercado",
							COALESCE(p_ins."PactoAndino",'N') 				AS "PactoAndino", 
							COALESCE(p_ins."FactorExposicion",0) 			AS "FactorExposicion",
							COALESCE(p_ins."LargoPlazo",'N') 				AS "LargoPlazo",
							COALESCE("Tasa",0) 								AS "Tasa",
							COALESCE("AfectoArt107",'N')					AS "AfectoArt107",
							COALESCE("AfectoArt104",'N')					AS "AfectoArt104",
							COALESCE("DividendoOtraMoneda",'N')				AS "DividendoOtraMoneda",				
							COALESCE(p_ins."EsAutorizado",'N')				AS "EsAutorizado",
							COALESCE(p_ins."CobroComisionesCustodia", 'S')	AS "CobroComisionesCustodia",
							COALESCE(p_ins."IdInstrumentoRaiz",0)			AS "IdInstrumentoRaiz" , 
							COALESCE(p_ins."IdCondicionLiquidacion",'')		AS "IdCondicionLiquidacion",
							COALESCE(p_ins."EsInstrumentoCalificado", 'N')	AS "EsInstrumentoCalificado"
							FROM :p_instrumentos p_ins 
							INNER JOIN :nemos_ok nemo on p_ins."IdInterno" = nemo."IdInterno";
	
	INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" ("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","RazonSocial","FechaEmision",
	"FechaVencimiento","IdEmisor","IdTipoReajuste","IdMoneda","IdTipoInstrumento","IdEstado","Tamano",
	"Precio","PrecioReferencia","FechaReferencia","FrecuenciaCupon","NumeroCupones","InstrumentoAcreditado","ValorizaPor","IdInstrumentoRelacionado","EsPutEsCall",
	"PrecioStrike","TipoBonoIRF", "OrigenInstrumento", "TipoRenta", "RelacionadoGrupo", "FormadorMercado", "PactoAndino", "FactorExposicion", "LargoPlazo", "Tasa", "AfectoArt107", "AfectoArt104", "DividendoOtraMoneda","EsAutorizado", "CobroComisionesCustodia", "IdInstrumentoRaiz", "IdCondicionLiquidacion", "EsInstrumentoCalificado")
	SELECT ins."IdInstrumento",ins."Nemotecnico",ins."ISIN",ins."CorteMinimo",ins."EstaInscrito",ins."RazonSocial",ins."FechaEmision",
	ins."FechaVencimiento",ins."IdEmisor",ins."IdTipoReajuste",ins."IdMoneda",ins."IdTipoInstrumento",ins."IdEstado",ins."Tamano",
	ins."Precio",ins."PrecioReferencia",ins."FechaReferencia",ins."FrecuenciaCupon",ins."NumeroCupones","InstrumentoAcreditado",'T' AS "ValorizaPor", ins."IdInstrumentoRelacionado",
	ins."EsPutEsCall", ins."PrecioStrike",ins."TipoBonoIRF", ins."OrigenInstrumento", ins."TipoRenta", ins."RelacionadoGrupo", ins."FormadorMercado", ins."PactoAndino", ins."FactorExposicion", ins."LargoPlazo", "Tasa", ins."AfectoArt107", 
	ins."AfectoArt104", ins."DividendoOtraMoneda", ins."EsAutorizado", ins."CobroComisionesCustodia", ins."IdInstrumentoRaiz", ins."IdCondicionLiquidacion", ins."EsInstrumentoCalificado"
	FROM :v_instrumentos ins
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tip ON ins."IdTipoInstrumento" = tip."IdTipoInstrumento";
	
	UPDATE ff
	SET "ValorizaPor" = 'P'
	FROM "BSCL.Plataforma.BD::INS.Instrumento" ff
	INNER JOIN :v_instrumentos inst ON ff."IdInstrumento" = inst."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tip ON inst."IdTipoInstrumento" = tip."IdTipoInstrumento"
	WHERE tip."IdClasificacionTipoInstrumento" IN ('RV','FM','DV');
	
	
	INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"("IdInstrumento","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo")
	SELECT ins."IdInstrumento",val."IdEntidadClasificadoraRiesgo",val."ValorClasificacionRiesgo" 
	FROM :v_instrumentos ins 
	INNER JOIN :p_ValoresRiesgo val on ins."IdInterno" = val."IdInterno";
	
	SELECT MAX("IdInstrumentoSubyacente") + 1 INTO v_idInstrumentoSubyacente
	FROM "BSCL.Plataforma.BD::INS.InstrumentoSubyacente";
	
	INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"
	("IdInstrumentoSubyacente", "IdTipoInstrumento", "IdBolsa", "IdTipoInstrumentoSubyacente", "IdInstrumento")
	SELECT :v_idInstrumentoSubyacente,'RRV','BVL','ACN',ins."IdInstrumento"
	FROM :v_instrumentos ins
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tip ON ins."IdTipoInstrumento" = tip."IdTipoInstrumento"
	WHERE tip."IdClasificacionTipoInstrumento" = 'RV' AND :v_Locale = 'PE';
	
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento"
	("IdBolsaInstrumento", "IdBolsa","IdInstrumento")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL AS "IdBolsaInstrumento",bol."IdBolsa",ins."IdInstrumento"
	FROM :v_instrumentos ins
	INNER JOIN :p_Bolsas bol ON ins."IdInterno" = bol."IdInterno";
	
	v_tablaMensajes = select "IdInterno","Codigo",'' as "CodigoDato1", '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from :v_out_ErroresDetalle;
	
	CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_tablaMensajes,v_out_TablaMensajes);
	
	p_out_ErroresDetalle = select det."IdInterno",det."Codigo",outt."Descripcion" from :v_out_ErroresDetalle det INNER JOIN :v_out_TablaMensajes outt on outt."IdInterno" = det."IdInterno" and outt."Codigo" = det."Codigo";
	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
	
	p_out_Resultado = select "IdInterno","IdInstrumento","Nemotecnico" from :v_instrumentos;
	
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
	DECLARE v_fechaHoraSistema NVARCHAR(40);
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
	p_IdEmpresa => p_IdEmpresa,
	p_out_FechaHoraSistema => v_fechaHoraSistema
	);
	SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;
	p_Log =
	Select
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "IdTipoLog"
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
	,'INGRESAR_INSTRUMENTO' as "IdTipoRegistro"
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
	,COALESCE (entrada."Nemotecnico", '') as "Nemo"
	,0 as "IdProceso"
	,'' as "EstadoProceso"
	,'Crear Instrumento Plataforma' as "NombreProceso"
	FROM :p_Instrumentos as entrada; 
	p_LogDetalle =
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'Autorizado' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog1."EsAutorizado", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog1
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'TipoInstrumento' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog2."IdTipoInstrumento", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog2
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'ISIN' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog3."ISIN", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog3
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'Emisor' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog4."IdEmisor", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog4
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'Inscrito' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog5."EstaInscrito", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog5
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'Acog.Art.107' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog6."AfectoArt107", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog6
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'Acog.Art.104' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog7."AfectoArt104", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog7
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'CobroComisionesCustodia' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog8."CobroComisionesCustodia", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog8
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'FechaEmision' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog9."FechaEmision", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog9
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'FechaVencimiento' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog10."FechaVencimiento", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog10
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'TipoReajuste' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog11."IdTipoReajuste", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog11
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'CorteMinimo' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(TO_VARCHAR(detlog12."CorteMinimo"), '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog12
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'PrecioBase' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(TO_VARCHAR(detlog13."PrecioBase"), '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog13
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'TasaTir' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(TO_VARCHAR(detlog14."Tasa"), '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog14
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'TamContrato' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(TO_VARCHAR(detlog15."Tamano"), '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog15
	UNION ALL
	SELECT  
	p_IdLog as "IdCabeceraLog"
	,'INGRESO' as "SubRegistro"
	,'EsInstrumentoCalificado' as "CampoRegistro"
	,'' as "ValorAntiguo"
	,COALESCE(detlog16."EsInstrumentoCalificado", '') as "ValorNuevo"
	FROM :p_Instrumentos as detlog16;
	CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (
	p_Log   => :p_Log,
	p_LogDetalle => :p_LogDetalle
	);
	END;		
	
END;
GO
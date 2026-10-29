SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoAnalitica" AS
SELECT INS."IdInstrumento" 
	,CAST(INS."Nemotecnico" AS NVARCHAR(40)) AS "Nemotecnico"
	,CAST(INS."ISIN" AS NVARCHAR (40)) AS "ISIN" 
	,INS."CorteMinimo"
	,INS."EstaInscrito"
	,INS."EsDivisible"
	,INS."RazonSocial"  as "RazonSocial"
	,TO_CHAR(IFNULL(INS."FechaEmision",'0001-01-01')) as "FechaEmision"
	,TO_CHAR(IFNULL(INS."FechaVencimiento",'0001-01-01')) as "FechaVencimiento"
	,CASE WHEN INS."IdInstrumentoRelacionado" = 0 THEN NULL ELSE INS."IdInstrumentoRelacionado" END AS "IdInstrumentoRelacionado" 
	,CASE WHEN INS."IdEmisor" = '' THEN NULL ELSE CAST(INS."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor" 
	,CASE WHEN COALESCE (INS."IdTipoReajuste", '')= '' THEN 'CLP' ELSE INS."IdTipoReajuste" END AS "IdTipoReajuste"
	,INS."Reajustable"
	,CASE WHEN INS."IdMoneda" = '' THEN 'CLP' ELSE INS."IdMoneda" END AS "IdMoneda"
	,CASE WHEN INS."IdTipoInstrumento" = '' THEN NULL ELSE INS."IdTipoInstrumento" END AS "IdTipoInstrumento" 
	,CASE WHEN INS."IdEstado" = '' THEN NULL ELSE INS."IdEstado" END AS "IdEstado" 
	,TO_BIGINT(INS."DecimalesPrecio") AS "DecimalesPrecio"
	,INS."Tamano"
	  ,TO_CHAR(IFNULL(INS."FechaInicioVigencia",'0001-01-01')) as "FechaInicioVigencia"
	,INS."TasaFiscal"
	,INS."IndArt104"
	,coalesce(CAST(INS."TipoValorizacion" AS NVARCHAR(10)),'') AS "TipoValorizacion"
	,COALESCE (INS."Tasa",0) AS "Tasa"
	,INS."Precio"
	,INS."ValorizaPor"
	,INS."TieneCupon"
	,CASE WHEN INS."IdPaisEmisor" = 0 THEN NULL ELSE INS."IdPaisEmisor" END AS "IdPaisEmisor" 
	,INS."IndicadorLiquidaDCV"
	,CAST(INS."Serie" AS NVARCHAR(40)) AS "Serie"
	,INS."IndicadorBolson"
	,INS."FrecuenciaCupon"
	,INS."PrecioReferencia"
	,TO_CHAR(IFNULL("FechaReferencia",'0001-01-01')) as "FechaReferencia"
	,TO_BIGINT(INS."NumeroCupones") AS "NumeroCupones"
	,CASE WHEN INS."CodigoRubro" = 0 THEN NULL ELSE INS."CodigoRubro" END AS "CodigoRubro"
	,CAST(COALESCE(RUB."Descripcion",'') AS NVARCHAR(500)) as "NombreCodigoRubro"
	,CAST(COALESCE(CLA."ValorClasificacionRiesgo",'') AS NVARCHAR(20)) AS "ClasificacionRiesgoInstrumento"
	,INS."PresenciaBursatilAjustada"
	, TIPO."IdClasificacionTipoInstrumento"
	,COALESCE (INS."IdInstrumentoRaiz",0) AS "IdInstrumentoRaiz"
	,CAST(INS."IdCondicionLiquidacion" AS NVARCHAR(2)) AS "IdCondicionLiquidacion"
 	             from "BSCL.Plataforma.BD::INS.Instrumento" as INS
 	             left join "BSCL.Plataforma.BD::INS.Rubro" as  RUB
   on RUB."CodigoRubro" = INS."CodigoRubro"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" CLA 
ON (INS."IdInstrumento" = CLA."IdInstrumento" AND CLA."IdEntidadClasificadoraRiesgo" ='BCS')
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPO
ON TIPO."IdTipoInstrumento" = INS."IdTipoInstrumento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
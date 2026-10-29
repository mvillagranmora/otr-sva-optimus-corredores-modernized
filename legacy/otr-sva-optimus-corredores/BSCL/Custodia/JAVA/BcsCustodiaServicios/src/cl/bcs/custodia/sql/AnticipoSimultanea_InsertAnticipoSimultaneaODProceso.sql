INSERT INTO "BSCL.Custodia.BD::GBL.AnticipoSimultanea_Proceso_Simultaneas"
(
	"EsSimultaneaEnviada",
	"IdCartera",
	"FolioCartera",
	"FolioCarteraOD",
	"IdCliente",
	"IdPortafolio",
	"IdInstrumento",
	"IdTipoProducto",
	"IdMonedaSistema",
	"IdBoveda",
	"IdBolsa",
	"FechaOperacion",
	"FolioExternoReferencia",
	"IdConcepto",
	"IdTipoOperacion",
	"IndicadorOrdenDirecta"
	--"CodigoMensaje","TipoMensaje","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
)
SELECT
	'N',  -- "EsSimultaneaEnviada"
	CAR."IdCartera",
	CAR."FolioCartera",
	PRO."FolioCartera",
	CAR."IdCliente",
	CAR."IdPortafolio",
	CAR."IdInstrumento",
	CAR."IdTipoProducto",
	CAR."IdMonedaSistema",
	CAR."IdBoveda",
	CAR."IdBolsa",
	CAR."FechaOperacion",
	CAR."FolioExternoReferencia",			
	CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN 'ANTSIC' ELSE 'ANTSIV' END, -- "IdConcepto"
	CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN 'ANSIMC' ELSE 'ANSIMV' END,	-- "IdTipoOperacion"
	'OD'    -- "IndicadorOrdenDirecta"
	-------'ANTICIPO_SIMULTANEA_OD','I','FolioCartera',s."FolioCartera",'FolioCarteraOD',c."FolioCartera"
FROM "BSCL.Custodia.BD::GBL.AnticipoSimultanea_Proceso_Simultaneas" AS PRO
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON       (CAR."FolioExternoReferencia" = PRO."FolioExternoReferencia"
														AND CAR."FechaOperacion" = PRO."FechaOperacion"
														AND CAR."IdModulo"       = 'FAC'
														AND CAR."IdEstado"       = 'VIG')
WHERE PRO."IndicadorOrdenDirecta" = 'OD';

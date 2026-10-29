		
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
	'S', -- "EsSimultaneaEnviada"
	"IdCartera",
	"FolioCartera",
	"FolioCartera",
	"IdCliente",
	"IdPortafolio",
	"IdInstrumento",
	"IdTipoProducto",
	"IdMonedaSistema",
	"IdBoveda",
	"IdBolsa",
	"FechaOperacion",
	"FolioExternoReferencia",
	CASE WHEN "IdTipoOperacion" = 'COMSIM' THEN 'ANTSIC' ELSE 'ANTSIV' END, -- "IdConcepto"
	CASE WHEN "IdTipoOperacion" = 'COMSIM' THEN 'ANSIMC' ELSE 'ANSIMV' END, -- "IdTipoOperacion"
	CASE WHEN "FolioExternoReferencia" != '' THEN 'OD' ELSE '' END          -- "IndicadorOrdenDirecta"
	
	--'ANTICIPO_SIMULTANEA_FOLIO', -- "CodigoMensaje"
	--'I', -- "TipoMensaje"
	--'FolioCartera', -- "CodigoDato1"
	--"FolioCartera", -- "ValorDato1"
	--NULL, -- "CodigoDato2"
	--NULL  -- "ValorDato2"
			
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "FolioCartera" = :FolioCartera
AND "IdEstado" = 'VIG';
		
		  
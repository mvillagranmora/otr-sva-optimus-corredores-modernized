SELECT
	DET."FolioCartola"
   ,DET. "FolioLineaCartola"
   ,DET."IdCargaMasiva"
FROM "BSCL.APV.BD::APV.CargaMasivaDetalleRecaudacion" AS DET
INNER JOIN "#CargaMasivaRecaudacion_CargaArchivo" CAR ON CAR."FolioCartola" = DET."FolioCartola" AND CAR."FolioLineaCartola" = DET."FolioLineaCartola"

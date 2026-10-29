SELECT 
	"IdPrestamosPendientes",
	"Nemotecnico",
	"Mercado",
	"DocumentoIdentidad",
	"TipoDocIdentidad",
	"Cantidad",
	"Tasa",
	"CondicionLiquidacion",
 	"FechaLiquidacion",
 	"CodigoCorredora",
 	"FolioSistemaExterno",
 	"Origen",
 	"Mensaje",
 	"Estado"		
FROM "BSCL.Custodia.BD::CUS.PrestamosPendientes" 
WHERE "FolioSistemaExterno" IN (:IdFolioSistemaExterno);
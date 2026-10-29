SELECT "IdInstrumentoPrestamo","CobraPrimaAlInicio", "IdEmisor", 
	"TipoPrestamoSistemaExterno",
	"IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo"
WHERE "IdTipoPrestamo" = 'PRTVTC' OR "IdTipoPrestamo" = 'PRVCRF';

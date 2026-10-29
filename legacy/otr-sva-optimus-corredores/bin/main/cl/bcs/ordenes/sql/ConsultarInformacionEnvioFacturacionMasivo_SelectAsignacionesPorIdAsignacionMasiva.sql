SELECT  
    "IdTransaccion","IdOrden","IdAsignacion","FolioTransaccion","CantidadAsignada","ValorRescateAsignado",
	"IdClasificacionTipoInstrumento",
	"IdTipoInstrumento","AccionOrden","IdBolsa","IdMonedaInstrumento","IdMonedaOperacion","NombreLibroOrdenes","Instrumento","EsAPlazo",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsAPlazo") as "EsAPlazoUI", "IdAsignacionMasiva"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ASIG
WHERE "IdAsignacionMasiva" = :p_IdAsignacionMasiva
AND "IdTipoInstrumento" NOT IN ('RRV','RRF')
AND "IdClasificacionTipoInstrumento" IN ('RV','RF','DV')
AND "IdEstadoAsignacion" = 'NEF';
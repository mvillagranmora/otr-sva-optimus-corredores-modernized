SELECT
	"CodigoRangoSeleccion"
	,"Descripcion"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarRangoNivelVenta"
WHERE "CodigoRangoSeleccion" in (:idsRangonivelVenta);

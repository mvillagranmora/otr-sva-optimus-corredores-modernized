SELECT
	"CodigoRangoSeleccion"
	,"Descripcion"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarRangoRenta"
WHERE "CodigoRangoSeleccion" in (:idsRangoRenta);

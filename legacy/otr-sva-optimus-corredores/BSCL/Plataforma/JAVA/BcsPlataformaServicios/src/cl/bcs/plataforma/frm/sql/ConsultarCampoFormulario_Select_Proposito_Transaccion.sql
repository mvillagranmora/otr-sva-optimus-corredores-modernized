SELECT
	"CodigoRangoSeleccion"
	,"Descripcion"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoTransaccion"
WHERE "CodigoRangoSeleccion" in (:idsPropositoTransaccion);

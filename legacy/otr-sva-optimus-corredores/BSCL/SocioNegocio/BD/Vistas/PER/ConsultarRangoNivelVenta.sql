SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarRangoNivelVenta";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarRangoNivelVenta" AS 
SELECT
	"CodigoRangoSeleccion" , "Descripcion" , "Valor1" , "Valor2" 
FROM
	"BSCL.SocioNegocio.BD::PER.RangoSeleccion" WHERE "CampoSeleccion" =  'RANGO_NIVEL_VENTA'
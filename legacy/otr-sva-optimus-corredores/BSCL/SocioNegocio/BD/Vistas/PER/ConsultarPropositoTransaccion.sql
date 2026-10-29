SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoTransaccion";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoTransaccion" AS 
SELECT
	"CodigoRangoSeleccion" , "Descripcion" 
FROM
	"BSCL.SocioNegocio.BD::PER.RangoSeleccion" WHERE "CampoSeleccion" =  'PROPOSITO_TRANSACCION'
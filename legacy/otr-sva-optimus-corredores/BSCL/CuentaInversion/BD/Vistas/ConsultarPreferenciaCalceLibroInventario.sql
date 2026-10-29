SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPreferenciaCalceLibroInventario";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPreferenciaCalceLibroInventario" AS 	

select 
	"IdPreferenciaCalceLibroInventario",
	"IdConcepto",
	"EsPagadoCon",
	"Preferencia",
	"IdEstado"
from "BSCL.CuentaInversion.BD::CTA.PreferenciaCalceLibroInventario"
WITH READ ONLY;	 	

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarPreferenciaCalceLibroInventario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  

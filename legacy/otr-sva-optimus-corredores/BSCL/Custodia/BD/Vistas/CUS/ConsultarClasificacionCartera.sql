SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" AS

Select 
	"IdClasificacionCartera",
	"CodigoCarteraIFRS",
	"CarteraIFRS",
	"CodigoCarteraCorredor",
	"CarteraCorredor",
	"CodigoCarteraClasificacion",
	"CarteraClasificacion",
	"IdEstado"
from "BSCL.Custodia.BD::CUS.ClasificacionCartera"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

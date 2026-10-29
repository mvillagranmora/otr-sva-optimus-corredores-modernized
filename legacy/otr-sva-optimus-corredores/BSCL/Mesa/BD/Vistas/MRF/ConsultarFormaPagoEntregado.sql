SET SCHEMA "BSCL";
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarFormaPagoEntregado";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarFormaPagoEntregado" AS

Select 
		0 as "IdFormaPagoEntregado",
		'' as "FormaPago",
		'' as "FechaPago",
		'' as "Banco",
		0 as "Monto"
from DUMMY
Where 1=2
WITH READ ONLY;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarFormaPagoEntregado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

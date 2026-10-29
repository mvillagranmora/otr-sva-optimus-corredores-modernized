SET SCHEMA "BSCL";
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarFormaPagoRecibido";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarFormaPagoRecibido" AS

Select 
		0 as "IdFormaPagoRecibido",
		'' as "FormaPago",
		'' as "FechaPago",
		'' as "Banco",
		0 as "Monto"
from DUMMY
Where 1=2
WITH READ ONLY;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarFormaPagoRecibido" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

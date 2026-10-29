SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaLite";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaLite" AS 

SELECT A.*,
		cli."DocumentoIdentidad", B."Nombre" as "NombreConcepto"

FROM "BSCL.Custodia.BD::GTA.Garantia" as A
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = A."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" B ON B."IdConcepto" = A."IdConcepto"
WITH READ ONLY;	 	

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaLite" TO BCS_DESARROLLADOR WITH GRANT OPTION;

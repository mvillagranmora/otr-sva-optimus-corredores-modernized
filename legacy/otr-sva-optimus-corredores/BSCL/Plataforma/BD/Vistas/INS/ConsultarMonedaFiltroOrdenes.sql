SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarMonedaFiltroOrdenes";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarMonedaFiltroOrdenes" AS

  SELECT moneda."IdMoneda"
  FROM "BSCL.Plataforma.BD::PLA.Moneda" moneda
  WHERE moneda."PermiteFiltroOrdenes" = 'S';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarMonedaFiltroOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL";

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarMonedaFiltroOrden";

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarMonedaFiltroOrden" AS

  SELECT moneda."IdMoneda"
        ,moneda."Nombre"
  FROM "BSCL.Plataforma.BD::PLA.Moneda" moneda
  WHERE moneda."PermiteFiltroOrdenes" = 'S';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarMonedaFiltroOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
SET SCHEMA "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaItemConfiguracion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaItemConfiguracion" AS

SELECT 
    ItemConfiguracion."IdConfiguracionItem",
    ItemConfiguracion."IdProcesoFuncion",
    ProcesoFuncion."Nombre"                     AS "NombreProcesoFuncion",
    ItemConfiguracion."IdItem",
    Item."IdTipoItem",
    ItemConfiguracion."Condicion",
    ItemConfiguracion."CuentaContable",
    ItemConfiguracion."CuentaOptimus",
    CuentaContable."NombreCuenta",
    ItemConfiguracion."Factor", 
    ItemConfiguracion."Monto",  
    ItemConfiguracion."SaldoDebeHaber",     
    ItemConfiguracion."Operacion",  
    ItemConfiguracion."MayorACero",
    ItemConfiguracion."MenorACero",
    ItemConfiguracion."PermiteCero",
    ItemConfiguracion."TipoItem"
FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" AS ItemConfiguracion
LEFT OUTER JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasContables" AS CuentaContable
             ON CuentaContable."Cuenta" = ItemConfiguracion."CuentaContable"
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaProcesoFuncion" AS ProcesoFuncion
             ON ProcesoFuncion."IdProcesoFuncion" = ItemConfiguracion."IdProcesoFuncion"
LEFT JOIN "BSCL.Normativa.BD::ILS.Item" AS Item
             ON Item."IdItem" = ItemConfiguracion."IdItem"
			 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaItemConfiguracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

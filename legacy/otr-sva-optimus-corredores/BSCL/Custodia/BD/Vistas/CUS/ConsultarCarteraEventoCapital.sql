SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital" 
AS 
  SELECT
      CAR."IdCartera"                AS "IdCartera"
    , CAR."FolioCartera"              AS "FolioCartera"
    , CAR."IdEmpresa"                AS "IdEmpresa"
    , CAR."FechaOperacion"              AS "FechaOperacion"
    , CAR."FechaVencimientoOperacion"        AS "FechaVencimientoOperacion"
    , CAR."FechaLiquidacion"            AS "FechaLiquidacion"
    , CAR."IdCliente"                AS "IdCliente"
    , CAR."IdPortafolio"              AS "IdPortafolio"
    ,CASE 
      WHEN CAR."IdTipoInstrumento" = 'SIM' or CAR."IdTipoInstrumento" = 'PRT' or CAR."IdTipoInstrumento" = 'EMP' then
        SUB."Nemotecnico"
      ELSE CAR."Nemotecnico"
    END                       AS "Nemotecnico"
    , CASE CAR."IdTipoInstrumento"
        WHEN 'SIM' THEN
          CASE CAR."Nemotecnico"   
            WHEN 'SIMULTANEA VENTA'   THEN 'SIV'
            WHEN 'SIMULTANEA COMPRA'  THEN 'SIC'
            ELSE 'SIM'
          END
            
        
        WHEN 'PRT' THEN 'PRT'
        WHEN 'EMP' THEN 'EMP'
        ELSE 'CUS'
      END                      AS "TipoCartera"
    , CAR."IdTipoInstrumento"            AS "IdTipoInstrumento"
    , CAR."INGCantidad"                AS "Cantidad"
    , CAR."IdBoveda"                AS "IdBoveda"
    , CAR."IdBolsa"                  AS "IdBolsa"
    , CAR."IdConcepto"                AS "IdConcepto"
    , CAR."IdEstado"                AS "IdEstado"
    , CAR."PrecioCompra"              AS "PrecioCompra"
    , CAR."TipoCarteraNormativa"      AS "TipoCarteraNormativa"
  	, CAR."IdRegimenFondoMutuo"
  FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
  LEFT JOIN (
    SELECT "FolioCarteraRelacionada","Nemotecnico","NroFila" FROM (
      SELECT  "IdSubyacente","FolioCarteraRelacionada","Nemotecnico","IdEstado", row_number() over(partition by "FolioCarteraRelacionada" ORDER BY "IdSubyacente" ) as "NroFila" 
      FROM "BSCL.Custodia.BD::CUS.Subyacente" AS miSub WHERE "IdEstado" = 'VIG' 
    ) WHERE "NroFila" = 1
  ) AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
  WHERE "IdEstado" = 'VIG' 

UNION ALL

  SELECT
     A."IdGarantia"                   AS "IdCartera",
    A."FolioGarantia"                     AS "FolioCartera",
    A."IdEmpresa"                 AS "IdEmpresa",
    A."FechaOperacion"              AS "FechaOperacion",  
    A."FechaVencimientoOperacion"        AS "FechaVencimientoOperacion",  
    A."FechaLiquidacion"            AS "FechaLiquidacion",
    A."IdCliente"                 AS "IdCliente",
    A."IdPortafolio"               AS "IdPortafolio",
    A."Nemotecnico"                AS "Nemotecnico",
    'GAR'                    AS "TipoCartera",
    A."IdTipoInstrumento"            AS "IdTipoInstrumento",
    A."Cantidad"                 AS "Cantidad",
    A."IdBovedaOperacionOrigen"          AS "IdBoveda",
    A."IdBolsaOperacionOrigen"          AS "IdBolsa",
    A."IdConcepto"                 AS "IdConcepto",
    A."IdEstado"                AS "IdEstado",
    A."PrecioCompraOperacionOrigen"        AS "PrecioCompra",
    A."TipoCarteraNormativa" AS "TipoCarteraNormativa",
    A."IdRegimenFondoMutuo"
  FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" A
  WHERE A."IdEstado" = 'VIG'


WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

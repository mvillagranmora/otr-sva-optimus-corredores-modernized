SELECT
ROW_NUMBER() OVER (ORDER BY CAR."IdPortafolio") 					AS "IdGenerarMovimientoCustodia"
,'Cartera'															AS "TablaAfectada"
,ConCliente."IdCliente"                         					AS "IdCliente"
,CAR."IdPortafolio"	                  								AS "IdPortafolio"
,CAR."IdBolsa"                           							AS "IdBolsa"
,TO_DATE(:FechaPago,'YYYY-MM-DD')         							AS "FechaPago"
,Instrumento."IdClasificacionTipoInstrumento"       				AS "IdClasificacionTipoInstrumento"
,Instrumento."IdTipoInstrumento"                    				AS "IdTipoInstrumento"
,Instrumento."IdInstrumento"                          				AS "IdInstrumento"
,CAR."Nemotecnico"		                          					AS "Nemotecnico"
,Instrumento."IdEmisor"                        						AS "IdEmisor"
,Instrumento."IdTipoReajuste"                  						AS "IdTipoReajuste"
,Instrumento."FechaEmision"  	            						AS "FechaEmision"
,Instrumento."IdMoneda"    		              						AS "IdMoneda"
,CAR."IdBoveda"                           							AS "IdBoveda"
,CAR."Cantidad"     												AS "Cantidad"
,CAR."PrecioCompra"			                						AS "PrecioCompra"
,'Canjes N°:' ||Cast(:IdEventoCapital as nvarchar(8))|| ' ' || Trim(CAR."Nemotecnico")  || ' Fecha: ' ||TO_VARCHAR(:FechaPago,'YYYY-MM-DD')	AS "Glosa"
,CAR."FolioCartera" AS "FolioCustodiaBusqueda"
,CAR."IdCartera"	AS "IdCartera"
FROM (SELECT
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
  
  FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
  LEFT JOIN (
    SELECT "FolioCarteraRelacionada","Nemotecnico","NroFila" FROM (
      SELECT  "IdSubyacente","FolioCarteraRelacionada","Nemotecnico","IdEstado", row_number() over(partition by "FolioCarteraRelacionada" ORDER BY "IdSubyacente" ) as "NroFila" 
      FROM "BSCL.Custodia.BD::CUS.Subyacente" AS miSub WHERE "IdEstado" = 'VIG' 
    ) WHERE "NroFila" = 1
  ) AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
  WHERE "IdEstado" = 'VIG') AS CAR
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
	ON CAR."IdPortafolio" = ConCliente."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS Instrumento
	ON CAR."Nemotecnico" = Instrumento."Nemotecnico"
WHERE 
CAR."FechaOperacion" <= :FechaLimite
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."Cantidad" > 0
AND CAR."IdEstado" = 'VIG'
AND EXISTS(SELECT 1 FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVCLI WHERE EVCLI."IdPortafolio" = CAR."IdPortafolio" AND EVCLI."IdEventoCapital" = :IdEventoCapital AND EVCLI."IdInstrumento" = Instrumento."IdInstrumento")

UNION

SELECT
ROW_NUMBER() OVER (ORDER BY CAR."IdPortafolio") 					AS "IdGenerarMovimientoCustodia"
,'Negativa'															AS "TablaAfectada"
,ConCliente."IdCliente"                         					AS "IdCliente"
,CAR."IdPortafolio"	                  								AS "IdPortafolio"
,CAR."IdBolsa"                           							AS "IdBolsa"
,TO_DATE(:FechaPago,'YYYY-MM-DD')         							AS "FechaPago"
,Instrumento."IdClasificacionTipoInstrumento"       				AS "IdClasificacionTipoInstrumento"
,Instrumento."IdTipoInstrumento"                    				AS "IdTipoInstrumento"
,Instrumento."IdInstrumento"                          				AS "IdInstrumento"
,CAR."Nemotecnico"		                          					AS "Nemotecnico"
,Instrumento."IdEmisor"                        						AS "IdEmisor"
,Instrumento."IdTipoReajuste"                  						AS "IdTipoReajuste"
,Instrumento."FechaEmision"  	            						AS "FechaEmision"
,Instrumento."IdMoneda"    		              						AS "IdMoneda"
,CAR."IdBoveda"                           							AS "IdBoveda"
,CAR."Cantidad"*(-1)     											AS "Cantidad"
,CAR."PrecioCompra"			                						AS "PrecioCompra"
,'Canjes N°:' ||Cast(:IdEventoCapital as nvarchar(8))|| ' ' || Trim(CAR."Nemotecnico")  || ' Fecha: ' ||TO_VARCHAR(:FechaPago,'YYYY-MM-DD')	AS "Glosa"
,CAR."FolioCartera" AS "FolioCustodiaBusqueda"
,CAR."IdCartera"	AS "IdCartera"
FROM (SELECT
      CAR."IdCarteraNegativa"										AS "IdCartera"
    , CAR."FolioCustodia"											AS "FolioCartera"
    , CAR."IdEmpresa"												AS "IdEmpresa"
    , CAR."FechaOperacion"											AS "FechaOperacion"
    , CAR."FechaVencimientoOperacion"								AS "FechaVencimientoOperacion"
    , CAR."FechaLiquidacion"										AS "FechaLiquidacion"
    , CAR."IdCliente"												AS "IdCliente"
    , CAR."IdPortafolio"											AS "IdPortafolio"
    , CAR."Nemotecnico"												AS "Nemotecnico"
    , 'NEG'                    										AS "TipoCartera"
    , INS."IdTipoInstrumento"										AS "IdTipoInstrumento"
    , CAR."Cantidad"												AS "Cantidad"
    , CAR."IdBoveda"												AS "IdBoveda"
    , CAR."IdBolsa"													AS "IdBolsa"
    , CAR."IdConcepto"												AS "IdConcepto"
    , CAR."IdEstado"												AS "IdEstado"
    , CAR."PrecioOperacion"											AS "PrecioCompra"
  
	FROM "BSCL.Custodia.BD::CUS.CarteraNegativa"AS CAR
  	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON INS."IdInstrumento" = CAR."IdInstrumento" 
	WHERE CAR."IdEstado" = 'VIG') AS CAR
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente ON CAR."IdPortafolio" = ConCliente."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS Instrumento ON CAR."Nemotecnico" = Instrumento."Nemotecnico"
WHERE CAR."FechaOperacion" <= :FechaLimite
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."Cantidad" > 0
AND CAR."IdEstado" = 'VIG'
AND EXISTS(SELECT 1 FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVCLI WHERE EVCLI."IdPortafolio" = CAR."IdPortafolio" AND EVCLI."IdEventoCapital" = :IdEventoCapital AND EVCLI."IdInstrumento" = Instrumento."IdInstrumento");
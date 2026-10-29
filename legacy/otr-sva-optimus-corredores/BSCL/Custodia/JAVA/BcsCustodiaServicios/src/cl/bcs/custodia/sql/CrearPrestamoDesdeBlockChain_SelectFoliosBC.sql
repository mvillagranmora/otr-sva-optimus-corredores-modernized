SELECT TEM."FolioSistemaExterno"
       ,COALESCE(CAR."FolioCartera",0) AS "FolioPrestamo"
FROM #ProcesaLogBlockChain_P_TABLAFOLIOSBC TEM
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."FolioSistemaExterno" = TEM."FolioSistemaExterno"



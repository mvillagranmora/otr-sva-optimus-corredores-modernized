
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdParametroFinanciero" AS varchar(200)) || ':' || cast("Fecha" AS varchar(200)),'Plataforma_ParametroFinanciero', 'I', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ParametroFinanciero"
WHERE "Fecha" = '2014-04-26';

select * from "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinancieroAnalitica" bpbv
where "IdParametroFinanciero" = 'UF'

select * from "BSCL.Plataforma.BD::BAT.Programa" bpbbp 
where "IdTipoPrograma" = 'GENERAR_INFORME_PATRIMONIO_DIARIO'




SELECT
"IdClasificacionTipoInstrumento"
   ,"IdTipoInstrumento"
   ,"IdInstrumento"
   ,"Nemotecnico"
   ,"IdEmisor"
   ,"EsFungible"
   ,"IdCliente"
   ,"IdPortafolio"
   ,"Producto"
   ,"IdBoveda"
   ,"IdCuentaCustodioPortafolio"
   ,"NumeroCuentaBoveda"
   ,"Cantidad"
   ,"PrecioCompra"
   ,"ValorCompra"
   ,"MontoCompra"
   ,"INGPrecioMercadoActual"
   ,"INGTasaMercadoActual"
   ,"ValorMercado"
   ,"MontoMercado"
   ,"IdMonedaInstrReajuste"
   ,"IdMonedaEmision"
   ,"IdMonedaPrincipal"
   ,"InteresCorrido"
   ,"IdTipoOperacion"
   ,"AccionCustodia"
   ,"IdMatrizAgrupador"
   ,"EsADC"
   ,"IdPersona"
   ,"ValorFactorMonedaInstrumento"
   ,"RazonSocialInstrumento"
   , "EsCustodiaExterna"
FROM
(
SELECT
        SUB."IdClasificacionTipoInstrumento"
       ,SUB."IdTipoInstrumento"
       ,SUB."IdInstrumento"
       ,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" ELSE SUB."Nemotecnico" END AS "Nemotecnico"
       ,SUB."IdEmisor"
       ,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."EsFungible" else 'N' END AS "EsFungible"
       ,CAR."IdCliente"
       ,CAR."IdPortafolio"
       ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
       ,CASE WHEN 'S'='N' THEN 1 ELSE CAR_SUB."IdBoveda" END AS "IdBoveda" 
       ,CASE WHEN 'S'='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
       ,CASE WHEN 'S'='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"             
       ,CAST(IFNULL(CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END, 0.0) AS DECIMAL(26,8)) AS "Cantidad"
       ,COALESCE(CAR_SUB."PrecioCompra",0.0) AS "PrecioCompra"                            
       ,COALESCE(SUB."INGMontoATasaCompraActual",0.0) AS "MontoCompra"                   
       ,COALESCE(SUB."INGMontoSubyacenteActualMonedaInstrumento",0.0) AS "ValorCompra"  
       ,COALESCE(SUB."INGPrecioMercadoActual",0.0) AS "INGPrecioMercadoActual"           
       ,COALESCE(SUB."INGTasaMercadoActual",0.0) AS "INGTasaMercadoActual"               
       ,COALESCE(SUB."INGMontoMercadoActualMonedaInstrumento",0.0) AS "ValorMercado"
       ,COALESCE(SUB."INGMontoATasaMercadoActual",0.0) AS "MontoMercado"
       ,CASE
            WHEN IFNULL(SUB."IdMonedaInstrumento",'') <> '' THEN SUB."IdMonedaInstrumento"
            WHEN IFNULL(SUB."IdTipoReajuste",'') <> '' THEN SUB."IdTipoReajuste"
            ELSE IFNULL(CAR."IdMonedaSistema",'')
        END AS "IdMonedaInstrReajuste"
       ,CASE
            WHEN IFNULL(SUB."IdMonedaEmision",'') <> '' THEN IFNULL(SUB."IdMonedaEmision",'') 
            WHEN IFNULL(SUB."IdMonedaInstrumento",'') <> '' THEN SUB."IdMonedaInstrumento"
            WHEN IFNULL(SUB."IdTipoReajuste",'') <> '' THEN SUB."IdTipoReajuste"
            ELSE IFNULL(CAR."IdMonedaSistema",'')
        END AS "IdMonedaEmision"
       ,CAR."IdMonedaSistema" as "IdMonedaPrincipal"
       ,CAR."INGMontoInteresCorridoCompraActual" AS "InteresCorrido"
       ,CAR."IdTipoOperacion"
       ,TipOpe."Accion" AS "AccionCustodia"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
       ,CLI."IdPersona"
       ,CAR_SUB."ValorFactorMonedaInstrumento"
       ,inst3."RazonSocial" AS "RazonSocialInstrumento"
       ,CAR."EsCustodiaExterna" AS "EsCustodiaExterna"
    FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
    INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG'
    INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR_SUB ON SUB."IdCarteraRetirada" = CAR_SUB."IdCartera"
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = 1 AND CLI."IdCliente" = CAR."IdCliente")
    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = CAR."IdTipoOperacion")
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
    WHERE
        CAR."IdEmpresa" = 1
    AND CAR."IdEstado" = 'VIG'
    AND CAR."IdTipoOperacion" IN ('VENSIM','EMPRES')
    AND CAR."IdEstado" NOT IN ('ANU', 'VCD','ANT')
-- AND CAR."INGFechaOperacion" <= :v_FechaConsulta
 AND CAR."IdCliente" = 190058
 AND CAR."IdPortafolio" = 247348
    )
SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" AS
(((SELECT DISTINCT
   cPag."IdComprobantePagoOperacion"
, cPag."IdEmpresa"
, cPag."IdOperacion"
, cPag."IdIngresoEgreso"
, cPag."IdAbono"
, cPag."IdCargo"
, cPag."IdTipoProducto" 
, cPag."IdFormaDePago"
, cPag."RecibePaga"
, DICCIONARIO."Descripcion" AS "RecibePagaDesc"
, cPag."IdModalidadPago"
, cPag."IdMoneda" AS "IdMonedaComprobantePago"
, cPag."Monto"
, cPag."FechaIngresoComprobantePagoOperacion"
, cPag."FechaLiquidacionComprobantePagoOperacion"
, cPag."IdEstado" AS "IdEstadoComprobantePago"
, cPag."AfectarTesoreria"
, MP."Nombre" AS "NombreFormaDePago"
, NULL AS "Dias"
, fPag."PorDefecto"
, fPag."RecibePagaPortafolio"
, fPag."RecibePagaPortafolioDesc"
, fPag."IdPreferenciaPago"
, fPag."IdTipoOperacion"
, fPag."IdMedioDePago"
, fPag."IdMoneda"
, fPag."UsaPersonaRelacionada"
, fPag."FechaCreacion"
, fPag."FechaModificacion"
, fPag."IdUsuarioCreador"
, fPag."IdEstado"
, fPag."NombreTipoOperacion"
, fPag."NombreTipoProducto"
, fPag."NombreMedioDePago"
, fPag."NombreMoneda"
, fPag."UsaCuentaBancaria"
, fPag."UsaTarjetaBancaria"
, fPag."NombreEstado"
, cPag."IdCuentaBanco" AS "IdCuentaBancaria"
, CBANC."IdBanco"
, CBANC."IdTipoCuentaBancaria"
, CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
        ELSE COALESCE(BANCBANC."NombreBanco",'')      
    END AS "NombreBancoCuentaBancaria"
, CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
        ELSE COALESCE(CBANC."NumeroCuentaBancaria",'')       
    END AS "NumeroCuentaBancaria"    
, COALESCE(PPP."IdPreferenciaPagoPortafolio",0) AS "IdPreferenciaPagoPortafolio"     
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" cPag
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" ope ON (cPag."IdOperacion"= ope."IdOperacion")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" fPag
    ON (cPag."IdFormaDePago" = fPag."IdPreferenciaPago" AND
        cPag."IdTipoProducto" = fPag."IdTipoProducto" AND
        cPag."IdMoneda"= fPag."IdMoneda" AND
        fPag."IdEstado"='ACT')
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF
    ON ("CodigoParametro"='PLA.IdiomaSistema' AND
        cPag."IdEmpresa" = CONF."IdEmpresa")
LEFT JOIN (
        SELECT
            "CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado"
            ,CASE WHEN DIC."CodDiccionario"='UI_RECIBIMOS' THEN 'R' WHEN DIC."CodDiccionario"='UI_ENTREGAMOS' THEN 'P' ELSE '' END AS "RecibePaga"
        FROM "BSCL.Plataforma.BD::PLA.Diccionario" DIC
        WHERE DIC."CodDiccionario" in ('UI_RECIBIMOS','UI_ENTREGAMOS')
        ) DICCIONARIO
    ON (cPag."RecibePaga"= DICCIONARIO."RecibePaga" AND
        CONF."Valor" = DICCIONARIO."CodLenguaje")
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP ON (ope."IdPortafolio"=PPP."IdPortafolio" AND fPag."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND fPag."IdEmpresa" = PPP."IdEmpresa" AND PPP."IdCuentaBancaria"=cPag."IdCuentaBanco" )
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON (PPP."IdPreferenciaPago" = PP."IdPreferenciaPago")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON (TIPOP."IdTipoOperacion" = PP."IdTipoOperacion")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON ope."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CBANCCOR ON PPP."IdCuentaBancaria" = CBANCCOR."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON (BANCBANC."IdBanco" = CBANC."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PP2  ON ( pp2."IdPreferenciaPago"= cPag."IdFormaDePago")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago"  AS MP ON  ( PP2."IdMedioDePago"= MP."IdMedioDePago")
WHERE cPag."IdTipoProducto" = 'MERF') UNION ALL (SELECT
   cPag."IdComprobantePagoOperacion"
, cPag."IdEmpresa"-- AS "IdEmpresaComprobantePagoOperacion"
, cPag."IdOperacion"
, cPag."IdIngresoEgreso"
, cPag."IdAbono"
, cPag."IdCargo"
, cPag."IdTipoProducto" --AS "IdTipoProductoComprobantePagoOperacion"
, cPag."IdFormaDePago"  --  AS "IdFormaDePago"
--, fPag."NombreFormaDePago"    AS "NombreFormaPago"
--, fPag."Dias"                 AS "DiasFormaPago"
--, fPag."IdPreferenciaPago"
--, fPag."IdMedioDePago"        AS "IdMedioPago"
--, fPag."NombreMedioDePago"    AS "NombreMedioPago"
, cPag."RecibePaga"
, DICCIONARIO."Descripcion" AS "RecibePagaDesc"
--, fPag."RecibePagaPortafolioDesc" AS "RecibePagaDesc"
, cPag."IdModalidadPago"
, cPag."IdMoneda" AS "IdMonedaComprobantePago"
--, cPag."IdMonedaLiquidacion"
, cPag."Monto"
--, cPag."MontoLiquidacion"
, cPag."FechaIngresoComprobantePagoOperacion"
, cPag."FechaLiquidacionComprobantePagoOperacion"
, cPag."IdEstado" AS "IdEstadoComprobantePago"
, cPag."AfectarTesoreria"
--, fPag."IdFormaDePago"
, fPag."NombreFormaDePago"--coalesce(UPPER(DICCIONARIO."Descripcion"), fPag."NombreFormaDePago") AS "NombreFormaDePago"
, fPag."Dias"
, fPag."PorDefecto"
, fPag."RecibePagaPortafolio"
, fPag."RecibePagaPortafolioDesc"--coalesce(UPPER(DICCIONARIO2."Descripcion"), fPag."RecibePagaPortafolioDesc") AS "RecibePagaPortafolioDesc"
, fPag."IdPreferenciaPago"
, fPag."IdTipoOperacion"
, fPag."IdMedioDePago"
, fPag."IdMoneda"
, fPag."UsaPersonaRelacionada"
, fPag."FechaCreacion"
, fPag."FechaModificacion"
, fPag."IdUsuarioCreador"
, fPag."IdEstado"
--, fPag."IdEmpresa"
, fPag."NombreTipoOperacion"
--, fPag."IdTipoProducto"
, fPag."NombreTipoProducto"
, fPag."NombreMedioDePago" --coalesce(UPPER(DICCIONARIO."Descripcion"), fPag."NombreMedioDePago") AS "NombreMedioDePago"
, fPag."NombreMoneda"
, fPag."UsaCuentaBancaria"
, fPag."UsaTarjetaBancaria"
, fPag."NombreEstado"
, PPP."IdCuentaBancaria"
, CBANC."IdBanco"
, CBANC."IdTipoCuentaBancaria"
, CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
        ELSE COALESCE(BANCBANC."NombreBanco",'') 
        /*(
            CASE TIPOP."RecibePagaPortafolio" 
                WHEN 'R' THEN COALESCE(BANCBANC."NombreBanco",'')
                WHEN 'P' THEN 
                    CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(BANCBANC."NombreBanco",'') ELSE COALESCE(CBANCCOR."NombreBanco",'') END                   
                ELSE '' 
            END)*/ 
    END AS "NombreBancoCuentaBancaria"
, CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
        ELSE COALESCE(CBANC."NumeroCuentaBancaria",'') 
        /*(
            CASE TIPOP."RecibePagaPortafolio" 
                WHEN 'R' THEN COALESCE(CBANC."NumeroCuentaBancaria",'')     
                WHEN 'P' THEN 
                    CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(CBANC."NumeroCuentaBancaria",'') ELSE COALESCE(CBANCCOR."NumeroCuentaBancaria",'') END            
                ELSE '' 
            END ) */
    END AS "NumeroCuentaBancaria"
, COALESCE(PPP."IdPreferenciaPagoPortafolio",0) AS "IdPreferenciaPagoPortafolio" 
FROM (
    SELECT
        cpo."IdComprobantePagoOperacion",
        cpo."IdEmpresa",
        cpo."IdOperacion",
        cpo."IdIngresoEgreso",
        cpo."IdAbono",
        cpo."IdCargo",
        cpo."IdTipoProducto",
        cpo."IdFormaDePago",
        cpo."RecibePaga",
        cpo."IdModalidadPago",
        cpo."IdMoneda",
        cpo."Monto",
        cpo."FechaIngresoComprobantePagoOperacion",
        cpo."FechaLiquidacionComprobantePagoOperacion",
        cpo."IdEstado",
        cpo."AfectarTesoreria",
        cpo."IdIngresoEgresoAnticipo",
        cpo."IdAbonoAnticipo",
        cpo."IdCargoAnticipo",
        cpo."IdCuentaBanco",
        ROW_NUMBER() OVER (
            PARTITION BY cpo."IdOperacion", cpo."RecibePaga"
            ORDER BY cpo."FechaIngresoComprobantePagoOperacion" DESC
        ) AS "ROWNUMBERS_ComprobantePago"
    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" cpo
) cPag
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" ope ON (cPag."IdOperacion"= ope."IdCartera" AND cPag."FechaIngresoComprobantePagoOperacion" = TO_DATE(ope."FechaOperacion"))
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" fPag
    ON (cPag."IdFormaDePago" = fPag."IdFormaDePago" AND
        cPag."IdTipoProducto" = fPag."IdTipoProducto" AND
        cPag."IdMoneda"= fPag."IdMoneda" AND
        fPag."IdEstado"='ACT')
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF
    ON ("CodigoParametro"='PLA.IdiomaSistema' AND
        cPag."IdEmpresa" = CONF."IdEmpresa")
LEFT JOIN (
        SELECT 
            "CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado"
            ,CASE WHEN DIC."CodDiccionario"='UI_RECIBIMOS' THEN 'R' WHEN DIC."CodDiccionario"='UI_ENTREGAMOS' THEN 'P' ELSE '' END AS "RecibePaga"
        FROM "BSCL.Plataforma.BD::PLA.Diccionario" DIC
        WHERE DIC."CodDiccionario" in ('UI_RECIBIMOS','UI_ENTREGAMOS')
        ) DICCIONARIO
    ON (cPag."RecibePaga"= DICCIONARIO."RecibePaga" AND
        CONF."Valor" = DICCIONARIO."CodLenguaje")
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP ON (ope."IdPortafolio"=PPP."IdPortafolio" AND fPag."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND fPag."IdEmpresa" = PPP."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON (PPP."IdPreferenciaPago" = PP."IdPreferenciaPago")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON (TIPOP."IdTipoOperacion" = PP."IdTipoOperacion")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON ope."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CBANCCOR ON PPP."IdCuentaBancaria" = CBANCCOR."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON (BANCBANC."IdBanco" = CBANC."IdBanco")
WHERE cPag."IdTipoProducto" = 'MEFW')) 

UNION ALL (SELECT 
   cPag."IdComprobantePagoOperacion"
, cPag."IdEmpresa"-- AS "IdEmpresaComprobantePagoOperacion"
, cPag."IdOperacion"
, cPag."IdIngresoEgreso"
, cPag."IdAbono"
, cPag."IdCargo"
, cPag."IdTipoProducto" --AS "IdTipoProductoComprobantePagoOperacion"
, cPag."IdFormaDePago"  --  AS "IdFormaDePago"
--, fPag."NombreFormaDePago"    AS "NombreFormaPago"
--, fPag."Dias"                 AS "DiasFormaPago"
--, fPag."IdPreferenciaPago"
--, fPag."IdMedioDePago"        AS "IdMedioPago"
--, fPag."NombreMedioDePago"    AS "NombreMedioPago"
, cPag."RecibePaga"
, DICCIONARIO."Descripcion" AS "RecibePagaDesc"
--, fPag."RecibePagaPortafolioDesc" AS "RecibePagaDesc"
, cPag."IdModalidadPago"
, cPag."IdMoneda" AS "IdMonedaComprobantePago"
--, cPag."IdMonedaLiquidacion"
, cPag."Monto"
--, cPag."MontoLiquidacion"
, cPag."FechaIngresoComprobantePagoOperacion"
, cPag."FechaLiquidacionComprobantePagoOperacion"
, cPag."IdEstado" AS "IdEstadoComprobantePago"
, cPag."AfectarTesoreria"
--, fPag."IdFormaDePago"
, fPag."NombreFormaDePago"--coalesce(UPPER(DICCIONARIO."Descripcion"), fPag."NombreFormaDePago") AS "NombreFormaDePago"
, fPag."Dias"
, fPag."PorDefecto"
, fPag."RecibePagaPortafolio"
, fPag."RecibePagaPortafolioDesc"--coalesce(UPPER(DICCIONARIO2."Descripcion"), fPag."RecibePagaPortafolioDesc") AS "RecibePagaPortafolioDesc"
, fPag."IdPreferenciaPago"
, fPag."IdTipoOperacion"
, fPag."IdMedioDePago"
, fPag."IdMoneda"
, fPag."UsaPersonaRelacionada"
, fPag."FechaCreacion"
, fPag."FechaModificacion"
, fPag."IdUsuarioCreador"
, fPag."IdEstado"
--, fPag."IdEmpresa"
, fPag."NombreTipoOperacion"
--, fPag."IdTipoProducto"
, fPag."NombreTipoProducto"
, fPag."NombreMedioDePago" --coalesce(UPPER(DICCIONARIO."Descripcion"), fPag."NombreMedioDePago") AS "NombreMedioDePago"
, fPag."NombreMoneda"
, fPag."UsaCuentaBancaria"
, fPag."UsaTarjetaBancaria"
, fPag."NombreEstado"
, CASE WHEN ope."IdTipoCanalIngreso" = 'FMM' AND cPag."RecibePaga" = 'P' THEN 
        CASE WHEN COALESCE(PPPSAR."IdCuentaBancaria",0) <> 0 THEN
             COALESCE(PPPSAR."IdCuentaBancaria",0) 
        ELSE 0 END
  ELSE CASE WHEN cPag."RecibePaga" = 'P'  THEN
             		COALESCE(PPP."IdCuentaBancaria",0) 
        		ELSE 
        			COALESCE(CBANCCORR."IdCuentaBanco",0)  
        		END
  END AS "IdCuentaBancaria"
, CASE WHEN ope."IdTipoCanalIngreso" = 'FMM' AND cPag."RecibePaga" = 'P' THEN
		CASE WHEN COALESCE(PPPSAR."IdCuentaBancaria",0) <> 0 THEN
			COALESCE(CTABANSAR."IdBanco" ,0) 
		ELSE 0 END
 ELSE CASE WHEN cPag."RecibePaga" = 'P'  THEN
             		COALESCE(CBANC."IdBanco" ,0) 
        		ELSE 
        			COALESCE(CBANCCORR."IdBanco" ,0) 
        		END
 END AS "IdBanco"
, CASE WHEN ope."IdTipoCanalIngreso" = 'FMM' AND cPag."RecibePaga" = 'P' THEN CASE WHEN COALESCE(PPPSAR."IdCuentaBancaria",0) <> 0 THEN COALESCE(CTABANSAR."IdTipoCuentaBancaria" ,'') ELSE '' END
	   ELSE CASE WHEN cPag."RecibePaga" = 'P'  THEN
             		COALESCE(CBANC."IdTipoCuentaBancaria" ,'') 
        		ELSE 
        			COALESCE(CBANCCORR."IdTipoCuentaBancaria" ,'') 
        		END
         END AS "IdTipoCuentaBancaria"
, CASE WHEN ope."IdTipoCanalIngreso" = 'FMM' AND cPag."RecibePaga" = 'P' THEN CASE WHEN COALESCE(PPPSAR."IdCuentaBancaria",0) <> 0 THEN COALESCE(BANSAR."NombreBanco",'') ELSE '' END
	   ELSE  CASE WHEN cPag."RecibePaga" = 'P'  THEN
             		COALESCE(BANCBANC."NombreBanco" ,'') 
        		ELSE 
        			COALESCE(CORBANCBANC."NombreBanco" ,'') 
        		END
         END AS "NombreBancoCuentaBancaria"
, CASE WHEN ope."IdTipoCanalIngreso" = 'FMM' AND cPag."RecibePaga" = 'P' THEN CASE WHEN COALESCE(PPPSAR."IdCuentaBancaria",0) <> 0 THEN COALESCE(CTABANSAR."NumeroCuentaBancaria",'') ELSE '' END
	   ELSE CASE WHEN cPag."RecibePaga" = 'P'  THEN
             		COALESCE(CBANC."NumeroCuentaBancaria" ,'') 
        		ELSE 
        			COALESCE(CBANCCORR."NumeroCuentaBancaria" ,'') 
        		END
         	END AS "NumeroCuentaBancaria"
,CASE WHEN ope."IdTipoCanalIngreso" = 'FMM' AND cPag."RecibePaga" = 'P' THEN 
        COALESCE(PPPSAR."IdPreferenciaPagoPortafolio",0)
  ELSE CASE WHEN cPag."RecibePaga" = 'P'  THEN
             		COALESCE(PPP."IdPreferenciaPagoPortafolio",0) 
        		ELSE 
        			COALESCE(CORPPP."IdPreferenciaPagoPortafolio",0)  
        		END
  END AS "IdPreferenciaPagoPortafolio"       
FROM (
    SELECT
        cpo."IdComprobantePagoOperacion",
        cpo."IdEmpresa",
        cpo."IdOperacion",
        cpo."IdIngresoEgreso",
        cpo."IdAbono",
        cpo."IdCargo",
        cpo."IdTipoProducto",
        cpo."IdFormaDePago",
        cpo."RecibePaga",
        cpo."IdModalidadPago",
        cpo."IdMoneda",
        cpo."Monto",
        cpo."FechaIngresoComprobantePagoOperacion",
        cpo."FechaLiquidacionComprobantePagoOperacion",
        cpo."IdEstado",
        cpo."AfectarTesoreria",
        cpo."IdIngresoEgresoAnticipo",
        cpo."IdAbonoAnticipo",
        cpo."IdCargoAnticipo",
        cpo."IdCuentaBanco",
        ROW_NUMBER() OVER (
            PARTITION BY cpo."IdOperacion"
            ORDER BY cpo."FechaIngresoComprobantePagoOperacion" DESC
        ) AS "ROWNUMBERS_ComprobantePago"
    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" cpo
) cPag
INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" ope ON (cPag."IdOperacion"= ope."IdOperacionSpot" AND cPag."FechaIngresoComprobantePagoOperacion" = TO_DATE(ope."FechaOperacion"))
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" fPag
    ON (cPag."IdFormaDePago" = fPag."IdFormaDePago" AND
        cPag."IdTipoProducto" = fPag."IdTipoProducto" AND
        cPag."IdMoneda"= fPag."IdMoneda" AND
        fPag."IdEstado"='ACT')    
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF
    ON ("CodigoParametro"='PLA.IdiomaSistema' AND
        cPag."IdEmpresa" = CONF."IdEmpresa")
LEFT JOIN (
        SELECT
            "CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado"
            ,CASE WHEN DIC."CodDiccionario"='UI_RECIBIMOS' THEN 'R' WHEN DIC."CodDiccionario"='UI_ENTREGAMOS' THEN 'P' ELSE '' END AS "RecibePaga"
        FROM "BSCL.Plataforma.BD::PLA.Diccionario" DIC
        WHERE DIC."CodDiccionario" in ('UI_RECIBIMOS','UI_ENTREGAMOS')
        ) DICCIONARIO
    ON (cPag."RecibePaga"= DICCIONARIO."RecibePaga" AND
        CONF."Valor" = DICCIONARIO."CodLenguaje")
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP ON (ope."IdPortafolio"=PPP."IdPortafolio" AND fPag."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND fPag."IdEmpresa" = PPP."IdEmpresa" AND PPP."IdEstado" = 'ACT' AND PPP."IdCuentaBancaria" = cPag."IdCuentaBanco")
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON (PPP."IdPreferenciaPago" = PP."IdPreferenciaPago")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON (TIPOP."IdTipoOperacion" = PP."IdTipoOperacion")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON ope."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CBANCCOR ON PPP."IdCuentaBancaria" = CBANCCOR."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON (BANCBANC."IdBanco" = CBANC."IdBanco")
--Datos Cuenta Bancaria para Spot canal FMM
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR
	ON ope."IdSolicitudAporteRescate" = SAR."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PPPSAR
	ON (ope."IdPortafolio"=PPPSAR."IdPortafolio" AND SAR."IdPreferenciaPagoPortafolio" = PPPSAR."IdPreferenciaPagoPortafolio" AND SAR."IdEmpresa" = PPPSAR."IdEmpresa" AND PPPSAR."IdEstado" = 'ACT')
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTABANSAR
	ON (CLI."IdPersona" = CTABANSAR."IdPersona" AND PPPSAR."IdCuentaBancaria" = CTABANSAR."IdCuentaBancaria")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANSAR
	ON (CTABANSAR."IdBanco" = BANSAR."IdBanco")
--Datos Cuenta Bancaria Corredora
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBANCCORR ON CBANCCORR."IdCuentaBanco" = cPag."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as CORBANCBANC ON (CORBANCBANC."IdBanco" = CBANCCORR."IdBanco")
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON ope."IdMesa" = MES."IdMesa" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLICOR ON MES."IdCliente" = CLICOR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CORCBANC ON CLICOR."IdPersona" = CORCBANC."IdPersona" AND  CORBANCBANC."IdBanco" = CORCBANC."IdBanco" 
  																AND	CBANCCORR."IdMoneda" = CORCBANC."IdMoneda"
  																AND	CBANCCORR."NumeroCuentaBancaria" = CORCBANC."NumeroCuentaBancaria" 
                                                                AND CORCBANC."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as CORPPP ON (MES."IdPortafolio"=CORPPP."IdPortafolio" AND fPag."IdPreferenciaPago" = CORPPP."IdPreferenciaPago" AND fPag."IdEmpresa" = CORPPP."IdEmpresa" AND CORPPP."IdEstado" = 'ACT' AND  CORPPP."IdCuentaBancaria" = CORCBANC."IdCuentaBancaria")
WHERE cPag."IdTipoProducto" = 'MEME'));


GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
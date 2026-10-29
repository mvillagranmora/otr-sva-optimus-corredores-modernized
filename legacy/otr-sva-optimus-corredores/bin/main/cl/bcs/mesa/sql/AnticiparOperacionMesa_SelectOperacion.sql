select  car."IdOperacion"
        ,car."FolioOperacion"
        ,car."FolioCartera"
        ,car."FechaOperacion"
        ,car."IdTipoProducto"
        ,car."IdTipoOperacion"
        ,tOpe."IdInstrumento"
        ,tOper."Nombre" as "NombreTipoOperacion"
        ,car."IdOperacionRelacionada"
        ,car."Intermesa"
        ,car."IdMesaOrigen"
        ,mesOrigen."Nombre" as "NombreMesaOrigen"
        ,car."IdMesaDestino"
        ,mesDestino."Nombre" as "NombreMesaDestino"
        ,soc."IdSocioDeNegocio"
        ,soc."IdPersona"
        ,car."IdCliente"
        ,cli."NombreCompleto"
        ,cli."DocumentoIdentidad"
        ,car."IdClienteCorredor"
        ,car."IdPortafolio"
        ,por."CodigoPropositoPortafolio"
        ,car."AfectaCustodia"
        ,car."IdPortafolioCorredor"
        ,car."IdEmpresa"
        ,CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END AS "RazonSocial"--car."RazonSocial"
        ,car."MontoOperacion"
        ,car."MontoOperacionInicial"
        ,car."MontoOperacionFinal"
        ,car."IdTipoReajuste"
        ,car."Tasa"
        ,car."IdMoneda"
        ,car."MontoOperacionMoneda"
        ,car."MontoOperacionInicialMoneda"
        ,car."MontoOperacionFinalMoneda"
        ,car."IdMonedaLiquidacion"
        
        ,car."PlazoEnDias"
        ,car."FechaVencimiento"
        ,CAST (car."FechaLiquidacion" AS DATE)  AS "FechaLiquidacion"
        ,car."IdCondicionLiquidacion"
        ,cond."Nombre" as "NombreCondicionLiquidacion"
        ,car."FechaCondicionLiquidacion"
        ,car."IdAgente"
        ,per."NombreCompleto" as "NombreEjecutivo"
        ,car."IdEstado"
        ,est."NombreEstado"
        ,car."FechaCreacion"
        ,car."TasaAgente"
        ,car."IdUsuarioCreacion"
        ,usr."CodigoUsuario"
        ,car."IdTipoCanalIngreso"
        ,car."Glosa"
        ,car."MontoOperacionAnticipo"
        ,car."MontoOperacionInicialAnticipo"
        ,car."MontoOperacionFinalAnticipo"
        ,car."TasaAnticipo"
        ,car."MontoOperacionMonedaAnticipo"
        ,car."MontoOperacionInicialMonedaAnticipo"
        ,car."MontoOperacionFinalMonedaAnticipo"
        ,car."IdMonedaLiquidacionAnticipo"
        ,car."IdCondicionLiquidacionAnticipo"
        ,condAnt."Nombre" as "NombreCondicionLiquidacionAnticipo"
        ,car."FechaCondicionLiquidacionAnticipo"
        ,car."IdEmisor"
        ,res."IdConcepto"               as "IdConceptoDetalle"
        ,res."IdReserva"                as "IdReservaDetalle"
        ,case when res."FolioCarteraOrigen" = 0 then 'S' else 'N' end as "CarteraNominal"
        ,res."FolioCarteraOrigen"       as "FolioCarteraDetalle"
        ,res."FolioCarteraCustodia"     as "FolioCarteraCustodiaDetalle"
        ,res."IdEmisor"                 as "IdEmisorDetalle"
        ,res."CorteMinimo"              as "CorteMinimoDetalle"
        ,res."IdTipoReajuste"           as "IdTipoReajusteDetalle"
        ,res."CantidadInicial"          as "CantidadDetalle"
        
        ,res."IdInstrumento"            as "IdInstrumentoDetalle"
        ,ins."Nemotecnico"              as "NemotecnicoDetalle"
        ,ins."TipoValorizacion"         as "TipoValorizacionDetalle"
        ,cla."ValorClasificacionRiesgo" as "ValorClasificacionRiesgoDetalle"
        ,res."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoDetalle"
        ,res."IdTipoInstrumento"        as "IdTipoInstrumentoDetalle"
        ,res."Precio"                   as "PrecioCompraDetalle"
        ,res."PrecioMercado"                 as "PrecioMercadoDetalle"
        ,res."Tasa"                     as "TasaCompraDetalle"
        ,res."TasaMercado"                       as "TasaMercadoDetalle"
        ,res."MontoValorizado"          as "MontoCompraDetalle"
        ,res."FechaVencimiento"         as "FechaVencimientoInstrumentoDetalle"
        ,res."EsFungible"               as "EsFungibleDetalle"
        ,res."IngCantidad"              as "CantidadReservadaDetalle"
        ,res."IngValorRescate"          as "ValorRescateDetalle"
        ,res."IdUsuarioCreacion"        as "IdUsrReservaDetalle"
        ,res."IdOperacion"              as "IdOperacionDetalle"
        ,res."IdBolsa"                  as "IdBolsaDetalle"
        ,res."IdBoveda"                 as "IdBovedaDetalle"
        ,COALESCE(CatIFRS."CarteraIFRS",'')         as "CodigoIFRSDetalle"
        ,Boveda."Nombre"                AS "NombreBovedaDetalle"
        ,ClasInst."Nombre"              AS "ClasificacionTipoInstrumentoDetalle"

from "BSCL.Mesa.BD::MES.Operacion" car--"BSCL.Mesa.BD.Vistas::ConsultarOperaciones" car
    inner join "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa"  est
        on (car."IdTipoProducto" = est."IdTipoProducto" and est."NombreEntidad" = 'INGRESO' and car."IdEstado" = est."IdEstado")
    inner join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" cond
        on (car."IdTipoOperacion" = cond."IdTipoOperacion" and car."IdCondicionLiquidacion" = cond."IdCondicionDeLiquidacion")
    left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" condAnt
        on (car."IdTipoOperacion" = condAnt."IdTipoOperacion" and car."IdCondicionLiquidacionAnticipo" = condAnt."IdCondicionDeLiquidacion")
    left join "BSCL.Mesa.BD::MES.Mesa" mesOrigen--"BSCL.Mesa.BD.Vistas::ConsultarMesas" mesOrigen
        on (car."IdMesaOrigen" = mesOrigen."IdMesa")
    left join "BSCL.Mesa.BD::MES.Mesa" mesDestino--"BSCL.Mesa.BD.Vistas::ConsultarMesas" mesDestino
        on (car."IdMesaDestino" = mesDestino."IdMesa")
    left join "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" res 
        on (car."IdOperacion" = res."IdOperacion")--falta unirlo con la cartera para cuando la oepracion se encuentre cerrada
    /*left join "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" cPag 
        on (car."IdEmpresa" = cPag."IdEmpresa" and car."IdOperacion" = cPag."IdOperacion" and car."IdTipoProducto" = cPag."IdTipoProducto")*/
    left join "BSCL.Plataforma.BD::INS.Instrumento" ins--"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins
        on (res."IdInstrumento" = ins."IdInstrumento")
    LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" cla ON (cla."IdInstrumento" = res."IdInstrumento")
    inner join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc
        on (car."IdCliente" = soc."IdCliente")
    inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
        on (car."IdCliente" = cli."IdCliente")
    inner join "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por
        on (car."IdPortafolio" = por."IdPortafolio")
    inner join "BSCL.Plataforma.BD::INS.TipoOperacion" tOper
        on (car."IdTipoOperacion" = tOper."IdTipoOperacion")
    left join "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tOpe
        on (car."IdTipoOperacion" = tOpe."IdTipoOperacion" and res."IdTipoInstrumento" = tOpe."IdTipoInstrumento")
    left join "BSCL.Plataforma.BD::ACC.Usuario" usr--"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" usr
        on (car."IdUsuarioCreacion" = usr."IdUsuario")
    left join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per
        on (car."IdAgente" = per."IdPersona")
    --* MASH
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS Boveda 
              ON (Res."IdBoveda" = Boveda."IdBoveda")       
    LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS ClasInst 
              ON (ClasInst."IdClasificacionTipoInstrumento" = res."IdClasificacionTipoInstrumento")
    LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" AS CatIFRS
              ON (Res."CodigoIFRS" = CatIFRS."CodigoCarteraIFRS")       
WHERE car."IdOperacion" IN (:IdOperacion) AND car."IdEstado" ='VIG' AND car."IdTipoOperacion" IN ('COMPAC','VENPAC');


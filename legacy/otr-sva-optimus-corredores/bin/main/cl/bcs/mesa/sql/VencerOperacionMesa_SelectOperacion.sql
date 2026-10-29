select  car."IdOperacion"
        ,car."FolioOperacion"
        ,car."FolioCartera"
        ,car."FechaOperacion"
        ,car."IdTipoProducto"
        ,car."IdTipoOperacion"
        ,car."IdInstrumento"
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
        ,car."IdUsuarioCreacion" AS "IdUsuario"
from "BSCL.Mesa.BD::MES.Operacion" car
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
    inner join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc
        on (car."IdCliente" = soc."IdCliente")
    inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
        on (car."IdCliente" = cli."IdCliente")
    inner join "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por
        on (car."IdPortafolio" = por."IdPortafolio")
    inner join "BSCL.Plataforma.BD::INS.TipoOperacion" tOper
        on (car."IdTipoOperacion" = tOper."IdTipoOperacion")
    left join "BSCL.Plataforma.BD::ACC.Usuario" usr
        on (car."IdUsuarioCreacion" = usr."IdUsuario")
    left join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per
        on (car."IdAgente" = per."IdPersona")    
WHERE car."IdOperacion" IN (:IdOperacion) AND car."IdEstado" ='VIG' AND car."IdTipoOperacion" IN ('COMPAC','VENPAC')
AND car."FechaVencimiento" <= (:FechaVencimiento);



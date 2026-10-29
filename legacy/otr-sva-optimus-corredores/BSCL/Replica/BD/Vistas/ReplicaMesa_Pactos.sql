SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_Pactos"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_Pactos" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdOperacion")  AS TABLE_KEY,
        		"Action" AS ACTION,
        		"IdOperacion" AS "IDOPERACION",
				"FolioOperacion" AS "FOLIOOPERACION",
				"FolioCartera" AS "FOLIOCARTERA",
				"FechaOperacion" AS "FECHAOPERACION",
				"IdTipoProducto" AS "IDTIPOPRODUCTO",
				"IdTipoOperacion" AS "IDTIPOOPERACION",
				"NombreTipoOperacion" AS "NOMBRETIPOOPERACION",
				"Intermesa" AS "INTERMESA",
				"IdMesaOrigen" AS "IDMESAORIGEN",
				"NombreMesaOrigen" AS "NOMBREMESAORIGEN",
				"IdMesaDestino" AS "IDMESADESTINO",
				"NombreMesaDestino" AS "NOMBREMESADESTINO",
				"IdSocioDeNegocio" AS "IDSOCIODENEGOCIO",
				"IdCliente" AS "IDCLIENTE",
				"NombreCompleto" AS "NOMBRECOMPLETO",
				"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
				"IdClienteCorredor" AS "IDCLIENTECORREDOR",
				"IdPortafolio" AS "IDPORTAFOLIO",
				"CodigoPropositoPortafolio" AS "CODIGOPROPOSITOPORTAFOLIO",
				"AfectaCustodia" AS "AFECTACUSTODIA",
				"IdPortafolioCorredor" AS "IDPORTAFOLIOCORREDOR",
				"IdEmpresa" AS "IDEMPRESA",
				"MontoOperacionInicial" AS "MONTOOPERACIONINICIAL",
				"MontoOperacionFinal" AS "MONTOOPERACIONFINAL",
				"IdTipoReajuste" AS "IDTIPOREAJUSTE",
				"Tasa" AS "TASA",
				"IdMoneda" AS "IDMONEDA",
				"MontoOperacionInicialMoneda" AS "MONTOOPERACIONINICIALMONEDA",
				"MontoOperacionFinalMoneda" AS "MONTOOPERACIONFINALMONEDA",
				"IdMonedaLiquidacion" AS "IDMONEDALIQUIDACION",
				"PlazoEnDias" AS "PLAZOENDIAS",
				"FechaVencimiento" AS "FECHAVENCIMIENTO",
				"FechaLiquidacion" AS "FECHALIQUIDACION",
				"IdCondicionLiquidacion" AS "IDCONDICIONLIQUIDACION",
				"NombreCondicionLiquidacion" AS "NOMBRECONDICIONLIQUIDACION",
				"FechaCondicionLiquidacion" AS "FECHACONDICIONLIQUIDACION",
				"IdAgente" AS "IDAGENTE",
				"NombreEjecutivo" AS "NOMBREEJECUTIVO",
				"IdEstado" AS "IDESTADO",
				"NombreEstado" AS "NOMBREESTADO",
				"FechaCreacion" AS "FECHACREACION",
				"IdUsuarioCreacion" AS "IDUSUARIOCREACION",
				"CodigoUsuario" AS "CODIGOUSUARIO",
				"IdTipoCanalIngreso" AS "IDTIPOCANALINGRESO",
				"Glosa" AS "GLOSA",
				"MontoOperacionInicialAnticipo" AS "MONTOOPERACIONINICIALANTICIPO",
				"MontoOperacionFinalAnticipo" AS "MONTOOPERACIONFINALANTICIPO",
				"TasaAnticipo" AS "TASAANTICIPO",
				"MontoOperacionInicialMonedaAnticipo" AS "MONTOOPERACIONINICIALMONEDAANTICIPO",
				"MontoOperacionFinalMonedaAnticipo" AS "MONTOOPERACIONFINALMONEDAANTICIPO",
				"IdMonedaLiquidacionAnticipo" AS "IDMONEDALIQUIDACIONANTICIPO",
				"IdCondicionLiquidacionAnticipo" AS "IDCONDICIONLIQUIDACIONANTICIPO",
				"NombreCondicionLiquidacionAnticipo" AS "NOMBRECONDICIONLIQUIDACIONANTICIPO",
				"FechaCondicionLiquidacionAnticipo" AS "FECHACONDICIONLIQUIDACIONANTICIPO"

        from "BSCL.Mesa.BD.Vistas::MRF.ConsultarPactosAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_Pactos' AND WST."Key" = "IdOperacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_Pactos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

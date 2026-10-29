--revisar el caso de nominales
SELECT  OPE."IdOperacion",
	    OPE."IdTipoOperacion",
	    OPE."IdCliente",
	    OPE."IdPortafolio",
	    OPE."IdClienteCorredor",
	    OPE."IdPortafolioCorredor",
	    OPE."FolioCartera" AS "FolioCarteraPacto",
	    CAR."IdCartera" AS "IdCarteraPacto",
	    OPE."IdInstrumento" AS "IdInstrumentoPacto",
	    OPE."IdTipoProducto",
	    RES."IdReserva",
	    RES."IdInstrumento" AS "IdInstrumentoDetalle" ,
	    RES."IngCantidad" AS "CantidadDetalle",
	    RES."IngValorRescate" AS "ValorRescateDetalle", 
	    RES."IngMontoValorizado" AS "MontoValorizadoDetalle",
	    RES."FolioSubyacente" AS "FolioCarteraOrigenDetalle", 	    
	    RES."IdCarteraOrigen" AS "IdCarteraOrigenDetalle",
	    RES."IdEmisor",
	    SUB."IdSubyacente" AS "IdCarteraSubyacenteOrigenDetalle",
	    CAR."FechaLiquidacion",
	    SUB."EsFungible",
	    SUB."IdMonedaEmision",
	    SUB."IdTipoReajuste",
	    SUB."IdMonedaInstrumento",
	    SUB."IdCarteraRetirada",
	    OPE."PlazoEnDias",
	    SUB."FechaVencimientoInstrumento"
FROM  	"BSCL.Mesa.BD::MES.Operacion" OPE
	    INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON (OPE."IdOperacion"= RES."IdOperacion" AND RES."IdEstado" IN ('VIG','LIQ'))
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON (OPE."FolioCartera" = SUB."FolioCarteraRelacionada" AND RES."FolioSubyacente" = SUB."FolioSubyacente" ) 
	    INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON (OPE."FolioCartera" = CAR."FolioCartera" AND OPE."IdOperacion" = CAR."IdOperacion")
WHERE 	OPE."IdOperacion" IN (:IdOperacion) AND OPE."IdEstado"='VIG'



SELECT  Cus."IdMesa"
            ,Cus."FolioCartera"
            ,Cus."IdCartera"
            ,Cus."Nemotecnico"
            ,Cus."IdEmisor"
            ,Cus."IdTipoReajuste"
            ,CASE WHEN Cus."EsFungible" = 'S' THEN (Cus."INGValorRescate" - Coalesce(Res."ValorRescateInicial", 0)) ELSE (Cus."INGCantidad" - Coalesce(Res."CantidadInicial", 0)) END  AS "Cantidad"            
            ,Cus."IdEmpresa"
            ,Cus."IdCliente"
            ,Cus."IdPortafolio"
            ,Cus."INGFechaOperacion"
            ,Cus."FechaLiquidacion"
            ,Cus."IdInstrumento"
            ,Cus."IdClasificacionTipoInstrumento"
            ,Cus."IdTipoInstrumento"
            ,TipoInstrumento."Nombre" as "TipoInstrumento"
            ,Cus."CorteMinimo"
            ,Cus."PrecioCompra"
            ,Cus."TasaCompra"
            ,Cus."MontoCompra"
            ,Cus."INGPrecioMercadoActual"
            ,Cus."INGTasaMercadoActual"
            ,Cus."INGMontoATasaMercadoActual"
            ,Cus."FechaVencimientoInstrumento"
            ,Cus."FechaVencimientoOperacion"
            ,Cus."EsFungible"
            ,Cus."CodigoIFRS"
            ,Cus."IdMonedaInstrumento"
            ,CASE WHEN Cus."EsFungible" = 'S' THEN Coalesce(Res."ValorRescateInicial", 0) ELSE Coalesce(Res."CantidadInicial", 0) END AS "CantidadTotalReservada"
            ,CASE WHEN Cus."EsFungible" = 'S' THEN Coalesce(ResCus."ValorRescateInicial", 0) ELSE Coalesce(ResCus."CantidadInicial", 0) END AS "CantidadReservadaCus"
            ,ResCus."IdUsuarioCreacion" AS "IdUsrReserva"
            ,Usr."CodigoUsuario" AS "CodUsrReserva"
            ,CASE WHEN Cus."EsFungible" = 'S' THEN Coalesce(ResMes."ValorRescateInicial", 0) ELSE Coalesce(ResMes."CantidadInicial", 0) END AS "CantidadReservadaMes"
    FROM    "BSCL.Custodia.BD::CUS.Cartera" AS Cus    
            LEFT JOIN ( SELECT "FolioCarteraOrigen",
                                SUM( "IngCantidad" ) AS "CantidadInicial",
                                SUM( "IngValorRescate" ) AS "ValorRescateInicial"
                        FROM    "BSCL.Custodia.BD::CUS.Reserva" 
                        WHERE   "IdEstado" IN ('VIG','PEN') --AND "IdTipoProducto" = 'CUST'
                        GROUP BY "FolioCarteraOrigen") AS Res 
            ON Res."FolioCarteraOrigen" = Cus."FolioCartera"
            LEFT JOIN ( SELECT  "FolioCarteraOrigen",
                                "IdUsuarioCreacion",
                                SUM( "IngCantidad" ) AS "CantidadInicial",
                                SUM( "IngValorRescate" ) AS "ValorRescateInicial"
                        FROM    "BSCL.Custodia.BD::CUS.Reserva" 
                        WHERE   "IdEstado" IN ('VIG','PEN') AND "IdTipoProducto" = 'CUST'
                        GROUP BY "FolioCarteraOrigen", "IdUsuarioCreacion" ) AS ResCus 
            ON ResCus."FolioCarteraOrigen" = Cus."FolioCartera"
            
            LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usr ON ResCus."IdUsuarioCreacion" = Usr."IdUsuario"
            
            LEFT JOIN (  SELECT  "FolioCarteraOrigen",
                                SUM("IngCantidad") AS "CantidadInicial",
                                SUM( "IngValorRescate" ) AS "ValorRescateInicial"
                        FROM    "BSCL.Custodia.BD::CUS.Reserva"
                        WHERE   "IdEstado" IN ('VIG','PEN') AND "IdTipoProducto" = 'MERF'                        
                        GROUP BY "FolioCarteraOrigen" ) AS ResMes
            ON ResMes."FolioCarteraOrigen" = Cus."FolioCartera"
            
            LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento 
            ON TipoInstrumento."IdTipoInstrumento" = Cus."IdTipoInstrumento" 
    INNER JOIN "BSCL.Custodia.BD::GBL.IngresoReservaFoliosConsultaPaso" AS TMP ON (TMP."FolioCartera" = Cus."FolioCartera")
    WHERE 
    Cus."IdEstado" = 'VIG'
    AND Cus."IdClasificacionTipoInstrumento" IN ( 'RF', 'IF') 
    AND Cus."IdTipoInstrumento" != 'PAC'
    AND TMP."IdTrabajo" = :IdTrabajo
    ORDER BY Cus."FolioCartera";

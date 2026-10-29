--* ------------------------------------------------------------------------
--* Nombre .....: ConsultarCarteraReservada
--* Modulo .....: Custodia
--* F. Creación : Junio 2016
--* Proposito ..: Consultar las custodia t0 y asociarle las reservas a cada cartera.
--* ------------------------------------------------------------------------

SET SCHEMA "BSCL"; 
--* ------------------------------------------------------------------------

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraReservada";
--* ------------------------------------------------------------------------

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraReservada" 
AS 
	SELECT	Cus."IdMesa" , COALESCE(Res."IdReserva", 0) AS "IdReserva"
			,Cus."FolioCartera"
			,Cus."Nemotecnico"
			,Cus."IdEmisor"
			,Cus."IdTipoReajuste"
--*			,Cus."MontoCompraMonedaInstrumento" AS "Cantidad"
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
			--,Coalesce(ResCus."CantidadInicial", 0) AS "CantidadTotalReservada"
			--+ Coalesce(ResMes."CantidadInicial", 0)
			--,ResCus."IdOperacion" AS "IdOperacionCus" 
			--,ResCus."IdReserva" AS "IdReservaCus"
			--,ResMes."IdOperacion" AS "IdOperacionMes" 
            --,ResMes."IdReserva" AS "IdReservaMes"
	FROM	"BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS Cus
	        LEFT JOIN ( SELECT "FolioCarteraOrigen",
                                --"IdOperacion",
                               "IdReserva",
                                SUM( "IngCantidad" ) AS "CantidadInicial",
                                SUM( "IngValorRescate" ) AS "ValorRescateInicial"
                        FROM    "BSCL.Custodia.BD::CUS.Reserva" 
                        WHERE   "IdEstado" IN ('VIG','PEN') --AND "IdTipoProducto" = 'CUST'
                        GROUP BY "FolioCarteraOrigen", "IdReserva") AS Res 
            ON Res."FolioCarteraOrigen" = Cus."FolioCartera"
			LEFT JOIN ( SELECT	"FolioCarteraOrigen",
								"IdUsuarioCreacion",
								--"IdOperacion",
								--"IdReserva",
								SUM( "IngCantidad" ) AS "CantidadInicial",
                                SUM( "IngValorRescate" ) AS "ValorRescateInicial"
						FROM	"BSCL.Custodia.BD::CUS.Reserva" 
						WHERE	"IdEstado" IN ('VIG','PEN') AND "IdTipoProducto" = 'CUST'
						GROUP BY "FolioCarteraOrigen", "IdUsuarioCreacion" ) AS ResCus 
			ON ResCus."FolioCarteraOrigen" = Cus."FolioCartera"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS Usr   
            ON ResCus."IdUsuarioCreacion" = Usr."IdUsuario" 
            
			LEFT JOIN (  SELECT  "FolioCarteraOrigen",
			                   --  "IdOperacion",
                                --"IdReserva",
                                SUM("IngCantidad") AS "CantidadInicial",
                                SUM( "IngValorRescate" ) AS "ValorRescateInicial"
                        FROM    "BSCL.Custodia.BD::CUS.Reserva"
                        WHERE   "IdEstado" IN ('VIG','PEN') AND "IdTipoProducto" = 'MERF'                        
                        GROUP BY "FolioCarteraOrigen" ) AS ResMes
            ON ResMes."FolioCarteraOrigen" = Cus."FolioCartera"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento 
			ON TipoInstrumento."IdTipoInstrumento" = Cus."IdTipoInstrumento" 
			
			
	WHERE	Cus."IdEstado" = 'VIG'
			AND Cus."IdClasificacionTipoInstrumento" IN ( 'RF', 'IF') AND Cus."IdTipoInstrumento" != 'PAC'		
	ORDER BY "FolioCartera"	
		
	WITH 	READ ONLY;		
--* ------------------------------------------------------------------------

GRANT 	SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraReservada"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

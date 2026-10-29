SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdSolicitudAporteRescate"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "TipoSolicitud"  AS "TIPOSOLICITUD", -- VARCHAR(3)
                  "IdSerieFondo"  AS "IDSERIEFONDO", -- VARCHAR(40)
                  "IdTipoMovimientoFondoMutuo"  AS "IDTIPOMOVIMIENTOFONDOMUTUO", -- VARCHAR(10)
                  "IdSerieFondoDestino"  AS "IDSERIEFONDODESTINO", -- VARCHAR(40)
                  "IdSerieFondoOrigen"  AS "IDSERIEFONDOORIGEN", -- VARCHAR(40)
                  "IdEmisorDestino"  AS "IDEMISORDESTINO", -- VARCHAR(40)
                  "IndicadorMLT"  AS "INDICADORMLT", -- VARCHAR(1)
                  "Overnight"  AS "OVERNIGHT", -- VARCHAR(1)
                  "IdTipoCartera"  AS "IDTIPOCARTERA", -- VARCHAR(3)
                  "CalculoPorMontoCuota"  AS "CALCULOPORMONTOCUOTA", -- VARCHAR(1)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "Estado"  AS "ESTADO", -- VARCHAR(3)
                  "IdMedioDePago"  AS "IDMEDIODEPAGO", -- VARCHAR(3)
                  "EstadoLiquidacion"  AS "ESTADOLIQUIDACION", -- VARCHAR(3)
                  "OrigenOperacion"  AS "ORIGENOPERACION", -- VARCHAR(1)
                  "SolicitudBatch"  AS "SOLICITUDBATCH", -- VARCHAR(1)
                  "ComisionInformada"  AS "COMISIONINFORMADA", -- VARCHAR(1)
                  "NombreSerieFondo"  AS "NOMBRESERIEFONDO", -- VARCHAR(100)
                  "CodigoSerie"  AS "CODIGOSERIE", -- VARCHAR(10)
                  "ExcluyeGarantia"  AS "EXCLUYEGARANTIA", -- VARCHAR(1)
                  "CobraComision"  AS "COBRACOMISION", -- VARCHAR(1)
                  "Omnibus"  AS "OMNIBUS", -- VARCHAR(1)
                  "Diferido"  AS "DIFERIDO", -- VARCHAR(1)
                  "IdMedioDePagoAbonoAGF"  AS "IDMEDIODEPAGOABONOAGF", -- VARCHAR(3)
                  "IdMedioDePagoCargoAGF"  AS "IDMEDIODEPAGOCARGOAGF", -- VARCHAR(3)
                  "IdFondo"  AS "IDFONDO", -- VARCHAR(40)
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "NombreFondo"  AS "NOMBREFONDO", -- VARCHAR(100)
                  "IdTipoFondo"  AS "IDTIPOFONDO", -- VARCHAR(10)
                  "CodigoSVS"  AS "CODIGOSVS", -- VARCHAR(40)
                  "MoneyMarket"  AS "MONEYMARKET", -- VARCHAR(1)
                  "IdCategoriaPerfilFondoMutuo"  AS "IDCATEGORIAPERFILFONDOMUTUO", -- VARCHAR(40)
                  "Vigencia"  AS "VIGENCIA", -- VARCHAR(1)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdMonedaFondo"  AS "IDMONEDAFONDO", -- VARCHAR(3)
                  "NombreCorto"  AS "NOMBRECORTO", -- VARCHAR(40)
                  "Descripcion"  AS "DESCRIPCION", -- VARCHAR(100)
                  "DescripcionTipoMovimiento"  AS "DESCRIPCIONTIPOMOVIMIENTO", -- VARCHAR(100)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "NombreEmpleado"  AS "NOMBREEMPLEADO", -- VARCHAR(302)
                  "TextoPreferenciaPago"  AS "TEXTOPREFERENCIAPAGO", -- VARCHAR(100)
                  "NombreEstadoLiquidacion"  AS "NOMBREESTADOLIQUIDACION", -- VARCHAR(100)
                  "IdTipoMovimientoFondoMutuoRelacionado"  AS "IDTIPOMOVIMIENTOFONDOMUTUORELACIONADO", -- VARCHAR(10)
                  "DescripcionTipoMovimientoRelacionado"  AS "DESCRIPCIONTIPOMOVIMIENTORELACIONADO", -- VARCHAR(100)
                  "FechaEmision"  AS "FECHAEMISION", -- VARCHAR(10)
                  "FechaAceptacion"  AS "FECHAACEPTACION", -- VARCHAR(10)
                  "FechaCertificacion"  AS "FECHACERTIFICACION", -- VARCHAR(10)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "FechaAnulacion"  AS "FECHAANULACION", -- VARCHAR(10)
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(10)
                  "FechaConstitucion"  AS "FECHACONSTITUCION", -- VARCHAR(10)
                  "FechaCierre"  AS "FECHACIERRE", -- VARCHAR(10)
                  "IdSolicitudAporteRescate"  AS "IDSOLICITUDAPORTERESCATE", -- BIGINT
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "IdDividendo"  AS "IDDIVIDENDO", -- BIGINT
                  "IdClienteDestino"  AS "IDCLIENTEDESTINO", -- BIGINT
                  "IdPortafolioDestino"  AS "IDPORTAFOLIODESTINO", -- BIGINT
                  "IdSolicitudAporteRescateRelacionado"  AS "IDSOLICITUDAPORTERESCATERELACIONADO", -- BIGINT
                  "Monto"  AS "MONTO", -- DECIMAL
                  "NumeroCuotas"  AS "NUMEROCUOTAS", -- DECIMAL
                  "ValorCuota"  AS "VALORCUOTA", -- DECIMAL
                  "CostoUF"  AS "COSTOUF", -- DECIMAL
                  "CostoMonedaLocal"  AS "COSTOMONEDALOCAL", -- DECIMAL
                  "ValorUFFechaInversion"  AS "VALORUFFECHAINVERSION", -- DECIMAL
                  "ParidadFechaInversion"  AS "PARIDADFECHAINVERSION", -- DECIMAL
                  "MontoComision"  AS "MONTOCOMISION", -- DECIMAL
                  "MontoPago"  AS "MONTOPAGO", -- DECIMAL
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
                  "MontoInversion"  AS "MONTOINVERSION", -- DECIMAL
                  "NumeroCuotasInversion"  AS "NUMEROCUOTASINVERSION", -- DECIMAL
                  "ValorCuotaInversion"  AS "VALORCUOTAINVERSION", -- DECIMAL
                  "CostoMonedaLocalInversion"  AS "COSTOMONEDALOCALINVERSION", -- DECIMAL
                  "ParidadFechaCertificacion"  AS "PARIDADFECHACERTIFICACION", -- DECIMAL
                  "MontoPagoLocal"  AS "MONTOPAGOLOCAL", -- DECIMAL
                  "MontoComisionLocal"  AS "MONTOCOMISIONLOCAL", -- DECIMAL
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "MontoMinimo"  AS "MONTOMINIMO", -- DECIMAL
                  "MontoMaximo"  AS "MONTOMAXIMO", -- DECIMAL
                  "DiasRetencionesRescate"  AS "DIASRETENCIONESRESCATE", -- BIGINT
                  "DecimalesValorCuota"  AS "DECIMALESVALORCUOTA", -- BIGINT
                  "DecimalesCuota"  AS "DECIMALESCUOTA", -- BIGINT
                  "IdClienteAGF"  AS "IDCLIENTEAGF", -- BIGINT
                  "IdPortafolioAGF"  AS "IDPORTAFOLIOAGF", -- BIGINT
                  "IdPaisOrigen"  AS "IDPAISORIGEN", -- BIGINT
                  "IdPersonaAGF"  AS "IDPERSONAAGF", -- BIGINT
                  "RentabilidadReal"  AS "RENTABILIDADREAL", -- DECIMAL
                  "IdRegimenTributario"  AS "IDREGIMENTRIBUTARIO", -- VARCHAR(40)
                  "IdMatrizAgrupador"  AS "IDMATRIZAGRUPADOR", -- BIGINT
                  "EsADC"  AS "ESADC", -- VARCHAR(1)
				  "FechaValorCuota" AS "FECHAVALORCUOTA",
				  "FolioContratoOperacion" AS "FOLIOCONTRATOOPERACION", -- VARCHAR(100)
				  "TipoContratoOperacion" AS "TIPOCONTRATOOPERACION", -- VARCHAR(40)
		  		  "MontoRetencion" AS "MONTORETENCION", -- DECIMAL
		  		  "IdOperacionSpot" AS "IDOPERACIONSPOT", -- BIGINT
		  		  "MontoMonedaSistema" AS "MONTOMONEDASISTEMA", -- DECIMAL
				  "MontoMonedaSistemaInicial" AS "MONTOMONEDASISTEMAINICIAL", -- DECIMAL
				  "Canal" as "CANAL"  -- VARCHAR(50)
        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_SolicitudAporteRescateFondoMutuo' AND WST."Key" = "IdSolicitudAporteRescate"
        --and 1 = 2
        --where "FechaEmision" >  '2013-01-01'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

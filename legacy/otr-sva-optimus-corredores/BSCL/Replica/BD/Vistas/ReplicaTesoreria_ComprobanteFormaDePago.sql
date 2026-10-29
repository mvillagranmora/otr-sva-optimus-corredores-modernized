SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteFormaDePago"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteFormaDePago" AS

        Select WST."IdStack" AS "IDSTACK", 
				  "IdComprobanteFormaDePago"  AS TABLE_KEY,
				  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(19)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "IdComprobanteFormaDePago"  AS "IDCOMPROBANTEFORMADEPAGO", -- BIGINT
                  "TipoComprobante"  AS "TIPOCOMPROBANTE", -- VARCHAR(10)
                  "IdMedioDePago"  AS "IDMEDIODEPAGO", -- VARCHAR(3)
                  "NombreMedioDePago"  AS "NOMBREMEDIODEPAGO", -- VARCHAR(100)
                  "TipoMedioDePago"  AS "TIPOMEDIODEPAGO", -- VARCHAR(3)
                  "IdModalidadDePago"  AS "IDMODALIDADDEPAGO", -- VARCHAR(6)
                  "NombreModalidadDePago"  AS "NOMBREMODALIDADDEPAGO", -- VARCHAR(100)
                  "IdCaja"  AS "IDCAJA", -- BIGINT
                  "CodigoCaja"  AS "CODIGOCAJA", -- VARCHAR(40)
                  "NombreCaja"  AS "NOMBRECAJA", -- VARCHAR(100)
                  "IdCuentaBancariaCorredora"  AS "IDCUENTABANCARIACORREDORA", -- BIGINT
                  "IdTipoCuentaBancariaCorredora"  AS "IDTIPOCUENTABANCARIACORREDORA", -- VARCHAR(4)
                  "NombreTipoCuentaBancariaCorredora"  AS "NOMBRETIPOCUENTABANCARIACORREDORA", -- VARCHAR(100)
                  "IdBancoCorredora"  AS "IDBANCOCORREDORA", -- BIGINT
                  "NombreBancoCorredora"  AS "NOMBREBANCOCORREDORA", -- VARCHAR(100)
                  "NombreCuentaBancariaCorredora"  AS "NOMBRECUENTABANCARIACORREDORA", -- VARCHAR(100)
                  "NumeroCuentaBancariaCorredora"  AS "NUMEROCUENTABANCARIACORREDORA", -- VARCHAR(100)
                  "IdTarjetaBancariaCorredora"  AS "IDTARJETABANCARIACORREDORA", -- BIGINT
                  "IdTipoTarjetaBancariaCorredora"  AS "IDTIPOTARJETABANCARIACORREDORA", -- VARCHAR(40)
                  "NombreTipoTarjetaBancariaCorredora"  AS "NOMBRETIPOTARJETABANCARIACORREDORA", -- VARCHAR(100)
                  "NumeroTarjetaBancariaCorredora"  AS "NUMEROTARJETABANCARIACORREDORA", -- VARCHAR(100)
                  "IdChequeraCorredora"  AS "IDCHEQUERACORREDORA", -- BIGINT
                  "NumeroSerieCorredora"  AS "NUMEROSERIECORREDORA", -- VARCHAR(40)
                  "NumeroDocumentoCorredora"  AS "NUMERODOCUMENTOCORREDORA", -- BIGINT
                  "FechaDocumentoPagoCorredora"  AS "FECHADOCUMENTOPAGOCORREDORA", -- VARCHAR(10)
                  "IdCuentaBancariaTitular"  AS "IDCUENTABANCARIATITULAR", -- BIGINT
                  "IdTipoCuentaBancariaTitular"  AS "IDTIPOCUENTABANCARIATITULAR", -- VARCHAR(4)
                  "NombreTipoCuentaBancariaTitular"  AS "NOMBRETIPOCUENTABANCARIATITULAR", -- VARCHAR(100)
                  "NombreCuentaBancariaTitular"  AS "NOMBRECUENTABANCARIATITULAR", -- VARCHAR(100)
                  "IdBancoTitular"  AS "IDBANCOTITULAR", -- BIGINT
                  "NombreBancoTitular"  AS "NOMBREBANCOTITULAR", -- VARCHAR(100)
                  "NumeroCuentaBancaria"  AS "NUMEROCUENTABANCARIA", -- VARCHAR(100)
                  "IdTarjetaBancaria"  AS "IDTARJETABANCARIA", -- BIGINT
                  "IdTipoTarjetaBancariaTitular"  AS "IDTIPOTARJETABANCARIATITULAR", -- VARCHAR(40)
                  "NombreTipoTarjetaBancariaTitular"  AS "NOMBRETIPOTARJETABANCARIATITULAR", -- VARCHAR(100)
                  "NumeroTarjetaBancariaTitular"  AS "NUMEROTARJETABANCARIATITULAR", -- VARCHAR(100)
                  "NumeroSerieTitular"  AS "NUMEROSERIETITULAR", -- VARCHAR(40)
                  "NumeroDocumentoTitular"  AS "NUMERODOCUMENTOTITULAR", -- BIGINT
                  "FechaDocumentoPagoTitular"  AS "FECHADOCUMENTOPAGOTITULAR", -- VARCHAR(10)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "DecimalesMoneda"  AS "DECIMALESMONEDA", -- INT
                  "NombreMoneda"  AS "NOMBREMONEDA", -- VARCHAR(100)
                  "Monto"  AS "MONTO", -- DECIMAL
                  "MontoEgreso"  AS "MONTOEGRESO", -- DECIMAL
                  "MontoIngreso"  AS "MONTOINGRESO", -- DECIMAL
                  "MontoConSigno"  AS "MONTOCONSIGNO", -- DECIMAL
                  "IdEstadoMedioPago"  AS "IDESTADOMEDIOPAGO", -- VARCHAR(3)
                  "NombreEstadoMedioPago"  AS "NOMBREESTADOMEDIOPAGO", -- VARCHAR(100)
                  "IdUsuarioGiro"  AS "IDUSUARIOGIRO", -- BIGINT
                  "NombreUsuarioGiro"  AS "NOMBREUSUARIOGIRO", -- VARCHAR(302)
                  "FechaGiro"  AS "FECHAGIRO", -- VARCHAR(19)
                  "IdComprobantePartida"  AS "IDCOMPROBANTEPARTIDA", -- BIGINT
                  "IdTipoSocioNegocioPersona"  AS "IDTIPOSOCIONEGOCIOPERSONA", -- VARCHAR(3)
                  "NombreTipoSocioNegocioPersona"  AS "NOMBRETIPOSOCIONEGOCIOPERSONA", -- VARCHAR(100)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "NombrePersona"  AS "NOMBREPERSONA", -- VARCHAR(300)
                  "IdTipoDocumentoIdentidadPersona"  AS "IDTIPODOCUMENTOIDENTIDADPERSONA", -- VARCHAR(3)
                  "DocumentoIdentidadPersona"  AS "DOCUMENTOIDENTIDADPERSONA", -- VARCHAR(40)
                  "IdTipoPersona"  AS "IDTIPOPERSONA", -- VARCHAR(2)
                  "NombreTipoPersona"  AS "NOMBRETIPOPERSONA", -- VARCHAR(100)
                  "IdTipoSocioNegocioPersonaTitular"  AS "IDTIPOSOCIONEGOCIOPERSONATITULAR", -- VARCHAR(3)
                  "NombreTipoSocioNegocioPersonaTitular"  AS "NOMBRETIPOSOCIONEGOCIOPERSONATITULAR", -- VARCHAR(100)
                  "IdPersonaTitular"  AS "IDPERSONATITULAR", -- BIGINT
                  "NombrePersonaTitular"  AS "NOMBREPERSONATITULAR", -- VARCHAR(300)
                  "IdPortafolioPersonaTitular"  AS "IDPORTAFOLIOPERSONATITULAR", -- BIGINT
                  "IdTipoPersonaTitular"  AS "IDTIPOPERSONATITULAR", -- VARCHAR(2)
                  "NombreTipoPersonaTitular"  AS "NOMBRETIPOPERSONATITULAR", -- VARCHAR(100)
                  "CodigoPortafolioPersonaTitular"  AS "CODIGOPORTAFOLIOPERSONATITULAR", -- VARCHAR(100)
                  "NombrePortafolioPersonaTitular"  AS "NOMBREPORTAFOLIOPERSONATITULAR", -- VARCHAR(621)
                  "IdUsuarioCertificacionMedioPago"  AS "IDUSUARIOCERTIFICACIONMEDIOPAGO", -- BIGINT
                  "NombreUsuarioCertificacionMedioPago"  AS "NOMBREUSUARIOCERTIFICACIONMEDIOPAGO", -- VARCHAR(302)
                  "FechaHoraCertificacionMedioPago"  AS "FECHAHORACERTIFICACIONMEDIOPAGO", -- VARCHAR(19)
                  "IdUsuarioLiquidacionMedioPago"  AS "IDUSUARIOLIQUIDACIONMEDIOPAGO", -- BIGINT
                  "NombreUsuarioLiquidacionMedioPago"  AS "NOMBREUSUARIOLIQUIDACIONMEDIOPAGO", -- VARCHAR(302)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "FechaHoraLiquidacionMedioPago"  AS "FECHAHORALIQUIDACIONMEDIOPAGO", -- VARCHAR(27)
                  "IdDeposito"  AS "IDDEPOSITO", -- BIGINT
                  "Depositado"  AS "DEPOSITADO", -- VARCHAR(1)
                  "IdNominaTransferencia"  AS "IDNOMINATRANSFERENCIA", -- BIGINT
                  "FechaAfectaSaldo"  AS "FECHAAFECTASALDO", -- VARCHAR(10)
                  "DiasRetencion"  AS "DIASRETENCION", -- BIGINT
                  "SaldoDisponible"  AS "SALDODISPONIBLE", -- DECIMAL
                  "SaldoContable"  AS "SALDOCONTABLE", -- DECIMAL
                  "AfectoSaldo"  AS "AFECTOSALDO", -- VARCHAR(1)
                  "DocumentoEnviaADeposito"  AS "DOCUMENTOENVIAADEPOSITO", -- VARCHAR(1)
                  "IdEstadoComprobante"  AS "IDESTADOCOMPROBANTE", -- VARCHAR(3)
                  "NombreEstadoComprobante"  AS "NOMBREESTADOCOMPROBANTE", -- VARCHAR(100)
                  "IdConceptoComprobante"  AS "IDCONCEPTOCOMPROBANTE", -- VARCHAR(6)
                  "NombreConceptoComprobante"  AS "NOMBRECONCEPTOCOMPROBANTE", -- VARCHAR(100)
                  "FechaLiquidacionComprobante"  AS "FECHALIQUIDACIONCOMPROBANTE" -- VARCHAR(10)

        from "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Tesoreria_ComprobanteFormaDePago' AND WST."Key" = "IdComprobanteFormaDePago"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

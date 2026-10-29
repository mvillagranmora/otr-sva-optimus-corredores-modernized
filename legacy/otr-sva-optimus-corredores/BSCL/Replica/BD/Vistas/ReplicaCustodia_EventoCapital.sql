SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_EventoCapital"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_EventoCapital" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdEventoCapital"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdEventoCapital"  AS "IDEVENTOCAPITAL", -- BIGINT
                  "FechaHoraIngresoRegistro"  AS "FECHAHORAINGRESOREGISTRO", -- VARCHAR(27)
                  "FechaHoraUltimaActualizacion"  AS "FECHAHORAULTIMAACTUALIZACION", -- VARCHAR(27)
                  "IdTipoEventoCapital"  AS "IDTIPOEVENTOCAPITAL", -- VARCHAR(2)
                  "NombreEventoCapital"  AS "NOMBREEVENTOCAPITAL", -- VARCHAR(100)
                  "Descripcion"  AS "DESCRIPCION", -- VARCHAR(100)
                  "Secuencia"  AS "SECUENCIA", -- BIGINT
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "NombreMoneda"  AS "NOMBREMONEDA", -- VARCHAR(100)
                  "Paridad"  AS "PARIDAD", -- DECIMAL
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemo"  AS "NEMO", -- VARCHAR(40)
                  "EsInscrito"  AS "ESINSCRITO", -- VARCHAR(1)
                  "FechaLimite"  AS "FECHALIMITE", -- VARCHAR(10)
                  "FechaActualizacion"  AS "FECHAACTUALIZACION", -- VARCHAR(10)
                  "FechaPago"  AS "FECHAPAGO", -- VARCHAR(10)
                  "IdMonedaLiquidacion"  AS "IDMONEDALIQUIDACION", -- VARCHAR(3)
                  "NombreMonedaLiquidacion"  AS "NOMBREMONEDALIQUIDACION", -- VARCHAR(100)
                  "Factor"  AS "FACTOR", -- DECIMAL
                  "Valor"  AS "VALOR", -- DECIMAL
                  "IdEmisorNuevo"  AS "IDEMISORNUEVO", -- VARCHAR(40)
                  "IdInstrumentoNuevo"  AS "IDINSTRUMENTONUEVO", -- BIGINT
                  "NemoNuevo"  AS "NEMONUEVO", -- VARCHAR(40)
                  "FactorNuevo"  AS "FACTORNUEVO", -- DECIMAL
                  "IdTipoDividendo"  AS "IDTIPODIVIDENDO", -- VARCHAR(2)
                  "NumeroDividendo"  AS "NUMERODIVIDENDO", -- BIGINT
                  "EmisorSorteoLetras"  AS "EMISORSORTEOLETRAS", -- DECIMAL
                  "InstrumentoSorteoLetras"  AS "INSTRUMENTOSORTEOLETRAS", -- DECIMAL
                  "IdMonedaSorteoLetras"  AS "IDMONEDASORTEOLETRAS", -- VARCHAR(3)
                  "BloqueoSorteoLetras"  AS "BLOQUEOSORTEOLETRAS", -- DECIMAL
                  "SorteadoSorteoLetras"  AS "SORTEADOSORTEOLETRAS", -- DECIMAL
                  "APagarSorteoLetras"  AS "APAGARSORTEOLETRAS", -- DECIMAL
                  "APagarPesosSorteoLetras"  AS "APAGARPESOSSORTEOLETRAS", -- DECIMAL
                  "CuentaDepositanteSorteoLetras"  AS "CUENTADEPOSITANTESORTEOLETRAS", -- DECIMAL
                  "GlosaMonedaSorteoLetras"  AS "GLOSAMONEDASORTEOLETRAS", -- VARCHAR(10)
                  "EmisorPagadorSorteoLetras"  AS "EMISORPAGADORSORTEOLETRAS", -- DECIMAL
                  "GlosaInstrumentoSorteoLetras"  AS "GLOSAINSTRUMENTOSORTEOLETRAS", -- VARCHAR(10)
                  "Origen"  AS "ORIGEN", -- VARCHAR(3)
                  "TipoIngreso"  AS "TIPOINGRESO", -- VARCHAR(1)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
				  "ImpuestoDivExtranjero" as "IMPUESTODIVEXTRANJERO", -- DECIMAL
				  "GastoTransferencia" as "GASTOTRANSFERENCIA" -- DECIMAL

        from "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Custodia_EventoCapital' AND WST."Key" = "IdEventoCapital"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_EventoCapital" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

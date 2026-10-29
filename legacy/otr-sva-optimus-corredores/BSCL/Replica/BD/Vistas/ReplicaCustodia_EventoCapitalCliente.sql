SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_EventoCapitalCliente"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_EventoCapitalCliente" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdEventoCapitalCliente"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEventoCapitalCliente"  AS "IDEVENTOCAPITALCLIENTE", -- BIGINT
                  "FechaHoraIngresoRegistro"  AS "FECHAHORAINGRESOREGISTRO", -- VARCHAR(27)
                  "FechaHoraUltimaActualizacion"  AS "FECHAHORAULTIMAACTUALIZACION", -- VARCHAR(27)
                  "IdEventoCapital"  AS "IDEVENTOCAPITAL", -- BIGINT
                  "IdTipoEventoCapital"  AS "IDTIPOEVENTOCAPITAL", -- VARCHAR(2)
                  "NombreEventoCapital"  AS "NOMBREEVENTOCAPITAL", -- VARCHAR(100)
                  "Secuencia"  AS "SECUENCIA", -- BIGINT
                  "FechaCierre"  AS "FECHACIERRE", -- VARCHAR(10)
                  "FechaPago"  AS "FECHAPAGO", -- VARCHAR(10)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "IdTipoOperacionGarantia"  AS "IDTIPOOPERACIONGARANTIA", -- VARCHAR(6)
                  "Origen"  AS "ORIGEN", -- VARCHAR(3)
                  "Descripcion"  AS "DESCRIPCION", -- VARCHAR(100)
                  "Proposito"  AS "PROPOSITO", -- VARCHAR(520)
                  "IdSocioNegocio"  AS "IDSOCIONEGOCIO", -- BIGINT
                  "TipoSocioNegocio"  AS "TIPOSOCIONEGOCIO", -- VARCHAR(3)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "IdTipoDocumentoIdentidad"  AS "IDTIPODOCUMENTOIDENTIDAD", -- VARCHAR(3)
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "DescripcionPortafolio"  AS "DESCRIPCIONPORTAFOLIO", -- VARCHAR(100)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemo"  AS "NEMO", -- VARCHAR(40)
                  "IdInstrumentoNuevo"  AS "IDINSTRUMENTONUEVO", -- BIGINT
                  "NemoNuevo"  AS "NEMONUEVO", -- VARCHAR(40)
                  "TipoCartera"  AS "TIPOCARTERA", -- VARCHAR(3)
                  "IdBoveda"  AS "IDBOVEDA", -- VARCHAR(3)
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "MontoReajuste"  AS "MONTOREAJUSTE", -- DECIMAL
                  "Precio"  AS "PRECIO", -- DECIMAL
                  "PrecioNuevo"  AS "PRECIONUEVO", -- DECIMAL
                  "NumeroDividendo"  AS "NUMERODIVIDENDO", -- BIGINT
                  "Paridad"  AS "PARIDAD", -- DECIMAL
                  "Factor"  AS "FACTOR", -- DECIMAL
                  "Valor"  AS "VALOR", -- DECIMAL
                  "CantidadDerecho"  AS "CANTIDADDERECHO", -- BIGINT
                  "CantidadNueva"  AS "CANTIDADNUEVA", -- BIGINT
                  "MontoCapital"  AS "MONTOCAPITAL", -- DECIMAL
                  "MontoInteres"  AS "MONTOINTERES", -- DECIMAL
                  "Total"  AS "TOTAL", -- DECIMAL
                  "SecuenciaCupon"  AS "SECUENCIACUPON", -- BIGINT
                  "IdConceptoGarantia"  AS "IDCONCEPTOGARANTIA", -- VARCHAR(6)
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
                  "MonedaEvento"  AS "MONEDAEVENTO", -- VARCHAR(100)
                  "CodigoUsuarioAgente"  AS "CODIGOUSUARIOAGENTE", -- VARCHAR(40)
                  "NombreAgente"  AS "NOMBREAGENTE", -- VARCHAR(302)
                  "CodigoSucursal"  AS "CODIGOSUCURSAL", -- VARCHAR(40)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
				  "MontoGastoTransferencia" AS "MONTOGASTOTRANSFERENCIA", -- DECIMAL
				  "ImpuestoDivExtranjero" AS "IMPUESTODIVEXTRANJERO", -- DECIMAL
				  "MontoImpuestoExtranjero" AS "MONTOIMPUESTOEXTRANJERO" -- DECIMAL

        from "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Custodia_EventoCapitalCliente' AND WST."Key" = "IdEventoCapitalCliente"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_EventoCapitalCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_Comprobante"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_Comprobante" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdComprobante"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "TipoComprobante"  AS "TIPOCOMPROBANTE", -- VARCHAR(7)
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "IdCaja"  AS "IDCAJA", -- BIGINT
                  "CodigoCaja"  AS "CODIGOCAJA", -- VARCHAR(40)
                  "NombreCaja"  AS "NOMBRECAJA", -- VARCHAR(100)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "DecimalesMoneda"  AS "DECIMALESMONEDA", -- BIGINT
                  "NombreMoneda"  AS "NOMBREMONEDA", -- VARCHAR(100)
                  "Monto"  AS "MONTO", -- DECIMAL
                  "MontoEgreso"  AS "MONTOEGRESO", -- DECIMAL
                  "MontoIngreso"  AS "MONTOINGRESO", -- DECIMAL
                  "MontoConSigno"  AS "MONTOCONSIGNO", -- DECIMAL
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdTipoPersonaTitular"  AS "IDTIPOPERSONATITULAR", -- VARCHAR(3)
                  "IdPersonaTitular"  AS "IDPERSONATITULAR", -- BIGINT
                  "IdTipoDocumentoIdentidadTitular"  AS "IDTIPODOCUMENTOIDENTIDADTITULAR", -- VARCHAR(40)
                  "DocumentoIdentidadTitular"  AS "DOCUMENTOIDENTIDADTITULAR", -- VARCHAR(40)
                  "NombrePersonaTitular"  AS "NOMBREPERSONATITULAR", -- VARCHAR(302)
                  "IdPortafolioTitular"  AS "IDPORTAFOLIOTITULAR", -- BIGINT
                  "CodigoPortafolioPersona"  AS "CODIGOPORTAFOLIOPERSONA", -- VARCHAR(100)
                  "NombrePortafolioPersona"  AS "NOMBREPORTAFOLIOPERSONA", -- VARCHAR(100)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdTipoPersona"  AS "IDTIPOPERSONA", -- VARCHAR(3)
                  "TipoPersona"  AS "TIPOPERSONA", -- VARCHAR(40)
                  "NombrePersona"  AS "NOMBREPERSONA", -- VARCHAR(302)
                  "IdTipoDocumentoIdentidadPersona"  AS "IDTIPODOCUMENTOIDENTIDADPERSONA", -- VARCHAR(3)
                  "DocumentoIdentidadPersona"  AS "DOCUMENTOIDENTIDADPERSONA", -- VARCHAR(40)
                  "SecuenciaAgrupacion"  AS "SECUENCIAAGRUPACION", -- VARCHAR(40)
                  "EsBilateral"  AS "ESBILATERAL", -- VARCHAR(1)
                  "ComprobanteNeteado"  AS "COMPROBANTENETEADO", -- VARCHAR(1)
                  "SeparadoPorConcepto"  AS "SEPARADOPORCONCEPTO", -- VARCHAR(1)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdMedioDePago"  AS "IDMEDIODEPAGO", -- VARCHAR(3)
                  "NombreMedioDePago"  AS "NOMBREMEDIODEPAGO", -- VARCHAR(100)
                  "IdModalidadDePago"  AS "IDMODALIDADDEPAGO", -- VARCHAR(6)
                  "NombreModalidadDePago"  AS "NOMBREMODALIDADDEPAGO", -- VARCHAR(100)
                  "NombreUsuarioLiquidacion"  AS "NOMBREUSUARIOLIQUIDACION", -- VARCHAR(302)
                  "IdUsuarioAnulacion"  AS "IDUSUARIOANULACION", -- BIGINT
                  "CodigoUsuarioAnulacion"  AS "CODIGOUSUARIOANULACION", -- VARCHAR(40)
                  "FechaHoraAnulacion"  AS "FECHAHORAANULACION", -- VARCHAR(19)
                  "NombreEmpresa"  AS "NOMBREEMPRESA", -- VARCHAR(100)
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
                  "CodigoUsuario"  AS "CODIGOUSUARIO" -- VARCHAR(40)

        from "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Tesoreria_Comprobante' AND WST."Key" = "IdComprobante"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTesoreria_Comprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroOrden"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroOrden" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdCobroOrden"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCobroOrden"  AS "IDCOBROORDEN", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdOrden"  AS "IDORDEN", -- BIGINT
                  "IdGrupoCobro"  AS "IDGRUPOCOBRO", -- BIGINT
                  "IdCobro"  AS "IDCOBRO", -- VARCHAR(40)
                  "NombreCobro"  AS "NOMBRECOBRO", -- VARCHAR(100)
                  "CodigoCobroClasificacion"  AS "CODIGOCOBROCLASIFICACION", -- VARCHAR(40)
                  "ValorCobroPerfil"  AS "VALORCOBROPERFIL", -- DECIMAL
                  "ValorCobroAplicado"  AS "VALORCOBROAPLICADO", -- DECIMAL
                  "MontoCobroCalculado"  AS "MONTOCOBROCALCULADO", -- DECIMAL
                  "MontoCobroCalculadoLocal"  AS "MONTOCOBROCALCULADOLOCAL", -- DECIMAL
                  "MontoCobroCalculadoLiquidacion"  AS "MONTOCOBROCALCULADOLIQUIDACION", -- DECIMAL
                  "FormaCobro"  AS "FORMACOBRO", -- VARCHAR(100)
                  "EsComprobante"  AS "ESCOMPROBANTE", -- VARCHAR(3)
                  "FechaIngresoCobro"  AS "FECHAINGRESOCOBRO", -- VARCHAR(27)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "MontoMinimo"  AS "MONTOMINIMO", -- DECIMAL
                  "IdMoneda"  AS "IDMONEDA" -- VARCHAR(3)

        from "BSCL.Orden.BD.Vistas::ConsultarCobroOrdenAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Orden_CobroOrden' AND WST."Key" = "IdCobroOrden"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

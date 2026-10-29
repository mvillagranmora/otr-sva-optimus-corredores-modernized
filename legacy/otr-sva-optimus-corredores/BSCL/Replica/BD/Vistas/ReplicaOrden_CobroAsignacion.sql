SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroAsignacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroAsignacion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdCobroAsignacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCobroAsignacion"  AS "IDCOBROASIGNACION", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdAsignacion"  AS "IDASIGNACION", -- BIGINT
                  "IdCobro"  AS "IDCOBRO", -- VARCHAR(40)
                  "NombreCobro"  AS "NOMBRECOBRO", -- VARCHAR(100)
                  "FechaIngresoCobro"  AS "FECHAINGRESOCOBRO", -- VARCHAR(27)
                  "CodigoCobroClasificacion"  AS "CODIGOCOBROCLASIFICACION", -- VARCHAR(40)
                  "ValorCobroPerfil"  AS "VALORCOBROPERFIL", -- DECIMAL
                  "ValorCobroAplicado"  AS "VALORCOBROAPLICADO", -- DECIMAL
                  "MontoCobroCalculado"  AS "MONTOCOBROCALCULADO", -- DECIMAL
                  "MontoCobroCalculadoLocal"  AS "MONTOCOBROCALCULADOLOCAL", -- DECIMAL
                  "MontoCobroCalculadoLiquidacion"  AS "MONTOCOBROCALCULADOLIQUIDACION", -- DECIMAL
                  "FormaCobro"  AS "FORMACOBRO", -- VARCHAR(40)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "IdMoneda"  AS "IDMONEDA" -- VARCHAR(3)

        from  "BSCL.Orden.BD.Vistas::ConsultarCobroAsignacionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Orden_CobroAsignacion' AND WST."Key" = "IdCobroAsignacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroAsignacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

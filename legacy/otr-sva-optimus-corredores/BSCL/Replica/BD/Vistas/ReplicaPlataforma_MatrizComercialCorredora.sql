SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialCorredora"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialCorredora" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPerfilComercialBase"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdPerfilComercialBase"  AS "IDPERFILCOMERCIALBASE", -- VARCHAR(40)
                  "DescripcionPerfilComercialBase"  AS "DESCRIPCIONPERFILCOMERCIALBASE", -- VARCHAR(100)
                  "IdCategoriaInversionista"  AS "IDCATEGORIAINVERSIONISTA", -- VARCHAR(40)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "TipoProducto"  AS "TIPOPRODUCTO", -- VARCHAR(100)
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "TipoOperacion"  AS "TIPOOPERACION", -- VARCHAR(100)
                  "IdCanalIngreso"  AS "IDCANALINGRESO", -- VARCHAR(3)
                  "CanalIngreso"  AS "CANALINGRESO", -- VARCHAR(100)
                  "IdCobroClasificacion"  AS "IDCOBROCLASIFICACION", -- BIGINT
                  "CodigoCobroClasificacion"  AS "CODIGOCOBROCLASIFICACION", -- VARCHAR(40)
                  "IdCobro"  AS "IDCOBRO", -- VARCHAR(10)
                  "NombreCobro"  AS "NOMBRECOBRO", -- VARCHAR(100)
                  "ValorCobro"  AS "VALORCOBRO", -- DECIMAL
                  "FormaCobro"  AS "FORMACOBRO", -- VARCHAR(1)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "ValorTopeMinimoOrden"  AS "VALORTOPEMINIMOORDEN", -- DECIMAL
                  "Prioridad"  AS "PRIORIDAD", -- BIGINT
                  "DiasPlazo"  AS "DIASPLAZO" -- BIGINT

        from "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialCorredoraAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_MatrizComercialCorredora' AND WST."Key" = "IdPerfilComercialBase"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialPortafolios"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialPortafolios"  AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPortafolio" AS   TABLE_KEY, --||':'|| "IdTipoProducto" || ':' || "IdTipoOperacion" || ':' || "IdCanalIngreso"  || ':' || "IdMoneda" || ':' || "IdCobro" || ':' || "IdValorCobro" AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "IdCategoriaInversionista"  AS "IDCATEGORIAINVERSIONISTA", -- VARCHAR(40)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "TipoProducto"  AS "TIPOPRODUCTO", -- VARCHAR(100)
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "EsExtraCobros"  AS "ESEXTRACOBROS", -- VARCHAR(1)
                  "EsExtraProductos"  AS "ESEXTRAPRODUCTOS", -- VARCHAR(1)
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
                  "DiasPlazo"  AS "DIASPLAZO", -- BIGINT
                  "IdValorCobro"  AS "IDVALORCOBRO" -- VARCHAR(40)
        from "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialPortafoliosAnalitica"
	        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
	        ON  WST."Table" = 'Plataforma_MatrizComercialPortafolios' 
    	    AND WST."Key" = "IdPortafolio" --("IdPortafolio" ||':'|| "IdTipoProducto" || ':' || "IdTipoOperacion" || ':' || "IdCanalIngreso"  || ':' || "IdMoneda" || ':' || "IdCobro" || ':' || "IdValorCobro")
        --AND WST."Key" = "IdPortafolio"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_MatrizComercialPortafolios" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

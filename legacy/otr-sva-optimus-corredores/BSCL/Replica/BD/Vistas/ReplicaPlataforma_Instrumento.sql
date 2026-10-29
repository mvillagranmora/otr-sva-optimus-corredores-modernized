SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Instrumento"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Instrumento" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdInstrumento"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "ISIN"  AS "ISIN", -- VARCHAR(40)
                  "CorteMinimo"  AS "CORTEMINIMO", -- DECIMAL
                  "EstaInscrito"  AS "ESTAINSCRITO", -- VARCHAR(1)
                  "EsDivisible"  AS "ESDIVISIBLE", -- VARCHAR(1)
                  "RazonSocial"  AS "RAZONSOCIAL", -- VARCHAR(100)
                  "FechaEmision"  AS "FECHAEMISION", -- VARCHAR(10)
                  "FechaVencimiento"  AS "FECHAVENCIMIENTO", -- VARCHAR(10)
                  "IdInstrumentoRelacionado"  AS "IDINSTRUMENTORELACIONADO", -- BIGINT
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "IdTipoReajuste"  AS "IDTIPOREAJUSTE", -- VARCHAR(3)
                  "Reajustable"  AS "REAJUSTABLE", -- VARCHAR(1)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "DecimalesPrecio"  AS "DECIMALESPRECIO", -- BIGINT
                  "Tamano"  AS "TAMANO", -- BIGINT
                  "FechaInicioVigencia"  AS "FECHAINICIOVIGENCIA", -- VARCHAR(10)
                  "TasaFiscal"  AS "TASAFISCAL", -- DECIMAL
                  "IndArt104"  AS "INDART104", -- VARCHAR(1)
                  "TipoValorizacion"  AS "TIPOVALORIZACION", -- VARCHAR(10)
                  "Tasa"  AS "TASA", -- DECIMAL
                  "Precio"  AS "PRECIO", -- DECIMAL
                  "ValorizaPor"  AS "VALORIZAPOR", -- VARCHAR(1)
                  "TieneCupon"  AS "TIENECUPON", -- VARCHAR(1)
                  "IdPaisEmisor"  AS "IDPAISEMISOR", -- BIGINT
                  "IndicadorLiquidaDCV"  AS "INDICADORLIQUIDADCV", -- VARCHAR(1)
                  "Serie"  AS "SERIE", -- VARCHAR(40)
                  "IndicadorBolson"  AS "INDICADORBOLSON", -- VARCHAR(1)
                  "FrecuenciaCupon"  AS "FRECUENCIACUPON", -- DECIMAL
                  "PrecioReferencia"  AS "PRECIOREFERENCIA", -- DECIMAL
                  "FechaReferencia"  AS "FECHAREFERENCIA", -- VARCHAR(10)
                  "NumeroCupones"  AS "NUMEROCUPONES", -- BIGINT
                  "CodigoRubro"  AS "CODIGORUBRO", -- BIGINT
                  "NombreCodigoRubro"  AS "NOMBRECODIGORUBRO", -- VARCHAR(500)
                  "ClasificacionRiesgoInstrumento"  AS "CLASIFICACIONRIESGOINSTRUMENTO", -- VARCHAR(20)
                  "PresenciaBursatilAjustada"  AS "PRESENCIABURSATILAJUSTADA", -- INT
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
				  "IdInstrumentoRaiz"  AS "IDINSTRUMENTORAIZ", -- BIGINT
                  "IdCondicionLiquidacion"  AS "IDCONDICIONLIQUIDACION" -- VARCHAR(2)

        from "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Instrumento' AND WST."Key" = "IdInstrumento"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Instrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
